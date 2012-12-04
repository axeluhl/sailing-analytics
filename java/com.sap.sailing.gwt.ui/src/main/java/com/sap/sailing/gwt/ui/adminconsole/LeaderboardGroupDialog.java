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

public class LeaderboardGroupDialog extends DataEntryDialog<LeaderboardGroupDialog.LeaderboardGroupDescriptor> {
    protected StringMessages stringMessages;
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    protected CheckBox displayLeaderboardsInReverseOrderCheckBox;
    protected CheckBox useOverallLeaderboardCheckBox;
    private Panel overallLeaderboardConfigPanel;
    private LongBox[] overallLeaderboardDiscardThresholdFields;
    private ListBox overallLeaderboardScoringSchemeListBox;
    
    public static class LeaderboardGroupDescriptor {
        private final String name;
        private final String description;
        private final boolean displayLeaderboardsInReverseOrder;
        private final boolean useOverallLeaderboard;
        private final int[] overallLeaderboardDiscardThresholds;
        private final ScoringSchemeType overallLeaderboardScoringSchemeType;
        public LeaderboardGroupDescriptor(String name, String description, boolean displayLeaderboardsInReverseOrder,
                boolean useOverallLeaderboard, int[] overallLeaderboardDiscardThresholds,
                ScoringSchemeType overallLeaderboardScoringSchemeType) {
            super();
            this.name = name;
            this.description = description;
            this.displayLeaderboardsInReverseOrder = displayLeaderboardsInReverseOrder;
            this.useOverallLeaderboard = useOverallLeaderboard;
            this.overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholds;
            this.overallLeaderboardScoringSchemeType = overallLeaderboardScoringSchemeType;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
        public boolean isDisplayLeaderboardsInReverseOrder() {
            return displayLeaderboardsInReverseOrder;
        }
        public boolean isUseOverallLeaderboard() {
            return useOverallLeaderboard;
        }
        public int[] getOverallLeaderboardDiscardThresholds() {
            return overallLeaderboardDiscardThresholds;
        }
        public ScoringSchemeType getOverallLeaderboardScoringSchemeType() {
            return overallLeaderboardScoringSchemeType;
        }
    }
    
    protected static class LeaderboardGroupParameterValidator implements Validator<LeaderboardGroupDescriptor> {
        private final StringMessages stringMessages;
        private final ArrayList<LeaderboardGroupDTO> existingGroups;
        
        public LeaderboardGroupParameterValidator(StringMessages stringMessages,
                Collection<LeaderboardGroupDTO> existingGroups) {
            this.stringMessages = stringMessages;
            this.existingGroups = new ArrayList<LeaderboardGroupDTO>(existingGroups);
        }

        @Override
        public String getErrorMessage(LeaderboardGroupDescriptor groupToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = groupToValidate.getName() != null && groupToValidate.getName().length() > 0;
            boolean descrNotEmpty = groupToValidate.getDescription() != null && groupToValidate.getDescription().length() > 0;
            
            boolean unique = true;
            for (LeaderboardGroupDTO group : existingGroups) {
                if (group.name.equals(groupToValidate.getName())) {
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

    public LeaderboardGroupDialog(LeaderboardGroupDTO group, StringMessages stringMessages,
            DialogCallback<LeaderboardGroupDescriptor> callback, Collection<LeaderboardGroupDTO> existingLeaderboardGroups) {
        super(stringMessages.leaderboardGroup(), null, stringMessages.ok(), stringMessages.cancel(),
                new LeaderboardGroupParameterValidator(stringMessages, existingLeaderboardGroups), callback);
        this.stringMessages = stringMessages;
        displayLeaderboardsInReverseOrderCheckBox = createCheckbox(stringMessages.displayGroupsInReverseOrder());
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
    protected LeaderboardGroupDescriptor getResult() {
        return new LeaderboardGroupDescriptor(nameEntryField.getText(), descriptionEntryField.getText(),
                displayLeaderboardsInReverseOrderCheckBox.getValue(),
                useOverallLeaderboardCheckBox.getValue(),
                useOverallLeaderboardCheckBox.getValue() ? AbstractLeaderboardDialog
                        .getDiscardThresholds(getOverallLeaderboardDiscardThresholdFields()) : null,
                        useOverallLeaderboardCheckBox.getValue() ? AbstractLeaderboardDialog.getSelectedScoringSchemeType(
                                overallLeaderboardScoringSchemeListBox, stringMessages) : null);
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
        panel.add(displayLeaderboardsInReverseOrderCheckBox);
        panel.add(useOverallLeaderboardCheckBox);
        useOverallLeaderboardCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateOverallLeaderboardDetailsVisibility(event.getValue(), panel);
            }
        });
        updateOverallLeaderboardDetailsVisibility(useOverallLeaderboardCheckBox.getValue(), panel);
        return panel;
    }

    private void updateOverallLeaderboardDetailsVisibility(boolean overallLeaderboardDetailsVisible, Panel panel) {
        if (overallLeaderboardDetailsVisible) {
            useOverallLeaderboard(panel);
        } else {
            dontUseOverallLeaderboard(panel);
        }
    }

    protected void useOverallLeaderboard(final Panel panel) {
        panel.add(overallLeaderboardConfigPanel);
    }
    
    protected void dontUseOverallLeaderboard(final Panel panel) {
        panel.remove(overallLeaderboardConfigPanel);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
