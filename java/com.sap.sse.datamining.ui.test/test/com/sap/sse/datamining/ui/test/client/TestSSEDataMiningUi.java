package com.sap.sse.datamining.ui.test.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.junit.client.GWTTestCase;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.MultiResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.PlainResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart;
import com.sap.sse.datamining.ui.client.presentation.TabbedResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.TabbedResultsPresenter.ResultsPresenterFactory;
import com.sap.sse.datamining.ui.test.client.presentation.DummyQueryResultDTO;
import com.sap.sse.datamining.ui.test.client.presentation.DummyResultsPresenter;

/**
 * GWT JUnit tests must extend GWTTestCase.
 * 
 * @author D064866
 */
public class TestSSEDataMiningUi extends GWTTestCase {

    /**
     * Must refer to a valid module that sources this class.
     */
    public String getModuleName() {
        return "com.sap.sse.datamining.ui.test.TestSSEDataMiningUiJUnit";
    }

    /**
     * Tests the mechanism {@link TabbedResultsPresenter#registerResultsPresenter(Class, ResultsPresenter)}.
     */
    public void testAvoidDuplicateResultTypeRegistration() {
        TabbedResultsPresenter presenter = new TabbedResultsPresenter(null, null, null);

        presenter.registerResultsPresenter(String.class, new ResultsPresenterFactory<>(PlainResultsPresenter.class,
                () -> new PlainResultsPresenter(null, null)));
        presenter.registerResultsPresenter(Double.class, new ResultsPresenterFactory<>(MultiResultsPresenter.class,
                () -> new MultiResultsPresenter(null, null, null)));
        try {
            presenter.registerResultsPresenter(String.class,
                    new ResultsPresenterFactory<>(ResultsChart.class, () -> new ResultsChart(null, null, true, null)));
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests the mechanism {@link TabbedResultsPresenter#showResult(QueryResultDTO)}.
     */
    public void testShowResultWithCorrectResultsPresenter() {
        TabbedResultsPresenter presenter = new TabbedResultsPresenter(null, null, null);
        List<DummyResultsPresenter> executedPresenter = new ArrayList<>();

        presenter.registerResultsPresenter(String.class, new ResultsPresenterFactory<>(
                StringDummyResultsPresenter.class, () -> new StringDummyResultsPresenter(executedPresenter)));
        presenter.registerResultsPresenter(Double.class, new ResultsPresenterFactory<>(
                DoubleDummyResultsPresenter.class, () -> new DoubleDummyResultsPresenter(executedPresenter)));

        presenter.showResult(null, new DummyQueryResultDTO<>(String.class));
        assertTrue(executedPresenter.size() == 1);
        assertTrue(executedPresenter.get(0) instanceof StringDummyResultsPresenter);

        presenter.showResult(null, new DummyQueryResultDTO<>(Double.class));
        assertTrue(executedPresenter.size() == 2);
        assertTrue(executedPresenter.get(1) instanceof DoubleDummyResultsPresenter);

        presenter.showResult(null, new DummyQueryResultDTO<>(Number.class));
        assertTrue(executedPresenter.size() == 2);
        assertTrue(executedPresenter.get(0) instanceof StringDummyResultsPresenter);
        assertTrue(executedPresenter.get(1) instanceof DoubleDummyResultsPresenter);
    }
    
    private static class StringDummyResultsPresenter extends DummyResultsPresenter {
        public StringDummyResultsPresenter(List<DummyResultsPresenter> executeResultsPresenter) {
            super(executeResultsPresenter);
        }
    }
    
    private static class DoubleDummyResultsPresenter extends DummyResultsPresenter {
        public DoubleDummyResultsPresenter(List<DummyResultsPresenter> executeResultsPresenter) {
            super(executeResultsPresenter);
        }
    }
}
