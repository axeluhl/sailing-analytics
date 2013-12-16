# Data Consistency

Author: Christopher Ronnewinkel

### Rationale

The SAP sailing solutions are a set of software applications that process data - live and recorded - about sailing regattas, in order to visualize sailing races, various linked measures, as well as, regatta results. In order to capture sailing races, information about the race course, the competitors, the boat positions, the weather conditions (especially wind and water current), mark passing times and race times are transmitted using various kinds of wireless communication and collected on servers as raw data. In further processing steps, this raw data is linked by data structures to allow for visualization, live ranking estimation, scoring calculation and calculation of aggregate measures like boat speed, velocity-made-good, distance-to-leader, and more.
Linkage of raw data for example includes mapping of GPS-tracker-IDs to mark-IDs and competitor-IDs, joining time-dependent data along UTC time line and handling differing sampling rates, as well as, transmission delays for different kinds of raw data.

In an ideal world, this linkage would always work correctly and result in an all-time consistent representation of the race in terms of visualization and derived measures. However, in the real world of public events, a complex mixture of causes typically leads to intermittent faulty, misaligned or even missing raw data which in turn influences derived measures negatively with regards to accuracy, consistency, or even their well-definedness. 

The experience from previous sailing events has shown that the SAP sailing solutions are currently quite sensitive to such intermittent faults in raw data. This results for example in misleading live ranking estimation, wrong visualization of the advantage line, non-availability of live play or replay of races or obvious out-of-range values of derived aggregate measures.

The status quo to tackle these problems is to work and improve on the data measurement units (e.g. GPS-trackers, wind measurement units), improve the data transmission infrastructure (e.g. wide-range Wifi vs. mobile network GPRS), increase the stability of required WAN network connections to remote servers (e.g. reserve DSL-lines for sailing IT) and extend education & discipline of involved people (e.g. sailors should not accidentally power-off GPS-trackers, race committee officers should not rearrange GPS-trackers across race course marks). But even on events were all these requirements were seemingly fulfilled, intermittent faults in raw data and hence faults of SAP sailing solutions have occurred in the past.

### Purpose

In the "Data Consistency" project, we take the new approach to gain higher stability of SAP sailing solutions by introducing robustness against raw data faults right into the heart of data processing: data structures should be able to represent raw data faults and aggregate calculations should react moderately ensuring well-definedness and consistency of visualization and all data displayed.

### Features

* Start Phase
  * Check correctness of start time based on several data sources: tracker, race committee, start detection
  * Stabilize start detection algorithms
* Completeness Checks
  * Considered levels: leg, race, regatta, regatta series
  * Add completeness state "complete/incomplete" on all levels
  * Refined calculation-behavior of measures dependent on completeness on all levels
  * Refined display-behavior in leaderboards and comparison charts dependent on completeness on all levels
  * Visualize completeness state
* Connected Checks
  * Considered entities: GPS-track, mark-track, wind measurement
  * Add connected state "connected/disconnected" on all entities
  * Log state changes
  * Visualize connected state
* Data Properties
  * Visualize (or make accessible) data properties
  * Sampling frequency of GPS-tracker (per competitor, on average)
  * Number of active GPS-satellites
  * Accuracies of measurements, especially GPS-positions and wind bearing & speed
* Outlier detection
  * Considered entities: GPS-track, mark-track, wind measurement
  * Add outlier state "in/out" on all entities
  * Detect faulty jumps in GPS-tracks and mark-tracks by checking physical limits
  * Detect faulty noise in wind measurements
* Correction of faulty raw data
  * Focus: GPS-tracks, mark-tracks, wind measurements
  * Apply interpolation and extrapolation where possible
* Robustness of Aggregate Calculations
  * Remove outliers from calculation
  * Remove incomplete entities from calculation (if possible, otherwise return "n/a")
  * Adapt to missing data or changed sampling rate
* Overall Measures of System State
  * Overall Consistency: measure for connectedness and completeness of sailing solution
  * Overall Confidence: profile describing accuracies of numerical quantities compared to reference ranges

### Description

The features listed above each address problems that have occurred frequently in the past while using the SAP sailing software during public events.

#### Start Phase

During the start phase of a race, it is a common problem to determine the actual start time of a race accurately. This is partly caused by time shifts of the various involved clocks, e.g. SAP server time, GPS time, tracking provider time, race committee time. Moreover, the manual start time input via the race committee app is subject to inaccuracies due to the user's reaction and software handling time. An automated start time detection based on GPS-tracks that takes into account other start time values shall lead to higher stability to avoid recording a race with a wrong start time.

