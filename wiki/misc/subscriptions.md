# Subscriptions

## Subscription Overview Page

The subscription overview page includes a set of subscription cards, like the free plan but also the plans subscribable by Chargebee.

Additionally there is a detailed list of all features which are included in a paid subscription.

### Add a subscription plan

1. Create a plan in Chargebee. We need afterwards the plan ID and price ID
2. Add a plan definition in class 
    
		com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan
	
	A plan definition includes the plan and price IDs and a list of connected roles.

3. Add following texts to class (and related property files)

		com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants
	
	- `<PLAN-ID>_name`
		- Name or title of the plan (`<H1>`)
	- `<PLAN-ID>_description`
		- A short description of the plan content (`<H4>`)
	- `<PLAN-ID>_info`
		- Can be uses for additional info (`<H5>`)
	- `<PLAN-ID>_features`
		- Comma separated list of feature labels


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
  		- An optional link to an external resource like picture, video or homepage

2. Register feateure with the &lt;FEATURE-ID&gt; in class

		com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardContainer
	
	as pure textual feature or feature with link. 

	    public SubscriptionCardContainer() {
	        initWidget(uiBinder.createAndBindUi(this));
	        addFeature("features_live_analytics");
	        addFeatureWithLink("features_organize_events");

### Further Resources

	com.sap.sse.security.ui.client.i18n.StringMessages