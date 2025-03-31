## General

This package contains lambda functions related to fix ingestion. It is based on AWS Lambda. AWS Lambda is a compute service that runs your code in response to events and automatically manages the compute resources for you, making it easy to build applications that respond quickly to new information. In this case we're responding to fix ingestion events that are sent through by devices. A fix in this case is currently assumed to contain GPS data but this is not carved in stone.

1. Devices send their fixes to https://fix-ingestion-eu-west2.sapsailing.com
2. Sailing Server endpoints register themselves with one or more device identifiers at https://endpoint-registration-eu-west2.sapsailing.com
3. Once a device sends fixes, these are being stored to S3 and then forwarded to registered endpoints
4. A compressor runs every 5 minutes and combines multiple fix files into one

## Architecture

The current architecture consist of the following parts in eu-west-2 (London).

- A route 53 entry that routes entries for domains
    - fix-ingestion-eu-west2.sapsailing.com
    - endpoint-registration-eu-west2.sapsailing.com
- An ALB that serves as the target for the DNS entries and routes data to respective target groups (FixIngestionLambda-1786459421.eu-west-2.elb.amazonaws.com)
- Two target groups that handle ALB traffic and proxies it to the lambdas
- Three Lambdas
    - FixIngestion (arn:aws:lambda:eu-west-2:017363970217:function:FixIngestion)
    - EndpointRegistration (arn:aws:lambda:eu-west-2:017363970217:function:EndpointRegistration)
    - FixCombination (arn:aws:lambda:eu-west-2:017363970217:function:FixCombination)
- One S3 bucket to hold the lambda binaries (arn:aws:s3:::sapsailing-lambda-functions-bucket-eu-west-2)
- One S3 bucket that holds single fixes (arn:aws:s3:::sapsailing-gps-fixes)
- One database running as a ElastiCache instance on Redis (arn:aws:elasticache:eu-west-2:017363970217:replicationgroup:fixingestionrediscache)
- A CloudWatch Event to trigger the execution of FixCombination Lambda (arn:aws:events:eu-west-2:017363970217:rule/FixCombinationLambda_Cron)

## Development

Development takes place in Eclipse running on Java 8. Make sure to install the official plugin from https://aws.amazon.com/eclipse/. Lambdas are classes that need to implement a specific interface and a method that gets called upon execution. See https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html for documentation. For our lambdas we have opted to use the RequestStreamHandler that just provides us with the binary stream. This makes it easier to accept different types of inputs (e.g. a GPS fix and a Bravo fix).

As the project is currently in development, the CloudWatch Event for the FixCombination Lambda is disabled. When you want to test the functionality of the Lambda, trigger a test on the AWS page for the Lambda itself or activate the Event in the Event Bridge console of AWS.

## Deployment

Before you can deploy you need to make sure that you have created an access key and stored the key to your machine. If you use a MFA token then make sure to read the last section of this document.

Deployment can easily be done by right clicking on the lambda class and selecting Amazon Web Services -> Upload function to AWS Lambda. Make sure to select the correct mapping of the class to the lambda endpoint. Also you need to select the IAM role arn:aws:iam::017363970217:role/fixstorageendpoint-lambda-role for ALL endpoints you are deploying.

> Notice: With the current environment in Eclipse 2022-06, it is not possible to use the AWS Toolkit in Eclipse due the fact that it is built with 
> Java 8 which is not allowed as an startup environment for Eclipse 2022-06. Use the described method for the deployment with Maven.

