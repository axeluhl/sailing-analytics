# Operating Igtimi WindBots

For wind measurements we're trying to consistently use Igtimi WindBot equipment. The AdminConsole has a tab "Igtimi Accounts" where accounts for the Igtimi YachtBot web site can be managed. WindBots associated with any of these accounts will feed their wind readings, when active, into races being tracked. Operations of the WindBots mainly needs to take care of charging the devices, turning them on when they leave the harbor and turning them off when they leave the race course. This can also be delegated to race officials carrying these devices on their vessels.

# Status LEDs

The devices have three LEDs whose status works as follows:

1. The three LED’s are “power”, “connection”, and “GPS” in order from top to bottom.
2. All the lights should be on when operating normally.
3. Slow flashing is normal while booting.
4. Fast flashing means that function can’t start or has failed - e.g. SIM not inserted.  i.e. you shouldn’t see this
5. During sync, the “power” light blips and the “connection” LED should stay on until the sync has finished.

More information on running the WindBots can be found [here](https://igtimi.desk.com/customer/portal/articles/1494533-yachtbot-tracker-basic-operation?b_id=1690).

# Operating near country borders

Confirm signal strengths by enabling cell debugging by adding two lines in the “WindBot prerequisites” section right at the top…

```
log file debug raw
log debug cell on
```

The device will already switch between carriers if an alternative carriers signal is stronger.  We don’t have any control over this – it’s a low level cellular protocol thing. Brent can help with forcing carrier selection using the “CELL OPERATOR <X>” command.  It can be a bit hit-and-miss to get <X> right sometimes, but if we send him logs with the debugging on, he should be able to give us a config change that forces the connection to stay with the carrier that we see in the logs.
Todo: clarify what to look for in the logs

# Update Firmware

[How to update the firmware](https://igtimi.desk.com/customer/portal/articles/1122866-how-to-update-the-firmware-in-the-yachtbot-trackers?b_id=1690)

# WindBot User Guide (v. 1.0 R3)

[User Guide v. 1.0 R3](/wiki/uploads/WindBot%20User%20Guide%201.0%20R3.pdf)

# Is the WindBot waterproof and does it sink if dropped it in the water?

Yes and yes.