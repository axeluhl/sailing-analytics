package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

/**
 * Creates the editable list with a checkbox for every list item
 * 
 * @author Robin Fleige(D067799)
 *
 * @param <ValueType>
 *            The type of the items in the list
 */
public class ExpandedUiWithCheckboxes<ValueType> extends GenericStringListInlineEditorComposite.ExpandedUi<ValueType> {

    private final List<CheckBox> checkBoxes;
    private final String checkBoxText;
    private final List<ChangeHandler> changeHandler = new ArrayList<>();
    static final ListEditorResources ress = GWT.create(ListEditorResources.class);

    /**
     * Creates an editable list with checkboxes for every entry
     * 
     * @param stringMessages
     *            the holder of the used strings
     * @param removeImage
     *            the icon for removing an entry from the editor
     * @param suggestValues
     *            values that will be suggested when entering something into the editor
     * @param placeholderTextForAddTextbox
     * @param textBoxSize
     *            the size if the textbox
     * @param checkBoxText
     *            the text shown next to the checkboxes
     */
    public ExpandedUiWithCheckboxes(StringMessages stringMessages, ImageResource removeImage,
            Iterable<String> suggestValues, String placeholderTextForAddTextbox, int textBoxSize, String checkBoxText) {
        super(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox, textBoxSize);
        this.checkBoxes = new ArrayList<CheckBox>();
        this.checkBoxText = checkBoxText;
        ress.css().ensureInjected();
    }

    /**
     * Used for the css styles
     */
    interface ListEditorResources extends ClientBundle {
        @Source("ListEditor.gss")
        ListEditorCSS css();
    }

    interface ListEditorCSS extends CssResource {
        String checkBoxInvisible();

        String checkBoxNormal();

        String checkBoxError();
    }

    /**
     * Returns the checkboxes,so they can be accessed from another class
     * 
     * @returns the checkboxes
     */
    public List<CheckBox> getCheckBoxes() {
        return checkBoxes;
    }

    /**
     * used to set the checkbox style from another class
     * 
     * @returns String the StyleName of the style
     */
    public static String getInvisibleStyle() {
        return ress.css().checkBoxInvisible();
    }

    public static String getErrorStyle() {
        return ress.css().checkBoxError();
    }

    public static String getNormalStyle() {
        return ress.css().checkBoxNormal();
    }

    /**
     * See {@link ExpandedListEditorUi#addRow(Object)}
     */
    @Override
    protected void addRow(ValueType newValue) {
        super.addRow(newValue);
        final CheckBox checkBox = new CheckBox(checkBoxText);
        checkBox.setStylePrimaryName(getInvisibleStyle());
        checkBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CheckBox source = (CheckBox) event.getSource();
                if (source.getValue()) {
                    source.setStylePrimaryName(getNormalStyle());
                } else {
                    source.setStylePrimaryName(getErrorStyle());
                }
                for (ChangeHandler changeHandler : ExpandedUiWithCheckboxes.this.changeHandler) {
                    changeHandler.onChange(null);
                }
            }
        });
        checkBoxes.add(checkBox);
        expandedValuesGrid.setWidget(expandedValuesGrid.getRowCount() - 1, 2, checkBox);
    }

    /**
     * See {@link ListEditorUiStrategy#initWidget()}
     */
    @Override
    public Widget initWidget() {
        expandedValuesGrid = new Grid(0, 3);
        expandedValuesGrid.ensureDebugId("ExpandedValuesGrid");

        VerticalPanel panel = new VerticalPanel();
        panel.add(createAddWidget());
        panel.add(expandedValuesGrid);
        return panel;

    }

    /**
     * See {@link ListEditorUiStrategy#onRowRemoved(int)}
     */
    @Override
    public void onRowRemoved(int rowIndex) {
        checkBoxes.remove(rowIndex);
    }

    public void addChangeHandler(ChangeHandler handler) {
        changeHandler.add(handler);
    }
}
