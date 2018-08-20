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
