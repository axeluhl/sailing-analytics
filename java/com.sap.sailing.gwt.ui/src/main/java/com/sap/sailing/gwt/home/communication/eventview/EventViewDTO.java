package com.sap.sailing.gwt.home.communication.eventview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.event.HasLogo;
import com.sap.sailing.gwt.ui.shared.TrackingConnectorInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class EventViewDTO extends EventMetadataDTO implements Result, HasLogo, SecuredDTO {

    private static final long serialVersionUID = 3549272772994999483L;

    private TreeSet<RegattaMetadataDTO> regattas = new TreeSet<>();
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    
    private boolean multiRegatta;
    private SeriesReferenceWithEventsDTO seriesData;
    private boolean hasMedia;
    private boolean hasAnalytics;
    private ImageDTO logoImage;
    private String officialWebsiteURL;
    private String sailorsInfoWebsiteURL;
    private String description;
    private String name;
    private List<SpotDTO> allWindFinderSpotIdsUsedByEvent;
    private Set<TrackingConnectorInfoDTO> trackingConnectorInfos;


    public Collection<RegattaMetadataDTO> getRegattas() {
        return regattas;
    }

    public String getVenueCountry() {
        // FIXME: We need a country?
        return "";
    }

    public boolean isHasMedia() {
        return hasMedia;
    }

    public void setHasMedia(boolean hasMedia) {
        this.hasMedia = hasMedia;
    }

    public boolean isHasAnalytics() {
        return hasAnalytics;
    }

    public void setHasAnalytics(boolean hasAnalytics) {
        this.hasAnalytics = hasAnalytics;
    }

    public boolean isRegattaIDKnown(String regattaId) {
        for (RegattaMetadataDTO regatta : regattas) {
            if(regatta.getId().equals(regattaId)) {
                return true;
            }
        }
        return false;
    }

    public void setLogoImage(ImageDTO logoImage) {
        this.logoImage = logoImage;
    }
    
    public ImageDTO getLogoImage() {
        return logoImage;
    }

    public void setOfficialWebsiteURL(String officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
    }
    
    public String getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }

    public String getSailorsInfoWebsiteURL() {
        return sailorsInfoWebsiteURL;
    }

    public void setSailorsInfoWebsiteURL(String sailorsInfoWebsiteURL) {
        this.sailorsInfoWebsiteURL = sailorsInfoWebsiteURL;
    }
    
    /**
     * In addition to the spots from the wind finder spot collections specified by this event explicitly (see
     * {@link #getAllWindFinderReviewedSpotsCollectionIds()}), this method may return additional spots based
     * on the tracked races reachable from this event's associated leaderboard groups and their wind sources. The
     * {@link WindSource#getId() wind source IDs} of all wind sources of type {@link WindSourceType#WINDFINDER} will be
     * collected and the corresponding {@link SpotDTO} objects are then returned.
     */
    public Iterable<SpotDTO> getAllWindFinderSpotIdsUsedByEvent() {
        final Iterable<SpotDTO> result;
        if (allWindFinderSpotIdsUsedByEvent == null) {
            result = Collections.emptySet();
        } else {
            result = allWindFinderSpotIdsUsedByEvent;
        }
        return result;
    }
    
    public void setAllWindFinderSpotsUsedByEvent(Iterable<SpotDTO> windFinderSpots) {
        this.allWindFinderSpotIdsUsedByEvent = new ArrayList<>();
        if (windFinderSpots != null) {
            Util.addAll(windFinderSpots, this.allWindFinderSpotIdsUsedByEvent);
        }
    }

    public String getLocationAndVenueAndCountry() {
        String venue = getLocationAndVenue();
        if(getVenueCountry() != null && !getVenueCountry().isEmpty()) {
            return venue + ", " + getVenueCountry();
        }
        return venue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SeriesReferenceWithEventsDTO getSeriesData() {
        return seriesData;
    }

    public void setSeriesData(SeriesReferenceWithEventsDTO seriesData) {
        this.seriesData = seriesData;
    }
    
    public void setMultiRegatta(boolean multiRegatta) {
        this.multiRegatta = multiRegatta;
    }
    
    public boolean isMultiRegatta() {
        return multiRegatta;
    }

    public Set<TrackingConnectorInfoDTO> getTrackingConnectorInfos() {
        return trackingConnectorInfos;
    }

    public void setTrackingConnectorInfos(Set<TrackingConnectorInfoDTO> trackingConnectorInfo) {
        this.trackingConnectorInfos = trackingConnectorInfo;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.EVENT;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public void setAccessControlList(AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public void setOwnership(OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getId().toString());
    }
}
