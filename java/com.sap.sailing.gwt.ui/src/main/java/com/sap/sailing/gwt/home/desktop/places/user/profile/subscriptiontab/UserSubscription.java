package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionItem;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.SubscriptionPlan;
import com.sap.sse.security.shared.SubscriptionPlanHolder;

/**
 * Implementation view for {@link UserSubscriptionView}
 * 
 * @author Tu Tran
 */
public class UserSubscription extends Composite implements UserSubscriptionView {
    interface MyUiBinder extends UiBinder<Widget, UserSubscription> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    DivElement rootUi;
    @UiField
    Button subscribeButtonUi;
    @UiField
    ListBox planListUi;
    @UiField(provided = true)
    CellList<SubscriptionItem> subscriptionListUi;

    private Presenter presenter;

    public UserSubscription(UserSubscriptionView.Presenter presenter) {
        initSubscriptionCellList(presenter);
        initWidget(uiBinder.createAndBindUi(this));

        presenter.setView(this);
        this.presenter = presenter;
    }

    @UiHandler("subscribeButtonUi")
    public void handleUpdateSubscriptionClick(ClickEvent e) {
        subscribeButtonUi.setEnabled(false);
        presenter.openCheckout(planListUi.getSelectedValue());
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        SubscriptionProfileDesktopResources.INSTANCE.css().ensureInjected();
    }

    @Override
    public void onStartLoadSubscription() {
        hide();
    }

    @Override
    public void onOpenCheckoutError(String error) {
        subscribeButtonUi.setEnabled(true);
        Notification.notify(error, NotificationType.ERROR);
    }

    @Override
    public void onCloseCheckoutModal() {
        subscribeButtonUi.setEnabled(true);
    }

    @Override
    public void updateView(SubscriptionDTO subscription) {
        updatePlanList(subscription);
        subscribeButtonUi.setEnabled(true);
        if (subscription == null) {
            subscriptionListUi.setRowData(new ArrayList<SubscriptionItem>());
        } else {
            subscriptionListUi.setRowData(Arrays.asList(subscription.getSubscriptionItems()));
        }
        subscriptionListUi.redraw();
        show();
    }

    private void initSubscriptionCellList(Presenter presenter) {
        subscriptionListUi = new CellList<SubscriptionItem>(new SubscriptionCell(presenter));
        Label emptyWidget = new Label(StringMessages.INSTANCE.noSubscriptions());
        emptyWidget.setStyleName(SubscriptionProfileDesktopResources.INSTANCE.css().emptySubscriptionsLabel());
        subscriptionListUi.setEmptyListWidget(emptyWidget);
        subscriptionListUi.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    }

    private void updatePlanList(SubscriptionDTO subscription) {
        planListUi.clear();
        if (subscription == null) {
            planListUi.addItem("", "");
        }
        SubscriptionPlan[] planList = SubscriptionPlanHolder.getInstance().getPlanList();
        for (SubscriptionPlan plan : planList) {
            planListUi.addItem(plan.getName(), plan.getId());
        }
        planListUi.setSelectedIndex(0);
    }

    private void hide() {
        rootUi.getStyle().setDisplay(Display.NONE);
    }

    private void show() {
        rootUi.getStyle().setDisplay(Display.BLOCK);
    }
}
