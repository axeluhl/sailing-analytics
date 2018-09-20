package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;
import com.sap.sse.security.ui.client.UserService;

/**
 * Used to display tags in various locations.
 */
// TODO: Remove share button from private tags
// TODO: change text of "created at" ...
public class TagCell extends AbstractCell<TagDTO> {

    /**
     * Contains different configurations for {@link TagCell}s. Used as {@link SafeHtmlTemplates template} for
     * {@link TagCell}s. <br/>
     * <br/>
     * <b>Notice:</b> CSS classes need to be provided as parameters as CSS classnames are context sensitive and can't be
     * hardcoded.
     */
    protected interface TagCellTemplate extends SafeHtmlTemplates {

        /**
         * Renders cell with dynamic content.
         * 
         * @param tag
         *            title
         * @param icon
         *            available configurations are {@link #icon} or empty {@link SafeHtml}
         * @param buttons
         *            available configurations are {@link #headerButtonsDeletable},
         *            {@link #headerButtonsDeletableAndModifyable} or empty {@link SafeHtml}
         * @param createdAt
         *            contains author name and createdAt time
         * @param content
         *            available configurations are {@link #contentWithCommentWithImage},
         *            {@link #contentWithCommentWithoutImage} and {@link #contentWithoutCommentWithImage}.
         * @param shareButton
         * @param content2
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'><div class='{1}'>{3}{4}{5}{8}</div><div class='{2}'>{6}</div>{7}</div>")
        SafeHtml cell(String classTag, String classTagHeading, String classTagCreated, SafeHtml icon, SafeHtml buttons,
                SafeHtml tag, SafeHtml created, SafeHtml content, SafeHtml shareButtonHeader);

        /**
         * Renders content with maximal configuration (comment and image).
         * 
         * @param imageURL
         *            image URL
         * @param comment
         *            comment
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'><img src='{2}'/></div><div class='{1}'>{3}</div>")
        SafeHtml contentWithCommentWithImage(String classTagImage, String classTagComment, SafeUri imageURL,
                SafeHtml comment);

        /**
         * Renders content with mixed configuration (comment, no image).
         * 
         * @param comment
         *            comment
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'>{1}</div>")
        SafeHtml contentWithCommentWithoutImage(String classTagComment, SafeHtml comment);

        /**
         * Renders content with mixed configuration (image, no comment).
         * 
         * @param imageURL
         *            image URL
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'><img src='{1}'/></div>")
        SafeHtml contentWithoutCommentWithImage(String classTagImage, SafeUri imageURL);

        /**
         * Renders heading button to delete a tag.
         * 
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'>{1}</div>")
        SafeHtml headerButtonsDeletable(String classTagHeadingButtons, SafeHtml deleteButton);

        /**
         * Renders heading button to edit and delete a tag.
         * 
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'>{1}{2}</div>")
        SafeHtml headerButtonsDeletableAndModifyable(String classTagHeadingButtons, SafeHtml editButton,
                SafeHtml deleteButton);

        /**
         * Renders part of heading holding a button to share a tag.
         * 
         * @return {@link SafeHtml HTML template}
         */
        @Template("<div class='{0}'>{1}</div>")
        SafeHtml shareButtonHeader(String classTagHeadingButtons, SafeHtml shareButton);

        /**
         * Renders icon with given source.
         * 
         * @return {@link SafeHtml HTML template}
         */
        @Template("<img src='{0}'>")
        SafeHtml icon(SafeUri safeUri);

        /**
         * Renders button with given content.
         * 
         * @param title
         *            will be shown as tooltip of button
         * @param content
         *            content of button
         * @return {@link SafeHtml HTML template}
         */
        @Template("<button class='{0}' title='{1}'>{2}</button>")
        SafeHtml button(String classButton, String title, SafeHtml content);
    }

    private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final UserService userService;
    private final boolean isPreviewCell;

    /**
     * Displays the content of a {@link TagDTO tag} by using {@link SafeHtmlTemplates}.
     * 
     * @param taggingPanel
     *            instance of {@link TaggingPanel}
     * @param isPreviewCell
     *            should be <code>true</code> if {@link TagCell cell} is used as {@link TagPreviewPanel preview cell},
     *            otherwise <code>false</code>
     */
    public TagCell(TaggingPanel taggingPanel, boolean isPreviewCell) {
        super("click");
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();
        this.userService = taggingPanel.getUserSerivce();
        this.isPreviewCell = isPreviewCell;
    }

    /**
     * Renders {@link TagDTO tag} and appends it to given {@link SafeHtmlBuilder html builder}. Checks tag for available
     * attributes and chooses suitable {@link TagCellTemplate rendering template}.
     */
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

