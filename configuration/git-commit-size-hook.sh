#!/bin/bash

# Script to warn whenever the size of a commit exceeds
# a certain limit

# File size limit is meant to be configured through 'hooks.filesizelimit' setting
filesizelimit=$(git config hooks.filesizelimit)

# If we haven't configured a file size limit, use default value of about 100M
if [ -z "$filesizelimit" ]; then
        filesizelimit=10000000 # 10MB
fi

z40=0000000000000000000000000000000000000000
while read local_ref local_sha remote_ref remote_sha
do
        if [ "$local_sha" = $z40 ]
        then
                # Delete
                echo ""
        else
                if [ "$remote_sha" = $z40 ]
                then
                        # New branch, examine all commits
                        range="$local_sha"
                else
                        # Update to existing branch, examine new commits
                        range="$remote_sha..$local_sha"
                fi

                biggest_checkin_normalized=$(git diff-tree -r $range | sort -k 4 -n -r | head -1 )
                file_blob_id=`echo $biggest_checkin_normalized | cut -d ' ' -f4,4`
                filesize=`git cat-file -s $file_blob_id`

                if [ $filesize -gt $filesizelimit ]; then
                        filename=`echo $biggest_checkin_normalized | cut -d ' ' -f6,6`
                        echo "You are attempting to push a commit containing a file ($filename) that is larger than the allowed $filesizelimit bytes ($filesize bytes)!"
                        echo "Please inform at least Axel Uhl and Simon Pamies about that!"
                        echo "(This message comes from a script located in .git/hooks/pre-push)"
                        exec < /dev/tty
                        read -p "I will comply (yes/[no]): " input
                        if [ "$input" = "yes" ]; then
                            exit 0
                        else
                            echo "Aborting push..."
                        fi
                fi
        fi
done
