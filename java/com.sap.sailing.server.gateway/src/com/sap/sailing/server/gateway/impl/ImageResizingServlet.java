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
        JSONArray tags;
        List<String> sizeTags = new ArrayList<>();
        BufferedImage img = null;
        try {
            img = ImageConverter.isToBi(getService().getFileStorageManagementService().getActiveFileStorageService().loadFile(new URI((String)obj.get("URI"))));
        } catch (NoCorrespondingServiceRegisteredException | OperationFailedException
                | InvalidPropertiesException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        
        if(obj != null) {
            tags = (JSONArray) obj.get("Tags");
            for(Object tag : tags) {
                if(((String)tag).equalsIgnoreCase(MediaTagConstants.LOGO)||((String)tag).equalsIgnoreCase(MediaTagConstants.STAGE)||((String)tag).equalsIgnoreCase(MediaTagConstants.TEASER)) {
                    sizeTags.add((String)tag);
                }
            }
            JSONArray ar = new JSONArray();
            if(sizeTags.size() == 0) {
                obj.put("SizeTag", "");
                resizeAndAddToAr(img, ar, "", obj);//Does not actually resize, because of the empty tag
            }else {
                for(String sizeTag : sizeTags) {
                    int[] dimensions = getDimensions(sizeTag, img);
                    obj.remove("Width");
                    obj.remove("Height");
                    obj.put("Width", dimensions[0]);
                    obj.put("Height", dimensions[1]);
                    obj.put("SizeTag", sizeTag);
                    resizeAndAddToAr(img, ar, sizeTag, obj);
                    obj = getObjFromJSON(jsonString);//obj has to be created again, because of the put resizeAndConvert
                }
            }
            resp.getWriter().write(ar.toJSONString());
        }else {
            //ERROR
        }
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

    public static int[] getDimensions(String tag, BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        switch(tag) {
        case MediaTagConstants.LOGO:
            return ImageConverter.calculateActualDimensions(width, height, MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT, false);
        case MediaTagConstants.STAGE:
            return ImageConverter.calculateActualDimensions(width, height, MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT, false);
        case MediaTagConstants.TEASER:
            return ImageConverter.calculateActualDimensions(width, height, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, false);
        default:
            return new int[] {0,0};
        }
    }
    
    private void resizeAndAddToAr(BufferedImage img, JSONArray ar, String tag,JSONObject obj) {
        String imgString;
        switch(tag) {
        case MediaTagConstants.LOGO:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT, ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1), false);
            break;
        case MediaTagConstants.STAGE:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT, ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1), false);
            break;
        case MediaTagConstants.TEASER:
            imgString = ImageConverter.resizeAndConvertToBase64(img, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1), false);
            break;
        default:
            imgString = ImageConverter.convertToBase64(img, ((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")));
            break;
        }
        if(imgString != null && !imgString.equals("")) {
            obj.put("FileType",((String)obj.get("URI")).substring(((String)obj.get("URI")).lastIndexOf(".")+1));
            obj.put("Base64Code", imgString);
            ar.add(obj);
        }else{
            ar.add("Error resizing to " + tag + " dimensions.");
        }
    }
}
