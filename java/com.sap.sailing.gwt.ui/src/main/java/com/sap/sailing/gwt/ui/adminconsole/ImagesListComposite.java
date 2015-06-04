package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;

/**
 * /** A composite showing the list of all images
 * 
 * @author Frank Mittag (C5163974)
 */
public class ImagesListComposite extends Composite {
    private final StringMessages stringMessages;

    private CellTable<ImageDTO> imageTable;
    private SingleSelectionModel<ImageDTO> imageSelectionModel;
    private ListDataProvider<ImageDTO> imageListDataProvider;
    private List<ImageDTO> allImages;
    private final Label noImagesLabel;

    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a target=\"_blank\" href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public ImagesListComposite(final StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        allImages = new ArrayList<ImageDTO>();

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        HorizontalPanel eventControlsPanel = new HorizontalPanel();
        eventControlsPanel.setSpacing(5);
        panel.add(eventControlsPanel);

        Button createEventBtn = new Button(stringMessages.actionAddEvent());
        createEventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateEventDialog();
            }
        });
        eventControlsPanel.add(createEventBtn);

        imageListDataProvider = new ListDataProvider<ImageDTO>();
        imageTable = createEventTable();
        imageTable.ensureDebugId("ImagesCellTable");
        imageTable.setVisible(false);

        @SuppressWarnings("unchecked")
        SingleSelectionModel<ImageDTO> multiSelectionModel = (SingleSelectionModel<ImageDTO>) imageTable.getSelectionModel();
        imageSelectionModel = multiSelectionModel;

        imageSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
            }
        });

        panel.add(imageTable);

        noImagesLabel = new Label("No images defined yet.");
        noImagesLabel.ensureDebugId("NoImagesLabel");
        noImagesLabel.setWordWrap(false);
        panel.add(noImagesLabel);

        initWidget(mainPanel);
    }

    private CellTable<ImageDTO> createEventTable() {
        CellTable<ImageDTO> table = new CellTable<ImageDTO>(/* pageSize */10000, tableRes);
        imageListDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        TextColumn<ImageDTO> titleColumn = new TextColumn<ImageDTO>() {
            @Override
            public String getValue(ImageDTO image) {
                return image.getTitle() != null ? image.getTitle() : "";
            }
        };

        TextColumn<ImageDTO> createdAtDateColumn = new TextColumn<ImageDTO>() {
            @Override
            public String getValue(ImageDTO image) {
                return image.getCreatedAtDate().toString();
            }
        };

        SafeHtmlCell tagsCell = new SafeHtmlCell();
        Column<ImageDTO, SafeHtml> tagsColumn = new Column<ImageDTO, SafeHtml>(tagsCell) {
            @Override
            public SafeHtml getValue(ImageDTO image) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int tagsCount = image.getTags().size();
                int i = 1;
                for (String tag : image.getTags()) {
                    builder.appendEscaped(tag);
                    if (i < tagsCount) {
                        builder.appendHtmlConstant(",&nbsp;");
                        // not more than 4 tags per line
                        if (i % 4 == 0) {
                            builder.appendHtmlConstant("<br>");
                        }
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };

        ImagesBarColumn<ImageDTO, ImageConfigImagesBarCell> imageActionColumn = new ImagesBarColumn<ImageDTO, ImageConfigImagesBarCell>(
                new ImageConfigImagesBarCell(stringMessages));
        imageActionColumn.setFieldUpdater(new FieldUpdater<ImageDTO, String>() {
            @Override
            public void update(int index, ImageDTO image, String value) {
                if (ImageConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    removeImage(image);
                } else if (ImageConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditImageDialog(image);
                }
            }
        });

        titleColumn.setSortable(true);
        createdAtDateColumn.setSortable(true);

        table.addColumn(titleColumn, stringMessages.title());
        table.addColumn(createdAtDateColumn, "Created At");
        table.addColumn(tagsColumn, "tags");
        table.addColumn(imageActionColumn, stringMessages.actions());
//        table.setSelectionModel(eventSelectionCheckboxColumn.getSelectionModel(),
//                eventSelectionCheckboxColumn.getSelectionManager());

        table.addColumnSortHandler(getEventTableColumnSortHandler(imageListDataProvider.getList(), titleColumn, createdAtDateColumn));
        table.getColumnSortList().push(createdAtDateColumn);

        return table;
    }

    private ListHandler<ImageDTO> getEventTableColumnSortHandler(List<ImageDTO> eventRecords,
            TextColumn<ImageDTO> titleColumn, TextColumn<ImageDTO> createdAtDateColumn) {
        ListHandler<ImageDTO> result = new ListHandler<ImageDTO>(eventRecords);
        result.setComparator(titleColumn, new Comparator<ImageDTO>() {
            @Override
            public int compare(ImageDTO i1, ImageDTO i2) {
                return new NaturalComparator().compare(i1.getTitle(), i2.getTitle());
            }
        });
        result.setComparator(createdAtDateColumn, new Comparator<ImageDTO>() {
            @Override
            public int compare(ImageDTO e1, ImageDTO e2) {
                int result;
                if (e1.getCreatedAtDate() != null && e2.getCreatedAtDate() != null) {
                    result = e2.getCreatedAtDate().compareTo(e1.getCreatedAtDate());
                } else if (e1.getCreatedAtDate() == null && e2.getCreatedAtDate() != null) {
                    result = 1;
                } else if (e1.getCreatedAtDate() != null && e2.getCreatedAtDate() == null) {
                    result = -1;
                } else {
                    result = 0;
                }
                return result;
            }
        });
        return result;
    }

    private void removeImage(ImageDTO event) {
    }

    private void openCreateEventDialog() {
//        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents),
//                existingLeaderboardGroups, stringMessages, new DialogCallback<ImageDTO>() {
//                    @Override
//                    public void cancel() {
//                    }
//
//                    @Override
//                    public void ok(ImageDTO newEvent) {
//                        createNewEvent(newEvent);
//                    }
//                });
//        dialog.show();
    }

    private void openEditImageDialog(final ImageDTO selectedEvent) {
//        List<ImageDTO> existingEvents = new ArrayList<ImageDTO>(eventListDataProvider.getList());
//        existingEvents.remove(selectedEvent);
//        List<LeaderboardGroupDTO> existingLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
//        Util.addAll(availableLeaderboardGroups, existingLeaderboardGroups);
//        EventEditDialog dialog = new EventEditDialog(selectedEvent, Collections.unmodifiableCollection(existingEvents),
//                existingLeaderboardGroups, stringMessages, new DialogCallback<ImageDTO>() {
//                    @Override
//                    public void cancel() {
//                    }
//
//                    @Override
//                    public void ok(ImageDTO updatedEvent) {
//                        updateEvent(selectedEvent, updatedEvent);
//                    }
//                });
//        dialog.show();
    }

    public void fillImages(List<ImageDTO> images) {
        if (images.isEmpty()) {
            imageTable.setVisible(false);
            noImagesLabel.setVisible(true);
        } else {
            imageTable.setVisible(true);
            noImagesLabel.setVisible(false);
        }

        imageSelectionModel.clear();
        allImages.clear();
        allImages.addAll(images);
    }

    public List<ImageDTO> getAllEvents() {
        return allImages;
    }
}
