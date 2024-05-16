#!/usr/bin/env zsh
REMOTE_HOST=potato2.local

./gradlew :sensor:linkReleaseExecutableNative
./gradlew :server:assembleDist

scp startup.sh "$REMOTE_HOST:~/hygrometer/."
scp sensor/build/bin/native/HygrometerSensorReleaseExecutable/HygrometerSensor.kexe "$REMOTE_HOST:~/hygrometer/."
#scp hygrometer.config "$REMOTE_HOST:~/hygrometer/."
scp server/build/distributions/server-0.0.1.tar "$REMOTE_HOST:~/hygrometer/."
scp server_daemon.sh "$REMOTE_HOST:~/hygrometer/hygrometer_server_daemon"
ssh $REMOTE_HOST -t "sudo cp ~/hygrometer/hygrometer_server_daemon /etc/init.d/."
ssh $REMOTE_HOST -t "sudo chmod +x /etc/init.d/hygrometer_server_daemon && sudo chown root:root /etc/init.d/hygrometer_server_daemon"
ssh $REMOTE_HOST -t "sudo /etc/init.d/hygrometer_server_daemon restart"
ssh $REMOTE_HOST -t "sudo update-rc.d hygrometer_server_daemon defaults"
