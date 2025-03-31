package com.sap.sailing.gwt.ui.common.client;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * See also:
 * "10 Youtube URL Tricks You Should Know About" http://www.makeuseof.com/tag/10-youtube-url-tricks-you-should-know-about/
 * "Useful YouTube URL Tricks" http://www.techairlines.com/useful-youtube-url-tricks/
 * "Useful YouTube Player Parameters" http://www.techairlines.com/youtube-parameters/
 * @author D047974
 *
 */
public class YoutubeApi {
    
    private static final RegExp YOUTUBE_ID_REGEX = RegExp.compile("^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]*).*");
    private static final RegExp HTTP_FTP_FILE_REGEX = RegExp.compile("^(http|ftp|file).*"); // starting with http, https or ftp
    
    /**
     * Extracts the youtube id of the passed youtube video URL.
     * Also accepts 
     * From http://stackoverflow.com/questions/3452546/javascript-regex-how-to-get-youtube-video-id-from-url, mantish Mar 4 at 15:33
     * @param url
     * @return The youtube 
     */
    public static String getIdByUrl(String url) {
        MatchResult match = YOUTUBE_ID_REGEX.exec(url);
        if ((match != null) && (match.getGroupCount() == 3)) {
            return match.getGroup(2);
        } else if (HTTP_FTP_FILE_REGEX.exec(url) == null) { //--> doesn't start with either http, https or ftp --> supposed to be a naked youtube id   
            return url.trim();
        } else {
            return null; // --> plain http, https or ftp URL --> no youtube 
        }
    }
    
    /**
     * From http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
     * @param id
     * @return
     */
    public static String getThumbnailUrl(String id) {
        return "//img.youtube.com/vi/" + id + "/mqdefault.jpg";
    }
    
}
