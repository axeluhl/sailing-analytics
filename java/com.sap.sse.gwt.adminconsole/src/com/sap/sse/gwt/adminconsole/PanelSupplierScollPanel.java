package com.sap.sse.gwt.adminconsole;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelSupplierScollPanel extends ScrollPanel {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    private final AdminConsolePanelSupplier<? extends Widget> supplier;
    
    public PanelSupplierScollPanel(AdminConsolePanelSupplier<? extends Widget> supplier) {
        this.supplier = supplier;
    }
    
    public void activate() {
        logger.info("Activate");
        if (getWidget() == null) {
            logger.info("init widget from supplier");
            supplier.getAsync(new RunAsyncCallback() {
                
                @Override
                public void onSuccess() {
                    logger.info("Successfully loaded async.");
                    Widget widget = supplier.get();
                    widget.setTitle(supplier.getTitle());
                    widget.setSize("100%", "100%");
                    setWidget(widget);
                }
                
                @Override
                public void onFailure(Throwable reason) {
                    logger.log(Level.SEVERE, "Error while init widget asynchronous.", reason);
                }
            });
        }
    }
}
