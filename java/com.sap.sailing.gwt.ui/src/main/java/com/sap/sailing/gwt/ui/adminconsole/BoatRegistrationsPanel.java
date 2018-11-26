package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyDisplay;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.security.ui.client.UserService;

/**
 * Shows two boat tables next to each other; the table on the left is that of the "registered" boats, the table
 * on the right is the "pool" of all boats with those already "registered" removed. Between the tables are buttons
 * to move boats left and right, thereby assigning them to the "registered" set or moving them back to the pool.
 * The {@link #getResult result} consists of all {@link BoatDTO boat} in the table of "registered" boats.<p>
 * 
 * @author Frank Mittag
 *
 */
public class BoatRegistrationsPanel extends FlowPanel implements BusyDisplay {
    private final BoatTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> allBoatsTable;
    private final BoatTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> registeredBoatsTable;
    private final ErrorReporter errorReporter;
    private Button registerBtn;
    private Button unregisterBtn;
    private Button addBoatButton;
    private CheckBox showOnlyBoatsOfLogCheckBox;
    private final BusyIndicator busyIndicator;
    private final Runnable validator;
    private final Consumer<AsyncCallback<Collection<BoatDTO>>> registeredBoatsRetriever;

    /**
     * @param boatClass
     *            The <code>boatClass</code> parameter describes the default shown boat class for new boats. The
     *            <code>boatClass</code> parameter is <code>null</code>, if you want to edit a boat or there is no
     *            boat class for the new boat.
     * @param validator
     *            when this panel is used to modify the resulting competitor set, this callback's {@link Runnable#run}
     *            method is invoked if a non-{@code null} value is passed here.
     * @param registeredBoatsRetriever
     *            although declared as a consumer, this is a provider of the set of competitors to be shown in the
     *            "registered" competitors table. Technically, it "consumes" a callback to which to pass the competitors
     *            retrieved as the "registered" ones.
     * @param restrictPoolToLeaderboard
     *            whether the pool of "all" boats is to be restricted to those obtained from the leaderboard, or
     *            to all boats in the server's boat store
     */
    protected BoatRegistrationsPanel(final SailingServiceAsync sailingService, final UserService userService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, boolean editable,
            String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace, String boatClass, Runnable validator,
            Consumer<AsyncCallback<Collection<BoatDTO>>> registeredBoatsRetriever,
            boolean restrictPoolToLeaderboard) {
        this.errorReporter = errorReporter;
        this.validator = validator;
         this.busyIndicator = new SimpleBusyIndicator();
        this.registeredBoatsRetriever = registeredBoatsRetriever;
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        addBoatButton = new Button(stringMessages.add(stringMessages.boat()));
        addBoatButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BoatDTO newBoat = new BoatDTO();
                registeredBoatsTable.openEditBoatDialog(newBoat, boatClass);
            }
        });
        
        final HorizontalPanel boatRegistrationPanel = new HorizontalPanel();
        final CaptionPanel allBoatsPanel = new CaptionPanel(stringMessages.boatPool());
        final CaptionPanel registeredBoatsPanel = new CaptionPanel(stringMessages.registeredBoats());
        allBoatsTable = new BoatTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */true, 20, false);
        registeredBoatsTable = new BoatTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */false,  20, false);
        allBoatsPanel.add(allBoatsTable);
        registeredBoatsPanel.add(registeredBoatsTable);
        VerticalPanel movePanel = new VerticalPanel();
        registerBtn = new Button("<");
        unregisterBtn = new Button(">");
        registerBtn.setEnabled(editable);
        unregisterBtn.setEnabled(editable);
        movePanel.add(registerBtn);
        movePanel.add(unregisterBtn);
        registerBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelected(allBoatsTable, registeredBoatsTable);
            }
        });
        unregisterBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelected(registeredBoatsTable, allBoatsTable);
            }
        });
        boatRegistrationPanel.add(registeredBoatsPanel);
        boatRegistrationPanel.add(movePanel);
        boatRegistrationPanel.setCellVerticalAlignment(movePanel, HasVerticalAlignment.ALIGN_MIDDLE);
        boatRegistrationPanel.add(allBoatsPanel);
        buttonPanel.add(addBoatButton);
        buttonPanel.add(busyIndicator);
        this.add(buttonPanel);
        showOnlyBoatsOfLogCheckBox = new CheckBox(stringMessages.showOnlyBoatsOfLog());
        showOnlyBoatsOfLogCheckBox.setValue(false);
        showOnlyBoatsOfLogCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.confirmLosingBoatEditsWhenTogglingLogBasedView())) {
                    refreshBoats();
                } else {
                    showOnlyBoatsOfLogCheckBox.setValue(!showOnlyBoatsOfLogCheckBox.getValue());
                }
            }
        });
        this.add(showOnlyBoatsOfLogCheckBox);
        this.add(boatRegistrationPanel);
        refreshBoats();
    }
    
    @Override
    public void setBusy(boolean isBusy) {
        busyIndicator.setBusy(isBusy);
    }

    protected void move(BoatTableWrapper<?> from, BoatTableWrapper<?> to, Iterable<BoatDTO> toMove) {
        from.getFilterField().removeAll(toMove);
        to.getFilterField().addAll(toMove);
    }
    
    private void moveSelected(BoatTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> from,
            BoatTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> to) {
        move(from, to, from.getSelectionModel().getSelectedSet());
        validateAndUpdate();
    }

    protected boolean showOnlyBoatsOfLog(){
        return showOnlyBoatsOfLogCheckBox.getValue();
    }

    protected void refreshBoats() {
        registeredBoatsTable.getFilterField().removeAll();
        allBoatsTable.getFilterField().removeAll();
        setRegisterableBoatsAndRegisteredBoats();
    }

    private void setRegisterableBoatsAndRegisteredBoats() {
        allBoatsTable.refreshBoatList(true, new Callback<Iterable<BoatDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<BoatDTO> result) {
                registeredBoatsRetriever.accept(new AsyncCallback<Collection<BoatDTO>>() {
                    @Override
                    public void onSuccess(Collection<BoatDTO> registeredBoats) {
                        moveFromPoolToRegistered(registeredBoats);
                        validateAndUpdate();
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered boats: " + reason.getMessage());
                    }
                });
            }
    
            @Override
            public void onFailure(Throwable reason) {
                errorReporter.reportError("Could not load pool of boats: " + reason.getMessage());
            }
        });
    }

    public void deactivateRegistrationButtons(String tooltip){
        registerBtn.setEnabled(false);
        unregisterBtn.setEnabled(false);
        addBoatButton.setEnabled(false);
        registerBtn.setTitle(tooltip);
        unregisterBtn.setTitle(tooltip);
        addBoatButton.setTitle(tooltip);
        validateAndUpdate();
    }
    
    private void validateAndUpdate() {
        if (validator != null) {
            validator.run();
        }
    }
    
    public void activateRegistrationButtons(){
        registerBtn.setEnabled(true);
        unregisterBtn.setEnabled(true);
        addBoatButton.setEnabled(true);
        registerBtn.setTitle("");
        unregisterBtn.setTitle("");
        addBoatButton.setTitle("");
        validateAndUpdate();
    }

    public Set<BoatDTO> getResult() {
        final Set<BoatDTO> registeredBoats = new HashSet<>();
        Util.addAll(registeredBoatsTable.getAllBoats(), registeredBoats);
        return registeredBoats;
    }

    public void grayOutCompetitorsFromRegistered(List<BoatDTO> boats) {
//        registeredBoatsTable.grayOutCompetitors(boats);
    }

    public void grayOutCompetitorsFromPool(List<BoatDTO> competitors) {
//        allBoatsTable.grayOutCompetitors(competitors);
    }

    public void moveFromPoolToRegistered(Collection<BoatDTO> registeredBoats) {
        move(allBoatsTable, registeredBoatsTable, registeredBoats);
    }
}
