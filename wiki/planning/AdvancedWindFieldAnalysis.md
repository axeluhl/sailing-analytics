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

