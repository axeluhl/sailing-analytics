package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.ImageResource;
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

public class FleetListEditableEditorComposite extends FleetListEditorComposite {

    public FleetListEditableEditorComposite(List<FleetDTO> initialValues, StringMessages stringMessages,
            ImageResource removeImage) {
        super(initialValues, stringMessages, removeImage);
    }

    protected ExpandedListEditorUi<FleetDTO> getExpandedUI(StringMessages stringMessages, ImageResource editImage) {
        return new ExpandedEditableUi(stringMessages, editImage);
    }

    private static class ExpandedEditableUi extends ExpandedListEditorUi<FleetDTO> {

        public ExpandedEditableUi(StringMessages stringMessages, ImageResource editImage) {
            super(stringMessages, editImage, false);
        }
        
        @Override
        protected StringMessages getStringMessages() {
            return (StringMessages) super.getStringMessages();
        }

        @Override
        protected Widget createAddWidget() {
            return new HorizontalPanel();
        }

        @Override
        protected Widget createValueWidget(int rowIndex, final FleetDTO fleet) {
            HorizontalPanel hPanel = new HorizontalPanel();
            hPanel.setSpacing(4);
            Label fleetLabel = new Label(getStringMessages().fleet() + " ");
            fleetLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            hPanel.add(fleetLabel);
            final TextBox nameBox = createNameBox(fleet.getName());
            nameBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    fleet.setName(nameBox.getText());
                }
            });
            nameBox.ensureDebugId("NameTextBox");
            hPanel.add(nameBox);

            final IntegerBox orderNoBox = createOrderNoBox(fleet.getOrderNo());
            orderNoBox.ensureDebugId("OrderNoIntegerBox");
            orderNoBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    fleet.setOrderNo(orderNoBox.getValue());
                }
            });

            final ListBox colorListBox = createColorListBox(nameBox, orderNoBox, fleet.getColor());
            colorListBox.ensureDebugId("ColorListBox");
            colorListBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    fleet.setColor(getSelectedColor(colorListBox));
                }
            });
            hPanel.add(new Label(getStringMessages().color() + " "));
            hPanel.add(colorListBox);
            hPanel.add(new Label(getStringMessages().rank() + " "));
            hPanel.add(orderNoBox);
            return hPanel;
        }

        private IntegerBox createOrderNoBox(final int orderNo) {
            IntegerBox orderNoBox = new IntegerBox();
            orderNoBox.setVisibleLength(3);
            orderNoBox.setValue(orderNo);
            return orderNoBox;
        }

        private TextBox createNameBox(final String name) {
            final TextBox nameBox = new TextBox();
            nameBox.setText(name);
            return nameBox;
        }

        private ListBox createColorListBox(final TextBox nameBox, final IntegerBox orderNoBox, final Color color) {
            final ListBox colorListBox = new ListBox(false);
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
            int index = 0;
            int i = 0;
            for (FleetColors value : FleetColors.values()) {
                i++;
                colorListBox.addItem(value.name());
                if (value.getColor().equals(color)) {
                    index = i;
                }
            }
            colorListBox.setSelectedIndex(index);
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

}
