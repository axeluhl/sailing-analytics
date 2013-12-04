# RaceCommittee App (User Guide)

[[_TOC_]]

## Introduction

This user guide is intended for race officers and other users of the RaceCommittee App. The document assumes that you have been handed a fully configured device (see [[guide for device administrators|wiki/racecommittee-app-administrator]]).

## Logon

Each race officer is in charge of managing the races of a course area (Alpha, Bravo, Off-Shore,...). After selecting the active event on the left pane, click on your course area.

<img src="" title="Logon onto course area" />

In the dialog screen choose "_Login as race officer (start vessel)_". Press **Login**. From now on you will see in the lower right corner the RaceCommittee App icon signaling that you are managing this course area. Using this notification icon you are always able to return back to your race management screen.

## Race management

On the race management screen you see all races grouped by their regatta and fleet on the left side. By default the race list is filtered showing only active races. Use the top action bar to switch to "show all races" if you need to see all.

<img src="" title="Race management screen" />

Clicking on a race in the race list opens up the race information screen on the right pane. This screen allows you to umpire a race.

Clicking the back button on your device or touching the home icon (top left corner) logs you out of the active course area.

### Scheduling a race

One of the most important task in the life of a race officer is scheduling her races. Whenever you select a **unschedulded race** the race information screen asks you to select a racing procedure and a start time. By default a start time in the near future is selected.

<img src="" title="Scheduling a race" />

When you are satisfied with the start time selected (hint: click on 'Today' to select a different date) and racing procedure, click '**Start**'. As you will see below you are always able to reschedule a race. Therefore the best race officers schedule multiple races at once. After scheduling the first race of the day, just select the next race in the race list and set a estimated start time.

On the same race information screen you are able to **postpone** an unschedulded race. Click on 'Postpone' and select the postponment flags.

#### Scheduling a race in the past

It is perfectly fine to schedule a race into the past. The RaceCommittee App will simulate the race and its startphase up until the present time. When scheduling a race in the past it might happen that the app asks you for additional information regarding the startphase of the selected racing procedure (e.g. setting a startmode flag).

### Managing the startphase

Selecting a scheduled race will take you to the startphase race information screen. On this screen you are able to manage the startphase of the race's racing procedure. While the available actions vary between the racing procedures you always see the following:

* Countdown to start
* Countdown to next flag change
* Currently displayed and removed flags

<img src="" title="Startphase" />

Regardless of the racing procedure you're able to **abort** (AP flag) and **reschedule** the race. 

### Managing an on-going race

While a race is on-going the race information screen shows the running race information screen. 

You are still able to **abort** the race (AP or November flag) and signal a **general recall**. Both actions will unschedule the race, taking you back to the schedule race screen.

<img src="" title="Running race" />

If active for your configuration/regatta you are able to signal an **individual recall** (X-Ray flag). Currently there you're not able to enter the competitors violating the starting line. The X-Ray flag will be removed after 4 minutes automatically. You can always remove it before the timer terminates.

When the race is about to finish (finishing vessel displays blue flag, first finisher crosses the line, last leg,...) click the blue flag to **start the finishing phase**.

### Finishing a race

While a race is finishing the race information screen will show you the finishing phase.

On this screen you're able to **abort** (November flag) the race or **end** the race. To end the race click on the blue flag.

### Further management activities

Besides the normal race management workflow described above there are additional management activities which can be executed on a race at any time. See also [Signaling a course change](#coursechange).

#### Protest time

If active for your configuration/regatta you can click the **bravo flag** on a fleet's header in the race list to signal the start of the protest time for this fleet.

<img src="" title="Setting the protest time" />

In the dialog window select all races of the fleet whose protest time should be set. By default all already finished races are selected.

## Signaling a course change
<a name="coursechange"></a>

## Troubleshooting

The following sections will help you with some common problems.

### Race reset

### Connectivity problems

### Crashes