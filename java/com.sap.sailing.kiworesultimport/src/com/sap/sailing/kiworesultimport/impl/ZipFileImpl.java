package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.Startbericht;
import com.sap.sailing.kiworesultimport.ZipFile;

public class ZipFileImpl implements ZipFile {
    private final Iterable<Startbericht> startberichte;
    private final Map<Pair<String, Integer>, Start> startsByBoatclassAndWettfahrt;
    private final List<ResultList> resultLists;
    
    public ZipFileImpl(Iterable<Startbericht> startberichte, Iterable<ResultList> resultLists) {
        this.startberichte = new ArrayList<Startbericht>();
        this.startsByBoatclassAndWettfahrt = new HashMap<Pair<String,Integer>, Start>();
        this.resultLists = new ArrayList<ResultList>();
        for (Startbericht startbericht : startberichte) {
            for (Start start : startbericht.getStarts()) {
                startsByBoatclassAndWettfahrt.put(new Pair<String, Integer>(start.getBootsklasse(), start.getWettfahrt()), start);
            }
        }
    }
    
    @Override
    public Iterable<Startbericht> getStartberichte() {
        return startberichte;
    }

    @Override
    public Iterable<ResultList> getResultLists() {
        return resultLists;
    }

}
