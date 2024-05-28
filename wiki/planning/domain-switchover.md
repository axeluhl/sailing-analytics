# Domain Switchover

## Locations that reference sapsailing.com

- The ChargeBee subscription module use for payment services has a callback method pointing to sapsailing.com where updates to the users are processed and then replicated across the landscape.
- SharedLandscapeConsants.DEFAULT_DOMAIN_NAME is set to "sapsailing.com" but can be overridden; for things to work, however, we would need to add the deviating domain as a "hosted zone" to our Route53 account so that the DNS-maintaining logic will run without exceptions.
- The certificate used in the ALBs is created for \*.sapsailing.com; to run under a different domain name, a certificate matching the domain name will be required. The ALB set-up in AwsLandscapeImpl.createLoadBalancerHttpsListener uses the default certificate domain "\*.sapsailing.com" from a constant; this would need to be made flexible, e.g., through a system property, if we were to use any form of landscape automation under a different domain name.
- Shared security configuration as in
    env.sh:#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.\*\.)?sapsailing\.com(:[0-9]\*)?$" 
- Mail sending and Amazon SES.
- REPLICATE_MASTER_SERVLET_HOST is sometimes hardcoded.
- Igtimi Authentication.
- Google Maps API is SAP provided.
- A number of bash scripts have sapsailing.com hardcoded, especially the setup scripts and imageupgrade_functions.sh. Ideally we will need to factor out into a constant that all bash scripts can access.
- Releases and p2 respository references.
- MongoDB references and rabbit references, or any other private IP references in route53.