# RaceCommittee App User Guide (Administrator)

This user guide will help you to configure all your RaceCommittee App devices. The guide is targeted at a multi-device, multi-course-area, multi-race-officer setting. So feel free to omit steps whenever you feel that you don't have to use every feature for your current regatta. It is reasonable to stick to the presented order of steps.

## Preparation of devices

The RaceCommittee App supports remote configuration via the AdminConsole interface on the server. Open up the AdminConsole and select the tab **Device Configuration**. Click **Add Configuration**.

<img src="/wiki/images/rcapp/admin_config_step_1.png" />

First you need to enter a device identifier. It is a good idea to label the device with the this identifier, to enable the identification of the device by an untrained user.

<img src="/wiki/images/rcapp/admin_config_step_2.png" />

You need to assign this identifier to the target device. Click on **QR-Sync**, recheck the current identifier and ensure that the Server URL is the correct server the device should be talking to. When done with your modifications hit **Generate**.

<img src="/wiki/images/rcapp/admin_config_step_3.png" />

Fire up the RaceCommittee app on the target device. Go to the **settings view** by clicking on the options overflow button (top right corner) or your device's menu button. Select the preference category **General** and click on **Synchronize connection settings**. Scan the barcode shown in the AdminConsole (if you don't have a barcode app installed the RaceCommittee App should redirect you to the Google Play Store offering an app). The **Device Identifier** and **Webservice URL** preferences should reflect the changes made.

<img src="/wiki/images/rcapp/admin_config_step_4.png" />

Back in the AdminConsole configure your device (see next step). When done click **Save + Clone** to use the current configuration as a template for the next device.

Repeat until you've configured all your devices.

## Configuration of devices

There are several preferences you can configure to control the overall functionality of the RaceCommittee App. 

All these options can be configured either via remote configuration (see above) or on the device itself. The remote configuration is fetched on selecting an event in the event selection screen. If there is a configuration stored on the server for the target device, each configured option will overwrite the corresponding option in the device's local configuration. If you leave an option unconfigured in the AdminConsole it will not overwrite the corresponding option, the device will preserves its value. E.g. by leaving the 'Results recipient' option unchecked in the AdminConsole will preserve the recipient configuration currently stored on the device.

In the following we will have a look at some of these settings. Most of them are enabled for remote configuration.

### General preferences

* **Device Identifier:** See above. Used to identify the device when fetching a remote configuration.
* **Server URL:** The server the device will talk to. On change you should reload the app.
* **Managed course areas:** The user is able to logon to the configured course areas.
* **Results recipient:** When active, the race's results (photos) will be sent to this mail.

### Racing Procedure

* **Overwrite Racing Procedure**: Selecting this option will let the app preselect your overwrite racing procedure regardless of the regatta's configuration and the history of the race's procedures.
* Configuration of procedure: Each racing procedure has some options to be configured. Keep in mind that changing these options will have an effect on _new_ racing procedures (ongoing race with no change in their racing procedure will keep the old settings!)

### Course Designer

* **Overwrite Course Designer**: See overwrite of racing procedure.
* Configuration of designers: Each designer has some options to be configured.

## Configuration of regattas

For races attached to a regatta (i.e. RegattaLeaderboard) you are able to configure certain options _per-regatta_. The regatta's configuration is fetched by the app together with the race information on logon.

In the **Regatta** tab of the AdminConsole select click on the **Edit **action of the regatta to be configured. You are able to configure the following options:

* **Regatta's default racing procedure:** the app will preselect the chosen racing procedure for all races of this regatta. _This option will have no effect when **Overwrite Racing Procedure** is set in the device's configuration_.
* **Regatta's default course designer:** the app will preselect the chosen course designer for all races of this regatta. This option will have no effect when **Overwrite Course Designer** is set in the device's configuration.
* **Set racing procedure configuration:** When checked enables you to configure the default racing procedure of this regatta. _This option will have no effect when **Overwrite racing procedure configuration** is set for the device's **remote** configuration_.