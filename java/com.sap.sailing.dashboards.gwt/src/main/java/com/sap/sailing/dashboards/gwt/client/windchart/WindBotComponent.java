package com.sap.sailing.dashboards.gwt.client.windchart;

import java.util.Iterator;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Point;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardDataRetrieverListener;
import com.sap.sailing.dashboards.gwt.client.WindBotDataRetrieverListener;
import com.sap.sailing.dashboards.gwt.client.startlineadvantage.LiveAverageComponent;
import com.sap.sailing.dashboards.gwt.client.windchart.compass.LocationPointerCompass;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.SinWave;
import com.sap.sailing.dashboards.gwt.shared.WindType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartPointRecalculator;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

/**
 * The class is an actual widget on the dashboard and shows the measured data of wind bot in a race. It contains a
 * {@link LiveAverageComponent} and a {@link VerticalWindChart} for each the measured true wind speed and the true wind
 * direction. The class implements the {@link RibDashboardDataRetrieverListener} and registers as listener to receive
 * wind data updates. Also it contains a {@link LocationPointerCompass} that indicates the direction and distance from
 * the users device to the wind bot!!!
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class WindBotComponent extends Composite implements HasWidgets, WindBotDataRetrieverListener {

    private SinWave speedSinWave;
    private SinWave directionSinWave;
    private boolean inSimulationMode;

    private MovingAverage movingAverageSpeed;
    private MovingAverage movingAverageDirection;

    private static WindBotComponentUiBinder uiBinder = GWT.create(WindBotComponentUiBinder.class);
    private static final String IN_SIMULATION_MODE = "simulation";

    interface WindBotComponentUiBinder extends UiBinder<Widget, WindBotComponent> {
    }

    interface WindBotComponentStyle extends CssResource {
    }

    /**
     * The header of the widget displaying the name of the wind bot.
     * */
    @UiField
    public HTMLPanel windBotNamePanel;

    /**
     * One of two {@link LiveAverageComponent}s that display in big font the live and the average value of the measured
     * wind speed.
     * */
    @UiField
    public LiveAverageComponent trueWindSpeedLiveAverageComponent;

    /**
     * One of two {@link LiveAverageComponent}s that display in big font the live and the average value of the measured
     * wind direction.
     * */
    @UiField
    public LiveAverageComponent trueWindDirectionLiveAverageComponent;

    /**
     * One of two {@link VerticalWindChart}s that shows the wind fixes speed measured by the wind bot in a chart
     * vertically.
     * */
    @UiField
    public VerticalWindChart trueWindSpeedVerticalWindChart;

    /**
     * One of two {@link VerticalWindChart}s that shows the wind fixes direction measured by the wind bot in a chart
     * vertically.
     * */
    @UiField
    public VerticalWindChart trueWindDirectionVerticalWindChart;

    /**
     * Compass Needle that shows the direction where the wind bot is located to the device. It shows also the distance
     * to the wind bot from the users position.
     * */
    @UiField
    public LocationPointerCompass locationPointerCompass;

    private String windBotId;

    public WindBotComponent(String windBotId) {
        this.windBotId = windBotId;
        String inSimulationModeParameter = Window.Location.getParameter(IN_SIMULATION_MODE);
        if (inSimulationModeParameter != null && inSimulationModeParameter.equals("true")) {
            inSimulationMode = true;
            this.speedSinWave = new SinWave(50, 8);
            this.directionSinWave = new SinWave(100, 8);
        } else {
            inSimulationMode = false;
        }
        movingAverageSpeed = new MovingAverage(500);
        movingAverageDirection = new MovingAverage(500);
        initWidget(uiBinder.createAndBindUi(this));
        windBotNamePanel.getElement().setInnerText("Wind Bot " + windBotId);
        trueWindSpeedVerticalWindChart.addVerticalWindChartClickListener(trueWindSpeedLiveAverageComponent);
        trueWindDirectionVerticalWindChart.addVerticalWindChartClickListener(trueWindDirectionLiveAverageComponent);
    }

    public WindTrackInfoDTO getWindTrackInfoDTOFromAndWindBotID(WindInfoForRaceDTO windInfoForRaceDTO, String id) {
        WindTrackInfoDTO windTrackInfo = null;
        for (WindSource windSource : windInfoForRaceDTO.windTrackInfoByWindSource.keySet()) {
            if (windSource.getType().equals(WindSourceType.EXPEDITION) && windSource.getId() != null) {
                if (windSource.getId().toString().equals(id))
                    windTrackInfo = windInfoForRaceDTO.windTrackInfoByWindSource.get(windSource);
            }
        }
        return windTrackInfo;
    }

    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }

    /**
     * Updates the classes {@link LiveAverageComponent}s and {@link VerticalWindChart}s with data AND updates the
     * {@link #locationPointerCompass} with a new wind bot position !!!
     * */
    @Override
    public void updateWindBotUI(WindInfoForRaceDTO windInfoForRaceDTO) {
        if (windInfoForRaceDTO != null) {
            WindTrackInfoDTO windTrackInfoDTO = getWindTrackInfoDTOFromAndWindBotID(windInfoForRaceDTO, windBotId);
            if (windTrackInfoDTO != null) {
                if (windTrackInfoDTO.windFixes != null && windTrackInfoDTO.windFixes.size() > 0) {
                    Point[] speedPoints = convertWindFixListIntoPointsArray(windTrackInfoDTO.windFixes, WindType.SPEED);
                    Point[] directionPoints = convertWindFixListIntoPointsArray(windTrackInfoDTO.windFixes,
                            WindType.DIRECTION);

                    trueWindSpeedLiveAverageComponent.updateValues(
                            ""
                                    + NumberFormat.getFormat("#0.0").format(
                                            speedPoints[speedPoints.length - 1].getY().doubleValue()), ""
                                    + NumberFormat.getFormat("#0.0").format(movingAverageSpeed.getAverage()));
                    trueWindDirectionLiveAverageComponent.updateValues(
                            ""
                                    + NumberFormat.getFormat("#0.0").format(
                                            directionPoints[directionPoints.length - 1].getY().doubleValue()), ""
                                    + NumberFormat.getFormat("#0.0").format(movingAverageDirection.getAverage()));
                    trueWindSpeedVerticalWindChart.addPointsToSeriesWithAverage(speedPoints,
                            movingAverageSpeed.getAverage());
                    trueWindDirectionVerticalWindChart.addPointsToSeriesWithAverage(directionPoints,
                            movingAverageDirection.getAverage());
                    locationPointerCompass.windBotPositionChanged(windTrackInfoDTO.windFixes
                            .get(windTrackInfoDTO.windFixes.size() - 1).position);
                }
            }
        }
    }

    private Point[] convertWindFixListIntoPointsArray(List<WindDTO> windFixes, WindType windtype) {
        if (windFixes != null && windFixes.size() > 0) {
            Point[] points = new Point[windFixes.size()];
            int counter = 0;
            if (windtype.equals(WindType.DIRECTION)) {
                Point previouspoint = new Point(windFixes.get(0).requestTimepoint, windFixes.get(0).trueWindBearingDeg);
                for (WindDTO windDTO : windFixes) {
                    double nextSinusValue = 0;
                    if (inSimulationMode == true) {
                        nextSinusValue = directionSinWave.getNexNumber();
                    }
                    points[counter] = new Point(windDTO.requestTimepoint, windDTO.trueWindBearingDeg + nextSinusValue);
                    points[counter] = adaptWindDirectionPointToStayCloseToLastPoint(previouspoint, points[counter]);
                    previouspoint = points[counter];
                        movingAverageDirection.add(windDTO.trueWindBearingDeg + nextSinusValue);
                    counter++;
                }
            } else if (windtype.equals(WindType.SPEED)) {
                Point previouspoint = new Point(windFixes.get(0).requestTimepoint,
                        windFixes.get(0).trueWindSpeedInKnots);
                for (WindDTO windDTO : windFixes) {
                    double nextSinusValue = 0;
                    if (inSimulationMode == true) {
                        nextSinusValue = speedSinWave.getNexNumber();
                    }
                    points[counter] = new Point(windDTO.requestTimepoint, windDTO.trueWindSpeedInKnots + nextSinusValue);
                    points[counter] = adaptWindDirectionPointToStayCloseToLastPoint(previouspoint, points[counter]);
                    previouspoint = points[counter];
                    movingAverageSpeed.add(windDTO.trueWindSpeedInKnots + nextSinusValue);
                    counter++;
                }
            }
            return points;
        } else {
            return null;
        }
    }

    private Point adaptWindDirectionPointToStayCloseToLastPoint(Point previousPoint, Point point) {
        return ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, point);
    }
}