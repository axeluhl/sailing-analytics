package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ScrolledTabLayoutPanel;
import com.sap.sailing.gwt.ui.datamining.DataMiningResources;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.QueryResult;

public class TabResultsPresenter implements ResultsPresenter<Number> {
    
    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    
    private final StringMessages stringMessages;
    
    private final ScrolledTabLayoutPanel tabPanel;
    
    public TabResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
    }

    @Override
    public Widget getWidget() {
        return tabPanel;
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        ResultsPresenter<Number> tabPresenter = addTabAndFocus(result.getResultSignifier());
        tabPresenter.showResult(result);
    }

    @Override
    public void showError(String error) {
        ResultsPresenter<Number> tabPresenter = addTabAndFocus(stringMessages.error());
        tabPresenter.showError(error);
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        ResultsPresenter<Number> tabPresenter = addTabAndFocus(stringMessages.error());
        tabPresenter.showError(mainError, detailedErrors);
    }

    @Override
    public void showBusyIndicator() {
        // Do nothing here. A management of the different tabs is necessary to handle this.
    }

    private ResultsPresenter<Number> addTabAndFocus(String headerText) {
        CloseableTabHeader tabHeader = new CloseableTabHeader(headerText);
        ResultsChart tabPresenter = new ResultsChart(stringMessages);
        tabPanel.add(tabPresenter.getWidget(), tabHeader.getWidget());
        tabPanel.selectTab(tabPanel.getWidgetCount() - 1);
        tabPanel.scrollToTab(tabPanel.getWidgetCount() - 1);
        return tabPresenter;
    }
    
    private class CloseableTabHeader {
        
        private final HorizontalPanel widget;
        
        public CloseableTabHeader(String text) {
            widget = new HorizontalPanel();
            HTML label = new HTML(text);
            label.getElement().getStyle().setMarginRight(5, Unit.PX);
            widget.add(label);
            Image closeImage = new Image(resources.closeIcon());
            closeImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (tabPanel.getWidgetCount() > 1) {
                        widget.removeFromParent();
                    }
                }
            });
            widget.add(closeImage);
        }
        
        public HorizontalPanel getWidget() {
            return widget;
        }
        
    }

}
