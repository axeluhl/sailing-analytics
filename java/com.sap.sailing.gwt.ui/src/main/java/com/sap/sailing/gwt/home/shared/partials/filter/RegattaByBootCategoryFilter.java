package com.sap.sailing.gwt.home.shared.partials.filter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaByBootCategoryFilter extends AbstractSelectionFilter<RegattaMetadataDTO, String> {
    
    private static final String NULL_VALUE = "null";
    private final ListBox selection = new ListBox();
    
    public RegattaByBootCategoryFilter() {
        selection.getElement().setAttribute("dir", "rtl");
        selection.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                selection.getElement().blur();
                notifyValueChangeHandlers();
            }
        });
        initWidget(selection);
    }

    @Override
    protected void addFilterItem(String value) {
        String label = value == null ? StringMessages.INSTANCE.allBoatClasses() : value;
        value = value == null ? NULL_VALUE : value;
        selection.addItem(label, value);
    }

    @Override
    protected String getFilterCriteria(RegattaMetadataDTO object) {
        return object.getBoatCategory();
    }

    @Override
    protected String getSelectedValue() {
        String selectedValue = selection.getSelectedValue();
        return NULL_VALUE.equals(selectedValue) ? null : selectedValue;
    }

}
