package com.sap.sailing.domain.tractracadapter.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tractracadapter.CourseUpdateResponse;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;

public class CourseDesignChangedByRaceCommitteeHandler implements CourseDesignChangedListener {
    
    private JsonSerializer<CourseBase> courseSerializer;
    private JsonDeserializer<CourseUpdateResponse> courseUpdateDeserializer;
    private final URI courseDesignUpdateURI;
    private final Serializable regattaId;
    private final Serializable raceId;
    
    private SecretKey secretKey;
    
    private final static byte[] SecretSalt = {-56, -34, -21, -81, -102, -88, -110, -106};
    private final static byte[] SecretIv = {-50, -11, -87, -81, 113, 109, 88, 51, -15, 61, -34, -17, -120, -34, 87, -40};
    private final static String SharedPassword = "h49km348frk38HHs";
    private final static String EncryptionAlgorithm = "AES";
    private final static String SecretKeyGenerationAlgorithm = "PBKDF2WithHmacSHA1";
    private final static String CipherAlgorithm = "AES/CBC/PKCS5Padding";
    
    private final static String HttpPostRequestMethod = "POST";
    private final static String ContentType = "Content-Type";
    private final static String ContentLength = "Content-Length";
    private final static String ContentTypeApplicationJson = "application/json";
    private final static String EncodingUtf8 = "UTF-8";
    private final static String CourseUpdateUrlTemplate = "%s?eventid=%s&raceid=%s&username=%s";
    private final static String ResponseCodeForFailure = "FAILURE";
    
    public CourseDesignChangedByRaceCommitteeHandler(URI courseDesignUpdateURI, Serializable regattaId, Serializable raceId) {
        this.courseDesignUpdateURI = courseDesignUpdateURI;
        this.regattaId = regattaId;
        this.raceId = raceId;
        this.secretKey = null;
        this.courseSerializer = new CourseJsonSerializer(
                new CourseBaseJsonSerializer(
                        new WaypointJsonSerializer(
                                new ControlPointJsonSerializer(
                                        new MarkJsonSerializer(), 
                                        new GateJsonSerializer(new MarkJsonSerializer())))));
        this.courseUpdateDeserializer = new CourseUpdateResponseDeserializer();
    }

    @Override
    public void courseDesignChanged(CourseBase newCourseDesign) throws MalformedURLException, IOException {
        JSONObject serializedCourseDesign = courseSerializer.serialize(newCourseDesign);
        String payload = serializedCourseDesign.toJSONString();

        URL currentCourseDesignURL = buildCourseUpdateURL();
        HttpURLConnection connection = (HttpURLConnection) currentCourseDesignURL.openConnection();
        try {
            setConnectionProperties(connection, payload);

            sendWithPayload(connection, payload);

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                Object responseBody = JSONValue.parseWithException(reader);
                JSONObject responseObject = Helpers.toJSONObjectSafe(responseBody);

                CourseUpdateResponse courseUpdateResponse = courseUpdateDeserializer.deserialize(responseObject);
                if (courseUpdateResponse.getStatus().equals(ResponseCodeForFailure)) {
                    System.out.println(courseUpdateResponse.getMessage());
                }
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendWithPayload(HttpURLConnection connection, String payload) throws IOException {
        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.writeBytes(payload);
        writer.flush();
        writer.close();
    }

    private void setConnectionProperties(HttpURLConnection connection, String payload) throws ProtocolException {
        connection.setRequestMethod(HttpPostRequestMethod);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty(ContentType, ContentTypeApplicationJson);
        connection.addRequestProperty(ContentLength, String.valueOf(payload.getBytes().length));
    }
    
    private URL buildCourseUpdateURL() throws MalformedURLException, UnsupportedEncodingException {
        String encryptedRegattaId = "";
        String encryptedRaceId = "";
        String encryptedUsername = "";
        
        try {
            encryptedRegattaId = encryptParameter(this.regattaId.toString(), SharedPassword);
            encryptedRaceId = encryptParameter(this.raceId.toString(), SharedPassword);
            encryptedUsername = encryptParameter("armin.zamani.farahani@sap.com", SharedPassword);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        String url = String.format(CourseUpdateUrlTemplate, 
                this.courseDesignUpdateURI.toString(), 
                URLEncoder.encode(encryptedRegattaId, EncodingUtf8), 
                URLEncoder.encode(encryptedRaceId, EncodingUtf8),
                URLEncoder.encode(encryptedUsername, EncodingUtf8));
        return new URL(url);
    }
    
    private String encryptParameter(String plainText, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        SecretKey secret = getOrCreateSecretKey(password);
        /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance(CipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(SecretIv));
        byte[] ciphertext = cipher.doFinal(plainText.getBytes(EncodingUtf8));
        return printHex(ciphertext);
    }

    private SecretKey getOrCreateSecretKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (secretKey == null) {
            secretKey = setupEncryptionSecret(password);
        }
        return secretKey;
    }
    private SecretKey setupEncryptionSecret(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SecretKeyGenerationAlgorithm);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SecretSalt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), EncryptionAlgorithm);
        return secret;
    }
    
    private String printHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", (b & 0xFF)));
        }
        return sb.toString();
    }


}
