package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TagListProvider;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterResources;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterResources.CompetitorFilterCss;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * 
 * @author Julian Rendl (D067890)
 * 
 */
public class TagFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<TagDTO> {

    private final static String LOCAL_STORAGE_TAGS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.tagsFilterSets";

    private final static CompetitorFilterCss css = CompetitorFilterResources.INSTANCE.css();
    private final TextBox searchTextBox;
    private final Button clearTextBoxButton;
    private final Button filterSettingsButton;
    private final TagsFilterSets tagFilterSets;
    private final FlowPanel searchBoxPanel;
    private final StringMessages stringMessages;
    private final TagListProvider tagProvider;

    private FilterSet<TagDTO, FilterWithUI<TagDTO>> lastActiveTagFilterSet;

    public TagFilterPanel(StringMessages stringMessages, TagListProvider tagProvider) {
        css.ensureInjected();
        this.stringMessages = stringMessages;
        this.tagProvider = tagProvider;
        this.setStyleName(css.competitorFilterContainer());

        TagsFilterSets loadedTagsFilterSets = loadTagsFilterSets();
        if (loadedTagsFilterSets != null) {
            tagFilterSets = loadedTagsFilterSets;
            tagProvider.setTagsFilterSet(tagFilterSets.getActiveFilterSetWithGeneralizedType());
        } else {
            tagFilterSets = createAndAddDefaultTagsFilter();
            storeTagsFilterSets(tagFilterSets);
        }

        Button submitButton = new Button();
        submitButton.setStyleName(css.button());
        submitButton.addStyleName(css.searchButton());
        submitButton.addStyleName(css.searchButtonBackgroundImage());

        searchTextBox = new TextBox();
        searchTextBox.getElement().setAttribute("placeholder", stringMessages.tagSearchTags());
        searchTextBox.addKeyUpHandler(this);
        searchTextBox.setStyleName(css.searchInput());

        clearTextBoxButton = new Button();
        clearTextBoxButton.setStyleName(css.button());
        clearTextBoxButton.addStyleName(css.clearButton());
        clearTextBoxButton.addStyleName(css.clearButtonBackgroundImage());
        clearTextBoxButton.addStyleName(css.hiddenButton());
        clearTextBoxButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });

        filterSettingsButton = new Button("");
        filterSettingsButton.setStyleName(css.button());
        filterSettingsButton.addStyleName(css.filterButton());
        filterSettingsButton.setTitle(stringMessages.tagsFilter());
        filterSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
        filterSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showEditTagsFiltersDialog();
            }
        });

        searchBoxPanel = new FlowPanel();
        searchBoxPanel.setStyleName(css.searchBox());
        searchBoxPanel.add(submitButton);
        searchBoxPanel.add(searchTextBox);
        searchBoxPanel.add(clearTextBoxButton);
        add(searchBoxPanel);
        add(filterSettingsButton);
    }

    private void showEditTagsFiltersDialog() {
        TagsFilterSetsDialog tagsFilterSetsDialog = new TagsFilterSetsDialog(tagFilterSets, stringMessages,
                new DialogCallback<TagsFilterSets>() {
                    @Override
                    public void ok(final TagsFilterSets newTagsFilterSets) {
                        tagFilterSets.getFilterSets().clear();
                        tagFilterSets.getFilterSets().addAll(newTagsFilterSets.getFilterSets());
                        tagFilterSets.setActiveFilterSet(newTagsFilterSets.getActiveFilterSet());

                        tagProvider.setTagsFilterSet(newTagsFilterSets.getActiveFilterSetWithGeneralizedType());
                        tagProvider.updateFilteredTags();
                        tagProvider.refresh();
                        updateTagsFilterControlState(newTagsFilterSets);
                        storeTagsFilterSets(newTagsFilterSets);
                    }

                    @Override
                    public void cancel() {
                    }

                });

        tagsFilterSetsDialog.show();
    }

    /**
     * Updates the tags filter checkbox state by setting its check mark and updating its label according to the current
     * filter selected
     */
    private void updateTagsFilterControlState(TagsFilterSets filterSets) {
        String tagsFilterTitle = stringMessages.tagsFilter();
        FilterSet<TagDTO, FilterWithUI<TagDTO>> activeFilterSet = filterSets.getActiveFilterSet();
        if (activeFilterSet != null) {
            if (lastActiveTagFilterSet == null) {
                filterSettingsButton.removeStyleName(css.filterInactiveButtonBackgroundImage());
                filterSettingsButton.addStyleName(css.filterActiveButtonBackgroundImage());
            }
            lastActiveTagFilterSet = activeFilterSet;
        } else {
            if (lastActiveTagFilterSet != null) {
                filterSettingsButton.removeStyleName(css.filterActiveButtonBackgroundImage());
                filterSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
            }
            lastActiveTagFilterSet = null;
        }
        if (lastActiveTagFilterSet != null) {
            filterSettingsButton.setTitle(tagsFilterTitle + " (" + lastActiveTagFilterSet.getName() + ")");
        } else {
            filterSettingsButton.setTitle(tagsFilterTitle);
        }
    }

    private TagsFilterSets loadTagsFilterSets() {
        TagsFilterSets result = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            try {
                String jsonAsLocalStore = localStorage.getItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY);
                if (jsonAsLocalStore != null && !jsonAsLocalStore.isEmpty()) {
                    TagsFilterSetsJsonDeSerializer deserializer = new TagsFilterSetsJsonDeSerializer();
                    JSONValue value = JSONParser.parseStrict(jsonAsLocalStore);
                    if (value.isObject() != null) {
                        result = deserializer.deserialize((JSONObject) value);
                    }
                }
            } catch (Exception e) {
                // exception during loading of tag filters from local storage
            }
        }
        return result;
    }

    private void storeTagsFilterSets(TagsFilterSets newTagsFilterSets) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            // delete old value
            localStorage.removeItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY);

            // store the tags filter set
            TagsFilterSetsJsonDeSerializer serializer = new TagsFilterSetsJsonDeSerializer();
            JSONObject jsonObject = serializer.serialize(newTagsFilterSets);
            localStorage.setItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, jsonObject.toString());
        }
    }

    private TagsFilterSets createAndAddDefaultTagsFilter() {
        TagsFilterSets filterSets = new TagsFilterSets();

        //TODO add standard filters here

        return filterSets;
    }

    private void clearSelection() {
        searchTextBox.setText("");
        clearTextBoxButton.addStyleName(css.hiddenButton());
        onKeyUp(null);
    }

    @Override
    public boolean matches(TagDTO object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public FilterWithUI<TagDTO> copy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        // TODO Auto-generated method stub

    }
}