package com.sap.sse.landscape.aws;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;
import com.sap.sse.landscape.mongodb.impl.MongoProcessInReplicaSetImpl;
import com.sap.sse.landscape.mongodb.impl.MongoReplicaSetImpl;

/**
 * Parses a MongoDB URI in the format <tt>"mongodb://{host[:port]}[,{host[:port]},...]/{databaseName}[?{key}={value}[,{key}={value},...]]"</tt>. The
 * database name is required. If one of the <tt>{key}={value}</tt> pairs uses the key {@code "replicaSet"} then the resulting {@link Database}'s
 * {@link Database#getEndpoint() endpoint} will be a {@link MongoReplicaSet} whose {@link MongoReplicaSet#getName() name} is determined
 * by the <tt>{value}</tt> of the {@code "replicaSet"} parameter; otherwise the result is a {@link MongoProcess}.
 */
public class MongoUriParser<ShardingKey> {
    private final String SCHEME = "mongodb";
    private final AwsLandscape<ShardingKey> landscape;
    private final Region region;
    
    public MongoUriParser(AwsLandscape<ShardingKey> landscape, Region region) {
        super();
        this.landscape = landscape;
        this.region = region;
    }

    public Database parseMongoUri(String mongoUri) throws URISyntaxException, UnknownHostException {
        final MongoEndpoint endpoint;
        final URI mongoUriAsUri = new URI(mongoUri);
        if (!Util.equalsWithNull(SCHEME, mongoUriAsUri.getScheme())) {
            throw new IllegalArgumentException("Expected scheme "+SCHEME);
        }
        final String[] hostnamesAndOptionalPorts = mongoUriAsUri.getAuthority().split(",");
        final String dbName = mongoUriAsUri.getPath().replaceAll("^/", "");
        if (!Util.hasLength(dbName)) {
            throw new IllegalArgumentException("Expected non-empty database name");
        }
        final String query = mongoUriAsUri.getQuery();
        String replicaSetName = null;
        if (query != null) {
            for (final String keyValuePair : query.split("&")) {
                final String[] keyAndValue = keyValuePair.split("=", 2);
                if (Util.equalsWithNull(keyAndValue[0], "replicaSet")) {
                    replicaSetName = keyAndValue[1];
                }
            }
        }
        if (replicaSetName != null) {
            MongoReplicaSet replicaSet = new MongoReplicaSetImpl(replicaSetName);
            for (final String hostnameAndOptionalPort : hostnamesAndOptionalPorts) {
                replicaSet.addReplica(getMongoProcessInReplicaSet(replicaSet, hostnameAndOptionalPort));
            }
            endpoint = replicaSet;
        } else {
            endpoint = getMongoProcess(hostnamesAndOptionalPorts[0]);
        }
        return endpoint.getDatabase(dbName);
    }

    private MongoEndpoint getMongoProcess(final String hostnameAndOptionalPort) throws UnknownHostException {
        final MongoEndpoint endpoint;
        Pair<AwsInstance<ShardingKey>, Integer> hostAndOptionalPort = getHostAndPort(hostnameAndOptionalPort);
        if (hostAndOptionalPort.getB() != null) {
            endpoint = new MongoProcessImpl(hostAndOptionalPort.getA(), hostAndOptionalPort.getB());
        } else {
            endpoint = new MongoProcessImpl(hostAndOptionalPort.getA());
        }
        return endpoint;
    }
    
    private MongoProcessInReplicaSet getMongoProcessInReplicaSet(final MongoReplicaSet replicaSet, final String hostnameAndOptionalPort) throws UnknownHostException {
        final MongoProcessInReplicaSet endpoint;
        Pair<AwsInstance<ShardingKey>, Integer> hostAndOptionalPort = getHostAndPort(hostnameAndOptionalPort);
        if (hostAndOptionalPort.getB() != null) {
            endpoint = new MongoProcessInReplicaSetImpl(replicaSet, hostAndOptionalPort.getB(), hostAndOptionalPort.getA());
        } else {
            endpoint = new MongoProcessInReplicaSetImpl(replicaSet, hostAndOptionalPort.getA());
        }
        return endpoint;
    }
    
    private Pair<AwsInstance<ShardingKey>, Integer> getHostAndPort(String hostnameAndOptionalPort) throws UnknownHostException {
        final String[] hostnameAndOptionalPortSplit = hostnameAndOptionalPort.split(":");
        final InetAddress address = InetAddress.getByName(hostnameAndOptionalPortSplit[0]);
        final AwsInstance<ShardingKey> hostByPublicIp = landscape.getHostByPrivateIpAddress(region, address.getHostAddress(), AwsInstanceImpl::new);
        final AwsInstance<ShardingKey> host;
        if (hostByPublicIp != null) {
            host = hostByPublicIp;
        } else {
            host = landscape.getHostByPrivateIpAddress(region, address.getHostAddress(), AwsInstanceImpl::new);
        }
        return new Pair<>(host, hostnameAndOptionalPortSplit.length<2?null:Integer.valueOf(hostnameAndOptionalPortSplit[1]));
    }
}
