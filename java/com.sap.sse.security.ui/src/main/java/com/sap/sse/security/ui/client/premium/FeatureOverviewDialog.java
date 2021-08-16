package com.sap.sse.security.ui.client.premium;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.subscription.BaseUserSubscriptionView;
import com.sap.sse.security.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class FeatureOverviewDialog extends DataEntryDialog<SubscriptionPlanDTO>{
    
    private FeatureOverviewTableWrapper table;
    private final StringMessages stringMessages;
    private final PaywallResolver paywallResolver;
    
    public FeatureOverviewDialog(PaywallResolver paywallResolver, StringMessages stringMessages, BaseUserSubscriptionView callingView) {
        super(stringMessages.subscriptionPlanOverview(), stringMessages.selectSubscriptionPlan(),
                stringMessages.subscribe(), stringMessages.cancel(), null, new DialogCallback<SubscriptionPlanDTO>() {
                    @Override
                    public void ok(SubscriptionPlanDTO editedObject) {
                        if (editedObject == null) {
                            Notification.notify(stringMessages.selectOption(), NotificationType.ERROR);
                        } else {
                            SubscriptionClientProvider subscriptionClientProvider = paywallResolver.getSubscriptionClientProvider();
                            if(subscriptionClientProvider != null) {
                                subscriptionClientProvider.getSubscriptionViewPresenter().startCheckout(editedObject.getId(), callingView);
                            }else {
                                Notification.notify(stringMessages.currentlyUnableToSubscribe(), NotificationType.ERROR);
                            }
                        }
                    }
                    @Override
                    public void cancel() {
                        //cancel operation does not require action.
                    }
                });
        this.stringMessages = stringMessages;
        this.paywallResolver = paywallResolver;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel mainPanel = new VerticalPanel();
        //FIXME: TableWrapper should fit in with the overall RaceBoard Error Reporting and style.
        this.table = new FeatureOverviewTableWrapper(stringMessages, null);
        final SubscriptionWriteServiceAsync<?, ?, ?> subscriptionService = paywallResolver.getSubscriptionWriteService();
        if(subscriptionService == null) {
            Notification.notify(stringMessages.currentlyUnableToSubscribe(), NotificationType.ERROR);
        }else {
            subscriptionService.getAllSubscriptionPlans(new AsyncCallback<Iterable<SubscriptionPlanDTO>>() {
                @Override
                public void onSuccess(Iterable<SubscriptionPlanDTO> result) {
                    result.forEach((dto) -> table.add(dto));
                }
                @Override
                public void onFailure(Throwable caught) {
                    Notification.notify(stringMessages.currentlyUnableToSubscribe(), NotificationType.ERROR);
                }
            });
        }
        mainPanel.add(table);
        table.getSelectionModel().addSelectionChangeHandler((changeEvent) -> {
            getOkButton().setEnabled(table.getSelectionModel().getSelectedObject() != null);
        });
        return mainPanel;
    }

    @Override
    protected SubscriptionPlanDTO getResult() {
        return table.getSelectionModel().getSelectedObject();
    }
    
}
