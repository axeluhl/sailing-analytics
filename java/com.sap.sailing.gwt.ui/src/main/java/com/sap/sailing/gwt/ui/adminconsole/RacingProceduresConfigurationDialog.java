package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RacingProceduresConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RacingProceduresConfigurationDTO.RacingProcedureConfigurationDTO;

public class RacingProceduresConfigurationDialog extends DataEntryDialog<DeviceConfigurationDTO.RacingProceduresConfigurationDTO> {
    
    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    private final StringMessages stringMessages;
    private final DeviceConfigurationDTO.RacingProceduresConfigurationDTO originalConfiguration;
    
    private VerticalPanel contentPanel;
    
    private CheckBox rrs26EnabledBox;
    private ListBox rrs26ClassFlagListBox;
    private CheckBox rrs26RecallBox;
    private ListBox rrs26StartModeFlagsBox;
    
    private CheckBox gateStartEnabledBox;
    private ListBox gateStartClassFlagListBox;
    private CheckBox gateStartRecallBox;
    private CheckBox gateStartPathfinderBox;
    
    private CheckBox essEnabledBox;
    private ListBox essClassFlagListBox;
    private CheckBox essRecallBox;
    
    private CheckBox basicEnabledBox;
    private ListBox basicClassFlagListBox;
    private CheckBox basicRecallBox;
    
