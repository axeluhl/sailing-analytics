#!/bin/bash
if [ $# -eq 0 ]; then
    echo "$0 -b <replication-bearer-token>"
    echo ""
    echo "-b replication bearer token; mandatory"
    echo
    echo "Example: $0 -b 7345983275087320/59870hfly945="
    exit 2
fi
options='b:'
while getopts $options option
do
    case $option in
        b) BEARER_TOKEN=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
for i in `./get-replica-ips`; do
  ssh -o StrictHostKeyChecking=no sailing@$i "cd /home/sailing/servers/tokyo2020; /home/sailing/code/java/target/stopReplicating.sh ${BEARER_TOKEN}"
done
