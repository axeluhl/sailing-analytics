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
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaConfigurationDialog extends DataEntryDialog<DeviceConfigurationDTO.RegattaConfigurationDTO> {

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private final StringMessages stringMessages;
    private final DeviceConfigurationDTO.RegattaConfigurationDTO originalConfiguration;

    private VerticalPanel contentPanel;

    private ListBox racingProcedureListBox;
    private ListBox designerModeEntryListBox;

    private DisclosurePanel rrs26DisclosurePanel;
    private CheckBox rrs26EnabledBox;
    private ListBox rrs26ClassFlagListBox;
    private CheckBox rrs26RecallBox;
    private ListBox rrs26StartModeFlagsBox;

    private DisclosurePanel gateStartDisclosurePanel;
    private CheckBox gateStartEnabledBox;
    private ListBox gateStartClassFlagListBox;
    private CheckBox gateStartRecallBox;
    private CheckBox gateStartPathfinderBox;
    private CheckBox gateStartGolfDownBox;

    private DisclosurePanel essDisclosurePanel;
    private CheckBox essEnabledBox;
    private ListBox essClassFlagListBox;
    private CheckBox essRecallBox;

    private DisclosurePanel basicDisclosurePanel;
    private CheckBox basicEnabledBox;
    private ListBox basicClassFlagListBox;
    private CheckBox basicRecallBox;

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
        setupGateStart();
        setupESS();
        setupBasic();
        
        if (rrs26EnabledBox.getValue()) {
            rrs26DisclosurePanel.setOpen(true);
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

        return contentPanel;
    }

    private void setupGeneral() {
        Grid grid = new Grid(2, 2);
        setupCourseDesignerListBox(grid, 0);
        setupRacingProcedureListBox(grid, 1);
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
                markAsDrity(true);
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
                markAsDrity(true);
            }
        });
        grid.setWidget(gridRow, 0, new Label(stringMessages.courseDesignerMode()));
        grid.setWidget(gridRow, 1, designerModeEntryListBox);
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
                markAsDrity(true);
            }
        });
        return box;
    }

    private CheckBox setupRecallBox() {
        CheckBox box = new CheckBox(stringMessages.activateIndividualRecall());
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
            }
        });
        return box;
    }

    private void setupRRS26() {
        rrs26DisclosurePanel = new DisclosurePanel(stringMessages.rrs26Start());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(3, 3);
        rrs26EnabledBox = new CheckBox(stringMessages.setConfiguration());
        rrs26EnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                boolean isActive = event.getValue();
                rrs26ClassFlagListBox.setEnabled(isActive);
                rrs26RecallBox.setEnabled(isActive);
                rrs26StartModeFlagsBox.setEnabled(isActive);
            }
        });
        rrs26ClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.rrs26Configuration);
        rrs26ClassFlagListBox.setWidth("100%");
        rrs26RecallBox = setupRecallBox();
        setupRRS26StartModeFlags();

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, rrs26ClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("RRS26")));
        grid.setWidget(1, 0, rrs26RecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, new Label(stringMessages.startmodeFlags() + ":"));
        grid.setWidget(2, 1, rrs26StartModeFlagsBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.startmodeFlagsHelpText()));

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
        ListBoxUtils.setupFlagsListBox(rrs26StartModeFlagsBox, selectedFlags);
        rrs26StartModeFlagsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                markAsDrity(true);
            }
        });
    }

    private void setupGateStart() {
        gateStartDisclosurePanel = new DisclosurePanel(stringMessages.gateStart());
        VerticalPanel panel = new VerticalPanel();

        Grid grid = new Grid(4, 3);
        gateStartEnabledBox = new CheckBox(stringMessages.setConfiguration());
        gateStartEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                gateStartClassFlagListBox.setEnabled(event.getValue());
                gateStartRecallBox.setEnabled(event.getValue());
                gateStartPathfinderBox.setEnabled(event.getValue());
                gateStartGolfDownBox.setEnabled(event.getValue());
            }
        });
        gateStartClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.gateStartConfiguration);
        gateStartClassFlagListBox.setWidth("100%");
        gateStartRecallBox = setupRecallBox();
        gateStartPathfinderBox = new CheckBox(stringMessages.activatePathfinder());
        gateStartPathfinderBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
            }
        });
        gateStartGolfDownBox = createCheckbox(stringMessages.hasAdditionalGolfDownTime());
        gateStartGolfDownBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
            }
        });

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, gateStartClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("Gate Start")));
        grid.setWidget(1, 0, gateStartRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        grid.setWidget(2, 0, gateStartPathfinderBox);
        grid.setWidget(2, 2, createHelpImage(stringMessages.pathfinderHelpText()));
        grid.setWidget(3, 0, gateStartGolfDownBox);
        grid.setWidget(3, 2, createHelpImage(stringMessages.additionalGolfDownTimeHelpText()));

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

        Grid grid = new Grid(2, 3);
        essEnabledBox = new CheckBox(stringMessages.setConfiguration());
        essEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                essClassFlagListBox.setEnabled(event.getValue());
                essRecallBox.setEnabled(event.getValue());
            }
        });
        essClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.essConfiguration);
        essClassFlagListBox.setWidth("100%");
        essRecallBox = setupRecallBox();

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, essClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("ESS")));
        grid.setWidget(1, 0, essRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));

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

        Grid grid = new Grid(2, 3);
        basicEnabledBox = new CheckBox(stringMessages.setConfiguration());
        basicEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                basicClassFlagListBox.setEnabled(event.getValue());
                basicRecallBox.setEnabled(event.getValue());
            }
        });
        basicClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null
                : originalConfiguration.basicConfiguration);
        basicClassFlagListBox.setWidth("100%");
        basicRecallBox = setupRecallBox();

        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, basicClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("basic start")));
        grid.setWidget(1, 0, basicRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));

        basicEnabledBox.setValue(originalConfiguration != null && originalConfiguration.basicConfiguration != null);
        ValueChangeEvent.fire(basicEnabledBox, basicEnabledBox.getValue());

        panel.add(basicEnabledBox);
        panel.add(grid);
        basicDisclosurePanel.add(panel);
        contentPanel.add(basicDisclosurePanel);
    }

    private void markAsDrity(boolean b) {
        // hmnn do we need to this?
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

        if (rrs26EnabledBox.getValue()) {
            result.rrs26Configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RRS26ConfigurationDTO();
            getRacingProcedureConfigurationResults(result.rrs26Configuration, rrs26ClassFlagListBox, rrs26RecallBox);
            List<Flags> flags = new ArrayList<Flags>();
            for (int i = 0; i < rrs26StartModeFlagsBox.getItemCount(); i++) {
                if (rrs26StartModeFlagsBox.isItemSelected(i)) {
                    flags.add(Flags.valueOf(rrs26StartModeFlagsBox.getValue(i)));
                }
            }
            result.rrs26Configuration.startModeFlags = flags.isEmpty() ? null : flags;
        }
        if (gateStartEnabledBox.getValue()) {
            result.gateStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.GateStartConfigurationDTO();
            getRacingProcedureConfigurationResults(result.gateStartConfiguration, gateStartClassFlagListBox,
                    gateStartRecallBox);
            result.gateStartConfiguration.hasPathfinder = gateStartPathfinderBox.getValue();
            result.gateStartConfiguration.hasAdditionalGolfDownTime = gateStartGolfDownBox.getValue();
        }
        if (essEnabledBox.getValue()) {
            result.essConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.ESSConfigurationDTO();
            getRacingProcedureConfigurationResults(result.essConfiguration, essClassFlagListBox, essRecallBox);
        }
        if (basicEnabledBox.getValue()) {
            result.basicConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO();
            getRacingProcedureConfigurationResults(result.basicConfiguration, basicClassFlagListBox, basicRecallBox);
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
            ListBox classListBox, CheckBox recallBox) {
        int index = classListBox.getSelectedIndex();
        if (index >= 0) {
            Flags flag = Flags.valueOf(classListBox.getValue(index));
            target.classFlag = flag == Flags.NONE ? null : flag;
        }
        target.hasInidividualRecall = recallBox.getValue();
    }
}
