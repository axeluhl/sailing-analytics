package com.sap.sailing.gwt.home.client.place.event.partials.listNavigation;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RegattaNavigationResources.LocalCss;
import com.sap.sailing.gwt.home.client.shared.DropdownHandler;

public class DropdownFilter<T> extends Composite {

    private static final LocalCss CSS = RegattaNavigationResources.INSTANCE.css();
    private static ListDropdownFilterUiBinder uiBinder = GWT.create(ListDropdownFilterUiBinder.class);

    interface ListDropdownFilterUiBinder extends UiBinder<Widget, DropdownFilter<?>> {
    }
    
    @UiField DivElement dropdownContainerUi;
    @UiField DivElement currentValueUi;
    @UiField FlowPanel filterItemContainerUi;
    private final DropdownFilterList<T> filterList;
    private final DropdownHandler dropdownHandler;
    private final String nullValueLabel;
    private T selectedFilterValue = null;

    public DropdownFilter(String nullValueLabel, DropdownFilterList<T> filterList) {
        this.nullValueLabel = nullValueLabel;
        this.filterList = filterList;
        CSS.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.dropdownHandler = new ListFilterDropdownHandler();
    }
    
    public void updateFilterValues() {
        filterItemContainerUi.clear();
        DropdownFilterItem filterItemToSelect = addFilterValue(null, nullValueLabel);
        for (T value : filterList.getSelectableValues()) {
            DropdownFilterItem newFilterItem = addFilterValue(value, String.valueOf(value));
            filterItemToSelect = value.equals(selectedFilterValue) ? newFilterItem : filterItemToSelect;
        }
        filterItemToSelect.select();
        setVisible(filterList.getSelectableValues().size() > 1);
    }
    
    private DropdownFilterItem addFilterValue(T value, String label) {
        DropdownFilterItem filterItem = new DropdownFilterItem(value, label);
        filterItemContainerUi.add(filterItem);
        return filterItem;
    }

    public interface DropdownFilterList<T> {
        Collection<T> getSelectableValues();
        void onSelectFilter(T value);
    }
    
    private class ListFilterDropdownHandler extends DropdownHandler {
        private ListFilterDropdownHandler() {
            super(dropdownContainerUi, filterItemContainerUi.getElement());
        }
        @Override
        protected void dropdownStateChanged(boolean dropdownShown) {
            if (dropdownShown) dropdownContainerUi.addClassName(CSS.jsdropdownactive());
            else dropdownContainerUi.removeClassName(CSS.jsdropdownactive());
        }
    }
    
    private class DropdownFilterItem extends Widget {
        private final T value;
        private final String label;
        private DropdownFilterItem(T value, String label) {
            this.value = value;
            this.label = label;
            this.setElement(DOM.createDiv().<DivElement>cast());
            this.getElement().setInnerText(label);
            this.addStyleName(CSS.regattanavigation_filter_dropdown_link());
            this.initClickHandler();
        }
        
        private void initClickHandler() {
            this.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if(LinkUtil.handleLinkClick((Event) event.getNativeEvent())) {
                        event.preventDefault();
                        DropdownFilterItem.this.select();
                        DropdownFilter.this.dropdownHandler.setVisible(false);
                    }
                }
            }, ClickEvent.getType());
        }
        
        private void select() {
            DropdownFilter.this.selectedFilterValue = value;
            DropdownFilter.this.currentValueUi.setInnerText(label);
            for (int i=0; i < filterItemContainerUi.getWidgetCount(); i++) {
                filterItemContainerUi.getWidget(i).removeStyleName(CSS.regattanavigation_filter_dropdown_linkactive());
            }
            this.addStyleName(CSS.regattanavigation_filter_dropdown_linkactive());
            DropdownFilter.this.filterList.onSelectFilter(value);
        }
    }
}
