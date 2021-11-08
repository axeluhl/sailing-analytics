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
 * From the respective {@link RedirectDTO} produces a corresponding {@link ReverseProxy} redirect that maps to the ARCHIVE server.
 * For the "Plain" / index.html routing we don't currently have a corresponding ARCHIVE rewrite macro, hence a plain
 * routing is mapped to a Home.html rewrite for the ARCHIVE.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ALBToReverseProxyArchiveRedirectMapper<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, LogT extends Log>
extends AbstractALBToReverseProxyRedirectMapper<ShardingKey, MetricsT, ProcessT, LogT>
implements RedirectVisitor {
    
    public ALBToReverseProxyArchiveRedirectMapper(ReverseProxy<ShardingKey, MetricsT, ProcessT, LogT> reverseProxy,
            String hostname, Optional<String> optionalKeyName, byte[] privateKeyDecryptionPassphrase) {
        super(reverseProxy, hostname, optionalKeyName, privateKeyDecryptionPassphrase);
    }

    @Override
    public void visit(PlainRedirectDTO plainRedirectDTO) throws Exception {
        getReverseProxy().setHomeArchiveRedirect(getHostname(), getOptionalKeyName(), getPrivateKeyDecryptionPassphrase());
    }

    @Override
    public void visit(HomeRedirectDTO homeRedirectDTO) throws Exception {
        getReverseProxy().setHomeArchiveRedirect(getHostname(), getOptionalKeyName(), getPrivateKeyDecryptionPassphrase());
    }

    @Override
    public void visit(EventRedirectDTO eventRedirectDTO) throws Exception {
        getReverseProxy().setEventArchiveRedirect(getHostname(), eventRedirectDTO.getId(), getOptionalKeyName(), getPrivateKeyDecryptionPassphrase());
    }

    @Override
    public void visit(EventSeriesRedirectDTO eventSeriesRedirectDTO) throws Exception {
        getReverseProxy().setEventSeriesArchiveRedirect(getHostname(), eventSeriesRedirectDTO.getId(), getOptionalKeyName(), getPrivateKeyDecryptionPassphrase());
    }
}
