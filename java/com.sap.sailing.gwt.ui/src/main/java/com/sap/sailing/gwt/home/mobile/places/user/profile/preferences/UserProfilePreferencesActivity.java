package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
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
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractSuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractSuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.UserProfilePreferencesPlace;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.client.UserService;

public class UserProfilePreferencesActivity extends AbstractActivity implements UserProfilePreferencesView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    private final UserProfilePreferencesView currentView;;
    private final SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider =
            new SuggestedMultiSelectionCompetitorDataProviderImpl();
    private final SuggestedMultiSelectionBoatClassDataProvider boatClassDataProvider =
            new SuggestedMultiSelectionBoatClassDataProviderImpl();
    
    private NotificationPreferences notificationPreferences;
    
    public UserProfilePreferencesActivity(UserProfilePreferencesPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.currentView = new UserProfilePreferencesViewImpl(this);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setAuthenticationContext(clientFactory.getAuthenticationManager().getAuthenticationContext());
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                currentView.setAuthenticationContext(event.getCtx());
            }
        });
        
        clientFactory.getUserService().getPreference(NotificationPreferences.PREF_NAME,
                new NotificationPreferences(), new AsyncCallback<NotificationPreferences>() {
            @Override
            public void onFailure(Throwable caught) {
                        clientFactory.createErrorView("Error while loading notification preferences!", caught);
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
        clientFactory.getDispatch().execute(new GetCompetitorsAction(favoriteCompetitorIds),
                new AsyncCallback<SortedSetResult<SimpleCompetitorWithIdDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        clientFactory.createErrorView("Error while loading notification preferences!", caught);
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
    
    private void persistNotificationPreferences() {
        UserService userService = clientFactory.getUserService();
        userService.setPreference(NotificationPreferences.PREF_NAME, notificationPreferences);
    }
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getNavigator().getSignInNavigation().goToPlace();
    }

    @Override
    public SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider() {
        return boatClassDataProvider;
    }

    @Override
    public SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider() {
        return competitorDataProvider;
    }
    
    private class SuggestedMultiSelectionBoatClassDataProviderImpl
            extends AbstractSuggestedMultiSelectionBoatClassDataProvider {
        @Override
        public void persist(Collection<BoatClassMasterdata> selectedItems) {
            List<BoatClassNotificationPreference> preferences = new ArrayList<>();
            for (BoatClassMasterdata boatClass : selectedItems) {
                preferences.add(new BoatClassNotificationPreference(boatClass, 
                        isNotifyAboutUpcomingRaces(), isNotifyAboutResults()));
            }
            notificationPreferences.getBoatClassPreferences().setBoatClasses(preferences);
            UserProfilePreferencesActivity.this.persistNotificationPreferences();
        }
    }
        
    private class SuggestedMultiSelectionCompetitorDataProviderImpl
        extends AbstractSuggestedMultiSelectionCompetitorDataProvider {
    
        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                final SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
            clientFactory.getDispatch().execute(new GetCompetitorSuggestionAction(queryTokens, limit),
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
            List<CompetitorNotificationPreference> preferences = new ArrayList<>();
            for (SimpleCompetitorWithIdDTO competitor : selectedItems) {
                preferences.add(new CompetitorNotificationPreference(
                        competitor.getIdAsString(), isNotifyAboutResults()));
            }
            notificationPreferences.getCompetitorPreferences().setCompetitors(preferences);
            UserProfilePreferencesActivity.this.persistNotificationPreferences();
        }
    }
}
