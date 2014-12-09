//
//  TrackingPageViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingPageViewController : UIPageViewController, UIPageViewControllerDataSource {
    
    var page1: UIViewController?
    var page2: UIViewController?
    var page3: UIViewController?
    
    override func viewDidLoad() {
        dataSource = self
        page1 = storyboard!.instantiateViewControllerWithIdentifier("Tracking") as? TrackingViewController
        page2 = storyboard!.instantiateViewControllerWithIdentifier("Speed") as? UIViewController
        page3 = storyboard!.instantiateViewControllerWithIdentifier("Heading") as? UIViewController
        setViewControllers([page1!], direction: UIPageViewControllerNavigationDirection.Forward, animated: false, completion: nil)
    }
    
    func pageViewController(pageViewController: UIPageViewController,
        viewControllerBeforeViewController viewController: UIViewController) -> UIViewController? {
            if (viewController == page1) {
                return page3
            } else if (viewController == page2) {
                return page1
            } else if (viewController == page3) {
                return page2
            }
            return nil
    }
    
    func pageViewController(pageViewController: UIPageViewController,
        viewControllerAfterViewController viewController: UIViewController) -> UIViewController? {
            if (viewController == page1) {
                return page2
            } else if (viewController == page2) {
                return page3
            } else if (viewController == page3) {
                return page1
            }
            return nil
    }
}