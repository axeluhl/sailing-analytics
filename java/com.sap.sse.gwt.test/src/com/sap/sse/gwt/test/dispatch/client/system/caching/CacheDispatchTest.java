package com.sap.sse.gwt.test.dispatch.client.system.caching;

import java.net.URL;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.client.system.caching.CachingDispatch;
import com.sap.sse.gwt.dispatch.shared.caching.HasClientCacheTotalTimeToLive;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

public class CacheDispatchTest {

    private CachingDispatch<DispatchContextMock> cd = new CachingDispatch<DispatchContextMock>(new DispatchMock(),
            false, 200);

    @Test
    public void testCacheCleanup() {
        final ResultHolder holder = new ResultHolder();
        checkIfCached(holder, new SomeDefaultCacheAction(), 20);
        checkIfCached(holder, new SomeDefaultCacheAction(), 20);
        checkIfCached(holder, new SomeDefaultCacheAction(), 20);
        Assert.assertEquals("One element in cache", 3, cd.ttlItemsInCache());
        wait(210);
        cd.cleanupExpiredItems();
        Assert.assertEquals("No element in cache", 0, cd.ttlItemsInCache());
    }

    @Test
    public void testNoCache() {
        final ResultHolder holder = new ResultHolder();
        SomeNoCacheAction action = new SomeNoCacheAction();
        cd.execute(action, new SimpleCallback<SomeResult>() {
            @Override
            public void onSuccess(SomeResult result) {
                holder.value = result;
            }
        });
        wait(100);
        cd.execute(action, new SimpleCallback<SomeResult>() {
            @Override
            public void onSuccess(SomeResult result) {
                Assert.assertNotSame("Not caching, should not be same", holder.value, result);
            }
        });
    }



    @Test
    public void testDefaultCache() {
        ResultHolder holder = new ResultHolder();
        SomeDefaultCacheAction action = new SomeDefaultCacheAction();
        checkIfCached(holder, action, 100);
        checkIfExpired(holder, action, 110);
    }

    @Test
    public void testActionWithTTLLongerThanDefaultCache() {
        ResultHolder holder = new ResultHolder();
        SomeDefaultProvidingTTLCacheAction action = new SomeDefaultProvidingTTLCacheAction(300);
        checkIfCached(holder, action, 210);
        checkIfExpired(holder, action, 100);
    }

    @Test
    public void testActionWithTTLShorterThanDefaultCache() {
        ResultHolder holder = new ResultHolder();
        SomeDefaultProvidingTTLCacheAction action = new SomeDefaultProvidingTTLCacheAction(100);
        checkIfCached(holder, action, 50);
        checkIfExpired(holder, action, 60);
    }

    @Test
    public void testResultWithTTLShorterThanDefaultCache() {
        ResultHolder holder = new ResultHolder();
        SomeResultWithTTLCacheAction action = new SomeResultWithTTLCacheAction(100);
        checkIfCached(holder, action, 50);
        checkIfExpired(holder, action, 60);
    }

    @Test
    public void testResultWithTTLLongerThanDefaultCache() {
        ResultHolder holder = new ResultHolder();
        SomeResultWithTTLCacheAction action = new SomeResultWithTTLCacheAction(300);
        checkIfCached(holder, action, 250);
        checkIfExpired(holder, action, 60);
    }

    @Test
    public void testActionResultWithTTL() {
        ResultHolder holder = new ResultHolder();
        SomeActionAndResultWithTTLCacheAction action = new SomeActionAndResultWithTTLCacheAction(50, 300);
        checkIfCached(holder, action, 200);
        checkIfExpired(holder, action, 110);
    }

    private <R extends Result, A extends Action<R, DispatchContextMock>> void checkIfCached(final ResultHolder holder,
            A action, int waitTime) {
        cd.execute(action, new SimpleCallback<R>() {
            @Override
            public void onSuccess(Result result) {
                holder.value = result;
            }
        });
        wait(waitTime);
        cd.execute(action, new SimpleCallback<R>() {
            @Override
            public void onSuccess(Result result) {
                Assert.assertSame("Should be the same", holder.value, result);
            }
        });
    }

