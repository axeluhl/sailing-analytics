package com.sap.sailing.server.tagging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogTagEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;

public class TaggingServiceImpl implements TaggingService {

    private final RacingEventService racingService;
    private final TagDTODeSerializer serializer;
    private final Map<Subject, ErrorCode> lastErrorCodes;

    public TaggingServiceImpl(RacingEventService racingService) {
        this.racingService = racingService;
        serializer = new TagDTODeSerializer();
        lastErrorCodes = new HashMap<Subject, ErrorCode>();
    }

    private void setLastErrorCode(ErrorCode errorCode) {
        lastErrorCodes.put(SecurityUtils.getSubject(), errorCode);
    }

    private String getCurrentUsername() {
        String result = null;
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal != null) {
            result = principal.toString();
        } else {
            setLastErrorCode(ErrorCode.NOT_LOGGED_IN);
        }
        return result;
    }

    private SecurityService getSecurityService() {
        SecurityService securityService = Activator.getSecurityService();
        if (securityService == null) {
            setLastErrorCode(ErrorCode.SECURITY_SERIVCE_NOT_FOUND);
        }
        return securityService;
    }

    private boolean addPublicTag(String leaderboardName, String raceColumnName, String fleetName, String tag,
            String comment, String imageURL, TimePoint raceTimepoint) {
        boolean successful = true;
        try {
            // TODO: As soon as permission-vertical branch got merged into master, apply
            // new permission system at this permission check (see bug 4104, comment 9)
            // functionality: Check if user has the permission to add RaceLogEvents to RaceLog.
            SecurityUtils.getSubject().checkPermission(
                    Permission.LEADERBOARD.getStringPermissionForObjects(Mode.UPDATE, leaderboardName));
            RaceLog raceLog = racingService.getRaceLog(leaderboardName, raceColumnName, fleetName);
            if (raceLog == null) {
                setLastErrorCode(ErrorCode.RACELOG_NOT_FOUND);
                successful = false;
            } else {
                // check if tag already exists
                boolean alreadyExists = false;
                List<TagDTO> publicTags = getPublicTags(leaderboardName, raceColumnName, fleetName);
                for (TagDTO publicTag : publicTags) {
                    // ignore revoked tags as TagDTO.equals() does ignore revokedAt timepoint
                    if (publicTag.getRevokedAt() == null && publicTag.equals(tag, comment, imageURL,
                            true, racingService.getServerAuthor().getName(), raceTimepoint)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (alreadyExists) {
                    setLastErrorCode(ErrorCode.TAG_ALREADY_EXISTS);
                    successful = false;
                } else {
                    raceLog.add(new RaceLogTagEventImpl(tag, comment, imageURL, raceTimepoint,
                            racingService.getServerAuthor(), raceLog.getCurrentPassId()));
                }
            }
        } catch (AuthorizationException e) {
            setLastErrorCode(ErrorCode.MISSING_PERMISSIONS);
            successful = false;
        }
        return successful;
    }

    private boolean addPrivateTag(String leaderboardName, String raceColumnName, String fleetName, String tag,
            String comment, String imageURL, TimePoint raceTimepoint) {
        boolean successful = true;
        SecurityService securityService = Activator.getSecurityService();
        if (securityService == null) {
            setLastErrorCode(ErrorCode.SECURITY_SERIVCE_NOT_FOUND);
            successful = false;
        } else {
            Subject subject = SecurityUtils.getSubject();
            if (subject.getPrincipal() != null) {
                String username = subject.getPrincipal().toString();
                TagDTODeSerializer serializer = new TagDTODeSerializer();
                String key = serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName);
                String privateTagsJson = securityService.getPreference(username, key);
                List<TagDTO> privateTags = serializer.deserializeTags(privateTagsJson);
                TagDTO tagToAdd = new TagDTO(tag, comment, imageURL, false, username, raceTimepoint,
                        MillisecondsTimePoint.now());
                if (privateTags.contains(tagToAdd)) {
                    setLastErrorCode(ErrorCode.TAG_ALREADY_EXISTS);
                    successful = false;
                } else {
                    privateTags.add(tagToAdd);
                    securityService.setPreference(username, key, serializer.serializeTags(privateTags));
                }
            } else {
                setLastErrorCode(ErrorCode.NOT_LOGGED_IN);
                successful = false;
            }
        }
        return successful;
    }

    private boolean removePublicTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag) {
        boolean successful = true;
        RaceLog raceLog = racingService.getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog == null) {
            setLastErrorCode(ErrorCode.RACELOG_NOT_FOUND);
            successful = false;
        } else {
            ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(racingService, raceLog);
            Iterable<RaceLogTagEvent> foundTagEvents = raceState.getTagEvents();
            for (RaceLogTagEvent tagEvent : foundTagEvents) {
                if (tagEvent.getRevokedAt() != null) {
                    continue;
                } else if (tagEvent.getTag().equals(tag.getTag()) && tagEvent.getComment().equals(tag.getComment())
                        && tagEvent.getImageURL().equals(tag.getImageURL())
                        && tagEvent.getUsername().equals(tag.getUsername())
                        && tagEvent.getLogicalTimePoint().equals(tag.getRaceTimepoint())) {

                    try {
                        // TODO: As soon as permission-vertical branch got merged into master, apply
                        // new permission system at this permission check (see bug 4104, comment 9)
                        // functionality: Check if user has the permission to delete tag from RaceLog (same user or
                        // admin).
                        Subject subject = SecurityUtils.getSubject();
                        subject.checkPermission(
                                Permission.LEADERBOARD.getStringPermissionForObjects(Mode.UPDATE, leaderboardName));
                        if ((subject.getPrincipal() != null && subject.getPrincipal().equals(tag.getUsername()))
                                || subject.hasRole("admin")) {
                            raceLog.revokeEvent(tagEvent.getAuthor(), tagEvent, "Revoked");
                        } else {
                            setLastErrorCode(ErrorCode.MISSING_PERMISSIONS);
                            successful = false;
                        }
                    } catch (AuthorizationException e) {
                        setLastErrorCode(ErrorCode.MISSING_PERMISSIONS);
                        successful = false;
                    } catch (NotRevokableException e) {
                        setLastErrorCode(ErrorCode.TAG_NOT_REVOKABLE);
                        successful = false;
                    }
                    break;
                }
            }
        }
        return successful;
    }

    private boolean removePrivateTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag) {
        boolean successful = true;
        String username = getCurrentUsername();
        if (username == null) {
            setLastErrorCode(ErrorCode.NOT_LOGGED_IN);
            successful = false;
        } else {
            List<TagDTO> privateTags = getPrivateTags(leaderboardName, raceColumnName, fleetName);
            privateTags.remove(tag);
            SecurityService securityService = getSecurityService();
            String key = serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName);
            // error code will be set during collection of required data
            if (username != null && securityService != null && key != null) {
                securityService.setPreference(username, key, serializer.serializeTags(privateTags));
            }
        }
        return successful;
    }

    @Override
    public boolean addTag(String leaderboardName, String raceColumnName, String fleetName, String tag, String comment,
            String imageURL, boolean visibleForPublic, TimePoint raceTimepoint) {
        boolean successful;
        // prefill optional parameters
        comment = comment == null ? "" : comment;
        imageURL = imageURL == null ? "" : imageURL;

        // check all parameters for validity
        if (tag == null || tag.isEmpty()) {
            setLastErrorCode(ErrorCode.TAG_NOT_EMPTY);
            successful = false;
        } else if (raceTimepoint == null || raceTimepoint.asMillis() == 0) {
            // TODO: Check if timepoint is near start/end of race (+/- x%)
            setLastErrorCode(ErrorCode.TIMEPOINT_NOT_EMPTY);
            successful = false;
        } else {
            // all parameters are valid => save tag
            if (visibleForPublic) {
                successful = addPublicTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL,
                        raceTimepoint);
            } else {
                successful = addPrivateTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL,
                        raceTimepoint);
            }
        }
        return successful;
    }

    @Override
    public boolean removeTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag) {
        boolean successful = true;
        // check all parameters for validity
        if (tag == null) {
            setLastErrorCode(ErrorCode.TAG_NOT_EMPTY);
            successful = false;
        } else {
            // all parameters are valid => remove tag
            if (tag.isVisibleForPublic()) {
                successful = removePublicTag(leaderboardName, raceColumnName, fleetName, tag);
            } else {
                successful = removePrivateTag(leaderboardName, raceColumnName, fleetName, tag);
            }
        }
        return successful;
    }

    @Override
    public boolean updateTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tagToUpdate,
            String tag, String comment, String imageURL, boolean visibleForPublic) {
        boolean successful = true;
        String username = getCurrentUsername();
        if (username != null && tagToUpdate.getUsername().equals(username)) {
            if (removeTag(leaderboardName, raceColumnName, fleetName, tagToUpdate)) {
                successful = addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL,
                        visibleForPublic, tagToUpdate.getRaceTimepoint());
            } else {
                successful = false;
            }
        }
        return successful;
    }

    @Override
    public List<TagDTO> getPublicTags(String leaderboardName, String raceColumnName, String fleetName) {
        final List<TagDTO> result = new ArrayList<TagDTO>();
        RaceLog raceLog = racingService.getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog == null) {
            setLastErrorCode(ErrorCode.RACELOG_NOT_FOUND);
        } else {
            ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(racingService, raceLog);
            Iterable<RaceLogTagEvent> foundTagEvents = raceState.getTagEvents();
            for (RaceLogTagEvent tagEvent : foundTagEvents) {
                result.add(new TagDTO(tagEvent.getTag(), tagEvent.getComment(), tagEvent.getImageURL(),
                        true, tagEvent.getUsername(), tagEvent.getLogicalTimePoint(), tagEvent.getCreatedAt(),
                        tagEvent.getRevokedAt()));
            }
        }
        return result;
    }

    @Override
    public List<TagDTO> getPublicTags(RegattaAndRaceIdentifier raceIdentifier, TimePoint searchSince) {
        final List<TagDTO> result = new ArrayList<TagDTO>();
        TrackedRace trackedRace = racingService.getExistingTrackedRace(raceIdentifier);
        Iterable<RaceLog> raceLogs = trackedRace.getAttachedRaceLogs();
        for (RaceLog raceLog : raceLogs) {
            ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(racingService, raceLog);
            Iterable<RaceLogTagEvent> foundTagEvents = raceState.getTagEvents();
            for (RaceLogTagEvent tagEvent : foundTagEvents) {
                if ((searchSince == null && tagEvent.getRevokedAt() == null)
                        || (searchSince != null && tagEvent.getRevokedAt() == null
                                && tagEvent.getCreatedAt().after(searchSince))
                        || (searchSince != null && tagEvent.getRevokedAt() != null
                                && tagEvent.getRevokedAt().after(searchSince))) {
                    result.add(new TagDTO(tagEvent.getTag(), tagEvent.getComment(), tagEvent.getImageURL(),
                            true, tagEvent.getUsername(), tagEvent.getLogicalTimePoint(), tagEvent.getCreatedAt(),
                            tagEvent.getRevokedAt()));
                }
            }
        }
        return result;
    }

    @Override
    public List<TagDTO> getPrivateTags(String leaderboardName, String raceColumnName, String fleetName) {
        final List<TagDTO> result = new ArrayList<TagDTO>();
        SecurityService securityService = getSecurityService();
        if (securityService != null) {
            String username = getCurrentUsername();
            if (username != null) {
                String key = serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName);
                String privateTagsJson = securityService.getPreference(username, key);
                List<TagDTO> privateTags = serializer.deserializeTags(privateTagsJson);
                result.addAll(privateTags);
            }
        }
        return result;
    }

    @Override
    public ErrorCode getLastErrorCode() {
        ErrorCode lastErrorCode = lastErrorCodes.get(SecurityUtils.getSubject());
        if (lastErrorCode == null) {
            lastErrorCode = ErrorCode.UNKNOWN_ERROR;
        }
        return lastErrorCode;
    }
}
