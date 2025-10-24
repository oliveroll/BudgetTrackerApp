/**
 * Firebase Data Migration Script
 * 
 * Purpose: Migrate data from root-level collections to user subcollections
 * 
 * BEFORE:
 * /transactions/{id} (with userId field)
 * /fixedExpenses/{id} (with userId field)
 * /incomeSources/{id} (with userId field)
 * /variableExpenseCategories/{id} (with userId field)
 * 
 * AFTER:
 * /users/{userId}/transactions/{id}
 * /users/{userId}/essentialExpenses/{id}
 * /users/{userId}/incomeSources/{id}
 * /users/{userId}/variableExpenseCategories/{id}
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin
if (!admin.apps.length) {
    admin.initializeApp();
}

const db = admin.firestore();

/**
 * Migrate transactions from root to user subcollections
 */
async function migrateTransactions() {
    console.log('\n📦 Migrating transactions...');
    
    try {
        const snapshot = await db.collection('transactions').get();
        console.log(`Found ${snapshot.size} transactions to migrate`);
        
        let migrated = 0;
        let errors = 0;
        
        for (const doc of snapshot.docs) {
            try {
                const data = doc.data();
                const userId = data.userId;
                
                if (!userId) {
                    console.warn(`⚠️ Transaction ${doc.id} has no userId, skipping`);
                    errors++;
                    continue;
                }
                
                // Copy to user subcollection
                await db.collection('users')
                    .doc(userId)
                    .collection('transactions')
                    .doc(doc.id)
                    .set(data);
                
                migrated++;
                
                if (migrated % 10 === 0) {
                    console.log(`  ✓ Migrated ${migrated}/${snapshot.size} transactions`);
                }
            } catch (error) {
                console.error(`❌ Error migrating transaction ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        console.log(`✅ Transactions migration complete: ${migrated} migrated, ${errors} errors`);
        return { migrated, errors };
    } catch (error) {
        console.error('❌ Failed to migrate transactions:', error);
        throw error;
    }
}

/**
 * Migrate fixed expenses from root to user subcollections
 */
async function migrateFixedExpenses() {
    console.log('\n📦 Migrating fixed expenses...');
    
    try {
        const snapshot = await db.collection('fixedExpenses').get();
        console.log(`Found ${snapshot.size} fixed expenses to migrate`);
        
        let migrated = 0;
        let errors = 0;
        
        for (const doc of snapshot.docs) {
            try {
                const data = doc.data();
                const userId = data.userId;
                
                if (!userId) {
                    console.warn(`⚠️ Fixed expense ${doc.id} has no userId, skipping`);
                    errors++;
                    continue;
                }
                
                // Copy to user subcollection (renamed to essentialExpenses)
                await db.collection('users')
                    .doc(userId)
                    .collection('essentialExpenses')
                    .doc(doc.id)
                    .set(data);
                
                migrated++;
            } catch (error) {
                console.error(`❌ Error migrating fixed expense ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        console.log(`✅ Fixed expenses migration complete: ${migrated} migrated, ${errors} errors`);
        return { migrated, errors };
    } catch (error) {
        console.error('❌ Failed to migrate fixed expenses:', error);
        throw error;
    }
}

/**
 * Migrate income sources from root to user subcollections
 */
async function migrateIncomeSources() {
    console.log('\n📦 Migrating income sources...');
    
    try {
        const snapshot = await db.collection('incomeSources').get();
        console.log(`Found ${snapshot.size} income sources to migrate`);
        
        let migrated = 0;
        let errors = 0;
        
        for (const doc of snapshot.docs) {
            try {
                const data = doc.data();
                const userId = data.userId;
                
                if (!userId) {
                    console.warn(`⚠️ Income source ${doc.id} has no userId, skipping`);
                    errors++;
                    continue;
                }
                
                // Copy to user subcollection
                await db.collection('users')
                    .doc(userId)
                    .collection('incomeSources')
                    .doc(doc.id)
                    .set(data);
                
                migrated++;
            } catch (error) {
                console.error(`❌ Error migrating income source ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        console.log(`✅ Income sources migration complete: ${migrated} migrated, ${errors} errors`);
        return { migrated, errors };
    } catch (error) {
        console.error('❌ Failed to migrate income sources:', error);
        throw error;
    }
}

/**
 * Migrate variable expense categories from root to user subcollections
 */
async function migrateVariableExpenseCategories() {
    console.log('\n📦 Migrating variable expense categories...');
    
    try {
        const snapshot = await db.collection('variableExpenseCategories').get();
        console.log(`Found ${snapshot.size} variable expense categories to migrate`);
        
        let migrated = 0;
        let errors = 0;
        
        for (const doc of snapshot.docs) {
            try {
                const data = doc.data();
                const userId = data.userId;
                
                if (!userId) {
                    console.warn(`⚠️ Variable expense category ${doc.id} has no userId, skipping`);
                    errors++;
                    continue;
                }
                
                // Copy to user subcollection
                await db.collection('users')
                    .doc(userId)
                    .collection('variableExpenseCategories')
                    .doc(doc.id)
                    .set(data);
                
                migrated++;
            } catch (error) {
                console.error(`❌ Error migrating variable expense category ${doc.id}:`, error.message);
                errors++;
            }
        }
        
        console.log(`✅ Variable expense categories migration complete: ${migrated} migrated, ${errors} errors`);
        return { migrated, errors };
    } catch (error) {
        console.error('❌ Failed to migrate variable expense categories:', error);
        throw error;
    }
}

/**
 * Verify migration by comparing document counts
 */
async function verifyMigration(userId) {
    console.log(`\n🔍 Verifying migration for user: ${userId}`);
    
    try {
        // Count documents in user subcollections
        const transactionsSnap = await db.collection('users')
            .doc(userId)
            .collection('transactions')
            .get();
        
        const expensesSnap = await db.collection('users')
            .doc(userId)
            .collection('essentialExpenses')
            .get();
        
        const incomesSnap = await db.collection('users')
            .doc(userId)
            .collection('incomeSources')
            .get();
        
        const categoriesSnap = await db.collection('users')
            .doc(userId)
            .collection('variableExpenseCategories')
            .get();
        
        console.log('📊 User subcollection counts:');
        console.log(`  - Transactions: ${transactionsSnap.size}`);
        console.log(`  - Essential Expenses: ${expensesSnap.size}`);
        console.log(`  - Income Sources: ${incomesSnap.size}`);
        console.log(`  - Variable Expense Categories: ${categoriesSnap.size}`);
        
        // Count original collections for this user
        const origTransactions = await db.collection('transactions')
            .where('userId', '==', userId)
            .get();
        
        console.log('\n📊 Original collection counts (for this user):');
        console.log(`  - Transactions: ${origTransactions.size}`);
        
        if (transactionsSnap.size === origTransactions.size) {
            console.log('✅ Transactions: Migration verified!');
        } else {
            console.log('⚠️ Transactions: Count mismatch!');
        }
    } catch (error) {
        console.error('❌ Verification failed:', error);
    }
}

/**
 * Main migration function
 */
async function runMigration() {
    console.log('🚀 Starting Firestore data migration...');
    console.log('================================================\n');
    
    const results = {
        transactions: { migrated: 0, errors: 0 },
        fixedExpenses: { migrated: 0, errors: 0 },
        incomeSources: { migrated: 0, errors: 0 },
        variableExpenseCategories: { migrated: 0, errors: 0 }
    };
    
    try {
        // Run migrations
        results.transactions = await migrateTransactions();
        results.fixedExpenses = await migrateFixedExpenses();
        results.incomeSources = await migrateIncomeSources();
        results.variableExpenseCategories = await migrateVariableExpenseCategories();
        
        // Verify for the first user (Oliver)
        await verifyMigration('WYe3hhJhPbag2uRbR7p56KvO4u92');
        
        // Summary
        console.log('\n================================================');
        console.log('🎉 Migration Summary:');
        console.log('================================================');
        
        const totalMigrated = Object.values(results).reduce((sum, r) => sum + r.migrated, 0);
        const totalErrors = Object.values(results).reduce((sum, r) => sum + r.errors, 0);
        
        console.log(`✅ Total documents migrated: ${totalMigrated}`);
        console.log(`❌ Total errors: ${totalErrors}`);
        
        console.log('\n📝 Detailed Results:');
        Object.entries(results).forEach(([collection, stats]) => {
            console.log(`  - ${collection}: ${stats.migrated} migrated, ${stats.errors} errors`);
        });
        
        console.log('\n⚠️ IMPORTANT NEXT STEPS:');
        console.log('1. Verify data in Firebase Console');
        console.log('2. Update all repositories to use new paths');
        console.log('3. Test app thoroughly');
        console.log('4. Delete old root-level collections (BACKUP FIRST!)');
        
    } catch (error) {
        console.error('💥 Migration failed:', error);
        process.exit(1);
    }
}

// Run if called directly
if (require.main === module) {
    runMigration()
        .then(() => {
            console.log('\n✅ Migration script completed successfully!');
            process.exit(0);
        })
        .catch((error) => {
            console.error('\n💥 Migration script failed:', error);
            process.exit(1);
        });
}

module.exports = {
    migrateTransactions,
    migrateFixedExpenses,
    migrateIncomeSources,
    migrateVariableExpenseCategories,
    verifyMigration,
    runMigration
};

