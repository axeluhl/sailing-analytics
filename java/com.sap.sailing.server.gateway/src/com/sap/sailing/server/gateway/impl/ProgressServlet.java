package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.sap.sse.common.fileupload.FileUploadConstants;

public class ProgressServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private static final long serialVersionUID = 6179739052238280324L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        final HttpSession session = request.getSession(true);
        if (session == null) {
            logger.warning("Sorry, session is null"); // just to be safe
        } else {
            final ProgressListener progressListener = (ProgressListener) session
                    .getAttribute(AbstractFileUploadServlet.PROGRESS_LISTENER_SESSION_ATTRIBUTE_NAME);
            if (progressListener == null) {
                logger.warning("Progress listener is null");
            } else {
                final JSONObject json = new JSONObject();
                json.put(FileUploadConstants.PROGRESS_PERCENTAGE, progressListener.getPercentDone());
                json.put(FileUploadConstants.PROGRESS_BYTE_DONE, progressListener.getTheBytesRead());
                json.put(FileUploadConstants.PROGRESS_BYTE_TOTAL, progressListener.getTheContentLength());
                json.writeJSONString(response.getWriter());
            }
        }
    }
}
