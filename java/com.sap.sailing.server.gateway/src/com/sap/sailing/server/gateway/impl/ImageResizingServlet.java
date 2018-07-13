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
        JSONArray alreadyResizedTags = null;
        List<String> sizeTags = new ArrayList<>();
        if(obj != null) {
            String fileType = ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1);
            obj.put("FileType", fileType);
            InputStream is = getInputStreamFromURIString(((String)obj.get("URI")));
            BufferedImage img = ImageConverter.isToBi(is);
            is.close();
        
            tags = (JSONArray) obj.get("Tags");
            alreadyResizedTags = (JSONArray) obj.get("AlreadyResizedTags");
            for(Object tag : tags) {
                if(((String)tag).equalsIgnoreCase(MediaTagConstants.LOGO)||((String)tag).equalsIgnoreCase(MediaTagConstants.STAGE)||((String)tag).equalsIgnoreCase(MediaTagConstants.TEASER)||((String)tag).equalsIgnoreCase(MediaTagConstants.GALLERY)) {
                    sizeTags.add((String)tag);
                }
            }
            JSONObject sizes = new JSONObject();
            if(sizeTags.size() == 0) {
                if(alreadyResizedTags.size() == 0) {//If there are already resized tags, this means, that there already is a converted image for all size tag and that the default case does not have to create a converted image without size tag
                    resizeAndAddToAr(img, sizes, "", obj, fileType);//Does not actually resize, because of the empty tag
                }
            }else {
                for(String sizeTag : sizeTags) {
                    resizeAndAddToAr(img, sizes, sizeTag, obj, fileType);
                }
            }
            obj.put("Sizes", sizes);
            resp.getWriter().write(obj.toJSONString());
        }else {
            //ERROR
        }
        if(alreadyResizedTags != null) {
            for(Object tag : alreadyResizedTags) {
                tags.add(tag);
            }
        }
        obj.remove("AlreadyResizedTags");
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
    
    private void resizeAndAddToAr(BufferedImage img, JSONObject sizes, String tag,JSONObject obj, String fileType) {
        String imgString;
        switch(tag) {
        case MediaTagConstants.LOGO:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT, fileType, false);
            sizes.put(MediaTagConstants.LOGO, imgString);
            break;
        case MediaTagConstants.STAGE:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT, fileType, false);
            sizes.put(MediaTagConstants.STAGE, imgString);
            break;
        case MediaTagConstants.TEASER:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, fileType, false);
            sizes.put(MediaTagConstants.TEASER, imgString);
            break;
        default://case for Gallery or for no size tags
            imgString = ImageConverter.convertToBase64(img,fileType);
            sizes.put(MediaTagConstants.GALLERY, imgString);
            break;
        }
    }
}
