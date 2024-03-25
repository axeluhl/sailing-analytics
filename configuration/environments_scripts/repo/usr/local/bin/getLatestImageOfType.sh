#!/bin/bash
imageType="$1"
aws ec2 describe-images --filter Name=tag:image-type,Values=${imageType} | jq --raw-output '.Images | sort_by(.CreationDate) | .[].ImageId' | tail -n 1
