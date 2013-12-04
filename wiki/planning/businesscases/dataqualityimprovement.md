# Data quality improvement

## Live-delay analysis of GPS
* per competitor
* wind for advantage line

## Outlier detection
* GPS tracks
* Wind

## Dropout interpolation
* GPS tracks
* Wind

## Connection loss
* GPS track:
  * introduce states "no data" / "no connection"
    * has to be based on transmission rates
    * no ping available
* Wind

## Data quality measures
* Start Phase
  * correctness of start time on start boat
  * stabilize start detection algorithms / heuristic
* Completeness of Leg
  * mark tracks & passings
  * gps fixes
  * wind measurements
  * leg type detection
* Completeness of Race
  * competitor tracks: gps, mark passings
  * mark tracks: validation (compare marks & competitor tracks); recalculation of mark passings
  * all competitor or topN competitors
  * all marks or future (remaining) marks
  * take into account #{common legs} of competitors in aggregate calculation
* Completeness of Regatta