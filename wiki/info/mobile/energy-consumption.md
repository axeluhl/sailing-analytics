# Power Consumption Of Android Apps

[[_TOC_]]

## Introduction
This page documents energy consumption tests of the SailInsight app in various scenarios. It is a crucial topic to consider as tracking often goes on for several hours and it would be desirable that the phone lasts throughout the whole session.

Though there are many opinions which can be found on the internet as to how big of an impact GPS or cellular network has on battery drain, most of them are very vague or do not comply with our specific usecase, so more abstract observations had to be done, see our findings below.

For further discussion on the topic there is bug [#3883]( https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=3883); you can also find detailed measurements of the initial tests on this topic [here](http://media.sapsailing.com/SmartphoneLogs/energyTracking.zip).

## Testing Methodology

Monitoring energy consumption in Android is not easy. Detailed statistics can only be obtained from the system if you have root access from Android 4.4 onwards. Information like what app is responsible for what portion of the energy consumption is either not available in APIs before 4.4 or would require rooting all the testing devices. Since that was not an option we decided to monitor the general battery level over a time of 2 hours to get somewhat representative results. We made sure that the phone was only running the regular set of background activities and no unnecessary applications.

Having decided that, we wrote an EnergyLog class that you can find in com.sap.sailing.android.shared.util on the “bug3883”-branch ready for future testing. It is able to monitor the following data by itself whenever `addEntryToLog` is called: a timestamp, the battery-percentage, battery temperature, CPU usage and GSM signal strength. Instantiate the class once for every log you want to write and when you are done logging/adding entries call `writeLog`. You can also monitor GPS accuracy and the amount of satellites used for finding the fix. This is useful if you want to measure energy consumption in the InSight app whenever `onLocationChanged` is called.

### Location APIs

<b>Differences and considerations</b>
There basically are two APIs for gathering location information: The "older" LocationManager API which is available in every API level, and the FusedLocationProvider API which works through the GooglePlayServices. The latter was used for all the Sailing Analytics Android apps and is also available on every phone in theory. Sometimes the app requires updates of the GooglePlayServices though, making it an additional hassle that could be avoided by not using it. Also we experienced issues on an Huawei Honor 6 device with Android 6 while using the InSight App when the phone was locked. The phone did not provide location updates anymore, which was the reason we looked into switching the APIs in the first place, since everything worked fine with the LocationManager.

Furthermore, you can specify the LocationManager API to only use GPS to determine exact locations, while the FusedLocationProvider API only takes criterias (such as the need for high-precision location), but it will fall back to unreasonably unprecise location fixes aggregated through WiFi and cellular network if there is no GPS connection yet.

The only advantage over the LocationManager here is that getting a coarse location as the initial fix can be much faster.

Unprecise locations are insufficient for Sail InSight and Buoy Pinger though, so waiting for a fine location instead of accepting an unprecise one should be the way to go. Thus the API used in these two apps will be switched to LocationManager.
As for the Race Committee app, unprecise locations will do here, since those are only used for setting wind information. Therefore, FusedLocationProviderApi grants better useability in terms of the speed of finding the location.

<b>Battery drain aspects</b>
We tested both APIs on three devices over a period of two hours. See the results below.
<a href="/wiki/info/mobile/energy-consumption/ComparisonAPIs.png"><img src="/wiki/info/mobile/energy-consumption/ComparisonAPIs.png"/></a>

As you can see above, the difference between the two APIs is relatively small. Moreover it seems that on some phones, one API has less impact on battery drain than the other, while on other phones it is the other way around, though this delta is probably within margin of error. So switching APIs really has no impact on battery consumption.

### 2G vs 3G connection in good network coverage

There is the common opinion that a 2G network connection drains less battery than 3G. This is only partially true though as in certain situations it is the other way around, especially in situations without good coverage (which is the case while sailing on the water most of the time).

See our tests in a good connection state below - again tested on three devices over two hours, the devices set to only use the respective network.
<a href="/wiki/info/mobile/energy-consumption/Comparison3G2G.png"><img src="/wiki/info/mobile/energy-consumption/Comparison3G2G.png"/></a>

The results show that for the Sail InSight app, 2G does not consume less battery in any case. The difference again is very small and probably within margin of error on two of our testing phones. Interestingly though, on the third phone, 2G actually drains the phone's battery noticeably stronger than 3G.

A possible reason for this might be that sending fixes takes longer on a 2G connection due to the lower bandwidth - leading the chip having to stay active longer each time a message is sent. Again, under hard conditions with problematic connectivity this would probably be the other way around.

### Comparing different message sending intervals and also good and bad connectivity
In order to simulate a weak cellular connection often found on the water - constantly changing 2G/3G/4G networks and signal strengths; even signal loss from time to time, we put the phones in an elevator. On the top floor, there is a good connection with 3G, the further down, the worse it gets up to no connection in the basement. Here the phones are sometimes able to catch a 2G signal, which is also not that strong.
For comparison, we also put the phones in a well-covered area.

As message sending intervals shown on the X-axis we chose the following:
immediately, 5 seconds, 10 seconds, 20 seconds, 30 seconds and 5 minutes.
On the Y-axis you can see the consumed energy over 2 hours.

See the results below.

<a href="/wiki/info/mobile/energy-consumption/ComparisonConnectivity.png"><img src="/wiki/info/mobile/energy-consumption/ComparisonConnectivity.png"/></a>

What you can see in this diagram is that battery consumption will drop with increasing sending intervals, no matter how good or bad cellular connection is. The most significant differences are observable on intervals ranging from 5 to 25 seconds. The difference between 30 seconds and 5 minutes is negligible, so is the difference between immediate sending and a 5 second interval.

When comparing good and bad connectivity, the graphs mostly confirm our concerns: The battery drains faster on most phones when exposed to changing cellular network or frequently lost connection. The phones consume more energy when they have to compensate for that with booking into other networks, reconnecting and so on. This means that tracking will use more energy under real conditions than in synthetic benchmarks on land, where you usually have a better connection.

Some phones though behave unexpectedly on a bad connection. The S2 shows the exact opposite of the expected results with the battery consumption being lower under bad connectivity. From this we can conclude that there are phones which are able to handle these bad conditions better than others or even get an advantage from it. When a phone is booked into a cellular network for overall less time it also consumes less power. But seemingly the expected increased consumption for booking in and out of networks is not present on some phones. 

## Conclusion

Generally we have to say that energy consumption heavily depends on the phone itself. On some phones the app would still be able to run 10 hours without optimization, on others only 4. It also depends on the specific device how effective energy conserving measures are.

<b>GPS fix interval</b>

* different GPS fix acquiring intervals do not make a difference in battery drain
* they would if the interval was much lower - say 5-10 minutes upwards
* this is not an option for a precise tracking app though so there is nothing which can be done at this point

<b>Connectivity state</b>

* better signal strength leads to a lower required transmission power by the phone's antenna
   --> a solid 2G connection is better than a bad 3G connection
* the longer the chip needs to be active, the higher the drain
   --> the faster data is sent, the better
   --> 3G is more economical, if stable, especially with bigger data batches
* frequent signal loss forces the phone to constanly try to reconnect to the cellular network

<b>Message sending interval</b>

* the main thing where power saving measures can be applied on all phones

<b>Heat</b>

* heat will have no short term influence on battery life on the current cycle
* only long term influence on battery degredation


<b>Resulting actions going forth</b>

* send messages _only_ as fast as necessary to still provide a reasonable experience
* intervals between 5 and 60 seconds are imaginable as a choice for the user
* provide a setting in the app for that - possibly displaying an educated guess on remaining battery life with each setting; taking into account the following:
* the user's device (battery level, capacity, average idle power drain)
* statistics provided by the backend (how power-intense are the different intervals on the average of devices)
* possibly taking android-provided expected time into account - see <b>Intent.ACTION_POWER_USAGE_SUMMARY</b>

---

Authors:
Adrian Riedel (D064867)
Daniel Wagner (D065058)