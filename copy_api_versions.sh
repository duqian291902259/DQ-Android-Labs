#!/bin/bash
echo Android sdk location: $ANDROID_SDK_HOME
mkdir $ANDROID_SDK_HOME/platform-tools/api/
cp $ANDROID_SDK_HOME/platforms/android-30/data/api-versions.xml $ANDROID_SDK_HOME/platform-tools/api/