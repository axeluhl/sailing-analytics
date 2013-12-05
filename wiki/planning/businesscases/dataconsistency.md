# Data Consistency

### Rationale

The SAP sailing solutions are a set of software applications that process data - live and recorded - about sailing regattas, in order to visualize sailing races, various linked measures, as well as, regatta results. In order to capture sailing races, information about the race course, the competitors, the boat positions, the weather conditions (especially wind and water current), mark passing times and race times are transmitted using various kinds of wireless communication and collected on servers as raw data. In further processing steps, this raw data is linked by data structures to allow for visualization, live ranking estimation, scoring calculation and calculation of aggregate measures like boat speed, velocity-made-good, distance-to-leader, and more.
Linkage of raw data for example includes mapping of GPS-tracker-IDs to mark-IDs and competitor-IDs, joining time-dependent data along UTC time line and handling differing sampling rates, as well as, transmission delays for different kinds of raw data.

In an ideal world, this linkage would always work correctly and result in an all-time consistent representation of the race in terms of visualization and derived measures. However, in the real world of public events, a complex mixture of causes typically leads to intermittent faulty, misaligned or even missing raw data which in turn influences derived measures negatively with regards to accuracy, consistency, or even their well-definedness. 

The experience from previous sailing events has shown that the SAP sailing solutions are currently quite sensitive to such intermittent faults in raw data. This results for example in misleading live ranking estimation, wrong visualization of the advantage line, non-availability of live play or replay of races or obvious out-of-range values of derived aggregate measures.

One approach to tackle these problems is to work and improve on the data measurement units (e.g. GPS trackers, wind measurement unit), improve the data transmission infrastructure (e.g. wide-range Wifi vs mobile network GPRS), increase the stability of required WAN network connections to remote servers (e.g. reserve DSL-lines for sailing IT) and extend education & discipline of involved people (e.g. sailors should not accidentally power-off GPS-trackers, race committee officers should not rearrange GPS-trackers across race course marks). But even on events were all these requirements were seemingly fulfilled, intermittent faults in raw data and hence faults of SAP sailing solutions have occurred in the past.

In the Data Consistency project, we take the approach to gain higher stability of SAP sailing solutions by introducing robustness against raw data faults right into the heart of data processing: data structures should be able to represent raw data faults and aggregate calculations should react moderately ensuring well-definedness and consistency of visualization and all data displayed.

### Purpose

### Features

## Minutes from Initial Brainstorming Meeting (December 2, 2013)

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
* Robustness of Aggergate Calculations
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
