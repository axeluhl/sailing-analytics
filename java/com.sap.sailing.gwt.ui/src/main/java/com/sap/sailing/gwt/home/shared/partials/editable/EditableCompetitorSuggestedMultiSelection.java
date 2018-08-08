package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection.NotificationCallback;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorItemDescription;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public final class EditableCompetitorSuggestedMultiSelection extends Composite {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, EditableCompetitorSuggestedMultiSelection> {
    }

    @UiField
    DivElement parentPanel;

    @UiField(provided = true)
    SuggestedMultiSelection<SimpleCompetitorWithIdDTO> multiSelect;

    @UiField
    SpanElement headerTitleUi;

    @UiField
    FlowPanel itemContainerUi;

    @UiField
    DivElement listContainerUi;

    @UiField
    Button toggleEditButtonUi;

    private final FlagImageResolver flagImageResolver;
    private boolean editMode = false;

    public EditableCompetitorSuggestedMultiSelection(SharedSailorProfileView.Presenter presenter,
            FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
        multiSelect = new CompetitorDisplayImpl(presenter.getFavoriteCompetitorsDataProvider(),
                flagImageResolver).selectionUi;
        initWidget(uiBinder.createAndBindUi(this));
        multiSelect.getElement().removeFromParent();
        headerTitleUi.setInnerText("Competitors");
    }

    public void setEditMode(boolean state) {
        this.editMode = state;
        listContainerUi.removeFromParent();
        multiSelect.getElement().removeFromParent();
        if (state) {
            parentPanel.appendChild(multiSelect.getElement());
        } else {
            parentPanel.appendChild(listContainerUi);
        }
    }

    @UiHandler("toggleEditButtonUi")
    void onEditButtonClicked(ClickEvent event) {
        setEditMode(!editMode);
    }

    private class CompetitorDisplayImpl implements SuggestedMultiSelectionCompetitorDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;
        private final HasEnabled notifyAboutResultsUi;

        private CompetitorDisplayImpl(final SuggestedMultiSelectionCompetitorDataProvider dataProvider,
                FlagImageResolver flagImageResolver) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, StringMessages.INSTANCE.competitors(),
                    flagImageResolver);
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, StringMessages.INSTANCE.notificationAboutNewResults());
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

    public void addListItem(SimpleCompetitorWithIdDTO comp) {
        SuggestedMultiSelectionCompetitorItemDescription item = new SuggestedMultiSelectionCompetitorItemDescription(
                comp, flagImageResolver);
        itemContainerUi.add(item);

        // TODO: move to CSS file
        item.getElement().getStyle().setProperty("borderTop", "1px solid #ddd");
        item.getElement().getStyle().setMarginTop(0.333333333333333333, Unit.EM);
        item.getElement().getStyle().setMarginBottom(0.333333333333333333, Unit.EM);
        item.getElement().getStyle().setMarginLeft(0.333333333333333333, Unit.EM);
        DOM.getChild(item.getElement(), 2).getStyle().setPosition(Position.RELATIVE);
        DOM.getChild(item.getElement(), 2).getStyle().setTop(-0.333333333333333333, Unit.EM);
    }

    public void setSelectedItems(List<SimpleCompetitorWithIdDTO> competitors) {
        multiSelect.setSelectedItems(competitors);
        itemContainerUi.clear();
        competitors.forEach(c -> addListItem(c));
    }

}
