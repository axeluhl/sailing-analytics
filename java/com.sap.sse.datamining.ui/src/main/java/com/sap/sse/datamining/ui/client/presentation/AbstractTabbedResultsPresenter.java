package com.sap.sse.datamining.ui.client.presentation;

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
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningResources;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart.DrillDownCallback;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.ScrolledTabLayoutPanel;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractTabbedResultsPresenter extends AbstractDataMiningComponent<Settings>
        implements ResultsPresenter<Settings> {

    protected static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    protected final ScrolledTabLayoutPanel tabPanel;
    protected final Map<Widget, ResultsPresenter<?>> presentersMappedByHeader;
    protected final DrillDownCallback drillDownCallback;
    protected final Map<String, ResultsPresenter<Settings>> registeredResultPresenterMap;

    public AbstractTabbedResultsPresenter(Component<?> parent, ComponentContext<?> context,
            DrillDownCallback drillDownCallback) {
        super(parent, context);
        this.drillDownCallback = drillDownCallback;
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
        presentersMappedByHeader = new HashMap<>();
        registeredResultPresenterMap = new HashMap<>();

        addNewTabTab();
        addTabAndFocus(new MultiResultsPresenter(this, context, drillDownCallback));
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
                    addTabAndFocus(new MultiResultsPresenter(AbstractTabbedResultsPresenter.this, getComponentContext(),
                            drillDownCallback));
                }
            }
        });
    }

    @Override
    public void showError(String error) {
        getSelectedHeader().setText(getDataMiningStringMessages().error());
        getSelectedPresenter().showError(error);
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        getSelectedHeader().setText(getDataMiningStringMessages().error());
        getSelectedPresenter().showError(mainError, detailedErrors);
    }

    @Override
    public void showBusyIndicator() {
        getSelectedHeader().setText(getDataMiningStringMessages().runningQuery());
        getSelectedPresenter().showBusyIndicator();
    }

    @Override
    public QueryResultDTO<?> getCurrentResult() {
        return getSelectedPresenter().getCurrentResult();
    }

    protected CloseableTabHeader getSelectedHeader() {
        return (CloseableTabHeader) tabPanel.getTabWidget(tabPanel.getSelectedIndex());
    }

    protected ResultsPresenter<?> getSelectedPresenter() {
        return presentersMappedByHeader.get(getSelectedHeader());
    }

    protected void addTabAndFocus(ResultsPresenter<?> tabPresenter) {
        CloseableTabHeader tabHeader = new CloseableTabHeader();
        presentersMappedByHeader.put(tabHeader, tabPresenter);

        tabPanel.insert(tabPresenter.getEntryWidget(), tabHeader, tabPanel.getWidgetCount() - 1);
        int presenterIndex = tabPanel.getWidgetIndex(tabPresenter.getEntryWidget());
        tabPanel.selectTab(presenterIndex);
        tabPanel.scrollToTab(presenterIndex);
    }

    protected void removeTab(CloseableTabHeader header) {
        header.removeFromParent();
        presentersMappedByHeader.remove(header);
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().tabbedResultsPresenter();
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

    public class CloseableTabHeader extends HorizontalPanel {

        private final HTML label;

        public CloseableTabHeader() {
            label = new HTML(getDataMiningStringMessages().empty());
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

    /**
     * Register a {@link ResultsPresenter} to handle the retrieved {@link resultType}. Each {@link resultType} can only
     * be registered once. Multiple registrations will cause an {@link IllegalStateException}. When
     * {@link #showResult(QueryResultDTO)} executes it checks whether the {@link resultType} was registered before and
     * triggers the corresponding {@link ResultsPresenter}.
     * 
     * @param resultType
     *            of the datamining query.
     * @param resultPresenter
     *            which shall be used to handle the datamaining query result.
     * 
     * @throws IllegalStateException
     *             if the {@link resultType} is already registered.
     */
    protected void registerResultPresenter(Class<?> resultType, ResultsPresenter<Settings> resultPresenter)
            throws IllegalStateException {
        String className = resultType.getName();
        if (!registeredResultPresenterMap.containsKey(className)) {
            registeredResultPresenterMap.put(className, resultPresenter);
        } else {
            throw new IllegalStateException(
                    "Multiple registration for result type key: " + resultType.toString() + "not allowed.");
        }
    }

    @Override
    public void showResult(QueryResultDTO<?> result) {
        try {
            if (result != null) {
                if (registeredResultPresenterMap.containsKey(result.getResultType())) {
                    CloseableTabHeader oldHeader = getSelectedHeader();
                    addTabAndFocus(registeredResultPresenterMap.get(result.getResultType()));
                    removeTab(oldHeader);
                } else {
                    if (!(getSelectedPresenter() instanceof MultiResultsPresenter)) {
                        CloseableTabHeader oldHeader = getSelectedHeader();
                        addTabAndFocus(new MultiResultsPresenter(this, getComponentContext(), drillDownCallback));
                        removeTab(oldHeader);
                    }
                    getSelectedHeader().setText(result.getResultSignifier());
                }
            }

        } finally {
            getSelectedPresenter().showResult(result);
        }
    }
}
