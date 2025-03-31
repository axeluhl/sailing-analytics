package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_FLEET_NAME;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueProvider;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionFleetView;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionSeriesView;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public abstract class RegattaCompetitionPresenter
        implements RefreshableWidget<ListResult<RaceCompetitionFormatSeriesDTO>>,
        FilterValueProvider<SimpleCompetitorDTO>, FilterValueChangeHandler<SimpleRaceMetadataDTO> {

    private final RegattaCompetitionView view;
    private Filter<SimpleRaceMetadataDTO> latestRacesByCompetitorFilter;
    
    private Map<RegattaCompetitionSeriesView, Map<RegattaCompetitionFleetView, 
            Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO>>> structure = new HashMap<>();
    private final PaywallResolver paywallResolver;

    public RegattaCompetitionPresenter(RegattaCompetitionView view, PaywallResolver paywallResolver) {
        this.view = view;
        this.paywallResolver = paywallResolver;
    }

    @Override
    public void setData(ListResult<RaceCompetitionFormatSeriesDTO> data) {
        view.clearContent();
        structure.clear();
        for (RaceCompetitionFormatSeriesDTO series : data.getValues()) {
            RegattaCompetitionSeriesView seriesView = view.addSeriesView(series);
            Map<RegattaCompetitionFleetView, Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO>> fleetMap = new HashMap<>();
            structure.put(seriesView, fleetMap);
            for (RaceCompetitionFormatFleetDTO fleet : series.getFleets()) {
                RegattaCompetitionFleetView fleetView = seriesView.addFleetView(fleet);
                fleetView.setNumberOfFleetsInSeries(series.getFleets().size());
                Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO> raceMap = new HashMap<>();
                fleetMap.put(fleetView, raceMap);
                for (SimpleRaceMetadataDTO race : fleet.getRaces()) {
                    final String fleetName = fleet.getFleet().getFleetName();
                    RegattaCompetitionRaceView raceView = fleetView.addRaceView(race,
                            fleetName == null ? DEFAULT_FLEET_NAME : fleetName, this);
                    raceMap.put(raceView, race);
                }
            }
            if (latestRacesByCompetitorFilter != null) {
                applyFilter(seriesView, latestRacesByCompetitorFilter);
            }
        }
    }

    @Override
    public void onFilterValueChanged(Filter<SimpleRaceMetadataDTO> filter) {
        latestRacesByCompetitorFilter = filter;
        for (RegattaCompetitionSeriesView seriesView : structure.keySet()) {
            applyFilter(seriesView, latestRacesByCompetitorFilter);
        }
    }
    
    @Override
    public Collection<SimpleCompetitorDTO> getFilterableValues() {
        Set<SimpleCompetitorDTO> filterableValues = new HashSet<>();
        for (Map<RegattaCompetitionFleetView, Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO>> series : structure.values()) {
            for (Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO> fleet : series.values()) {
                for (SimpleRaceMetadataDTO raceMetadata : fleet.values()) {
                    filterableValues.addAll(raceMetadata.getCompetitors());
                }
            }
        }
        return filterableValues;
    }
    
    private void applyFilter(RegattaCompetitionSeriesView seriesView, Filter<SimpleRaceMetadataDTO> filter) {
        int unfilteredFleetCount = 0;
        Map<RegattaCompetitionFleetView, Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO>> fleetMap = structure.get(seriesView);
        for (Entry<RegattaCompetitionFleetView, Map<RegattaCompetitionRaceView, SimpleRaceMetadataDTO>> fleetEntry : fleetMap.entrySet()) {
            boolean filterFleet = true;
            for (Entry<RegattaCompetitionRaceView, SimpleRaceMetadataDTO> raceEntry : fleetEntry.getValue().entrySet()) {
                boolean filterRace = !filter.matches(raceEntry.getValue());
                raceEntry.getKey().doFilter(filterRace);
                filterFleet &= filterRace;
            }
            fleetEntry.getKey().doFilter(filterFleet);
            unfilteredFleetCount = unfilteredFleetCount + (filterFleet ? 0 : 1);
        }
        for (RegattaCompetitionFleetView fleetView : fleetMap.keySet()) {
            fleetView.setNumberOfFleetsInSeries(unfilteredFleetCount);
        }
        seriesView.doFilter(unfilteredFleetCount == 0);
    }
    
    protected abstract String getRaceViewerURL(SimpleRaceMetadataDTO raceMetadata, String mode);
    
    protected abstract String getMapAndWindChartUrl(String leaderboardName, String raceName, String fleetName);

    public PaywallResolver getPaywallResolver() {
        return paywallResolver;
    }


}
