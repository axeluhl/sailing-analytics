package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.tagging.RaceLogNotFoundException;
import com.sap.sailing.domain.common.tagging.TagAlreadyExistsException;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.tagging.TagDTODeSerializer;
import com.sap.sailing.server.tagging.TaggingService;
import com.sap.sailing.server.tagging.TaggingServiceImpl;
import com.sap.sailing.server.testsupport.SecurityBundleTestWrapper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Tests {@link TaggingService} which is used for all CRUD operations regarding {@link TagDTO tags}.
 */
public class TaggingServiceTest {

    // user
    private final static String username = "abc";
    private final static String email = "e@mail.com";
    private final static String password = "password";
    private final static String fullName = "Full Name";
    private final static String company = "Company";

    // race definition
    private final static String leaderboardName = "Leaderboard";
    private final static String raceColumnName = "RaceColumn";
    private final static String fleetName = "Default";
    private final static WildcardPermission editLeaderboardPermission = SecuredDomainType.LEADERBOARD
            .getPermissionForObjects(DefaultActions.UPDATE, leaderboardName);

    // tagging & utilities
    private final static Logger logger = Logger.getLogger(TaggingServiceTest.class.getName());
    private final static TagDTODeSerializer serializer = new TagDTODeSerializer();
    private static RacingEventService racingService;
    private static SecurityService securityService;
    private static TaggingService taggingService;
    private static Subject subject;

