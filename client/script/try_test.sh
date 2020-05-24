#!/bin/sh
java_installed=$(which java | wc -l)
java_version=$(sudo find "/" -name "java-8-openjdk-armhf" | wc -l)

if [ "$java_installed" -eq 0 ] || [ "$java_version" -eq 0 ]; then
  echo "not found"
else
  echo "found"
fi
