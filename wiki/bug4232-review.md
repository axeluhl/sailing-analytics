**This page has the purpose to prepare and track the progress of the bug4232 review**  
(See [bugzilla](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4232))  

# Test Scenarios

## Prerequisites
* two devices (Smartphone and/or Tablet)
* device configuration: bug4232 (so all settings are correct - or manual)
* server: https://dev.sapsailing.com
* refresh time: 20 sec (because of faster local refreshes)
* event: Extreme Sailing Series 2016 Act 8 Sydney

The edit dialog (open with the pencil) should always be closed if a warning (yellow or red) will be displayed. In case of a red warning, the publish button is always disabled.

One device will be called **A** and the other **B**, so they have unique names while testing.

## While starting

### Test 1
* devices open the penalty fragment
* device A give Omar Air OCS, device B set DNS for Omar Air
* device A closes the penalty view (without publishing)
* device B should see a red warning sign after the automatic refresh
* publish should be disabled and can be activated by changing every red item in the list

### Test 2
* same as [test 1](#test-scenarios_while-starting_test-1), but this time the result should be published
* same result as [test 1](#test-scenarios_while-starting_test-1).
* publish should be disabled and can be activated by opening every red item in the list

### Test 3 
* same as [test 1](#test-scenarios_while-starting_test-1), but after refresh device B should close the penalty view
* after refresh device A should also see the red warning sign, without the possibility to publish
* publish should be disabled and can be activated by opening every red item in the list

### Test 4
* both devices open the penalty fragment
* device A gives Omar Air OCS, device B sets DNS for Alinghi
* device A closes the penalty view (without publishing)
* device B should see both entries in the list after refresh

### Test 5
* same as [test 4](#test-scenarios_while-starting_test-4), but this time the result should be published
* same result as [test 4](#test-scenarios_while-starting_test-4).

### Test 6
* same as [test 4](#test-scenarios_while-starting_test-4), but after refresh device B should publish the merged result
* after the refresh device A also sees both entries

## While finishing

### Test 7
* both devices open result list
* device A adds Omar Air, Alinghi and LR BAR into the list
* device A closes the result list
* after refresh device B see the three competitors in the result list

### Test 8
* both devices open result list
* both devices add Omar Air to the result list
* device A sets the penalty to OCS and closes the result list
* device B should see after the refresh a yellow warning sign

### Test 9
* both devices open result list
* both devices add Omar Air to the result list
* both devices set the penalty to different values
* device A closes the result list
* device B should see a red warning sign and the data should show the values from device A
* publish should be disabled and can be activated by opening every red item in the list

### Test 10
* both devices open result list
* both devices add Omar Air to the result list
* device A changes to penalty view and sets the value to DNF and changes back to result list
* device B should see a yellow warning sign after refresh

### Test 11
* both devices open result list
* device A add Omar Air and Alinghi
* device B add Alinghi and Omar Air
* device A closes the result list
* device B should see red warning sign and the order Omar Air and Alinghi
* publish should be disabled and can be activated by opening every red item in the list

### Test 12
* both devices open result list
* device A add Omar Air and Alinghi
* device B add Omar Air and LR Bar
* device A closes the result list
* device B should see 1. Omar Air, 2. Alinghi and 2. LR Bar
* publish should be disabled and can be activated by opening every item in the list and clean the duplicate rankings

### Test 13
* devices open the penalty fragment
* device A give Omar Air OCS, device B set DNS for Omar Air
* device A pressed the home button
* device B should see a red warning sign after the automatic refresh
* publish should be disabled and can be activated by opening every red item in the list