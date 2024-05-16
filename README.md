# Smart Hygrometer Project

# Circuit

![Circuit](./circuit.svg)

## Notes

This was developed for Libre's Le Potato running RaspbianOS instead of RaspberryPi itself. The pin line numbers may be
incorrect given the schematic above.

## Setup

### Install Prereqs

* libgpiod
* avahi-utils

### Make Initial Config File

Example: 
```json
{
    "devicePin": 0,
    "sampleDurationSeconds": 5,
    "smsPhoneNumber": "+[your phone number]",
    "plantName": "Hosta on the windowsill",
    "thresholdVoltage": 1.5,
    "twilioAccountSid": "[your twilio api key]", 
    "twilioAuthToken": ""
}
```

### Steps

1. Create a raspbian image with SSH public key auth configured using `[whatever hostname]`.
2. Start up the device
3. `ssh [whatever hostname].local`
4. `sudo apt-get update && sudo apt-get upgrade`
5. `sudo apt-get install openjdk-17-jre libgpiod-dev avahi-utils`
6. Make a `hygrometer` directory in the home dir on the raspberry pi
7. Update `install_on_pi.zsh` to use `[whatever hostname]` as the `REMOTE_HOST`
8. Run `install_on_pi.zsh` from your workstation/laptop.

### Running the sensor 

1. Build the raspberrypi executable
   ```bash
   ./gradlew :sensor:linkReleaseExecutableNative
   ```
2. Upload it to your RaspberryPi
   ```bash
   scp sensor/build/bin/native/HygrometerSensorReleaseExecutable/HygrometerSensor.kexe \
     raspberrypi:~/HygrometerSensor
   ```
3. Create a [config file](#make-initial-config-file) on your RaspberryPi and place it somewhere.
4. Set up a cronjob to run `HygrometerSensor [path to config file]` as frequently as you want it to run.