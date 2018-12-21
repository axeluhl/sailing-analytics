package com.sap.sailing.windestimation.datavisualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;

public class SingleDimensionAggregatesPlottingFrame extends JFrame {

    private static final long serialVersionUID = 5673299850159956983L;
    private final AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager;

    public SingleDimensionAggregatesPlottingFrame(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager) {
        super(persistenceManager.getCollectionName());
        this.persistenceManager = persistenceManager;
        DefaultTableXYDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                persistenceManager.getDimensionType().toString().toLowerCase(),
                persistenceManager.getDimensionType().toString().toLowerCase(), "Abs. TWD change in degrees", dataset);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesPaint(1, Color.PINK);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesPaint(3, Color.BLACK);
        renderer.setSeriesPaint(4, Color.BLACK);
        renderer.setSeriesPaint(5, Color.BLUE);
        renderer.setSeriesPaint(6, Color.BLUE);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        Shape shape = new Ellipse2D.Double(0, 0, 2, 2);
        renderer.setBaseShape(shape);
        BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        renderer.setBaseStroke(stroke);
        for (int i = 0; i < 7; i++) {
            renderer.setSeriesStroke(i, stroke);
            renderer.setSeriesShape(i, shape);
        }
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);
        chart.getLegend().setFrame(BlockBorder.NONE);
        // chart.setTitle(new TextTitle("Average Salary per Age",
        // new Font("Serif", java.awt.Font.BOLD, 18)
        // )
        // );
        return chart;
    }

    protected DefaultTableXYDataset createDataset() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        final XYSeries meanSeries = new XYSeries("Mean", true, false);
        final XYSeries stdSeries = new XYSeries("Standard deviation", true, false);
        final XYSeries medianSeries = new XYSeries("Median", true, false);
        final XYSeries q1Series = new XYSeries("Q1", true, false);
        final XYSeries q3Series = new XYSeries("Q3", true, false);
        final XYSeries p1Series = new XYSeries("P1", true, false);
        final XYSeries p99Series = new XYSeries("P99", true, false);
        fillSeries(meanSeries, stdSeries, medianSeries, q1Series, q3Series, p1Series, p99Series);
        dataset.addSeries(meanSeries);
        dataset.addSeries(stdSeries);
        dataset.addSeries(medianSeries);
        dataset.addSeries(q1Series);
        dataset.addSeries(q3Series);
        dataset.addSeries(p1Series);
        dataset.addSeries(p99Series);
        return dataset;
    }

    protected void fillSeries(XYSeries meanSeries, XYSeries stdSeries, XYSeries medianSeries, XYSeries q1Series,
            XYSeries q3Series, XYSeries p1Series, XYSeries p99Series) {
        AggregatedSingleDimensionBasedTwdTransition prev = null;
        for (PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> iterator = persistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = iterator.next();
            if (prev != null && prev.getDimensionValue() >= aggregate.getDimensionValue()) {
                throw new IllegalStateException("prev value cannot be >= current value");
            }
            try {
                meanSeries.add(aggregate.getDimensionValue(), aggregate.getMean());
                stdSeries.add(aggregate.getDimensionValue(), aggregate.getStd());
                medianSeries.add(aggregate.getDimensionValue(), aggregate.getMedian());
                q1Series.add(aggregate.getDimensionValue(), aggregate.getQ1());
                q3Series.add(aggregate.getDimensionValue(), aggregate.getQ3());
                p1Series.add(aggregate.getDimensionValue(), aggregate.getP1());
                p99Series.add(aggregate.getDimensionValue(), aggregate.getP99());
            } catch (Exception e) {
                e.printStackTrace();
            }
            prev = aggregate;
        }
    }

    public void renderChart() {
        SwingUtilities.invokeLater(() -> {
            setSize(800, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        });
    }

}
