package com.sap.sailing.gwt.home.desktop.places.user.profile.settingstab;

import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.shared.partials.filter.UserSettingsByKeyTextBoxFilter;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.common.Util.Function;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.util.NaturalComparator;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class UserSettings extends Composite implements UserSettingsView {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserSettings> {
    }

    @UiField
    UserSettingsByKeyTextBoxFilter userSettingsFilterUi;
    @UiField
    DivElement tableWrapper;
    @UiField(provided = true)
    final SortedCellTable<UserSettingsEntry> userSettingsTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);
    private final Column<UserSettingsEntry, String> keyColumn = new Column<UserSettingsEntry, String>(new TextCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return entry.getKeyWithoutContext();
        }
    };
    private final Column<UserSettingsEntry, String> documentSettingsIdColumn = new Column<UserSettingsEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return entry.getDocumentSettingsId();
        }
    };
    private final Column<UserSettingsEntry, String> deleteColumn = new Column<UserSettingsEntry, String>(
            new ButtonCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return StringMessages.INSTANCE.remove();
        }
    };
    private final Column<UserSettingsEntry, String> showColumn = new Column<UserSettingsEntry, String>(
            new ButtonCell()) {
        @Override
        public String getValue(UserSettingsEntry entry) {
            return StringMessages.INSTANCE.show();
        }
    };

    public UserSettings(UserSettingsView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));

        deleteColumn.setFieldUpdater(new FieldUpdater<UserSettingsEntry, String>() {
            @Override
            public void update(int index, UserSettingsEntry object, String value) {
                presenter.remove(object);
            }
        });

        showColumn.setFieldUpdater(new FieldUpdater<UserSettingsEntry, String>() {
            @Override
            public void update(int index, UserSettingsEntry object, String value) {
                new SettingsEntryDialog(object, () -> presenter.remove(object)).show();
            }
        });

        userSettingsTable.addColumn(keyColumn, StringMessages.INSTANCE.settingsId(),
                new StringComparator(UserSettingsEntry::getKeyWithoutContext), true);
        userSettingsTable.addColumn(documentSettingsIdColumn, StringMessages.INSTANCE.documentSettingsId(),
                new StringComparator(UserSettingsEntry::getDocumentSettingsId), true);
        showColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        userSettingsTable.addColumn(showColumn, "", null, false);
        deleteColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        userSettingsTable.addColumn(deleteColumn, "", null, false);
        userSettingsFilterUi.addFilterValueChangeHandler(filter -> presenter.updateData());
        presenter.setView(this);
    }

    @Override
    public Filter<UserSettingsEntry> getFilter() {
        return userSettingsFilterUi.getFilter();
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
