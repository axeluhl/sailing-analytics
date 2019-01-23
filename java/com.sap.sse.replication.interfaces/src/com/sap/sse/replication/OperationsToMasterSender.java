package com.sap.sse.replication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import com.sap.sse.common.WithID;

public interface OperationsToMasterSender<S, O extends OperationWithResult<S, ?>> extends OperationsToMasterSendingQueue, WithID {
    final Logger logger = Logger.getLogger(OperationsToMasterSender.class.getName());
    
    /**
     * @return the descriptor of the master from which this replica is replicating this {@link Replicable}, or
     *         {@code null} if this {@link Replicable} is currently running as a master.
     */
    ReplicationMasterDescriptor getMasterDescriptor();

    /**
     * Writes an operation to an output stream such that it can be read by {@link #readOperation}.
     * 
     * @param closeStream
     *            if <code>true</code>, the stream will be closed after having written the operation; in any case, the
     *            content written will be flushed to the <code>outputStream</code> so that the caller may continue to
     *            invoke this method for other operations and/or on other replicables without producing corrupt data.
     */
    void writeOperation(OperationWithResult<?, ?> operation, OutputStream outputStream, boolean closeStream) throws IOException;

    /**
     * When a replica has initiated (not received through replication) an operation, this operation needs to be sent to
     * the master for execution from where it will replicate across the replication tree. This method uses the
     * {@link ReplicationMasterDescriptor#getSendReplicaInitiatedOperationToMasterURL(String) URL for sending an
     * operation to the replication servlet on the master} and through the POST request's output stream first sends the
     * target replicable's ID as a string using a {@link DataOutputStream}, then
     * {@link #writeOperation(OperationWithResult, OutputStream, boolean) serializes the operation}.<p>
     */
    default <T> void sendReplicaInitiatedOperationToMaster(OperationWithResult<S, T> operation) throws IOException {
        ReplicationMasterDescriptor masterDescriptor = getMasterDescriptor();
        assert masterDescriptor != null;
        final OperationWithResultWithIdWrapper<S, T> operationWithResultWithIdWrapper = new OperationWithResultWithIdWrapper<S, T>(operation);
        // TODO bug4018: if sending the operation fails, e.g., because of an HTTP response code != 2xx, enqueue the operation for retry
        addOperationSentToMasterForReplication(operationWithResultWithIdWrapper);
        URL url = masterDescriptor.getSendReplicaInitiatedOperationToMasterURL(this.getId().toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true); // we want to post the serialized operation
        logger.info("Sending operation "+operation+" to master "+masterDescriptor+"'s replicable with ID "+this+" for initial execution and replication");
        connection.connect();
        OutputStream outputStream = connection.getOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeUTF(getId().toString());
        this.writeOperation(operationWithResultWithIdWrapper, outputStream, /* closeStream */ true);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        bufferedReader.close();
    }

    /**
     * Records that the {@code operationWithResultWithIdWrapper} was sent to master for further processing and replication.
     * The {@link #hasSentOperationToMaster(OperationWithResult)} will return {@code true} for an operation equal to
     * {@code operationWithResultWithIdWrapper} exactly once after this method has returned.
     */
    void addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper<S, ?> operationWithResultWithIdWrapper);

    /**
     * If an operation equal to <code>operationWithResultWithIdWrapper</code> has previously been passed to a call to
     * {@link OperationsToMasterSender#addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper)}, the call returns <code>true</code>
     * exactly once.
     */
    boolean hasSentOperationToMaster(OperationWithResult<S, ?> operation);
}
