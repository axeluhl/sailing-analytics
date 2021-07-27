package com.sap.sse.security.ui.client.premium;


import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class FeatureOverviewDialog extends DialogBox{
    
    private FeatureOverviewTableWrapper table;
    private final StringMessages stringMessages;
    private final PayWallResolver payWallResolver;
    
    public FeatureOverviewDialog(PayWallResolver payWallResolver) {
        this.stringMessages = StringMessages.INSTANCE;
        this.payWallResolver = payWallResolver;
        //FIXME: TableWrapper should fit in with the overall RaceBoard Error Reporting and style.
        final VerticalPanel mainPanel = new VerticalPanel();
        this.table = new FeatureOverviewTableWrapper(stringMessages, null);
        final SubscriptionWriteServiceAsync<?, ?, ?> subscriptionService = payWallResolver.getSubscriptionWriteService();
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
        final Button button = new Button(stringMessages.subscribe());
        button.addClickHandler((event) ->{
            final Set<SubscriptionPlanDTO> selectedSet = table.getSelectionModel().getSelectedSet();
            if(selectedSet.size() == 1) {
                if(selectedSet.iterator().next() == null) {
                    Notification.notify(stringMessages.selectOption(), NotificationType.ERROR);
                }else {
                    Notification.notify("open checkout here", NotificationType.SUCCESS);
                }
            }
        });
        table.getSelectionModel().addSelectionChangeHandler((changeEvent) ->{
            GWT.debugger();
            SetSelectionModel<SubscriptionPlanDTO> selectionModel = table.getSelectionModel();
            Set<SubscriptionPlanDTO> selectedSet = selectionModel.getSelectedSet();
            button.setEnabled(selectedSet.size() != 0);
         });
        mainPanel.add(button);
        setWidget(mainPanel);
    }
    
}
