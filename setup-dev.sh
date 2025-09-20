#!/bin/bash

# Budget Tracker Development Environment Setup
echo "🚀 Setting up Budget Tracker development environment..."

# Create Python virtual environment for development tools
if [ ! -d ".venv" ]; then
    echo "📦 Creating Python virtual environment..."
    python3 -m venv .venv
    
    # Activate virtual environment
    source .venv/bin/activate
    
    # Install development tools
    echo "🔧 Installing development tools..."
    pip install --upgrade pip
    pip install pre-commit
    pip install gitpython
    pip install requests
    
    # Create requirements file
    pip freeze > dev-requirements.txt
    
    echo "✅ Virtual environment created at .venv/"
else
    echo "✅ Virtual environment already exists"
fi

# Create development scripts directory
mkdir -p scripts

# Make sure Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Please set it to your Android SDK path."
    echo "   Example: export ANDROID_HOME=/home/oliver/Android/Sdk"
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo "⚠️  Java not found. Please install OpenJDK 11 or higher."
fi

# Create local.properties template if it doesn't exist
if [ ! -f "local.properties" ]; then
    echo "📝 Creating local.properties template..."
    cat > local.properties << EOF
# Location of the Android SDK. This is only used by Gradle.
# For customization when using a Version Control System, please read the
# header note.
sdk.dir=/home/oliver/Android/Sdk
EOF
    echo "✅ local.properties created (add to .gitignore)"
fi

echo ""
echo "🎉 Development environment setup complete!"
echo ""
echo "Next steps:"
echo "1. Activate virtual environment: source .venv/bin/activate"
echo "2. Open project in Android Studio"
echo "3. Set ANDROID_HOME if not already set"
echo "4. Update local.properties with your SDK path"
echo ""
