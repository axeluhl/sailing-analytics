This page is used to think through the features a Waypoint-inferring mechanism needs to have from an end-to-end perspective.

### Scenario 1: Starting a race with no information about Marks and Waypoints. 

WaypointInferrer should detect possible Marks, decide for the PassingInstructions and then store the Waypoint persistent in the race:

 * First approach is to work only with smartphone tracked races. <del>A check box in Smartphone Tracking connector is needed.</del> The checkbox should be integrated in the CourseLayoutEditor. Also SailInsight2.0 should have an option to use that feature.
 * <del>If that checkbox is checked the inferred marks and waypoints should also appear in CourseLayoutEditor.</del> They can then be updated and/or refined. The EditMarkPositions feature on the RaceMap is then also enabled. 
 * That checkbox is default <del>unchecked</del> not checkable. The box becomes checkable only if the correct course-designer is chosen in regatta configuration. If a race is reloaded the stored Waypoints are used. The WaypointInferrer can be forced to detect Waypoints in a race that already has some by checking the box and saving the CourseLayoutEditor. Then it will delete the Waypoints already in the race before trying to infer new ones, those will be stored. Old ones are lost, but the Marks are still avalaible so there is a possibility to restore the original Waypoints manually.
 * There should be a course-designer in the regatta configuration with the effect <del>to check</del> to make the box checkable and prevent the RaceManagerApp from interferring with the WaypointInferrer.

 * If we decide for a Mark position based on a threeshold number of competitors already passed should we take in consideration the upcoming competitors for the final Mark position and if we do so how can we achieve that?

### Next steps:
 * Implement the above mentioned mechanisms to update a race.
 * Improve the WaypointInferrer algorithm.