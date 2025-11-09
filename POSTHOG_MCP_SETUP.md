# PostHog MCP Server Setup Guide

## ✅ Step 1: Configuration Added

I've added the PostHog MCP server to your `.mcp-config.json` file:

```json
"posthog": {
  "command": "npx",
  "args": [
    "-y",
    "mcp-remote@latest",
    "https://mcp.posthog.com/sse",
    "--header",
    "Authorization:${POSTHOG_AUTH_HEADER}"
  ],
  "env": {
    "POSTHOG_AUTH_HEADER": "Bearer INSERT_YOUR_PERSONAL_API_KEY_HERE"
  }
}
```

---

## 🔑 Step 2: Get Your PostHog Personal API Key

1. **Go to PostHog Settings:**
   - Visit: https://us.i.posthog.com/settings/user-api-keys
   - Or click your profile → **Personal API Keys**

2. **Create a New Key:**
   - Click **"Create personal API key"**
   - **Important:** Select the **"MCP Server"** preset
   - This gives the right permissions for AI agents

3. **Copy the Key:**
   - It will look like: `phx_xxxxxxxxxxxxxxxxxxxxx`
   - Save it securely (you won't see it again!)

---

## 🔐 Step 3: Add Your API Key to Configuration

**Option A: Store Securely in local.properties (Recommended)**

Add to your `/home/oliver/BudgetTrackerApp/local.properties`:

```properties
# PostHog MCP Personal API Key
POSTHOG_MCP_API_KEY=phx_YOUR_ACTUAL_KEY_HERE
```

Then update `.mcp-config.json`:
```json
"env": {
  "POSTHOG_AUTH_HEADER": "Bearer ${POSTHOG_MCP_API_KEY}"
}
```

**Option B: Add Directly to .mcp-config.json (Quick but less secure)**

Open `.mcp-config.json` and replace:
```json
"POSTHOG_AUTH_HEADER": "Bearer INSERT_YOUR_PERSONAL_API_KEY_HERE"
```

With:
```json
"POSTHOG_AUTH_HEADER": "Bearer phx_YOUR_ACTUAL_KEY_HERE"
```

⚠️ **Note:** If using Option B, make sure `.mcp-config.json` is git-ignored!

---

## 🔄 Step 4: Restart Cursor

1. **Close all Cursor windows**
2. **Reopen Cursor**
3. The PostHog MCP server will load automatically

---

## ✅ Step 5: Verify Installation

Once Cursor restarts, you can test the MCP by asking the AI:

```
Can you list all my PostHog dashboards?
```

or

```
Show me the events tracked in my PostHog project
```

The AI should now have access to your PostHog data!

---

## 🎯 What You Can Do with PostHog MCP

The AI can now help you:

### **Dashboards:**
- Create, update, and delete dashboards
- Add insights to dashboards
- List all dashboards

### **Analytics:**
- Run queries on your data
- Create insights from natural language questions
- Analyze trends and funnels

### **Feature Flags:**
- Create and manage feature flags
- Check flag status
- Update flag rollouts

### **Experiments:**
- Create A/B tests
- View experiment results
- Update experiment configurations

### **Error Tracking:**
- List errors in your project
- Get error details
- Analyze error patterns

### **Documentation:**
- Search PostHog docs
- Get help with PostHog features

### **Example Queries:**

```
"Create a dashboard showing my app's key metrics"
```

```
"What are the most common errors in my project this week?"
```

```
"Show me a funnel of user signup to first transaction"
```

```
"Create a feature flag for the new dark mode UI"
```

---

## 🔒 Security Best Practices

✅ **Use MCP Server preset** when creating your API key  
✅ **Scope to specific project** if possible  
✅ **Store key in local.properties** (git-ignored)  
✅ **Review AI actions** before they execute  
✅ **Rotate keys** if compromised  

❌ **Don't commit API keys** to version control  
❌ **Don't share keys** in Slack/email  
❌ **Don't use production keys** for testing  

---

## 🛠️ Troubleshooting

### MCP Server Not Loading?

1. **Check API key is valid:**
   ```bash
   curl -H "Authorization: Bearer YOUR_KEY" https://us.i.posthog.com/api/projects/
   ```

2. **Check .mcp-config.json syntax:**
   - Must be valid JSON
   - No trailing commas
   - Proper quotes

3. **Restart Cursor completely:**
   - Close all windows
   - Quit from menu (not just close window)
   - Reopen

4. **Check Cursor logs:**
   - Help → Show Logs
   - Look for MCP-related errors

### API Key Invalid?

- Regenerate key in PostHog settings
- Make sure you used "MCP Server" preset
- Check for extra spaces or characters

### Environment Variable Not Working?

If `${POSTHOG_MCP_API_KEY}` doesn't work, use the full key directly:
```json
"POSTHOG_AUTH_HEADER": "Bearer phx_your_actual_key_here"
```

---

## 📚 Resources

- [PostHog MCP Documentation](https://posthog.com/docs/model-context-protocol)
- [PostHog API Documentation](https://posthog.com/docs/api)
- [PostHog Dashboard](https://us.i.posthog.com)
- [Model Context Protocol Spec](https://modelcontextprotocol.io/)

---

## 🎉 Next Steps

Once configured:

1. ✅ **Test the MCP** by asking AI to query PostHog
2. ✅ **Create dashboards** for your Budget Tracker metrics
3. ✅ **Set up feature flags** for new features
4. ✅ **Run experiments** to optimize user experience
5. ✅ **Monitor errors** and app performance

---

## Summary

**Current Status:**
- ✅ MCP configuration added to `.mcp-config.json`
- ⏳ Waiting for your Personal API Key
- ⏳ Restart Cursor after adding key

**Your Configuration File:**
```
/home/oliver/BudgetTrackerApp/.mcp-config.json
```

**Next Action:**
1. Get your API key from PostHog
2. Add it to the configuration
3. Restart Cursor
4. Start using AI with PostHog! 🚀




