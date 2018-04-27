package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * Helper class to obtain the last and the first REQUIRED_SIZE bytes of a resource defined by a given url. 
 */
public class JSDownloadUtils {
    private static final Double REQUIRED_SIZE = 10000000.0;
    
    public interface JSDownloadCallback {
        void progress(Double current, Double total);

        void error(Object msg);

        void complete(Int8Array start, Int8Array end, Double skipped);
    }
    
    public interface JSHrefCallback {
        void newHref(String foundLink);
        void noResult();
        void complete();
    }
    
    interface JSSizeCallback {
        void size(Double total);
    }
    
    /**
     * Downloads the required parts of the file, or if necessary the full file and slices the required parts from it. 
     */
    public static void getData(String url, JSDownloadCallback callback){
        getFileSizeIfFastPath(url, new JSSizeCallback() {
            
            @Override
            public void size(Double total) {
                if (total != 0 && total > 2 * REQUIRED_SIZE) {
                    //used if range and accept headers are set correctly
                    getDataFast(url, callback, total, REQUIRED_SIZE);
                } else {
                    //fallback will always work, if cors is supported, does not require any other server configuration
                    getDataSlow(url, callback, REQUIRED_SIZE);
                }
            }
        });
    }
    
    /**
     * Fast file download method based on range requests, only the first and the last REQUIRED_SIZE is loaded.
     */
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
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
                    }
                }
            }
        };
        xhr.send();
    } catch (error) {
        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
    }
}-*/;
    
    /**
     * Determines if a server can deliver a file with range requests, and if so returns the size of the file
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
     * If range requests are not possible, the whole file is downloaded and then slices to the required parts 
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

    public static void getFileList(String url, JSHrefCallback callback) {
        getFileListNative(url, callback);
    }
    
    private native static void getFileListNative(String url, JSHrefCallback callback)/*-{
        try{
            var xmlHttp = null;
            var allLinks = []; //set of all internal and external links
            xmlHttp = new XMLHttpRequest();
            xmlHttp.open( "GET", url, true );
            xmlHttp.send( null );
            xmlHttp.onreadystatechange = function () {
                if ( xmlHttp.readyState == 4) {
                     if (xmlHttp.status == 200){
                        var container = document.createElement("p");
                        container.innerHTML = xmlHttp.responseText;
                        var anchors = container.getElementsByTagName("a");
                        for (var i = 0; i < anchors.length; i++) {
                            try {
                                var href = anchors[i].getAttribute("href");
                                callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSHrefCallback::newHref(Ljava/lang/String;)(href);
                            } catch (error) {
                                console.log(error);
                            }
                        }
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSHrefCallback::complete()();
                    } else {
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSHrefCallback::noResult()();
                    }
                } 
            };
        } catch (error) {
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSHrefCallback::noResult()();
        }

    }-*/;
}
