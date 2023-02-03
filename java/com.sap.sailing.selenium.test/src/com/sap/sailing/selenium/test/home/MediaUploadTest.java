package com.sap.sailing.selenium.test.home;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO.EventEntryPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventUpdateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.event.UploadVideosDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.event.VideosTabPO;
import com.sap.sailing.selenium.pages.home.HomePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MediaUploadTest extends AbstractSeleniumTest {

    private static final String EVENT = "Kieler Woche 2015"; //$NON-NLS-1$
    private static final String BOAT_CLASS_49ER = "49er"; //$NON-NLS-1$
    private static final String REGATTA_49ER = "KW 2015 Olympic - 49er"; //$NON-NLS-1$
    private static final String REGATTA_49ER_WITH_SUFFIX = REGATTA_49ER + " (" + BOAT_CLASS_49ER + ")"; //$NON-NLS-1$
    private static final String EVENT_DESC = "Kieler Woche 2015"; //$NON-NLS-1$
    private static final String VENUE = "Kieler F�rde"; //$NON-NLS-1$
    private static final Date EVENT_START_TIME = DatatypeConverter.parseDateTime("2015-06-20T08:00:00-00:00").getTime();
    private static final Date EVENT_END_TIME = DatatypeConverter.parseDateTime("2015-06-28T20:00:00-00:00").getTime();
    private static final String YOUTUBE_URL_1 = "https://youtu.be/tJFuTKPH_i8";
    private static final String YOUTUBE_URL_2 = "https://www.youtube.com/watch?v=tJFuTKPH_i8";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        
    }

    @Test
    public void testHomepageMedia() throws UnsupportedEncodingException {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(EVENT, EVENT_DESC, VENUE,
                EVENT_START_TIME, EVENT_END_TIME, true, REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, EVENT_START_TIME,
                EVENT_END_TIME, false);
        EventEntryPO entryPO = events.getEventEntry(EVENT);
        String eventUrl = entryPO.getEventURL();
        String eventId = entryPO.getUUID();
        EventUpdateDialogPO eventUpdateDialogPO = entryPO.openUpdateEventDialog();
        VideosTabPO videosTabPO = eventUpdateDialogPO.goToVideosTab();
        UploadVideosDialogPO uploadVideosDialogPO = videosTabPO.clickAddVideoBtn();
        uploadVideosDialogPO.enterUrl(YOUTUBE_URL_1);
        String mimeType = uploadVideosDialogPO.getMimeTypeString();
        assertThat(mimeType,equalTo("YouTube"));
        
        //FileStoragePO fileStoragePO = adminConsole.goToFileStorage();
        //fileStoragePO.setLocalStorageService(FileStoragePO.SERVICE_LOCAL_STORAGE_VALUE);
        
        HomePage homePage = HomePage.goToHomeUrl(getWebDriver(), eventUrl);
        homePage.clickOnEvent(eventId);
        homePage.clickOnEventsMenuItem();
        homePage.getPageTitle();
    }
}
