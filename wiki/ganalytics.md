##Bug 2043 <br/> 

Google Analytics Tracking ID: **UA-52789785-1**<br/> 
GAnalytics Account: gmsponsorship@gmail.com <br/> 
GAnalytics PW: <br/> 

**code for standard tracking (page view counter):**<br/> 
We need to embed the following code in our HTML entry point documents:<br/> 
> <script>
>   (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
>   (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new
> Date();a=s.createElement(o),
>  
> m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
>   })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

>   ga('create', 'UA-52789785-1', 'auto');
>   ga('send', 'pageview');

> </script> <br/> 

**track Events (Click on Links):**<br/> 
insert onClick() method for each link <br/> 
e.g. 
> <a href="#" onClick="_gaq.push(['_trackEvent', 'Videos', 'Play', 'Baby\'s First Birthday']);">Play</a> 
https://developers.google.com/analytics/devguides/collection/gajs/eventTrackerGuide

**track page load time:**<br/> 
https://developers.google.com/analytics/devguides/collection/gajs/methods/gaJSApiBasicConfiguration#_gat.GA_Tracker_._trackPageview 



