#!/bin/bash
for IP in $(aws ec2 --region eu-west-2 describe-instances --filters Name=tag-key,Values="ReverseProxy" | jq -r '.Reservations[].Instances[].PublicIpAddress'); do 
  ssh ec2-user@${IP} 'cd ~ && ./updateHttpd.sh'; 
done;




#scope of proxy ProxyPass
