package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * A dialog to create, edit and delete {@link TagFilterSets}
 */
public class TagFilterSetsDialog extends DataEntryDialog<TagFilterSets> {
    private final TagFilterSets tagFilterSets;
    private final StringMessages stringMessages;

    private final Button addFilterSetButton;
    private Grid tagFilterSetsGrid;
    private VerticalPanel mainPanel;

    private final List<String> availableTagFilterNames;

    private final List<RadioButton> activeFilterSetRadioButtons;
    private final List<Button> editFilterSetButtons;
    private final List<Button> deleteFilterSetButtons;
    private final List<FilterSet<TagDTO, FilterWithUI<TagDTO>>> filterSets;
    private final String ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME = "ActiveFilterSetRB";
    private final String filterNothingFiltersetName;
    private final UserService userService;

    protected static class TagsFilterSetsValidator implements Validator<TagFilterSets> {
        public TagsFilterSetsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(TagFilterSets tagsFilterSets) {
            return null;
        }
    }

    public TagFilterSetsDialog(TagFilterSets tagFilterSets, StringMessages stringMessages,
            DialogCallback<TagFilterSets> callback, UserService userService) {
        super(stringMessages.tagsFilter(), null, stringMessages.ok(), stringMessages.cancel(),
                new TagsFilterSetsValidator(), callback);
        this.userService = userService;
        this.tagFilterSets = tagFilterSets;
        this.stringMessages = stringMessages;
        filterNothingFiltersetName = stringMessages.filterNothing();
        tagFilterSetsGrid = new Grid(0, 0);
        activeFilterSetRadioButtons = new ArrayList<RadioButton>();
        editFilterSetButtons = new ArrayList<Button>();
        deleteFilterSetButtons = new ArrayList<Button>();
        filterSets = new ArrayList<>();
        addFilterSetButton = new Button(stringMessages.actionAddFilter());
        availableTagFilterNames = new ArrayList<String>();
        availableTagFilterNames.add(TagTagFilter.FILTER_NAME);
        availableTagFilterNames.add(TagUsernameFilter.FILTER_NAME);
    }

