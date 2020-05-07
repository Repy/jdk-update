#!/bin/bash
mkdir -p "${HOME}/JDK"
cd "${HOME}/JDK"
"$(dirname $0)/image/bin/java" "-Xdock:icon=$(dirname $0)/../Resources/icon.png" -m "info.repy.adoptopenjdkupdate/info.repy.adoptopenjdkupdate.Main"
