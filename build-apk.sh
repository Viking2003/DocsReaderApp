#!/bin/bash

# Build APK and copy to apks folder for distribution
# Usage: ./build-apk.sh [debug|release]

BUILD_TYPE=${1:-release}

echo "Building $BUILD_TYPE APK..."

# Build the APK
./gradlew assemble$BUILD_TYPE

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"

    # Create apks directory if it doesn't exist
    mkdir -p apks

    # Copy the APK to apks folder
    cp app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE.apk apks/

    echo "APK copied to apks/app-$BUILD_TYPE.apk"
    echo "You can now commit the apks/ folder to Git"
else
    echo "Build failed!"
    exit 1
fi
