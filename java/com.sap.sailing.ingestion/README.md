## General

This package contains lambda functions related to fix ingestion

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