package com.sap.sailing.server.gateway.impl;

import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.media.ImageConverter;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ImageResizingServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4240540462729675752L;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jsonString = new BufferedReader(new InputStreamReader(req.getInputStream())).readLine();
        JSONObject obj = getObjFromJSON(jsonString);
        JSONArray tags = null;
        List<String> sizeTags = new ArrayList<>();
        if(obj != null) {
            String fileType = ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1);
            obj.put("FileType", fileType);
            InputStream is = getInputStreamFromURIString(((String)obj.get("URI")));
            BufferedImage img = ImageConverter.isToBi(is);
            is.close();
        
            tags = (JSONArray) obj.get("Tags");
            for(Object tag : tags) {
                if(((String)tag).equalsIgnoreCase(MediaTagConstants.LOGO)||((String)tag).equalsIgnoreCase(MediaTagConstants.STAGE)||((String)tag).equalsIgnoreCase(MediaTagConstants.TEASER)) {
                    sizeTags.add((String)tag);
                }
            }
            JSONObject uriMap = (JSONObject) obj.get("UriMap");
            if(uriMap == null) {
                uriMap = new JSONObject();
                obj.put("UriMap", uriMap);
            }
            for(String sizeTag : sizeTags) {
                if(uriMap.get(sizeTag) == null) {
                    resizeAndAddToAr(img, uriMap, sizeTag, fileType);
                }
            }
            resp.getWriter().write(obj.toJSONString());
        }else {
            //ERROR
        }
    }

    private InputStream getInputStreamFromURIString(String uri) {
        try {
            return getService().getFileStorageManagementService().getActiveFileStorageService().loadFile(new URI(uri));
        } catch (NoCorrespondingServiceRegisteredException | OperationFailedException | InvalidPropertiesException
                | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getObjFromJSON(String json) {
        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void resizeAndAddToAr(BufferedImage img, JSONObject uriMap, String tag, String fileType) {
        String imgUri = "";
        switch(tag) {
        case MediaTagConstants.LOGO:
            try (InputStream resizedImage = ImageConverter.biToIs(ImageConverter.resize(img, MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT, fileType, false),fileType)){
                imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(resizedImage, fileType, resizedImage.available()).toString();
            } catch (NoCorrespondingServiceRegisteredException | IOException | OperationFailedException
                    | InvalidPropertiesException e) {
                e.printStackTrace();
            }
            uriMap.put(MediaTagConstants.LOGO, imgUri);
            break;
        case MediaTagConstants.STAGE:
            try(InputStream resizedImage = ImageConverter.biToIs(ImageConverter.resize(img, MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT, fileType, false),fileType)) {
                imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(resizedImage, fileType, resizedImage.available()).toString();
            } catch (NoCorrespondingServiceRegisteredException | IOException | OperationFailedException
                    | InvalidPropertiesException e) {
                e.printStackTrace();
            }
            uriMap.put(MediaTagConstants.LOGO, imgUri);
            break;
        case MediaTagConstants.TEASER:
            try (InputStream resizedImage = ImageConverter.biToIs(ImageConverter.resize(img, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, fileType, false),fileType)){
                imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(resizedImage, fileType, resizedImage.available()).toString();
            } catch (NoCorrespondingServiceRegisteredException | IOException | OperationFailedException
                    | InvalidPropertiesException e) {
                e.printStackTrace();
            }
            uriMap.put(MediaTagConstants.LOGO, imgUri);
            break;
        default://can not occur, because we only loop over the sizeTags, which are the same as the switch cases
            break;
        }
    }
}
