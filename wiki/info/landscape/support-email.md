# Email Accounts and Email Forwarding
## 1. General Information
We currently use two different methods to handle incoming emails. One is a full-fletched email account, including a mailbox and 50GB storage quota. Those come at a price of currently 4$ per user per month [Amazon WorkMail Pricing](https://aws.amazon.com/workmail/pricing/). It also includes a Webmailer.

The second is a simple email forwarder, using a [Amazon Simple Email Service (SES) Set-Up](wiki/info/landscape/mail-relaying) rule to handle incoming emails and forward them to a specified list of recipients.

### When to use what
If you need a working email, including inbox, IMAP access etc., account then proceed with a Workmail account. If you just want to forward emails for a given user (e.g. no-reply) then use the lambda method.

## 2. WorkMail
### Configuration
We created a WorkMail organization (sapsailing). Also the following DNS entries for sapsailing.com have been modified:  
Created:
>``CNAME autodiscover.sapsailing.com. autodiscover.mail.eu-west-1.awsapps.com``

>``TXT  _dmarc.sapsailing.com.  "v=DMARC1;p=quarantine;pct=100;fo=1"``

Modified:  
   before: 
   >``TXT sapsailing.com "v=spf1 mx ip4:54.229.94.254 include:mail.zendesk.com include:servers.mcsv.net -all"``

   after:  
   >``TXT sapsailing.com "v=spf1 mx ip4:54.229.94.254 include:amazonses.com include:mail.zendesk.com include:servers.mcsv.net -all"``

There is a user created in WorkMail, ``support@sapsailing.com``. For this user we created a redirect for all incoming emails, using the [webmail access](https://sapsailing.awsapps.com/mail) and following the instructions provided here [Working with email rules](https://docs.aws.amazon.com/workmail/latest/userguide/email-rules.html).

### Create new user or change existing
Simply follow the instructions here: [Managing user accounts](https://docs.aws.amazon.com/workmail/latest/adminguide/manage-users.html).

## 3. Lambda Forwarder
### current set-up description
We have established a couple of email addresses to use the ``email-forwarder-python`` Lambda to handle incoming emails. Therefore there is a SES rule that stores incoming emails for those addresses to a S3 bucket called ``emails-for-sapsailing-com`` and notifies the SNS topic ``email-forwarder-for-sapsailing-com``. 

From there a Lambda function picks them up, modifies the original headers and sends out the Email to a JSON array of recipients, configurable in the ``MailRecipientsJSON`` environment variable of the ``email-forwarder-python`` Lambda.

The Lambda preserves the original ``From`` and ``To`` headers by adding the value to new headers called ``X-From`` and ``X-To``. In addition it sets the ``Reply-To`` header field to the initial ``From`` value if blank. The new ``From`` value is configurable in the ``MailSender`` environment variable.

The role ``lambda-email-forwarder-role`` is created to execute the Lambda. The policy ``lambda-email-forward-policy`` is attached to that role. See also [https://aws.amazon.com/blogs/messaging-and-targeting/forward-incoming-email-to-an-external-destination/](https://aws.amazon.com/blogs/messaging-and-targeting/forward-incoming-email-to-an-external-destination/) for further information about the whole setup.

In addition there is an alarm ``Email-Forwarding-Failed-Alarm`` configured, which posts to ``email-forwarder-for-sapsailing-com-alarms`` topic. 

The relevant files have been committed to our git as ``configuration/mail_forward.py`` for the Lambda's code, and as ``configuration/mail_forward_policy.json`` for the security policy for the IAM role used for the Lambda.

### Change the list of recipients
Go to [https://eu-west-1.console.aws.amazon.com/lambda/home?region=eu-west-1#/functions/email-forwarder-python?tab=configuration](https://eu-west-1.console.aws.amazon.com/lambda/home?region=eu-west-1#/functions/email-forwarder-python?tab=configuration) and edit the ``MailRecipientsJSON`` environment variable accordingly. So if you want to change the recipient list for support@sapsailing.com change the JSON array to the desired list.
``{"support@sapsailing.com": ["oldrecipient1@abc.de", "newrecipient2@abc.de"]}``

### Add a new forwarded address
The method described above is extensible. You can configure new email addresses to be forwarded by the forwarder. To do so you need to create a new rule in the default rule set for Email Receiving in the SES console [https://eu-west-1.console.aws.amazon.com/ses/home?region=eu-west-1#rule-set:default-rule-set](https://eu-west-1.console.aws.amazon.com/ses/home?region=eu-west-1#rule-set:default-rule-set). You need to specify the email address that you want to forward, store it in the S3 bucket mentioned above and use the address as object prefix. After that you need to add the desired recipients for that address to the MailRecipientsJSON in the lambda configuration. You may also add a key,value pair in the MailSenderJSON to specify a name from which you want to send the email, however this is not mandatory as the forwarder will default to the forwarded address. 

### Limitations
The email forwarder forwards 'To', 'Cc' and 'Bcc' headers, however not every use case for normal emails is handled. Keep in mind that this is not a email account, but rather a simple forwarder to be used for service accounts. 
A known limitation is that the forwarding is not working correct if the original email handled by the fowarder contains two different email addresses handled by the forwarder in the recipients. 