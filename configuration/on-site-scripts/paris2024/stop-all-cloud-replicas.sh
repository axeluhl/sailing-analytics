#!/bin/bash
if [ $# -eq 0 ]; then
    echo "$0 -b <replication-bearer-token>"
    echo ""
    echo "-b replication bearer token; mandatory"
    echo
    echo "Example: $0 -b 7345983275087320/59870hfly945="
    echo
    echo "Will tell all replicas in the cloud to stop replicating. This works by invoking the"
    echo "get-replica-ips script and for each of them to stop replicating, using the stopReplicating.sh"
    echo "script in their /home/sailing/servers/paris2024 directory, passing through the bearer token."
    echo "Note: this will NOT stop replication on the local replica on sap-p1-2!"
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
  ssh -o StrictHostKeyChecking=no root@$i "su - sailing -c \"cd /home/sailing/servers/paris2024; ./stopReplicating.sh ${BEARER_TOKEN}\""
done
