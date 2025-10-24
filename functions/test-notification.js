const admin = require('firebase-admin');

// Initialize Firebase Admin
const serviceAccount = require('./budget-tracker-app-oliver-firebase-adminsdk-h5uu0-c2f3bb9b00.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// FCM token from Firestore
const fcmToken = 'd85LdyXRSkucKEVqBGwHED:APA91bEMfr_CR5rc56KSVamPc-NhPJXeOHZoHLRA3PTGpoUZoL5ZjU7OZzQTST_EUBNKHCAaOJpNwfuhWliuWxXq5PFLz2lGnbbAOqkYpUymHXw_VcE3alw';

// Test notification message
const message = {
  token: fcmToken,
  notification: {
    title: '🎉 Budget Tracker Test',
    body: 'Your notification system is working perfectly!'
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

// Send notification
admin.messaging().send(message)
  .then((response) => {
    console.log('✅ Notification sent successfully!');
    console.log('Response:', response);
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Error sending notification:', error);
    process.exit(1);
  });