    private <R extends Result, A extends Action<R, DispatchContextMock>> void checkIfExpired(final ResultHolder holder,
            A action, int waitTime) {
        wait(waitTime);
        cd.execute(action, new SimpleCallback<R>() {
            @Override
            public void onSuccess(Result result) {
                Assert.assertNotSame("Should have expired, should not be same", holder.value, result);
            }
        });
    }

    private class SomeNoCacheAction implements Action<SomeResult, DispatchContextMock> {
        @Override
        public SomeResult execute(DispatchContextMock ctx) throws DispatchException {
            return new SomeResult();
        }
    }

    private class SomeDefaultCacheAction implements Action<SomeResult, DispatchContextMock>, IsClientCacheable {
        private String instanceIdentifier = UUID.randomUUID().toString();

        @Override
        public SomeResult execute(DispatchContextMock ctx) throws DispatchException {
            return new SomeResult();
        }

        @Override
        public void cacheInstanceKey(StringBuilder key) {
            key.append(instanceIdentifier);
        }
    }

    private class SomeDefaultProvidingTTLCacheAction extends SomeDefaultCacheAction implements IsClientCacheable,
            HasClientCacheTotalTimeToLive {
        private int ttl;

        public SomeDefaultProvidingTTLCacheAction(int ttl) {
            this.ttl = ttl;
        }

        @Override
        public int cacheTotalTimeToLiveMillis() {
            return ttl;
        }
    }

    private class SomeResultWithTTLCacheAction implements Action<SomeResultWithTTL, DispatchContextMock>,
            IsClientCacheable {
        private String instanceIdentifier = UUID.randomUUID().toString();
        private int ttl;

        public SomeResultWithTTLCacheAction(int ttl) {
            this.ttl = ttl;
        }

        @Override
        public SomeResultWithTTL execute(DispatchContextMock ctx) throws DispatchException {
            return new SomeResultWithTTL(ttl);
        }

        @Override
        public void cacheInstanceKey(StringBuilder key) {
            key.append(instanceIdentifier);
        }
    }

    private class SomeActionAndResultWithTTLCacheAction implements Action<SomeResultWithTTL, DispatchContextMock>,
            IsClientCacheable,
            HasClientCacheTotalTimeToLive {
        private String instanceIdentifier = UUID.randomUUID().toString();
        private int actionTTL;
        private int resultTTL;

        public SomeActionAndResultWithTTLCacheAction(int actionTTL, int resultTTL) {
            this.actionTTL = actionTTL;
            this.resultTTL = resultTTL;
        }

        @Override
        public SomeResultWithTTL execute(DispatchContextMock ctx) throws DispatchException {
            return new SomeResultWithTTL(resultTTL);
        }

        @Override
        public void cacheInstanceKey(StringBuilder key) {
            key.append(instanceIdentifier);
        }

        @Override
        public int cacheTotalTimeToLiveMillis() {
            return actionTTL;
        }
    }

    private class SomeResult implements Result {
    }

    private class SomeResultWithTTL implements Result, HasClientCacheTotalTimeToLive {
        private int ttl;

        public SomeResultWithTTL(int ttl) {
            super();
            this.ttl = ttl;
        }

        @Override
        public int cacheTotalTimeToLiveMillis() {
            return ttl;
        }
    }

    private class ResultHolder {
        Result value;
    }

    private class DispatchContextMock implements DispatchContext {

        @Override
        public HttpServletRequest getRequest() {
            return null;
        }

        @Override
        public URL getRequestBaseURL() throws DispatchException {
            return null;
        }
    }

    private abstract class SimpleCallback<T> implements AsyncCallback<T> {
        @Override
        public void onFailure(Throwable caught) {
        }
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }
    }

    private class DispatchMock implements DispatchSystemAsync<DispatchContextMock> {
        @Override
        public <R extends Result, A extends Action<R, DispatchContextMock>> void execute(A action,
                AsyncCallback<R> callback) throws DispatchException {
            callback.onSuccess(action.execute(new DispatchContextMock()));
        }
    }
}
