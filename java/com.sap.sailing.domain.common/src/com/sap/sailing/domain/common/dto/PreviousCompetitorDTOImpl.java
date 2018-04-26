package com.sap.sailing.domain.common.dto;

import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Equality and hash code are based on the index pointing into a previous leaderboard's competitors list.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class PreviousCompetitorDTOImpl implements CompetitorDTO {
    private static final long serialVersionUID = 8820028699103040805L;
    private int indexInPreviousCompetitorList;

    @Deprecated
    PreviousCompetitorDTOImpl() {} // for serialization only
    
    public PreviousCompetitorDTOImpl(int indexInPreviousCompetitorList) {
        super();
        this.indexInPreviousCompetitorList = indexInPreviousCompetitorList;
    }
    
    protected int getIndexInPreviousCompetitorList() {
        return indexInPreviousCompetitorList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + indexInPreviousCompetitorList;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PreviousCompetitorDTOImpl other = (PreviousCompetitorDTOImpl) obj;
        if (indexInPreviousCompetitorList != other.indexInPreviousCompetitorList)
            return false;
        return true;
    }
    
    @Override
    public String getShortInfo() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getTwoLetterIsoCountryCode() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getThreeLetterIocCountryCode() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getCountryName() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getShortName() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public Color getColor() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getIdAsString() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getName() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }
    
    @Override
    public String getEmail() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getSearchTag() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public void addToSearchTag(String searchTag) {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getImageURL() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getFlagImageURL() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public boolean hasEmail() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public Double getTimeOnTimeFactor() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return previousVersion.competitors.get(indexInPreviousCompetitorList);
    }

    @Override
    public boolean hasBoat() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }
}