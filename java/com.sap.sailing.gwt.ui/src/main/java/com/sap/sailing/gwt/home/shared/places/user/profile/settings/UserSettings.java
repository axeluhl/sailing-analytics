package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.common.Util.Function;
import com.sap.sse.common.util.NaturalComparator;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class UserSettings extends Composite implements UserSettingsView {

    private static UserPreferencesUiBinder uiBinder = GWT.create(UserPreferencesUiBinder.class);

    interface UserPreferencesUiBinder extends UiBinder<Widget, UserSettings> {
    }

    interface Style extends CssResource {
        String edgeToEdge();
    }

    @UiField
    Style style;
    @UiField
    SharedResources res;
    @UiField(provided = true)
    final SortedCellTable<UserSettingsEntry> userSettingsTable = new SortedCellTable<>(0, DesignedCellTableResources.INSTANCE);
    private final Column<UserSettingsEntry, String> keyColumn = new Column<UserSettingsEntry, String>(new TextCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return entry.getKey();
        }
    };
    private final Column<UserSettingsEntry, String> documentSettingsIdColumn = new Column<UserSettingsEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return entry.getDocumentSettingsId();
        }
    };

    public UserSettings(UserSettingsView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));

        userSettingsTable.addColumn(keyColumn, "TODO: key", new StringComparator(UserSettingsEntry::getKey), true);
        userSettingsTable.addColumn(documentSettingsIdColumn, "TODO: document settings ID", new StringComparator(UserSettingsEntry::getDocumentSettingsId), true);
        
        presenter.setView(this);
    }

    public void setEdgeToEdge(boolean edgeToEdge) {
        // TODO do we need this?
    }

    @Override
    public void setEntries(List<UserSettingsEntry> entries) {
        userSettingsTable.setPageSize(entries.size());
        userSettingsTable.setList(entries);
    }

    private static final class StringComparator extends InvertibleComparatorAdapter<UserSettingsEntry> {
        private final NaturalComparator innerComnparator = new NaturalComparator();
        private final Function<UserSettingsEntry, String> valueExtractor;

        public StringComparator(Function<UserSettingsEntry, String> valueExtractor) {
            this.valueExtractor = valueExtractor;
        }

        @Override
        public int compare(UserSettingsEntry o1, UserSettingsEntry o2) {
            final String s1 = valueExtractor.get(o1);
            final String s2 = valueExtractor.get(o2);
            return innerComnparator.compare(s1, s2);
        }
    }
}
