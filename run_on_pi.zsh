#!/usr/bin/env zsh
REMOTE_HOST=raspberrypi.local

./gradlew :sensor:linkReleaseExecutableNative
./gradlew :server:assembleDist

scp startup.sh "$REMOTE_HOST:~/hygrometer/."
scp sensor/build/bin/native/HygrometerSensorReleaseExecutable/HygrometerSensor.kexe "$REMOTE_HOST:~/hygrometer/."
scp hygrometer.config "$REMOTE_HOST:~/hygrometer/."
scp server/build/distributions/server-0.0.1.tar "$REMOTE_HOST:~/hygrometer/."
ssh $REMOTE_HOST -t "./hygrometer/startup.sh"
