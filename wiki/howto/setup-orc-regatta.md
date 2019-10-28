# Setting up an ORC Regatta with Performance Curve Scoring (PCS)

[[_TOC_]]

## Introduction

The Offshore Racing Congress (ORC, see [https://orc.org](https://org.org)) has defined a process and set of [rules](https://www.orc.org/rules/ORC%20Rating%20Systems%202019.pdf) to measure, rate and rank boats in regattas that is based on the boats' performance as predicted by a so-called "[Velocity Prediction Program](https://orc.org/index.asp?id=21)" (VPP). The output of this program is a measurement certificate, as shown [here](https://data.orc.org/public/WPub.dll/CC/64934.pdf). Such a certificate contains predicted boat speeds for different true wind angles (TWAs) and wind speeds, displayed in table form. With this, boats in a regatta can be ranked according to their performance relative to their performance predicted.

The key concept behind this so-called *Performance Curve Scoring* is for each boat in a regatta to derive a mathematical function---the performance curve---based on the certificate's data and the course to sail that maps the wind speed on the course to the time the boat is expected to require to sail the course. In doing so, the course can be modeled at different levels of detail, depending largely on how stable and/or predictable the wind conditions are or were while sailing the course. If rather stable wind conditions are met on a leg, the leg can be defined as a "constructed" course where the leg's distance and true wind angle are specified. For long legs with changing wind conditions, boat performance is averaged across a profile of different assumed wind directions on that leg, with variants called *circular random* and *coastal long distance*.

The performance curve function for each boat can also be inverted, mapping the time elapsed for a competitor while sailing the course to the wind speed this competitor would have required according to the prediction to sail that course in this time. This wind speed is called the *implied wind*.

## The two Different Versions of Performance Curve Scoring

The process of how performance curve scoring is to be applied has changed in 2015. Now, it is up to the discretion of a regatta to decide which version to use.

### Before 2015 - Individual Implied Wind

With the performance curve of each boat in the fleet, for each competitor the implied wind for the course sailed can be computed. These implied wind speeds are used to rank the competitors in the race: the higher the implied wind, the better the boat ranks.

In order to obtain a corrected time for all boats that is consistent with the ranking by implied wind, the monotonous performance curve function of a selected boat---called the *scratch boat*---is evaluated for the implied wind values of each competitor, putting each competitors performance into the "coordinate system" of the scratch boat selected.

Different strategies for selecting a scratch boat exist, and there don't seem to be strict prescriptions for the selection. Regularly, the boat with the least "general performance handicap" (GPH), making it the fastest boat in the field on average, is used as scratch boat, but in other cases the winning boat may be selected. It can also happen that the boat that would regularly be selected as the scratch boat is disqualified or does not score in the race for other reasons. In this case, another scratch boat must be selected for corrected times calculations, and it may be an arbitrary one picked by the race committee. The selection does not influence the overall ranking; it only affects the differences and absolute numbers of the corrected times calculated for all competitors.

### Since 2015 - Corrected Times with General Implied Wind

In 2015 the congress suggested a different way of ranking races with performance curve scoring. Instead of using the implied wind values of each boat as the ranking criterion, one implied wind value---usually the maximum of all implied wind values obtained for the fleet---is applied to all competitors' performance curve to determine the time in which they are expected to finish sailing the course given under the implied wind speed selected. The actual elapsed times of each boat can then be compared to their individual time predicted, and the difference is used for ranking the fleet.

The selection of the single implied wind speed used to determine all time allowances may also be overruled by the race committee, e.g., in case there is concern that even the maximum implied wind speed achieved during the race is not close to the actual wind speed on the course at the time of sailing. Alternatively, e.g., in case of separately ranking sub-groups of a larger fleet, a sub-group's implied wind speed used for ranking may come from the overall group of boats, assuming that in a larger group of competitors chances are better to achieve a maximum implied wind speed that is close to the actual wind speed on the course.

With this method, corrected times are originally provided as a relative difference only. If the highest implied wind speed achieved was used for ranking and the best-ranking boat stayed in the range between six and 20 knots of implied wind, that boat's relative corrected time would be 00:00:00 because her elapsed time led to the implied wind used again to compute the allowance, which---by definition of the inverted performance-curve function---has to provide the elapsed time again for this competitor.

In order to obtain absolute corrected times, similar to the scratch boat selection of the pre-2015 method, some baselining has to occur. Here, selecting a "scratch boat" makes that boat's elapsed time the baseline for applying all relative corrected time differences, simply by adding them. For example, if the boat winning the race is selected as this scratch boat and that boat stays within the 6-20kts implied wind speed range, that boat's relative corrected time will be 00:00:00 and all other boats' relative corrected time will be greater than this because they ranked worse, and hence the winning boat's corrected time under these circumstances would equal its elapsed time, whereas all other boat's corrected time results from adding their relative corrected time to the winning boat's elapsed time.

## Configuring ORC Performance Curve Scoring

The SAP Sailing Analytics support scoring regattas using the different flavors of ORC Performance Curve Scoring (PCS). This requires a number of configuration steps:

* selecting the variant of ORC PCS (see above)
* providing the measurement certificates for all boats in the fleet
    * for the entire regatta
    * individually overriding for single races in the regatta
* Optionally:
    * define the course legs explicitly instead of using tracking data for course geometry and TWAs
    * select a scratch boat explicitly
    * define the implied wind to use for ranking explicitly

### Creating a Regatta with Ranking Based on ORC Performance Curve Scoring

### Managing Certificates

### Managing Courses

### Scratch Boat Selection

### Implied Wind Selection