Alternatively, you can build the project with Maven, but you have to take some precautions to be able to build the package. The pom.xml of the project ``com.sap.sailing.ingestion`` declares dependencies to the following other projects of the codebase to reuse code for types, deserializers, etc.:
- com.google.gwt.dev
- org.json.simple
- com.sap.sse.common
- com.sap.sse.datamining.annotations
- com.sap.sse.shared.android
- com.googlecode.java-diff-utils
- com.sap.sailing.domain
- com.sap.sailing.domain.common
- com.sap.sailing.domain.shared.android
- com.sap.sailing.geocoding
- com.sap.sse.datamining.shared
- com.sap.sse
- com.sap.sailing.declination
- com.sap.sse.security
- com.sap.sse.operationaltransformation
- com.sap.sse.mail
- com.sap.sse.replication.interfaces
- com.sap.sse.security.persistence
- com.sap.sse.security.interface
- com.sap.sse.mongodb
- com.sap.sse.replication
- com.sap.sse.replication.persistence
- com.sap.sailing.shared.server
- com.sap.sailing.shared.persistence
- com.sap.sailing.server.gateway.interfaces
- com.sap.sailing.server.gateway.serialization
- com.sap.sailing.server.gateway.serialization.shared.android

You have to make sure that each of the projects is installed to your local maven repository, so that the pom.xml of the ingestion project can use them. For this case there exists a new profile ``with-aws-lambda`` in the pom.xml of the java directory (java/pom.xml) which can be used to install all the required dependencies to your local maven repository. But to use this pom effectively it is useful to know which profiles are currently active in your environment to be able to exclude the ones you don't want. Go to the root directory of the git repository and use the following command to display all active profiles:
```
mvn help:active-profiles
``` 
Afterwards, you can use the following command to activate the new profile and to disable the other ones.

```
mvn clean -Dmaven.test.skip=true install -Pwith-aws-lambda,-sailing.analytics,-with-not-android-relevant,-non-leandi
```

In this example, the profiles ``sailing.analytics``, ``with-not-android-relevant`` and ``non-leandi`` will be disabled. This command will also create the ``bin`` folder in the project directory ``java/com.sap.sailing.ingestion`` and install the resulting JAR files to your local maven repository. When the dependencies of the project are changed please also make sure to update the java pom.xml respectively. After the initial install of the dependencies it is also possible to use the following command to build the package which can be deployed to AWS:

```
cd java/com.sap.sailing.ingestion
mvn clean -Dmaven.test.skip=true package
```

This will produce a JAR file in the project's ``bin`` folder. To deploy it, you need to have a valid AWS session token in your environment. See the `configuration/aws-automation/awsmfalogon.sh` script that you need to invoke with the ARN of your MFA device and the MFA token that you read from your MFA app, for example, in your GIT root:

```
. configuration/aws-automation/awsmfalogon.sh "arn:aws:iam::017363970217:mfa/axeluhl" 123456
```

