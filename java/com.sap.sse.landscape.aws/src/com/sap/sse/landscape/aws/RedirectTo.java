package com.sap.sse.landscape.aws;

public interface RedirectTo extends LoadBalancerRuleAction {
    enum RedirectResponse {
        PERMANENTLY_MOVED(301), FOUND(302);
        
        private int responseCode;

        RedirectResponse(int responseCode) {
            this.responseCode = responseCode;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }
    }
    
    String getProtocol();
    Integer getPort();
    String getHost();
    String getPath();
    String getQuery();
    RedirectResponse getResponse();
}
