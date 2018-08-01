package com.sap.sailing.server.gateway.impl;

import static java.lang.Math.toIntExact;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.media.ImageConverter;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

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
            JSONArray tags = (JSONArray) obj.get("Tags");
            JSONObject resizeMap = (JSONObject) obj.get("ResizeMap");
            IIOMetadata metadata = null;
            BufferedImage img;
            try(InputStream is = getInputStreamFromURIString(((String)obj.get("URI")))){
                ImageReader reader = ImageIO.getImageReadersBySuffix(fileType).next();
                reader.setInput(ImageIO.createImageInputStream(is));
                metadata = reader.getImageMetadata(0);
                img = reader.read(0);
            }catch(Exception e) {
                InputStream is = getInputStreamFromURIString(((String)obj.get("URI")));
                img = ImageConverter.isToBi(is);
                is.close();
            }
            
            for(Object tagKey : resizeMap.keySet()) {
                if((boolean) resizeMap.get(tagKey)) {
                    resizeTags.add((String)tagKey);//size tags, that have the resize checkBox checked
                }else{
                    notResizeSizeTags.add((String)tagKey);//size tags, that not have the resize checkBox checked
                }
            }
            
            for(String resizeTag : resizeTags) {
                for(String toDeleteTag : resizeTags) {//delete all size tags
                    tags.remove(toDeleteTag);
                }
                for(String toDeleteTag : notResizeSizeTags) {//delete all size tags
                    tags.remove(toDeleteTag);
                }
                tags.add(resizeTag);//read the deleted specific size tag
                resizeAndAddToAr(img, toReturnArray, obj, resizeTag, fileType, metadata);
                obj = getObjFromJSON(jsonString);//reset the object
                tags = (JSONArray) obj.get("Tags");//and the tags
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
                    tags.add(resizeTag);//re-add the deleted specific size tag
                    toReturnArray.add(obj);
                    obj = getObjFromJSON(jsonString);//reset the object for next size-tag-iteration
                    tags = (JSONArray) obj.get("Tags");//and the tags
                }
            }
            
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
    
    private void resizeAndAddToAr(BufferedImage img, JSONArray array, JSONObject resizedImageObject, String tag, String fileType, IIOMetadata metaData) {
        URI imgUri = null;
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
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream())){
            //the following should write the exif data of the image to all copies of the image//it should already work, but due to a bug the data array stays empty
            Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(fileType);
            byte[] bytes = null;
            while(writers.hasNext() && bytes == null) {
                ImageWriter writer = writers.next();
                if(writer != null) {
                    writer.setOutput(ios);
                    IIOImage iioImage = new IIOImage(resizedImage, null, metaData);
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(param);
                    writer.write(streamMetadata, iioImage, param);
                    writer.dispose();
                    bytes = new byte[toIntExact(ios.length())*2];
                    ios.read(bytes);
                    bytes = trim(bytes);
                    if(bytes.length == 0)
                        bytes = null;
                }
            }
            if(bytes == null) {
                InputStream is = ImageConverter.biToIs(resizedImage, fileType);
                imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(is, "."+fileType, new Long(is.available()));
                is.close();
            }else {
                
                ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                imgUri = getService().getFileStorageManagementService().getActiveFileStorageService().storeFile(byteStream, "."+fileType, new Long(byteStream.available()));
                byteStream.close();
            }
        } catch (IOException | NoCorrespondingServiceRegisteredException | OperationFailedException | InvalidPropertiesException e1) {
            e1.printStackTrace();
        }
        
        
        
        resizedImageObject.put("URI", imgUri.toString());
        resizedImageObject.put("Width", resizedImage.getWidth());
        resizedImageObject.put("Height", resizedImage.getHeight());
        array.add(resizedImageObject);
    }
    
    static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }
}
