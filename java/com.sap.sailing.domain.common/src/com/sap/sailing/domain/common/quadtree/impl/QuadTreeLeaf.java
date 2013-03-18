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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTreeLeaf.java,v
// $
// $RCSfile$
// $Revision: 45 $
// $Date: 2008-06-08 22:04:43 +0200 (Sun, 08 Jun 2008) $
// $Author: uhl $
// 
// **********************************************************************

package com.sap.sailing.domain.common.quadtree.impl;

import java.io.Serializable;

import com.sap.sailing.domain.common.Position;

public class QuadTreeLeaf<T> implements Serializable {

    static final long serialVersionUID = 7885745536157252519L;

    private Position point;
    private T object;

    public QuadTreeLeaf(Position point, T obj) {
        this.point = point;
        this.object = obj;
    }

    public Position getPoint() {
        return point;
    }

    public T getObject() {
        return object;
    }

    public String toString() {
        return "QuadTreeLeaf at (" + point.getLatDeg() + ", " + point.getLngDeg() + ") with object " + getObject();
    }

}