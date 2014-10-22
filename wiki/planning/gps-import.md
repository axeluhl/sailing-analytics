# GPS Import

Somewhat similar to [[Racelog Tracking|wiki/racelog-tracking]] where sailors' smartphones would be used as position capturing devices, positions can as well be captured by a variety of different GPS devices. Using a common core set of features also necessary for smartphone tracking, we could allow sailors to import arbitrary tracks from any standard GPS file format, such as GPX or KML.

Since usually the marks will not have been tracked, manual addition of course information will be necessary in this case. Once done, other tracks may be added to the course. A mark passing algorithm can then cut the track into legs and perform our standard analyses.

Manual wind entry should be supported. Based on the boat class and its typical tacking and gybing angles it may however even be possible to distinguish upwind from downwind legs and therefore infer much about the wind automatically.