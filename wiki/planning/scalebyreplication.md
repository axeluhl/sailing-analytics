## Scaling by Replication

When we want to offer our live leader board and race board with the "2D Tracking" to a larger audience, we need to work on a scale-out approach. Currently, our limit to scalability is the bandwidth required to transmit the live leader board to many clients because we don't send differences only but always transmit the full leader board. We will be able to handle a certain factor (maybe 10-100) more clients as we can right now by using a differential leader board transmission scheme. However, then, the bandwidth of a single host cannot be further extended by any typical data center.

We then need to run the server on multiple hosts, making sure that the state across these nodes is at least eventually consistent. We have a prototypical replication feature that works under "lab conditions" that now needs to be matured to work in production environments.

See bug 523 (http://sapcoe-app01.pironet-ndh.com/bugzilla/show_bug.cgi?id=523). 