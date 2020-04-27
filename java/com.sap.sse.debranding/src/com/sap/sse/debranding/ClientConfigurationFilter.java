package com.sap.sse.debranding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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
        HttpServletRequestWrapper wrappedRequest = new HeadersRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, wrappedResponse);
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

    private static class HeadersRequestWrapper extends HttpServletRequestWrapper {
        private HeadersRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (name.equalsIgnoreCase("Cache-Control")) {
                return "no-cache";
            }
            if (name.equalsIgnoreCase("If-Modified-Since")) {
                return "Thu, 1. Jan 1970 00:00:00 GMT";
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name.equalsIgnoreCase("Cache-Control")) {
                return new OneElementEnum("no-cache");
            }
            if (name.equalsIgnoreCase("If-Modified-Since")) {
                return new OneElementEnum("Thu, 1. Jan 1970 00:00:00 GMT");
            }
            return super.getHeaders(name);
        }

        @Override
        public long getDateHeader(String name) {
            if (name.equalsIgnoreCase("If-Modified-Since")) {
                return 0;
            }
            return super.getDateHeader(name);
        }
        
    }

     private static class OneElementEnum implements Enumeration<String> {
            private boolean delivered = false;
            private String value;
            
            public OneElementEnum(String value) {
                this.value = value;
            }

            @Override
            public boolean hasMoreElements() {
                return !delivered;
            }

            @Override
            public String nextElement() {
                delivered = true;
                return value;
            }
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
//            super.flushBuffer();
        }

        @Override
        public void reset() {
//            super.reset();
        }

        @Override
        public void resetBuffer() {
//            super.resetBuffer();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                
                @Override
                public void write(int arg0) throws IOException {
                    output.write(arg0);
                }
                
                @Override
                public void setWriteListener(WriteListener writeListener) {
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
