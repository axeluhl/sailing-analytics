package com.sap.sse.landscape.aws;

public interface RedirectVisitor {
    void visit(PlainRedirectDTO plainRedirectDTO) throws Exception;
    void visit(HomeRedirectDTO homeRedirectDTO) throws Exception;
    void visit(EventRedirectDTO eventRedirectDTO) throws Exception;
    void visit(EventSeriesRedirectDTO eventSeriesRedirectDTO) throws Exception;
}
