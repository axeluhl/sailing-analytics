package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.typedarrays.shared.Int8Array;

public class JSDownloadUtils {

    interface JSDownloadCallback{
        void progress(Double current,Double total);
        void error(Object msg);
        void complete(Int8Array start, Int8Array end, Double skipped);
    }
    
    
    /**
     *  Starts a download to get the native progess response from the browser (due to cors, it is problematic to use the Content-Length header)
     *  Aborts download as soon as possible. Save for filesize until 9007199254740991 bytes, see MAX_SAFE_INTEGER for reasons
     */
    public native static void getData(String url, JSDownloadCallback callback) /*-{
        try {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", url, true);
            xhr.responseType = "arraybuffer";
            xhr.onprogress = function(evt) {
               callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::progress(Ljava/lang/Double;Ljava/lang/Double;)(evt.loaded,evt.total);
            }
            xhr.error = function(error) {
                callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
            }
            
            xhr.onreadystatechange = function() {
            var state = xhr.readyState;
                if (state == 4){
                    if(xhr.response){
                        var length = xhr.response.byteLength;
                        alert(length);
                        var start = new Int8Array(xhr.response.slice(0,1000000));
                        alert(start)
                        var end = new Int8Array(xhr.response.slice(length-1000000));
                        alert(end)
                        var sparse = length-2*1000000;
                        alert("sparse " + sparse)
                        callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::complete(Lcom/google/gwt/typedarrays/shared/Int8Array;Lcom/google/gwt/typedarrays/shared/Int8Array;Ljava/lang/Double;)(start,end,sparse);
                    }
                }
            };
            xhr.send();
        }catch (error){
            callback.@com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback::error(Ljava/lang/Object;)(error);
        }
    }-*/;

}