where `123456` is the example MFA token produced, e.g., by your authenticator app. To find out the ARN of your MFA device, go to [https://us-east-1.console.aws.amazon.com/iam/home#/users/yourusername?section=security_credentials](https://us-east-1.console.aws.amazon.com/iam/home#/users/yourusername?section=security_credentials) and copy the ARN from "Assigned MFA device". It should be something like `arn:aws:iam::017363970217:mfa/yourusername`. As you're "sourcing" the script using the "." notation, you will end up with a few `AWS_...` variables in your environment that encode the MFA session token and access key.

You also need to be aware of the AWS region you'd like to deploy the lambdas to. Please always use the ``--region`` parameter to avoid any ambiguity.


Deploy it using the following command. Use the function name for ``${function-name}``, such as ``FixIngestion`` or ``EndpointRegistration``, respectively:

```
aws --region eu-west-2 lambda update-function-code --function-name ${function-name} --zip-file fileb://java/com.sap.sailing.ingestion/bin/com.sap.sailing.ingestion-1.0.0-SNAPSHOT.jar
```

The output should look something like this:

```
{
    "Version": "$LATEST",
    "CodeSize": 23695265,
    "VpcConfig": {
        "SecurityGroupIds": [
            "sg-063f9f90be58558b4"
        ],
        "VpcId": "vpc-e5ba568c",
        "SubnetIds": [
            "subnet-7fdb2616",
            "subnet-cf1119b7",
            "subnet-08391042"
        ]
    },
    "TracingConfig": {
        "Mode": "PassThrough"
    },
    "MemorySize": 256,
    "CodeSha256": "z1alnizk/76g9aZ0+uxGJfMedrQMg1PJEyvX9LZz0Ng=",
    "Role": "arn:aws:iam::017363970217:role/fixstorageendpoint-lambda-role",
    "FunctionArn": "arn:aws:lambda:eu-west-2:017363970217:function:FixIngestion",
    "RevisionId": "7c5b654a-2b40-4c78-b1ed-be6b7ee59f2f",
    "Runtime": "java8",
    "Description": "Fix Ingestion storing data into S3 and triggering endpoints",
    "Timeout": 75,
    "Handler": "com.sap.sailing.ingestion.FixIngestionLambda",
    "FunctionName": "FixIngestion",
    "LastModified": "2021-01-01T01:11:08.096+0000"
}
```

## Lambda Endpoints

### Endpoint Registration and Deregistration

Endpoints can register themselves as consumers for fixes. They need to provide the URL and UUIDs of the devices.

##### URL

https://endpoint-registration-eu-west2.sapsailing.com

##### Method

POST

##### Format (Body)

```
{
  "endpointUuid": "sailing-analytics-server-endpoint",
  "action": "register",
  "endpointCallbackUrl": "https://test.test.de/sailingserver/api/v1/gps_fixes",
  "devicesUuid": [
    "2605a6ea-ae9c-4f95-866c-396aebe7c369"
  ]
}
```

##### Example Code (cURL)

```
curl --location --request POST 'https://endpoint-registration-eu-west2.sapsailing.com' \
--header 'Content-Type: application/json' \
--data-raw '{
  "endpointUuid": "sailing-analytics-server-endpoint",
  "action": "register",
  "endpointCallbackUrl": "http://ec2-18-130-80-242.eu-west-2.compute.amazonaws.com:8888/sailingserver/api/v1/gps_fixes",
  "devicesUuid": [
    "2605a6ea-ae9c-4f95-866c-396aebe7c369"
  ]
}'
```


### Fix Ingestion

Devices can send one or more fixes to be stored in S3 and forwarded to registered endpoints.

##### URL

https://fix-ingestion-eu-west2.sapsailing.com

##### Method

POST 

##### Format (Body)

```
{
    "deviceUuid": "2605a6ea-ae9c-4f95-866c-396aebe7c369",
    "fixes": [
        {
            "timestamp": 14144168490000,
            "latitude": 55.12456,
            "longitude": 8.03456,
            "speed": 5.1,
            "course": 14.2
        }
    ]
}
```

##### Example Code (cURL)

```
curl --location --request POST 'https://fix-ingestion-eu-west2.sapsailing.com' \
--header 'Content-Type: application/json' \
--data-raw '{
    "deviceUuid": "2605a6ea-ae9c-4f95-866c-396aebe7c369",
    "fixes": [
        {
            "timestamp": 14144168490000,
            "latitude": 55.12456,
            "longitude": 8.03456,
            "speed": 5.1,
            "course": 14.2
        }
    ]
}'
```
### FixCombination

The FixCombination Lambda can be triggered by the defined CloudWatch Event `FixCombinationLambda_Cron` and can be activated in the Amazon EventBridge section of AWS. It collects single fixes 
created by the `FixIngestion` Lambda to collections for further data operations.

## MFA based AWS session token

In order to work with MFA the following command might come in handy. Requires jq to be installed and sapsailing set up as a profile. Replace MFA_CURRENT_TOKEN with the token from your MFA device, e.g. 230499.

```
$ cat ~ /.aws/config
[default]
output = json
region = eu-west-1
[profile sapsailing]
aws_access_key = ASIAQICXZFCUSOF5JLC5

$ aws sts get-session-token --serial-number arn:aws:iam::017363970217:mfa/simonpamies --token-code MFA_CURRENT_TOKEN > aws.json \
	&& aws configure --profile sapsailing set aws_access_key_id `jq -r .Credentials.AccessKeyId aws.json` \
	&& aws configure --profile sapsailing set aws_secret_access_key `jq -r .Credentials.SecretAccessKey aws.json` \
	&& aws configure --profile sapsailing set aws_session_token `jq -r .Credentials.SessionToken aws.json`
```
