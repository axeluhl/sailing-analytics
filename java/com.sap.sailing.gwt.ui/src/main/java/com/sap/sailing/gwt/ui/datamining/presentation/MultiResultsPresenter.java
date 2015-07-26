package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;

public class MultiResultsPresenter extends AbstractResultsPresenter<Number> {
    
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<Descriptor> presentersListBox;
    private AbstractResultsPresenter<Number> currentPresenter;
    
    public MultiResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);

        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        
        controlsPanel.add(new Label(getStringMessages().choosePresentation() + ":"));
        presentersListBox = new ValueListBox<>(new AbstractObjectRenderer<Descriptor>() {
            @Override
            protected String convertObjectToString(Descriptor nonNullObject) {
                return nonNullObject.getName();
            }
        });
        presentersListBox.addValueChangeHandler(new ValueChangeHandler<Descriptor>() {
            @Override
            public void onValueChange(ValueChangeEvent<Descriptor> event) {
                if (currentPresenter != null) {
                    for (Widget controlWidget : currentPresenter.getControlWidgets()) {
                        controlWidget.removeFromParent();
                    }
                }
                currentPresenter = event.getValue().getPresenter();
                for (Widget controlWidget : currentPresenter.getControlWidgets()) {
                    addControlWidget(controlWidget);
                }
                updatePresentationWidget();
            }
        });
        controlsPanel.add(presentersListBox);
        addControlWidget(controlsPanel);
        
        List<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new ColumnChartDescriptor());
        descriptors.add(new PlainDescriptor());
        presentersListBox.setValue(descriptors.get(0), true);
        presentersListBox.setAcceptableValues(descriptors);
    }

    @Override
    protected Widget getPresentationWidget() {
        return getCurrentPresenter().getPresentationWidget();
    }
    
    @Override
    protected Iterable<Widget> getControlWidgets() {
        Collection<Widget> controlWidgets = new ArrayList<>();
        controlWidgets.add(controlsPanel);
        return controlWidgets;
    }

    @Override
    protected void internalShowResult() {
        getCurrentPresenter().showResult(getCurrentResult());
    }

    private AbstractResultsPresenter<Number> getCurrentPresenter() {
        return currentPresenter;
    }
    
    private interface Descriptor {
        
        public String getName();
        
        public AbstractResultsPresenter<Number> getPresenter();
        
    }
    
    private class PlainDescriptor implements Descriptor {
        
        private final AbstractResultsPresenter<Number> presenter;
        
        public PlainDescriptor() {
            presenter = new PlainResultsPresenter(getStringMessages());
        }

        @Override
        public String getName() {
            return getStringMessages().plainText();
        }
        
        @Override
        public AbstractResultsPresenter<Number> getPresenter() {
            return presenter;
        }
        
    }
    
    private class ColumnChartDescriptor implements Descriptor {
        
        private final ResultsChart presenter;

        public ColumnChartDescriptor() {
            presenter = new ResultsChart(getStringMessages());
        }

        @Override
        public String getName() {
            return getStringMessages().columnChart();
        }

        @Override
        public AbstractResultsPresenter<Number> getPresenter() {
            return presenter;
        }
        
    }
    
}
