package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * Helper class to obtain the last and the first REQUIRED_SIZE of a ressource given a url 
 */
public class JSDownloadUtils {
    private static final Double REQUIRED_SIZE = 1000000.0;
    
    
    interface JSDownloadCallback {
        void progress(Double current, Double total);

        void error(Object msg);

        void complete(Int8Array start, Int8Array end, Double skipped);
    }
    
    interface JSSizeCallback {
        void size(Double total);
    }
    
    
    public static void getData(String url, JSDownloadCallback callback){
        getFileSizeIfFastPath(url, new JSSizeCallback() {
            
            @Override
            public void size(Double total) {
                if(total != 0 && total > 0){
                    //used if range and accept headers are set correctly
                    getDataFast(url, callback, total, REQUIRED_SIZE);
                }else{
                    //fallback will always work, if cors is supported, does not require any other server configuration
                    getDataSlow(url, callback, REQUIRED_SIZE);
                }
            }
        });
    }
    
    private native static void getDataFast(String url, JSDownloadCallback callback, Double length, Double REQUIRED_SIZE) /*-{
    try {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);
        xhr.setRequestHeader("Range", "bytes=0-" + REQUIRED_SIZE);
        xhr.responseType = "arraybuffer";
        xhr.onprogress = function(evt) {
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::progress(Ljava/lang/Double;Ljava/lang/Double;)(evt.loaded, evt.total);
        }
        xhr.error = function(error) {
            alert(error)
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
        }
        
        xhr.onreadystatechange = function() {
        var state = xhr.readyState;
            if (state == 4) {
                if (xhr.response) {
                    var startLength = xhr.response.byteLength;
                    var start = new Int8Array(xhr.response);

                    try {
                        var xhr2 = new XMLHttpRequest();
                        xhr2.open("GET", url, true);
                        xhr2.setRequestHeader("Range", "bytes=" + (length - REQUIRED_SIZE));
                        xhr2.responseType = "arraybuffer";
                        xhr2.onprogress = function(evt) {
                            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::progress(Ljava/lang/Double;Ljava/lang/Double;)(evt.loaded, evt.total);
                        }
                        xhr2.error = function(error) {
                            alert(error)
                            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
                        }
                        
                        xhr2.onreadystatechange = function() {
                            var state = xhr2.readyState;
                            if (state == 4) {
                                if (xhr2.response) {
                                    var endLength = xhr2.response.byteLength;
                                    var end = new Int8Array(xhr2.response);
                                    var sparse = length - startLength - endLength;
                                    callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::complete(Lcom/google/gwt/typedarrays/shared/Int8Array;Lcom/google/gwt/typedarrays/shared/Int8Array;Ljava/lang/Double;)(start, end, sparse);
                                }
                            }
                        };
                        xhr2.send();
                    } catch (error) {
                        alert(error);
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
                    }
                }
            }
        };
        xhr.send();
    } catch (error) {
        alert(error);
        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
    }
}-*/;
    
    /**
     * If content lenght works, it is most likely that range will also work 
     */
    private native static void getFileSizeIfFastPath(String url, JSSizeCallback callback) /*-{
                try {
            var xhr = new XMLHttpRequest();
            xhr.open("HEAD", url, true);
            xhr.responseType = "arraybuffer";
            xhr.error = function(error) {
                callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSSizeCallback::size(Ljava/lang/Double;)(-1.0);
            }
            
            xhr.onreadystatechange = function() {
            var state = xhr.readyState;
                if (state == 4) {
                    if (xhr.response) {
                        var length = xhr.getResponseHeader("Content-Length");
                        var range = xhr.getResponseHeader("Content-Range");
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSSizeCallback::size(Ljava/lang/Double;)(length);                        
                    } else {
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSSizeCallback::size(Ljava/lang/Double;)(-1.0);
                    }
                }
            };
            xhr.send();
        } catch (error) {
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSSizeCallback::size(Ljava/lang/Double;)(-1.0);
        }
    }-*/;

    /**
     *  Starts a download to get the native progess response from the browser (due to cors, it is problematic to use the Content-Length header)
     *  Aborts download as soon as possible. Save for filesize until 9007199254740991 bytes, see MAX_SAFE_INTEGER for reasons
     */
    private native static void getDataSlow(String url, JSDownloadCallback callback, Double REQUIRED_SIZE) /*-{
        try {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", url, true);
            xhr.responseType = "arraybuffer";
            xhr.onprogress = function(evt) {
                callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::progress(Ljava/lang/Double;Ljava/lang/Double;)(evt.loaded, evt.total);
            }
            xhr.error = function(error) {
                callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
            }
            
            xhr.onreadystatechange = function() {
            var state = xhr.readyState;
                if (state == 4) {
                    if (xhr.response) {
                        var length = xhr.response.byteLength;
                        var start = new Int8Array(xhr.response.slice(0, REQUIRED_SIZE));
                        var end = new Int8Array(xhr.response.slice(length - REQUIRED_SIZE));
                        var sparse = length - 2 * REQUIRED_SIZE;
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::complete(Lcom/google/gwt/typedarrays/shared/Int8Array;Lcom/google/gwt/typedarrays/shared/Int8Array;Ljava/lang/Double;)(start, end, sparse);
                    } else {
                         callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)("No result");
                    }
                }
            };
            xhr.send();
        } catch (error) {
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
        }
    }-*/;

}
