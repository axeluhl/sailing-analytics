package com.sap.sse.replication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sap.sse.util.HttpUrlConnectionHelper;

public interface OperationsToMasterSender<S, O extends OperationWithResult<S, ?>> extends Replicable<S, O>, UnsentOperationsForMasterQueue {
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
        final HttpURLConnection connection = (HttpURLConnection) HttpUrlConnectionHelper.redirectConnection(url, "POST"); // sets doOutput to true
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
}
