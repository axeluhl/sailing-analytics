package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaConfigurationDialog extends DataEntryDialog<DeviceConfigurationDTO.RegattaConfigurationDTO> {

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    private final DeviceConfigurationDTO.RegattaConfigurationDTO originalConfiguration;

    private VerticalPanel contentPanel;

    private ListBox racingProcedureListBox;
    private ListBox designerModeEntryListBox;
    private IntegerBox protestTimeInMinutesTextBox;

    private DisclosurePanel rrs26DisclosurePanel;
    private CheckBox rrs26EnabledBox;
    private ListBox rrs26ClassFlagListBox;
    private CheckBox rrs26RecallBox;
    private CheckBox rrs26ResultEntryBox;
    private ListBox rrs26StartModeFlagsBox;

    private DisclosurePanel swcStartDisclosurePanel;
    private CheckBox swcStartEnabledBox;
    private ListBox swcStartClassFlagListBox;
    private CheckBox swcStartRecallBox;
    private CheckBox swcStartResultEntryBox;
    private ListBox swcStartModeFlagsBox;

    private DisclosurePanel gateStartDisclosurePanel;
    private CheckBox gateStartEnabledBox;
    private ListBox gateStartClassFlagListBox;
    private CheckBox gateStartRecallBox;
    private CheckBox gateStartResultEntryBox;
    private CheckBox gateStartPathfinderBox;
    private CheckBox gateStartGolfDownBox;

    private DisclosurePanel essDisclosurePanel;
    private CheckBox essEnabledBox;
    private ListBox essClassFlagListBox;
    private CheckBox essRecallBox;
    private CheckBox essResultEntryBox;

    private DisclosurePanel basicDisclosurePanel;
    private CheckBox basicEnabledBox;
    private ListBox basicClassFlagListBox;
    private CheckBox basicRecallBox;
    private CheckBox basicResultEntryBox;

    private DisclosurePanel leagueDisclosurePanel;
    private CheckBox leagueEnabledBox;
    private ListBox leagueClassFlagListBox;
    private CheckBox leagueRecallBox;
    private CheckBox leagueResultEntryBox;

    public RegattaConfigurationDialog(DeviceConfigurationDTO.RegattaConfigurationDTO regattaConfiguration,
            StringMessages messages, DataEntryDialog.DialogCallback<RegattaConfigurationDTO> callback) {
        super(messages.racingProcedureConfiguration(), "", messages.save(), messages.cancel(), /* validator */ null, callback);
        this.stringMessages = messages;
        this.originalConfiguration = regattaConfiguration;
    }

    @Override
    protected Widget getAdditionalWidget() {
        contentPanel = new VerticalPanel();
        setupGeneral();
        setupRRS26();
        setupSWCStart();
        setupGateStart();
        setupESS();
        setupBasic();
        setupLeague();
        
        if (rrs26EnabledBox.getValue()) {
            rrs26DisclosurePanel.setOpen(true);
        }
        if (swcStartEnabledBox.getValue()) {
            swcStartDisclosurePanel.setOpen(true);
        }
        if (gateStartEnabledBox.getValue()) {
            gateStartDisclosurePanel.setOpen(true);
        }
        if (essEnabledBox.getValue()) {
            essDisclosurePanel.setOpen(true);
        }
        if (basicEnabledBox.getValue()) {
            basicDisclosurePanel.setOpen(true);
        }
        if (leagueEnabledBox.getValue()) {
            leagueDisclosurePanel.setOpen(true);
        }

        return contentPanel;
    }

    private void setupGeneral() {
        Grid grid = new Grid(3, 2);
        setupCourseDesignerListBox(grid, 0);
        setupRacingProcedureListBox(grid, 1);
        setupProtestTimeTextBox(grid, 2);
        contentPanel.add(grid);
    }

    private void setupRacingProcedureListBox(Grid grid, int gridRow) {
        racingProcedureListBox = new ListBox();
        racingProcedureListBox.setMultipleSelect(false);

        racingProcedureListBox.setWidth("100%");
        ListBoxUtils.setupRacingProcedureTypeListBox(racingProcedureListBox,
                originalConfiguration.defaultRacingProcedureType, stringMessages.dontoverwrite());
        racingProcedureListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
        grid.setWidget(gridRow, 0, new Label(stringMessages.racingProcedure()));
        grid.setWidget(gridRow, 1, racingProcedureListBox);
    }

    private void setupCourseDesignerListBox(Grid grid, int gridRow) {
        designerModeEntryListBox = new ListBox();
        designerModeEntryListBox.setMultipleSelect(false);
        designerModeEntryListBox.setWidth("100%");
        ListBoxUtils.setupCourseDesignerModeListBox(designerModeEntryListBox,
                originalConfiguration.defaultCourseDesignerMode, stringMessages.dontoverwrite());
        designerModeEntryListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
        grid.setWidget(gridRow, 0, new Label(stringMessages.courseDesignerMode()));
        grid.setWidget(gridRow, 1, designerModeEntryListBox);
    }

    private void setupProtestTimeTextBox(Grid grid, int gridRow) {
        final Integer protestTimeDurationInMinutes = originalConfiguration.defaultProtestTimeDuration == null ? null :
            (int) originalConfiguration.defaultProtestTimeDuration.asMinutes();
        protestTimeInMinutesTextBox = createIntegerBox(protestTimeDurationInMinutes, 3);
        grid.setWidget(gridRow, 0, new Label(stringMessages.protestTime()));
        grid.setWidget(gridRow, 1, protestTimeInMinutesTextBox);
    }

    private ListBox setupClassFlagListBox(RacingProcedureConfigurationDTO config) {
        ListBox box = createListBox(false);
        Flags selectedFlag = null;
        if (config != null) {
            selectedFlag = config.classFlag;
        }
        ListBoxUtils.setupFlagsListBox(box, selectedFlag, stringMessages.dontoverwrite());
        box.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
        return box;
    }

    private CheckBox setupRecallBox(RacingProcedureConfigurationDTO config) {
        CheckBox box = new CheckBox(stringMessages.activateIndividualRecall());
        if (config != null) {
            box.setValue(config.hasIndividualRecall);
        }
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
            }
        });
        return box;
    }

    private CheckBox setupResultEntryBox(RacingProcedureConfigurationDTO config) {
        CheckBox box = new CheckBox(stringMessages.activateResultEntry());
        if (config != null) {
            box.setValue(config.isResultEntryEnabled);
        }
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
            }
        });
        return box;
    }

    private void setupRRS26() {
        rrs26DisclosurePanel = new DisclosurePanel(stringMessages.rrs26Start());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(4, 3);
        rrs26EnabledBox = new CheckBox(stringMessages.setConfiguration());
        rrs26EnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isActive = event.getValue();
                rrs26ClassFlagListBox.setEnabled(isActive);
                rrs26RecallBox.setEnabled(isActive);
                rrs26ResultEntryBox.setEnabled(isActive);
                rrs26StartModeFlagsBox.setEnabled(isActive);
            }
        });
        rrs26ClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.rrs26Configuration);
        rrs26ClassFlagListBox.setWidth("100%");
        rrs26RecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.rrs26Configuration);
        rrs26ResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.rrs26Configuration);
        setupRRS26StartModeFlags();

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, rrs26ClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("RRS26")));
        grid.setWidget(1, 0, rrs26RecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, rrs26ResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));
        grid.setWidget(3, 0, new Label(stringMessages.startmodeFlags() + ":"));
        grid.setWidget(3, 1, rrs26StartModeFlagsBox);
        grid.setWidget(3, 2, createHelpImage(stringMessages.startmodeFlagsHelpText()));

        rrs26EnabledBox.setValue(originalConfiguration != null && originalConfiguration.rrs26Configuration != null);
        ValueChangeEvent.fire(rrs26EnabledBox, rrs26EnabledBox.getValue());

        panel.add(rrs26EnabledBox);
        panel.add(grid);
        rrs26DisclosurePanel.add(panel);
        contentPanel.add(rrs26DisclosurePanel);
    }

    private void setupRRS26StartModeFlags() {
        rrs26StartModeFlagsBox = new ListBox();
        rrs26StartModeFlagsBox.setMultipleSelect(true);
        rrs26StartModeFlagsBox.setWidth("100%");
        List<Flags> selectedFlags = new ArrayList<Flags>();
        if (originalConfiguration != null && originalConfiguration.rrs26Configuration != null
                && originalConfiguration.rrs26Configuration.startModeFlags != null) {
            selectedFlags = originalConfiguration.rrs26Configuration.startModeFlags;
        }
        ListBoxUtils.setupRRS26StartmodeFlagsListBox(rrs26StartModeFlagsBox, selectedFlags);
        rrs26StartModeFlagsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
    }

    private void setupSWCStart() {
        swcStartDisclosurePanel = new DisclosurePanel(stringMessages.sailingWorldCupStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(4, 3);
        swcStartEnabledBox = new CheckBox(stringMessages.setConfiguration());
        swcStartEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isActive = event.getValue();
                swcStartClassFlagListBox.setEnabled(isActive);
                swcStartRecallBox.setEnabled(isActive);
                swcStartResultEntryBox.setEnabled(isActive);
                swcStartModeFlagsBox.setEnabled(isActive);
            }
        });
        swcStartClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.swcStartConfiguration);
        swcStartClassFlagListBox.setWidth("100%");
        swcStartRecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.swcStartConfiguration);
        swcStartResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.swcStartConfiguration);
        setupSWCStartModeFlags();

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, swcStartClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("SWC Start")));
        grid.setWidget(1, 0, swcStartRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, swcStartResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));
        grid.setWidget(3, 0, new Label(stringMessages.startmodeFlags() + ":"));
        grid.setWidget(3, 1, swcStartModeFlagsBox);
        grid.setWidget(3, 2, createHelpImage(stringMessages.startmodeFlagsHelpText()));

        swcStartEnabledBox.setValue(originalConfiguration != null && originalConfiguration.swcStartConfiguration != null);
        ValueChangeEvent.fire(swcStartEnabledBox, swcStartEnabledBox.getValue());

        panel.add(swcStartEnabledBox);
        panel.add(grid);
        swcStartDisclosurePanel.add(panel);
        contentPanel.add(swcStartDisclosurePanel);
    }

    private void setupSWCStartModeFlags() {
        swcStartModeFlagsBox = new ListBox();
        swcStartModeFlagsBox.setMultipleSelect(true);
        swcStartModeFlagsBox.setWidth("100%");
        List<Flags> selectedFlags = new ArrayList<Flags>();
        if (originalConfiguration != null && originalConfiguration.swcStartConfiguration != null
                && originalConfiguration.swcStartConfiguration.startModeFlags != null) {
            selectedFlags = originalConfiguration.swcStartConfiguration.startModeFlags;
        }
        ListBoxUtils.setupSWCStartmodeFlagsListBox(swcStartModeFlagsBox, selectedFlags);
        swcStartModeFlagsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
    }

    private void setupGateStart() {
        gateStartDisclosurePanel = new DisclosurePanel(stringMessages.gateStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(5, 3);
        gateStartEnabledBox = new CheckBox(stringMessages.setConfiguration());
        gateStartEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                gateStartClassFlagListBox.setEnabled(event.getValue());
                gateStartRecallBox.setEnabled(event.getValue());
                gateStartResultEntryBox.setEnabled(event.getValue());
                gateStartPathfinderBox.setEnabled(event.getValue());
                gateStartGolfDownBox.setEnabled(event.getValue());
            }
        });
        gateStartClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.gateStartConfiguration);
        gateStartClassFlagListBox.setWidth("100%");
        gateStartRecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.gateStartConfiguration);
        gateStartResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.gateStartConfiguration);
        gateStartPathfinderBox = createCheckbox(stringMessages.activatePathfinder());
        if (originalConfiguration.gateStartConfiguration != null) {
            gateStartPathfinderBox.setValue(originalConfiguration.gateStartConfiguration.hasPathfinder);
        }
        gateStartGolfDownBox = createCheckbox(stringMessages.hasAdditionalGolfDownTime());
        if (originalConfiguration.gateStartConfiguration != null) {
            gateStartGolfDownBox.setValue(originalConfiguration.gateStartConfiguration.hasAdditionalGolfDownTime);
        }
        
        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, gateStartClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("Gate Start")));
        grid.setWidget(1, 0, gateStartRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, gateStartResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));
        grid.setWidget(3, 0, gateStartPathfinderBox);
        grid.setWidget(3, 2, createHelpImage(stringMessages.pathfinderHelpText()));
        grid.setWidget(4, 0, gateStartGolfDownBox);
        grid.setWidget(4, 2, createHelpImage(stringMessages.additionalGolfDownTimeHelpText()));

        gateStartEnabledBox.setValue(originalConfiguration != null
                && originalConfiguration.gateStartConfiguration != null);
        ValueChangeEvent.fire(gateStartEnabledBox, gateStartEnabledBox.getValue());

        panel.add(gateStartEnabledBox);
        panel.add(grid);
        gateStartDisclosurePanel.add(panel);
        contentPanel.add(gateStartDisclosurePanel);
    }

    private void setupESS() {
        essDisclosurePanel = new DisclosurePanel(stringMessages.essStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(3, 3);
        essEnabledBox = new CheckBox(stringMessages.setConfiguration());
        essEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                essClassFlagListBox.setEnabled(event.getValue());
                essRecallBox.setEnabled(event.getValue());
                essResultEntryBox.setEnabled(event.getValue());
            }
        });
        essClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.essConfiguration);
        essClassFlagListBox.setWidth("100%");
        essRecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.essConfiguration);
        essResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.essConfiguration);

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, essClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("ESS")));
        grid.setWidget(1, 0, essRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, essResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));

        essEnabledBox.setValue(originalConfiguration != null && originalConfiguration.essConfiguration != null);
        ValueChangeEvent.fire(essEnabledBox, essEnabledBox.getValue());

        panel.add(essEnabledBox);
        panel.add(grid);
        essDisclosurePanel.add(panel);
        contentPanel.add(essDisclosurePanel);
    }

    private void setupBasic() {
        basicDisclosurePanel = new DisclosurePanel(stringMessages.basicStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(3, 3);
        basicEnabledBox = new CheckBox(stringMessages.setConfiguration());
        basicEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                basicClassFlagListBox.setEnabled(event.getValue());
                basicRecallBox.setEnabled(event.getValue());
                basicResultEntryBox.setEnabled(event.getValue());
            }
        });
        basicClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.basicConfiguration);
        basicClassFlagListBox.setWidth("100%");
        basicRecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.basicConfiguration);
        basicResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.basicConfiguration);

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, basicClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("basic start")));
        grid.setWidget(1, 0, basicRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, basicResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));

        basicEnabledBox.setValue(originalConfiguration != null && originalConfiguration.basicConfiguration != null);
        ValueChangeEvent.fire(basicEnabledBox, basicEnabledBox.getValue());

        panel.add(basicEnabledBox);
        panel.add(grid);
        basicDisclosurePanel.add(panel);
        contentPanel.add(basicDisclosurePanel);
    }

    private void setupLeague() {
        leagueDisclosurePanel = new DisclosurePanel(stringMessages.leagueStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(3, 3);
        leagueEnabledBox = new CheckBox(stringMessages.setConfiguration());
        leagueEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                leagueClassFlagListBox.setEnabled(event.getValue());
                leagueRecallBox.setEnabled(event.getValue());
                leagueResultEntryBox.setEnabled(event.getValue());
            }
        });
        leagueClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.leagueConfiguration);
        leagueClassFlagListBox.setWidth("100%");
        leagueRecallBox = setupRecallBox(originalConfiguration==null?null:originalConfiguration.leagueConfiguration);
        leagueResultEntryBox = setupResultEntryBox(originalConfiguration==null?null:originalConfiguration.leagueConfiguration);

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, leagueClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("league start")));
        grid.setWidget(1, 0, leagueRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, leagueResultEntryBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.resultEntryHelpText()));

        leagueEnabledBox.setValue(originalConfiguration != null && originalConfiguration.leagueConfiguration != null);
        ValueChangeEvent.fire(leagueEnabledBox, leagueEnabledBox.getValue());

        panel.add(leagueEnabledBox);
        panel.add(grid);
        leagueDisclosurePanel.add(panel);
        contentPanel.add(leagueDisclosurePanel);
    }

    private Widget createHelpImage(String tooltip) {
        Image help = new Image();
        help.setResource(resources.help());
        help.setTitle(tooltip);
        help.setAltText(tooltip);
        return help;
    }

    @Override
    protected RegattaConfigurationDTO getResult() {
        DeviceConfigurationDTO.RegattaConfigurationDTO result = new DeviceConfigurationDTO.RegattaConfigurationDTO();
        int index = racingProcedureListBox.getSelectedIndex();
        if (index >= 0) {
            RacingProcedureType type = RacingProcedureType.valueOf(racingProcedureListBox.getValue(index));
            result.defaultRacingProcedureType = type == RacingProcedureType.UNKNOWN ? null : type;
        }
        result.defaultRacingProcedureType = getSelectedRacingProcedure();
        index = designerModeEntryListBox.getSelectedIndex();
        if (index >= 0) {
            CourseDesignerMode mode = CourseDesignerMode.valueOf(designerModeEntryListBox.getValue(index));
            result.defaultCourseDesignerMode = mode == CourseDesignerMode.UNKNOWN ? null : mode;
        }
        result.defaultProtestTimeDuration = protestTimeInMinutesTextBox.getValue() == null ? null :
            Duration.ONE_MINUTE.times(protestTimeInMinutesTextBox.getValue());
        if (rrs26EnabledBox.getValue()) {
            result.rrs26Configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RRS26ConfigurationDTO();
            getRacingProcedureConfigurationResults(result.rrs26Configuration, rrs26ClassFlagListBox, rrs26RecallBox, rrs26ResultEntryBox);
            List<Flags> flags = new ArrayList<Flags>();
            for (int i = 0; i < rrs26StartModeFlagsBox.getItemCount(); i++) {
                if (rrs26StartModeFlagsBox.isItemSelected(i)) {
                    flags.add(Flags.valueOf(rrs26StartModeFlagsBox.getValue(i)));
                }
            }
            result.rrs26Configuration.startModeFlags = flags.isEmpty() ? null : flags;
        }
        if (swcStartEnabledBox.getValue()) {
            result.swcStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.SWCStartConfigurationDTO();
            getRacingProcedureConfigurationResults(result.swcStartConfiguration, swcStartClassFlagListBox, swcStartRecallBox, swcStartResultEntryBox);
            List<Flags> flags = new ArrayList<Flags>();
            for (int i = 0; i < swcStartModeFlagsBox.getItemCount(); i++) {
                if (swcStartModeFlagsBox.isItemSelected(i)) {
                    flags.add(Flags.valueOf(swcStartModeFlagsBox.getValue(i)));
                }
            }
            result.swcStartConfiguration.startModeFlags = flags.isEmpty() ? null : flags;
        }
        if (gateStartEnabledBox.getValue()) {
            result.gateStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.GateStartConfigurationDTO();
            getRacingProcedureConfigurationResults(result.gateStartConfiguration, gateStartClassFlagListBox,
                    gateStartRecallBox, gateStartResultEntryBox);
            result.gateStartConfiguration.hasPathfinder = gateStartPathfinderBox.getValue();
            result.gateStartConfiguration.hasAdditionalGolfDownTime = gateStartGolfDownBox.getValue();
        }
        if (essEnabledBox.getValue()) {
            result.essConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.ESSConfigurationDTO();
            getRacingProcedureConfigurationResults(result.essConfiguration, essClassFlagListBox, essRecallBox, essResultEntryBox);
        }
        if (basicEnabledBox.getValue()) {
            result.basicConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO();
            getRacingProcedureConfigurationResults(result.basicConfiguration, basicClassFlagListBox, basicRecallBox, basicResultEntryBox);
        }
        if (leagueEnabledBox.getValue()) {
            result.leagueConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.LeagueConfigurationDTO();
            getRacingProcedureConfigurationResults(result.leagueConfiguration, leagueClassFlagListBox, leagueRecallBox, leagueResultEntryBox);
        }
        return result;
    }

    private RacingProcedureType getSelectedRacingProcedure() {
        int index = racingProcedureListBox.getSelectedIndex();
        if (index >= 0) {
            RacingProcedureType type = RacingProcedureType.valueOf(racingProcedureListBox.getValue(index));
            return type == RacingProcedureType.UNKNOWN ? null : type;
        }
        return null;
    }

    private void getRacingProcedureConfigurationResults(
            DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO target,
            ListBox classListBox, CheckBox recallBox, CheckBox resultEntryBox) {
        int index = classListBox.getSelectedIndex();
        if (index >= 0) {
            Flags flag = Flags.valueOf(classListBox.getValue(index));
            target.classFlag = flag == Flags.NONE ? null : flag;
        }
        target.hasIndividualRecall = recallBox.getValue();
        target.isResultEntryEnabled = resultEntryBox.getValue();
    }
}
