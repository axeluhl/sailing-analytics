**This page has the purpose to prepare and track the progress of the bug4232 review**  
(See [bugzilla](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4232))  

# Test Scenarios

## Prerequisites
* two devices (Smartphone and/or Tablet)
* device configuration: bug4232 (so all settings are correct - or manual)
* server: https://dev.sapsailing.com
* refresh time: 20 sec (because of faster local refreshes)
* event: Extreme Sailing Series 2016 Act 8 Sydney

One device will be called **A** and the other **B**, so they are have unique names while testing.

## While starting

### Test 1
* devices open the penalty fragment
* device A give Omar Air OSC, device B set DNS for Omar Air
* device A closes the penalty view (without publishing)
* device B should see a red warning sign (without active publish button) after the automatic refresh

### Test 2
* same as [test 1](#test-scenarios_while-starting_test-1), but this time the result should be published
* same result as [test 1](#test-scenarios_while-starting_test-1).

### Test 3 
* same as [test 1](#test-scenarios_while-starting_test-1), but after refresh device B should close the penalty view
* after refresh device A should also see the red warning sign, without the possibility to publish

### Test 4
* both devices open the penalty fragment
* device A give Omar Air OSC, device B set for Alinghi the value DNS
* device A closes the penalty view (without publishing)
* device B should see both entries in the list after refresh

### Test 5
* same as [test 4](#test-scenarios_while-starting_test-4), but this time the result should be published
* same result as [test 4](#test-scenarios_while-starting_test-4).

### Test 6
* same as [test 4](#test-scenarios_while-starting_test-4), but after refresh device B should publish the merged result
* after the refresh device A also see both entries
