package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * A filter filtering tags by the name of their author
 * 
 * @author Julian Rendl (D067890)
 *
 */
public class TagUsernameFilter extends AbstractTextFilter<TagDTO> implements FilterWithUI<TagDTO> {
    public static final String FILTER_NAME = "TagUsernameFilter";

    public TagUsernameFilter() {
    }

    @Override
    public boolean matches(TagDTO tag) {
        boolean result = false;
        if (value != null && operator != null) {
            switch (operator.getOperator()) {
            case Contains:
                if (tag.getUsername().contains(value)) {
                    result = true;
                }
                break;
            case Equals:
                if (tag.getUsername().equals(value)) {
                    result = true;
                }
                break;
            case NotContains:
                if (!tag.getUsername().contains(value)) {
                    result = true;
                }
                break;
            case NotEqualTo:
                if (!tag.getUsername().equals(value)) {
                    result = true;
                }
                break;
            case EndsWith:
                if (tag.getUsername().endsWith(value)) {
                    result = true;
                }
                break;
            case StartsWith:
                if (tag.getUsername().startsWith(value)) {
                    result = true;
                }
                break;
            default:
                throw new RuntimeException("Operator " + operator.getOperator().name() + " is not supported.");
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        // return stringMessages.username();
        // TODO: Proper way to handle multiple instances of string messages?
        return com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE.username();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        // return stringMessages.username();
        // TODO: Proper way to handle multiple instances of string messages?
        return com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE.username();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if (value == null) {
            errorMessage = stringMessages.pleaseEnterAValue();
        }
        return errorMessage;
    }

    @Override
    public TagUsernameFilter copy() {
        TagUsernameFilter result = new TagUsernameFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        return new TagUsernameFilterUIFactory(this);
    }
}
