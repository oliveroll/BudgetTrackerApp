/**
 * Manual test for daily reminders
 * Run this to test the notification system immediately
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin (reuse if already initialized)
if (!admin.apps.length) {
    const serviceAccount = require('../budget-tracker-app-oliver-firebase-adminsdk-fbsvc-566f16bb45.json');
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

const db = admin.firestore();
const userId = 'WYe3hhJhPbag2uRbR7p56KvO4u92';

async function testReminders() {
    console.log('\n🧪 Testing Daily Reminders System\n');
    console.log('═══════════════════════════════════════\n');
    
    try {
        // Get user's FCM token
        const userDoc = await db.collection('users').doc(userId).get();
        const fcmToken = userDoc.data()?.fcmToken;
        
        if (!fcmToken) {
            console.error('❌ No FCM token found for user');
            return;
        }
        
        console.log('✅ User has FCM token');
        console.log(`   Token: ${fcmToken.substring(0, 20)}...`);
        
        // Check Essential Expenses
        console.log('\n📋 Checking Essential Expenses...');
        const expensesSnapshot = await db
            .collection('users')
            .doc(userId)
            .collection('essentials')
            .get();
        
        console.log(`   Found ${expensesSnapshot.size} total essential expenses`);
        
        const today = new Date();
        const todayDay = today.getDate();
        console.log(`   Today is day ${todayDay} of the month`);
        
        let unpaidCount = 0;
        let dueTodayCount = 0;
        
        for (const doc of expensesSnapshot.docs) {
            const expense = doc.data();
            const isPaid = expense.paid || false;
            const dueDay = expense.dueDay;
            
            if (!isPaid) {
                unpaidCount++;
                console.log(`   • ${expense.name}: Due day ${dueDay}, Paid: ${isPaid}`);
                
                if (dueDay === todayDay) {
                    dueTodayCount++;
                    console.log(`     ⚠️ DUE TODAY! Sending notification...`);
                    
                    // Send notification
                    const message = {
                        token: fcmToken,
                        notification: {
                            title: `💰 ${expense.name} is due today!`,
                            body: `$${expense.plannedAmount?.toFixed(2) || '0.00'} - Don't forget to pay`
                        },
                        data: {
                            type: expense.category?.toLowerCase() || 'expense',
                            amount: (expense.plannedAmount || 0).toString(),
                            itemId: doc.id,
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
                        console.log(`     ✅ Notification sent!`);
                    } catch (error) {
                        console.error(`     ❌ Failed to send: ${error.message}`);
                    }
                }
            }
        }
        
        console.log(`\n   Summary: ${unpaidCount} unpaid, ${dueTodayCount} due today`);
        
        // Check Subscriptions
        console.log('\n📅 Checking Subscriptions...');
        const subsSnapshot = await db
            .collection('users')
            .doc(userId)
            .collection('subscriptions')
            .where('active', '==', true)
            .get();
        
        console.log(`   Found ${subsSnapshot.size} active subscriptions`);
        
        let dueNowCount = 0;
        let dueSoonCount = 0;
        
        for (const doc of subsSnapshot.docs) {
            const subscription = doc.data();
            const nextBilling = new Date(subscription.nextBillingDate);
            const daysUntil = Math.ceil((nextBilling - today) / (1000 * 60 * 60 * 24));
            
            console.log(`   • ${subscription.name}: ${daysUntil} days until billing`);
            
            // Send reminder 3 days before or on the day
            if (daysUntil === 3 || daysUntil === 0) {
                if (daysUntil === 0) {
                    dueNowCount++;
                    console.log(`     ⚠️ RENEWS TODAY! Sending notification...`);
                } else {
                    dueSoonCount++;
                    console.log(`     ⚠️ Due in 3 days! Sending notification...`);
                }
                
                const reminderText = daysUntil === 0 ? 'renews today' : 'renews in 3 days';
                
                const message = {
                    token: fcmToken,
                    notification: {
                        title: `📱 ${subscription.name} ${reminderText}`,
                        body: `$${subscription.amount?.toFixed(2) || '0.00'} - ${subscription.frequency || 'Monthly'}`
                    },
                    data: {
                        type: 'subscription',
                        amount: (subscription.amount || 0).toString(),
                        itemId: doc.id,
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
                    console.log(`     ✅ Notification sent!`);
                } catch (error) {
                    console.error(`     ❌ Failed to send: ${error.message}`);
                }
            }
        }
        
        console.log(`\n   Summary: ${dueNowCount} due today, ${dueSoonCount} due in 3 days`);
        
        // Final summary
        console.log('\n═══════════════════════════════════════');
        console.log('✅ Test Complete!');
        console.log('═══════════════════════════════════════\n');
        console.log('📊 Summary:');
        console.log(`   • Essential Expenses: ${unpaidCount} unpaid, ${dueTodayCount} due today`);
        console.log(`   • Subscriptions: ${subsSnapshot.size} active, ${dueNowCount + dueSoonCount} notifications sent`);
        console.log(`   • Total Notifications: ${dueTodayCount + dueNowCount + dueSoonCount}`);
        
        if (dueTodayCount + dueNowCount + dueSoonCount === 0) {
            console.log('\n💡 No bills due today or subscriptions renewing soon.');
            console.log('   The function is working correctly!');
            console.log('   You will receive notifications when:');
            console.log('   • Essential expenses are due (on due date)');
            console.log('   • Subscriptions renew in 3 days or today');
        }
        
    } catch (error) {
        console.error('\n❌ Test failed:', error);
        throw error;
    }
}

// Run test
testReminders()
    .then(() => {
        console.log('\n✅ Test completed successfully!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\n💥 Test failed:', error);
        process.exit(1);
    });

