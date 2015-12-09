package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.UserProfileDetailsPlace;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class UserProfileDetailsTabView extends Composite implements UserProfileTabView<UserProfileDetailsPlace> {

    interface MyBinder extends UiBinder<Widget, UserProfileDetailsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private UserProfileView.Presenter currentPresenter;
    
    public UserProfileDetailsTabView() {
    }

    @Override
    public Class<UserProfileDetailsPlace> getPlaceClassForActivation() {
        return UserProfileDetailsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(UserProfileDetailsPlace myPlace, AcceptsOneWidget contentArea) {
        initWidget(ourUiBinder.createAndBindUi(this));
        
        // TODO fill contents
        currentPresenter.getUser();
        
        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public UserProfileDetailsPlace placeToFire() {
        return new UserProfileDetailsPlace();
    }

    @Override
    public void setPresenter(UserProfileView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}