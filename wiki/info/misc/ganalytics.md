# Introducing Google Analytics (Bug 2043)

GAnalytics Homepage: http://www.google.com/intl/en/analytics/ <br/>
GAnalytics Account: gmsponsorship@gmail.com <br/> 
GAnalytics PW: sailing123<br/>
<br/>
GAnalytics Tracking ID: **UA-52789785-1** (to make tracking possible for the webpage the tracking code has to be embeedded in the html code)<br/> 
<br/> 
**track events (clicks on links):**<br/> 
* 1. for event tracking please embed the following javascript-code in the html code of the web page to create the object _gaq <br/>
When calling method _gaq.push(['_setAccount', 'UA-XXXXX-X'])  please use the specific ganalytics tracking code as param<br/>
Coding:<br/>
<script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-XXXXX-X']);
  _gaq.push(['_trackPageview']);
  (function() {
  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';              
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
</script><br/>
* 2. to embed tracking for hyperlinks use the onClick() method for each link <br/> 
Syntax: <a href="#" onClick="_gaq.push(['_trackEvent', category, action, label]) <br/> 

definition of params:<br/>
•	category (required)<br/>
The name you supply for the group of objects you want to track.<br/>
•	action (required)<br/>
A string that is uniquely paired with each category, and commonly used to define the type of user interaction for the web object.<br/>
•	label (optional)<br/>
An optional string to provide additional dimensions to the event data.<br/>
•	value (optional)<br/>
An integer that you can use to provide numerical data about the user event.<br/>
•	non-interaction (optional)<br/>
A boolean that when set to true, indicates that the event hit will not be used in bounce-rate calculation.<br/>

see also: https://developers.google.com/analytics/devguides/collection/gajs/eventTrackerGuide <br/>

It might take a day, until the results are available on the dashboard (it will appear under "Behavior/Verhalten")<br/>

**track page load time:**<br/> 
see: https://developers.google.com/analytics/devguides/collection/gajs/methods/gaJSApiBasicConfiguration#_gat.GA_Tracker_._trackPageview 

**useful information for web page tracking with ganalytics:** <br/>
https://developers.google.com/analytics/devguides/collection/gajs/