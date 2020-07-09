package com.sap.sse.landscape.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.NamedImpl;

/**
 * Region ID and key name together form a unique key for AWS key pairs.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SSHKeyPair extends NamedImpl implements Named {
    private static final long serialVersionUID = 2877813132246472243L;
    private final String regionId;
    private final String creatorName;
    private final TimePoint creationTime;
    private final byte[] publicKey;
    private final byte[] privateKey;

    public SSHKeyPair(String regionId, String creatorName, TimePoint creationTime, String keyName, byte[] publicKey, byte[] privateKey) {
        super(keyName);
        this.regionId = regionId;
        this.creatorName = creatorName;
        this.creationTime = creationTime;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    
    public KeyPair getKeyPair(JSch jsch) throws JSchException {
        return KeyPair.load(jsch, getPrivateKey(), getPublicKey());
    }
    
    public String getRegionId() {
        return regionId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public TimePoint getCreationTime() {
        return creationTime;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }
}
