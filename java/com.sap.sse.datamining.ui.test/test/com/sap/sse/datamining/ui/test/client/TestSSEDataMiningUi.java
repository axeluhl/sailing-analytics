package com.sap.sse.datamining.ui.test.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.junit.client.GWTTestCase;
import com.sap.sse.datamining.ui.client.presentation.MultiResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.PlainResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart;
import com.sap.sse.datamining.ui.client.presentation.TabbedResultsPresenter;
import com.sap.sse.datamining.ui.test.client.presentation.DummyQueryResultDTO;
import com.sap.sse.datamining.ui.test.client.presentation.DummyResultsPresenter;

/**
 * GWT JUnit tests must extend GWTTestCase.
 */
public class TestSSEDataMiningUi extends GWTTestCase {

    /**
     * Must refer to a valid module that sources this class.
     */
    public String getModuleName() {
        return "com.sap.sse.datamining.ui.test.TestSSEDataMiningUiJUnit";
    }

    /**
     * Tests the mechanism {@link TabbedResultsPresenter#registerResultPresenter(Class, ResultsPresenter)}.
     */
    public void testAvoidDuplicateResultTypeRegistration() {
        TabbedResultsPresenter presenter = new TabbedResultsPresenter(null, null, null);

        presenter.registerResultPresenter(String.class, new PlainResultsPresenter(null, null));
        presenter.registerResultPresenter(Double.class, new MultiResultsPresenter(null, null, null));
        try {
            presenter.registerResultPresenter(String.class, new ResultsChart(null, null, true, null));
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

        DummyResultsPresenter stringResultsPresenter = new DummyResultsPresenter(executedPresenter);
        DummyResultsPresenter doubleResultsPresenter = new DummyResultsPresenter(executedPresenter);

        presenter.registerResultPresenter(String.class, stringResultsPresenter);
        presenter.registerResultPresenter(Double.class, doubleResultsPresenter);

        presenter.showResult(new DummyQueryResultDTO<>(String.class));
        assertTrue(executedPresenter.size() == 1);
        assertEquals(executedPresenter.get(0), stringResultsPresenter);

        presenter.showResult(new DummyQueryResultDTO<>(Double.class));
        assertTrue(executedPresenter.size() == 2);
        assertEquals(executedPresenter.get(1), doubleResultsPresenter);

        presenter.showResult(new DummyQueryResultDTO<>(Number.class));
        assertTrue(executedPresenter.size() == 2);
        assertEquals(executedPresenter.get(0), stringResultsPresenter);
        assertEquals(executedPresenter.get(1), doubleResultsPresenter);
    }
}
