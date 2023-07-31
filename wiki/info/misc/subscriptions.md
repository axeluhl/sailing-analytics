# Subscriptions

## Subscription Overview Page

The subscription overview page includes a set of subscription cards, like the free plan but also the plans subscribable by Chargebee.

Additionally there is a detailed list of all features which are included in a paid/trial subscription.

### Add a subscription plan

1. Create a plan in Chargebee. We need afterwards the plan ID and price ID
2. Add a plan definition in class 
    
		com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan
	
	A plan definition includes the plan and price IDs and a list of connected roles.
	
3. Group the plan by defining a (SubscriptionPlan.)PlanGroup and set the group to init of SailingSubscriptionPlan

	A group will bundle the plans on one card. E.g. a premium weekly and premium yearly plan are in one group and
	therefore shown on one (Premium) card.

4. Add the plan to one or many categories. This helps to identify a content coverage of the plans. 

	An example: Premium plan is in category PREMIUM. A data mining plan is build on premium and is therefore attached
	to the PREMIUM and DATA_MINING category. If the user subscribe to premium we know because of the categories, that a 
    data mining plan would include the premium plan (it is covering all categories from premium). So we can detect an
	'update' which could follow up by an auto cancellation of an existing plan.

4. Add following texts to class (and related property files)

		com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants
	
	- `<GROUP-ID>_name`
		- Name or title of the plan (`<H1>`)
	- `<GROUP-ID>_description`
		- A short description of the plan content (`<H4>`)
	- `<GROUP-ID>_info`
		- Can be uses for additional info (`<H5>`)
	- `<GROUP-ID>_price_info`
		- It's a red rubber stamp over the price. It will only be rendered if price is a one time payment.
	- `<GROUP-ID>_features`
		- Pipe (|) separated list of feature labels


### Business Model Info

To edit text of business model description you can adjust the texts in resouce class (and related property files)

		com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants

- `businessModelTitle`
	- Titel of text shown in modal dialog
- `businessModelDescription`
	- The text body of the modal dialog

### Features

To add, remove or edit a feature of the feature list

1. Add following texts to class (and related property files)

		com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants

	- `<FEATURE-ID>_title`
   		- Name or title of the feature
	- `<FEATURE-ID>_description`
  		- A description of the feature
	- `<FEATURE-ID>_link`
  		- An __optional__ link to an external resource like picture, video or homepage

2. Register feateure with the &lt;FEATURE-ID&gt; in class

		com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardContainer
	
	as pure textual feature or feature with link. The boolean values activate or deactivate the checkboxes for the FREE and PREMIUM diff view.

	    public SubscriptionCardContainer() {
	        initWidget(uiBinder.createAndBindUi(this));
	        addFeature("features_live_analytics", true, true);
	        addFeatureWithLink("features_organize_events", false, true);

Short names for the diff view can be defined with following keys

 - `free_subscription_plan_shortname`
 - `premium_subscription_plan_shortname`
 
 They will be displayed in uppercase letters.

## Cancellation

### Non-renewing subscriptions

A user can subscribe to a non-renewing plan, like weekly plans. They are marked on UI with a stamp 'one time payment'.
The subscription is then directly canceled to the end of the subscription time.

### Stop auto-renewing

The opposite are the yearly plans, which are renewing Abonnements. They can be canceled in 

	> user details > subscription

This will trigger a cancel event by the chargebee API with the following settings 
(see also `ChargebeeNonRenewingSubscriptionRequest.java`)

	end_of_term = true (1)

1. The contract will be canceled after the subscription is at the end of the current subscription billing cycle
   [chargebee docu](https://apidocs.chargebee.com/docs/api/subscriptions#cancel_subscription_for_items_end_of_term)

The result is that the subscription changed the state from `active` -> `non_renewing`

### Auto cancellation

If we detect an update of an already active (`active` or `non_renewing`) plan, which means that the potential new one 
is covering all features of the old one (detected by category comparison), we will cancel the old one automatically.
This is only done after the new plan is subscribed successfully. It will cancel the old plan with following settings
(see also `ChargebeeCancelSubscriptionRequest.java`)

	end_of_term = false (1)
	credit_option_for_current_term_charges = PRORATE (2)
	account_receivables_handling = SCHEDULE_PAYMENT_COLLECTION (3)
	refundable_credits_handling = SCHEDULE_REFUND (4)

1. The contract will be canceled immediately
[chargebee docu](https://apidocs.chargebee.com/docs/api/subscriptions#cancel_subscription_for_items_end_of_term)
2. The refund will be calculated out of the remaining days 
[chargebee docu](https://apidocs.chargebee.com/docs/api/subscriptions#cancel_subscription_for_items_credit_option_for_current_term_charges)
3. All credits will be refunded on the available payment method 
[chargebee docu](https://apidocs.chargebee.com/docs/api/subscriptions#cancel_subscription_for_items_account_receivables_handling)
4. Remaining refundable credits will be refunded asynchronously 
[chargebee docu](https://apidocs.chargebee.com/docs/api/subscriptions#cancel_subscription_for_items_refundable_credits_handling)

The result is that the old subscription state is set to `cancel`

### Support Link

The "service and information request" function is opening the default mail program (mailto:) to 

	support@sapsailing.com
	
This is hard coded as String constant in `SubscriptionCardContainer.java`.

The subject text can be changed in `SubscriptionStringConstants.properties` with key 

 - `support_subject`

### Further Resources

	com.sap.sse.security.ui.client.i18n.StringMessages

### Further Documentation

[Chargebee-API Documentation](https://apidocs.chargebee.com/docs/api/subscriptions)
