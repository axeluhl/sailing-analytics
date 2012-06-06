package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithGroupsCreateDialog extends DataEntryDialog<SeriesDTO> {

    private StringMessages stringConstants;
    private SeriesDTO series;

    private TextBox nameEntryField;

    private List<TextBox> groupNameEntryFields;
    private List<ListBox> groupColorEntryFields;
    private List<IntegerBox> groupOrderNoEntryFields;

    private Grid groupsGrid;

    protected static class SeriesParameterValidator implements Validator<SeriesDTO> {

        private StringMessages stringConstants;
        private ArrayList<SeriesDTO> existingSeries;

        public SeriesParameterValidator(StringMessages stringConstants, Collection<SeriesDTO> existingSeries) {
            this.stringConstants = stringConstants;
            this.existingSeries = new ArrayList<SeriesDTO>(existingSeries);
        }

        @Override
        public String getErrorMessage(SeriesDTO seriesToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = seriesToValidate.name != null && seriesToValidate.name.length() > 0;

            boolean unique = true;
            for (SeriesDTO series : existingSeries) {
                if (series.name.equals(seriesToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!unique) {
                errorMessage = stringConstants.seriesWithThisNameAlreadyExists();
            }

            if(errorMessage == null) {
                List<FleetDTO> groupsToValidate = seriesToValidate.getFleets();
                int index = 0;
                boolean groupNameNotEmpty = true;

                for (FleetDTO group : groupsToValidate) {
                    groupNameNotEmpty = group.name != null && group.name.length() > 0;
                    if(!groupNameNotEmpty) {
                        break;
                    }
                    index++;
                }

                int index2 = 0;
                boolean groupUnique = true;
                
                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (FleetDTO group: groupsToValidate) {
                    if(!setToFindDuplicates.add(group.name)) {
                        groupUnique = false;
                        break;
                    }
                    index2++;
                }

                if (!groupNameNotEmpty) {
                    errorMessage = stringConstants.group() + " " + (index + 1) + ": " + stringConstants.pleaseEnterNonEmptyName();
                } else if (!groupUnique) {
                    errorMessage = stringConstants.group() + " " + (index2 + 1) + ": " + stringConstants.groupWithThisNameAlreadyExists();
                }
                
            }
            
            return errorMessage;
        }

    }

    public SeriesWithGroupsCreateDialog(Collection<SeriesDTO> existingSeries, StringMessages stringConstants,
            AsyncCallback<SeriesDTO> callback) {
        super(stringConstants.series(), null, stringConstants.ok(), stringConstants.cancel(),  
                new SeriesParameterValidator(stringConstants, existingSeries), callback);
        this.stringConstants = stringConstants;
        this.series = new SeriesDTO();

        nameEntryField = createTextBox(null);
        nameEntryField.setWidth("200px");

        groupNameEntryFields = new ArrayList<TextBox>();
        groupColorEntryFields = new ArrayList<ListBox>();
        groupOrderNoEntryFields = new ArrayList<IntegerBox>(); 

        groupsGrid = new Grid(0, 0);
    }

    private Widget createGroupNameWidget(String defaultName) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setWidth("200px");
        groupNameEntryFields.add(textBox);
        return textBox; 
    }

    private Widget createGroupColorWidget(Color defaultColor) {
        ListBox listBox = createListBox(false); 
        for(GroupColors value: GroupColors.values())
            listBox.addItem(value.name());
        groupColorEntryFields.add(listBox);
        return listBox; 
    }

    private Widget createGroupOrderNoWidget(int defaultValue) {
        IntegerBox intBox = createIntegerBox(defaultValue, 3); 
        groupOrderNoEntryFields.add(intBox);
        return intBox; 
    }

    @Override
    protected SeriesDTO getResult() {
        series.name = nameEntryField.getText();

        List<FleetDTO> fleets = new ArrayList<FleetDTO>();
        int groupsCount = groupNameEntryFields.size();
        for(int i = 0; i < groupsCount; i++) {
            FleetDTO fleetDTO = new FleetDTO();
            fleetDTO.name = groupNameEntryFields.get(i).getValue();
            Color color = new RGBColor(0, 0, 0);
            fleetDTO.setColor(color);
            fleetDTO.setOrderNo(groupOrderNoEntryFields.get(i).getValue());
            fleets.add(fleetDTO);
        }
        
        series.setFleets(fleets);

        return series;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        
        panel.add(createHeadlineLabel(stringConstants.groups()));
        panel.add(groupsGrid);
        
        Button addGroupButton = new Button("Add group");
        addGroupButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                createGroupNameWidget(null);
                createGroupOrderNoWidget(0);
                createGroupColorWidget(null);
                updateGroupsGrid(panel);
            }
        });
        panel.add(addGroupButton);
        
        return panel;
    }

    private void updateGroupsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(groupsGrid);
        parentPanel.remove(groupsGrid);
        
        int groupsCount = groupNameEntryFields.size();
        groupsGrid = new Grid(groupsCount + 1, 4);
        groupsGrid.setCellSpacing(3);

        groupsGrid.setHTML(0, 1, stringConstants.name());
        groupsGrid.setHTML(0, 2, stringConstants.no());
        groupsGrid.setHTML(0, 3, stringConstants.color());

        for(int i = 0; i < groupsCount; i++) {
            Label groupLabel = new Label((i+1) + ". " + stringConstants.group() + ":");
            groupLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            groupLabel.setWordWrap(false);
            groupsGrid.setWidget(i+1, 0, groupLabel);
            groupsGrid.setWidget(i+1, 1, groupNameEntryFields.get(i));
            groupsGrid.setWidget(i+1, 2, groupOrderNoEntryFields.get(i));
            groupsGrid.setWidget(i+1, 3, groupColorEntryFields.get(i));
        }

        parentPanel.insert(groupsGrid, widgetIndex);
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
