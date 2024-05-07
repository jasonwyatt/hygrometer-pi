#!/usr/bin/env bash

cd ~/hygrometer || exit
export HYGROMETER_CONFIG_PATH=~/hygrometer/hygrometer.config
export HYGROMETER_SENSOR_PATH=~/hygrometer/HygrometerSensor.kexe
tar -xf ./server-0.0.1.tar
killall java
./server-0.0.1/bin/server || cd - || exit
