package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

public class MultiRegattaList extends Composite implements RefreshableWidget<SortedSetResult<RegattaWithProgressDTO>> {

    private static MultiRegattaListUiBinder uiBinder = GWT.create(MultiRegattaListUiBinder.class);

    interface MultiRegattaListUiBinder extends UiBinder<Widget, MultiRegattaList> {
    }

    @UiField FlowPanel regattasContainerUi;
    private final Presenter currentPresenter;
    private final Collection<String> selectableBoatCategories = new TreeSet<>();
    private final boolean showMessageIfEmpty;
    private boolean hasData = false;

    public MultiRegattaList(Presenter presenter, boolean showMessageIfEmpty) {
        this.currentPresenter = presenter;
        this.showMessageIfEmpty = showMessageIfEmpty;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(SortedSetResult<RegattaWithProgressDTO> data) {
        this.setListData(data == null ? Collections.<RegattaWithProgressDTO>emptySet() : data.getValues());
    }

    public void setListData(Collection<RegattaWithProgressDTO> data) {
        regattasContainerUi.clear();
        selectableBoatCategories.clear();
        hasData = !data.isEmpty();
        if (!hasData && showMessageIfEmpty) {
            regattasContainerUi.add(new InfoPlaceholder(StringMessages.INSTANCE.noDataForEvent()));
        }
        for (RegattaWithProgressDTO regattaWithProgress : data) {
            regattasContainerUi.add(new MultiRegattaListItem(regattaWithProgress, currentPresenter));
            Iterable<String> leaderboardGroupNames = regattaWithProgress.getLeaderboardGroupNames();
            if (leaderboardGroupNames != null) {
                Util.addAll(leaderboardGroupNames, selectableBoatCategories);
            }
        }
    }
    
    public Collection<String> getSelectableBoatCategories() {
        return selectableBoatCategories;
    }
    
    public void setVisibleLeaderboardGroup(String leaderboardGroupName) {
        if(!hasData) {
            return;
        }
        for (int i=0; i < regattasContainerUi.getWidgetCount(); i++) {
            MultiRegattaListItem item = (MultiRegattaListItem) regattasContainerUi.getWidget(i);
            item.setVisibilityDependingOnLeaderboardGroup(leaderboardGroupName);
        }
    }

}
