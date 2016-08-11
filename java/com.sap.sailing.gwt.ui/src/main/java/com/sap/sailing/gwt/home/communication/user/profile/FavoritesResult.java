package com.sap.sailing.gwt.home.communication.user.profile;

import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class FavoritesResult implements Result {
    
    private FavoriteBoatClassesDTO favoriteBoatClasses;
    private FavoriteCompetitorsDTO favoriteCompetitors;
    
    protected FavoritesResult() {}
    
    public FavoritesResult(FavoriteBoatClassesDTO favoriteBoatClasses, FavoriteCompetitorsDTO favoriteCompetitors) {
        this.favoriteBoatClasses = favoriteBoatClasses;
        this.favoriteCompetitors = favoriteCompetitors;
    }
    
    public FavoriteBoatClassesDTO getFavoriteBoatClasses() {
        return favoriteBoatClasses;
    }
    
    public FavoriteCompetitorsDTO getFavoriteCompetitors() {
        return favoriteCompetitors;
    }

}
