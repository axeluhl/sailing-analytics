package com.sap.sailing.domain.common.dto;

public class PreviousCompetitorWithBoatDTOImpl extends PreviousCompetitorDTOImpl implements CompetitorWithBoatDTO {
    private static final long serialVersionUID = 3728470772065966386L;

    @Deprecated
    PreviousCompetitorWithBoatDTOImpl() {} // for GWT serialization only
    
    public PreviousCompetitorWithBoatDTOImpl(int indexInPreviousCompetitorList) {
        super(indexInPreviousCompetitorList);
    }
    
    @Override
    public String getShortInfo() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }


    @Override
    public String getSailID() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public BoatDTO getBoat() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public void setBoat(BoatDTO boat) {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public BoatClassDTO getBoatClass() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorWithBoatDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public CompetitorWithBoatDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return (CompetitorWithBoatDTO) super.getCompetitorFromPrevious(previousVersion);
    }

}
