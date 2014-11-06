package com.sap.sailing.dashboards.gwt.client.windchart;

import java.util.Date;
import java.util.Iterator;

import org.moxieapps.gwt.highcharts.client.Point;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardDataRetriever;
import com.sap.sailing.dashboards.gwt.client.RibDashboardDataRetrieverListener;
import com.sap.sailing.dashboards.gwt.client.startlineadvantage.LiveAverageComponent;
import com.sap.sailing.dashboards.gwt.client.windchart.compass.LocationPointerCompass;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.WindBotComponentDTO;

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
public class WindBotComponent extends Composite implements HasWidgets, RibDashboardDataRetrieverListener {

    private static WindBotComponentUiBinder uiBinder = GWT.create(WindBotComponentUiBinder.class);

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
        RibDashboardDataRetriever.getInstance().addDataObserver(this);
        initWidget(uiBinder.createAndBindUi(this));
        windBotNamePanel.getElement().setInnerText("Wind Bot " + windBotId);
    }

    /**
     * Updates the classes {@link LiveAverageComponent}s and {@link VerticalWindChart}s with data AND updates the
     * {@link #locationPointerCompass} with a new wind bot position !!!
     * */
    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        WindBotComponentDTO windBotDTO = liveRaceInfoDTO.windBotDTOForID.get(windBotId);
        if (windBotDTO != null) {
            double now = new Date().getTime();
            trueWindSpeedLiveAverageComponent.updateValues(
                    "" + NumberFormat.getFormat("#0.0").format(windBotDTO.liveWindSpeedInKts), ""
                            + NumberFormat.getFormat("#0.0").format(windBotDTO.averageWindSpeedInKts));
            trueWindDirectionLiveAverageComponent.updateValues(
                    "" + NumberFormat.getFormat("#0.0").format(windBotDTO.liveWindDirectionInDegrees), ""
                            + NumberFormat.getFormat("#0.0").format(windBotDTO.averageWindDirectionInDegrees));
            trueWindSpeedVerticalWindChart.addPointToSeriesWithAverage(new Point(now, windBotDTO.liveWindSpeedInKts),
                    windBotDTO.averageWindSpeedInKts, false);
            trueWindDirectionVerticalWindChart.addPointToSeriesWithAverage(new Point(now,
                    windBotDTO.liveWindDirectionInDegrees), windBotDTO.averageWindDirectionInDegrees, true);
            locationPointerCompass.windBotPositionChanged(windBotDTO.position);
        }
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
}