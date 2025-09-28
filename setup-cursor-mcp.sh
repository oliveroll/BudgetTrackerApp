#!/bin/bash

# Setup script for Cursor MCP configuration
echo "🚀 Setting up Cursor MCP for Budget Tracker App..."

# Determine the correct path based on OS
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows
    MCP_DIR="$USERPROFILE/.cursor"
else
    # macOS/Linux
    MCP_DIR="$HOME/.cursor"
fi

MCP_FILE="$MCP_DIR/mcp.json"

# Create directory if it doesn't exist
mkdir -p "$MCP_DIR"

# Check if mcp.json already exists
if [ -f "$MCP_FILE" ]; then
    echo "⚠️  Found existing $MCP_FILE"
    echo "📝 Creating backup as mcp.json.backup"
    cp "$MCP_FILE" "$MCP_FILE.backup"
fi

# Create the MCP configuration
cat > "$MCP_FILE" << 'EOF'
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
EOF

echo "✅ Created Cursor MCP configuration at: $MCP_FILE"
echo ""
echo "📋 Configuration contents:"
cat "$MCP_FILE"
echo ""
echo "🔄 Next steps:"
echo "1. Restart Cursor for changes to take effect"
echo "2. Open your Budget Tracker project in Cursor" 
echo "3. Test with: 'What are the main features of this Budget Tracker app?'"
echo ""
echo "🌐 You can also test directly at: https://gitmcp.io/oliveroll/BudgetTrackerApp"
echo ""
echo "🎉 Setup complete!"
