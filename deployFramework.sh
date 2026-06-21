#!/bin/bash

# Deploy the framework JAR to Tomcat webapps directory

# Set Tomcat directory - change this if needed
TOMCAT_DIR="/home/maria/Documents/L1/apache-tomcat-9.0.104"

# Check if Tomcat directory exists
if [ ! -d "$TOMCAT_DIR" ]; then
  echo "Error: Tomcat directory not found at $TOMCAT_DIR"
  echo "Please set the correct TOMCAT_DIR in this script."
  exit 1
fi

# Build the framework JAR
echo "Building framework..."
cd framework
if ! mvn clean package; then
  echo "Failed to build framework"
  exit 1
fi
cd ..

# Copy the JAR to Tomcat webapps
FRAMEWORK_JAR="framework/target/framework-1.0-SNAPSHOT.jar"
if [ -f "$FRAMEWORK_JAR" ]; then
  echo "Copying framework JAR to $TOMCAT_DIR/webapps/"
  cp "$FRAMEWORK_JAR" "$TOMCAT_DIR/webapps/"
else
  echo "Error: Framework JAR not found at $FRAMEWORK_JAR"
  exit 1
fi

echo "Framework deployed successfully."