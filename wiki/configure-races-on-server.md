# Configure Races On Server

After a restart of a java production server that contained archived races one need to reconfigure all these archived races. Here you find the steps needed including all druid knowledge required :-)

### Default excludes for races

* Do not have a boat class
* Time higher than 20:00 (e.g. 21:14)
* Have Test somewhere in the name

### General process

* Select TracTrac Configuration
* Load races list
* Select correct regatta
* Select races according to the rules below

### Desaster recovery

### Table with associations

| TracTrac Configuration Name | Regatta Name | Race Rules |
|:-----------|------------:|:------------:|
| 49 European Championship      |        49er Qualification Round 1 |     Yellow, Blue     |
| 49 European Championship      |        49er Qualification Round 2 |     Silver, Gold (except Gold Race 9)  |
| 49 European Championship      |        No Regatta |     All except _default excludes_  | 