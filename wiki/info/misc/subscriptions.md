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

### Support Link

The "service and information request" function is opening the default mail program (mailto:) to 

	support@sapsailing.com
	
This is hard coded as String constant in `SubscriptionCardContainer.java`.

The subject text can be changed in `SubscriptionStringConstants.properties` with key 

 - `support_subject`

### Further Resources

	com.sap.sse.security.ui.client.i18n.StringMessages