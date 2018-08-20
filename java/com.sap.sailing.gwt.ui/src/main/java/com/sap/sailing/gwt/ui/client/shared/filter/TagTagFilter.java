package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * A filter filtering tags by their tag attribute
 * 
 * @author Julian Rendl(D067890)
 * 
 */
public class TagTagFilter extends AbstractTextFilter<TagDTO> implements FilterWithUI<TagDTO> {
    public static final String FILTER_NAME = "Tag";

    public TagTagFilter() {
    }

    @Override
    public boolean matches(TagDTO tag) {
        boolean result = false;
        if (value != null && operator != null) {
            switch (operator.getOperator()) {
            case Contains:
                if (tag.getTag().contains(value)) {
                    result = true;
                }
                break;
            case Equals:
                if (tag.getTag().equals(value)) {
                    result = true;
                }
                break;
            case NotContains:
                if (!tag.getTag().contains(value)) {
                    result = true;
                }
                break;
            case NotEqualTo:
                if (!tag.getTag().equals(value)) {
                    result = true;
                }
                break;
            case EndsWith:
                if (tag.getTag().endsWith(value)) {
                    result = true;
                }
                break;
            case StartsWith:
                if (tag.getTag().startsWith(value)) {
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
    public TagTagFilter copy() {
        TagTagFilter result = new TagTagFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public String getName() {
        if (this.getOperator() != null) {
            return FILTER_NAME + " " + this.getOperator().getName() + " " + this.getValue();
        }
        return FILTER_NAME + " " + this.getValue();
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.tag();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return "Top " + this.getValue() + " " + stringMessages.tag();
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
    public FilterUIFactory<TagDTO> createUIFactory() {
        return new TagTagFilterUIFactory(this);
    }
}
