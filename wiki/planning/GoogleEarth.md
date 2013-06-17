# Using Google Earth Plugin as Map Display

The Google Map display currently has two major issues:

 * It cannot be rotated which sailors would love to do to get a wind-up instead of a true north up display

 * The Google Maps v2 API that we're currently using is deprecated and is likely to be switched off by Google before the end of 2013; an upgrade to the Google Maps v3 API would then be necessary, and we may consider using this "opportunity" to change to something else which would then also give us a wind-up display.

There may be a couple of tricky tradeoffs to make. While a from-scratch canvas-based solution will perform very well, it will require adding at least the shore line from some map provider, handle zooming and panning and do some reasonable form of Mercator projection onto the canvas. Hit detection on trails may be much more difficult although they provide some potential.