package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSpeedStepping;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;

/**
 * A Panel which allows the user to configure a {@link WindSpeedStepping}.
 * 
 * @author d054528 Frederik Petersen
 *
 */
public class WindSteppingConfiguratorPanel extends HorizontalPanel {
    
    private List<TextBox> textBoxes = new ArrayList<TextBox>();
    private Button minusButton;
    private Button plusButton;

    public WindSteppingConfiguratorPanel(WindSteppingWithMaxDistance windStepping) {
        setupPlusAndMinusButtons();
        double[] levels = windStepping.getRawStepping();
        for (int i = 0; i < levels.length; i++) {
            TextBox textBox = createSingleBox((int) Math.round(levels[i]));
            textBoxes.add(textBox);
        }
        updateTextBoxes();
    }

    private void setupPlusAndMinusButtons() {
        minusButton = new Button("-");
        minusButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                textBoxes.remove(textBoxes.size() - 1);
                updateTextBoxes();
            }
        });
        plusButton = new Button("+");
        plusButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                TextBox newBox = createSingleBox(Integer.parseInt(textBoxes.get(textBoxes.size() - 1).getText()) + 2);
                textBoxes.add(newBox);
                updateTextBoxes();
            }
        });
    }

    private void updateTextBoxes() {
        this.clear();
        for (Widget widget : textBoxes) {
            this.add(widget);
        }
        if (textBoxes.size() > 1) {
            this.add(minusButton);
        }
        this.add(plusButton);
    }

    public WindSteppingWithMaxDistance getStepping(double maxDistance) {
        List<Double> levelList = new ArrayList<Double>(); 
        for (TextBox box : textBoxes) {
            levelList.add(new Double(Integer.parseInt(box.getValue())));
        }
        Collections.sort(levelList);
        double[] levels = new double[levelList.size()];
        int i=0;
        for (Double level : levelList) {
            levels[i++] = level;
        }
        return new WindSteppingWithMaxDistance(levels, maxDistance);
    }
    
    private TextBox createSingleBox(int level) {
        TextBox textBox = new TextBox();
        textBox.setMaxLength(2);
        textBox.setVisibleLength(2);
        textBox.setText(Integer.toString(level));
        return textBox;
    }
    
    

}
