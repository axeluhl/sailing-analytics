//
//  SMTWiFiStatus.m
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
// See: http://www.enigmaticape.com/blog/determine-wifi-enabled-ios-one-weird-trick

#import <Foundation/Foundation.h>

#import <ifaddrs.h>
#import <net/if.h>
#import <SystemConfiguration/CaptiveNetwork.h>

#import "SMTWiFiStatus.h"

@implementation SMTWiFiStatus : NSObject

+ (WiFiStatus)wifiStatus {
    NSCountedSet *cset = [NSCountedSet new];
    struct ifaddrs *interfaces;
    if (!getifaddrs(&interfaces)) {
        for (struct ifaddrs *interface = interfaces; interface; interface = interface->ifa_next) {
            if ((interface->ifa_flags & IFF_UP) == IFF_UP ) {
                [cset addObject:[NSString stringWithUTF8String:interface->ifa_name]];
            }
        }
    }
    freeifaddrs(interfaces);
    NSUInteger count = [cset countForObject:@"awdl0"];
    return count > 1 ? On : (count > 0 ? Off : Unknown);
}

@end
