package com.sap.sailing.domain.common.dto;

import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

/**
 * Master data about a single race that is to be transferred to the client.<p>
 * 
 * The permission type represented by this class of objects is {@link SecuredDomainType#TRACKED_RACE}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceDTO extends BasicRaceDTO implements SecuredDTO {
    private static final long serialVersionUID = 2613189982608149975L;
    
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    /**
     * Tells if this race is currently being tracked, meaning that a {@link RaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean isTracked;

    public RaceStatusDTO status;

    public PlacemarkOrderDTO places;

    public TrackedRaceStatisticsDTO trackedRaceStatistics;

    public String boatClass;
    
    private RankingMetrics rankingMetricType;
    
    private RegattaAndRaceIdentifier raceIdentifier;
    
    @Deprecated
    public RaceDTO() {}

    public RaceDTO(RegattaAndRaceIdentifier raceIdentifier, TrackedRaceDTO trackedRace, boolean isCurrentlyTracked, RankingMetrics rankingMetricType) {
        super(raceIdentifier, trackedRace);
        this.raceIdentifier = raceIdentifier;
        this.isTracked = isCurrentlyTracked;
        this.rankingMetricType = rankingMetricType;
    }

    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public RankingMetrics getRankingMetricType() {
        return rankingMetricType;
    }
    
    public String getRegattaName() {
        return raceIdentifier.getRegattaName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + (isTracked ? 1231 : 1237);
        result = prime * result + ((places == null) ? 0 : places.hashCode());
        result = prime * result + ((getRegattaName() == null) ? 0 : getRegattaName().hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceDTO other = (RaceDTO) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        if (isTracked != other.isTracked)
            return false;
        if (places == null) {
            if (other.places != null)
                return false;
        } else if (!places.equals(other.places))
            return false;
        if (getRegattaName() == null) {
            if (other.getRegattaName() != null)
                return false;
        } else if (!getRegattaName().equals(other.getRegattaName()))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        return true;
    }
    
    @Override
    public final AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }
    
    @Override
    public HasPermissions getPermissionType() {
        return getPermissionTypeForClass();
    }
    
    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public static HasPermissions getPermissionTypeForClass() {
        return SecuredDomainType.TRACKED_RACE;
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(RegattaAndRaceIdentifier raceIdentifier) {
        return raceIdentifier.getTypeRelativeObjectIdentifier();
    }
    
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(raceIdentifier);
    }

}
