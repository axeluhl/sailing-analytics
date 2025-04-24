package com.sap.sse.gwt.shared;

/**
 * Whenever media content is made available that may have been uploaded by an end user, there is a possibility that
 * other users would like to request this content to be taken down, hidden or otherwise made unavailable. Reasons for
 * this could be IP infringements, copyright / licensing violations, nudity, violence or otherwise offensive content.
 * <p>
 * 
 * In order to allow users to be specific in their take-down notice / request, an instance of this class collects all
 * information required to generate a very specific message to an administrator that provides data about the location /
 * URL where the content was found, as well as a link to the content itself, such as the image / video URL.
 * <p>
 * 
 * See also <a href="https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=6105">bug 6105</a>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TakedownNoticeRequestContext {
    public static enum NatureOfClaim {
        NONE,
        COPYRIGHT_INFRINGEMENT,
        DEFAMATORY_CONTENT,
        OTHER
    }
    
    /**
     * A message ID and parameters for, e.g., an e-mail message or some other form of notification to a human
     * administrator user. The message identified by the ID describes where on the site and in which role the medium has
     * occurred. Examples:
     * <ul>
     * <li>competitor image for competitor THA 1423 in regatta 29er Worlds 2015</li>
     * <li>event stage image for event Kieler Woche 2023</li>
     * <li>tag image as provided by user fhs19 on race R9 in regatta 470 World Championships 2023</li>
     * </ul>
     * The message will be translated using a {@link ResourceBundleStringMessages} for the administrator user's locale.
     * The first element of the {@link Iterable} is assumed to be the message ID. All remaining elements, if any, are
     * assumed to be parameters that will be passed to the
     * {@code ResourceBundleStringMessages.get(Locale locale, String messageKey, String... parameters)} method.
     */
    private final Iterable<String> contextDescriptionMessageIdAndParameters;
    
    /**
     * The URL pointing to the media object
     */
    private final String contentUrl;
    
    private final NatureOfClaim natureOfClaim;
    
    /**
     * A free text form for the reporting user to describe the nature of the claim
     */
    private final String reportingUserComment;
    
    /**
     * Optional additional URLs provided by the user reporting the medium, providing information
     * about the nature or justification of the claim.
     */
    private final Iterable<String> supportingURLs;
}
