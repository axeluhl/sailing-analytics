## Architecture

### Overview
The orchestration project consists out of two components. The orchestrator on the one hand and the agent on the other.
The orchestrator takes care on all actions in regards of AWS and to execute the agent. The agent will be copied via ssh to a target system and then called with a subset of stages to execute. The actual implementation of e.g. handling apache2, is done inside the agent. Once the agent execution of a stage is finished the return will be transferred back to the orchestrator, who continues with the workflow.

![architecture](https://wiki.sapsailing.com/wiki/images/orchestration/architecture.png)

### Build and Test
Currently we provide a Makefile, which implements all required steps to build and test the software. Optionally you can of couse also use go test directly. Just make sure the `PREFIX` enviroment is set and points to the working directory of the orchestrator. You can also `source env.sh` to do this and also set the `GOPATH` correctly. By default, all go dependencies should be downloaded to `$HOME/.go`.

To build the software, just call `make build`. Make sure that all dependencies are downloaded before via `make dep`. 
To test the software, just call `make test` and to check the tests, call `make checktest`. The verbose output of the logs is written to a folder called testlogs (which initially needs to be created).

## Get and prepare the software
You can get the current release of the orchestration software from [here](http://static.sapsailing.com/orchestration-0.0.1_7f7e4e40d30f21ffeb8495a370a540c23c496745.tgz). The releases of each build process can be downloaded directly on hudson via archived artifacts. The currently set up hudson job is called `orchestration-master` and can be started manually (runs on the archive failover, where go is installed).

![hudson artifacts](https://wiki.sapsailing.com/wiki/images/orchestration/hudson-artifacts.png)

Once downloaded, you just need to extract the archive locally to your machine. You will find the orchestrator and agent binaries in bin and all configurations in configs (like the instance templates or secrets).
To get going, place your private key inside the secrets folder and name it `id_rsa`. Furthermore, please create an according `conf_aws.json` file (copy `conf_aws.json.sample` and fill in the values). A good example is

```
{
  "AWS_ACCESS_KEY_ID": "<your access key id>,
  "AWS_SECRET_ACCESS_KEY": "<your secret access key>",
  "AWS_DEFAULT_REGION": "eu-west-1",
  "AWS_ELB_NAME": "Sailing-eu-west-1",
  "AWS_SNS_TOPIC_ARN": "arn:aws:sns:eu-west-1:017363970217:SAPSailing-General-Alerting"
}
```

You are ready to use the orchestration tool! Please note, that currently only linux-adm64 versions are built. Windows versions could be easily added, as go can also run under windows and the project currently does not have any hard dependencies to linux.

## Run the software
All you need is to source the environment file env.sh, which you can find in the root folder. You will need to source it via

```
source env.sh
```

and then you can run the orchestrator using

```
./bin/orchestrator-linux-amd64 <commands>
```

As we use loglevels, you can increase logging messages to another level by specifying a ENV variable in front of the orchestrator command, like:

```
LOG=DEBUG ./bin/orchestrator-linux-amd64 <commands>
```

### General
In general, the orchestrator accepts a set of sub parameters. The first level describes the type of action to execute (like a use-case). The following parameters behind, are used to specify the use-case parameter with details like servername.

To show all available <commands> just call the orchestrator without any parameters.

Non use-case specific <commands> can be, to get the `version` of the orchestrator. To do so, just use 

```
./bin/orchestrator-linux-amd64 version
```

It will show you the version, build revision (git commit) and the branch, from where the software was built (ideally you are using a version from master).

If you want to skip some steps done by the orchestrator on AWS level, you can use a command called `agent-only` as parameter. This will require a empty EC2 instance, where only agent actions (like bootstrapping,...) are executed. This step helps do debug actions of the agent, as you do not need to fire up a new EC2 instance all the time, but can use a already existing one. A sample call of this command looks like

```
./bin/orchestrator-linux-amd64 agent-only event="test-steffen123-1:1. Steffen Liga" servername=test-steffen123-1 internalhostname=ec2-34-242-13-180.eu-west-1.compute.amazonaws.com externalip=34.242.13.180 internalip=172.31.22.105 scenario=master analyticsenvironment=live-master-server analyticsrelease=build-201807231413
```

All those parameters are parsed by the orchestrator, when using the full EC2 bootstrap process. Some of the parameters are also optional (like `analyticsrelease`).

### Use case: Create Master Event Server
This is the first ready to use and implemented use-case. It will spawn a new EC2 instance (on the AMI base called "NEXTGEN" (using Ubuntu 16 with systemd as base) and configure a new java instance on it. Furthermore it makes sure, that apache2 is configured correctly and if so the workflow continues to create according target groups (default and master), attach the just created EC2 instance, create according rules in the load balancer and finally creating an Cloudwatch alarm for all target groups to get notified, if a failure occurs inside a target group.

The workflow can create multiple events on a java instance and configure apache2, EC2 load balancing and the java application itself (event creating, user creation, password change, ...).

To use the workflow you simply use the orchestrator command `master`. A sample could look like:

```
./bin/orchestrator-linux-amd64 master event="steffen2018:1. Steffen Liga" admin-password=supersicher123 user-name=steffen user-password=supersicher123 env=live-master-server release=build-201807231413
```

The following parameters are required for the master use-case:

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
- release=<release to use from releases.sapsailing,com>
- notify=<email for notify parameter in env.sh>
```
