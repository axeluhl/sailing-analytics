package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.TagFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.TagFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.TagFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel used to select and modify tag filter.
 */
public class TagFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<TagDTO> {

    private final static String LOCAL_STORAGE_TAGS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.tagsFilterSets";

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final UserService userService;
    private final TagListProvider tagListProvider;

    private final TagFilterSets tagFilterSets;
    private final TextBox searchTextBox;
    private final Button clearTextBoxButton, filterSettingsButton;
    private final AbstractListFilter<TagDTO> filter;

    private FilterSet<TagDTO, FilterWithUI<TagDTO>> lastActiveTagFilterSet;
    private final TagFilterLabel currentFilter;

    public TagFilterPanel(TaggingPanel taggingPanel) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();
        this.userService = taggingPanel.getUserSerivce();
        this.tagListProvider = taggingPanel.getTagListProvider();

        tagFilterSets = new TagFilterSets();
        setStyleName(style.tagFilterPanel());

        searchTextBox = new TextBox();
        clearTextBoxButton = new Button();
        filterSettingsButton = new Button();
        currentFilter = new TagFilterLabel(taggingPanel);

        loadTagFilterSets();
        filter = new AbstractListFilter<TagDTO>() {
            @Override
            public Iterable<String> getStrings(TagDTO tag) {
                final List<String> result = new ArrayList<>(
                        Arrays.asList(tag.getTag().toLowerCase(), tag.getComment()));
                return result;
            }
        };
        initializeUI();
    }

    private void initializeUI() {
        setStyleName(style.tagFilterPanel());

        Button submitButton = new Button();
        submitButton.setStyleName(style.tagFilterButton());
        submitButton.addStyleName("gwt-Button");
        submitButton.addStyleName(style.tagFilterSearchButton());
        submitButton.addStyleName(style.imageSearch());

        searchTextBox.getElement().setAttribute("placeholder", stringMessages.tagSearchTags());
        searchTextBox.addKeyUpHandler(this);
        searchTextBox.setStyleName(style.tagFilterSearchInput());

        clearTextBoxButton.setStyleName(style.tagFilterButton());
        clearTextBoxButton.addStyleName(style.tagFilterClearButton());
        clearTextBoxButton.addStyleName(style.imageClearSearch());
        clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
        clearTextBoxButton.addStyleName("gwt-Button");
        clearTextBoxButton.addClickHandler(event -> {
            clearSelection();
        });

        filterSettingsButton.setStyleName(style.tagFilterButton());
        filterSettingsButton.addStyleName(style.tagFilterFilterButton());
        filterSettingsButton.addStyleName(style.imageInactiveFilter());
        filterSettingsButton.addStyleName("gwt-Button");
        filterSettingsButton.setTitle(stringMessages.tagsFilter());
        filterSettingsButton.addClickHandler(event -> {
            showFilterDialog();
        });

        Panel searchBoxPanel = new FlowPanel();
        searchBoxPanel.setStyleName(style.tagFilterSearchBox());
        searchBoxPanel.add(submitButton);
        searchBoxPanel.add(searchTextBox);
        searchBoxPanel.add(clearTextBoxButton);

        add(searchBoxPanel);
        add(filterSettingsButton);
        add(currentFilter);
    }

    private void showFilterDialog() {
        TagFilterSetsDialog tagsFilterSetsDialog = new TagFilterSetsDialog(tagFilterSets, stringMessages,
                new DialogCallback<TagFilterSets>() {
                    @Override
                    public void ok(final TagFilterSets newTagFilterSets) {
                        tagFilterSets.getFilterSets().clear();
                        tagFilterSets.getFilterSets().addAll(newTagFilterSets.getFilterSets());
                        tagFilterSets.setActiveFilterSet(newTagFilterSets.getActiveFilterSet());

                        tagListProvider.setCurrentFilterSet(newTagFilterSets.getActiveFilterSetWithGeneralizedType());
                        tagListProvider.updateFilteredTags();
                        tagListProvider.refresh();

                        updateTagFilterControlState(newTagFilterSets);
                        if (userService.getCurrentUser() != null) {
                            storeTagFilterSets(newTagFilterSets);
                        }
                        taggingPanel.updateContent();
                    }

                    @Override
                    public void cancel() {
                    }
                }, userService);

        tagsFilterSetsDialog.show();
    }

    /**
     * Updates the tags filter checkbox state by setting its check mark and updating its label according to the current
     * filter selected
     */
    private void updateTagFilterControlState(TagFilterSets filterSets) {
        String tagsFilterTitle = stringMessages.tagsFilter();
        FilterSet<TagDTO, FilterWithUI<TagDTO>> activeFilterSet = filterSets.getActiveFilterSet();
        if (activeFilterSet != null) {
            if (lastActiveTagFilterSet == null) {
                filterSettingsButton.removeStyleName(style.imageInactiveFilter());
                filterSettingsButton.addStyleName(style.imageActiveFilter());
            }
            lastActiveTagFilterSet = activeFilterSet;
        } else {
            if (lastActiveTagFilterSet != null) {
                filterSettingsButton.removeStyleName(style.imageActiveFilter());
                filterSettingsButton.addStyleName(style.imageInactiveFilter());
            }
            lastActiveTagFilterSet = null;
        }

        if (lastActiveTagFilterSet != null) {
            filterSettingsButton.setTitle(tagsFilterTitle + " (" + lastActiveTagFilterSet.getName() + ")");
        } else {
            filterSettingsButton.setTitle(tagsFilterTitle);
        }
    }

    /*
     * loads users Tag Filter Sets from server and stores it into tagFilterSets
     */
    protected void loadTagFilterSets() {
        if (userService.getCurrentUser() != null) {
            userService.getPreference(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    // do nothing
                }

                @Override
                public void onSuccess(String result) {
                    tagFilterSets.removeAllFilterSets();
                    if (result != null && !result.isEmpty()) {
                        final TagFilterSetsJsonDeSerializer deserializer = new TagFilterSetsJsonDeSerializer();
                        final JSONValue value = JSONParser.parseStrict(result);
                        if (value.isObject() != null) {
                            for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : deserializer
                                    .deserialize((JSONObject) value).getFilterSets()) {
                                tagFilterSets.addFilterSet(filterSet);
                            }
                            tagListProvider.setCurrentFilterSet(tagFilterSets.getActiveFilterSetWithGeneralizedType());
                        }
                    } else {
                        tagListProvider.setCurrentFilterSet(null);
                    }
                }
            });
        } else {
            tagFilterSets.removeAllFilterSets();
            tagListProvider.setCurrentFilterSet(null);
        }
    }

    private void storeTagFilterSets(TagFilterSets newTagsFilterSets) {
        TagFilterSetsJsonDeSerializer serializer = new TagFilterSetsJsonDeSerializer();
        JSONObject jsonObject = serializer.serialize(newTagsFilterSets);
        userService.setPreference(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, jsonObject.toString(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.tagFilterNotSavable(), NotificationType.WARNING);
            }

            @Override
            public void onSuccess(Void result) {
            }
        });
    }

    private void clearSelection() {
        searchTextBox.setText("");
        clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
        onKeyUp(null);
    }

    private void ensureSetSearchFilter() {
        if (tagListProvider.getTagFilterSet() == null
                || !Util.contains(tagListProvider.getTagFilterSet().getFilters(), this)) {
            FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(getName());
            if (tagListProvider.getTagFilterSet() != null) {
                for (Filter<TagDTO> oldFilter : tagListProvider.getTagFilterSet().getFilters()) {
                    newFilterSetWithThis.addFilter(oldFilter);
                }
            }
            newFilterSetWithThis.addFilter(this);
            tagListProvider.setCurrentFilterSet(newFilterSetWithThis);
        }
    }

    private void removeSearchFilter() {
        if (tagListProvider.getTagFilterSet() != null
                && Util.contains(tagListProvider.getTagFilterSet().getFilters(), this)) {
            FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(
                    tagListProvider.getTagFilterSet().getName());
            for (Filter<TagDTO> oldFilter : tagListProvider.getTagFilterSet().getFilters()) {
                if (oldFilter != this) {
                    newFilterSetWithThis.addFilter(oldFilter);
                }
            }
            tagListProvider.setCurrentFilterSet(newFilterSetWithThis);
        }
    }

    @Override
    public boolean matches(TagDTO tag) {
        final Iterable<String> lowercaseKeywords = Util
                .splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchTextBox.getText().toLowerCase());
        return !Util.isEmpty(filter.applyFilter(lowercaseKeywords, Collections.singleton(tag)));
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String validate(StringMessages stringMessages) {
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
        return null;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        return null;
    }

    /**
     * @param event
     *            ignored; may be <code>null</code>
     */
    @Override
    public void onKeyUp(KeyUpEvent event) {
        String newValue = searchTextBox.getValue();
        if (newValue.trim().isEmpty()) {
            removeSearchFilter();
            clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
        } else {
            if (newValue.length() >= 2) {
                clearTextBoxButton.removeStyleName(style.tagFilterHiddenButton());
                ensureSetSearchFilter();
                tagListProvider.setCurrentFilterSet(tagListProvider.getTagFilterSet()); //
            }
        }
    }
}
