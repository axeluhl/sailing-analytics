# Minutes, December 2, 2013

## Initial Brainstorming Meeting

### Live-Delay Analysis
* GPS per competitor
* wind for advantage line

### Outlier detection
* GPS tracks
* Wind

### Dropout interpolation
* GPS tracks
* Wind

### Connection loss
* GPS Track:
  * introduce states "no data" / "no connection"
    * has to be based on transmission rates
    * no ping available
* Wind

### Data consistency measures
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
  * (all races complete) only in rare cases achievable
  * cleansing of races, if possible
  * include race states, e.g. DNF, into aggregate calculations, e.g. average speed
  * take into account #{common races} of competitors in aggregate calculation
* Robustness of Aggregate Calculations
  * on all levels: leg, race, regatta, regatta series
  * aggregate calculation can be corrupted by filtered outliers, lost connection
* Quality based on Outliers and/or Dropouts
  * GPS
  * Wind
* Network Connectivity
  * all required channels

### Master Data
* Competitors: names
* Sail numbers
* Boat classes: names
* Scoring schemes: score corrections

### Data properties to be visualized or accessible
* Sampling frequency for GPS
* Number of active GPS satellites
* Reflect accuracies of measurements somehow on the UI, e.g. tails along GPS tracks

### Extra-/Interpolation of GPS tracks
* bridge phase without connection to network or connection to GPS satellites

### Extra-/Interpolation for wind
* bridge phases without estimation or measurement
