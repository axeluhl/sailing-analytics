# NMEA
This page gives a short summary on the first attempt to integrate data from a sailing vessel's nmea network into the SAP Sailing Analytics during my three month internship from 01/17 - 03/17. The current code is a prototype and can not be integrated into any productive release.

## GOAL
The reason for the integration of NMEA data into the SAP Sailing Analytics is the assumption that the GPS of a sailing vessel is better than the GPS of a smartphone. Therefore it desireable to retrieve the location from the vessel's GPS if available. Furthermore most sailing vessels have additional wind sensors. These sensors could be used as an additional wind source for the SAP Sailing Analytics. This would allow a more detailed analysis of the wind conditions during a race.

## Documentaion
All the code regarding the NMEA functionality is on the branch "nmea" in SAP Sailing Analytics git. The current state of the is documented in NmeaDocu.pptx in the doc folder of the branch.