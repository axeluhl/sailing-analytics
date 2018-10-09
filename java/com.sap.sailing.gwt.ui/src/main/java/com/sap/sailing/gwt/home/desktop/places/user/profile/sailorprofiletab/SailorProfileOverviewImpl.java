package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.NavigatorColumn;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.partials.dialog.ConfirmDialogFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.view.SailorProfileOverview;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/** Sailor Profile Overview where all the sailor profiles are listed */
public class SailorProfileOverviewImpl extends Composite implements SailorProfileOverview {
    private static ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);

    interface MyBinder extends UiBinder<Widget, SailorProfileOverviewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);
    private SailingProfileOverviewPresenter presenter;

    @UiField
    StringMessages i18n;

    @UiField(provided = true)
    final SortedCellTable<SailorProfileDTO> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField
    FlowPanel footerUi;

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        this.presenter = presenter;
        SharedSailorProfileResources.INSTANCE.css().ensureInjected();
        SailorProfileDesktopResources.INSTANCE.css().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        setupTable();
        createFooter();
    }

    @Override
    public void setProfileList(Collection<SailorProfileDTO> entries) {
        sailorProfilesTable.setPageSize(entries.size());
        sailorProfilesTable.setList(entries);
    }

    private void setupTable() {
        sailorProfilesTable.addColumn(profileNameColumn, i18n.profileName());
        sailorProfilesTable.addColumn(badgeColumn, i18n.badges());
        sailorProfilesTable.addColumn(competitorColumn, i18n.competitors());
        sailorProfilesTable.addColumn(boatClassColumn, i18n.boatClasses());
        sailorProfilesTable.addColumn(navigatorColumn);
        sailorProfilesTable.addColumn(removeColumn);

        addWordwrapStyle(profileNameColumn);
        addWordwrapStyle(badgeColumn);
        addWordwrapStyle(competitorColumn);
        addWordwrapStyle(boatClassColumn);

        addButtonStyle(navigatorColumn);
        navigatorColumn.setFieldUpdater((int index, SailorProfileDTO entry, String value) -> presenter
                .getClientFactory().getPlaceController().goTo(getTargetPlace(entry)));

        removeColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        removeColumn.setFieldUpdater((int index, SailorProfileDTO dto, String value) -> ConfirmDialogFactory
                .showConfirmDialog(StringMessages.INSTANCE.sailorProfileRemoveMessage(), new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                        presenter.removeSailorProfile(dto.getKey());
                    }

                    @Override
                    public void cancel() {
                    }
                }));

        sailorProfilesTable.addCellPreviewHandler(e -> {
            /* no navigation for remove column */
            if ("click".equals(e.getNativeEvent().getType()) && e.getColumn() != 5) {
                presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(e.getValue().getKey()));
            }
        });
    }

    private void addWordwrapStyle(Column<SailorProfileDTO, ?> col) {
        col.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().textCellWordWrap() + " "
                + SailorProfileDesktopResources.INSTANCE.css().clickableColumn());
    }

    private void addButtonStyle(Column<SailorProfileDTO, ?> col) {
        col.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell() + " "
                + SailorProfileDesktopResources.INSTANCE.css().clickableColumn());

    }

    private void createFooter() {
        Button addButton = new Button("+ " + i18n.addSailorProfileMessage());
        addButton.addStyleName(SharedResources.INSTANCE.mainCss().buttonprimary());
        addButton.addStyleName(SharedResources.INSTANCE.mainCss().button());
        footerUi.add(addButton);
        addButton.addClickHandler(
                event -> presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(true)));
    }

    private final Column<SailorProfileDTO, String> profileNameColumn = new Column<SailorProfileDTO, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileDTO entry) {
            return entry.getName();
        }
    };
    private final Column<SailorProfileDTO, String> badgeColumn = new Column<SailorProfileDTO, String>(new TextCell()) {
        @Override
        public String getValue(SailorProfileDTO entry) {
            if (entry != null && entry.getBadges() != null) {
                return entry.getBadges().stream().map(BadgeDTO::getName).collect(Collectors.joining(", "));
            }
            return "-";
        }
    };
    private final Column<SailorProfileDTO, SailorProfileDTO> boatClassColumn = new Column<SailorProfileDTO, SailorProfileDTO>(
            new AbstractCell<SailorProfileDTO>() {
                @Override
                public void render(Context context, SailorProfileDTO value, SafeHtmlBuilder sb) {
                    for (BoatClassDTO boatclass : value.getBoatclasses()) {
                        sb.append(SharedSailorProfileResources.TEMPLATES.buildBoatclassIcon(BoatClassImageResolver
                                .getBoatClassIconResource(boatclass.getName()).getSafeUri().asString()));
                    }
                }
            }) {
        @Override
        public SailorProfileDTO getValue(SailorProfileDTO object) {
            return object;
        }
    };

    private final Column<SailorProfileDTO, String> competitorColumn = new Column<SailorProfileDTO, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileDTO entry) {
            if (entry != null && entry.getCompetitors() != null) {
                return entry.getCompetitors().stream().map(c -> c.getName()).collect(Collectors.joining(", "));
            }
            return "-";
        }
    };

    private final Column<SailorProfileDTO, String> navigatorColumn = new NavigatorColumn<SailorProfileDTO>() {
        @Override
        public String getValue(SailorProfileDTO object) {
            return "#" + historyMapper.getToken(getTargetPlace(object));
        }
    };

    private final Column<SailorProfileDTO, String> removeColumn = new Column<SailorProfileDTO, String>(
            new RemoveButtonCell()) {
        @Override
        public String getValue(SailorProfileDTO entry) {
            return "X";
        }
    };

    @Override
    public NeedsAuthenticationContext authentificationContextConsumer() {
        return decoratorUi;
    }

    private SailorProfilePlace getTargetPlace(SailorProfileDTO entry) {
        return new SailorProfilePlace(entry.getKey());
    }

    private class RemoveButtonCell extends ButtonCell {
        @Override
        public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            sb.append(SailorProfileDesktopResources.TEMPLATE.removeButtonCell(data));
        }
    }
}
