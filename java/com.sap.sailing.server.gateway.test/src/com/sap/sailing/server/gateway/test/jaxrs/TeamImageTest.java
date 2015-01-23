package com.sap.sailing.server.gateway.test.jaxrs;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.server.gateway.jaxrs.api.CompetitorsResource;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sap.sse.filestorage.testsupport.AmazonS3TestSupport;

public class TeamImageTest extends AbstractJaxRsApiTest {    
    private static final String name = "Heiko KRÖGER";
    private static final String id = "af855a56-9726-4a9c-a77e-da955bd289be";
    private static final String boatClassName = "49er";
    private static final String sailID = "GER 1";
    private static final String nationality = "GER";
    private static final String teamImageFile = "Bundesliga2014_Regatta6_eventteaser.jpg";
    
    private FileStorageService storageService;

    @Before
    public void setUpSubClass() throws InvalidPropertiesException {
        super.setUp();
        racingEventService = spy(racingEventService);
        storageService = AmazonS3TestSupport.createService();
        doReturn(storageService).when(racingEventService).getFileStorageService();
        DynamicTeam team = new TeamImpl(null, Collections.singleton(new PersonImpl(null, new NationalityImpl(
                nationality), null, null)), null);
        DynamicBoat boat = new BoatImpl(null, new BoatClassImpl(boatClassName, false), sailID);
        racingEventService.getBaseDomainFactory().getOrCreateCompetitor(id, name, null, team, boat);
    }

    @Test
    public void storeAndRemoveTeamImage() throws URISyntaxException, ParseException, MalformedURLException,
            IOException, OperationFailedException, InvalidPropertiesException {
        //set team image
        CompetitorsResource r = spyResource(new CompetitorsResource());
        URL fileUrl = getClass().getResource("/" + teamImageFile);
        URI fileUri = new URI(fileUrl.toString());
        String fileExtension = teamImageFile.substring(teamImageFile.lastIndexOf("."));
        long length = new File(fileUri).length();
        InputStream stream = getClass().getResourceAsStream("/" + teamImageFile);
        String jsonString = r.setTeamImage(id, stream, fileExtension, length);
        
        //now download and compare
        JSONObject json = (JSONObject) JSONValue.parseWithException(jsonString);
        String imageUriString = (String) json.get(DeviceMappingConstants.JSON_TEAM_IMAGE_URI);
        URI imageUri = new URI(imageUriString);
        
        InputStream downloadStream = imageUri.toURL().openStream();
        stream = getClass().getResourceAsStream("/" + teamImageFile);
        IOUtils.contentEquals(downloadStream, stream);
        
        storageService.removeFile(imageUri);
    }
}
