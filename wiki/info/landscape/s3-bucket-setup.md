# Setting Up a Dedicated S3 Bucket

When you want to equip a server with a File Storage for easy upload of media content, S3 is one of the options supported. For our "own" servers we often use a single technical account that has permissions to write to the ``media.sapsailing.com`` bucket. We may not want to extend the write credentials to that bucket to other users or servers that are not only administered by SAP. For those scenarios, setting up a dedicated bucket with a dedicated user and specific permissions is required.

## Creating a New User

In the AWS console, go to IAM, Users, and add a new user. Ideally, call the user like you will call the bucket, for easy identification and mapping. Tick the "Programmatic access" checkbox but not the "AWS Management Console access" box. Give the user a permission document using the "Add inline policy" link and choosing the JSON tab, as follows, replacing ``<TheNameOfTheBucket>`` by the bucket name you will choose below:

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:AbortMultipartUpload",
                "s3:Get*",
                "s3:List*",
                "s3:Put*"
            ],
            "Resource": "arn:aws:s3:::<TheNameOfTheBucket>/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListAllMyBuckets"
            ],
            "Resource": "*"
        }
    ]
}
```

Record the access ID and key displayed. You will need to enter them in the SAP Sailing Analytics Admin Console Advanced/File Storage tab.

## Creating the Bucket

Go to [https://s3.console.aws.amazon.com/s3/home?region=eu-west-1](https://s3.console.aws.amazon.com/s3/home?region=eu-west-1) and press the "Create Bucket" button. Work through the creation wizard. When done, select the bucked from the bucket list and go to the "Permissions" tab. Select "Bucket Policy" and then paste and adjust the following. Here is an example document that can be pasted and adjusted by replacing the ``<TheNameOfTheUser>`` and ``<TheNameOfTheBucket>`` by the respective user and bucket names:

```
{
    "Version": "2012-10-17",
    "Id": "Policy1518892533142",
    "Statement": [
        {
            "Sid": "Stmt1518892527243",
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::017363970217:user/<TheNameOfTheUser>"
            },
            "Action": "s3:ListBucket",
            "Resource": "arn:aws:s3:::<TheNameOfTheBucket>"
        },
        {
            "Sid": "Stmt1518892527243",
            "Effect": "Allow",
            "Principal": {
                "AWS": "*"
            },
            "Action": "s3:Get*",
            "Resource": "arn:aws:s3:::<TheNameOfTheBucket>/*"
        }
    ]
}
```

## Configuring File Storage

Go to the AdminConsole, Advanced, File Storage. Select "Amazon S3", enter the access ID and key from above, as well as the bucket name you chose above and press "Save." No error should result. Then press "Set Active" and observe that "Amazon S3" is displayed as the "Active" configuration.