#!/usr/bin/env zsh
./gradlew :sensor:linkReleaseExecutableNative
scp sensor/build/bin/native/HygrometerSensorReleaseExecutable/HygrometerSensor.kexe raspberrypi:~/HygrometerSensor.kexe
scp hygrometer.config raspberrypi:~/hygrometer.config
ssh raspberrypi -t './HygrometerSensor.kexe ~/hygrometer.config && exit'
