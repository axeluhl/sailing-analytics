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
- Two lambdas (arn:aws:lambda:eu-west-2:017363970217:function:FixIngestion and arn:aws:lambda:eu-west-2:017363970217:function:EndpointRegistration)
- One S3 bucket to hold the lambda binaries (arn:aws:s3:::sapsailing-lambda-functions-bucket-eu-west-2)
- One S3 bucket that holds single fixes (arn:aws:s3:::sapsailing-gps-fixes)
- One database running as a ElastiCache instance on Redis (arn:aws:elasticache:eu-west-2:017363970217:replicationgroup:fixingestionrediscache)

## Development

Development takes place in Eclipse running on Java 8. Make sure to install the official plugin from https://aws.amazon.com/eclipse/. Lambdas are classes that need to implement a specific interface and a method that gets called upon execution. See https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html for documentation. For our lambdas we have opted to use the RequestStreamHandler that just provides us with the binary stream. This makes it easier to accept different types of inputs (e.g. a GPS fix and a Bravo fix).

## Deployment

Before you can deploy you need to make sure that you have created an access key and stored the key to your machine. If you use a MFA token then make sure to read the last section of this document.

Deployment can easily be done by right clicking on the lambda class and selecting Amazon Web Services -> Upload function to AWS Lambda. Make sure to select the correct mapping of the class to the lambda endpoint. Also you need to select the IAM role arn:aws:iam::017363970217:role/fixstorageendpoint-lambda-role for ALL endpoints you are deploying.

Alternatively, build the project with Maven, using the following command:
```
mvn -Dmaven.test.skip=true package
```
This will produce a JAR file in the project's ``bin`` folder. Deploy it using the following command. Use the function name for ``${function-name}``, such as ``FixIngestion`` or ``EndpointRegistration``, respectively:

```
aws lambda update-function-code --function-name ${function-name} --zip-file fileb://java/com.sap.sailing.ingestion/bin/com.sap.sailing.ingestion-1.0.0-SNAPSHOT.jar
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
  "endpointCallbackUrl": "https://test.test.de/v1/api/gps_fixes",
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
  "endpointCallbackUrl": "http://ec2-18-130-80-242.eu-west-2.compute.amazonaws.com",
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