#Bug 2043 <br/> 

Google Analytics Tracking ID: **UA-52789785-1**<br/> 
http://www.google.com/intl/en/analytics/ <br/>
GAnalytics Account: gmsponsorship@gmail.com <br/> 
GAnalytics PW: <br/>

**track events (clicks on links):**<br/> 
insert onClick() method for each link <br/> 
Syntax: _onClick="_gaq.push(['_trackEvent', category, action, label])_ <br/> 
see also: https://developers.google.com/analytics/devguides/collection/gajs/eventTrackerGuide <br/>
It might take a day, until the results are available on the dashboard (it will appear under "Behavior/Verhalten")<br/>

**track page load time:**<br/> 
see: https://developers.google.com/analytics/devguides/collection/gajs/methods/gaJSApiBasicConfiguration#_gat.GA_Tracker_._trackPageview 