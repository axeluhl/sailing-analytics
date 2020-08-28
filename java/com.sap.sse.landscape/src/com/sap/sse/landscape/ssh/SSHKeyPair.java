package com.sap.sse.landscape.ssh;

import java.io.ByteArrayOutputStream;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.NamedImpl;

/**
 * Region ID and key name together form a unique key for AWS key pairs. The private key is always stored in an encrypted form,
 * and it is up to the client to manage the passphrase that was used initially when symmetrically encrypting it so that it
 * can later be used to obtain a decrypted key pair that is good for initiating an SSH session, for example.
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
    private final byte[] encryptedPrivateKey;

    /**
     * This constructor variant accepts an unencrypted private key but in turn insists on a valid, non-{@code null}
     * encryption passphrase which will be used to encrypt the private key before storing it in the new object's
     * {@link #encryptedPrivateKey} field.
     */
    public SSHKeyPair(String regionId, String creatorName, TimePoint creationTime, String keyName, byte[] publicKey, byte[] unencryptedPrivateKey,
            byte[] privateKeyEncryptionPassphrase) throws JSchException {
        this(regionId, creatorName, creationTime, keyName, publicKey, encryptPrivateKey(unencryptedPrivateKey, privateKeyEncryptionPassphrase));
    }
    
    private static byte[] encryptPrivateKey(byte[] unencryptedPrivateKey, byte[] privateKeyEncryptionPassphrase) throws JSchException {
        if (privateKeyEncryptionPassphrase.length == 0) {
            throw new IllegalArgumentException("Non-empty passphrase required to protect private key");
        }
        final KeyPair keyPair = KeyPair.load(new JSch(), unencryptedPrivateKey, /* pubkey */ null);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.writePrivateKey(bos, privateKeyEncryptionPassphrase);
        return bos.toByteArray();
    }

    public SSHKeyPair(String regionId, String creatorName, TimePoint creationTime, String keyName, byte[] publicKey, byte[] encryptedPrivateKey) {
        super(keyName);
        this.regionId = regionId;
        this.creatorName = creatorName;
        this.creationTime = creationTime;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
    
    /**
     * @param passphrase
     *            used to {@link KeyPair#decrypt(byte[]) decrypt} the encrypted private key so that the resulting key
     *            pair can be used to initiate sessions.
     */
    public KeyPair getKeyPair(JSch jsch, byte[] passphrase) throws JSchException {
        final KeyPair result = KeyPair.load(jsch, getEncryptedPrivateKey(), getPublicKey());
        result.decrypt(passphrase);
        return result;
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

    public byte[] getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
}
