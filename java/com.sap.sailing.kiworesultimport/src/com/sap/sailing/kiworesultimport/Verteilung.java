package com.sap.sailing.kiworesultimport;

public interface Verteilung {
    Iterable<Boat> getBoats();
    
    /**
     * Matches <code>sailID</code> with {@link Boat#getSailingNumber()}
     * 
     * @return <code>null</code> if no such {@link Boat} is found in {@link #getBoats}, or the boat found otherwise.
     */
    Boat getBoatBySailID(String sailID);
}
