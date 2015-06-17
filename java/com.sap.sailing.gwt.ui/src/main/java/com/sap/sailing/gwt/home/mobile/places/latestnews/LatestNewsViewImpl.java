package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class LatestNewsViewImpl extends Composite implements LatestNewsView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, LatestNewsViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true)
    protected UpdatesBox updatesBoxUi;

    public LatestNewsViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        updatesBoxUi = new UpdatesBox(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        UUID uuid = UUID.fromString(presenter.getCtx().getEventId());
        refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(uuid));

    }

    @Override
    public void showNews(List<NewsEntryDTO> news) {
        updatesBoxUi.setData(news);
    }

    

}
