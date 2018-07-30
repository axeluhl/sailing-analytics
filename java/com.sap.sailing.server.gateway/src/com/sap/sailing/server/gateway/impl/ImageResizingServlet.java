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
        //In following size tag will describe the tags, that have a defined size in MediaConstants, which is LOGO, TEASER and STAGE at the moment
        String jsonString = new BufferedReader(new InputStreamReader(req.getInputStream())).readLine();
        JSONObject obj = getObjFromJSON(jsonString);
        List<String> resizeTags = new ArrayList<>();
        List<String> notResizeSizeTags = new ArrayList<>();
        JSONArray toReturnArray = new JSONArray();
        if(obj != null) {
            String fileType = ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1);
            JSONObject tags = (JSONObject) obj.get("Tags");
            InputStream is = getInputStreamFromURIString(((String)obj.get("URI")));
            BufferedImage img = ImageConverter.isToBi(is);
            is.close();
            
            for(Object tagKey : tags.keySet()) {
                if((boolean) tags.get(tagKey)) {
                    resizeTags.add((String)tagKey);//size tags, that have the resize checkBox checked
                }else if(tags.get(tagKey) != null){
                    notResizeSizeTags.add((String)tagKey);//size tags, that not have the resize checkBox checked
                }//else all non-size tags
            }
            
            for(String resizeTag : resizeTags) {
                for(String toDeleteTag : resizeTags) {//delete all size tags
                    tags.remove(toDeleteTag);
                }
                for(String toDeleteTag : notResizeSizeTags) {//delete all size tags
                    tags.remove(toDeleteTag);
                }
                tags.put(resizeTag, "Done");//read the deleted specific size tag
                resizeAndAddToAr(img, toReturnArray, obj, resizeTag, fileType);
                obj = getObjFromJSON(jsonString);//reset the object
                tags = (JSONObject) obj.get("Tags");//and the tags
            }
            if(notResizeSizeTags.isEmpty()) {//if there is no size tag that does not need a resize we can delete the original source
                try {
                    getService().getFileStorageManagementService().getActiveFileStorageService().removeFile(new URI((String)obj.get("URI")));
                } catch (NoCorrespondingServiceRegisteredException | OperationFailedException
                        | InvalidPropertiesException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }else {
                for(String resizeTag : notResizeSizeTags) {
                    for(String toDeleteTag : resizeTags) {//delete all size tags
                        tags.remove(toDeleteTag);
                    }
                    for(String toDeleteTag : notResizeSizeTags) {//delete all size tags
                        tags.remove(toDeleteTag);
                    }
                    tags.put(resizeTag, "Done");//re-add the deleted specific size tag
                    toReturnArray.add(obj);
                    obj = getObjFromJSON(jsonString);//reset the object for next size-tag-iteration
                    tags = (JSONObject) obj.get("Tags");//and the tags
                }
            }
            
        }else {
            //ERROR
        }
        resp.getWriter().write(toReturnArray.toJSONString());
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
    
    private void resizeAndAddToAr(BufferedImage img, JSONArray array, JSONObject resizedImageObject, String tag, String fileType) {
        String imgUri = "";
        BufferedImage resizedImage = null;
        switch(tag) {
        case MediaTagConstants.LOGO:
            resizedImage = ImageConverter.resize(img, MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT, fileType, false);
            break;
        case MediaTagConstants.STAGE:
            resizedImage = ImageConverter.resize(img, MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT, fileType, false);
            break;
        case MediaTagConstants.TEASER:
            resizedImage = ImageConverter.resize(img, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, fileType, false);
            break;
        default://can not occur, because we only loop over the sizeTags, which are the same as in the switch case
            break;
        }
        try (InputStream resizedImageInputStream = ImageConverter.biToIs(resizedImage,fileType)){
            imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(resizedImageInputStream, "."+fileType, resizedImageInputStream.available()).toString();
        } catch (NoCorrespondingServiceRegisteredException | IOException | OperationFailedException
                | InvalidPropertiesException e) {
            e.printStackTrace();
        }
        resizedImageObject.put("URI", imgUri);
        resizedImageObject.put("Width", resizedImage.getWidth());
        resizedImageObject.put("Height", resizedImage.getHeight());
        array.add(resizedImageObject);
    }
}
