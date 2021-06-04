#!/bin/bash
for i in `./get-replica-ips`; do
  ssh -o StrictHostKeyChecking=no sailing@$i "cd /home/sailing/servers/tokyo2020; /home/sailing/code/java/target/stopReplicating.sh 4qUrxMVQanLghETmM95XX3fshkHK0wNAQycuPAVNW0E="
done
