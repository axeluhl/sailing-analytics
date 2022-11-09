package com.sap.sse.landscape.ssh;

import java.io.ByteArrayOutputStream;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Region ID and key name together form a unique key for AWS key pairs. The private key is always stored in an encrypted form,
 * and it is up to the client to manage the passphrase that was used initially when symmetrically encrypting it so that it
 * can later be used to obtain a decrypted key pair that is good for initiating an SSH session, for example.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SSHKeyPair extends NamedImpl implements Named, WithQualifiedObjectIdentifier {
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
     * Returns a {@link KeyPair} with decrypted private key that can be used to initiate an SSH session, e.g., using
     * {@link Host#createSshChannel(String, java.util.Optional, byte[])}.
     * 
     * @param passphrase
     *            used to {@link KeyPair#decrypt(byte[]) decrypt} the encrypted private key so that the resulting key
     *            pair can be used to initiate sessions. Has to equal the passphrase used when encrypting the private key,
     *            e.g., when calling {@link #SSHKeyPair(String, String, TimePoint, String, byte[], byte[], byte[])}.
     */
    public KeyPair getKeyPair(JSch jsch, byte[] passphrase) throws JSchException {
        final KeyPair result = KeyPair.load(jsch, getEncryptedPrivateKey(), getPublicKey());
        if (!result.decrypt(passphrase)) {
            throw new IllegalStateException("Could not decrypt private key of "+this+"; probably incorrect passphrase?");
        }
        return result;
    }
    
    /**
     * Returns a{@link boolean} whether the passphrase was valid and correct.
     * 
     * @param passphrase
     *            entered by the user in the textfield and should be correct for decrypting the selected ssh-private
     *            key, returns false if this passphrase could not be used for decrypting the key or if the format is not
     *            valid/supported
     */
    public boolean checkPassphrase(JSch jsch, byte[] passphrase) {
        boolean res;
        try {
            // return false if key is not loaded properly or has the wrong format
            final KeyPair result = KeyPair.load(jsch, getEncryptedPrivateKey(), getPublicKey());
            // return false if result is not decrypted
            res = result.decrypt(passphrase);
        } catch (JSchException e) {
            res = false;
        }
        return res;
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

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(new TypeRelativeObjectIdentifier(getRegionId(), getName()));
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredLandscapeTypes.SSH_KEY;
    }
}
