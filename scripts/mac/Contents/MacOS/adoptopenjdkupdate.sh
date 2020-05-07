#!/bin/bash
mkdir -p "${HOME}/JDK"
cd "${HOME}/JDK"
"$(dirname $0)/image/bin/java" -m "info.repy.adoptopenjdkupdate/info.repy.adoptopenjdkupdate.Main"
