// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source$
// $RCSfile$
// $Revision: 15 $
// $Date: 2008-04-20 02:18:58 +0200 (Sun, 20 Apr 2008) $
// $Author: uhl $
// 
// **********************************************************************

package com.sap.sailing.domain.common.quadtree.impl;

/**
 * A *really* simple class used as a changable double.
 */
public class MutableDistance {
    public double value = 0;

    public MutableDistance(double distance) {
        value = distance;
    }
}