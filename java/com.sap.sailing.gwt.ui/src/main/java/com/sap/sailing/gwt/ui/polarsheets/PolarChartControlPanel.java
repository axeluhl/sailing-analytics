package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
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
        // Button exportButton = new Button("Export");
        // setExportButtonListener(exportButton);
        // leftPanel.add(exportButton);
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
    
    private void setExportButtonListener(Button exportButton) {
        ClickHandler handler = new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String name = nameListBox.getItemText(nameListBox.getSelectedIndex());
                if (name!=null && !name.isEmpty()) {
                    StringBuffer exportData = new StringBuffer();
                    exportData.append(name + "\n");
                    Series[] seriesPerWindspeed = chartPanel.getSeriesPerWindspeedForName(name);
                    for (Series series : seriesPerWindspeed) {
                        if (series == null) {
                            continue;
                        }
                        String nameOfSeries = chartPanel.getNameForSeries(series);
                        String[] split = nameOfSeries.split("-");
                        int windSpeed = Integer.parseInt(split[2]);
                        Point[] points = series.getPoints();
                        if (windSpeed == 4) {
                            int[] degs = {0,50,60,110,130,180};
                            exportData.append("4 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 6) {
                            int[] degs = {0,47,60,110,135,180};
                            exportData.append("6 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 8) {
                            int[] degs = {0,43,60,110,135,180};
                            exportData.append("8 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 10) {
                            int[] degs = {0,41,60,110,140,180};
                            exportData.append("10 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 12) {
                            int[] degs = {0,40,60,110,145,180};
                            exportData.append("12 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 14) {
                            int[] degs = {0,39,60,110,155,180};
                            exportData.append("14 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 16) {
                            int[] degs = {0,38,60,110,155,180};
                            exportData.append("16 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 20) {
                            int[] degs = {0,38,60,110,160,180};
                            exportData.append("20 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 25) {
                            int[] degs = {0,39,60,110,168,180};
                            exportData.append("25 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 30) {
                            int[] degs = {0,41,60,110,157,180};
                            exportData.append("30 " + createStringForDegrees(points, degs) + "\n");
                        }
                        
                    }
                    
                    Window.alert(exportData.toString());
                }
                
            }
        };
        
        exportButton.addClickHandler(handler);
        
    }
    
    private String createStringForDegrees(Point[] points, int[] degrees) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < degrees.length; i++) {
            int deg = degrees[i];
            double average;
            if (deg > 0 && deg < 180) {
                double windRight = points[deg].getY().doubleValue();
                double windLeft = points[360 - deg].getY().doubleValue();
                average = (windRight + windLeft) / 2;
            } else {
                average = points[deg].getY().doubleValue();
            }
            NumberFormat fmt = NumberFormat.getDecimalFormat();
            fmt.overrideFractionDigits(2);
            buffer.append(deg + " " + fmt.format(average) + " ");
        }
        return buffer.toString();
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