#### Completeness Checks

There are various reasons why in terms of raw data a competitor does not finish a race, e.g. tracker lost power, tracker lost network, tracker lost satellites, SAP server lost connection to tracking provider server, competitor capsized, competitor decided to finish the race earlier. From the raw data most these cases cannot be distinguished. But based on comparisons of GPS-tracks, mark-tracks and times it can be determined whether the current leg has been finished. Based on the finished legs it can be decided whether the race has been regularly finished. And in the same manner, it can be derived whether regatta and regatta series have been regularly finished by a competitor.

The completeness state of legs, races, regattas and regatta series has to be taken into account for calculated scores and measures. Moreover, the completeness state has to be taken into account and visualized in leaderboards and comparison charts so that the user is able to easily understand that scores and measures may have been influenced by "incomplete" state.

#### Connected Checks

Since raw data is transmitted using various network technologies that are vulnerable to connection dropouts, it is of interest for data processing and also the user of SAP sailing software to know when a connection is or was lost. The connection to GPS-trackers can unfortunately not be tested with alive-pings but needs to be monitored based on average transmission rate and time-out threshold. The connected state of GPS-trackers and wind measurement units shall be logged and visualized in the application so that equipment failures as a reason for non-availability or changes of data and/or measures becomes apparent to the eye of the user.

#### Data Properties

The base properties of data like GPS sampling rate & number of active satellites are interesting to expert users and shall be accessible via the UI. Furthermore, accuracies of measurement shall be optionally visualized (or at least accessible via the UI) to avoid misinterpretations of the visualized races and belonging measures, e.g. the general accuracy of GPS positions is up to 20 meters which should be at least optionally be visualized especially when trying to clarify race protests of sailors.

#### Outlier detection

GPS-tracks sometimes contain intermittent jumps (caused by changes in the GPS satellite reception of the tracker and shadowing from other boats) that should be detected in order to ignore them because such jumps can drastically change the aggregate measures and lead to severely wrong data display. Similarly, wind measurements can suffer from very strong noise that should be detected to be able to display plausible wind information and construct a meaningful advantage line.

#### Correction of faulty raw data

For time series data, like GPS-tracks and wind measurements, detected faulty raw data may be corrected using interpolation or extrapolation. The essential ingredient is assuming a numerical model for boat movement or wind pattern which is calibrated by live raw data (or recorded raw data) and used to bridge detected intermittent raw data faults. The duration of time that may be bridged shall be limited by a time threshold above which equipment failures should be visualized, as described in paragraph "Connected Checks".

#### Robustness of Aggregate Calculations

Aggregate calculations depend on a set of raw data and are influenced by intermittent faulty raw data as determined by outlier state, as well as, incomplete raw data as determined by the completeness state. In order to do automatically identify constellations in which parts of the raw data due to faultiness or incompleteness should be ignored, all aggregate calculations have to incorporate outlier state and completeness state to compute plausible results also in special raw data constellations.

#### Overall Measures of System State

Based on the state information on completeness, connectedness and accuracy, measures describing the overall state of the system can be derived.

The "Overall Consistency" of the sailing solution summarizes completeness and connectedness. It is a value ranging from 0% to 100%, where 0% represents a state where fundamental connectivity, e.g. WAN for accessing GPS-trackers has been lost completely, whereas, overall consistency 100% is reached when all sensors are well-connected and race data is complete.

The "Overall Confidence" of the sailing solution is based on the numerical variance or deviations of measurements. Since for each type of data, different measurement units are used and different ranges of deviations are acceptable, the overall confidence cannot easily be represented by an average value of deviations, but has to be represented as distribution across quality categories, e.g. 40% "good", 35% "average", 25% "bad", where the percentages describe the portion of data sources in each quality category, e.g. number of GPS-trackers divided by total number of GPS-tracker.

### Synergies

The data consistency features belong closely to the calculation procedures implemented in the sailing analytics server.

### Risks

It may be hard to get access to domain experts who can tell us what exactly the inconsistencies are and which ones are important for which type of analysis.

### Prototype

n/a

### Further Information

* [[Meeting Minutes, December 2, 2013|wiki/planning/businesscases/dataconsistency20131202]]