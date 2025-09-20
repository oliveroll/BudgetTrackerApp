#!/bin/bash

# Git cleanup script for Budget Tracker
echo "🧹 Cleaning up Git repository..."

# Remove any files that should be ignored
echo "📝 Checking for files that should be ignored..."

# Remove build directories if they exist
if [ -d "build" ]; then
    echo "🗑️  Removing build/ directory"
    rm -rf build/
fi

if [ -d "app/build" ]; then
    echo "🗑️  Removing app/build/ directory"
    rm -rf app/build/
fi

if [ -d ".gradle" ]; then
    echo "🗑️  Removing .gradle/ directory"
    rm -rf .gradle/
fi

# Remove IDE files
if [ -d ".idea" ]; then
    echo "🗑️  Removing .idea/ directory"
    rm -rf .idea/
fi

# Remove any .iml files
find . -name "*.iml" -type f -delete

# Remove local.properties if it exists (will be recreated)
if [ -f "local.properties" ]; then
    echo "🗑️  Removing local.properties"
    rm local.properties
fi

# Reset git if needed
echo "🔄 Resetting git status..."
git reset HEAD .

# Show current status
echo ""
echo "📊 Current git status:"
git status

echo ""
echo "✅ Git cleanup complete!"
echo "💡 Run './setup-dev.sh' to set up development environment"
