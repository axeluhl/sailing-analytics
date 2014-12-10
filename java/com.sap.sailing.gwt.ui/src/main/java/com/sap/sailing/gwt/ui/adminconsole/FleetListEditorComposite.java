package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.FleetColors;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.listedit.ExpandedListEditorUi;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

public class FleetListEditorComposite extends ListEditorComposite<FleetDTO> {
    public FleetListEditorComposite(Iterable<FleetDTO> initialValues, StringMessages stringMessages, ImageResource removeImage) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage));
    }
    
    private static class ExpandedUi extends ExpandedListEditorUi<FleetDTO> {

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage) {
            super(stringMessages, removeImage, /*canRemoveItems*/true);
        }

        
        @Override
        protected StringMessages getStringMessages() {
            return (StringMessages) super.getStringMessages();
        }

        @Override
        protected Widget createAddWidget() {
            HorizontalPanel hPanel = new HorizontalPanel();
            hPanel.setSpacing(4);
            hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

            final TextBox nameBox = createNameBox();
            nameBox.ensureDebugId("NameTextBox");
            
            final IntegerBox orderNoBox = createOrderNoBox();
            orderNoBox.ensureDebugId("OrderNoIntegerBox");
            
            final ListBox colorListBox = createColorListBox(nameBox, orderNoBox);
            colorListBox.ensureDebugId("ColorListBox");
            
            final Button addButton = new Button(getStringMessages().add());
            addButton.ensureDebugId("AddButton");
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addValue(new FleetDTO(nameBox.getValue(), orderNoBox.getValue(), getSelectedColor(colorListBox)));
                }
            });

            hPanel.add(new Label(getStringMessages().color() + ":"));
            hPanel.add(colorListBox);
            hPanel.add(new Label(getStringMessages().name() + ":"));
            hPanel.add(nameBox);
            hPanel.add(new Label(getStringMessages().rank() + ":"));
            hPanel.add(orderNoBox);
            hPanel.add(addButton);
            return hPanel;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, FleetDTO fleet) {
            HorizontalPanel hPanel = new HorizontalPanel();
            hPanel.setSpacing(4);
            Label fleetLabel = new Label(getStringMessages().fleet() + " '" + fleet.getName() + "' :");
            fleetLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            hPanel.add(fleetLabel);
            
            String valueText = "";
            if(fleet.getColor() != null) {
                valueText += getStringMessages().color() + " '" + fleet.getColor().toString() + "'";
            } else {
                valueText += getStringMessages().noColor();
            }
            valueText += ", " + getStringMessages().rank() + " " + fleet.getOrderNo();
            
            hPanel.add(new Label(valueText));
            
            return hPanel;
        }

        private IntegerBox createOrderNoBox() {
            IntegerBox orderNoBox = new IntegerBox();
            orderNoBox.setVisibleLength(3);
            orderNoBox.setValue(0);
            return orderNoBox;
        }

        private TextBox createNameBox() {
            final TextBox nameBox = new TextBox();
            nameBox.setVisibleLength(40);
            nameBox.setWidth("175px");
            return nameBox;
        }

        private ListBox createColorListBox(final TextBox nameBox, final IntegerBox orderNoBox) {
            final ListBox colorListBox = new ListBox();
            colorListBox.setMultipleSelect(false);

            colorListBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    // set default order no of the selected color
                    int selIndex = colorListBox.getSelectedIndex();
                    if (selIndex == 0) {
                        orderNoBox.setValue(0);
                    } else {
                        String value = colorListBox.getValue(selIndex);
                        final FleetColors color = FleetColors.valueOf(value);
                        if (color != null) {
                            orderNoBox.setValue(color.getDefaultOrderNo());
                            nameBox.setValue(getColorText(color));
                        }
                    }
                }
            });
            colorListBox.addItem(getStringMessages().noColor());
            for (FleetColors value : FleetColors.values()) {
                colorListBox.addItem(value.name());
            }
            colorListBox.setSelectedIndex(0);
            return colorListBox;
        }

        private Color getSelectedColor(ListBox colorListBox) {
            Color result = null;
            int selIndex = colorListBox.getSelectedIndex();
            // the zero index represents the 'no color' option
            if (selIndex > 0) {
                String value = colorListBox.getValue(selIndex);
                for (FleetColors color : FleetColors.values()) {
                    if (color.name().equals(value)) {
                        result = color.getColor();
                        break;
                    }
                }
            }
            return result;
        }

        private String getColorText(FleetColors color) {
            if (color == null) {
                return getStringMessages().noColor();
            }
            return color.name().charAt(0) + color.name().toLowerCase().substring(1);
        }

    }

};
