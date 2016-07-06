package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.preferences.BoatClassNotificationPreference;
import com.sap.sailing.domain.common.preferences.BoatClassNotificationPreferences;
import com.sap.sailing.domain.common.preferences.CompetitorNotificationPreference;
import com.sap.sailing.domain.common.preferences.CompetitorNotificationPreferences;
import com.sap.sailing.domain.common.preferences.NotificationPreferences;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CompetitorSuggestionResult;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorSuggestionAction;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorsAction;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractBoatClassSuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractCompetitorSuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.BoatClassSuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.CompetitorSuggestedMultiSelectionDataProvider;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesPresenter implements UserProfilePreferencesView.Presenter {

    private final UserProfilePreferencesView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private NotificationPreferences notificationPreferences;
    private final CompetitorSuggestedMultiSelectionDataProvider competitorDataProvider =
            new CompetitorSuggestedMultiSelectionDataProviderImpl();
    private final BoatClassSuggestedMultiSelectionDataProvider boatClassDataProvider =
            new BoatClassSuggestedMultiSelectionDataProviderImpl();
            
    public UserProfilePreferencesPresenter(final UserProfilePreferencesView view,
            final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        view.setPresenter(this);
    }
    
    @Override
    public void start() {
        userProfilePresenter.getClientFactory().getUserService().getPreference(NotificationPreferences.PREF_NAME,
                new NotificationPreferences(), new AsyncCallback<NotificationPreferences>() {
            @Override
            public void onFailure(Throwable caught) {
                userProfilePresenter.getClientFactory().createErrorView(
                        "Error while loading notification preferences!", caught);
            }
            
            @Override
            public void onSuccess(NotificationPreferences result) {
                notificationPreferences = result;
                initFavoriteCompetitors(result.getCompetitorPreferences());
                initFavoriteBoatClasses(result.getBoatClassPreferences());
            }
        });
    }
    
    private void initFavoriteCompetitors(CompetitorNotificationPreferences preferences) {
        boolean initialNotifyAboutResults = false;
        List<String> favoriteCompetitorIds = new ArrayList<>();
        for (CompetitorNotificationPreference pref : preferences.getCompetitors()) {
            favoriteCompetitorIds.add(pref.getCompetitorId());
            initialNotifyAboutResults |= pref.isNotifyAboutResults();
        }
        competitorDataProvider.initNotifications(initialNotifyAboutResults);
        userProfilePresenter.getClientFactory().getDispatch().execute(new GetCompetitorsAction(favoriteCompetitorIds),
                new AsyncCallback<SortedSetResult<SimpleCompetitorWithIdDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        userProfilePresenter.getClientFactory()
                                .createErrorView("Error while loading notification preferences!", caught);
                    }

                    @Override
                    public void onSuccess(SortedSetResult<SimpleCompetitorWithIdDTO> result) {
                        competitorDataProvider.initSelectedItems(result.getValues());
                    }
                });
    }
    
    private void initFavoriteBoatClasses(BoatClassNotificationPreferences preferences) {
        boolean initialNotifyAboutResults = false, initialNotifyAboutUpcomingRaces = false;
        List<BoatClassMasterdata> favoriteBoatClasses = new ArrayList<>();
        for (BoatClassNotificationPreference pref : preferences.getBoatClasses()) {
            favoriteBoatClasses.add(pref.getBoatClass());
            initialNotifyAboutUpcomingRaces |= pref.isNotifyAboutUpcomingRaces();
            initialNotifyAboutResults |= pref.isNotifyAboutResults();
        }
        boatClassDataProvider.initSelectedItems(favoriteBoatClasses);
        boatClassDataProvider.initNotifications(initialNotifyAboutUpcomingRaces, initialNotifyAboutResults);
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
    
    @Override
    public BoatClassSuggestedMultiSelectionDataProvider getFavoriteBoatClassesDataProvider() {
        return boatClassDataProvider;
    }
    
    @Override
    public CompetitorSuggestedMultiSelectionDataProvider getFavoriteCompetitorsDataProvider() {
        return competitorDataProvider;
    }
    
    private class BoatClassSuggestedMultiSelectionDataProviderImpl
            extends AbstractBoatClassSuggestedMultiSelectionDataProvider {
        @Override
        public void persist(Collection<BoatClassMasterdata> selectedItems) {
            GWT.debugger();
            List<BoatClassNotificationPreference> preferences = new ArrayList<>();
            for (BoatClassMasterdata boatClass : selectedItems) {
                preferences.add(new BoatClassNotificationPreference(boatClass, 
                        isNotifyAboutUpcomingRaces(), isNotifyAboutResults()));
            }
            notificationPreferences.getBoatClassPreferences().setBoatClasses(preferences);
            userProfilePresenter.getClientFactory().getUserService().setPreference(
                    NotificationPreferences.PREF_NAME, notificationPreferences);
        }
    }
    
    private class CompetitorSuggestedMultiSelectionDataProviderImpl
            extends AbstractCompetitorSuggestedMultiSelectionDataProvider {
        
        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                final SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
            userProfilePresenter.getClientFactory().getDispatch().execute(
                    new GetCompetitorSuggestionAction(queryTokens, limit),
                    new AsyncCallback<CompetitorSuggestionResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error while loading competitor suggestion");
                        }

                        @Override
                        public void onSuccess(CompetitorSuggestionResult result) {
                            callback.setSuggestionItems(result.getValues());
                        }
                    });
        }
        
        @Override
        public void persist(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
            GWT.debugger();
            List<CompetitorNotificationPreference> preferences = new ArrayList<>();
            for (SimpleCompetitorWithIdDTO competitor : selectedItems) {
                preferences.add(new CompetitorNotificationPreference(competitor.getIdAsString(), isNotifyAboutResults()));
            }
            notificationPreferences.getCompetitorPreferences().setCompetitors(preferences);
            userProfilePresenter.getClientFactory().getUserService().setPreference(
                    NotificationPreferences.PREF_NAME, notificationPreferences);
        }
    }
}
