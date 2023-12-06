#!/usr/bin/perl
# Calls another bash script to determine whether or not an instance should display itself as healthy. 
# The script then returns the right error code for the aws healthcheck to pass or fail.
my $stat=`./reverseProxyHealthCheck.sh`;
$stat =~ s/^\s+|\s+$//g; #trims leading and trailing whitespace
if ( $stat eq 'unhealthy' ) {
        printf("Status: 503 Not in the same az\n");
} else {
        printf("Status: 200 Healthy\n");
}
print "Content-type: text/html\n\n";
print $stat;