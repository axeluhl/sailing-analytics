package com.sap.sse.datamining.ui.client.presentation;

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
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart.DrillDownCallback;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MultiResultsPresenter extends AbstractDataMiningComponent<Settings> implements ResultsPresenter<Settings> {

    private final DeckLayoutPanel presenterPanel;
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<PresenterDescriptor<Object>> presentersListBox;
    private AbstractResultsPresenter<?> currentPresenter;

    private List<PresenterDescriptor<Object>> availablePresenters;

    public MultiResultsPresenter(Component<?> parent, ComponentContext<?> context,
            DrillDownCallback drillDownCallback) {
        super(parent, context);
        availablePresenters = new ArrayList<>();
        availablePresenters.add(new ColumnChartDescriptor(drillDownCallback));
        availablePresenters.add(new ColumnChartDescriptorWithErrorBars(drillDownCallback));
        availablePresenters.add(new PlainDescriptor());

        presenterPanel = new DeckLayoutPanel();

        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        controlsPanel.add(new Label(getDataMiningStringMessages().choosePresentation() + ":"));
        presentersListBox = new ValueListBox<>(new AbstractObjectRenderer<PresenterDescriptor<? extends Object>>() {
            @Override
            protected String convertObjectToString(PresenterDescriptor<? extends Object> nonNullObject) {
                return nonNullObject.getName();
            }
        });
        presentersListBox.addValueChangeHandler(new ValueChangeHandler<PresenterDescriptor<Object>>() {
            @Override
            public void onValueChange(ValueChangeEvent<PresenterDescriptor<Object>> event) {
                setCurrentPresenter(event.getValue().getPresenter());
            }
        });
        controlsPanel.add(presentersListBox);
        presentersListBox.setValue(availablePresenters.get(0), false);
        presentersListBox.setAcceptableValues(availablePresenters);

        setCurrentPresenter(availablePresenters.get(0).getPresenter());
    }

    private void setCurrentPresenter(AbstractResultsPresenter<?> presenter) {
        controlsPanel.removeFromParent();
        presenter.addControl(controlsPanel);

        if (presenter instanceof AbstractNumericResultsPresenter
                && currentPresenter instanceof AbstractNumericResultsPresenter) {
            String dataKey = ((AbstractNumericResultsPresenter<?>) currentPresenter).getSelectedDataKey();
            ((AbstractNumericResultsPresenter<?>) presenter).setSelectedDataKey(dataKey);
        }

        currentPresenter = presenter;
        presenterPanel.setWidget(currentPresenter.getEntryWidget());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                presenterPanel.onResize();
            }
        });
    }

    @Override
    public QueryResultDTO<? extends Object> getCurrentResult() {
        return currentPresenter.getCurrentResult();
    }

    @Override
    public void showResult(QueryResultDTO<?> result) {
        for (PresenterDescriptor<Object> descriptor : availablePresenters) {
            descriptor.getPresenter().showResult(result);
        }
    }

    @Override
    public void showError(String error) {
        for (PresenterDescriptor<Object> descriptor : availablePresenters) {
            descriptor.getPresenter().showError(error);
        }
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        for (PresenterDescriptor<Object> descriptor : availablePresenters) {
            descriptor.getPresenter().showError(mainError, detailedErrors);
        }
    }

    @Override
    public void showBusyIndicator() {
        for (PresenterDescriptor<Object> descriptor : availablePresenters) {
            descriptor.getPresenter().showBusyIndicator();
        }
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().multiResultsPresenter();
    }

    @Override
    public Widget getEntryWidget() {
        return presenterPanel;
    }

    @Override
    public boolean isVisible() {
        return presenterPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        presenterPanel.setVisible(visibility);
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
        return "multiResultsPresenter";
    }

    private interface PresenterDescriptor<ResultType> {

        public String getName();

        public AbstractResultsPresenter<?> getPresenter();

    }

    private class PlainDescriptor implements PresenterDescriptor<Object> {

        private final AbstractResultsPresenter<?> presenter;

        public PlainDescriptor() {
            presenter = new PlainResultsPresenter(MultiResultsPresenter.this, getComponentContext());
        }

        @Override
        public String getName() {
            return getDataMiningStringMessages().plainText();
        }

        @Override
        public AbstractResultsPresenter<?> getPresenter() {
            return presenter;
        }

    }

    private abstract class AbstractColumnChartDescriptor implements PresenterDescriptor<Object> {
        private final ResultsChart presenter;
        private final String name;

        public AbstractColumnChartDescriptor(String name, boolean showErrorBars, DrillDownCallback drillDownCallback) {
            this.name = name;
            presenter = new ResultsChart(MultiResultsPresenter.this, getComponentContext(), showErrorBars,
                    drillDownCallback);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public AbstractResultsPresenter<?> getPresenter() {
            return presenter;
        }
    }

    private class ColumnChartDescriptor extends AbstractColumnChartDescriptor {
        public ColumnChartDescriptor(DrillDownCallback drillDownCallback) {
            super(getDataMiningStringMessages().columnChart(), /* showErrorBars */ false, drillDownCallback);
        }
    }

    private class ColumnChartDescriptorWithErrorBars extends AbstractColumnChartDescriptor {
        public ColumnChartDescriptorWithErrorBars(DrillDownCallback drillDownCallback) {
            super(getDataMiningStringMessages().columnChartWithErrorBars(), /* showErrorBars */ true,
                    drillDownCallback);
        }
    }

    @Override
    public String getId() {
        return "MultiResultsPresenter";
    }

}
