package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableCompetitorSuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

/**
 * Implementation of {@link SharedSailorProfileView} where users can change their preferred selections and
 * notifications.
 */
public class EditSailorProfile extends Composite implements SharedSailorProfileView {

    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, EditSailorProfile> {
    }

    interface Style extends CssResource {
        String edgeToEdge();
    }

    @UiField
    Style style;
    @UiField
    SharedResources res;
    @UiField(provided = true)
    EditableCompetitorSuggestedMultiSelection competitorSelectionUi;
    @UiField
    InlineEditLabel titleUi;

    public EditSailorProfile(SharedSailorProfileView.Presenter presenter, FlagImageResolver flagImageResolver) {
        competitorSelectionUi = new EditableCompetitorSuggestedMultiSelection(presenter, flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        // TODO hide notificationsTextUi if the user's mail address is already verified
    }

    public void setEdgeToEdge(boolean edgeToEdge) {
        competitorSelectionUi.setStyleName(style.edgeToEdge(), edgeToEdge);
        competitorSelectionUi.getElement().getParentElement().removeClassName(res.mediaCss().column());
    }

    public void setEntry(SailorProfileEntry entry) {
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        titleUi.setText(entry.getName());
    }
}
