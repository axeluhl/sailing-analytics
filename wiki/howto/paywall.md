# Paywall
The paywall represents all features that are locked to users with buyable permissions. 
The following article describes how the paywall is implemented and what specifics to look out for when interacting with or implementing relating software parts.

## SubscriptionService
The SubscriptionService is the client side representation of all Functions that are exposed by the server to interact with user subscriptions and subscription plans. 
It is split in a readOnly and a write variant, while the write variant only communicates with the master instance of the targetet server cluster. 
The functions provided range from handling initiation and conclusion of a chargebee hosted page checkout process, over reading all available subscription plans, to issueing a cancel request for a subscription.	
The paywall mainly interacts with the SubscriptionService through the PaywallResolver.

## PaywallResolver
The PaywallResolver is used as the main utility tool to interact with all services that are related to the paywall. It has been introduced mainly to slim down the number of parameters handed down through constructors in the application. 
As an example, it is injected into all Settings of type AbstractSecuredValueCollectionSettings and AbstractSecuredValueSettings to forward the actual permission evaluation.

it does provide the following features: 
On SubscriptionService side:
	- Gives access to functions directly related to subscriptions.
	- Gives a list of all SubscriptionPlans that would unlock a specified feature.
On UserService side:
	- Evaluates user permissions
	- Registers user status event handlers, reacting when a users attributes change (e.g. when a user logs in, he might have another permission set)

## SecuredSettings
A subset of Settings, which have been extended by a corresponding action. These actions are tied to permissions, which must be owned by a user if they wnat to execute the action. 


The current implementation implies, that if a user wants to change a setting from its default value, he/she need the required permissions to do so. Otherwise the application will always fall back to the default setting when reading or setting a value.
As an example see the code below. To infer wether the user possesses the required permissions, three components are required:
1. PaywallResolver. As stated before it is used as an interface / utility class for all things subscritpion related. In this case it forwards the hasPermission call to the userService.
2. Action. The Action is set inside of the Setting itself on creation (in the Constructor of all AbstractSecuredValueSettings / AbstractSecuredValueCollectionSettings)
3. DTOContext. It represents a SecuredDTO object, that gives information on which object the action is to be performed on. Hence providing information about ownership.
A permission check is not possible if not all three components are present and therefore the default value would be used in such a case.

```
@Override
    public final T getValue() {
        if(dtoContext != null && dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            return super.getValue();
        }else {
            return super.getDefaultValue();
        }
    }
```

## Premium UI Elements
Anything extending com.sap.sse.security.ui.client.premium.uielements.PremiumUiElement<T> can be used to establish additional paywall features. 
These UI elements require a setting to be fed to them at constructor call. These settings then represent the permissions that a user must have to access the feature. 
A Premium UI Element usually has two states: Locked and Unlocked. In a locked state the Element sends the user to the Subscription page when interacted with. 
It also suggests any subscription plan to the user that would unlock the respective feature when acquired.
When unlocked premium elements behave the same as their non premium counterparts.