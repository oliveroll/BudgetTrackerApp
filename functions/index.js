const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Send daily reminders for bills and subscriptions
 * Scheduled to run every day at 9:00 AM
 */
exports.sendDailyReminders = functions.pubsub
  .schedule('0 9 * * *') // Cron: 9 AM every day
  .timeZone('America/New_York') // Change to your timezone
  .onRun(async (context) => {
    const db = admin.firestore();
    const today = new Date();
    const todayDay = today.getDate();
    
    console.log(`Running daily reminders check at ${today.toISOString()}`);
    
    try {
      // Get all users
      const usersSnapshot = await db.collection('users').get();
      console.log(`Found ${usersSnapshot.size} users to check`);
      
      let notificationsSent = 0;
      
      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const fcmToken = userDoc.data().fcmToken;
        
        if (!fcmToken) {
          console.log(`User ${userId} has no FCM token, skipping`);
          continue;
        }
        
        // Check Essential Expenses (Fixed expenses with due dates)
        const expensesSnapshot = await db
          .collection('essentialExpenses')
          .where('userId', '==', userId)
          .where('paid', '==', false)
          .get();
        
        console.log(`User ${userId}: Found ${expensesSnapshot.size} unpaid expenses`);
        
        for (const expenseDoc of expensesSnapshot.docs) {
          const expense = expenseDoc.data();
          const dueDay = expense.dueDay;
          
          if (dueDay && dueDay === todayDay) {
            // Send notification for expense due today
            const message = {
              token: fcmToken,
              notification: {
                title: `${expense.name} is due today!`,
                body: `$${expense.plannedAmount.toFixed(2)} - Don't forget to pay`
              },
              data: {
                type: expense.category ? expense.category.toLowerCase() : 'expense',
                amount: expense.plannedAmount.toString(),
                itemId: expenseDoc.id,
                screen: 'budget'
              },
              android: {
                priority: 'high',
                notification: {
                  channelId: 'budget_reminders',
                  priority: 'high',
                  defaultVibrateTimings: true
                }
              }
            };
            
            try {
              await admin.messaging().send(message);
              notificationsSent++;
              console.log(`Sent reminder for ${expense.name} to user ${userId}`);
            } catch (error) {
              console.error(`Failed to send notification to ${userId}:`, error.message);
              
              // If token is invalid, remove it
              if (error.code === 'messaging/invalid-registration-token' ||
                  error.code === 'messaging/registration-token-not-registered') {
                await db.collection('users').doc(userId).update({ fcmToken: null });
                console.log(`Removed invalid FCM token for user ${userId}`);
              }
            }
          }
        }
        
        // Check Subscriptions (3 days before and on the day)
        const subscriptionsSnapshot = await db
          .collection('subscriptions')
          .where('userId', '==', userId)
          .where('active', '==', true)
          .get();
        
        console.log(`User ${userId}: Found ${subscriptionsSnapshot.size} active subscriptions`);
        
        for (const subDoc of subscriptionsSnapshot.docs) {
          const subscription = subDoc.data();
          const nextBilling = new Date(subscription.nextBillingDate);
          const daysUntil = Math.ceil((nextBilling - today) / (1000 * 60 * 60 * 24));
          
          // Send reminder 3 days before or on the day
          if (daysUntil === 3 || daysUntil === 0) {
            const reminderText = daysUntil === 0 ? 'renews today' : 'renews in 3 days';
            
            const message = {
              token: fcmToken,
              notification: {
                title: `${subscription.name} ${reminderText}`,
                body: `$${subscription.amount.toFixed(2)} - ${subscription.frequency || 'Monthly'}`
              },
              data: {
                type: 'subscription',
                amount: subscription.amount.toString(),
                itemId: subDoc.id,
                screen: 'budget',
                daysUntil: daysUntil.toString()
              },
              android: {
                priority: 'high',
                notification: {
                  channelId: 'budget_reminders',
                  priority: 'high',
                  defaultVibrateTimings: true
                }
              }
            };
            
            try {
              await admin.messaging().send(message);
              notificationsSent++;
              console.log(`Sent subscription reminder for ${subscription.name} to user ${userId}`);
            } catch (error) {
              console.error(`Failed to send subscription notification to ${userId}:`, error.message);
              
              // If token is invalid, remove it
              if (error.code === 'messaging/invalid-registration-token' ||
                  error.code === 'messaging/registration-token-not-registered') {
                await db.collection('users').doc(userId).update({ fcmToken: null });
                console.log(`Removed invalid FCM token for user ${userId}`);
              }
            }
          }
        }
      }
      
      console.log(`Daily reminders completed. Sent ${notificationsSent} notifications.`);
      return { success: true, notificationsSent };
      
    } catch (error) {
      console.error('Error in sendDailyReminders:', error);
      throw error;
    }
  });

/**
 * Send test notification (callable function for testing)
 */
exports.sendTestNotification = functions.https.onCall(async (data, context) => {
  // Check authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to send test notification'
    );
  }
  
  const userId = context.auth.uid;
  
  try {
    // Get user's FCM token
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    const fcmToken = userDoc.data()?.fcmToken;
    
    if (!fcmToken) {
      throw new functions.https.HttpsError(
        'not-found',
        'FCM token not found for user'
      );
    }
    
    // Send test notification
    const message = {
      token: fcmToken,
      notification: {
        title: '🎉 Test Notification',
        body: 'Budget Tracker notifications are working! You will receive reminders for bills and subscriptions.'
      },
      data: {
        type: 'test',
        screen: 'dashboard'
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'budget_reminders',
          priority: 'high',
          defaultVibrateTimings: true
        }
      }
    };
    
    await admin.messaging().send(message);
    console.log(`Test notification sent to user ${userId}`);
    
    return { success: true, message: 'Test notification sent successfully' };
    
  } catch (error) {
    console.error('Error sending test notification:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Failed to send test notification',
      error.message
    );
  }
});

/**
 * Send immediate reminder for a specific expense (callable function)
 */
exports.sendExpenseReminder = functions.https.onCall(async (data, context) => {
  // Check authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated'
    );
  }
  
  const userId = context.auth.uid;
  const { expenseId, expenseName, amount } = data;
  
  if (!expenseId || !expenseName || !amount) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'Missing required fields: expenseId, expenseName, amount'
    );
  }
  
  try {
    // Get user's FCM token
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    const fcmToken = userDoc.data()?.fcmToken;
    
    if (!fcmToken) {
      throw new functions.https.HttpsError(
        'not-found',
        'FCM token not found for user'
      );
    }
    
    // Send reminder
    const message = {
      token: fcmToken,
      notification: {
        title: `Reminder: ${expenseName}`,
        body: `$${parseFloat(amount).toFixed(2)} - Payment reminder`
      },
      data: {
        type: 'expense_reminder',
        amount: amount.toString(),
        itemId: expenseId,
        screen: 'budget'
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'budget_reminders',
          priority: 'high',
          defaultVibrateTimings: true
        }
      }
    };
    
    await admin.messaging().send(message);
    console.log(`Manual expense reminder sent to user ${userId} for ${expenseName}`);
    
    return { success: true, message: 'Reminder sent successfully' };
    
  } catch (error) {
    console.error('Error sending expense reminder:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Failed to send reminder',
      error.message
    );
  }
});