        SafeHtml icon = SafeHtmlUtils.EMPTY_SAFE_HTML;
        if (!tag.isVisibleForPublic()) {
            icon = tagCellTemplate.icon(safeIsPrivateImageUri);
        }

        SafeHtml shareButton = tagCellTemplate.button(style.tagActionButton() + " " + style.tagShareButton(),
                stringMessages.tagShareTag(), tagCellTemplate.icon(resources.shareIcon().getSafeUri()));
        SafeHtml shareButtonHeader = tagCellTemplate.shareButtonHeader(style.tagCellHeadingButtons(), shareButton);

        SafeHtml headingButtons = SafeHtmlUtils.EMPTY_SAFE_HTML;
        // preview cells do not show buttons
        if (!isPreviewCell) {
            if (userService.getCurrentUser() != null) {
                // create buttons with icons
                SafeHtml deleteButton = tagCellTemplate.button(style.tagActionButton() + " " + style.tagDeleteButton(),
                        stringMessages.tagDeleteTag(), tagCellTemplate.icon(resources.deleteIcon().getSafeUri()));
                SafeHtml editButton = tagCellTemplate.button(style.tagActionButton() + " " + style.tagEditButton(),
                        stringMessages.tagEditTag(), tagCellTemplate.icon(resources.editIcon().getSafeUri()));
                // TODO: As soon as permission-vertical branch got merged into master, apply
                // new permission system at this if-statement and remove this old way of
                // checking for permissions. (see bug 4104, comment 9)
                // functionality: Check if user has the permission to delete or edit this tag.
                if (tag.getUsername().equals(userService.getCurrentUser().getName())) {
                    headingButtons = tagCellTemplate.headerButtonsDeletableAndModifyable(style.tagCellHeadingButtons(),
                            editButton, deleteButton);
                } else if (userService.getCurrentUser().hasRole("admin")) {
                    headingButtons = tagCellTemplate.headerButtonsDeletable(style.tagCellHeadingButtons(),
                            deleteButton);
                }
            }
        }

        String cellStyle = style.tagCell();
        if (tag.equals(taggingPanel.getSelectedTag())) {
            cellStyle = style.tagCell() + " " + style.tagCellActive();
        }
        SafeHtml cell = tagCellTemplate.cell(cellStyle, style.tagCellHeading(), style.tagCellCreated(), icon,
                headingButtons, safeTag, safeCreated, content, shareButtonHeader);
        htmlBuilder.append(cell);
    }

    /**
     * Asks user for confirmation if user presses the delete button on the {@link TagCell}. If user confirms deletion,
     * {@link TagDTO tag} will be deleted from {@link TaggingPanel} including
     * {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog} or {@link com.sap.sse.security.UserStore
     * UserStore}, depending on where the tag is saved. Also allows users to edit {@link TagDTO tags}, by putting
     * {@link TaggingPanel} into state {@link TaggingPanel.State#EDIT_TAG edit} when user presses the edit button.
     */
    @Override
    public void onBrowserEvent(Context context, Element parent, TagDTO tag, NativeEvent event,
            ValueUpdater<TagDTO> valueUpdater) {
        super.onBrowserEvent(context, parent, tag, event, valueUpdater);
        // Ignore browser events when tagging panel is in "Edit-Tag" mode so selection can't change during editing of
        // tags.
        if (!taggingPanel.getCurrentState().equals(State.EDIT_TAG)) {
            if ("click".equals(event.getType())) {
                EventTarget eventTarget = event.getEventTarget();
                if (!Element.is(eventTarget)) {
                    return;
                }
                NodeList<Element> elements = parent.getElementsByTagName("button");
                for (int i = 0; i < elements.getLength(); i++) {
                    Element button = elements.getItem(i);
                    // identify intended button-functionality by classname
                    if (button == null || !button.isOrHasChild(Element.as(eventTarget))) {
                        continue;
                    } else if (button.hasClassName(style.tagDeleteButton())) {
                        new ConfirmationDialog(stringMessages, stringMessages.tagConfirmDeletionHeading(),
                                stringMessages.tagConfirmDeletion(tag.getTag()), (confirmed) -> {
                                    if (confirmed) {
                                        taggingPanel.removeTag(tag);
                                    }
                                });
                    } else if (button.hasClassName(style.tagEditButton())) {
                        taggingPanel.setCurrentState(State.EDIT_TAG);
                    } else if (button.hasClassName(style.tagShareButton())) {
                        String currentURL = Window.Location.getHref();
                        String urlWithTagParam = currentURL
                                .concat("&tag=" + tag.getRaceTimepoint().asMillis() + tag.getTag());
                        new TagSharedURLDialog(taggingPanel, urlWithTagParam).show();
                    }
                }
            }
        }
    }
}
