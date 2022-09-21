package com.sap.sse.gwt.server;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sse.common.TimePoint;

public abstract class DelegatingProxiedRemoteServiceServlet extends ProxiedRemoteServiceServlet {
    private static final Logger logger = Logger.getLogger(DelegatingProxiedRemoteServiceServlet.class.getName())
            ;
    private static final long serialVersionUID = -5543378343472849437L;

    /**
     * Overwritten version of {@link RemoteServiceServlet#processCall(RPCRequest)} that calls
     * {@link #invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int, Writer)} instead of
     * {@link RPC#invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)}.
     * {@link #invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int, Writer)} in turn calls
     * {@link #encodeResponseForSuccess(Method, SerializationPolicy, int, Object)} which subclasses can override
     * in order to customize result serialization, e.g., by picking a cached serialized version.
     */
    @Override
    public void processCall(RPCRequest rpcRequest, Writer writer) throws SerializationException {
        final TimePoint startOfProcessCall = beforeProcessCall();
        try {
            final Object delegate = this;
            try {
                onAfterRequestDeserialized(rpcRequest);
                invokeAndEncodeResponse(delegate, rpcRequest.getMethod(), rpcRequest.getParameters(),
                        rpcRequest.getSerializationPolicy(), rpcRequest.getFlags(), writer);
            } catch (IncompatibleRemoteServiceException ex) {
                log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
                RPC.encodeResponseForFailedRequest(rpcRequest, ex, writer);
            } catch (RpcTokenException tokenException) {
                log("An RpcTokenException was thrown while processing this call.", tokenException);
                RPC.encodeResponseForFailedRequest(rpcRequest, tokenException, writer);
            }
        } finally {
            afterProcessCall(rpcRequest, startOfProcessCall);
        }
    }

    /**
     * Similar to {@link RPC#invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)}, but
     * calls {@link #encodeResponseForSuccess(Method, SerializationPolicy, int, Object)}, giving subclasses
     * a way to override result serialization.
     * @param writer the writer to serialize the output to
     */
    private void invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy, int flags, Writer writer) throws SerializationException {
        if (serviceMethod == null) {
            throw new NullPointerException("serviceMethod");
        }
        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy");
        }
        try {
            Object result = serviceMethod.invoke(target, args);
            encodeResponseForSuccess(serviceMethod, serializationPolicy, flags, result, writer);
        } catch (IllegalAccessException e) {
            SecurityException securityException = new SecurityException(formatIllegalAccessErrorMessage(target, serviceMethod));
            securityException.initCause(e);
            throw securityException;
        } catch (IllegalArgumentException e) {
            SecurityException securityException = new SecurityException(formatIllegalArgumentErrorMessage(target, serviceMethod, args));
            securityException.initCause(e);
            throw securityException;
        } catch (InvocationTargetException e) {
            // Try to encode the caught exception
            Throwable cause = resolveCause(e);
            logger.log(Level.SEVERE, "Uncaught exception, forwarded to client", cause);
            try {
                RPC.encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags, writer);
            } catch (SerializationException se) {
                logger.warning("Couldn't serialize exception of type " + cause.getClass()
                + "; serializing a RuntimeException with the message \"" + cause.getMessage() + "\" only.");
                RPC.encodeResponseForFailure(serviceMethod, new RuntimeException(cause.getMessage()), serializationPolicy, flags, writer);
            }
        }
    }

    private Throwable resolveCause(InvocationTargetException e) {
        Throwable result = e;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    /**
     * To be overwritten by subclasses to customize the serialization of the result object.
     */
    protected void encodeResponseForSuccess(Method serviceMethod, SerializationPolicy serializationPolicy, int flags,
            Object result, Writer writer) throws SerializationException {
        RPC.encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags, writer);
    }

    /**
     * Taken from {@link RPC}.
     */
    private String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to access inaccessible method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");
        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }
        sb.append("; this is either misconfiguration or a hack attempt");
        return sb.toString();
    }

    /**
     * Taken from {@link RPC}.
     */
    private String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod, Object[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to invoke method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");
        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }
        sb.append(" with invalid arguments");
        if (args != null && args.length > 0) {
            sb.append(Arrays.asList(args));
        }
        return sb.toString();
    }

    private String getSourceRepresentation(Method method) {
        return method.toString().replace('$', '.');
    }

    /**
     * Taken from {@link RPC}.
     */
    private String printTypeName(Class<?> type) {
        // Primitives
        if (type.equals(Integer.TYPE)) {
            return "int";
        } else if (type.equals(Long.TYPE)) {
            return "long";
        } else if (type.equals(Short.TYPE)) {
            return "short";
        } else if (type.equals(Byte.TYPE)) {
            return "byte";
        } else if (type.equals(Character.TYPE)) {
            return "char";
        } else if (type.equals(Boolean.TYPE)) {
            return "boolean";
        } else if (type.equals(Float.TYPE)) {
            return "float";
        } else if (type.equals(Double.TYPE)) {
            return "double";
        }
        // Arrays
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return printTypeName(componentType) + "[]";
        }
        // Everything else
        return type.getName().replace('$', '.');
    }
}
