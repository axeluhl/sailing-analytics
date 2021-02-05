# Chargebee Configuration

## Site and API Key
We need to provide these system properties(VM arguments):

- chargebee.site: Site name

For test, it should be "syrf-test". And value should be "syrf" for live site.


- chargebee.apikey: API Key

Go into API Key setting page, from Dashboard -> Settings -> Configure Chargebee -> API Keys (API keys tab)
If there's no keys yet then Press on Add new key to create a new one.
Copy value of a key

## Webhook
Configure webhook to enable Chargebee webhook events listening in application

Go to Dashboard -> Settings -> Configure Chargebee -> API Keys (Webhooks tab)
Press on Add webhook button and fill popup form. Webhook URL is {host}/sailingserver/subscription/hooks/chargebee.
Turn on "Protect webhook URL with basic authentication" option, fill username, password. We also need to create a user in application with same username and password so basic authentication would work correctly.


If all configurations are provided then Chargebee service will be active in application, otherwise it will be inactive.
