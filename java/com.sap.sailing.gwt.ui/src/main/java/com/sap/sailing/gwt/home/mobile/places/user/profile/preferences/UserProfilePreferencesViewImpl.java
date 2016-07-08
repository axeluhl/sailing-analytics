package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection.NotificationCallback;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorMobile;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.userprofile.mobile.userheader.UserHeader;

public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private final StringMessages i18n = StringMessages.INSTANCE;
    @UiField(provided = true) final UserHeader userHeaderUi;
    @UiField(provided = true) final AuthorizedContentDecoratorMobile decoratorUi;
    @UiField(provided = true) SuggestedMultiSelection<SimpleCompetitorWithIdDTO> favoriteCompetitorsSelctionUi;
    @UiField(provided = true) SuggestedMultiSelection<BoatClassMasterdata> favoriteBoatClassesSelctionUi;
    @UiField DivElement notificationsTextUi;
    
    public UserProfilePreferencesViewImpl(Presenter presenter) {
        userHeaderUi = new UserHeader(SharedResources.INSTANCE);
        decoratorUi = new AuthorizedContentDecoratorMobile(presenter);
        favoriteCompetitorsSelctionUi = new CompetitorDisplayImpl(
                presenter.getFavoriteCompetitorsDataProvider()).selectionUi;
        favoriteBoatClassesSelctionUi = new BoatClassDisplayImpl(
                presenter.getFavoriteBoatClassesDataProvider()).selectionUi;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        userHeaderUi.setAuthenticationContext(authenticationContext);
        decoratorUi.setAuthenticationContext(authenticationContext);
    }
    
    private class CompetitorDisplayImpl implements SuggestedMultiSelectionCompetitorDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;
        private final HasEnabled notifyAboutResultsUi;
        
        private CompetitorDisplayImpl(final SuggestedMultiSelectionCompetitorDataProvider dataProvider) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, i18n.favouriteCompetitors());
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, i18n.notificationAboutNewResults());
            dataProvider.addDisplay(this);
        }
        
        @Override
        public void setSelectedItems(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }

        @Override
        public void setNotifyAboutResults(boolean notifyAboutResults) {
            notifyAboutResultsUi.setEnabled(notifyAboutResults);
        }
    }
    
    private class BoatClassDisplayImpl implements SuggestedMultiSelectionBoatClassDataProvider.Display {
        private final SuggestedMultiSelection<BoatClassMasterdata> selectionUi;
        private final HasEnabled notifyAboutUpcomingRacesUi;
        private final HasEnabled notifyAboutResultsUi;
        
        private BoatClassDisplayImpl(final SuggestedMultiSelectionBoatClassDataProvider dataProvider) {
            selectionUi = SuggestedMultiSelection.forBoatClasses(dataProvider, i18n.favouriteBoatClasses());
            notifyAboutUpcomingRacesUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutUpcomingRaces(enabled);
                }
            }, i18n.notificationAboutUpcomingRaces());
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, i18n.notificationAboutNewResults());
            dataProvider.addDisplay(this);
        }
        
        @Override
        public void setSelectedItems(Collection<BoatClassMasterdata> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }
        
        @Override
        public void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces) {
            notifyAboutUpcomingRacesUi.setEnabled(notifyAboutUpcomingRaces);
        }
        
        @Override
        public void setNotifyAboutResults(boolean notifyAboutResults) {
            notifyAboutResultsUi.setEnabled(notifyAboutResults);
        }
    }
}
