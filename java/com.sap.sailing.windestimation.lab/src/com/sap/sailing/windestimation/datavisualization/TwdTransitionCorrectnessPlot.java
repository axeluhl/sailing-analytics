package com.sap.sailing.windestimation.datavisualization;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import com.sap.sailing.windestimation.data.LabeledTwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.serialization.TwdTransitionJsonSerializer;

public class TwdTransitionCorrectnessPlot extends JFrame {

    private static final long serialVersionUID = 5673299850159956983L;
    private final TwdTransitionPersistenceManager persistenceManager;
    private final HistogramDataset dataset;

    public TwdTransitionCorrectnessPlot(TwdTransitionPersistenceManager persistenceManager) {
        super(persistenceManager.getCollectionName());
        this.persistenceManager = persistenceManager;
        dataset = new HistogramDataset();
        JFreeChart chart = ChartFactory.createHistogram("TWD Transition correctnees", "TWD delta in degrees", null,
                dataset, PlotOrientation.VERTICAL, true, false, false);
        // Create Panel
        ChartPanel mainChartPanel = new ChartPanel(chart);
        setContentPane(mainChartPanel);
        try {
            ChartUtilities.saveChartAsPNG(new File("twdTransitionCorrectnessHistogram.png"), chart, 1920, 1080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void fillSeries() {
        double[] correctValues = new double[(int) persistenceManager
                .countElements(new Document(TwdTransitionJsonSerializer.CORRECT, true))];
        double[] incorrectValues = new double[(int) persistenceManager
                .countElements(new Document(TwdTransitionJsonSerializer.CORRECT, false))];
        int i = 0;
        int j = 0;
        for (PersistedElementsIterator<LabeledTwdTransition> iterator = persistenceManager.getIterator(); iterator
                .hasNext();) {
            LabeledTwdTransition twdTransition = (LabeledTwdTransition) iterator.next();
            if (twdTransition.isCorrect()) {
                correctValues[i++] = twdTransition.getTwdChange().getDegrees();
            } else {
                incorrectValues[j++] = twdTransition.getTwdChange().getDegrees();
            }
        }
        dataset.addSeries("Correct", correctValues, 50);
        dataset.addSeries("Incorrect", incorrectValues, 50);
    }

    public void renderChart() {
        SwingUtilities.invokeLater(() -> {
            setSize(800, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        });
    }

    public static void main(String[] args) throws UnknownHostException {
        TwdTransitionCorrectnessPlot plot = new TwdTransitionCorrectnessPlot(new TwdTransitionPersistenceManager());
        plot.renderChart();
    }

}
