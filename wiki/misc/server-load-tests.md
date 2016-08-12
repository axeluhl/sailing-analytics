# Server Load Tests

It can make sense to put a server under heavy load to test how it reacts to such increased request numbers. One way to do that is to fire up instances that start a browser that points to an URL. There is an AMI that helps you doing this. Execute the following steps.

- Select the AMI `Browser Test 1.0`
- Make sure to select `m1.medium` instances. You can not use other instances as the number is limited. Amazon has only granted us to start more than 50 instances when they are of that type.
- Input the URL you want the browser to load into the User Data field. No quotes or such needed.
- Start up to `900` instances

Currently we can not use Spot instances as this is a separate limit pool. 

# A Typical Load Test Scenario

Run a typical live server instance. Import the master data for the event that you'd like to simulate, including wind and everything. Then load all races except for those that you'd like to simulate as being "live." For those, activate the "Simulate with "now" as start time" checkbox. If necessary, manually override the Race Committee start time in the leaderboard configuration panel of the AdminConsole.

Then, test a URL with your local browser on a simulated live raceboard. Use this URL as the User Details for firing up a number of "Brower Test" instances and observe how the load on the server is increasing as the instances come online.

You can then observe the server instance either by using a JVM profiler or by looking at it through JMX (see [here](wiki/jmx)).