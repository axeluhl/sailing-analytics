# Monitoring and Improving Data Quality

Our data is obtained from sensors and user input. Both can be flaky and lack accuracy. We already detect major outliers in the GPS and wind data. However, many data quality issues remain. Particularly, aggregating data of different levels of quality may need to propagate some information about quality and confidence to the aggregated values so that consumers may judge the overall confidence of the aggregated results.

Also, when data has been detected as flaky or plain wrong, it should be possible to exclude such data from further reporting.

See also [Bug 1243](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1243).