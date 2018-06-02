package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ScrolledTabLayoutPanel;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.presentation.ResultsChart.DrillDownCallback;
import com.sap.sailing.gwt.ui.datamining.resources.DataMiningResources;
import com.sap.sailing.gwt.ui.polarmining.PolarBackendResultsPresenter;
import com.sap.sailing.gwt.ui.polarmining.PolarResultsPresenter;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class TabbedResultsPresenter extends AbstractComponent<Settings> implements ResultsPresenter<Settings> {

    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);

    private final StringMessages stringMessages;

    private final ScrolledTabLayoutPanel tabPanel;
    private final Map<Widget, ResultsPresenter<?>> presentersMappedByHeader;
    private final DrillDownCallback drillDownCallback;

    public TabbedResultsPresenter(Component<?> parent, ComponentContext<?> context, DrillDownCallback drillDownCallback,
            StringMessages stringMessages) {
        super(parent, context);
        this.stringMessages = stringMessages;
        this.drillDownCallback = drillDownCallback;
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
        presentersMappedByHeader = new HashMap<>();

        addNewTabTab();
        addTabAndFocus(new MultiResultsPresenter(this, context, drillDownCallback, stringMessages));
    }

    private void addNewTabTab() {
        Label widget = new Label("This should never be shown");
        Image header = new Image(resources.plusIcon());
        tabPanel.add(widget, header);
        // This is necessary to stop the selection of this pseudo tab
        tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
            @Override
            public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
                if (event.getItem() == tabPanel.getWidgetCount() - 1) {
                    event.cancel();
                    addTabAndFocus(new MultiResultsPresenter(TabbedResultsPresenter.this, getComponentContext(),
                            drillDownCallback, stringMessages));
                }
            }
        });
    }

    @Override
    public void showResult(QueryResultDTO<?> result) {
        if (result != null) {
            if (result.getResultType().equals("com.sap.sailing.polars.datamining.shared.PolarAggregation")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(
                        new PolarResultsPresenter(TabbedResultsPresenter.this, getComponentContext(), stringMessages));
                removeTab(oldHeader);
            } else if (result.getResultType().equals("com.sap.sailing.polars.datamining.shared.PolarBackendData")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new PolarBackendResultsPresenter(TabbedResultsPresenter.this, getComponentContext(),
                        stringMessages));
                removeTab(oldHeader);
            } else if (result.getResultType()
                    .equals("com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new ManeuverSpeedDetailsResultsPresenter(TabbedResultsPresenter.this,
                        getComponentContext(), stringMessages));
                removeTab(oldHeader);
            } 
            else if (result.getResultType()
                    .equals("com.sap.sse.datamining.shared.data.PairWithStats")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new NumberPairResultsPresenter(TabbedResultsPresenter.this,
                        getComponentContext(), stringMessages));
                removeTab(oldHeader);
            }
            else {
                if (!(getSelectedPresenter() instanceof MultiResultsPresenter)) {
                    CloseableTabHeader oldHeader = getSelectedHeader();
                    addTabAndFocus(new MultiResultsPresenter(this, getComponentContext(), drillDownCallback, stringMessages));
                    removeTab(oldHeader);
                }
            }
            getSelectedHeader().setText(result.getResultSignifier());
        }
        getSelectedPresenter().showResult(result);
    }

    @Override
    public void showError(String error) {
        getSelectedHeader().setText(stringMessages.error());
        getSelectedPresenter().showError(error);
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        getSelectedHeader().setText(stringMessages.error());
        getSelectedPresenter().showError(mainError, detailedErrors);
    }

    @Override
    public void showBusyIndicator() {
        getSelectedHeader().setText(stringMessages.runningQuery());
        getSelectedPresenter().showBusyIndicator();
    }

    @Override
    public QueryResultDTO<?> getCurrentResult() {
        return getSelectedPresenter().getCurrentResult();
    }

    private CloseableTabHeader getSelectedHeader() {
        return (CloseableTabHeader) tabPanel.getTabWidget(tabPanel.getSelectedIndex());
    }

    private ResultsPresenter<?> getSelectedPresenter() {
        return presentersMappedByHeader.get(getSelectedHeader());
    }

    private void addTabAndFocus(ResultsPresenter<?> tabPresenter) {
        CloseableTabHeader tabHeader = new CloseableTabHeader();
        presentersMappedByHeader.put(tabHeader, tabPresenter);

        tabPanel.insert(tabPresenter.getEntryWidget(), tabHeader, tabPanel.getWidgetCount() - 1);
        int presenterIndex = tabPanel.getWidgetIndex(tabPresenter.getEntryWidget());
        tabPanel.selectTab(presenterIndex);
        tabPanel.scrollToTab(presenterIndex);
    }

    private void removeTab(CloseableTabHeader header) {
        header.removeFromParent();
        presentersMappedByHeader.remove(header);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.tabbedResultsPresenter();
    }

    @Override
    public Widget getEntryWidget() {
        return tabPanel;
    }

    @Override
    public boolean isVisible() {
        return tabPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        tabPanel.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent(Settings settings) {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
        // no-op
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return "tabbedResultsPresenters";
    }

    private class CloseableTabHeader extends HorizontalPanel {

        private final HTML label;

        public CloseableTabHeader() {
            label = new HTML(stringMessages.empty());
            label.getElement().getStyle().setMarginRight(5, Unit.PX);
            this.add(label);
            Image closeImage = new Image(resources.closeIcon());
            closeImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (tabPanel.getWidgetCount() > 2) {
                        removeTab(CloseableTabHeader.this);
                    }
                }
            });
            this.add(closeImage);
        }

        public void setText(String text) {
            label.setText(text);
            tabPanel.checkIfScrollButtonsNecessary();
        }

    }

    @Override
    public String getId() {
        return "TabbedResultsPresenter";
    }
}
