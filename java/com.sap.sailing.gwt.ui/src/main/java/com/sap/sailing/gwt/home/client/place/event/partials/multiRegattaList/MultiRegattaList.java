package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class MultiRegattaList extends Composite implements RefreshableWidget<SortedSetResult<RegattaWithProgressDTO>> {

    private static MultiRegattaListUiBinder uiBinder = GWT.create(MultiRegattaListUiBinder.class);

    interface MultiRegattaListUiBinder extends UiBinder<Widget, MultiRegattaList> {
    }

    @UiField FlowPanel regattasContainerUi;
    private final Presenter currentPresenter;
    private final Collection<String> selectableBoatCategories = new TreeSet<>();

    public MultiRegattaList(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(SortedSetResult<RegattaWithProgressDTO> data, long nextUpdate, int updateNo) {
        this.setListData(data == null ? Collections.<RegattaWithProgressDTO>emptySet() : data.getValues());
    }

    public void setListData(Collection<RegattaWithProgressDTO> data) {
        regattasContainerUi.clear();
        selectableBoatCategories.clear();
        for (RegattaWithProgressDTO regattaWithProgress : data) {
            regattasContainerUi.add(new MultiRegattaListItem(regattaWithProgress, currentPresenter));
            String boatCategory = regattaWithProgress.getBoatCategory();
            if(boatCategory != null) {
                selectableBoatCategories.add(boatCategory);
            }
        }
    }
    
    public Collection<String> getSelectableBoatCategories() {
        return selectableBoatCategories;
    }
    
    public void setVisibleBoatCategory(String visibleBoatCategory) {
        for (int i=0; i < regattasContainerUi.getWidgetCount(); i++) {
            MultiRegattaListItem item = (MultiRegattaListItem) regattasContainerUi.getWidget(i);
            item.setVisibilityDependingOnBoatCategory(visibleBoatCategory);
        }
    }

}
