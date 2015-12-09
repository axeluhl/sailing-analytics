package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.partials.userprofileheader.UserProfileHeader;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopUserProfileView extends Composite implements UserProfileView<AbstractUserProfilePlace, UserProfileView.Presenter> {
    private static final ApplicationHistoryMapper historyMapper = GWT.<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private UserProfileView.Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopUserProfileView> {
    }

    @UiField StringMessages i18n;
    
    @UiField(provided = true)
    TabPanel<UserProfileView.Presenter> tabPanelUi;
    
    @UiField(provided = true)
    UserProfileHeader headerUi;

    public TabletAndDesktopUserProfileView() {
    }

    @Override
    public void registerPresenter(final UserProfileView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(currentPresenter, historyMapper);
        
        headerUi = new UserProfileHeader();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void navigateTabsTo(AbstractUserProfilePlace place) {
        tabPanelUi.activatePlace(place);
        // TODO implement
//        StringBuilder titleBuilder = new StringBuilder(TextMessages.INSTANCE.sapSailing()).append(" - ");
//
//        titleBuilder.append(currentPresenter.getSeriesDTO().getDisplayName());
//
//        String currentTabTitle = tabPanelUi.getCurrentTabTitle();
//        if (currentTabTitle != null && !currentTabTitle.isEmpty()) {
//            titleBuilder.append(" - ").append(currentTabTitle);
//        }
//        Window.setTitle(titleBuilder.toString());
        Window.setTitle("TODO User Profile title");
    }

    @SuppressWarnings("unchecked")
    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, UserProfileView.Presenter>) e.getSelectedActivity());
    }
    
    @Override
    public void showErrorInCurrentTab(IsWidget errorView) {
        tabPanelUi.overrideCurrentContentInTab(errorView);
    }
}