    @Override
    protected Widget getAdditionalWidget() {
        mainPanel = new VerticalPanel();
        if (userService.getCurrentUser() == null) {
            Label notLoggedInLabel = new Label(stringMessages.tagCreateFilterNotLoggedIn());
            notLoggedInLabel.getElement().getStyle().setColor("red");
            mainPanel.add(notLoggedInLabel);
        }
        String headLineText;
        if (tagFilterSets.getFilterSets().size() < 1) {
            headLineText = stringMessages.tagCreateFilterHint();
        } else {
            headLineText = stringMessages.availableFilters();
        }
        mainPanel.add(new Label(headLineText));
        mainPanel.add(tagFilterSetsGrid);
        // create a dummy filter for the "filter nothing" option
        FilterSet<TagDTO, FilterWithUI<TagDTO>> noFilterSet = new FilterSet<>(filterNothingFiltersetName);
        createActiveFilterSetRadioButton(noFilterSet, tagFilterSets.getActiveFilterSet() == null);
        Button noFilterSetEditBtn = createEditFilterSetButton(noFilterSet);
        Button noFilterSetDeleteBtn = createDeleteFilterSetButton(noFilterSet);
        filterSets.add(noFilterSet);
        noFilterSetEditBtn.setVisible(false);
        noFilterSetDeleteBtn.setVisible(false);
        for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : tagFilterSets.getFilterSets()) {
            createActiveFilterSetRadioButton(filterSet, tagFilterSets.getActiveFilterSet() == filterSet);
            createEditFilterSetButton(filterSet);
            createDeleteFilterSetButton(filterSet);
            filterSets.add(filterSet);
        }
        updateTagsFilterSetsGrid(mainPanel);
        mainPanel.add(addFilterSetButton);
        addFilterSetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> existingFilterSetNames = new ArrayList<String>();
                for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : getResult().getFilterSets()) {
                    existingFilterSetNames.add(filterSet.getName());
                }
                CreateTagsFilterSetDialog dialog = new CreateTagsFilterSetDialog(existingFilterSetNames,
                        availableTagFilterNames, stringMessages,
                        new DialogCallback<FilterSet<TagDTO, FilterWithUI<TagDTO>>>() {
                            @Override
                            public void ok(final FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet) {
                                createActiveFilterSetRadioButton(filterSet, false);
                                createEditFilterSetButton(filterSet);
                                createDeleteFilterSetButton(filterSet);
                                filterSets.add(filterSet);
                                updateTagsFilterSetsGrid(mainPanel);
                                validateAndUpdate();
                            }

                            @Override
                            public void cancel() {
                            }
                        });
                dialog.show();
            }
        });

        return mainPanel;
    }

    private RadioButton createActiveFilterSetRadioButton(FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet,
            boolean isActiveFilterSet) {
        RadioButton activeFilterSetRadioButton = createRadioButton(ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME,
                filterSet.getName());
        activeFilterSetRadioButton.setValue(isActiveFilterSet);
        activeFilterSetRadioButtons.add(activeFilterSetRadioButton);
        return activeFilterSetRadioButton;
    }

    /**
     * Creates a Button, which if clicked creates and opens a {@link EditTagsFilterSetDialog}, 
     * with which a user can edit a {@link FilterSet} 
     * @param filterSetToEdit The {@link FilterSet} to edit
     * @return This Button, which now can be placed into the UI
     */
    private Button createEditFilterSetButton(final FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSetToEdit) {
        final Button editFilterSetBtn = new Button(stringMessages.edit());
        final String filterSetToEditName = filterSetToEdit.getName();
        editFilterSetBtn.addStyleName("inlineButton");
        editFilterSetBtn.setVisible(filterSetToEdit.isEditable());
        editFilterSetButtons.add(editFilterSetBtn);
        editFilterSetBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> existingFilterSetNames = new ArrayList<String>();
                for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : getResult().getFilterSets()) {
                    if (!filterSet.getName().equals(filterSetToEditName)) {
                        existingFilterSetNames.add(filterSet.getName());
                    }
                }
                EditTagsFilterSetDialog dialog = new EditTagsFilterSetDialog(filterSetToEdit, availableTagFilterNames,
                        existingFilterSetNames, stringMessages,
                        new DialogCallback<FilterSet<TagDTO, FilterWithUI<TagDTO>>>() {
                            @Override
                            public void ok(final FilterSet<TagDTO, FilterWithUI<TagDTO>> changedFilterSet) {
                                // update the changed filter set
                                int index = -1;
                                for (int i = 0; i < filterSets.size(); i++) {
                                    if (filterSetToEditName.equals(filterSets.get(i).getName())) {
                                        index = i;
                                        break;
                                    }
                                }
                                boolean isActiveFilterSet = activeFilterSetRadioButtons.get(index).getValue();

                                activeFilterSetRadioButtons.remove(index);
                                editFilterSetButtons.remove(index);
                                deleteFilterSetButtons.remove(index);
                                filterSets.remove(index);

                                createActiveFilterSetRadioButton(changedFilterSet, isActiveFilterSet);
                                createEditFilterSetButton(changedFilterSet);
                                createDeleteFilterSetButton(changedFilterSet);
                                filterSets.add(changedFilterSet);

                                updateTagsFilterSetsGrid(mainPanel);
                                validateAndUpdate();
                            }

                            @Override
                            public void cancel() {
                            }
                        });
                dialog.show();
            }
        });
        return editFilterSetBtn;
    }

    /**
     * Creates a Button, which if clicked  deletes a {@link FilterSet} can be deleted
     * @param filterSet The regarding {@link FilterSet} which will get deleted if clicked
     * @return The Button
     */
    private Button createDeleteFilterSetButton(FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet) {
        final Button deleteFilterSetBtn = new Button(stringMessages.delete());
        deleteFilterSetBtn.addStyleName("inlineButton");
        deleteFilterSetBtn.setVisible(filterSet.isEditable());
        deleteFilterSetButtons.add(deleteFilterSetBtn);
        deleteFilterSetBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int index = 0;
                for (Button btn : deleteFilterSetButtons) {
                    if (deleteFilterSetBtn == btn) {
                        break;
                    }
                    index++;
                }
                // in case the filter set to delete is the 'active' one, we set the "Filter nothing" filter set 'active'
                if (activeFilterSetRadioButtons.get(index).getValue()) {
                    activeFilterSetRadioButtons.get(0).setValue(true);
                }
                activeFilterSetRadioButtons.remove(index);
                editFilterSetButtons.remove(index);
                deleteFilterSetButtons.remove(index);
                filterSets.remove(index);
                updateTagsFilterSetsGrid(mainPanel);
                validateAndUpdate();
            }
        });
        return deleteFilterSetBtn;
    }
    
    /**
     * Returns the maintained {@link TagFilterSets}
     */
    @Override
    protected TagFilterSets getResult() {
        TagFilterSets result = new TagFilterSets();
        int filterSetCount = activeFilterSetRadioButtons.size();
        for (int i = 0; i < filterSetCount; i++) {
            FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet = filterSets.get(i);
            boolean isActiveFilterSet = activeFilterSetRadioButtons.get(i).getValue();
            if (!filterSet.getName().equals(filterNothingFiltersetName)) {
                result.addFilterSet(filterSet);
                if (isActiveFilterSet) {
                    result.setActiveFilterSet(filterSet);
                }
            } else {
                if (isActiveFilterSet) {
                    result.setActiveFilterSet(null);
                }
            }
        }

        return result;
    }

    private void updateTagsFilterSetsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(tagFilterSetsGrid);
        parentPanel.remove(tagFilterSetsGrid);

        int filterCount = activeFilterSetRadioButtons.size();
        if (filterCount > 0) {
            tagFilterSetsGrid = new Grid(filterCount, 3);
            tagFilterSetsGrid.setCellSpacing(3);
            for (int i = 0; i < filterCount; i++) {
                tagFilterSetsGrid.setWidget(i, 0, activeFilterSetRadioButtons.get(i));
                tagFilterSetsGrid.setWidget(i, 1, editFilterSetButtons.get(i));
                tagFilterSetsGrid.setWidget(i, 2, deleteFilterSetButtons.get(i));
                tagFilterSetsGrid.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                tagFilterSetsGrid.getCellFormatter().setVerticalAlignment(i, 1, HasVerticalAlignment.ALIGN_MIDDLE);
                tagFilterSetsGrid.getCellFormatter().setVerticalAlignment(i, 2, HasVerticalAlignment.ALIGN_MIDDLE);
            }
        } else {
            tagFilterSetsGrid = new Grid(0, 0);
        }
        parentPanel.insert(tagFilterSetsGrid, widgetIndex);
    }
}
