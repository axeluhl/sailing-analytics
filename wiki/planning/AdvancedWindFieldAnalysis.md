# Advanced Wind Field Analysis

## Making more of what we already have

The boat tracks contain more information about the wind than we
currently extract. There is spatial information based on where the
boats are. If two boats are near to each other at a given time and
sail on different tacks, this increases the local confidence of the
wind estimation at that point. If boats close to each other at the
same time but different location lead to a different estimation, this
can let us estimate a position-dependent wind field.

Also, we need to improve the accuracy of the current estimator. See
also bug
[240](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=125).

If we get measurements from more than one wind sensor, we usually also
receive the sensor's position. We currently don't use this information
in answerin queries for wind data at a time point and a position. We
should come to a pragmatic interpolation algorithm that at least
roughly understands the air flow across the race course area. To a
degree this is also touched by bug
[1201](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1201).

