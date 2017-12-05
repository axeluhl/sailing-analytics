Description:

0. Requirements for use
  0.1 Configure AWS CLI
  0.2 Adjust default values
  0.3 Show manual

1. Structure 
  1.1 Files and their tasks
  1.2 Scenarios

2. Functionality
  2.1 Initialization of required variables
  2.2 Error handling

3. TODO
  3.1 Bug fixes
  3.2 Improvements
  3.3 Extensions

0. Requirements for use

- AWS CLI (https://aws.amazon.com/de/cli)
- Cygwin Packages:
-- tmux
-- jq
-- openssh
-- wget
-- curl

0.1 Configure AWS CLI

Enter "aws configure" into the console. Enter your Access Key and Secret Access Key of your AWS user.
If not already available, a corresponding key can be generated via the AWS Web console.

To do this, a privileged user navigates to the following entry within the AWS Web console:
IAM Console >> Users >> Your Username >> Security Credentials >> Create access key

0.2 Adjust default values

To avoid repeated input of user-specific information, the file aws_variables.sh contains default values ​​
for variables. It is possible to set the default region and default key name for instance.

0.3 Show manual

Call ./aws-setup.sh or ./aws-setup.sh --help to display instructions for usage.

1. Structure

1.1 Files and their tasks

aws-setup.sh:
Functions for parameter recording, sourcing and documentation. Entry point of program.

lib/functions_app.sh:
Functions for modifying the state of the SAP Sailing Analytics instance (event creation, password change, apache configuration).

lib/functions_ec2.sh:
Functions for creating EC2 instances and querying their attributes.

lib/functions_elb.sh:
Functions for creating load balancers, listeners, rules and target groups.

lib/functions_io.sh:
Functions for processing user input and initializing variables.

lib/functions_route53.sh:
Functions for creating Route53 entries.

lib/functions_tmux.sh:
Functions for executing commands via tmux and for constructing the user interface.

lib/functions_wrapper.sh:
Functions for error handling of functions and simplification of repeated calls.

lib/scenario_associate_alb.sh,
lib/scenario_associate_clb.sh,
lib/scenario_associate_elastic_ip.sh,
lib/scenario_instance.sh,
lib/scenarion_tail.sh:
See script-internal documentation

lib/util_functions.sh:
Features to facilitate user input, validation features and other potentially relevant helper functions.

lib/util_variables.sh:
Miscellaneous auxiliary variables (time stamp, script name, etc.)

lib/utils.sh:
Functions for the output of colored messages, logging and sourcing of remaining bash files

1.2 Scenarios

Scenarios are encapsulated execution units that are orchestrated by AWS and instance-specific functions 
to automate a specific use case.

Scenarios include the following functions:

- Function to start the execution of the scenario
- Function to check preconditions (dependencies of packages or environment variables) [optional]
- Function to ensure the initialization of variables needed
- Function to execute the program logic

2. Functionality

2.1 Initialization of required variables

Each scenario requires certain initialized variables for its execution. The assignment of a value to a variable takes place
either at the start of the script via the input of a parameter or if no parameter has been transferred, by input of the user after being prompted. 
When the user is prompted to enter a value for a variable, he is being offered a default value out of the aws_variables.sh file.
This facilitates user input because certain values, such as the standard key name to connect to an instance don't change very often. 

2.2 Error handling

To check the successful execution of a function, various strategies are used:

1. Checking the return value of a function

This strategy is applied to AWS-specific functions. When calling an AWS command (e.g. aws elb create-load-balancer) 
the return value is either 0 (error occured) or 1 (no error occured).

2. Checking the HTTP status code

When a curl command is called, the option -w "\ n% {http_code}" is also passed as a parameter. In doing so, response and HTTP code can be assigned
to two different variables, allowing separate processing. The HTTP status code can then be compared to the expected result, without loosing information 
about the actual response.

3. TODO

3.1 Bug fixes

- Creating user fails with message "Precondition failed (username does not meet policy)"

3.2 Improvements

- Factorize run-instance() function

3.3 Extensions

- list() function with output of all instance names and their DNS name, instance id, etc. so that --tail does not require the web GUI anymore
- More detailed output of intermediate results of the script via the console
- Add more scenarios (Multi-Instance-Setup, Shutdown, Replica Mgmt.)
- region-specific sourcing of files with variables, so that commenting out is no longer necessary


Tested under Microsoft Windows [Version 10.0.10586]:
- Cygwin 2.8.2(0.313/5/3)
- aws-cli/1.11.129
- tmux 2.8.2(0.313/5/3)
- openssh 7.5p1-1
- wget 1.19.1
- curl 7.53.0

=> Works (Route53 entry creation could not be tested yet)

Tested under Microsoft Windows [Version 6.3.96001]:
- Cygwin 2.9.0(0.318/5/3)
- aws-cli/1.11.166
- tmux 2.4
- openssh 7.4p1
- wget 1.19.1
- curl 7.55.1-1

=> Works (Route53 entry creation could not be tested yet)
