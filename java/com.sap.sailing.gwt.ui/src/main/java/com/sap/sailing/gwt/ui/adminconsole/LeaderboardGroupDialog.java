package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class LeaderboardGroupDialog extends DataEntryDialog<LeaderboardGroupDTO> {
    
    protected StringMessages stringMessages;
    protected LeaderboardGroupDTO group;
    
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    
    private CheckBox useOverallLeaderboardCheckBox;
    private Panel overallLeaderboardConfigPanel;
    private LongBox[] overallLeaderboardDiscardThresholdFields;
    private ListBox overallLeaderboardScoringSchemeListBox;
    
    protected static class LeaderboardGroupParameterValidator implements Validator<LeaderboardGroupDTO> {
        private final StringMessages stringMessages;
        private final ArrayList<LeaderboardGroupDTO> existingGroups;
        
        public LeaderboardGroupParameterValidator(StringMessages stringMessages,
                Collection<LeaderboardGroupDTO> existingGroups) {
            this.stringMessages = stringMessages;
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
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (!descrNotEmpty) {
                errorMessage = stringMessages.pleaseEnterNonEmptyDescription();
            } else if (!unique) {
                errorMessage = stringMessages.groupWithThisNameAlreadyExists();
            } else if (groupToValidate.getOverallLeaderboardScoringSchemeType() != null) {
                final List<StrippedLeaderboardDTO> emptyStrippedLeaderboardDTOList = Collections.emptyList();
                errorMessage = new FlexibleLeaderboardDialog.LeaderboardParameterValidator(stringMessages,
                        emptyStrippedLeaderboardDTOList).getErrorMessage(new LeaderboardDescriptor("Overall",
                                groupToValidate.getOverallLeaderboardScoringSchemeType(),
                                groupToValidate.getOverallLeaderboardDiscardThresholds()));
                ;
            }
            return errorMessage;
        }
    }

    protected ListBox getOverallLeaderboardScoringSchemeListBox() {
        return overallLeaderboardScoringSchemeListBox;
    }

    protected LongBox[] getOverallLeaderboardDiscardThresholdFields() {
        return overallLeaderboardDiscardThresholdFields;
    }
    
    protected CheckBox getUseOverallLeaderboardCheckBox() {
        return useOverallLeaderboardCheckBox;
    }

    public LeaderboardGroupDialog(LeaderboardGroupDTO group, StringMessages stringMessages,
            DialogCallback<LeaderboardGroupDTO> callback, Collection<LeaderboardGroupDTO> existingLeaderboardGroups) {
        super(stringMessages.leaderboardGroup(), null, stringMessages.ok(), stringMessages.cancel(),
                new LeaderboardGroupParameterValidator(stringMessages, existingLeaderboardGroups), callback);
        this.stringMessages = stringMessages;
        this.group = group;
        useOverallLeaderboardCheckBox = createCheckbox(stringMessages.useOverallLeaderboard());
        Grid formGrid = new Grid(3,2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, new Label(stringMessages.scoringSystem() + ":"));
        overallLeaderboardScoringSchemeListBox = AbstractLeaderboardDialog.createScoringSchemeListBox(this, stringMessages);
        if (group.getOverallLeaderboardScoringSchemeType() != null) {
            overallLeaderboardScoringSchemeListBox.setSelectedIndex(Arrays.asList(ScoringSchemeType.values()).indexOf(
                    group.getOverallLeaderboardScoringSchemeType()));
        }
        formGrid.setWidget(0, 1, overallLeaderboardScoringSchemeListBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(3);
        if (group.hasOverallLeaderboard()) {
            overallLeaderboardDiscardThresholdFields = AbstractLeaderboardDialog.initPrefilledDiscardThresholdBoxes(
                    group.getOverallLeaderboardDiscardThresholds(), this);
        } else {
            overallLeaderboardDiscardThresholdFields = AbstractLeaderboardDialog.initEmptyDiscardThresholdBoxes(this);
        }
        for (int i = 0; i < overallLeaderboardDiscardThresholdFields.length; i++) {
            hp.add(new Label("" + (i + 1) + "."));
            hp.add(overallLeaderboardDiscardThresholdFields[i]);
        }
        formGrid.setWidget(1, 1, hp);
        overallLeaderboardConfigPanel = formGrid;
    }

    @Override
    protected LeaderboardGroupDTO getResult() {
        group.name = nameEntryField.getText();
        group.description = descriptionEntryField.getText();
        if (useOverallLeaderboardCheckBox.getValue()) {
            group.setOverallLeaderboardDiscardThresholds(AbstractLeaderboardDialog
                    .getDiscardThresholds(getOverallLeaderboardDiscardThresholdFields()));
            group.setOverallLeaderboardScoringSchemeType(AbstractLeaderboardDialog.getSelectedScoringSchemeType(
                    overallLeaderboardScoringSchemeListBox, stringMessages));
        } else {
            group.setOverallLeaderboardDiscardThresholds(null);
            group.setOverallLeaderboardScoringSchemeType(null);
        }
        return group;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(new Label(stringMessages.name()));
        panel.add(nameEntryField);
        panel.add(new Label(stringMessages.description()));
        descriptionEntryField.setCharacterWidth(30);
        descriptionEntryField.setVisibleLines(6);
        descriptionEntryField.getElement().getStyle().setProperty("resize", "none");
        panel.add(descriptionEntryField);
        panel.add(useOverallLeaderboardCheckBox);
        useOverallLeaderboardCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    useOverallLeaderboard(panel);
                } else {
                    dontUseOverallLeaderboard(panel);
                }
            }

        });
        return panel;
    }

    protected void useOverallLeaderboard(final VerticalPanel panel) {
        panel.add(overallLeaderboardConfigPanel);
    }
    
    protected void dontUseOverallLeaderboard(final VerticalPanel panel) {
        panel.remove(overallLeaderboardConfigPanel);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
