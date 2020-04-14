# Set Up of Support E Mail Address support@sapsailing.com with AWS SES/SNS/Lambda

We use AWS SES for all e-mail services, inbound and outbound. See also [Amazon Simple Email Service (SES) Set-Up](wiki/info/landscape/mail-relaying) for details on the general SES set-up. We have established ``support@sapsailing.com`` now as another inbound e-mail address that forwards to an SNS topic named ``Support-Mail``.

The messages forwarded from SES to SNS are JSON-formatted wrappers around the original e-mail message augmented with various meta-data. Forwarding these messages with a straight SNS subscription of type EMAIL results in human-unreadable e-mails in JSON format with all the metadata included.

Instead, we subscribe a Lambda function on the ``Support-Mail`` SNS topic that parses the original message from the JSON document and re-sends it through SES to a list of recipients that can be configured in the ``to_address`` environment variable used by the Lambda. The ``to_address`` environment variable needs to be a valid JSON array that contains the recipients' e-mail addresses as string objects.

The role that is created to execute the Lambda needs to have a policy set that can access SES and write logs. See also https://github.com/sirceljm/AWS-SES-simple-mail-forward/blob/master/mail_forward_policy.json.

The relevant files have been committed to our git as ``configuration/mail_forward.js`` for the Lambda's code, and as ``configuration/mail_forward_policy.json`` for the security policy for the IAM role used for the Lambda.