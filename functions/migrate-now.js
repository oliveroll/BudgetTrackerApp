/**
 * INSTANT DATA MIGRATION
 * 
 * Run this script to migrate all your transactions from root collection
 * to user subcollections RIGHT NOW.
 * 
 * Usage:
 *   cd functions
 *   npm install
 *   export GOOGLE_APPLICATION_CREDENTIALS="../serviceAccountKey.json"
 *   node migrate-now.js
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin
const serviceAccount = require('../budget-tracker-app-oliver-firebase-adminsdk-fbsvc-566f16bb45.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const userId = 'WYe3hhJhPbag2uRbR7p56KvO4u92'; // Oliver's user ID

async function migrateTransactions() {
    console.log('\n🚀 Starting transaction migration...\n');
    
    try {
        // Get all transactions from legacy root collection
        const snapshot = await db.collection('transactions')
            .where('userId', '==', userId)
            .get();
        
        console.log(`📊 Found ${snapshot.size} transactions to migrate\n`);
        
        let migrated = 0;
        let errors = 0;
        
        // Batch operations for efficiency
        const batchSize = 500; // Firestore batch limit
        let batch = db.batch();
        let operationCount = 0;
        
        for (const doc of snapshot.docs) {
            try {
                const data = doc.data();
                
                // Write to NEW user subcollection
                const newRef = db.collection('users')
                    .doc(userId)
                    .collection('transactions')
                    .doc(doc.id);
                
                batch.set(newRef, data);
                operationCount++;
                
                // Delete from OLD root collection
                batch.delete(doc.ref);
                operationCount++;
                
                // Commit batch if we hit the limit
                if (operationCount >= batchSize) {
                    await batch.commit();
                    console.log(`✓ Batch committed: ${operationCount / 2} transactions`);
                    batch = db.batch();
                    operationCount = 0;
                }
                
                migrated++;
                
                if (migrated % 10 === 0) {
                    process.stdout.write(`\r✓ Migrated ${migrated}/${snapshot.size} transactions`);
                }
                
            } catch (error) {
                console.error(`\n❌ Error migrating transaction ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        // Commit remaining batch
        if (operationCount > 0) {
            await batch.commit();
            console.log(`\n✓ Final batch committed: ${operationCount / 2} transactions`);
        }
        
        console.log(`\n\n✅ Migration complete!`);
        console.log(`   • Migrated: ${migrated} transactions`);
        console.log(`   • Errors: ${errors}`);
        console.log(`   • New location: /users/${userId}/transactions`);
        console.log(`   • Old location: /transactions (cleaned up)\n`);
        
        // Verify migration
        const newSnapshot = await db.collection('users')
            .doc(userId)
            .collection('transactions')
            .get();
        
        console.log(`\n🔍 Verification:`);
        console.log(`   • Transactions in new location: ${newSnapshot.size}`);
        
        const oldSnapshot = await db.collection('transactions')
            .where('userId', '==', userId)
            .get();
        
        console.log(`   • Transactions in old location: ${oldSnapshot.size}`);
        
        if (newSnapshot.size === migrated && oldSnapshot.size === 0) {
            console.log(`\n✅ ✅ ✅ MIGRATION SUCCESSFUL! ✅ ✅ ✅\n`);
        } else {
            console.log(`\n⚠️ Verification mismatch - please check Firebase Console\n`);
        }
        
    } catch (error) {
        console.error('\n❌ Migration failed:', error);
        process.exit(1);
    }
}

// Run migration
console.log('╔════════════════════════════════════════════════╗');
console.log('║  Budget Tracker - Data Migration Script       ║');
console.log('║  Migrating transactions to user subcollections ║');
console.log('╚════════════════════════════════════════════════╝');

migrateTransactions()
    .then(() => {
        console.log('🎉 All done! Check Firebase Console to see your new structure.');
        process.exit(0);
    })
    .catch((error) => {
        console.error('💥 Fatal error:', error);
        process.exit(1);
    });

