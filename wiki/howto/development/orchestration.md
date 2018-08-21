## Architecture

### Overview
The orchestration project consists out of two components. The orchestrator on the one hand and the agent on the other.
The orchestrator takes care on all actions in regards of AWS and to execute the agent. The agent will be copied via ssh to a target system and then called with a subset of stages to execute. The actual implementation of e.g. handling apache2, is done inside the agent. Once the agent execution of a stage is finished the return will be transferred back to the orchestrator, who continues with the workflow.

![architecture](https://wiki.sapsailing.com/wiki/images/orchestration/architecture.png)

### Build and Test
Currently we provide a Makefile, which implements all required steps to build and test the software. Optionally you can of course also use `go test` directly. Just make sure that the `PREFIX` enviroment is set and points to the project directory (contains `configs` and `bin` subdirectories). You can also use `source env.sh` to do this, which on top sets the `GOPATH` correctly. By default, all go dependencies should be downloaded to `$HOME/.go`. They should not be stored in the git project location.

To build the software, just call `make build`. Make sure that all dependencies are downloaded before via `make dep`. 
To test the software, just call `make test` and to check the tests, call `make checktest`. There are also make steps for `fmt` and others. Just check out the Makefile in the project root. 
The verbose output of the test logs is written to a folder called `testlogs` (which initially needs to be created).

### Develop
As the only Plugin for Eclipse is not really maintained anymore, we do not recommend to use it (goclipse). There is actually also no reason for it, as we are using Go and not Java or any OSGI features.
As go is pretty simple and straight forward, you can use any Go supported IDE as stated on https://golang.org/doc/editors.html.

Anyways, we recommend either using Goland (from IntelliJ), VS Code or Atom with respective go-plus Plugin.
All you need to do after [installing go](https://golang.org/doc/install) and the IDE, is to make sure to set the above mentioned environment variable `PREFIX` to the code directory inside your IDE, so that tests can be successfully executed. Furthermore make sure to set the `GOPATH` correctly, create a directory (outside of the git workspace) and point the `GOPATH` to it. Then, add the code directory from git also to the `GOPATH`. This will result in the fact, that go will download all external dependencies with e.g. `make dep` or `go get` to the first folder and not put everything into the code directories. You will find all dependencies needed for the project inside the top lines for the `makefile`.

An example for a correct `GOPATH` could be: `GOPATH=/home/steffen/.godep:/home/steffen/sailing-automation/`

Happy Coding!

## Get and prepare the software
You can get the current release of the orchestration software currently from [here](http://static.sapsailing.com/orchestration-0.0.1_7f7e4e40d30f21ffeb8495a370a540c23c496745.tgz) until a release model is decided. The releases of each build can be downloaded directly on hudson via archived artifacts. The currently set up hudson job is called `orchestration-master` and can be started manually (runs on the archive failover, where go is installed).

![hudson artifacts](https://wiki.sapsailing.com/wiki/images/orchestration/hudson-artifacts.png)

Once downloaded, you just need to extract the archive locally to your machine. You will find the orchestrator and agent binaries in `bin` and all configurations in `configs` (like the instance templates or secrets).
To get going, place your private key inside the secrets folder and name it `id_rsa`. This key must be pre-deployed in the used AWS AMI image. Furthermore, create a file called `conf_aws.json` (copy `conf_aws.json.sample` and fill in the values). A good example is

```
{
  "AWS_ACCESS_KEY_ID": "<your access key id>,
  "AWS_SECRET_ACCESS_KEY": "<your secret access key>",
  "AWS_DEFAULT_REGION": "eu-west-1",
  "AWS_ELB_NAME": "Sailing-eu-west-1",
  "AWS_SNS_TOPIC_ARN": "arn:aws:sns:eu-west-1:017363970217:SAPSailing-General-Alerting"
}
```

Inside the `config` folder you will find a folder called `instance_templates`. You can pre-define variables for launching EC2 instances. A valid example is

```
{
  "image": "ami-839f8769",
  "instance-type": "t2.medium",
  "vpc": "vpc-14b3477f",
  "subnet": "subnet-eab34781",
  "count": 1,
  "security-groups": [
    "sg-eaf31e85"
  ],
  "tags": {
  }
}

```

You are ready to use the orchestration tool! Please note, that currently only linux-amd64 versions are built. Windows versions can be easily added, as go can also run under Windows and the project currently does not have any dependencies to linux.

## Run the software
You can run the orchestrator using

```
./bin/orchestrator-linux-amd64 <commands>
```

As we use log levels, you can increase logging verbosity to a different level by specifying a ENV variable in front of the orchestrator command, like:

```
LOG=DEBUG ./bin/orchestrator-linux-amd64 <commands>
```
We offer the following log levels: `DEBUG`, `INFO` (default), `WARNING`, `PANIC`, `FATAL`. If execution fails, you may re-execute the orchestrator using a higher log level to get more insights, what has happened.

### General
In general, the orchestrator accepts a set of arguments. The first level of parameters describe the type of action to be executed (basically a use-case). The following arguments are used to specify the use-case specific parameters.

To show all available commands just call the orchestrator without any parameters.

Non use-case specific commands are example commands like the `version` of the orchestrator. To do so, just use 

```
./bin/orchestrator-linux-amd64 version
```

It will show the `version`, `build revision` (git commit) and the `branch`, from where the software was built. The agent should have the same version as the orchestrator and will be transferred to a target at each execution to make sure the target has the correct version.

If you want to skip some steps done by the orchestrator on AWS level, you can use a command called `agent-only` as parameter. This will require a empty EC2 instance, where only agent actions (like bootstrapping,...) are executed. This step helps do debug actions of the agent, as you do not need to fire up a new EC2 instance all the time, but can use a already existing one. A sample call of this command looks like

```
./bin/orchestrator-linux-amd64 agent-only event="test-steffen123-1:1. Steffen Liga" servername=test-steffen123-1 internalhostname=ec2-34-242-13-180.eu-west-1.compute.amazonaws.com externalip=34.242.13.180 internalip=172.31.22.105 scenario=master analyticsenvironment=live-master-server analyticsrelease=build-201807231413
```

### Use case: Create Master Server
This is the first ready to use and implemented use-case. It will spawn a new EC2 instance (you need to use an AMI in your instance template from the "NEXTGEN" base using Ubuntu 16 with systemd) and spawn/configure a new java instance on it. Furthermore it makes sure, that apache2 is configured correctly and if so the workflow continues to create target groups (default and master), attach the just created EC2 instance, create according rules in the load balancer and finally create an Cloudwatch alarm for all target groups to get notified, if a failure occurs inside a target group.

If a step during execution fails, the orchestrator decides if it can continue or not. If not the execution is stopped and actions done via the agent are rolled back. Rollback of AWS features is not yet implemented.

The workflow can create multiple events on a java instance and configure apache2, EC2 load balancing and the java application itself (event creation, user creation, password changes, ...).

To use the use-case you simply use the orchestrator command `master`. An example could look like:

```
./bin/orchestrator-linux-amd64 master event="steffen2018:1. Steffen Liga" admin-password=supersicher123 user-name=steffen user-password=supersicher123 env=live-master-server release=build-201807231413
```

The following parameters are required for this use-case:

```
- event=<hostname>:<eventname> (the command can be repeated to create multiple events on the java server)
- admin-password=<password of default admin account>
- user-name=<name of the new user>
- user-password=<password of the new user>
- env=<environment for env.sh from releases.sapsailing.com>
```

Optionally you can specify the following parameters:

```
- template-file=<path to template file>
- release=<release to use from releases.sapsailing.com, default: get latest>
- notify=<email for notify parameter in env.sh>
```
