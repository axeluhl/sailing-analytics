package com.sap.sse.debranding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ClientConfigurationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, wrappedResponse);
        String body = wrappedResponse.toString();
        String replaced = new String(body);
        final boolean deBrandingActive = Boolean
                .valueOf(System.getProperty(ClientConfigurationServlet.DEBRANDING_PROPERTY_NAME, "false"));
        for (Map.Entry<String, String> item : createReplacementMap(deBrandingActive).entrySet()) {
            replaced = replaced.replace("${" + item.getKey() + "}", item.getValue());
        }
        response.getWriter().write(replaced);
    }

    private Map<String, String> createReplacementMap(boolean deBrandingActive) {
        final Map<String, String> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (deBrandingActive) {
            title = "";
            whitelabeled = "-whitelabeled";
        } else {
            title = "SAP ";
            whitelabeled = "";
        }
        map.put("SAP", title);
        map.put("debrandingActive", Boolean.toString(deBrandingActive));
        map.put("whitelabeled", whitelabeled);
        return map;
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

    private static class CharResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayOutputStream output;

        public String toString() {
            return output.toString();
        }

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            response.setBufferSize(0); // prevent buffering here to not need to think about it later
            output = new ByteArrayOutputStream();
        }

        public PrintWriter getWriter() {
            return new PrintWriter(output);
        }

        @Override
        public void setResponse(ServletResponse response) {
            super.setResponse(response);
        }

        @Override
        public void setBufferSize(int size) {
            super.setBufferSize(0);
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException {
            // intentionally left blank, not supporting buffering here
        }

        @Override
        public void reset() {
            // intentionally left blank, not supporting buffering here
        }

        @Override
        public void resetBuffer() {
            // intentionally left blank, not supporting buffering here
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {

                @Override
                public void write(int octet) throws IOException {
                    output.write(octet);
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // disable asynchronous writing, not needed for static pages
                    throw new IllegalStateException("not supported by ClientConfigurationServlet");
                }

                @Override
                public boolean isReady() {
                    return true;
                }
            };
        }
    }
}
