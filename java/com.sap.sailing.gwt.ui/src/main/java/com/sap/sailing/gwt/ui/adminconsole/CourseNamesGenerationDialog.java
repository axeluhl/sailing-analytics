package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class CourseNamesGenerationDialog extends DataEntryDialog<List<String>> {

    private static String[] courseLayouts = new String[] { "0,Windward/Leeward", "1,Windward/Leeward",
            "2,Windward/Leeward", "3,Windward/Leeward", "4,Trapezoid", "5,Trapezoid", "6,Triangle", "7,Triangle",
            "8,Windward/Leeward" };
    
    private IntegerBox minRoundsBox;
    private IntegerBox maxRoundsBox;

    public CourseNamesGenerationDialog(final StringMessages stringMessages,
            DataEntryDialog.DialogCallback<List<String>> callback) {
        super(stringMessages.courseNames(), "Yeah do it!", stringMessages.generate(), stringMessages.cancel(), 
                new NamesValidator(stringMessages), callback);
        minRoundsBox = new IntegerBox();
        minRoundsBox.setValue(2);
        maxRoundsBox = new IntegerBox();
        maxRoundsBox.setValue(3);
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid grid = new Grid(2, 2);

        minRoundsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        minRoundsBox.addKeyPressHandler(numbersOnlyHandler);
        grid.setWidget(0, 0, new Label("Minimum number of rounds:"));
        grid.setWidget(0, 1, minRoundsBox);

        
        maxRoundsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        maxRoundsBox.addKeyPressHandler(numbersOnlyHandler);
        grid.setWidget(1, 0, new Label("Maximum number of rounds:"));
        grid.setWidget(1, 1, maxRoundsBox);

        return grid;
    }
    
    @Override
    public void show() {
        super.show();
        minRoundsBox.setFocus(true);
    }

    @Override
    protected List<String> getResult() {
        List<String> courseNames = new ArrayList<String>();

        Integer minNumberOfRounds = minRoundsBox.getValue();
        Integer maxNumberOfRounds = maxRoundsBox.getValue();
        if (minNumberOfRounds == null || maxNumberOfRounds == null) {
            return courseNames;
        }
        
        for (int layout = 0; layout < courseLayouts.length; layout++) {
            String[] courseNumberAndName = courseLayouts[layout].split(",");
            for (int round = minNumberOfRounds; round <= maxNumberOfRounds; round++) {
                courseNames.add(courseNumberAndName[0] + " " + round + " (" + courseNumberAndName[1] + ")");
            }
        }
        return courseNames;
    }
    
    private KeyPressHandler numbersOnlyHandler = new KeyPressHandler() {
        @Override
        public void onKeyPress(KeyPressEvent event) {
            if(!Character.isDigit(event.getCharCode())) {
                ((IntegerBox)event.getSource()).cancelKey();
            }
        }
    };
    
    private static class NamesValidator implements Validator<List<String>> {
        
        private final StringMessages messages;
        
        public NamesValidator(StringMessages messages) {
            this.messages = messages;
        }
        
        @Override
        public String getErrorMessage(List<String> valueToValidate) {
            if (valueToValidate.isEmpty()) {
                return messages.errorWhileAddingSeriesToChart();
            }
            return null;
        }
    };

}
