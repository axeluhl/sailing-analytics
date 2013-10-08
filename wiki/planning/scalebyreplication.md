## Scaling by Replication

When we want to offer our live leader board and race board with the "2D Tracking" to a larger audience, we need to work on a scale-out approach. Currently, our limit to scalability is the bandwidth required to transmit the live leader board to many clients because we don't send differences only but always transmit the full leader board. We have been able to handle a certain factor (maybe 10-100) more clients as we did before by using a differential leader board transmission scheme. However, now, the bandwidth of a single host cannot be further extended by any typical data center.

We then need to run the server on multiple hosts, making sure that the state across these nodes is at least eventually consistent. We have a replication feature that has shown to work at least under the production conditions of the Extreme Sailing Series. We can now build on this to also use replication for a scale-out approach.

It comes in handy that we're now preparing for [moving our landscape to Amazon EC2](http://wiki.sapsailing.com/wiki/amazon-ec2) which Simon is preparing. There, we should be able to quickly launch and stop new replicas. We need to learn how to use load balancing components such as Pound or other typical components used in the EC2 environment.

See bug 523 (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=523). 