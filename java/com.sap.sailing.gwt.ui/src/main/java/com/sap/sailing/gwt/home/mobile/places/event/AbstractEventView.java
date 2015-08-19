package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public abstract class AbstractEventView extends Composite implements EventViewBase {
    
    private static AbstractEventViewUiBinder uiBinder = GWT.create(AbstractEventViewUiBinder.class);

    interface AbstractEventViewUiBinder extends UiBinder<Widget, AbstractEventViewLayout> {
    }
    
    static class AbstractEventViewLayout {
        @UiField(provided = true) EventHeader eventHeaderUi;
        @UiField Quickfinder quickFinderUi;
        @UiField SimpleInfoBlock simpleInfoUi;
        @UiField SimplePanel viewContentUi;
        
        private AbstractEventViewLayout(EventViewDTO event, PlaceNavigation<?> logoNavigation) {
            this.eventHeaderUi = new EventHeader(event, logoNavigation);
        }
    }

    private final Presenter currentPresenter;
    private final AbstractEventViewLayout layout;
    protected final RefreshManager refreshManager;

    public AbstractEventView(Presenter presenter, boolean enableLogoNavigation) {
        this.currentPresenter = presenter;
        PlaceNavigation<?> logoNavigation = enableLogoNavigation ? currentPresenter.getEventNavigation() : null;
        this.layout = new AbstractEventViewLayout(currentPresenter.getCtx().getEventDTO(), logoNavigation);
        this.refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        initWidget(uiBinder.createAndBindUi(this.layout));
        layout.viewContentUi.setWidget(getViewContent());
    }
    
    protected abstract Widget getViewContent();

    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaLeaderboards(layout.quickFinderUi, currentPresenter, regattaMetadatas);
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesLeaderboards(layout.quickFinderUi, seriesName, currentPresenter, eventsOfSeries);
    }
    
    @Override
    public void hideQuickfinder() {
        layout.quickFinderUi.removeFromParent();
    }
    
}
