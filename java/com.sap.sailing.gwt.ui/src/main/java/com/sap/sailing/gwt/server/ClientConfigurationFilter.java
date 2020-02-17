package com.sap.sailing.gwt.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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

import org.apache.commons.lang3.StringUtils;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.shared.Branding;


public class ClientConfigurationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response0, FilterChain chain)
            throws IOException, ServletException {

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        HttpServletResponse response = (HttpServletResponse) response0;

        HttpServletResponse responseWrapper = new HttpServletResponseWrapper(response) {

            private final ServletOutputStream sos = new ServletOutputStream() {

                @Override
                public void write(int character) throws IOException {
                    buffer.write(character);
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // intentionally left blank, not implemented
                    System.out.println("this is a breakpoint");
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void close() throws IOException {
                    super.close();
                }

                @Override
                public void flush() throws IOException {
                    super.flush();
                }
            };

            private final PrintWriter writer = new PrintWriter(sos);

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return sos;
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return writer;
            }

            @Override
            public int getBufferSize() {
                return 0;
            }

            @Override
            public void flushBuffer() throws IOException {
                // do nothing here, output will be flushed in the end otherwise string replacement might not work
                System.out.println("this is a breakpoint");
            }

            @Override
            public void setBufferSize(int size) {
                super.setBufferSize(size);
            }

            @Override
            public void setStatus(int sc) {
                // disable caching
                if (sc == 304) {
                    sc = 200;
                }
                super.setStatus(sc);

            }

            @Override
            public void setStatus(int sc, String sm) {
                // disable caching
                if (sc == 304) {
                    sc = 200;
                }
                super.setStatus(sc, sm);
            }

        };
        chain.doFilter(request, responseWrapper);
        
        String title = "";
        String faviconPath = "images/whitelabel.ico";
        String appiconPath = "images/sailing-app-icon.png";
        if (Branding.getInstance().isActive()) {
            title = "SAP ";
            faviconPath = "images/sap.ico";
            appiconPath = "images/sap-sailing-app-icon.png";
        }
        
        String content = buffer.toString();
        String replaced = content.replace("${SAP}", title).replace("${faviconPath}", faviconPath).replace("${appiconPath}", appiconPath);

        
        response.getOutputStream().write(replaced.getBytes());
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

}
