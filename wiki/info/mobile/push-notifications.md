# Push Notifications

This page contains pointers on sending push notifications to Android and iOS from a Java Server.

## GCM (Android)

The following Google Java project can be used for sending push notifications to Android:

https://code.google.com/p/gcm/

See package `com.google.android.gcm.server` for sending notifications. 

You need to generate a Google API key, see 

http://stackoverflow.com/questions/13151054/how-to-create-api-key-for-gcm. 

Add this API key to both to the server (see `com.google.android.gcm.server.Sender`) and the Android app.

## APNS (iOS)

This open source Java project works really well with APNS:

https://code.google.com/p/javapns/

To send pushes, an APNS certificate needs to be generated for the App ID at https://developer.apple.com. The app needs be signed with a matching mobile provisioning profile to receive push notifications. Note that the process is very error-prone. Here is a step-by-step tutorial:

https://www.pushwoosh.com/programming-push-notification/ios/ios-configuration-guide/

**Gotcha** For testing Ad-Hoc App builds, a "Production Certificate" needs be used on the server (not a "Development Certificate").

**Gotcha** When exporting the private key (in order to use the certificate on the server and to sign an app) always use a password ("Keychain Access" allows you to export a key without a password).

**Gotcha** The push payload may not exceed 255 bytes, watch out when using UTF-8 encoding.