package com.sap.sse.operationaltransformation.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.operationaltransformation.ClientServerOperationPair;
import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.operationaltransformation.Peer;
import com.sap.sse.operationaltransformation.Peer.Role;
import com.sap.sse.operationaltransformation.PeerImpl;
import com.sap.sse.operationaltransformation.Transformer;
import com.sap.sse.operationaltransformation.test.util.Base64;

public class OperationalTransformationTest {
    private Peer<StringInsertOperation, StringState> server;
    private Peer<StringInsertOperation, StringState> client1, client2;
    
    public static class StringInsertOperation implements Operation<StringState> {
        private int pos;
        private String s;

        public StringInsertOperation(int pos, String s) {
            this.pos = pos;
            this.s = s;
        }

        public int getPos() {
            return pos;
        }

        public String getS() {
            return s;
        }

        public String toString() {
            return "insert(" + getPos() + ", \"" + getS() + "\")";
        }

        @Override
        public StringState applyTo(StringState toState) {
            return toState.apply(this);
        }

        @Override
        public boolean requiresSynchronousExecution() {
            return false;
        }
    }
   
    public static class StringInsertTransformer implements Transformer<StringState, StringInsertOperation> {
        @Override
	public ClientServerOperationPair<StringState, StringInsertOperation> transform(StringInsertOperation clientOp, StringInsertOperation serverOp) {
	    final StringInsertOperation resultClientOp;
	    final StringInsertOperation resultServerOp;
	    if (clientOp == null || serverOp == null) {
	        resultClientOp = clientOp;
	        resultServerOp = serverOp;
	    } else if (clientOp.getPos() >= serverOp.getPos()) {
		resultServerOp = serverOp;
		resultClientOp = new StringInsertOperation(clientOp.getPos()+serverOp.getS().length(), clientOp.getS());
	    } else {
		resultClientOp = clientOp;
		resultServerOp = new StringInsertOperation(serverOp.getPos()+clientOp.getS().length(), serverOp.getS());
	    }
	    return new ClientServerOperationPair<StringState, StringInsertOperation>(resultClientOp, resultServerOp);
	}
    }
    
    public static class StringState {
	final private String state;
	final private UUID id;

	public StringState(String state) {
	    super();
	    id = UUID.randomUUID();
	    this.state = state;
	}
	
	public String getState() {
	    return state;
	}
	
	public boolean equals(Object o) {
	    return o instanceof StringState &&
	    	this.getState().equals(((StringState) o).getState()) &&
	    	this.id.equals(((StringState) o).id);
	}
	
	public int hashCode() {
	    return getState().hashCode() ^ id.hashCode();
	}

	public StringState apply(StringInsertOperation operation) {
	    final String s = this.getState().substring(0, operation.getPos()) +
	    			operation.getS()+this.getState().substring(operation.getPos());
	    final StringState result = new StringState(s);
	    return result;
	}

	private UUID getId() {
	    return id;
	}

	public String toString() {
	    return "\""+getState()+"\" @ "+getId();
	}
    }
    
    public static class SimpleClient extends PeerImpl<StringInsertOperation, StringState> {
	public SimpleClient(String name, StringState initialState) {
	    super(name, new StringInsertTransformer(), initialState, Role.SERVER);
	}
    }
    
    @Before
    public void setUp() {
	server = new PeerImpl<StringInsertOperation, StringState>(
		"Server", new StringInsertTransformer(), new StringState(""), Role.SERVER);
	client1 = new PeerImpl<StringInsertOperation, StringState>("Client1", new StringInsertTransformer(), server);
	client2 = new PeerImpl<StringInsertOperation, StringState>("Client2", new StringInsertTransformer(), server);
    }
    
    @Test
    public void testBasicTransformation() {
	client1.apply(new StringInsertOperation(0, "abc"));
	client2.apply(new StringInsertOperation(0, "def"));
	client1.waitForNotRunning();
	client2.waitForNotRunning();
	server.waitForNotRunning();
	assertEquals(server.getCurrentState().getState(), client1.getCurrentState().getState());
	assertEquals(server.getCurrentState().getState(), client2.getCurrentState().getState());
	assertEquals(6, server.getCurrentState().getState().length());
	assertTrue(server.getCurrentState().getState().equals("abcdef") || server.getCurrentState().getState().equals("defabc"));
    }

