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
        page1 = storyboard!.instantiateViewController(withIdentifier: "Timer") as? TimerViewController
        page2 = storyboard!.instantiateViewController(withIdentifier: "Course")
        page3 = storyboard!.instantiateViewController(withIdentifier: "Speed")
        setViewControllers([page1!], direction: UIPageViewControllerNavigationDirection.forward, animated: false, completion: nil)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController,
        viewControllerBefore viewController: UIViewController) -> UIViewController? {
            if (viewController == page1) {
                return page3
            } else if (viewController == page2) {
                return page1
            } else if (viewController == page3) {
                return page2
            }
            return nil
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        if (viewController == page1) {
            return page2
        } else if (viewController == page2) {
            return page3
        } else if (viewController == page3) {
            return page1
        }
        return nil
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 3
    }
    
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return 0
    }
    
}
