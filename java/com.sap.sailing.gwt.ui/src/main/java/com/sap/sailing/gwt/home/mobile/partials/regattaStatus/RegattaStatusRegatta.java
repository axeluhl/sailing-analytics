package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.client.BoatClassImageResources;
import com.sap.sailing.gwt.home.mobile.partials.section.IsMobileSection;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class RegattaStatusRegatta extends Composite implements IsMobileSection {

    private static RegattaStatusRegattaUiBinder uiBinder = GWT.create(RegattaStatusRegattaUiBinder.class);

    interface RegattaStatusRegattaUiBinder extends UiBinder<Widget, RegattaStatusRegatta> {
    }
    
    @UiField MobileSection itemContainerUi;
    @UiField SectionHeaderContent headerUi;

    public RegattaStatusRegatta(RegattaMetadataDTO regatta, PlaceNavigation<?> placeNavigation) {
        initWidget(uiBinder.createAndBindUi(this));
        initRegattaHeader(regatta, placeNavigation);
    }
    
    @Override
    public MobileSection getMobileSection() {
        return itemContainerUi;
    }
    
    public void addRaces(Set<LiveRaceDTO> races) {
        for (LiveRaceDTO race : races) {
            itemContainerUi.addContent(new RegattaStatusRace(race));
        }
        headerUi.setLabelType(LabelType.LIVE);
    }

    private void initRegattaHeader(RegattaMetadataDTO regatta, final PlaceNavigation<?> placeNavigation) {
        headerUi.setImageUrl(getBootClassIcon(regatta.getBoatClass()).getSafeUri().asString());
        headerUi.setSectionTitle(regatta.getDisplayName());
        headerUi.setClickAction(placeNavigation);
    }
    
    private ImageResource getBootClassIcon(String bootClass) {
        if (bootClass != null) {
            ImageResource image = BoatClassImageResolver.getBoatClassIconResource(bootClass);
            if (image != null) {
                return image;
            }
        }
        return BoatClassImageResources.INSTANCE.genericBoatClass();
    }

}
