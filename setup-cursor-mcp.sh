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

# Ask user for configuration type
echo "Choose MCP configuration:"
echo "1) Basic (Budget Tracker only)"
echo "2) Standard (Budget Tracker + Generic)"
echo "3) Extended (Budget Tracker + Related Technologies)"
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        # Basic configuration
        cat > "$MCP_FILE" << 'EOF'
{
  "mcpServers": {
    "budget-tracker": {
      "url": "https://gitmcp.io/oliveroll/BudgetTrackerApp"
    }
  }
}
EOF
        ;;
    2)
        # Standard configuration
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
        ;;
    3)
        # Extended configuration
        cat > "$MCP_FILE" << 'EOF'
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
EOF
        ;;
    *)
        echo "Invalid choice. Using standard configuration..."
        # Default to standard configuration
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
        ;;
esac

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
