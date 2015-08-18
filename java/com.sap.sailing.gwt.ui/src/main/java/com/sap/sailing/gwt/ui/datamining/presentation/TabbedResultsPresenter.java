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
import com.sap.sailing.gwt.ui.datamining.DataMiningResources;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.QueryResult;

public class TabbedResultsPresenter implements ResultsPresenter<Number> {
    
    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    
    private final StringMessages stringMessages;
    
    private final ScrolledTabLayoutPanel tabPanel;
    private final Map<Widget, ResultsPresenter<Number>> presentersMappedByHeader;
    
    public TabbedResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
        presentersMappedByHeader = new HashMap<>();
        
        addNewTabTab();
        addTabAndFocus();
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
                    addTabAndFocus();
                }
            }
        });
    }

    @Override
    public Widget getWidget() {
        return tabPanel;
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        getSelectedHeader().setText(result.getResultSignifier());
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
    public QueryResult<Number> getCurrentResult() {
        return getSelectedPresenter().getCurrentResult();
    }
    
    private CloseableTabHeader getSelectedHeader() {
        return (CloseableTabHeader) tabPanel.getTabWidget(tabPanel.getSelectedIndex());
    }

    private ResultsPresenter<Number> getSelectedPresenter() {
        return presentersMappedByHeader.get(getSelectedHeader());
    }

    private void addTabAndFocus() {
        CloseableTabHeader tabHeader = new CloseableTabHeader();
        ResultsPresenter<Number> tabPresenter = new MultiResultsPresenter(stringMessages);
        presentersMappedByHeader.put(tabHeader, tabPresenter);
        
        tabPanel.insert(tabPresenter.getWidget(), tabHeader, tabPanel.getWidgetCount() - 1);
        int presenterIndex = tabPanel.getWidgetIndex(tabPresenter.getWidget());
        tabPanel.selectTab(presenterIndex);
        tabPanel.scrollToTab(presenterIndex);
    }
    
    private void removeTab(CloseableTabHeader header) {
        header.removeFromParent();
        presentersMappedByHeader.remove(header);
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

}
