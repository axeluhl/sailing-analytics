//
//  Appearance.m
//  SAPTracker
//
//  Created by computing on 01/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

#import "Appearance.h"
#import <UIKit/UIKit.h>

@implementation Appearance

+ (void)setAppearance {
    [[UILabel appearanceWhenContainedIn:[UITableViewHeaderFooterView class], nil] setFont:[UIFont fontWithName:@"OpenSans-Bold" size:16]];
    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]setTitleTextAttributes:@{NSFontAttributeName:[UIFont fontWithName:@"OpenSans" size:17]} forState:UIControlStateNormal];
}

@end
