package com.sap.sailing.gwt.ui.test;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.shared.panels.AbstractFilterablePanel;

public class AbstrectFilterablePanelTest {
    AbstractFilterablePanel<String> panel;
    ArrayList<String> all;
    ListDataProvider<String> String;
    CellTable<String> table;
    

    @Before
    public void setUp() throws Exception {
        all = new ArrayList<String>();
        all.add("Happy Birthday");
        all.add("Bla Bla Bla");
        table = new CellTable<String>();
        
    }

    @Test
    public void test() {

    
        
    }
}
