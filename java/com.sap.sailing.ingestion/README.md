## General

This package contains lambda functions related to fix ingestion

## Endpoints

### Endpoint Registration

URL

Format

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


### Fix Ingestion

URL

https://fix-ingestion-eu-west2.sapsailing.com pointing to FixIngestionLambda

Format

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


## Development

In order to work with MFA the following command might come in handy. Requires jq to be installed and sapsailing set up as a profile.

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