    @BeforeClass
    public static void setUpClass()
            throws MalformedURLException, IOException, InterruptedException, UserManagementException, MailException, UserGroupManagementException {
        MongoDBService.INSTANCE.getDB().dropDatabase();
        // setup racing service and racelog
        racingService = new RacingEventServiceImpl();
        RacingEventServiceOperation<FlexibleLeaderboard> addLeaderboardOp = new CreateFlexibleLeaderboard(
                leaderboardName, leaderboardName, new int[] { 5 }, new LowPoint(), null);
        racingService.apply(addLeaderboardOp);
        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn = new AddColumnToLeaderboard(raceColumnName,
                leaderboardName, true);
        racingService.apply(addLeaderboardColumn);
        // setup security service
        securityService = new SecurityBundleTestWrapper().initializeSecurityServiceForTesting();
        // create & login user
        securityService.createSimpleUser(username, email, password, fullName, company, null);
        subject = SecurityUtils.getSubject();
        subject.login(new UsernamePasswordToken(username, password));
        // setup tagging service
        taggingService = new TaggingServiceImpl(racingService);
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            subject.logout();
            securityService.deleteUser(username);
        } catch (UserManagementException e) {
            logger.severe("Could not teardown TaggingServiceTest!");
        }
        MongoDBService.INSTANCE.getDB().dropDatabase();
    }

    @Before
    public void resetEnvironment() {
        securityService.addPermissionForUser(username, editLeaderboardPermission);
        securityService.unsetPreference(username,
                serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName));
        final RaceLog raceLog = racingService.getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            for (RaceLogEvent event : raceLog.getUnrevokedEvents()) {
                try {
                    raceLog.revokeEvent(event.getAuthor(), event);
                } catch (NotRevokableException e) {
                    logger.warning(
                            "Could not clean up test setup for TaggingServiceTest as public tag could not be removed!");
                }
            }
        }
        try {
            List<TagDTO> tags = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false);
            logger.info("Tags after environment reset: " + tags.toString());
        } catch (Exception e) {
            logger.severe("Could not reset test environment");
        }
    }

    @Test
    public void testAddTag() {
        logger.entering(getClass().getName(), "testAddTag");
        final String tag = "TagToCreate";
        final String comment = "Comment To Create";
        final String imageURL = "";
        final TimePoint raceTimepoint = new MillisecondsTimePoint(1);
        try {
            logger.info("Trying to add public tag with missing title which should be catched by this test.");
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, null, comment, imageURL, imageURL, true,
                    raceTimepoint);
            fail("Tag should not be added because the tag title is missing!");
        } catch (IllegalArgumentException e) {
            assertTrue("Invalid arguments were caught correctly!", true);
        } catch (Exception e) {
            fail("Caught unexpected exception while adding public tag! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        try {
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, false,
                    raceTimepoint);
            String preference = securityService.getPreference(username,
                    serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName));
            List<TagDTO> privateTags = serializer.deserializeTags(preference);
            assertTrue("Create private tag", privateTags.size() == 1 && privateTags.get(0).equals(tag, comment,
                    imageURL, imageURL, false, subject.getPrincipal().toString(), raceTimepoint));
        } catch (Exception e) {
            fail("Caught unexpected exception while adding private tag! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        try {
            logger.info("Trying to add public tag with missing permissions which should be catched by this test.");
            securityService.removePermissionFromUser(username, editLeaderboardPermission);
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, true,
                    raceTimepoint);
            fail("Tag should not be added because user is missing permissions!");
        } catch (AuthorizationException e) {
            assertTrue("Missing permissions were caught correctly!", true);
        } catch (Exception e) {
            fail("Caught unexpected exception while adding public tag! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        } finally {
            securityService.addPermissionForUser(username, editLeaderboardPermission);
        }
        try {
            logger.info(
                    "Trying to add public tag with wrong racelog identifiers which should be catched by this test.");
            taggingService.addTag(leaderboardName, "bla", fleetName, tag, comment, imageURL, imageURL, true, raceTimepoint);
            fail("Tag should not be added because racelog does not exist!");
        } catch (RaceLogNotFoundException e) {
            assertTrue("Missing racelog was caught correctly!", true);
        } catch (Exception e) {
            fail("Caught unexpected exception while adding public tag! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        try {
            logger.info("Trying to add public tag, should succeed");
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, true,
                    raceTimepoint);
            List<TagDTO> publicTags = taggingService.getPublicTags(leaderboardName, raceColumnName, fleetName, null,
                    false);
            assertTrue("", publicTags.size() == 1 && publicTags.get(0).equals(tag, comment, imageURL, imageURL, true,
                    subject.getPrincipal().toString(), raceTimepoint));
        } catch (Exception e) {
            fail("Caught unexpected exception while adding public tag which should succeed! " + e.getClass().getName()
                    + ", message: " + e.getMessage());
        }
        try {
            logger.info("Trying to add already existing public tag which should be catched by this test.");
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, true,
                    raceTimepoint);
            fail("Tag should not be added because it already exists!");
        } catch (TagAlreadyExistsException e) {
            assertTrue("Tag already exists was caught correctly!", true);
        } catch (Exception e) {
            fail("Caught unexpected exception while adding public tag! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        logger.exiting(getClass().getName(), "testAddTag");
    }

    @Test
    public void testGetTags() {
        logger.entering(getClass().getName(), "testGetTags");
        final String tag = "TagToLoad";
        final String comment = "Comment To Load";
        final String imageURL = "localhost";
        final TimePoint raceTimepoint = new MillisecondsTimePoint(1000);
        try {
            logger.info("Adding tags which should be loaded via getTags() afterwards.");
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, false,
                    raceTimepoint);
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, true,
                    raceTimepoint);
            assertTrue("Private tags contain added tag",
                    taggingService.getPrivateTags(leaderboardName, raceColumnName, fleetName).get(0).equals(tag,
                            comment, imageURL, imageURL, false, subject.getPrincipal().toString(), raceTimepoint));
            assertTrue("Public tags contain added tag",
                    taggingService.getPublicTags(leaderboardName, raceColumnName, fleetName, null, false).get(0)
                            .equals(tag, comment, imageURL, imageURL,  true, subject.getPrincipal().toString(), raceTimepoint));
            assertEquals("Public tags contain added tag with matching creation date filter", 1, taggingService
                    .getPublicTags(leaderboardName, raceColumnName, fleetName, raceTimepoint, false).size());
            assertEquals("Public tags do not contain added tag with non-matching creation date filter", 0,
                    taggingService.getPublicTags(leaderboardName, raceColumnName, fleetName,
                            MillisecondsTimePoint.now(), false).size());
        } catch (Exception e) {
            fail("Caught unexpected exception while loading tags which were previously added! " + e.getClass().getName()
                    + ", message: " + e.getMessage());
        }
        logger.exiting(getClass().getName(), "testGetTags");
    }

    @Test
    public void testUpdateTag() {
        logger.entering(getClass().getName(), "testUpdateTag");
        final String tag = "TagToUpdate";
        final String comment = "Comment To Update";
        final String imageURL = "localhost";
        final TimePoint raceTimepoint = new MillisecondsTimePoint(1000);
        final String updatedTag = "Upd/ated %Ta!g!���";
        final String updatedComment = "New comment...";
        final String updatedImageURL = "";
        try {
            // add tag
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, false,
                    raceTimepoint);
            TagDTO tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            // update tag title
            taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagObject, updatedTag, comment,
                    imageURL, imageURL, false);
            tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            assertEquals("Updated tag title", updatedTag, tagObject.getTag());
            // update comment
            taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagObject, tag, updatedComment,
                    imageURL, imageURL, false);
            tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            assertEquals("Updated comment", updatedComment, tagObject.getComment());
            // update image URL
            taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagObject, tag, comment,
                    updatedImageURL, updatedImageURL, false);
            tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            assertEquals("Updated image URL", updatedImageURL, tagObject.getImageURL());
            // update visibility (private -> public)
            taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagObject, tag, comment, imageURL,
                    imageURL, true);
            tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            assertEquals("Updated visibility (private -> public)", true, tagObject.isVisibleForPublic());
            // update visibility (public -> private)
            taggingService.updateTag(leaderboardName, raceColumnName, fleetName, tagObject, tag, comment, imageURL,
                    imageURL, false);
            tagObject = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).get(0);
            assertEquals("Updated visibility (public -> private)", false, tagObject.isVisibleForPublic());
        } catch (Exception e) {
            fail("Caught unexpected exception while updating tags! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        logger.exiting(getClass().getName(), "testUpdateTag");
    }

    @Test
    public void testRemoveTag() {
        logger.entering(getClass().getName(), "testRemoveTag");
        final String tag = "TagToRemove";
        final String comment = " Comment To Remove";
        final String imageURL = "localhost";
        final TimePoint raceTimepoint = new MillisecondsTimePoint(1);
        try {
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, false,
                    raceTimepoint);
            taggingService.addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL, imageURL, true,
                    raceTimepoint);
            final List<TagDTO> tags = taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false);
            assertEquals("Tags were added successfully so they can be removed afterwards", 2, tags.size());
            for (TagDTO tagObject : tags) {
                taggingService.removeTag(leaderboardName, raceColumnName, fleetName, tagObject);
            }
            assertEquals("Tag list should be empty after deleting all tags", 0,
                    taggingService.getTags(leaderboardName, raceColumnName, fleetName, null, false).size());
        } catch (Exception e) {
            fail("Caught unexpected exception while removing tags! " + e.getClass().getName() + ", message: "
                    + e.getMessage());
        }
        logger.exiting(getClass().getName(), "testRemoveTag");
    }
}
