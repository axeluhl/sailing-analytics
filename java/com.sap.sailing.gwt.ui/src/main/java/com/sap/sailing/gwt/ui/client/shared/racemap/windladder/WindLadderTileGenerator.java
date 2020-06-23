package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.dom.client.CanvasElement;

@FunctionalInterface
public interface WindLadderTileGenerator {
    CanvasElement getTile();
}