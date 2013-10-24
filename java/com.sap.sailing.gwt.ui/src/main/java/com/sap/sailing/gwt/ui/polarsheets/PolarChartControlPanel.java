package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarChartControlPanel extends FlexTable implements PolarSheetListChangeListener {

    private final StringMessages stringMessages;
    private final PolarSheetsChartPanel chartPanel;
    private final ListBox nameListBox;
    private final List<Button> buttons;

    public PolarChartControlPanel(final StringMessages stringMessages, final PolarSheetsChartPanel chartPanel) {
        this.stringMessages = stringMessages;
        this.chartPanel = chartPanel;
        nameListBox = new ListBox();
        buttons = new ArrayList<Button>();
        setChangeHandlerForNameListBox();
        createRemoveAllRow();
        createPickIndividualSheetRow();
        createIndividualRemoveRow();
    }

    private void setChangeHandlerForNameListBox() {
        nameListBox.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                onListBoxChange();
            }
        });
    }

    private void createIndividualRemoveRow() {
        Button removeButton = new Button(stringMessages.remove());
        buttons.add(removeButton);
        removeButton.setEnabled(false);
        removeButton.addClickHandler(createRemoveIndividualHandler());
        this.setWidget(2, 1, removeButton);
    }

    private ClickHandler createRemoveIndividualHandler() {
        return new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (nameListBox.getItemCount() > 0) {
                    int selectedIndex = nameListBox.getSelectedIndex();
                    String name = nameListBox.getItemText(selectedIndex);
                    if (name != null && !name.isEmpty()) {
                        chartPanel.removeSeries(name);
                        nameListBox.removeItem(selectedIndex);
                        onListBoxChange();
                    }
                }
            }
        };
    }

    private void createPickIndividualSheetRow() {
        Label pickSheetLabel = new Label(stringMessages.selectSheet() + ":");       
        this.setWidget(1, 0, pickSheetLabel);
        this.setWidget(1, 1, nameListBox);
    }

    private void createRemoveAllRow() {
        Label removeAllLabel = new Label(stringMessages.removeAllSheets() + ":");
        this.setWidget(0, 0, removeAllLabel);
        this.getFlexCellFormatter().setHeight(0, 0, "100px");
        Button clearAllButton = new Button(stringMessages.removeAll());
        buttons.add(clearAllButton);
        clearAllButton.setEnabled(false);
        clearAllButton.addClickHandler(createRemoveAllButtonHandler());
        this.setWidget(0, 1, clearAllButton);
    }

    private ClickHandler createRemoveAllButtonHandler() {
        return new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                nameListBox.clear();
                onListBoxChange();
                chartPanel.removeAllSeries();
            }
        };
    }

    @Override
    public void polarSheetAdded(String name) {
        nameListBox.addItem(name);
        onListBoxChange();
    }

    private void onListBoxChange() {
        if (nameListBox.getItemCount() < 1) {
            for (Button button : buttons) {
                button.setEnabled(false);
            }
        } else {
            for (Button button : buttons) {
                button.setEnabled(true);
            }
        }
    }

}
