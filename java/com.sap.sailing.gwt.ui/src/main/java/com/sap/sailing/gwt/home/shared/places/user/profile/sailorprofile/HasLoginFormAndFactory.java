package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

public interface HasLoginFormAndFactory {

    void doTriggerLoginForm();

    ClientFactoryWithDispatchAndErrorAndUserService getClientFactory();

}
