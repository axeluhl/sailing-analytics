# Domain Switchover

## Locations that reference sapsailing.com

- The ChargeBee subscription module used for payment services has a callback method pointing to ``sapsailing.com`` where updates to the users are processed and then replicated across the landscape.

- ``SharedLandscapeConsants.DEFAULT_DOMAIN_NAME`` is set to "sapsailing.com" but can be overridden; for things to work, however, we would need to add the deviating domain as a "hosted zone" to our Route53 account so that the DNS-maintaining logic will run without exceptions.

- The certificate used in the ALBs is created for \*.sapsailing.com; to run under a different domain name, a certificate matching the domain name will be required. The ALB set-up in AwsLandscapeImpl.createLoadBalancerHttpsListener uses the default certificate domain "\*.sapsailing.com" from a constant; this would need to be made flexible, e.g., through a system property, if we were to use any form of landscape automation under a different domain name.

- Shared security configuration as in
    env.sh:#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.\*\.)?sapsailing\.com(:[0-9]\*)?$"
    
- The ``target-group-tag-route53-nfs-elasticIP-setup.sh`` script used as the last stage of the setup process of a new central reverse proxy. It queries the Route 53 ID of the "sapsailing.com" hosted zone which would have to become a parameter or would need to be changed in the script.

- Mail sending and Amazon SES.

- ``REPLICATE_MASTER_SERVLET_HOST`` is sometimes hardcoded.

- Igtimi Authentication: the Igtimi REST API and authentication scheme uses a callback URL that we have configured on the igtimi.com web site, pointing to sapsailing.com.

- Google Maps API is SAP provided in our current production environment, and a different API token/key is required once SAP stops its support.

- A number of bash scripts have sapsailing.com hardcoded, especially the setup scripts and imageupgrade_functions.sh. Ideally we will need to factor out into a constant that all bash scripts can access.

- Releases and p2 respository references, where "releases.sapsailing.com" will be replaced over time by Github releases which the ``refreshInstance.sh`` script can then download.

- MongoDB references and rabbit references, or any other private IP references in route53. Such references can in particular be found in the ``environments/`` folder on ``releases.sapsailing.com`` for the default parameterization of certain application process types, such as ``security-service-master`` or ``archive-server``.

- branch.io is used for "deep linking" for the apps. We have an integration with our Route53 hosted zone "sapsailing.com" for the specific domain names, allowing smart payload transfer through from-scratch installations, e.g., from a QR code. The branch.io account would need to be transferred and would need to have the configuration changed; or a new account would be required. The apps' deep linking capabilities are expressed in their manifests, and hence the apps would need to be adjusted to work with a different domain.