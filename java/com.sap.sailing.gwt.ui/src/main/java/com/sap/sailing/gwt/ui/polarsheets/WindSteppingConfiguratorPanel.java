package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.PolarSheetsWindStepping;

public class WindSteppingConfiguratorPanel extends HorizontalPanel {
    
    private List<TextBox> textBoxes = new ArrayList<TextBox>();

    public WindSteppingConfiguratorPanel(PolarSheetsWindStepping windStepping) {
        Integer[] levels = windStepping.getRawStepping();
        for (int i = 0; i < levels.length; i++) {
            TextBox textBox = new TextBox();
            textBox.setMaxLength(2);
            textBox.setVisibleLength(2);
            textBox.setText(Integer.toString(levels[i]));
            textBoxes.add(textBox);
        }
        updateTextBoxes();
    }

    private void updateTextBoxes() {
        this.clear();
        for (Widget widget : textBoxes) {
            this.add(widget);
        }
    }

    public PolarSheetsWindStepping getStepping() {
        List<Integer> levelList = new ArrayList<Integer>(); 
        for (TextBox box : textBoxes) {
            levelList.add(Integer.parseInt(box.getValue()));
        }
        Collections.sort(levelList);
        Integer[] levels = levelList.toArray(new Integer[levelList.size()]);
        return new PolarSheetsWindStepping(levels);
    }
    
    

}
