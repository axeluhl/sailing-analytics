This article can be used to store information on the data consumption that can be experienced with the SAP Sailing Analytics Android apps.

## Android apps

### Sail InSight

Energy consumption of the tracking itself is - without any optimization and compression of the messages at the time of writing this - quite low already. Ten hours of tracking mean less than five MB of data volume for the messages themselve, if you choose to send them immediately. If sent in intervalls you can minimize the size of the messages further, since you do not have to sent the devices UUID with every fix. Sending every second message together with the previous one will theoretically equal to around three to four MB of data volume needed for ten hours of tracking.

It is technically possible to compress the sent JSON messages with GZIP. Even if every message is sent individually you can achieve a compression ratio of ~103% which on it's own is not a lot, but at least it does not make it worse. Where it would really come in handy is when several messages are sent together. At a 5 second sending intervall, you can already achieve compression ratios of 300% and more. The challenge with sending GZIP-compressed JSON messages to the server is that this has to be supported by the webserver. Normally only messages sent from the server to the client can contain compressed content. There are ways to enable Apaches to receive compressed content but that probably is a lot of work.

A better idea to cut down on data consumption of the fixes messages in the InSight app is to assemble byte messages directly instead of taking JSON messages and encoding them with UTF-8. You can simply assemble a DataOutputStream  / ByteArrayOutputStream with the UUID as 2 64-bit longs at the beginning and then append the values of all the parameters in a certain order with certain data types of certain lengths. On the server-side you can then read that stream of data, extract the first 16 bytes as UUID and then split the rest of the data into the fixes again.
On [Bug 3933](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=3933) you can already find an implementation for assembling such a byte message. We already evaluated that you can save more than two thirds of the message length by doing this.
This only has to be implemented further down to the point where the data is sent and on the server side, possibly with a parameter in the URL to enable the servers to accept both JSON and direct byte messages, an implementation to split them apart again.

Since the app not only produces traffic when tracking, but also when checking in to a event or reopening that, when loading and sending team photos etc. energy consumption is significantly higher than the previously mentioned values. Since we cannot determine how often users will open and close the app, open the regattas, refresh photos etc. we cannot give good estimations for real world use cases even if we knew how much data volume every single one of these actions costs.
What we can say is, that testing the app extensively over a time of four weeks with booking into events probably more than 100 times and tracking way more than 30 hours caused around 50 to 100 MB of traffic on the testing phones.
Under normal conditions the traffic for one event should stay well below the former of these numbers.

### Buoy Pinger app

no information yet

### Racecommittee app

no information yet

## iOS apps

### Sail InSight

no information yet