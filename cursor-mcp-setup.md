# Cursor MCP Setup for Budget Tracker App

## Official Cursor Configuration

According to the official GitMCP documentation, add this to your Cursor configuration file at `~/.cursor/mcp.json`:

### Option 1: Specific Repository MCP Server

```json
{
  "mcpServers": {
    "budget-tracker": {
      "url": "https://gitmcp.io/oliveroll/BudgetTrackerApp"
    }
  }
}
```

### Option 2: Generic MCP Server (Dynamic Repository Access)

```json
{
  "mcpServers": {
    "gitmcp-docs": {
      "url": "https://gitmcp.io/docs"
    }
  }
}
```

### Option 3: Both Servers Combined

```json
{
  "mcpServers": {
    "budget-tracker": {
      "url": "https://gitmcp.io/oliveroll/BudgetTrackerApp"
    },
    "gitmcp-docs": {
      "url": "https://gitmcp.io/docs"
    }
  }
}
```

### Option 4: Extended Configuration with Related Technologies

```json
{
  "mcpServers": {
    "budget-tracker": {
      "url": "https://gitmcp.io/oliveroll/BudgetTrackerApp"
    },
    "android-jetpack-compose": {
      "url": "https://gitmcp.io/android/compose-samples"
    },
    "firebase-android": {
      "url": "https://gitmcp.io/firebase/quickstart-android"
    },
    "kotlin-examples": {
      "url": "https://gitmcp.io/JetBrains/kotlin"
    },
    "gitmcp-generic": {
      "url": "https://gitmcp.io/docs"
    }
  }
}
```

## How to Use

1. **Create/Edit Cursor MCP Configuration**:
   - Create or edit the file: `~/.cursor/mcp.json`
   - On Windows: `%USERPROFILE%\.cursor\mcp.json`
   - On macOS/Linux: `~/.cursor/mcp.json`

2. **Add MCP Configuration**:
   - Copy one of the configurations above
   - Paste it into your `mcp.json` file
   - Save the file

3. **Restart Cursor**:
   - Close and reopen Cursor for changes to take effect

3. **Test the Setup**:
   - Open your Budget Tracker project in Cursor
   - Try prompting with: "How do I add a new transaction in this Budget Tracker app?"
   - Cursor should use GitMCP to fetch relevant documentation

## Available Tools

Once configured, your AI assistant will have access to:

- `fetch_budget_tracker_documentation`: Get project overview and docs
- `search_budget_tracker_documentation`: Search through documentation  
- `search_budget_tracker_code`: Search through the codebase
- `fetch_url_content`: Get content from referenced links

## Example Prompts

Try these prompts after setup:

- "How is the Transaction model structured in this app?"
- "What budget templates are available?"
- "How do I add a new transaction category?"
- "Explain the Clean Architecture implementation"
- "What Firebase collections are used?"

## Verification

To verify the setup is working:

1. Open Cursor with your Budget Tracker project
2. Ask: "What are the main features of this Budget Tracker app?"
3. The AI should provide detailed information about OPT/visa features, transaction categories, etc.

If it works, you'll see responses that reference your actual project documentation rather than generic budget app information.
