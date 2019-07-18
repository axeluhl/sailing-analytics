package com.sap.sailing.windestimation.datavisualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.CountDownLatch;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
    private JFreeChart mainChart;
    private JFreeChart histoChart;
    private XYSeries meanSeries;
    private XYSeries medianSeries;
    private XYSeries q1Series;
    private XYSeries q3Series;
    private XYSeries p1Series;
    private XYSeries p99Series;
    private XYSeries valuesSeries;
    private XYSeries stdSeries;
    private XYSeries zeroMeanStdSeries;
    private final CountDownLatch windowClosedLatch = new CountDownLatch(1);

    public SingleDimensionAggregatesPlottingFrame(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager) {
        super(persistenceManager.getCollectionName());
        this.persistenceManager = persistenceManager;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                windowClosedLatch.countDown();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                setLayout(null);
                setVisible(false);
                dispose();
            }
        });
        initializeDataWithCharts();
    }

    private void initializeDataWithCharts() {
        createSeries();
        fillSeries();
        DefaultTableXYDataset mainDataset = createMainDataset();
        DefaultTableXYDataset histoDataset = createHistoDataset();
        mainChart = createMainChart(mainDataset);
        histoChart = createHistoChart(histoDataset);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ChartPanel mainChartPanel = new ChartPanel(mainChart);
        ChartPanel histoChartPanel = new ChartPanel(histoChart);
        JButton refreshButton = new JButton("Refresh charts");
        refreshButton.addActionListener(event -> refreshCharts());
        panel.add(refreshButton);
        panel.add(mainChartPanel);
        panel.add(histoChartPanel);
        setContentPane(panel);
    }

    public void refreshCharts() {
        SwingUtilities.invokeLater(() -> {
            initializeDataWithCharts();
            revalidate();
        });
    }

    private void createSeries() {
        meanSeries = new XYSeries("Mean", true, false);
        medianSeries = new XYSeries("Median", true, false);
        q1Series = new XYSeries("Q1", true, false);
        q3Series = new XYSeries("Q3", true, false);
        p1Series = new XYSeries("P1", true, false);
        p99Series = new XYSeries("P99", true, false);
        valuesSeries = new XYSeries("Values", true, false);
        stdSeries = new XYSeries("Sigma", true, false);
        zeroMeanStdSeries = new XYSeries("Zero mean sigma", true, false);
    }

    private JFreeChart createMainChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(persistenceManager.getDimensionType().getDimensionName(),
                persistenceManager.getDimensionType().getUnitName(), "Abs. TWD change in degrees", dataset);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.BLACK);
        renderer.setSeriesPaint(3, Color.BLACK);
        renderer.setSeriesPaint(4, Color.BLUE);
        renderer.setSeriesPaint(5, Color.BLUE);
        renderer.setSeriesPaint(6, Color.PINK);
        renderer.setSeriesPaint(7, Color.GREEN);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        Shape shape = new Ellipse2D.Double(0, 0, 2, 2);
        renderer.setBaseShape(shape);
        BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        renderer.setBaseStroke(stroke);
        for (int i = 0; i < 8; i++) {
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

    private JFreeChart createHistoChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(null, persistenceManager.getDimensionType().getUnitName(),
                "Values", dataset);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        Shape shape = new Ellipse2D.Double(0, 0, 2, 2);
        renderer.setBaseShape(shape);
        BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        renderer.setBaseStroke(stroke);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, shape);
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

    protected DefaultTableXYDataset createMainDataset() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(meanSeries);
        dataset.addSeries(medianSeries);
        dataset.addSeries(q1Series);
        dataset.addSeries(q3Series);
        dataset.addSeries(p1Series);
        dataset.addSeries(p99Series);
        dataset.addSeries(stdSeries);
        dataset.addSeries(zeroMeanStdSeries);
        return dataset;
    }

    protected DefaultTableXYDataset createHistoDataset() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(valuesSeries);
        return dataset;
    }

    protected void fillSeries() {
        AggregatedSingleDimensionBasedTwdTransition prev = null;
        for (PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> iterator = persistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = iterator.next();
            if (prev != null && prev.getDimensionValue() >= aggregate.getDimensionValue()) {
                throw new IllegalStateException("prev value cannot be >= current value (prev = "
                        + prev.getDimensionValue() + ", current = " + aggregate.getDimensionValue() + ")");
            }
            try {
                meanSeries.add(aggregate.getDimensionValue(), aggregate.getMean());
                medianSeries.add(aggregate.getDimensionValue(), aggregate.getMedian());
                q1Series.add(aggregate.getDimensionValue(), aggregate.getQ1());
                q3Series.add(aggregate.getDimensionValue(), aggregate.getQ3());
                p1Series.add(aggregate.getDimensionValue(), aggregate.getP1());
                p99Series.add(aggregate.getDimensionValue(), aggregate.getP99());
                valuesSeries.add(aggregate.getDimensionValue(), aggregate.getNumberOfValues());
                stdSeries.add(aggregate.getDimensionValue(), aggregate.getStd());
                zeroMeanStdSeries.add(aggregate.getDimensionValue(), aggregate.getZeroMeanStd());
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

    public void awaitWindowClosed() throws InterruptedException {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        windowClosedLatch.await();
    }

}
