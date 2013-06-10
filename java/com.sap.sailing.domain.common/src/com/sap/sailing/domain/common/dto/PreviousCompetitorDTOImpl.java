package com.sap.sailing.domain.common.dto;

public class PreviousCompetitorDTOImpl implements CompetitorDTO {
    private static final long serialVersionUID = 8820028699103040805L;
    private int indexInPreviousCompetitorList;

    PreviousCompetitorDTOImpl() {} // for serialization only
    
    public PreviousCompetitorDTOImpl(int indexInPreviousCompetitorList) {
        super();
        this.indexInPreviousCompetitorList = indexInPreviousCompetitorList;
    }
    
    @Override
    public String getTwoLetterIsoCountryCode() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getThreeLetterIocCountryCode() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getCountryName() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getSailID() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getIdAsString() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public BoatClassDTO getBoatClass() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public String getName() {
        throw new RuntimeException("Internal error. Objects of type "+PreviousCompetitorDTOImpl.class.getName()+
                " need to be replaced by an object of "+CompetitorDTOImpl.class.getName()+" after deserialization");
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return previousVersion.competitors.get(indexInPreviousCompetitorList);
    }
}