    @Test
    public void testInsertAtBeginningAndEndTransformation() throws InterruptedException {
        server.apply(new StringInsertOperation(0, "0123456789"));
        server.waitForNotRunning();
        client2.apply(new StringInsertOperation(10, "def"));
        client2.waitForNotRunning();
        client1.apply(new StringInsertOperation(0, "abc"));
        client1.waitForNotRunning();
        client2.waitForNotRunning();
        server.waitForNotRunning();
        assertEquals(server.getCurrentState().getState(), client1.getCurrentState().getState());
        assertEquals(server.getCurrentState().getState(), client2.getCurrentState().getState());
        assertEquals(16, server.getCurrentState().getState().length());
        assertEquals("abc0123456789def", server.getCurrentState().getState());
    }

    @Test
    public void testTwoMassInserts() {
	final int COUNT = 100;
	for (int i = 0; i < COUNT; i++) {
	    client1.apply(new StringInsertOperation(0, "abc"));
	    client2.apply(new StringInsertOperation(0, "def"));
	}
	client1.waitForNotRunning();
	client2.waitForNotRunning();
	server.waitForNotRunning();
	assertEquals(server.getCurrentState().getState(), client1.getCurrentState().getState());
	assertEquals(server.getCurrentState().getState(), client2.getCurrentState().getState());
	assertEquals(6*COUNT, server.getCurrentState().getState().length());
    }

    /**
     * Randomizes the number of changes applied to each client per iteration,
     * the insert position and the string to be inserted.
     */
    @Test
    public void testRandomInserts() throws InterruptedException {
	final int COUNT = 100;
	final Random r = new Random();
	final AtomicInteger totalLength = new AtomicInteger(0);
	final Thread client1FillingThread = new Thread(()->addStringsToClient(COUNT, client1, totalLength, r));
	client1FillingThread.start();
	final Thread client2FillingThread = new Thread(()->addStringsToClient(COUNT, client2, totalLength, r));
	client2FillingThread.start();
	client1FillingThread.join();
	client2FillingThread.join();
	client1.waitForNotRunning();
	client2.waitForNotRunning();
	server.waitForNotRunning();
	assertEquals(server.getCurrentState().getState(), client1.getCurrentState().getState());
	assertEquals(server.getCurrentState().getState(), client2.getCurrentState().getState());
	assertEquals(totalLength.get(), server.getCurrentState().getState().length());
    }

    private void addStringsToClient(int count, Peer<StringInsertOperation, StringState> client, AtomicInteger totalLength, Random r) {
        for (int i = 0; i < count; i++) {
            for (int j = r.nextInt(10); j > 0; j--) {
                byte[] b = new byte[r.nextInt(10)];
                r.nextBytes(b);
                String s = Base64.encode(b);
                synchronized (client) {
                    client.apply(new StringInsertOperation(
                            r.nextInt(client.getCurrentState().getState().length()+1), s));
                }
                totalLength.addAndGet(s.length());
            }
        }
    }

    @Test
    public void testTwoMassInsertsWithServerReplica() {
	Peer<StringInsertOperation, StringState> server2 = new PeerImpl<StringInsertOperation, StringState>("Server2",
		new StringInsertTransformer(), server);
	final int COUNT = 100;
	for (int i = 0; i < COUNT; i++) {
	    client1.apply(new StringInsertOperation(0, "abc"));
	    client2.apply(new StringInsertOperation(0, "def"));
	}
	client1.waitForNotRunning();
	client2.waitForNotRunning();
	server.waitForNotRunning();
	server2.waitForNotRunning();
	assertEquals(server.getCurrentState().getState(), client1.getCurrentState().getState());
	assertEquals(server.getCurrentState().getState(), client2.getCurrentState().getState());
	assertEquals(server.getCurrentState().getState(), server2.getCurrentState().getState());
	assertEquals(6*COUNT, server2.getCurrentState().getState().length());
    }
}
