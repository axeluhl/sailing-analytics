package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class ChooseNameDenoteEventDialog extends AbstractChooseNameDenoteEventDialog<String> {
    private final RadioButton defaultName;
    private final RadioButton ownPrefix;
    private final TextBox name;
    private final Label example;
    private final StrippedLeaderboardDTO strippedLeaderboardDTO;
    
    public ChooseNameDenoteEventDialog(StringMessages stringMessages,StrippedLeaderboardDTO leaderboard,DialogCallback<String> callback){
        super(stringMessages.chooseAName(), stringMessages, null, callback);
        final String NAME_TYPE_RADIO_BUTTON_GROUP = "namegroup";
        this.strippedLeaderboardDTO=leaderboard;
        this.defaultName=createRadioButton(NAME_TYPE_RADIO_BUTTON_GROUP, stringMessages.defaultName());
        this.defaultName.setValue(true);
        this.ownPrefix=createRadioButton(NAME_TYPE_RADIO_BUTTON_GROUP, stringMessages.ownPrefix());
        this.name=createTextBox("R", 5);
        this.name.setEnabled(false);
        this.example=new Label();
        this.updateExample();
        this.ownPrefix.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                defaultName.setValue(false);
                name.setEnabled(true);
                updateExample();
            }
        });
        this.defaultName.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                name.setEnabled(false);
                ownPrefix.setValue(false);
                updateExample();
            }
        });
        this.name.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
              updateExample();
            }
        });
        this.getOkButton().setFocus(true);
    }

    protected void updateExample() {
        if (this.defaultName.getValue()) {
            this.example.setText(stringMessages.exampleTextForName() + " " + strippedLeaderboardDTO.getName() + " "
                    + strippedLeaderboardDTO.getRaceList().get(0).getName() + " "
                    + strippedLeaderboardDTO.getRaceList().get(0).getFleets().get(0).getName());
        } else if (this.ownPrefix.getValue()) {
            this.example.setText(stringMessages.exampleTextForName() + " " + name.getValue() + 1);
        }
    }
    
    @Override
    protected Widget getAdditionalWidget(){
        final VerticalPanel panel = new VerticalPanel();
        Grid formGrid = new Grid(3,2);
        formGrid.setWidget(0, 0, defaultName);
        formGrid.setWidget(1, 0, ownPrefix);
        formGrid.setWidget(1, 1, name);
        formGrid.setWidget(2, 0, example);
        panel.add(formGrid);
        return panel;
    }
    
    @Override
    protected String getResult() {
        final String result;
        if (this.ownPrefix.getValue()) {
            result = name.getValue();
        } else {
            result = null;
        }
        return result;
    }
}
