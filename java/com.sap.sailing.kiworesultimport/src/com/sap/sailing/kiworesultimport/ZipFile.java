package com.sap.sailing.kiworesultimport;

public interface ZipFile {
    Iterable<StartReport> getStartReports();
    
    Iterable<ResultList> getResultLists();

    RegattaSummary getRegattaSummary(String boatClassName);
    
    Iterable<String> getBoatClassNames();
    
    Iterable<RegattaSummary> getRegattaSummaries();
}
