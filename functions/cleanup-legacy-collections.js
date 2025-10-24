/**
 * CLEANUP LEGACY ROOT COLLECTIONS
 * 
 * Deletes obsolete root-level collections that are no longer used.
 * All data has been migrated to user subcollections.
 * 
 * Usage:
 *   cd functions
 *   node cleanup-legacy-collections.js
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin (reuse existing app if already initialized)
if (!admin.apps.length) {
    const serviceAccount = require('../budget-tracker-app-oliver-firebase-adminsdk-fbsvc-566f16bb45.json');
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

const db = admin.firestore();
const userId = 'WYe3hhJhPbag2uRbR7p56KvO4u92';

/**
 * Delete all documents in a collection for a specific user
 */
async function deleteCollection(collectionName) {
    console.log(`\n🗑️  Deleting ${collectionName}...`);
    
    try {
        // Get all documents for this user
        const snapshot = await db.collection(collectionName)
            .where('userId', '==', userId)
            .get();
        
        console.log(`   Found ${snapshot.size} documents to delete`);
        
        if (snapshot.size === 0) {
            console.log(`   ✓ Collection already clean`);
            return { deleted: 0, errors: 0 };
        }
        
        let deleted = 0;
        let errors = 0;
        
        // Batch delete
        const batchSize = 500;
        let batch = db.batch();
        let operationCount = 0;
        
        for (const doc of snapshot.docs) {
            try {
                batch.delete(doc.ref);
                operationCount++;
                
                if (operationCount >= batchSize) {
                    await batch.commit();
                    deleted += operationCount;
                    console.log(`   ✓ Batch deleted: ${operationCount} documents`);
                    batch = db.batch();
                    operationCount = 0;
                }
            } catch (error) {
                console.error(`   ❌ Error deleting ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        // Commit remaining batch
        if (operationCount > 0) {
            await batch.commit();
            deleted += operationCount;
            console.log(`   ✓ Final batch: ${operationCount} documents`);
        }
        
        console.log(`   ✅ Deleted ${deleted} documents from ${collectionName}`);
        
        return { deleted, errors };
        
    } catch (error) {
        console.error(`   ❌ Failed to delete ${collectionName}:`, error.message);
        return { deleted: 0, errors: 1 };
    }
}

/**
 * Main cleanup function
 */
async function cleanupLegacyCollections() {
    console.log('╔════════════════════════════════════════════════╗');
    console.log('║  Budget Tracker - Legacy Collection Cleanup   ║');
    console.log('║  Removing obsolete root-level collections     ║');
    console.log('╚════════════════════════════════════════════════╝');
    
    console.log('\n📋 Collections to clean:');
    console.log('   1. fixedExpenses');
    console.log('   2. incomeSources');
    console.log('   3. variableExpenseCategories');
    console.log('   4. transactions (if any remain)');
    
    console.log('\n⚠️  Note: Only YOUR data will be deleted (userId: ' + userId + ')');
    console.log('   Other users\' data (if any) will remain untouched.\n');
    
    const results = {
        fixedExpenses: { deleted: 0, errors: 0 },
        incomeSources: { deleted: 0, errors: 0 },
        variableExpenseCategories: { deleted: 0, errors: 0 },
        transactions: { deleted: 0, errors: 0 }
    };
    
    try {
        // Clean up each collection
        results.fixedExpenses = await deleteCollection('fixedExpenses');
        results.incomeSources = await deleteCollection('incomeSources');
        results.variableExpenseCategories = await deleteCollection('variableExpenseCategories');
        results.transactions = await deleteCollection('transactions');
        
        // Summary
        console.log('\n════════════════════════════════════════════════');
        console.log('✅ CLEANUP COMPLETE!');
        console.log('════════════════════════════════════════════════');
        
        const totalDeleted = Object.values(results).reduce((sum, r) => sum + r.deleted, 0);
        const totalErrors = Object.values(results).reduce((sum, r) => sum + r.errors, 0);
        
        console.log(`\n📊 Summary:`);
        console.log(`   • fixedExpenses: ${results.fixedExpenses.deleted} deleted`);
        console.log(`   • incomeSources: ${results.incomeSources.deleted} deleted`);
        console.log(`   • variableExpenseCategories: ${results.variableExpenseCategories.deleted} deleted`);
        console.log(`   • transactions: ${results.transactions.deleted} deleted`);
        console.log(`\n   Total: ${totalDeleted} documents deleted`);
        console.log(`   Errors: ${totalErrors}`);
        
        if (totalDeleted > 0) {
            console.log('\n🔥 Legacy collections cleaned up!');
            console.log('   Your app now uses ONLY user subcollections.');
            console.log('   Refresh Firebase Console to see the clean structure.\n');
        } else {
            console.log('\n✨ Collections were already clean!\n');
        }
        
    } catch (error) {
        console.error('\n💥 Fatal error:', error);
        process.exit(1);
    }
}

// Run cleanup
cleanupLegacyCollections()
    .then(() => {
        console.log('✅ Cleanup script completed successfully!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('💥 Cleanup script failed:', error);
        process.exit(1);
    });

