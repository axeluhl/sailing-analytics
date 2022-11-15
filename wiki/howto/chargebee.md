# Chargebee Configuration

## Site and API Key
We need to provide these system properties as VM arguments:

- chargebee.site: Site name

For test, it should be "syrf-test". And value should be "syrf" for a live / production site.


- chargebee.apikey: API Key

Go into API Key setting page at [https://app.chargebee.com/](https://app.chargebee.com/), from Dashboard -> Settings -> Configure Chargebee -> API Keys (API keys tab).
If there's no keys yet then press on "Add new key" to create a new one.
Copy the value of the key.

## Webhook
Configure a webhook to enable Chargebee webhook events listening in application.

Go to Dashboard -> Settings -> Configure Chargebee -> API Keys (Webhooks tab).
Press on the "Add webhook" button and fill the popup form. Webhook URL is `{host}/sailingserver/subscription/hooks/chargebee`.
Turn on the "Protect webhook URL with basic authentication" option, fill username and password. We also need to create a user in the application with the same username and password so basic authentication would work correctly. The user for this account requires the permission `USER:ADD_SUBSCRIPTION` on any user the web hook shall be able to manage subscriptions for.

If all configurations are provided then Chargebee service will be active in the application, otherwise it will be inactive.

## Plan Configuration
See `com.sap.sse.security.shared.subscription.SubscriptionPlan` for the definitions of subscription plans available and the security roles they imply, together with their role qualifications. An example
can look like this:

```
    STARTER("starter", "Starter", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.mediaeditor.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER) }),
    PREMIUM("premium", "Premium", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.moderator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.SUBSCRIBING_USER_DEFAULT_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.NONE) });
```

Note that the role IDs (the first parameter, such as `"starter"`) is expected to match with a plan ID as specified within the Chargbee web site.

#Chargebee Subscription Manual Upgrade

1. Go to Subscriptions -> Select relevant subscription of user -> cancel subscription (on the right) -> immediately
2. Go to Product Catalog -> Coupons -> Create Coupon -> create a coupon equivalent to amount to be refunded (One time use). This may be the full amount paid for the original subscription or less depending on circumstances.
3. Send Coupon code to user