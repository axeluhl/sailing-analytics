# Use like this:
#     . awsmfalogon.sh {mfaDeviceArn} {tokenCode}
# It will set the necessary environment variables that will allow the "aws" client to work
# with a session token
# Use with a bash alias definition like this, replacing {ARN-of-your-MFA-device} with the ARN of your MFA device:
#     alias awsmfa='echo -n "Token: "; read aws_mfa_token; . awsmfalogon.sh "{ARN-of-your-MFA-device}" ${aws_mfa_token}'
# Then, you can invoke the alias "awsmfa" on your bash command line, and you will get prompted for an MFA token
# which, when entered, will add the necessary environment variables to your bash session that will allow your aws
# client to function with a valid session key.
mfaDeviceArn=$1
tokenCode=$2
jsonOutput="$(aws sts get-session-token --serial-number "${mfaDeviceArn}" --token-code ${tokenCode})"
export AWS_ACCESS_KEY_ID=$( echo "${jsonOutput}" | jq --raw-output '.Credentials.AccessKeyId' )
export AWS_SECRET_ACCESS_KEY=$( echo "${jsonOutput}" | jq --raw-output '.Credentials.SecretAccessKey' )
export AWS_SESSION_TOKEN=$( echo "${jsonOutput}" | jq --raw-output '.Credentials.SessionToken' )
