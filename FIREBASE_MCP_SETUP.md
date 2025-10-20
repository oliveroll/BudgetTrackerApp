# Firebase MCP Server Integration Guide

## ✅ Configuration Applied

The Firebase MCP server has been integrated into your Cursor configuration at:
- **Location:** `/home/oliver/.cursor/mcp.json`
- **Server Name:** `firebase-mcp`
- **Project ID:** `budget-tracker-app-oliver`
- **Storage Bucket:** `budget-tracker-app-oliver.firebasestorage.app`

---

## 🔑 Required: Download Service Account Key

To complete the setup, you need to download your Firebase service account key:

### Step 1: Go to Firebase Console

1. Visit: https://console.firebase.google.com/project/budget-tracker-app-oliver/settings/serviceaccounts/adminsdk
2. Or navigate manually:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select project: **budget-tracker-app-oliver**
   - Click ⚙️ **Settings** → **Project settings**
   - Click **Service accounts** tab

### Step 2: Generate New Private Key

1. Scroll down to **Firebase Admin SDK** section
2. Click **"Generate new private key"** button
3. Confirm by clicking **"Generate key"**
4. A JSON file will download automatically

### Step 3: Save the Key File

1. Rename the downloaded file to: `budget-tracker-app-oliver-serviceAccountKey.json`
2. Move it to: `/home/oliver/.firebase/`
3. Run this command to set proper permissions:
   ```bash
   chmod 600 /home/oliver/.firebase/budget-tracker-app-oliver-serviceAccountKey.json
   ```

### Quick Setup Commands

```bash
# Create directory (already done)
mkdir -p ~/.firebase

# Move the downloaded key file
mv ~/Downloads/*serviceAccountKey*.json ~/.firebase/budget-tracker-app-oliver-serviceAccountKey.json

# Set secure permissions
chmod 600 ~/.firebase/budget-tracker-app-oliver-serviceAccountKey.json

# Verify it exists
ls -la ~/.firebase/
```

---

## 🔧 How It Works

### 1. MCP Server Registration

The Firebase MCP server is now registered in your Cursor configuration:

```json
{
  "mcpServers": {
    "firebase-mcp": {
      "command": "npx",
      "args": ["-y", "@gannonh/firebase-mcp"],
      "env": {
        "SERVICE_ACCOUNT_KEY_PATH": "/home/oliver/.firebase/budget-tracker-app-oliver-serviceAccountKey.json",
        "FIREBASE_STORAGE_BUCKET": "budget-tracker-app-oliver.firebasestorage.app",
        "DEBUG_LOG_FILE": "true"
      }
    }
  }
}
```

### 2. How Cursor Calls Firebase MCP

When you interact with Cursor AI:

1. **Cursor detects Firebase-related queries** (e.g., "update Firestore rules", "query transactions collection")
2. **Launches the MCP server** using `npx @gannonh/firebase-mcp`
3. **Passes environment variables** (service account key path, storage bucket)
4. **MCP server initializes** Firebase Admin SDK with your credentials
5. **Executes Firebase operations** and returns results to Cursor
6. **Cursor displays the results** in the chat

### 3. Available Firebase Tools

Once configured, you can use these Firebase operations directly in Cursor:

#### **Firestore Tools:**
- `firestore_add_document` - Add documents to collections
- `firestore_list_documents` - List documents with filtering
- `firestore_get_document` - Get specific documents
- `firestore_update_document` - Update existing documents
- `firestore_delete_document` - Delete documents
- `firestore_list_collections` - List all collections
- `firestore_query_collection_group` - Query across subcollections

#### **Storage Tools:**
- `storage_list_files` - List files in Storage
- `storage_get_file_info` - Get file metadata and URLs
- `storage_upload` - Upload files
- `storage_upload_from_url` - Upload from URL

#### **Authentication Tools:**
- `auth_get_user` - Get user details by ID or email

---

## 📋 Usage Examples

### Example 1: Query Firestore

**You ask Cursor:**
> "Show me all transactions from October 2025"

**Cursor will:**
1. Use `firestore_list_documents` tool
2. Query the `transactions` collection
3. Filter by date range
4. Display the results

### Example 2: Update Security Rules

**You ask Cursor:**
> "Update Firestore security rules to allow authenticated read access"

**Cursor will:**
1. Read current `firestore.rules` file
2. Modify the rules
3. Deploy using Firebase CLI (already deployed! ✅)

### Example 3: Upload to Storage

**You ask Cursor:**
> "Upload this image to Firebase Storage in the 'profile-pics' folder"

**Cursor will:**
1. Use `storage_upload` tool
2. Upload to specified path
3. Return download URL

---

## 🔍 Debugging

### Enable Debug Logging

Debug logging is already enabled (`DEBUG_LOG_FILE: true`).

**View logs:**
```bash
# Default log location
tail -f ~/.firebase-mcp/debug.log

# Or follow in real-time
watch -n 1 'tail -20 ~/.firebase-mcp/debug.log'
```

### Verify Service Account Permissions

Your service account needs these roles:
- **Firebase Admin** (full access)
- **Cloud Datastore User** (for Firestore)
- **Storage Admin** (for Cloud Storage)

Check in [IAM Console](https://console.cloud.google.com/iam-admin/iam?project=budget-tracker-app-oliver)

---

## ✅ Verification Checklist

After downloading the service account key, verify your setup:

```bash
# 1. Check if key file exists
test -f ~/.firebase/budget-tracker-app-oliver-serviceAccountKey.json && echo "✅ Key file found" || echo "❌ Key file missing"

# 2. Check file permissions (should be 600)
stat -c "%a %n" ~/.firebase/budget-tracker-app-oliver-serviceAccountKey.json

# 3. Check if npx can access Firebase MCP
npx @gannonh/firebase-mcp --version

# 4. Restart Cursor
# Close and reopen Cursor for changes to take effect
```

---

## 🚀 Next Steps

1. **Download your service account key** (see instructions above)
2. **Restart Cursor** to load the new MCP configuration
3. **Test it out** by asking Cursor to:
   - "List all Firestore collections"
   - "Show me the transactions collection structure"
   - "Query users with userId = WYe3hhJhPbag2uRbR7p56KvO4u92"

---

## 📚 Additional Resources

- [Firebase MCP GitHub](https://github.com/gannonh/firebase-mcp)
- [MCP Documentation](https://modelcontextprotocol.io/)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Your Firebase Console](https://console.firebase.google.com/project/budget-tracker-app-oliver)

---

## 🎉 What This Solves

This integration directly addresses your current issue:

**Problem:** Firebase security rules were blocking your app from reading transactions
**Solution:** With Firebase MCP, you can now:
- ✅ Query Firestore data directly from Cursor
- ✅ Update security rules and deploy them
- ✅ Debug Firebase issues in real-time
- ✅ Test queries before implementing them in code
- ✅ Manage Firebase Storage files
- ✅ Inspect user authentication data

**Your transactions are safe!** All 92 transactions are in Firebase - the MCP server will help you access and manage them easily.


