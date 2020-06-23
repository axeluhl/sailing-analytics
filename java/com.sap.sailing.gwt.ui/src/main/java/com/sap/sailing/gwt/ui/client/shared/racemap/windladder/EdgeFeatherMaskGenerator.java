package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;

public class EdgeFeatherMaskGenerator implements WindLadderMaskGenerator {
    protected final WindLadderTileGenerator edgeTileGen = new DynamicTileGenerator(256) {
        @Override
        protected void drawTile(Context2d ctx) {
            CanvasGradient grad = ctx.createLinearGradient(0, 0, tileSize, 0);
            grad.addColorStop(0, "rgba(255, 255, 255, 0)");
            grad.addColorStop(1, "rgba(255, 255, 255, 1)");
            ctx.setFillStyle(grad);
            ctx.fillRect(0, 0, tileSize, tileSize);
        }
    };
    protected final WindLadderTileGenerator cornerTileGen = new DynamicTileGenerator(256) {
        @Override
        protected void drawTile(Context2d ctx) {
            CanvasGradient grad = ctx.createLinearGradient(0, 0, tileSize, 0);
            grad.addColorStop(0, "rgba(255, 255, 255, 0)");
            grad.addColorStop(1, "rgba(255, 255, 255, 1)");
            ctx.setFillStyle(grad);
            // Lower-left triangle (horizontal gradient)
            ctx.beginPath();
            ctx.moveTo(0, 0); // Top-left
            ctx.lineTo(tileSize, tileSize); // Bottom-right
            ctx.lineTo(0, tileSize); // Bottom-left
            ctx.closePath();
            ctx.fill();
            // Upper-right triangle (vertical gradient)
            ctx.beginPath();
            ctx.moveTo(0, 0); // Top-left
            ctx.lineTo(tileSize, tileSize); // Bottom-right
            ctx.lineTo(tileSize, 0); // Top-right
            ctx.closePath();
            ctx.rotate(Math.PI / 2); // Rotate gradient from horizontal to vertical
            ctx.fill();
        }
    };

    protected final double featherAmount;

    public EdgeFeatherMaskGenerator(double featherAmount) {
        this.featherAmount = featherAmount;
    }

    @Override
    public void drawMask(int width, int height, Context2d ctx) {
        final int featherDistance = (int) (Math.min(width, height) * featherAmount);

        // Contract requirement
        ctx.save();

        // Fill center
        ctx.setFillStyle("rgba(255, 255, 255, 1)");
        ctx.fillRect(featherDistance, featherDistance, width - 2 * featherDistance, height - 2 * featherDistance);
        // Left edge
        ctx.drawImage(edgeTileGen.getTile(), 0, featherDistance, featherDistance, height - 2 * featherDistance);
        // Top-left corner
        ctx.drawImage(cornerTileGen.getTile(), 0, 0, featherDistance, featherDistance);
        // Top edge
        ctx.translate(width, 0);
        ctx.rotate(Math.PI / 2);
        ctx.drawImage(edgeTileGen.getTile(), 0, featherDistance, featherDistance, width - 2 * featherDistance);
        // Top-right corner
        ctx.drawImage(cornerTileGen.getTile(), 0, 0, featherDistance, featherDistance);
        // Right edge
        ctx.translate(height, 0);
        ctx.rotate(Math.PI / 2);
        ctx.drawImage(edgeTileGen.getTile(), 0, featherDistance, featherDistance, height - 2 * featherDistance);
        // Bottom-right corner
        ctx.drawImage(cornerTileGen.getTile(), 0, 0, featherDistance, featherDistance);
        // Bottom edge
        ctx.translate(width, 0);
        ctx.rotate(Math.PI / 2);
        ctx.drawImage(edgeTileGen.getTile(), 0, featherDistance, featherDistance, width - 2 * featherDistance);
        // Bottom-left corner
        ctx.drawImage(cornerTileGen.getTile(), 0, 0, featherDistance, featherDistance);
        ctx.restore();
    }
}
