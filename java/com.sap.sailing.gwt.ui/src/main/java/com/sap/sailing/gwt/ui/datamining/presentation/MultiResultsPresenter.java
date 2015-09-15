package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenterWithControls;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class MultiResultsPresenter implements ResultsPresenter<Object> {
    
    private final StringMessages stringMessages;
    
    private final DeckLayoutPanel presenterPanel;
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<Descriptor<Object>> presentersListBox;
    private ResultsPresenter<Object> currentPresenter;

    private List<Descriptor<Object>> availableDescriptors;
    
    public MultiResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        availableDescriptors = new ArrayList<>();
        availableDescriptors.add(new ColumnChartDescriptor());
        availableDescriptors.add(new PlainDescriptor());

        presenterPanel = new DeckLayoutPanel();
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        controlsPanel.add(new Label(stringMessages.choosePresentation() + ":"));
        presentersListBox = new ValueListBox<>(new AbstractObjectRenderer<Descriptor<? extends Object>>() {
            @Override
            protected String convertObjectToString(Descriptor<? extends Object> nonNullObject) {
                return nonNullObject.getName();
            }
        });
        presentersListBox.addValueChangeHandler(new ValueChangeHandler<Descriptor<Object>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Descriptor<Object>> event) {
                setCurrentPresenter(event.getValue().getPresenter());
            }
        });
        controlsPanel.add(presentersListBox);
        presentersListBox.setValue(availableDescriptors.get(0), false);
        presentersListBox.setAcceptableValues(availableDescriptors);
        
        setCurrentPresenter(availableDescriptors.get(0).getPresenter());
    }

    private void setCurrentPresenter(ResultsPresenterWithControls<Object> presenter) {
        controlsPanel.removeFromParent();
        presenter.addControl(controlsPanel);
        
        currentPresenter = presenter;
        presenterPanel.setWidget(currentPresenter.getWidget());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                presenterPanel.onResize();
            }
        });
    }

    @Override
    public Widget getWidget() {
        return presenterPanel;
    }

    @Override
    public QueryResultDTO<? extends Object> getCurrentResult() {
        return currentPresenter.getCurrentResult();
    }

    @Override
    public void showResult(QueryResultDTO<Object> result) {
        for (Descriptor<Object> descriptor : availableDescriptors) {
            descriptor.getPresenter().showResult(result);
        }
    }

    @Override
    public void showError(String error) {
        for (Descriptor<Object> descriptor : availableDescriptors) {
            descriptor.getPresenter().showError(error);
        }
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        for (Descriptor<Object> descriptor : availableDescriptors) {
            descriptor.getPresenter().showError(mainError, detailedErrors);
        }
    }

    @Override
    public void showBusyIndicator() {
        for (Descriptor<Object> descriptor : availableDescriptors) {
            descriptor.getPresenter().showBusyIndicator();
        }
    }

    private interface Descriptor<ResultType> {
        
        public String getName();
        
        public ResultsPresenterWithControls<ResultType> getPresenter();
        
    }
    
    private class PlainDescriptor implements Descriptor<Object> {
        
        private final AbstractResultsPresenter<Object> presenter;
        
        public PlainDescriptor() {
            presenter = new PlainResultsPresenter(stringMessages);
        }

        @Override
        public String getName() {
            return stringMessages.plainText();
        }
        
        @Override
        public AbstractResultsPresenter<Object> getPresenter() {
            return presenter;
        }
        
    }
    
    private class ColumnChartDescriptor implements Descriptor<Object> {
        
        private final ResultsChart presenter;

        public ColumnChartDescriptor() {
            presenter = new ResultsChart(stringMessages);
        }

        @Override
        public String getName() {
            return stringMessages.columnChart();
        }

        @Override
        public AbstractResultsPresenter<Object> getPresenter() {
            return presenter;
        }
        
    }
    
}
