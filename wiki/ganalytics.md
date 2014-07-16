**_Bug 2043_**
Google Analytics Tracking ID: **UA-52789785-1**
GAnalytics Account: gmsponsorship@gmail.com 
GAnalytics PW: 

**code for standard tracking (page view counter):**
We need to embed the following code in our HTML entry point documents:

> <script>
>   (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
>   (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new
> Date();a=s.createElement(o),
>  
> m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
>   })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

>   ga('create', 'UA-52789785-1', 'auto');
>   ga('send', 'pageview');

> </script> 

**track Events (Click on Links): **
insert onClick() method for each link 
e.g. 
> <a href="#" onClick="_gaq.push(['_trackEvent', 'Videos', 'Play', 'Baby\'s First Birthday']);">Play</a> 
https://developers.google.com/analytics/devguides/collection/gajs/eventTrackerGuide

**track page load time:**
https://developers.google.com/analytics/devguides/collection/gajs/methods/gaJSApiBasicConfiguration#_gat.GA_Tracker_._trackPageview 



