# Simulator Integration

The SAP Sailing Simulator, as can be seen, e.g., [here](http://www.sapsailing.com/gwt/Simulator.html), has a number of features that could nicely be integrated with the SAP Sailing Analytics. For example, at any point in a    tracked race the user could ask the simulator for its prediction of the optimal course and then compare that to the course actually sailed.

Backlog:
- make simulation available as overlay on sailing analytics referring to the currently selected leg
  - integrate simulator with measured spatial wind
  - race simulation overlay executing simulation and visualizing simulation results (first version done)
  - integration with polar sheets required, in order to have beat/jibe angles & speed available
  - user interface of race simulation to be clarified: user options, UI elements, etc.

- visualize ladder rungs, i.e. advantage line plus intermediate isochrones in distance-steps or time-steps
  - for average wind based on convex hull of polar diagram (generalization of advantage line to 360-degree-view)
  - for spatially resolved wind based on simulation

- use multiple wind sources to derive a spatially resolved wind field and use it in calculation of ladder rungs and simulation of courses
  - solved with Igtimi equipment
  - current measurements are required to improve accuracy of polar sheets and race simulation