package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.EVENT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.Displayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.TableWrapperWithMultiSelectionAndFilterForSecuredDTO;

public class AIAgentConfigurationPanel extends SimplePanel {
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final StringMessages stringMessages;
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final TableWrapperWithMultiSelectionAndFilterForSecuredDTO<EventDTO, StringMessages, AdminConsoleTableResources> eventsTableWrapper;
    private final Set<EventDTO> selectedEvents;
    private boolean selectionUpdatedAfterEventsHaveLoaded;
    private boolean handleSelectionChangeEvents;

    public AIAgentConfigurationPanel(final Presenter presenter, final StringMessages stringMessages) {
        this.sailingServiceWrite = presenter.getSailingService();
        this.stringMessages = stringMessages;
        this.userService = presenter.getUserService();
        this.errorReporter = presenter.getErrorReporter();
        this.selectedEvents = new HashSet<>();
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, EVENT);
        eventsTableWrapper = new TableWrapperWithMultiSelectionAndFilterForSecuredDTO<EventDTO, StringMessages, AdminConsoleTableResources>(stringMessages, this.errorReporter,
                /* enablePager */ true, Optional.of(new EntityIdentityComparator<EventDTO>() {
                    @Override
                    public boolean representSameEntity(EventDTO dto1, EventDTO dto2) {
                        return dto1.getId().equals(dto2.getId());
                    }

                    @Override
                    public int hashCode(EventDTO t) {
                        return t.getId().hashCode();
                    }
                }), GWT.create(AdminConsoleTableResources.class),
                userService, Optional.of(stringMessages.filterEventsByName())) {
            @Override
            protected Iterable<String> getSearchableStrings(EventDTO t) {
                return Arrays.asList(t.getName(), t.getDescription(), t.getVenue().getName());
            }
        };
        eventsTableWrapper.addColumn(EventDTO::getName, stringMessages.name());
        eventsTableWrapper.addColumn(EventDTO::getDescription, stringMessages.description());
        eventsTableWrapper.addColumn(e->e.getVenue().getName(), stringMessages.venue());
        eventsTableWrapper.addColumn(e->e.getId().toString(), stringMessages.id());
        eventsTableWrapper.getSelectionModel().addSelectionChangeHandler(selectionChangeEvent->{
            if (handleSelectionChangeEvents) {
                final Set<EventDTO> newSelection = eventsTableWrapper.getSelectionModel().getSelectedSet();
                final Set<EventDTO> addedToSelection = new HashSet<>(newSelection);
                addedToSelection.removeAll(selectedEvents);
                final Set<EventDTO> removedFromSelection = new HashSet<>(selectedEvents);
                removedFromSelection.removeAll(newSelection);
                selectedEvents.clear();
                selectedEvents.addAll(eventsTableWrapper.getSelectionModel().getSelectedSet());
                addedToSelection.forEach(e->sailingServiceWrite.startAICommentingOnEvent(e.getId(), new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(stringMessages.successfullyActivatedAICommentingForEvent(e.getName()), NotificationType.INFO);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(stringMessages.errorActivatingAICommentingForEvent(e.getName(), caught.getMessage()), NotificationType.ERROR);
                    }
                }));
                removedFromSelection.forEach(e->sailingServiceWrite.stopAICommentingOnEvent(e.getId(), new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(stringMessages.successfullyStoppedAICommentingForEvent(e.getName()), NotificationType.INFO);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(stringMessages.errorStoppingAICommentingForEvent(e.getName(), caught.getMessage()), NotificationType.ERROR);
                    }
                }));
            }
        });
        final Button refresh = buttonPanel.addUnsecuredAction(stringMessages.refresh(), () -> presenter.getEventsRefresher().reloadAndCallFillAll());
        refresh.ensureDebugId("RefreshEventsButton");
        final CaptionPanel captionPanel = new CaptionPanel(stringMessages.selectEventsForWhichToUseAICommenting());
        final VerticalPanel contents = new VerticalPanel();
        contents.add(buttonPanel);
        contents.add(eventsTableWrapper);
        captionPanel.setContentWidget(contents);
        mainPanel.add(captionPanel);
        setWidget(mainPanel);
    }
    
    public Displayer<EventDTO> getEventsDisplayer() {
        return new Displayer<EventDTO>() {
            @Override
            public void fill(Iterable<EventDTO> result) {
                eventsTableWrapper.getFilterPanel().updateAll(result);
                if (!selectionUpdatedAfterEventsHaveLoaded) {
                    selectionUpdatedAfterEventsHaveLoaded = true;
                    sailingServiceWrite.getIdsOfEventsWithAICommenting(new AsyncCallback<List<EventDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.errorTryingToFetchEventsWithAICommentingActive(caught.getMessage()), NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(List<EventDTO> result) {
                            selectedEvents.clear();
                            selectedEvents.addAll(result);
                            handleSelectionChangeEvents = false;
                            result.forEach(e->eventsTableWrapper.getSelectionModel().setSelected(e, true));
                            handleSelectionChangeEvents = true;
                        }
                    });
                }
            }
        };
    }
}
