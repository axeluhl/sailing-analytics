package com.sap.sse.datamining.ui.test.client.presentation;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.presentation.AbstractResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.TabbedResultsPresenter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * Dummy class for testing the
 * {@link TabbedResultsPresenter#registerResultsPresenter(Class, com.sap.sse.datamining.ui.client.ResultsPresenter)}
 * method. Therefore its {@link #showResult(QueryResultDTO)} method is overridden and simply stores a reference of this
 * presenter instance.
 * 
 * @author D064866
 *
 */
public class DummyResultsPresenter extends AbstractResultsPresenter<Settings> {

    private final DockLayoutPanel layoutPanel;
    private final List<DummyResultsPresenter> executedResultsPresenter;

    public DummyResultsPresenter(List<DummyResultsPresenter> executeResultsPresenter) {
        super(null, null);
        this.executedResultsPresenter = executeResultsPresenter;
        layoutPanel = new DockLayoutPanel(Unit.PCT);
    }

    /**
     * Stores <code>this</code> ResultsPresenter for test purposes.
     */
    @Override
    public void showResult(StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result) {
        executedResultsPresenter.add(this);
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent(Settings useTheseSettings) {
        return null;
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    @Override
    protected Widget getPresentationWidget() {
        return layoutPanel;
    }

    @Override
    protected void internalShowResults(QueryResultDTO<?> result) {
    }

}
