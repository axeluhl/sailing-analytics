# Supporting HTTPS / SSL

On sapsailing.com, we have a security certificate installed. For root users its key is visible at `/etc/pki/tls/private/star_sapsailing_com.key`. The certicicate is at `/etc/pki/tls/certs/star_sapsailing_com.crt`. The intermediate CA file from [https://knowledge.symantec.com/support/ssl-certificates-support/index?page=content&actp=CROSSLINK&id=INFO2045](https://knowledge.symantec.com/support/ssl-certificates-support/index?page=content&actp=CROSSLINK&id=INFO2045) is at `/etc/pki/tls/certs/server-chain.crt`. To enable SSL on a server, the module `mod_ssl` needs to be installed. Use ``yum install mod_ssl``.

The `/etc/httpd/conf/httpd.conf` file must contain the following in order to ensure that the `ServerName` setting is respected for selecting the appropriate `VirtualHost` element:

<pre>
    NameVirtualHost *:80
    NameVirtualHost *:443
</pre>

When this is done, the server can use the following sequence of directives inside a &lt;VirtualHost&gt; element:

<pre>
        SSLEngine  On
        SSLCertificateFile /etc/pki/tls/certs/star_sapsailing_com.crt
        SSLCertificateKeyFile /etc/pki/tls/private/star_sapsailing_com.key
        SSLCertificateChainFile /etc/pki/tls/certs/server-chain.crt
</pre>

We have this also in a macro called SSL that is defined in `/etc/httpd/conf.d/000-macros.conf` like this:

```
<Macro SSL>
        SSLEngine  On
        SSLCertificateFile /etc/pki/tls/certs/star_sapsailing_com.crt
        SSLCertificateKeyFile /etc/pki/tls/private/star_sapsailing_com.key
        SSLCertificateChainFile /etc/pki/tls/certs/server-chain.crt
</Macro>
```

It can then simply be used in any `VirtualHost` definition using `Use SSL`.

A full-blown &lt;VirtualHost&gt; element in a rewrite macro in `000-macros.conf` then could look like this:

```
<Macro Event-SSL $HOSTNAME $EVENTUUID $IP $PORT>
    <VirtualHost *:443>
        ServerName $HOSTNAME
        Use SSL
        Use Headers
        RewriteEngine on
        RewriteCond %{REQUEST_URI} "^(/)?$"
        RewriteRule ^(/)?$ "https://$HOSTNAME/gwt/Home.html?%{QUERY_STRING}#/event/:eventId=$EVENTUUID" [L,NE]
        Use Rewrite $IP $PORT
    </VirtualHost>
</Macro>
```

It can then be used inside the `001-events.conf` file like this

```
Use Event-SSL ssltest.sapsailing.com "f8087b3c-c641-4fda-bf8d-0bc2abe09e40" 172.31.22.239 8888
```

Keep in mind that the certificate we have only is valid for `*.sapsailing.com` which does not include 2nd-level sub-domains such as a.b.sapsailing.com.

To have a non-SSL `VirtualHost` redirect to the SSL counterpart, use a definition like this:

```
<VirtualHost *:80>
        ServerName jobs.sapsailing.com
        RedirectPermanent / https://jobs.sapsailing.com/
</VirtualHost>
```