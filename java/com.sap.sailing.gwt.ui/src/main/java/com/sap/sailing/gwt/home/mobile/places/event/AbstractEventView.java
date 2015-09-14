package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public abstract class AbstractEventView<P extends EventViewBase.Presenter> extends Composite implements EventViewBase {
    
    private static AbstractEventViewUiBinder uiBinder = GWT.create(AbstractEventViewUiBinder.class);

    interface AbstractEventViewUiBinder extends UiBinder<Widget, AbstractEventViewLayout> {
    }
    
    static class AbstractEventViewLayout {
        @UiField(provided = true) EventHeader eventHeaderUi;
        @UiField Quickfinder quickFinderUi;
        @UiField SimpleInfoBlock simpleInfoUi;
        @UiField SimplePanel viewContentUi;
        
        private AbstractEventViewLayout(EventViewDTO event, String regattaName, PlaceNavigation<?> logoNavigation) {
            this.eventHeaderUi = new EventHeader(event, regattaName, logoNavigation);
        }
    }

    protected final P currentPresenter;
    protected final RefreshManager refreshManager;
    private final AbstractEventViewLayout layout;

    public AbstractEventView(P presenter, boolean showRegattaName, boolean enableLogoNavigation) {
        this.currentPresenter = presenter;
        this.refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        String regattaName = showRegattaName ? currentPresenter.getCtx().getRegatta().getDisplayName() : null;
        PlaceNavigation<?> logoNavigation = enableLogoNavigation ? currentPresenter.getEventNavigation() : null;
        this.layout = new AbstractEventViewLayout(currentPresenter.getCtx().getEventDTO(), regattaName, logoNavigation);
        initWidget(uiBinder.createAndBindUi(this.layout));
    }
    
    protected void setViewContent(Widget contentWidget) {
        layout.viewContentUi.setWidget(contentWidget);
    }
    
    protected UUID getEventId() {
        return currentPresenter.getCtx().getEventDTO().getId();
    }
    
    protected String getRegattaId() {
        return currentPresenter.getCtx().getRegattaId();
    }
    
    protected boolean isMultiRegattaEvent() {
        return currentPresenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA;
    }
    
    protected void setQuickFinderValues(Quickfinder quickfinder, Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaLeaderboards(quickfinder, currentPresenter, regattaMetadatas);
    }
    
    protected void initRacesNavigation(Panel container) {
        MobileSection mobileSection = new MobileSection();
        SectionHeaderContent header = new SectionHeaderContent();
        header.setSectionTitle(StringMessages.INSTANCE.racesCount(currentPresenter.getCtx().getRegatta().getRaceCount()));
        header.setInfoText(StringMessages.INSTANCE.showAll());
        header.setClickAction(currentPresenter.getRegattaRacesNavigation(getRegattaId()));
        mobileSection.addHeader(header);
        container.add(mobileSection);
    }

    @Override
    public final void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        setQuickFinderValues(layout.quickFinderUi, regattaMetadatas);
    }
    
    @Override
    public final void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesLeaderboards(layout.quickFinderUi, seriesName, currentPresenter, eventsOfSeries);
    }
    
    @Override
    public final void hideQuickfinder() {
        layout.quickFinderUi.removeFromParent();
    }
    
    @Override
    public void setSailorInfos(String description, String buttonLabel, String url) {
        layout.simpleInfoUi.setDescription(SafeHtmlUtils.fromString(description.replace("\n", " ")));
        layout.simpleInfoUi.setAction(buttonLabel, url);
    }
    
    @Override
    public void setSeriesNavigation(String buttonLabel, PlaceNavigation<?> placeNavigation) {
        layout.simpleInfoUi.setAction(buttonLabel, placeNavigation);
    }
    
}
