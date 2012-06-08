package com.sap.sailing.kiworesultimport;

public interface ZipFile {
    Iterable<Startbericht> getStartberichte();
    
    Iterable<ResultList> getResultLists();
}
