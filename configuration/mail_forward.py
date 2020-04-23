# Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# This file is licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License. A copy of the
# License is located at
#
# http://aws.amazon.com/apache2.0/
#
# This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
# OF ANY KIND, either express or implied. See the License for the specific
# language governing permissions and limitations under the License.

import os
import boto3
import email
import re
import json
from botocore.exceptions import ClientError
from email.parser import Parser
from email.policy import default

region = os.environ['Region']

def get_message_from_s3(bucket_name, object_key):
    # Create a new S3 client.
    client_s3 = boto3.client("s3")
    # Get the email object from the S3 bucket.
    object_s3 = client_s3.get_object(Bucket=bucket_name, Key=object_key)
    # Read the content of the message.
    file = object_s3['Body'].read()
    return file

def create_message(file):
    senderMap = json.loads(os.environ["MailSenderJSON"])
    recipientMap = json.loads(os.environ["MailRecipientsJSON"])
    mailobject = email.message_from_string(file.decode('utf-8'))
    # Define sender, either from 'To'-Header or from 'Cc'-Header
    sender = None
    # Check 'To'-Header
    toHeader = Parser(policy=default).parsestr('To: ' + mailobject['To'])
    for x in toHeader['to'].addresses:
        if x.addr_spec in recipientMap:
            recipientList = recipientMap.get(x.addr_spec)
            sender = senderMap.get(x.addr_spec, x.addr_spec)
    # Check 'Cc'-Header
    if sender is None:
        if mailobject['Cc']:
            ccHeader = Parser(policy=default).parsestr('Cc: ' + mailobject['Cc'])
            for x in ccHeader['cc'].addresses:
                if x.addr_spec in recipientMap:
                    recipientList = recipientMap.get(x.addr_spec)
                    sender = senderMap.get(x.addr_spec, x.addr_spec)
        # Bcc mail, there is no header so find address from 'Received'-Header
        else:
            receivedString = re.search(r'\<(.*?)\>', ''.join(mailobject.get_all('Received'))).group(1)
            if receivedString in recipientMap:
                recipientList = recipientMap.get(receivedString)
                sender = senderMap.get(receivedString, receivedString)
    # Change original headers to X-headers
    mailobject['X-From'] = mailobject['From']
    if not mailobject['Reply-To']:
        mailobject['Reply-To'] = mailobject['From']
    mailobject['X-To'] = mailobject['To']
    mailobject['X-Return-Path'] = mailobject['Return-Path']
    # Replace original headers and set to SES Value
    del mailobject['DKIM-Signature']
    mailobject.replace_header('From', sender)
    mailobject.replace_header('Return-Path', sender)
    mailobject.replace_header('To', ','.join(recipientList))
    message = {
        "Source": sender,
        "Destinations": recipientList,
        "Data" : mailobject.as_string()
    }
    return message

def send_email(message):
    aws_region = os.environ['Region']
    # Create a new SES client.
    client_ses = boto3.client('ses', region)
    # Send the email.
    try:
        #Provide the contents of the email.
        response = client_ses.send_raw_email(
            Source=message['Source'],
            Destinations=
                message['Destinations']
            ,
            RawMessage={
                'Data':message['Data']
            }
        )
    # Display an error if something goes wrong.
    except ClientError as e:
        output = e.response['Error']['Message']
    else:
        # Modify output message
        if re.search(r'\<(.*?)\>', message['Source']):
            fromString = ','.join(re.findall(r'\<(.*?)\>', message['Source']))
        else: 
            fromString = message['Source']
        output = "Email for: " + fromString + " forwarded to: " + ', '.join(message['Destinations']) + "! Message ID: " + response['MessageId']
    return output

def lambda_handler(event, context):
    # Get the unique ID of the message. This corresponds to the name of the file in S3.
    sns = event['Records'][0]['Sns']
    message = json.loads(sns['Message'])
    bucket_name = message['receipt']['action']['bucketName']
    object_key = message['receipt']['action']['objectKey']
    print(f"Received message with object key {object_key} in bucket {bucket_name}")
    # Retrieve the file from the S3 bucket.
    file = get_message_from_s3(bucket_name, object_key)
    # Create the message.
    message = create_message(file)
    # Send the email and print the result.
    result = send_email(message)
    print(result)
