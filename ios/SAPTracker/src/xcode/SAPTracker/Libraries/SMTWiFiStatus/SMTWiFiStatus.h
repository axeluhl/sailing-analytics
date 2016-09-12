//
//  SMTWiFiStatus.h
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
// See: http://www.enigmaticape.com/blog/determine-wifi-enabled-ios-one-weird-trick

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, WiFiStatus) {
    On,
    Off,
    Unknown
};

@interface SMTWiFiStatus : NSObject

+ (WiFiStatus)wifiStatus;

@end
