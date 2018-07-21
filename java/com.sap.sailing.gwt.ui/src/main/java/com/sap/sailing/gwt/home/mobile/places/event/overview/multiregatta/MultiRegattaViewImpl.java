package com.sap.sailing.gwt.home.mobile.places.event.overview.multiregatta;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.communication.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.home.communication.event.RegattasAndLiveRacesDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterPresenter;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueProvider;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.RegattaByLeaderboardGroupNameFilter;
import com.sap.sailing.gwt.home.shared.partials.regattalist.RegattaListPresenter;

public class MultiRegattaViewImpl extends AbstractEventOverview {
    
    private RegattaStatus regattaStatusUi;
    
    public MultiRegattaViewImpl(EventViewBase.Presenter presenter) {
        super(presenter, false, false);
        FlowPanel container = new FlowPanel();
        this.setupOverviewStage(container);
        this.setupEventDescription(container);
        this.setupRegattaStatusList(container);
        this.setupUpdateBox(container);
        this.setupImpressions(container);
        this.setupStatisticsBox(container, false);
        setViewContent(container);
    }
    
    private void setupRegattaStatusList(Panel container) {
        regattaStatusUi = new RegattaStatus(currentPresenter);
        RegattaListPresenter<RegattasAndLiveRacesDTO> regattaListPresenter = 
                new RegattaListPresenter<RegattasAndLiveRacesDTO>(regattaStatusUi);
        RegattaByLeaderboardGroupNameFilter leaderboardGroupNameFilter = new RegattaByLeaderboardGroupNameFilter();
        regattaStatusUi.setFilterSectionWidget(leaderboardGroupNameFilter);
        MultiRegattaViewImplFilterPresenter filterPresenter = new MultiRegattaViewImplFilterPresenter(
                leaderboardGroupNameFilter, regattaListPresenter, regattaListPresenter);
        container.add(regattaStatusUi);
        refreshManager.add(filterPresenter.getRefreshableWidgetWrapper(
                regattaListPresenter.getRefreshableWidgetWrapper(regattaStatusUi)), 
                new GetRegattasAndLiveRacesForEventAction(getEventId()));
    }
    
    private class MultiRegattaViewImplFilterPresenter extends FilterPresenter<RegattaMetadataDTO, String> {

        private final List<FilterValueProvider<String>> valueProviders;
        private final List<FilterValueChangeHandler<RegattaMetadataDTO>> valueChangeHandlers;
        
        public MultiRegattaViewImplFilterPresenter(FilterWidget<RegattaMetadataDTO, String> filterWidget,
                FilterValueProvider<String> valueProvider,
                FilterValueChangeHandler<RegattaMetadataDTO> valueChangeHandler) {
            super(filterWidget);
            this.valueProviders = Arrays.asList(valueProvider);
            this.valueChangeHandlers = Arrays.asList(valueChangeHandler);
            super.addHandler(valueChangeHandler);
        }
        
        @Override
        protected List<FilterValueProvider<String>> getCurrentValueProviders() {
            return valueProviders;
        }

        @Override
        protected List<FilterValueChangeHandler<RegattaMetadataDTO>> getCurrentValueChangeHandlers() {
            return valueChangeHandlers;
        }
    }

}