    public RacingProceduresConfigurationDialog(
            DeviceConfigurationDTO.RacingProceduresConfigurationDTO proceduresConfiguration,
            StringMessages messages,
            DataEntryDialog.DialogCallback<RacingProceduresConfigurationDTO> callback) {
        super(messages.racingProcedureConfiguration(), "", messages.save(), messages.cancel(), null, callback);
        this.stringMessages = messages;
        this.originalConfiguration = proceduresConfiguration;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        contentPanel = new VerticalPanel();
        setupRRS26();
        setupGateStart();
        setupESS();
        setupBasic();
        return contentPanel;
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
        CaptionPanel caption = new CaptionPanel(stringMessages.rrs26Start());
        VerticalPanel panel = new VerticalPanel();
        
        Grid grid = new Grid(3, 3);
        rrs26EnabledBox = new CheckBox(stringMessages.overwriteConfiguration());
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
        rrs26ClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null : originalConfiguration.rrs26Configuration);
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
        caption.add(panel);
        contentPanel.add(caption);
    }

    private void setupRRS26StartModeFlags() {
        rrs26StartModeFlagsBox = new ListBox(true);
        rrs26StartModeFlagsBox.setWidth("100%");
        List<Flags> selectedFlags = new ArrayList<Flags>();
        if (originalConfiguration != null && originalConfiguration.rrs26Configuration != null && originalConfiguration.rrs26Configuration.startModeFlags != null) {
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
        CaptionPanel caption = new CaptionPanel(stringMessages.gateStart());
        VerticalPanel panel = new VerticalPanel();
        
        Grid grid = new Grid(3, 3);
        gateStartEnabledBox = new CheckBox(stringMessages.overwriteConfiguration());
        gateStartEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                gateStartClassFlagListBox.setEnabled(event.getValue());
                gateStartRecallBox.setEnabled(event.getValue());
                gateStartPathfinderBox.setEnabled(event.getValue());
            }
        });
        gateStartClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null : originalConfiguration.gateStartConfiguration);
        gateStartClassFlagListBox.setWidth("100%");
        gateStartRecallBox = setupRecallBox();
        gateStartPathfinderBox = new CheckBox(stringMessages.activatePathfinder());
        gateStartPathfinderBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
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
        
        gateStartEnabledBox.setValue(originalConfiguration != null && originalConfiguration.gateStartConfiguration != null);
        ValueChangeEvent.fire(gateStartEnabledBox, gateStartEnabledBox.getValue());
        
        panel.add(gateStartEnabledBox);
        panel.add(grid);
        caption.add(panel);
        contentPanel.add(caption);
    }

    private void setupESS() {
        CaptionPanel caption = new CaptionPanel(stringMessages.essStart());
        VerticalPanel panel = new VerticalPanel();
        
        Grid grid = new Grid(2, 3);
        essEnabledBox = new CheckBox(stringMessages.overwriteConfiguration());
        essEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                essClassFlagListBox.setEnabled(event.getValue());
                essRecallBox.setEnabled(event.getValue());
            }
        });
        essClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null : originalConfiguration.essConfiguration);
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
        caption.add(panel);
        contentPanel.add(caption);
    }

    private void setupBasic() {
        CaptionPanel caption = new CaptionPanel(stringMessages.basicStart());
        VerticalPanel panel = new VerticalPanel();
        
        Grid grid = new Grid(2, 3);
        basicEnabledBox = new CheckBox(stringMessages.overwriteConfiguration());
        basicEnabledBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDrity(true);
                basicClassFlagListBox.setEnabled(event.getValue());
                basicRecallBox.setEnabled(event.getValue());
            }
        });
        basicClassFlagListBox = setupClassFlagListBox(originalConfiguration == null ? null : originalConfiguration.basicConfiguration);
        basicClassFlagListBox.setWidth("100%");
        basicRecallBox = setupRecallBox();
        
        grid.setWidget(0, 0, new Label(stringMessages.classFlag() + ":"));
        grid.setWidget(0, 1, basicClassFlagListBox);
        grid.setWidget(0, 2, createHelpImage(stringMessages.classFlagHelpText("basic start")));
        grid.setWidget(1, 0,  basicRecallBox);
        grid.setWidget(1, 2, createHelpImage(stringMessages.individualRecallHelpText()));
        
        basicEnabledBox.setValue(originalConfiguration != null && originalConfiguration.basicConfiguration != null);
        ValueChangeEvent.fire(basicEnabledBox, basicEnabledBox.getValue());
        
        panel.add(basicEnabledBox);
        panel.add(grid);
        caption.add(panel);
        contentPanel.add(caption);
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
    protected RacingProceduresConfigurationDTO getResult() {
        DeviceConfigurationDTO.RacingProceduresConfigurationDTO result = new DeviceConfigurationDTO.RacingProceduresConfigurationDTO();
        if (rrs26EnabledBox.getValue()) {
            result.rrs26Configuration = new DeviceConfigurationDTO.RacingProceduresConfigurationDTO.RRS26ConfigurationDTO();
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
            result.gateStartConfiguration = new DeviceConfigurationDTO.RacingProceduresConfigurationDTO.GateStartConfigurationDTO();
            getRacingProcedureConfigurationResults(result.gateStartConfiguration, gateStartClassFlagListBox, gateStartRecallBox);
            result.gateStartConfiguration.hasPathfinder = gateStartPathfinderBox.getValue();
            //result.gateStartConfiguration.hasAdditionalGolfDownTime = ???
        }
        if (essEnabledBox.getValue()) {
            result.essConfiguration = new DeviceConfigurationDTO.RacingProceduresConfigurationDTO.ESSConfigurationDTO();
            getRacingProcedureConfigurationResults(result.essConfiguration, essClassFlagListBox, essRecallBox);
        }
        if (basicEnabledBox.getValue()) {
            result.basicConfiguration = new DeviceConfigurationDTO.RacingProceduresConfigurationDTO.RacingProcedureConfigurationDTO();
            getRacingProcedureConfigurationResults(result.basicConfiguration, basicClassFlagListBox, basicRecallBox);
        }
        return result;
    }

    private void getRacingProcedureConfigurationResults(
            DeviceConfigurationDTO.RacingProceduresConfigurationDTO.RacingProcedureConfigurationDTO target,
            ListBox classListBox, CheckBox recallBox) {
        int index = classListBox.getSelectedIndex();
        if (index >= 0) {
            Flags flag = Flags.valueOf(classListBox.getValue(index));
            target.classFlag = flag == Flags.NONE ? null : flag;
        }
        target.hasInidividualRecall = recallBox.getValue();
    }
}
