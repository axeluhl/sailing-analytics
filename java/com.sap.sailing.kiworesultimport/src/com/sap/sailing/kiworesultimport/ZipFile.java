package com.sap.sailing.kiworesultimport;

public interface ZipFile {
    Iterable<StartReport> getStartReports();
    
    Iterable<ResultList> getResultLists();
}
