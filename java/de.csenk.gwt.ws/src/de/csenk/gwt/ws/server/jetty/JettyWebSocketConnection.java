/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.csenk.gwt.ws.server.jetty;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import de.csenk.gwt.ws.shared.Connection;
import de.csenk.gwt.ws.shared.FilterChain;
import de.csenk.gwt.ws.shared.Handler;
import de.csenk.gwt.ws.shared.Sender;
import de.csenk.gwt.ws.shared.filter.DefaultFilterChain;

/**
 * @author senk.christian@googlemail.com
 * @date 25.08.2010
 * @time 13:56:30
 * 
 */
public class JettyWebSocketConnection extends WebSocketAdapter implements Connection {

    private final Handler handler;

    private Session session;
    private Sender sender;

    private final FilterChain filterChain;

    /**
     * @param handler
     */
    public JettyWebSocketConnection(final Handler handler) {
        this.handler = handler;
        this.filterChain = new DefaultFilterChain(this);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        filterChain.fireMessageReceived(new String(payload, offset, len));
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        filterChain.fireConnectionClosed();
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        sender = new OutboundSender(session);
        filterChain.fireConnectionOpened();
    }

    @Override
    public void onWebSocketText(String message) {
        filterChain.fireMessageReceived(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.csenk.gwt.ws.shared.Connection#close()
     */
    @Override
    public void close() {
        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.csenk.gwt.ws.shared.Connection#getFilterChain()
     */
    @Override
    public FilterChain getFilterChain() {
        return filterChain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.csenk.gwt.ws.shared.Connection#getHandler()
     */
    @Override
    public Handler getHandler() {
        return handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.csenk.gwt.ws.shared.Connection#getSender()
     */
    @Override
    public Sender getSender() {
        return sender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.csenk.gwt.ws.shared.Connection#send(java.lang.Object)
     */
    @Override
    public void send(Object message) {
        filterChain.fireSend(message);
    }

}
