# RaceCommittee App (Administrator's User Guide)

[[_TOC_]]

## Introduction

This user guide will help you to configure all your RaceCommittee App devices. The guide is targeted at a multi-device, multi-course-area, multi-race-officer setting. So feel free to omit steps whenever you feel that you don't have to use every feature for your current regatta. It is reasonable to stick to the presented order of steps.

## Preparation of devices

The RaceCommittee App supports remote configuration via the AdminConsole interface on the server. Open up the AdminConsole and select the tab **Device Configuration**. Click **Add Configuration**.

<img src="/wiki/images/rcapp/admin_config_step_1.png" />

First you need to enter a device identifier. It is a good idea to label the device with this identifier, to enable the identification of the device by an untrained user.

<img src="/wiki/images/rcapp/admin_config_step_2.png" />

You need to assign this identifier to the target device. Click on **QR-Sync**, recheck the current identifier and ensure that the Server URL is the correct server the device should be talking to.

<img src="/wiki/images/rcapp/admin_config_step_3.png" />

Fire up the RaceCommittee app on the target device. Go to the **settings view** by clicking on the options overflow button (top right corner) or your device's menu button. Select the preference category **General** and click on **Synchronize connection settings**. Scan the barcode shown in the AdminConsole (if you don't have a barcode app installed the RaceCommittee App should redirect you to the Google Play Store offering an app). The **Device Identifier** and **Webservice URL** preferences should reflect the changes made.

_Hint: if you don't have the app installed yet on the target device, use the QR-Code as an URL to download the app for the first time. This should work with any barcode scanner app._

<img src="/wiki/images/rcapp/admin_config_step_3a.png" /> <img src="/wiki/images/rcapp/admin_config_step_3b.png" />

After the server URL has been updated the app will check the server whether it should auto-update itself or not. To ensure that your app is compatible with the server you should choose to install the APK provided by the server. Follow the instructions on the screen and reopen the newly updated app. After the update you must re-synchronize your device with the server.

<img src="/wiki/images/rcapp/admin_config_step_4.png" />

Back in the AdminConsole configure your device (see next step). When done click **Save + Clone** to use the current configuration as a template for the next device.

Repeat until you've configured all your devices.

## Configuration of devices

There are several preferences you can configure to control the overall functionality of the RaceCommittee App. 

All these options can be configured either via remote configuration (see above) or on the device itself. The remote configuration is fetched on selecting an event in the event selection screen. If there is a configuration stored on the server for the target device, each configured option will override the corresponding option in the device's local configuration. If you leave an option unconfigured in the AdminConsole, the device will preserve its value. E.g. by leaving the 'Results recipient' option unchecked in the AdminConsole will preserve the recipient configuration currently stored on the device.

In the following we will have a look at some of these settings. Most of them are enabled for remote configuration.

### General preferences

* **Device Identifier:** See above. Used to identify the device when fetching a remote configuration.
* **Server URL:** The server the device will talk to. On change you should reload the app.
* **Managed course areas:** The user is able to logon to the configured course areas.
* **Results recipient:** When active, the race's results (photos) will be sent to this mail.

### Course Designer

* Configuration of designers: Each designer has some options to be configured.

### Regatta Configuration (default)

* **Default racing procedure:** the app will preselect the chosen racing procedure for all races, whose specific regatta configuration (see below) doesn't override this.
* **Default course designer:** the app will preselect the chosen course designer for all races, whose specific regatta configuration (see below) doesn't override this.
* Configuration of procedure: Each racing procedure has some options to be configured. An option value applies to all races, whose specific regatta configuration doesn't have this option set. 

<img src="/wiki/images/rcapp/admin_config_regatta_configuration.png" />

As you can see you are able to select for which racing procedure type you wish to create a configuration. If you don't activate a racing procedure's configuration the device's local configuration values will be preserved.

Keep in mind that changing these options in the app will only have an effect on _new_ racing procedures (ongoing race with no change in their racing procedure will keep the old settings).

## Configuration of regattas

For races attached to a regatta (i.e. RegattaLeaderboard) you are able to configure certain options _per-regatta_. The regatta's configuration is fetched by the app together with the race information on logon.

In the **Regatta** tab of the AdminConsole click the **Edit** action of the regatta to be configured. If you check **Set regatta configuration** this configuration will override any device configuration for races of this regatta.

* **Regatta's default racing procedure:** the app will preselect the chosen racing procedure for all races of this regatta. _This option will override the device's configuration_.
* **Regatta's default course designer:** the app will preselect the chosen course designer for all races of this regatta. This option will override the device's configuration.
* **Set racing procedure configuration:** When checked enables you to configure the default racing procedure of this regatta. Setting regatta-specific preferences here will override device-global preferences.

<img src="/wiki/images/rcapp/admin_config_edit_regatta.png" />

## On-Device Configuration

As stated above it is possible to (re-)configure these preferences in the app. Changes on the device will not be synchronized with the server.

## Troubleshooting

Let's have a look at the most common problems.

### Connectivity

In the top right corner three dots indicate the connectivity status. If there is a problem with sending or receiving data to/from the server the dots will turn red. The device will re-try to send data every minute.

![Connectivity ok](/wiki/images/rcapp/app_conn_ok.jpg) **versus** ![Connectivity bad](/wiki/images/rcapp/app_conn_bad.jpg)

If your device users experience persistent connectivity issues, advise them to click on the three red dots. This will display further information including the last successful sent time. Recheck that the device has an overall network connectivity and check if your server is not accepting the data sent by the device. This might be the case when the RaceCommittee App is trying to send data for already deleted/renamed regattas.

![Expert view](/wiki/images/rcapp/app_expert.jpg)

Selecting the "Expert information" option you'll have access to the expert information screen. This screen gives you the chance to have a look at all the data which could not be sent. Additionally you stop the device from trying to send this data by clicking on "Clear Events". Be sure to re-logon onto the course area or 'Reload' all data!

### Failure to load data

When trying to logon onto a course area the RaceCommittee App opens an error dialog showing an error similar to "Failure to load data: 'null'" or something more expressive. Most of the times this happens when the current version of the app is not compatible with the server. Redo the synchronization step or even force an update via the app's preference screen.

If this doesn't help have a look in the runtime-log (see below).

### Runtime- and Crash-Logs

For further fault localization the app maintains runtime-logs. They are stored on the (emulated) SD-Storage partition under

    /sd/racecommittee/DATE.log

In the case of a crash due to a unchecked exception to app writes to a crash log located in

    /sd/racecommittee/DATE_crash.log