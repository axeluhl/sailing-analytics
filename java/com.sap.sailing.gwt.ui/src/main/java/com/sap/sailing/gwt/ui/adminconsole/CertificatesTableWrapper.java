package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.ui.client.UserService;

public class CertificatesTableWrapper<S extends RefreshableSelectionModel<ORCCertificate>> extends TableWrapper<ORCCertificate, S> {

    private final LabeledAbstractFilterablePanel<ORCCertificate> filterField;

    public CertificatesTableWrapper(SailingServiceAsync sailingService, final UserService userService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager, int pagingSize) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager, pagingSize, new EntityIdentityComparator<ORCCertificate>() {
            @Override
            public boolean representSameEntity(ORCCertificate cert1, ORCCertificate cert2) {
                return cert1.getId().equals(cert2.getId());
            }
            @Override
            public int hashCode(ORCCertificate cert) {
                return cert.getId().hashCode();
            }
        });
        ListHandler<ORCCertificate> certificateColumnListHandler = getColumnSortHandler();
        TextColumn<ORCCertificate> certificateIdColumn = new TextColumn<ORCCertificate>() {
            @Override
            public String getValue(ORCCertificate certificate) {
                return certificate.getId();
            }
        };
        certificateIdColumn.setSortable(true);
        certificateColumnListHandler.setComparator(certificateIdColumn, new Comparator<ORCCertificate>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(ORCCertificate o1, ORCCertificate o2) {
                return comparator.compare(o1.getId(), o2.getId());
            }
        });
        TextColumn<ORCCertificate> boatClassColumn = new TextColumn<ORCCertificate>() {
            @Override
            public String getValue(ORCCertificate certificate) {
                return certificate.getBoatclass() != null ? certificate.getBoatclass() : "";
            }
        };
        boatClassColumn.setSortable(true);
        certificateColumnListHandler.setComparator(boatClassColumn, new Comparator<ORCCertificate>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(ORCCertificate o1, ORCCertificate o2) {
                return comparator.compare(o1.getBoatclass(), o2.getBoatclass());
            }
        });
        TextColumn<ORCCertificate> boatNameColumn = new TextColumn<ORCCertificate>() {
            @Override
            public String getValue(ORCCertificate certificate) {
                return certificate.getBoatName() != null ? certificate.getBoatName() : "";
            }
        };
        boatNameColumn.setSortable(true);
        certificateColumnListHandler.setComparator(boatNameColumn, new Comparator<ORCCertificate>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(ORCCertificate o1, ORCCertificate o2) {
                return comparator.compare(o1.getBoatName(), o2.getBoatName());
            }
        });
        Column<ORCCertificate, SafeHtml> sailIdColumn = new Column<ORCCertificate, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(ORCCertificate certificate) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(certificate.getSailnumber());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);
        certificateColumnListHandler.setComparator(sailIdColumn, new Comparator<ORCCertificate>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(ORCCertificate o1, ORCCertificate o2) {
                return comparator.compare(o1.getSailnumber(), o2.getSailnumber());
            }
        });
        Column<ORCCertificate, SafeHtml> gphColumn = new Column<ORCCertificate, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(ORCCertificate certificate) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(Util.padPositiveValue(certificate.getGPHInSecondsToTheMile(),
                        /* digitsLeftOfDecimal */ 3, /* digitsRightOfDecimal */ 1, /* round */ true));
                return sb.toSafeHtml();
            }
        };
        gphColumn.setSortable(true);
        certificateColumnListHandler.setComparator(gphColumn, new Comparator<ORCCertificate>() {
            @Override
            public int compare(ORCCertificate o1, ORCCertificate o2) {
                return Double.compare(o1.getGPHInSecondsToTheMile(), o2.getGPHInSecondsToTheMile());
            }
        });
        Column<ORCCertificate, SafeHtml> issuingDateColumn = new Column<ORCCertificate, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(ORCCertificate certificate) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(certificate.getIssueDate().toString());
                return sb.toSafeHtml();
            }
        };
        issuingDateColumn.setSortable(true);
        final Comparator<TimePoint> timePointComparator = Comparator.naturalOrder();
        final Comparator<ORCCertificate> validCertificateComparator = (c1, c2)->timePointComparator.compare(c1.getIssueDate(), c2.getIssueDate());
        certificateColumnListHandler.setComparator(issuingDateColumn, Comparator.nullsLast(validCertificateComparator));
        filterField = new LabeledAbstractFilterablePanel<ORCCertificate>(new Label(stringMessages.filterCertificates()),
                new ArrayList<ORCCertificate>(), dataProvider, stringMessages) {
            @Override
            public Iterable<String> getSearchableStrings(ORCCertificate certificate) {
                List<String> string = new ArrayList<String>();
                string.add(certificate.getId());
                string.add(certificate.getSailnumber());
                string.add(certificate.getBoatclass());
                string.add(certificate.getBoatName());
                return string;
            }

            @Override
            public AbstractCellTable<ORCCertificate> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        // BoatTable edit features
        final ImagesBarColumn<ORCCertificate, ORCCertificatesConfigImagesBarCell> certificateActionColumn = new ImagesBarColumn<>(
                new ORCCertificatesConfigImagesBarCell(getStringMessages()));
        certificateActionColumn.setFieldUpdater(new FieldUpdater<ORCCertificate, String>() {
            @Override
            public void update(int index, ORCCertificate certificate, String value) {
                if (ORCCertificatesConfigImagesBarCell.ACTION_SHOW.equals(value)) {
                    showCertificate(certificate);
                }
            }
        });
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(certificateColumnListHandler);
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(certificateIdColumn, stringMessages.id());
        table.addColumn(boatClassColumn, stringMessages.boatClass());
        table.addColumn(boatNameColumn, stringMessages.name());
        table.addColumn(gphColumn, "GPH"); // no i18n required
        table.addColumn(issuingDateColumn, stringMessages.certificateIssuingDate());
        table.addColumn(certificateActionColumn, stringMessages.actions());
        table.ensureDebugId("BoatsWithVertificateTable");
    }
    
    protected void showCertificate(ORCCertificate certificate) {
        Window.confirm("Certificate: "+certificate);
        // TODO Implement CertificatesTableWrapper.showCertificate(...)
    }

    public void setCertificates(Collection<ORCCertificate> result) {
        filterField.removeAll();
        addCertificates(result);
    }

    public void addCertificates(Iterable<ORCCertificate> result) {
        final Map<String, ORCCertificate> certificatesById = new HashMap<>();
        for (final ORCCertificate certificate : filterField.getAll()) {
            certificatesById.put(certificate.getId(), certificate);
        }
        for (final ORCCertificate certificate : result) {
            final ORCCertificate existingCertificateWithSameId = certificatesById.get(certificate.getId());
            if (existingCertificateWithSameId != null) {
                filterField.remove(existingCertificateWithSameId); // replace certificates with same ID by removing here and later adding
            }
        }
        filterField.addAll(result);
    }

}
