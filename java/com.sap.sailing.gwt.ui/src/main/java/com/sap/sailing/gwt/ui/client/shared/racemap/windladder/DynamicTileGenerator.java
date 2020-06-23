package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;

public abstract class DynamicTileGenerator implements WindLadderTileGenerator {
    protected Canvas tile;
    protected final int tileSize;

    public DynamicTileGenerator(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cannot generate tile smaller than 1 px: " + size);
        }
        tileSize = size;
    }

    @Override
    public CanvasElement getTile() {
        generateTile();
        return tile.getCanvasElement();
    }

    protected abstract void drawTile(Context2d ctx);

    protected void generateTile() {
        if (tile == null) {
            tile = Canvas.createIfSupported();
        }
        // Setting the size clears the canvas
        tile.setPixelSize(tileSize, tileSize);
        tile.setCoordinateSpaceWidth(tileSize);
        tile.setCoordinateSpaceHeight(tileSize);

        drawTile(tile.getContext2d());
    }
}
