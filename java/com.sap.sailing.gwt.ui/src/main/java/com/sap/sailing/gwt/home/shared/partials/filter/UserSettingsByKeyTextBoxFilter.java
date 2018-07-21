package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Arrays;

import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractKeywordFilter;
import com.sap.sse.common.filter.Filter;

public class UserSettingsByKeyTextBoxFilter extends AbstractTextBoxFilter<UserSettingsEntry, String> {

    public UserSettingsByKeyTextBoxFilter() {
        super(StringMessages.INSTANCE.userSettingsFilter());
    }

    private final UserSettingsByKeyFilter filter = new UserSettingsByKeyFilter();

    @Override
    protected Filter<UserSettingsEntry> getFilter(String searchString) {
        this.filter.setKeywords(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchString));
        return filter;
    }

    private class UserSettingsByKeyFilter extends AbstractKeywordFilter<UserSettingsEntry> {

        @Override
        public Iterable<String> getStrings(UserSettingsEntry userSettingsEntry) {
            return Arrays.asList(userSettingsEntry.getKey());
        }

    }

}
