package com.sap.sse.landscape.aws.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.aws.AwsLandscape;

import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

/**
 * When during landscape discovery several similar DNS requests are required, e.g., mapping a hosted zone
 * name to a hosted zone ID, or listing the resource record sets within a hosted zone, this cache can be used
 * to avoid repetitive requests. This cache has no invalidation logic. Toss it after your series of requests
 * is finished.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class DNSCache {
    private final Route53AsyncClient route53Client;
    
    private final ConcurrentMap<String, CompletableFuture<String>> hostedZoneNamesToHostedZoneIds;
    
    private final ConcurrentMap<String, CompletableFuture<Iterable<ResourceRecordSet>>> hostedZoneIdsToResourceRecordSets;
    
    public DNSCache(Route53AsyncClient route53Client) {
        this.route53Client = route53Client;
        hostedZoneNamesToHostedZoneIds = new ConcurrentHashMap<>();
        hostedZoneIdsToResourceRecordSets = new ConcurrentHashMap<>();
    }

    public CompletableFuture<String> getHostedZoneId(String hostedZoneName) {
        return hostedZoneNamesToHostedZoneIds.computeIfAbsent(hostedZoneName,
                hzn->route53Client.listHostedZonesByName(b->b.dnsName(hzn)).handle(
                        (response, e)->response.hostedZones().iterator().next().id().replaceFirst("^\\/hostedzone\\/", "")));
    }
    
    private CompletableFuture<Iterable<ResourceRecordSet>> getResourceRecordSets(String hostedZoneId) {
        final ConcurrentLinkedQueue<ResourceRecordSet> result = new ConcurrentLinkedQueue<>();
        return hostedZoneIdsToResourceRecordSets.computeIfAbsent(hostedZoneId, hzid->{
            return route53Client.listResourceRecordSetsPaginator(b->b.hostedZoneId(hzid))
                    .subscribe(rrs->result.addAll(rrs.resourceRecordSets())).handle((v, e)->result);
        });
    }
    
    public CompletableFuture<Iterable<ResourceRecordSet>> getResourceRecordSetsAsync(String hostname) {
        return getHostedZoneId(AwsLandscape.getHostedZoneName(hostname)).thenCompose(hostedZoneId->
            getResourceRecordSets(hostedZoneId)).handle((resourceRecordSets, e)->
                Util.filter(resourceRecordSets, resourceRecordSet->resourceRecordSet.name().replaceFirst("\\.$", "").equals(hostname)));
    }
}
