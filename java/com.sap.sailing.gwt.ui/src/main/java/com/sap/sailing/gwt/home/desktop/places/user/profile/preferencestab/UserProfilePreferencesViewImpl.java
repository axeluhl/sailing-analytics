package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.BoatClassSuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.CompetitorSuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection.NotificationCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {

    interface MyBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) SuggestedMultiSelection<SimpleCompetitorWithIdDTO> favoriteCompetitorsSelctionUi;
    @UiField(provided = true) SuggestedMultiSelection<BoatClassMasterdata> favoriteBoatClassesSelctionUi;
    @UiField DivElement notificationsTextUi;
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        favoriteCompetitorsSelctionUi = new CompetitorDisplayImpl(
                presenter.getFavoriteCompetitorsDataProvider()).selectionUi;
        favoriteBoatClassesSelctionUi = new BoatClassDisplayImpl(
                presenter.getFavoriteBoatClassesDataProvider()).selectionUi;
        initWidget(uiBinder.createAndBindUi(this));
        // TODO hide notificationsTextUi if the user's mail address is already verified
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
        
    private class CompetitorDisplayImpl implements CompetitorSuggestedMultiSelectionDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;
        private final HasEnabled notifyAboutResultsUi;
        
        private CompetitorDisplayImpl(final CompetitorSuggestedMultiSelectionDataProvider dataProvider) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, "TODO Favourite competitors");
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, "TODO Email notification about new results");
            dataProvider.setDisplay(this);
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
    
    private class BoatClassDisplayImpl implements BoatClassSuggestedMultiSelectionDataProvider.Display {
        private final SuggestedMultiSelection<BoatClassMasterdata> selectionUi;
        private final HasEnabled notifyAboutUpcomingRacesUi;
        private final HasEnabled notifyAboutResultsUi;
        
        private BoatClassDisplayImpl(final BoatClassSuggestedMultiSelectionDataProvider dataProvider) {
            selectionUi = SuggestedMultiSelection.forBoatClasses(dataProvider, "TODO Favourite boat classes");
            notifyAboutUpcomingRacesUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutUpcomingRaces(enabled);
                }
            }, "TODO Email notification about upcoming races");
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, "TODO Email notification about new results");
            dataProvider.setDisplay(this);
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