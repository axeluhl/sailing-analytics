This page is used to think through the features a Waypoint-inferring mechanism needs to have from an end-to-end perspective.

### Version 2:

WaypointInferrer should detect Marks and Waypoins in a tracked race and decide wether to add new ones to the race or update given information about the race (e.g. waypoint list already prepared, positions pinged for all marks except the windward mark, so WaypointInferrer should detect that mark position.)

The WaypointInferrer should be treated similar to the MarkPassingCalculator, the information is only stored in-memory, every reload of a tracked race would need the algorithm to start again.

A checkbox in smartphone tracking connector next to start tracking button is needed. If checked the race is tracked with an instance of WaypointInferrer.


### Next steps:
 * Implement the checkbox and the restore functionality.
 * Improve the WaypointInferrer algorithm to match the desired behaviour.
 * Create test cases with real data and scenarios.