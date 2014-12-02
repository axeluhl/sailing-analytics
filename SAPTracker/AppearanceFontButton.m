//
//  AppearanceFontButton.m
//  SAPTracker
//
//  Created by computing on 01/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

#import "AppearanceFontButton.h"

@implementation AppearanceFontButton

- (void)setTitleFont:(UIFont *)titleFont
{
    if (_titleFont != titleFont) {
        _titleFont = titleFont;
        [self.titleLabel setFont:_titleFont];
    }
}

@end
