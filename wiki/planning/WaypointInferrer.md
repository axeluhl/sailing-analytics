This page is used to think through the features a Waypoint-inferring mechanism needs to have from a end-to-end perspective.

### Scenario 1: Starting a race with no information about Marks and Waypoints. 

WaypointInferrer should detect possible Marks, decide for the PassingInstructions and then store the Waypoint persistent in the race

* If we decide for a Mark position based on a threeshold number of competitors already passed should we take in consideration the upcoming competitors for the final Mark position and if we do so how can we achieve that?

* First approach is to work only with smartphone tracked races. A check box in Smartphone Tracking connector is needed. Also SailInsight2.0 should have an option to use that feature.

