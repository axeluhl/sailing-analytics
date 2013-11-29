# RaceCommittee App User Guide (Administrator)

This user guide will help you to configure all your RaceCommittee App devices. The guide is targeted at a multi-device, multi-course-area, multi-race-officer setting. So feel free to omit steps whenever you feel that you don't have to use every feature for your current regatta. It is reasonable to stick to the presented order of steps.

## Preparation of devices

The RaceCommittee App supports remote configuration via the AdminConsole interface on the server. Open up the AdminConsole and select the tab **Device Configuration**. Click **Add Configuration**.

<img src="/wiki/images/rcapp/admin_config_step_1.png" width="70%" height="70%"/>

First you need to enter a device identifier. It is a good idea to label the device with the this identifier, to enable the identification of the device by an untrained user.

<img src="/wiki/images/rcapp/admin_config_step_2.png" width="70%" height="70%"/>

You need to assign this identifier to the target device. Click on **QR-Sync**, recheck the current identifier and ensure that the Server URL is the correct server the device should be talking to. When done with your modifications hit **Generate**.

<img src="/wiki/images/rcapp/admin_config_step_3.png" width="70%" height="70%"/>

Fire up the RaceCommittee app on the target device. Go to the **settings view** by clicking on the options overflow button (top right corner) or your device's menu button. Select the preference category **General** and click on **Synchronize connection settings**. Scan the barcode shown in the AdminConsole (if you don't have a barcode app installed the RaceCommittee App should redirect you to the Google Play Store offering an app). The **Device Identifier** and **Webservice URL** preferences should reflect the changes made.

<img src="/wiki/images/rcapp/admin_config_step_4.png" width="70%" height="70%"/>

Back in the AdminConsole configure your device (see next step). When done click **Save + Clone** to use the current configuration as a template for the next device.

Repeat until you've configured all your devices.