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
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterPresenter;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.RegattaByBootCategoryFilter;
import com.sap.sailing.gwt.home.shared.partials.regattalist.RegattaListPresenter;

public class MultiRegattaViewImpl extends AbstractEventOverview {
    
    private RegattaStatus regattaStatusUi;
    
    public MultiRegattaViewImpl(EventViewBase.Presenter presenter) {
        super(presenter, false, false);
        FlowPanel container = new FlowPanel();
        this.setupOverviewStage(container);
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
        RegattaByBootCategoryFilter bootCategoryFilter = new RegattaByBootCategoryFilter();
        if (ExperimentalFeatures.SHOW_BOAT_CATEGORY_FILTER_ON_MOBILE) {
            regattaStatusUi.setFilterSectionWidget(bootCategoryFilter);
        }
        MultiRegattaViewImplFilterPresenter filterPresenter = 
                new MultiRegattaViewImplFilterPresenter(bootCategoryFilter, regattaListPresenter);
        container.add(regattaStatusUi);
        refreshManager.add(filterPresenter.getRefreshableWidgetWrapper(
                regattaListPresenter.getRefreshableWidgetWrapper(regattaStatusUi)), 
                new GetRegattasAndLiveRacesForEventAction(getEventId()));
    }
    
    private class MultiRegattaViewImplFilterPresenter extends FilterPresenter<RegattaMetadataDTO, String> {
        private final List<FilterValueChangeHandler<RegattaMetadataDTO, String>> valueChangeHandler;
        
        public MultiRegattaViewImplFilterPresenter(FilterWidget<RegattaMetadataDTO, String> filterWidget,
            FilterValueChangeHandler<RegattaMetadataDTO, String> valueChangeHandler) {
            super(filterWidget);
            this.valueChangeHandler = Arrays.asList(valueChangeHandler);
            super.addHandler(valueChangeHandler);
        }
        @Override
        protected List<FilterValueChangeHandler<RegattaMetadataDTO, String>> getCurrentValueChangeHandlers() {
            return valueChangeHandler;
        }
        
    }

}
