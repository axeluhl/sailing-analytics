package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Suggest oracle for use in {@link CourseTemplateEditDialog} which oracles the visible mark templates.
 */
public class MarkTemplateSuggestOracle extends AbstractListSuggestOracle<MarkTemplateDTO> {

    private final Collection<MarkTemplateDTO> allTemplates = new ArrayList<>();
    private final SailingServiceAsync sailingServiceAsync;
    private final StringMessages stringMessages;

    public MarkTemplateSuggestOracle(final SailingServiceAsync sailingServiceAsync,
            final StringMessages stringMessages) {
        this.sailingServiceAsync = sailingServiceAsync;
        this.stringMessages = stringMessages;
        refresh();
    }

    public void refresh() {
        sailingServiceAsync.getMarkTemplates(new AsyncCallback<List<MarkTemplateDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadMarkTemplates());
            }

            @Override
            public void onSuccess(List<MarkTemplateDTO> result) {
                allTemplates.clear();
                Util.addAll(result, allTemplates);
                setSelectableValues(allTemplates);
            }
        });
    }

    /**
     * Clears the oracle suggestions, adds all existing templates and finally removes the existing templates from the
     * oracle.
     */
    public void resetAndRemoveExistingTemplates(Iterable<String> existingTemplates) {
        Collection<MarkTemplateDTO> templates = new ArrayList<>(allTemplates);
        templates = templates.stream().filter(u -> !Util.contains(existingTemplates, u.getName()))
                .collect(Collectors.toList());
        super.setSelectableValues(templates);
    }

    /**
     * @returns a {@link MarkTemplateDTO}-object from the current selectable values of this oracle, which is associated
     *          with the given template name.
     */
    public MarkTemplateDTO fromString(final String templateName) {
        if (this.getSelectableValues() == null) {
            throw new NullPointerException("Mark Templates are not loaded yet or could not be loaded.");
        }

        for (MarkTemplateDTO template : this.getSelectableValues()) {
            if (template.getName().equals(templateName)) {
                return template;
            }
        }
        return null;
    }

    @Override
    protected String createSuggestionKeyString(MarkTemplateDTO value) {
        return value.getName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(MarkTemplateDTO value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(MarkTemplateDTO value) {
        return this.getSelectableValues().stream().map(r -> createSuggestionKeyString(r)).collect(Collectors.toList());
    }
}
