package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.formatDateAndTime;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DefaultCellTableBuilder;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileDesktopResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.client.celltable.SortedCellTable;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

/**
 * Implementation view for {@link UserSubscriptionsView}
 */
public class UserSubscriptions extends Composite implements UserSubscriptionsView {

    interface MyUiBinder extends UiBinder<Widget, UserSubscriptions> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    StringMessages i18n;
    @UiField
    UserProfileSubscriptionsResources local_res;

    @UiField
    Button subscribeButtonUi;
    @UiField(provided = true)
    SortedCellTable<SubscriptionDTO> subscriptionsUi = new SortedCellTable<>(0, DesignedCellTableResources.INSTANCE);

    private final Presenter presenter;
    private final SubscriptionStringConstants stringConstants = SubscriptionStringConstants.INSTANCE;

    public UserSubscriptions(final UserSubscriptionsView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        initSubscriptionsTable(presenter);
        presenter.setView(this);
        this.presenter = presenter;
    }

    @UiHandler("subscribeButtonUi")
    public void onSubscribeClicked(final ClickEvent event) {
        presenter.navigateToSubscribe();
    }

    @Override
    public void updateView(final SubscriptionListDTO subscriptions) {
        subscribeButtonUi.setEnabled(true);
        if (subscriptions == null) {
            subscriptionsUi.setPageSize(0);
            subscriptionsUi.setList(new ArrayList<SubscriptionDTO>());
        } else {
            subscriptionsUi.setPageSize(subscriptions.getSubscriptionItems().length);
            subscriptionsUi.setList(Arrays.asList(subscriptions.getSubscriptionItems()));
        }
        setVisible(true);
    }

    private void initSubscriptionsTable(final Presenter presenter) {

        subscriptionsUi.setTableBuilder(new DefaultCellTableBuilder<SubscriptionDTO>(subscriptionsUi) {
            @Override
            public void buildRowImpl(final SubscriptionDTO rowValue, final int absRowIndex) {
                super.buildRowImpl(rowValue, absRowIndex);

                if (rowValue.isInTrial()) {
                    final TableRowBuilder tr = startRow();
                    tr.className(local_res.css().borderTableRow() + " " + local_res.css().textColorRed());
                    final TableCellBuilder td = tr.startTD();
                    td.colSpan(cellTable.getColumnCount());
                    td.className(cellTable.getResources().style().cell());
                    final DivBuilder div = td.startDiv();
                    div.text(i18n.trialText(getTrialRemainingText(rowValue),
                            formatDateAndTime(rowValue.getCurrentEnd().asDate())));
                    div.endDiv();
                    td.endTD();
                    tr.endTR();
                }
            }
        });
        subscriptionsUi.setRowStyles(new RowStyles<SubscriptionDTO>() {
            @Override
            public String getStyleNames(final SubscriptionDTO row, final int rowIndex) {
                return row.isInTrial() ? local_res.css().defaultTableRow() : local_res.css().borderTableRow();
            }
        });

        subscriptionsUi.addColumn(new TextColumn<SubscriptionDTO>() {
            @Override
            public String getValue(final SubscriptionDTO object) {
                final SubscriptionPlanDTO plan = getSubscriptionPlan(object);
                return plan == null ? "-" : stringConstants.getString(plan.getSubscriptionPlanNameMessageKey());
            }
        }, i18n.name());

        subscriptionsUi.addColumn(new TextColumn<SubscriptionDTO>() {
            @Override
            public String getValue(final SubscriptionDTO object) {
                if (object.isInTrial()) {
                    return i18n.inTrial();
                } else if (object.isActive()) {
                    return i18n.active();
                } else if (object.isPaused()) {
                    return i18n.paused();
                }
                return "-";
            }
        }, i18n.status());

        subscriptionsUi.addColumn(new TextColumn<SubscriptionDTO>() {

            @Override
            public String getCellStyleNames(final Context context, final SubscriptionDTO object) {
                if (object.isActive()) {
                    return object.isPaymentSuccess() && !object.isRefunded()
                            ? local_res.css().textColorBlue()
                            : local_res.css().textColorRed();
                }
                return super.getCellStyleNames(context, object);
            }

            @Override
            public String getValue(final SubscriptionDTO object) {
                if (object.isActive() && object.getPaymentStatus() != null) {
                    if (object.isPaymentSuccess()) {
                        if (object.isRefunded()) {
                            return i18n.refunded();
                        } else {
                            return i18n.paymentStatusSuccess();
                        }
                    } else {
                        return i18n.paymentStatusNoSuccess();
                    }
                }
                return "-";
            }
        }, i18n.paymentStatus());

        final Column<SubscriptionDTO, String> cancelColumn = new Column<SubscriptionDTO, String>(new ButtonCell() {
            @Override
            public void render(final Context context, final SafeHtml data, final SafeHtmlBuilder sb) {
                sb.append(SailorProfileDesktopResources.TEMPLATE.removeButtonCell(data));
            }
        }) {
            @Override
            public String getValue(final SubscriptionDTO object) {
                return i18n.cancelSubscription();
            }
        };
        cancelColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        cancelColumn.setFieldUpdater(new FieldUpdater<SubscriptionDTO, String>() {
            @Override
            public void update(final int index, final SubscriptionDTO object, final String value) {
                // FIXME: Implement "Cancel subscription" operation abstracted for all Providers
                presenter.cancelSubscription(object.getSubscriptionPlanId(), "chargebee");
                Window.alert("[TODO] Cancelling ...");
            }
        });
        subscriptionsUi.addColumn(cancelColumn);
    }

    private SubscriptionPlanDTO getSubscriptionPlan(final SubscriptionDTO subscription) {
        try {
            return subscription != null ? presenter.getPlanById(subscription.getSubscriptionPlanId()).get() : null;
        } catch (Exception e) {
            // TODO handle error
            return null;
        }
    }

    private String getTrialRemainingText(final SubscriptionDTO subscription) {
        long remainingSecs = Math.round(TimePoint.now().until(subscription.getCurrentEnd()).asSeconds());
        final StringBuilder remainText = new StringBuilder();
        if (remainingSecs <= 0) {
            remainText.append(i18n.numHours(0));
        } else {
            final int days = (int) (remainingSecs / 86400);
            if (days > 0) {
                remainText.append(i18n.numDays(days));
            }
            remainingSecs = remainingSecs % 86400;
            final int hours = (int) (remainingSecs / 3600);
            if (hours > 0) {
                if (remainText.length() > 0) {
                    remainText.append(" ");
                }
                remainText.append(i18n.numHours(hours));
            }
            if (days == 0) {
                remainingSecs = remainingSecs % 3600;
                final int mins = (int) (remainingSecs / 60);
                if (mins > 0) {
                    if (remainText.length() > 0) {
                        remainText.append(" ");
                    }
                    remainText.append(i18n.numMinutes(mins));
                }
            }
        }

        if (remainText.length() == 0) {
            remainText.append(i18n.numHours(0));
        }

        return remainText.toString();
    }

}
