package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupDialog extends DataEntryDialog<LeaderboardGroupDTO> {
    
    protected StringMessages stringConstants;
    protected LeaderboardGroupDTO group;
    
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    
    protected static class LeaderboardGroupParameterValidator implements Validator<LeaderboardGroupDTO> {
        
        private StringMessages stringConstants;
        private ArrayList<LeaderboardGroupDTO> existingGroups;
        
        public LeaderboardGroupParameterValidator(StringMessages stringConstants,
                Collection<LeaderboardGroupDTO> existingGroups) {
            this.stringConstants = stringConstants;
            this.existingGroups = new ArrayList<LeaderboardGroupDTO>(existingGroups);
        }

        @Override
        public String getErrorMessage(LeaderboardGroupDTO groupToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = groupToValidate.name != null && groupToValidate.name.length() > 0;
            boolean descrNotEmpty = groupToValidate.description != null && groupToValidate.description.length() > 0;
            
            boolean unique = true;
            for (LeaderboardGroupDTO group : existingGroups) {
                if (group.name.equals(groupToValidate.name)) {
                    unique = false;
                    break;
                }
            }
            
            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!descrNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyDescription();
            } else if (!unique) {
                errorMessage = stringConstants.groupWithThisNameAlreadyExists();
            }
            
            return errorMessage;
        }
        
    }
    
    public LeaderboardGroupDialog(LeaderboardGroupDTO group, LeaderboardGroupParameterValidator validator,
            StringMessages stringConstants, AsyncCallback<LeaderboardGroupDTO> callback) {
        super(stringConstants.leaderboardGroup(), "", stringConstants.ok(), stringConstants.cancel(), validator, callback);
        this.stringConstants = stringConstants;
        this.group = group;
    }

    @Override
    protected LeaderboardGroupDTO getResult() {
        group.name = nameEntryField.getText();
        group.description = descriptionEntryField.getText();
        return group;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(new Label(stringConstants.name()));
        panel.add(nameEntryField);
        panel.add(new Label(stringConstants.description()));
        descriptionEntryField.setCharacterWidth(30);
        descriptionEntryField.setVisibleLines(6);
        descriptionEntryField.getElement().getStyle().setProperty("resize", "none");
        panel.add(descriptionEntryField);
        return panel;
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
