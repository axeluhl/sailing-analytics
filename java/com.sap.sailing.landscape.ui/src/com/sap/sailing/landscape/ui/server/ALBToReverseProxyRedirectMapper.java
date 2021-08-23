package com.sap.sailing.landscape.ui.server;

import java.util.Optional;

import com.sap.sailing.landscape.ui.shared.EventRedirectDTO;
import com.sap.sailing.landscape.ui.shared.EventSeriesRedirectDTO;
import com.sap.sailing.landscape.ui.shared.HomeRedirectDTO;
import com.sap.sailing.landscape.ui.shared.PlainRedirectDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.RedirectVisitor;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ReverseProxy;

/**
 * From the respective {@link RedirectDTO} produces a corresponding {@link ReverseProxy} redirect.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ALBToReverseProxyRedirectMapper<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, LogT extends Log>
implements RedirectVisitor {
    private final ReverseProxy<ShardingKey, MetricsT, ProcessT, LogT> reverseProxy;
    private final String hostname;
    private final ProcessT master;
    private final Optional<String> optionalKeyName;
    private final byte[] privateKeyDecryptionPassphrase;
    
    public ALBToReverseProxyRedirectMapper(ReverseProxy<ShardingKey, MetricsT, ProcessT, LogT> reverseProxy,
            String hostname, ProcessT master, Optional<String> optionalKeyName, byte[] privateKeyDecryptionPassphrase) {
        super();
        this.reverseProxy = reverseProxy;
        this.hostname = hostname;
        this.master = master;
        this.optionalKeyName = optionalKeyName;
        this.privateKeyDecryptionPassphrase = privateKeyDecryptionPassphrase;
    }

    @Override
    public void visit(PlainRedirectDTO plainRedirectDTO) throws Exception {
        reverseProxy.setPlainRedirect(hostname, master, optionalKeyName, privateKeyDecryptionPassphrase);
    }

    @Override
    public void visit(HomeRedirectDTO homeRedirectDTO) throws Exception {
        reverseProxy.setHomeRedirect(hostname, master, optionalKeyName, privateKeyDecryptionPassphrase);
    }

    @Override
    public void visit(EventRedirectDTO eventRedirectDTO) throws Exception {
        reverseProxy.setEventRedirect(hostname, master, eventRedirectDTO.getId(), optionalKeyName, privateKeyDecryptionPassphrase);
    }

    @Override
    public void visit(EventSeriesRedirectDTO eventSeriesRedirectDTO) throws Exception {
        reverseProxy.setEventSeriesRedirect(hostname, master, eventSeriesRedirectDTO.getId(), optionalKeyName, privateKeyDecryptionPassphrase);
    }

}
