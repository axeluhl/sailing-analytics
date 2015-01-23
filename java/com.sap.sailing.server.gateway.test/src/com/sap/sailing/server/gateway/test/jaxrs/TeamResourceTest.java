package com.sap.sailing.server.gateway.test.jaxrs;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.server.gateway.jaxrs.api.TeamResource;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.impl.AmazonS3FileStorageServiceImpl;
import com.sun.jersey.core.header.FormDataContentDisposition;

public class TeamResourceTest extends AbstractJaxRsApiTest {
    private final String name = "Heiko KRÖGER";
    private final String id = "af855a56-9726-4a9c-a77e-da955bd289be";
    private final String boatClassName = "49er";
    private final String sailID = "GER 1";
    private final String nationality = "GER";
    private final String teamImageFile = "Bundesliga2014_Regatta6_eventteaser.jpg";
    
    private final FileStorageService storageService = new AmazonS3FileStorageServiceImpl();

    @Override
    public void setUp() {
        super.setUp();
        racingEventService = spy(racingEventService);
        doReturn(storageService).when(racingEventService).getFileStorageService();
        DynamicTeam team = new TeamImpl(null, Collections.singleton(new PersonImpl(null, new NationalityImpl(
                nationality), null, null)), null);
        DynamicBoat boat = new BoatImpl(null, new BoatClassImpl(boatClassName, false), sailID);
        racingEventService.getBaseDomainFactory().getOrCreateCompetitor(id, name, null, team, boat);
    }

    @Test
    public void storeAndRemoveTeamImage() throws URISyntaxException, ParseException, MalformedURLException, IOException {
        //set team image
        TeamResource r = new TeamResource();
        long length = new File(new URI(getClass().getResource(teamImageFile).toString())).length();
        System.out.println("Found file with size (bytes) " + length);
        InputStream stream = getClass().getResourceAsStream(teamImageFile);

        FormDataContentDisposition fileDetails = FormDataContentDisposition.name("file").size(length)
                .fileName(teamImageFile).build();
        String jsonString = r.setTeamImage(id, stream, fileDetails);
        
        //now download and compare
        JSONObject json = (JSONObject) JSONValue.parseWithException(jsonString);
        String imageUri = (String) json.get(DeviceMappingConstants.JSON_TEAM_IMAGE_URI);
        
        InputStream downloadStream = new URI(imageUri).toURL().openStream();
        stream = getClass().getResourceAsStream(teamImageFile);
        IOUtils.contentEquals(downloadStream, stream);
    }
}
