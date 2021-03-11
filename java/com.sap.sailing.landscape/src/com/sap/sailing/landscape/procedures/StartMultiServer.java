package com.sap.sailing.landscape.procedures;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.orchestration.StartEmptyServer;
import com.sap.sse.landscape.ssh.SshCommandChannel;
import com.sap.sse.util.Wait;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Starts an empty multi-server. The image will cause a {@code /home/sailing/servers/server} directory to exist, but
 * after successfully launching, that directory will be removed. A {@link DeployProcessOnMultiServer} procedure needs to
 * be run with the {@link #getHost()} of this procedure telling the host on which to deploy the process.<p>
 * 
 * The implementation specializes the {@link StartEmptyServer} procedure in {@link Builder#setNoShutdown(boolean) no-shutdown} mode.
 * After running that part, the {@code httpd} service is launched.<p>
 * 
 * You want to at least specify an {@link Builder#setInstanceName(String) instance name} and {@link Builder#setInstanceType(InstanceType)}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <SailingAnalyticsHost<ShardingKey>>
 */
public class StartMultiServer<ShardingKey>
extends StartEmptyServer<StartMultiServer<ShardingKey>, ShardingKey, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
implements StartFromSailingAnalyticsImage {
    private static final Logger logger = Logger.getLogger(StartMultiServer.class.getName());
    private Optional<Duration> optionalTimeout;
    
    /**
     * Under all circumstances, this builder will return {@code true} for {@link #isNoShutdown()}, making sure
     * that after the upgrade progress the server does not try to re-boot. Defaults:<ul>
     * <li>The instance name defaults to "Multi-Server"</li>
     * <li>The instance type defaults to {@link InstanceType#C5_D_4_XLARGE}</li>
     * <li>The {@link #setImageType(String) image type} defaults to {@link StartFromSailingAnalyticsImage#IMAGE_TYPE_TAG_VALUE_SAILING}
     * ({@code "sailing-analytics-server"}).
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartEmptyServer.Builder<BuilderT, StartMultiServer<ShardingKey>, ShardingKey, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartEmptyServer.BuilderImpl<BuilderT, StartMultiServer<ShardingKey>,
    ShardingKey, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
    implements Builder<BuilderT, ShardingKey> {
        @Override
        public StartMultiServer<ShardingKey> build() {
            return new StartMultiServer<>(this);
        }

        @Override
        protected boolean isNoShutdown() {
            return true;
        }

        @Override
        protected String getImageType() {
            return super.getImageType() == null ? IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }
        
        @Override
        protected String getInstanceName() {
            final String result;
            if (isInstanceNameSet()) {
                result = super.getInstanceName();
            } else {
                result = "Multi-Server";
            }
            return result;
        }
        
        @Override
        protected HostSupplier<ShardingKey, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> getHostSupplier() {
            final HostSupplier<ShardingKey, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> result;
            if (super.getHostSupplier() == null) {
                result = (instanceId, az, privateIpAddress, launchTimePoint, landscape)->
                    new SailingAnalyticsHostImpl<>(instanceId, az, privateIpAddress, launchTimePoint, landscape, (host, port, serverDirectory, telnetPort, serverName, additionalProperties)->{
                        try {
                            return new SailingAnalyticsProcessImpl<ShardingKey>(port, host, serverDirectory, telnetPort, serverName,
                                    ((Number) additionalProperties.get(SailingProcessConfigurationVariables.EXPEDITION_PORT.name())).intValue());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            } else {
                result = super.getHostSupplier();
            }
            return result;
        }
        
        @Override
        protected InstanceType getInstanceType() {
            final InstanceType result;
            if (super.getInstanceType() == null) {
                result = InstanceType.C5_D_4_XLARGE;
            } else {
                result = super.getInstanceType();
            }
            return result;
        }
        
        /**
         * Make visible in package
         */
        @Override
        protected Optional<Duration> getOptionalTimeout() {
            return super.getOptionalTimeout();
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> Builder<BuilderT, ShardingKey> builder() {
        return new BuilderImpl<>();
    }

    protected StartMultiServer(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
        this.optionalTimeout = builder.getOptionalTimeout();
    }
    
    @Override
    public void run() throws Exception {
        super.run(); // this will trigger the "sailing" init.d script running in the background, triggering the image upgrade, then the httpd stop and clean-up
        final String instanceId = getHost().getInstanceId();
        getHost().getReverseProxy().createInternalStatusRedirect(optionalTimeout, Optional.of(getKeyName()), getPrivateKeyEncryptionPassphrase());
        logger.info("Waiting for image upgrade process to finish on "+getHost());
        final Callable<Boolean> imageUpgradeFinishedDetector = ()->{
            boolean fileFound = false;
            final ChannelSftp sftpChannel = getHost().createRootSftpChannel(optionalTimeout, Optional.of(getKeyName()), getPrivateKeyEncryptionPassphrase());
            sftpChannel.connect();
            try {
                final SftpATTRS stat = sftpChannel.stat("/tmp/image-upgrade-finished");
                if (stat.isReg()) { // regular file; see also /configuration/imageupgrade.sh which writes this file in case of no-shutdown
                    fileFound = true;
                }
            } catch (SftpException e) {
                fileFound = false;
            } finally {
                sftpChannel.getSession().disconnect();
            }
            return fileFound;
        };
        final boolean imageUpgradeFinished = Wait.wait(
                imageUpgradeFinishedDetector, optionalTimeout, /* sleepBetweenAttempts */ Duration.ONE_SECOND.times(5),
                Level.INFO, "Waiting for image upgrade process to finish on "+getHost());
        logger.info("Image upgrade process on "+getHost()+" did "+(imageUpgradeFinished?"":"not ")+"finish.");
        final SshCommandChannel sshCommandChannel = getHost().createRootSshChannel(optionalTimeout, Optional.of(getKeyName()), getPrivateKeyEncryptionPassphrase());
        logger.info("stdout for removing "+ApplicationProcessHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance "+instanceId+": "+
                sshCommandChannel.runCommandAndReturnStdoutAndLogStderr("rm -rf "+ApplicationProcessHost.DEFAULT_SERVER_PATH+"; service httpd start",
                        "stderr for removing "+ApplicationProcessHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance \""+instanceId+"\": ", Level.INFO));
        logger.info("exit status for removing "+ApplicationProcessHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance \""+instanceId+"\": "+sshCommandChannel.getExitStatus());
    }
}
