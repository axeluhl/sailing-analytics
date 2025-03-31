# Sailing Domain Algorithms

Here is a draft description of the algorithms we use for the various non-trivial key figures displayed in our leaderboard.

All values are based on GPS tracks which are therefore subject to the usual GPS accuracy. Furthermore, all fixes that are considered outliers are not considered for calculations. A fix is considered an outlier if the object tracked would have had to move at a speed of 50 knots or more from the previous or to the next fix to reach the fix. Mark roundings, which define a competitors entry into and exit out of a leg, are currently provided to us by the tracking provider used. Therefore, these time points depend on their algorithms for mark rounding detection. We know that in particular TracTrac currently does not distinguish between lines and gates and for both uses the time point at which the tracked object crosses the line between the two marks as the mark rounding time point. For gates, this is not in full accordance with the ISAF definition of the mark rounding time for gates.

##  Current Speed over Ground (SOG)

All fixes in the open interval starting 4s before and ending 4s after the query time point are aggregated into a weighted average; the averaging algorithm used is exponential, with the weight of a fix halved every four seconds the fix is further away from the query time point.

## Distance traveled between two time points

Aggregates the great circle distance from fix to fix between the time points. For start and end, if the time point does not exactly match a fix, the tracked object's position at the query time point is estimated by linear interpolation between the adjacent fixes.

## Combined Wind

We aggregate a number of wind sources into a combined wind reading which is then used for further calculations. Wind sources are equipped with a general level of confidence. The higher the confidence, the higher the wind source's weight in the average computed across the wind sources available. The wind sources supported are: manual entry (confidence 0.9), measured (confidence 0.9), course layout at race start for an upwind start (confidence 0.3), estimation based on GPS-tracked beat angles across the fleet (confidence 0.5). Confidences are further reduced as the time between a measurement and the query time point increases. The confidence reduction is hyperbolic over the time difference, halved every 3s. For the wind estimation based on GPS-tracked beat angles, confidence is further reduced by small fleet sizes on one tack, as well as proximity to mark roundings and major direction changes such as maneuvers. The result is currently determined independently of the location for which the wind is queried.

##Leg bearing

For a given time point the position of the leg's start end end waypoint are determined. A gate's or a line's position is assumed to be half the way on the great circle between the two marks forming the gate or the line, respectively. The leg's bearing is the true bearing from the start waypoint's position at query time and the end waypoint's position at query time.

## Leg type

For a given time point, the leg type is determined based on the true wind direction at that time and the leg's bearing relative to the true wind direction. Legs whose bearing is pointing windwards within 45 degrees left or right are considered UPWIND legs. Legs pointing leewards within 45 degrees left or right are considered DOWNWIND legs. All other legs are considered REACHING legs.

##Velocity Made Good (VMG)

For UPWIND and DOWNWIND legs, the speed over ground is projected onto the combined wind direction to obtain the windward or leeward speed, respectively. For REACHING legs, the speed over ground is projected onto the leg's direction, resulting in the along-track speed. Note that during mark roundings the VMG may be very small or even zero although the speed over ground is positive.

##Average Cross-Track Error (XTE)

The arithmetic average across all fixes within a time interval of their distance to their respective leg's course middle line at the fix's time point (the great circle segment connecting the leg's start waypoint's position at the fix's time point with the leg's end waypoint's position at the fix's time point)

##Estimated Time of Arrival at Next Mark (ETA)

For UPWIND and DOWNWIND legs, this is the windward or leeward distance to the next mark divided by the VMG. For REACHING legs, this is the along-track distance to the next mark divided by the along-track speed.

##Gap to Leader (Time)

For the leader, this is obviously zero. For all other competitors, if the competitor is in the same leg with the leader, in UPWIND, DOWNWIND and REACHING legs, the gap is defined to be the windward, leeward and along-track distance, respectively, to the leader divided by the competitor's VMG at the query time point. If the leader is already in another leg, the competitor's ETA is used to which the time that passed since the leader passed the competitor's next mark is added.

##Windward Distance to Leader

For the leader, this is obviously zero. For all other competitors, if the competitor is in the same leg with the leader, in UPWIND, DOWNWIND and REACHING legs, the windward distance to leader is defined to be the windward, leeward and along-track distance, respectively, to the leader. If the leader is already in another leg, the competitor's windward/leeward/along-track distance to the next mark is used to which the windward/leeward/along-track distance of all subsequent legs the leader has already completed are added, plus the windward/leeward/along-track distance the leader has already completed in the leader's current leg.

##Average Velocity Made Good

For a given time point, the windward distance a competitor traveled in a leg up to that time point is computed. If the competitor has finished the leg already at that time point, the finishing time point is used instead. The windward distance is then divided by the time interval starting when the competitor entered the leg and ending at the given time point or the time point when the competitor finished the leg, whichever is earlier. Note that this is not the same as computing an arithmetic average of all the VMG values at all time points of a competitor's fixes in the leg. The individual VMG values are all computed based on the combined wind at their respective time point whereas the average is computed based on the wind at the time point specified or the time point the competitor finished the leg, whichever comes first. 

##Maneuver Loss (Distance)

The maximum speed over ground right before the maneuver begins is extrapolated to the time point after the maneuver when the competitor has again reached maximum speed over ground. The extrapolated position is then compared to the actual position, and the difference vector is projected onto the wind direction for UPWIND and DOWNWIND legs, and onto the leg's bearing for REACHING legs, respectively. The result is the windward/leeward/along-track distance lost by the maneuver.
