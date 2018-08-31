package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.security.ui.client.UserService;

/**
 * Used to display tags in various locations.
 */
public class TagCell extends AbstractCell<TagDTO> {

    public interface TagCellTemplate extends SafeHtmlTemplates {
        @Template("<div class='{0}'><div class='{1}'>{3}</div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cell(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag, SafeHtml createdAt,
                SafeHtml content);

        @Template("<div class='{0}'><div class='{1}'>{3}<div style=\"position: relative\"><button>X</button></div></div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cellRemovable(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content);

        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}</div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml privateCell(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content, SafeUri safeUri);

        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}<div style=\"position: relative\"><button>X</button></div></div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml privateCellRemovable(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content, SafeUri safeUri);

        @Template("<div class='{0}'><img src='{2}'/></div><div class='{1}'>{3}</div>")
        SafeHtml contentWithCommentWithImage(String styleTagImage, String styleTagComment, SafeUri imageURL,
                SafeHtml comment);

        @Template("<div class='{0}'>{1}</div>")
        SafeHtml contentWithCommentWithoutImage(String styleTagComment, SafeHtml comment);

        @Template("<div class='{0}'><img src='{1}'/></div>")
        SafeHtml contentWithoutCommentWithImage(String styleTagImage, SafeUri imageURL);
    }

    private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final UserService userService;
    private final boolean isPreviewCell;

    public TagCell(TaggingPanel taggingPanel, boolean isPreviewCell) {
        super("click");
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();
        this.userService = taggingPanel.getUserSerivce();
        this.isPreviewCell = isPreviewCell;
    }

    @Override
    public void render(Context context, TagDTO tag, SafeHtmlBuilder htmlBuilder) {
        if (tag == null) {
            return;
        }

        SafeHtml safeTag = SafeHtmlUtils.fromString(tag.getTag());
        SafeHtml safeCreated = SafeHtmlUtils.fromString(stringMessages.tagCreated(tag.getUsername(),
                DateTimeFormat.getFormat("E d/M/y, HH:mm").format(tag.getRaceTimepoint().asDate())));
        SafeHtml safeComment = SafeHtmlUtils.fromString(tag.getComment());
        SafeUri trustedImageURL = UriUtils.fromTrustedString(tag.getImageURL());

        SafeUri safeIsPrivateImageUri = resources.privateIcon().getSafeUri();

        SafeHtml content = SafeHtmlUtils.EMPTY_SAFE_HTML;

        if (!tag.getComment().isEmpty() && tag.getImageURL().isEmpty()) {
            content = tagCellTemplate.contentWithCommentWithoutImage(style.tagCellComment(), safeComment);
        } else if (tag.getComment().isEmpty() && !tag.getImageURL().isEmpty()) {
            content = tagCellTemplate.contentWithoutCommentWithImage(style.tagCellImage(), trustedImageURL);
        } else if (!tag.getComment().isEmpty() && !tag.getImageURL().isEmpty()) {
            content = tagCellTemplate.contentWithCommentWithImage(style.tagCellImage(), style.tagCellComment(),
                    trustedImageURL, safeComment);
        }

        SafeHtml cell;
        // TODO: As soon as permission-vertical branch got merged into master, apply
        // new permission system at this if-statement and remove this old way of
        // checking for permissions. (see bug 4104, comment 9)
        // functionality: Check if user has the permission to delete this tag.
        if (!isPreviewCell && userService.getCurrentUser() != null
                && (tag.getUsername().equals(userService.getCurrentUser().getName())
                        || userService.getCurrentUser().hasRole("admin"))) {
            if (tag.isVisibleForPublic()) {
                cell = tagCellTemplate.cellRemovable(style.tagCell(), style.tagCellHeading(), style.tagCellCreated(),
                        safeTag, safeCreated, content);
            } else {
                cell = tagCellTemplate.privateCellRemovable(style.tagCell(), style.tagCellHeading(),
                        style.tagCellCreated(), safeTag, safeCreated, content, safeIsPrivateImageUri);
            }

        } else {
            if (tag.isVisibleForPublic()) {
                cell = tagCellTemplate.cell(style.tagCell(), style.tagCellHeading(), style.tagCellCreated(), safeTag,
                        safeCreated, content);
            } else {
                cell = tagCellTemplate.privateCell(style.tagCell(), style.tagCellHeading(), style.tagCellCreated(),
                        safeTag, safeCreated, content, safeIsPrivateImageUri);
            }
        }
        htmlBuilder.append(cell);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, TagDTO tag, NativeEvent event,
            ValueUpdater<TagDTO> valueUpdater) {
        super.onBrowserEvent(context, parent, tag, event, valueUpdater);
        if ("click".equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            Element button = parent.getElementsByTagName("button").getItem(0);
            if (button != null && button.isOrHasChild(Element.as(eventTarget))) {
                new ConfirmationDialog(stringMessages, stringMessages.tagConfirmDeletionHeading(),
                        stringMessages.tagConfirmDeletion(tag.getTag()), (confirmed) -> {
                            if (confirmed) {
                                taggingPanel.removeTag(tag);
                            }
                        });
            }
        }
    }
}
