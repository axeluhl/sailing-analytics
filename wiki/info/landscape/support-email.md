# Set Up of Support E Mail Address support@sapsailing.com with AWS SES/SNS/Lambda

We use AWS SES for all e-mail services, inbound and outbound. See also [Amazon Simple Email Service (SES) Set-Up](wiki/info/landscape/mail-relaying) for details on the general SES set-up. We have established ``support@sapsailing.com`` now as another inbound e-mail address that stores the incoming email messages in a S3 bucket called ``SupportMails``. After being stored, they will notify a SNS topic called ``SupportMailStoredInS3``. 

From there a Lambda function picks them up, modifies the original headers and sends out the Email to a JSON array of recipients, configurable in the ``MailRecipientsJSON`` environment variable of the ``support-email-forwarding-python`` Lambda.

The Lambda preserves the original ``From`` and ``To`` headers by adding the value to new headers called ``X-From`` and ``X-To``. In addition it sets the ``Reply-To`` header field to the initial ``From`` value if blank. The new ``From`` value is configured in the ``MailSender`` environment variable, currently set to: ``SES Email Forwarder <support@sapsailing.com>``.

The role that is created to execute the Lambda needs to have a policy set that can access SES and write logs. See also [https://aws.amazon.com/blogs/messaging-and-targeting/forward-incoming-email-to-an-external-destination/](https://aws.amazon.com/blogs/messaging-and-targeting/forward-incoming-email-to-an-external-destination/)

The relevant files have been committed to our git as ``configuration/mail_forward.py`` for the Lambda's code, and as ``configuration/mail_forward_policy.json`` for the security policy for the IAM role used for the Lambda.

## Changing the List of Recipients
Go to [https://eu-west-1.console.aws.amazon.com/lambda/home?region=eu-west-1#/functions/support-email-forwarding-python?tab=configuration](https://eu-west-1.console.aws.amazon.com/lambda/home?region=eu-west-1#/functions/support-email-forwarding-python?tab=configuration) and edit the ``MailRecipientsJSON`` environment variable accordingly.