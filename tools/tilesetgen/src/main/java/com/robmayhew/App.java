package com.robmayhew;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App {

    private static final int TILE_SIZE = 64;

    // ===== WATER TILE CONSTANTS =====

    private static final Color WATER_DEEP    = new Color(30,  100, 200);
    private static final Color WATER_SHALLOW = new Color(100, 180, 240);
    private static final Color LAND_COLOR    = new Color(80,  160,  60);
    private static final Color SHORE_COLOR   = new Color(210, 190, 120);

    // Shore edge tile dimensions
    private static final int SHORE_SIZE    = 16; // land border thickness (px)
    private static final int SHORE_STRIP   =  4; // sandy beach strip width (px)
    private static final int SHALLOW_STRIP =  8; // shallow-water band width (px)

    // Radius of the arc used for corner tiles
    private static final int CORNER_RADIUS = TILE_SIZE / 2; // 32 px

    // ===== ENTRY POINT =====

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "shapes";
        if ("water".equals(mode)) {
            generateWaterTileset();
        } else {
            generateShapesTileset();
        }
    }

    // ===== WATER TILESET =====

    /**
     * Generates a 2-D top-down water tileset and saves it as water_tileset.png.
     *
     * Tile catalogue (15 tiles, 8 per row):
     *   Row 0: water, water-shallow, land,
     *           shore-n, shore-s, shore-e, shore-w,
     *           corner-ne
     *   Row 1: corner-nw, corner-se, corner-sw,
     *           inner-ne, inner-nw, inner-se, inner-sw
     *
     * "shore-X"  – water tile with a land edge on side X
     * "corner-X" – water tile with a small land protrusion at corner X (convex)
     * "inner-X"  – land tile with a small water bay at corner X (concave)
     */
    private static void generateWaterTileset() {
        String[] tiles = {
            "water", "water-shallow", "land",
            "shore-n", "shore-s", "shore-e", "shore-w",
            "corner-ne",
            "corner-nw", "corner-se", "corner-sw",
            "inner-ne",  "inner-nw",  "inner-se",  "inner-sw"
        };

        int cols = 8;
        int rows = (int) Math.ceil((double) tiles.length / cols);
        BufferedImage image = new BufferedImage(
                cols * TILE_SIZE, rows * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < tiles.length; i++) {
            int tx = (i % cols) * TILE_SIZE;
            int ty = (i / cols) * TILE_SIZE;
            drawWaterTile(g2d, tiles[i], tx, ty);
        }

        g2d.dispose();
        saveImage(image, "water_tileset.png");
    }

    private static void drawWaterTile(Graphics2D g2d, String tile, int x, int y) {
        // Clip to this tile's bounds so arcs never bleed into neighbours.
        Shape oldClip = g2d.getClip();
        g2d.setClip(x, y, TILE_SIZE, TILE_SIZE);

        switch (tile) {
            case "water":         drawDeepWater(g2d, x, y);                break;
            case "water-shallow": drawShallowWater(g2d, x, y);             break;
            case "land":          drawFullLand(g2d, x, y);                 break;
            case "shore-n":       drawShoreEdge(g2d, x, y, "n");           break;
            case "shore-s":       drawShoreEdge(g2d, x, y, "s");           break;
            case "shore-e":       drawShoreEdge(g2d, x, y, "e");           break;
            case "shore-w":       drawShoreEdge(g2d, x, y, "w");           break;
            case "corner-ne":     drawShoreOuterCorner(g2d, x, y, "ne");   break;
            case "corner-nw":     drawShoreOuterCorner(g2d, x, y, "nw");   break;
            case "corner-se":     drawShoreOuterCorner(g2d, x, y, "se");   break;
            case "corner-sw":     drawShoreOuterCorner(g2d, x, y, "sw");   break;
            case "inner-ne":      drawShoreInnerCorner(g2d, x, y, "ne");   break;
            case "inner-nw":      drawShoreInnerCorner(g2d, x, y, "nw");   break;
            case "inner-se":      drawShoreInnerCorner(g2d, x, y, "se");   break;
            case "inner-sw":      drawShoreInnerCorner(g2d, x, y, "sw");   break;
        }

        g2d.setClip(oldClip);
    }

    // --- Full tiles ---

    private static void drawDeepWater(Graphics2D g2d, int x, int y) {
        g2d.setColor(WATER_DEEP);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        addWaves(g2d, x, y, TILE_SIZE, TILE_SIZE);
    }

    private static void drawShallowWater(Graphics2D g2d, int x, int y) {
        g2d.setColor(WATER_SHALLOW);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        addWaves(g2d, x, y, TILE_SIZE, TILE_SIZE);
    }

    private static void drawFullLand(Graphics2D g2d, int x, int y) {
        g2d.setColor(LAND_COLOR);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    // --- Shore edge tiles ---
    //
    // dir = which side has LAND ("n"=top, "s"=bottom, "e"=right, "w"=left).
    // The tile transitions from land → sandy shore → shallow water → deep water.

    private static void drawShoreEdge(Graphics2D g2d, int x, int y, String dir) {
        int d  = SHORE_SIZE;    // land depth
        int ss = SHORE_STRIP;   // sandy strip
        int sw = SHALLOW_STRIP; // shallow-water band

        // Fill entire tile with land first, then paint water bands over it.
        g2d.setColor(LAND_COLOR);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        switch (dir) {
            case "n": // land at top, water below
                g2d.setColor(SHORE_COLOR);
                g2d.fillRect(x, y + d, TILE_SIZE, ss);
                g2d.setColor(WATER_SHALLOW);
                g2d.fillRect(x, y + d + ss, TILE_SIZE, sw);
                g2d.setColor(WATER_DEEP);
                g2d.fillRect(x, y + d + ss + sw, TILE_SIZE, TILE_SIZE - d - ss - sw);
                addWaves(g2d, x, y + d + ss + sw, TILE_SIZE, TILE_SIZE - d - ss - sw);
                break;

            case "s": // land at bottom, water above
                g2d.setColor(WATER_DEEP);
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE - d - ss - sw);
                g2d.setColor(WATER_SHALLOW);
                g2d.fillRect(x, y + TILE_SIZE - d - ss - sw, TILE_SIZE, sw);
                g2d.setColor(SHORE_COLOR);
                g2d.fillRect(x, y + TILE_SIZE - d - ss, TILE_SIZE, ss);
                addWaves(g2d, x, y, TILE_SIZE, TILE_SIZE - d - ss - sw);
                break;

            case "e": // land at right, water to the left
                g2d.setColor(WATER_DEEP);
                g2d.fillRect(x, y, TILE_SIZE - d - ss - sw, TILE_SIZE);
                g2d.setColor(WATER_SHALLOW);
                g2d.fillRect(x + TILE_SIZE - d - ss - sw, y, sw, TILE_SIZE);
                g2d.setColor(SHORE_COLOR);
                g2d.fillRect(x + TILE_SIZE - d - ss, y, ss, TILE_SIZE);
                addWaves(g2d, x, y, TILE_SIZE - d - ss - sw, TILE_SIZE);
                break;

            case "w": // land at left, water to the right
                g2d.setColor(SHORE_COLOR);
                g2d.fillRect(x + d, y, ss, TILE_SIZE);
                g2d.setColor(WATER_SHALLOW);
                g2d.fillRect(x + d + ss, y, sw, TILE_SIZE);
                g2d.setColor(WATER_DEEP);
                g2d.fillRect(x + d + ss + sw, y, TILE_SIZE - d - ss - sw, TILE_SIZE);
                addWaves(g2d, x + d + ss + sw, y, TILE_SIZE - d - ss - sw, TILE_SIZE);
                break;
        }
    }

    // --- Outer corner tiles (convex) ---
    //
    // Mostly water; land protrudes from the named corner as a curved headland.
    // From the corner outward: land core → sandy fringe → shallow ring → deep water.
    //
    // Arc technique: fillArc draws a filled pie-slice from the circle centre.
    // Drawing larger pies first, then smaller ones on top, produces concentric bands.
    //
    // Corner → arc-centre → Java startAngle (0=east, CCW positive):
    //   NE (top-right)   → 180° (lower-left quadrant enters the tile)
    //   NW (top-left)    → 270°
    //   SE (bottom-right) → 90°
    //   SW (bottom-left)  → 0°

    private static void drawShoreOuterCorner(Graphics2D g2d, int x, int y, String dir) {
        int[] cp = cornerParams(x, y, dir);
        int cx = cp[0], cy = cp[1], startAngle = cp[2];

        // Background: deep water
        g2d.setColor(WATER_DEEP);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        // Shallow ring around the land protrusion
        int rShallow = CORNER_RADIUS + SHALLOW_STRIP;
        g2d.setColor(WATER_SHALLOW);
        g2d.fillArc(cx - rShallow, cy - rShallow, 2 * rShallow, 2 * rShallow, startAngle, 90);

        // Sandy shore fringe
        g2d.setColor(SHORE_COLOR);
        g2d.fillArc(cx - CORNER_RADIUS, cy - CORNER_RADIUS,
                    2 * CORNER_RADIUS, 2 * CORNER_RADIUS, startAngle, 90);

        // Land core
        int rLand = CORNER_RADIUS - SHORE_STRIP;
        g2d.setColor(LAND_COLOR);
        g2d.fillArc(cx - rLand, cy - rLand, 2 * rLand, 2 * rLand, startAngle, 90);

        addWaves(g2d, x, y, TILE_SIZE, TILE_SIZE);
    }

    // --- Inner corner tiles (concave) ---
    //
    // Mostly land; a water bay curves into the named corner.
    // From the corner outward: deep water core → shallow ring → sandy fringe → land.

    private static void drawShoreInnerCorner(Graphics2D g2d, int x, int y, String dir) {
        int[] cp = cornerParams(x, y, dir);
        int cx = cp[0], cy = cp[1], startAngle = cp[2];

        // Background: land
        g2d.setColor(LAND_COLOR);
        g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        // Shore fringe (outermost layer of the bay)
        g2d.setColor(SHORE_COLOR);
        g2d.fillArc(cx - CORNER_RADIUS, cy - CORNER_RADIUS,
                    2 * CORNER_RADIUS, 2 * CORNER_RADIUS, startAngle, 90);

        // Shallow water
        int rShallow = CORNER_RADIUS - SHORE_STRIP;
        g2d.setColor(WATER_SHALLOW);
        g2d.fillArc(cx - rShallow, cy - rShallow, 2 * rShallow, 2 * rShallow, startAngle, 90);

        // Deep water core (innermost – closest to the corner)
        int rDeep = rShallow - SHALLOW_STRIP;
        g2d.setColor(WATER_DEEP);
        g2d.fillArc(cx - rDeep, cy - rDeep, 2 * rDeep, 2 * rDeep, startAngle, 90);
    }

    /** Returns {cx, cy, startAngle} for a corner arc centred at the named tile corner. */
    private static int[] cornerParams(int x, int y, String dir) {
        switch (dir) {
            case "ne": return new int[]{ x + TILE_SIZE, y,            180 };
            case "nw": return new int[]{ x,             y,            270 };
            case "se": return new int[]{ x + TILE_SIZE, y + TILE_SIZE, 90 };
            case "sw": return new int[]{ x,             y + TILE_SIZE,  0 };
            default:   return new int[]{ x,             y,              0 };
        }
    }

    // --- Wave detail overlay ---
    //
    // Draws a grid of small semi-circular wave arcs in a translucent lighter blue.
    // Every other row is offset half a spacing unit for a natural stagger.

    private static void addWaves(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.setColor(new Color(200, 230, 255, 120));
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int waveW = 12, waveH = 5, spacing = 18;
        for (int row = 0; row * spacing < h; row++) {
            for (int col = 0; col * spacing < w; col++) {
                int wx = x + col * spacing + (row % 2) * 9;
                int wy = y + row * spacing + 4;
                if (wx + waveW <= x + w && wy + waveH <= y + h) {
                    g2d.drawArc(wx, wy, waveW, waveH, 0, 180);
                }
            }
        }

        g2d.setStroke(old);
    }

    // --- I/O helper ---

    private static void saveImage(BufferedImage image, String filename) {
        try {
            File out = new File(filename);
            ImageIO.write(image, "PNG", out);
            System.out.println("Generated: " + out.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating " + filename + ": " + e.getMessage());
        }
    }

    // ===== SHAPES TILESET (original) =====

    private static void generateShapesTileset() {
        try {
            String[] shapeNames = {
                    "circle", "square", "triangle", "star", "hexagon", "pentagon", "diamond", "octagon",
                    "road-h", "road-v", "road-ne", "road-nw", "road-se", "road-sw",
                    "road-n-t", "road-s-t", "road-e-t", "road-w-t", "road-4way"
            };
            Color[] colors = {
                    Color.RED,
                    Color.BLUE,
                    Color.GREEN,
                    Color.YELLOW,
                    Color.MAGENTA,
                    Color.CYAN,
                    Color.ORANGE,
                    new Color(128, 0, 128) // Purple
            };

            int totalTiles = shapeNames.length * colors.length;
            int rows = (int) Math.ceil((double) totalTiles / shapeNames.length);
            int imageWidth = shapeNames.length * TILE_SIZE;
            int imageHeight = rows * TILE_SIZE;

            BufferedImage tileset = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tileset.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int row = 0;
            for (Color color : colors) {
                int col = 0;
                for (String shape : shapeNames) {
                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE;
                    col++;
                    g2d.setColor(color);
                    drawShape(g2d, shape, x, y);
                }
                row++;
            }

            g2d.dispose();

            File outputFile = new File("tileset.png");
            ImageIO.write(tileset, "PNG", outputFile);
            System.out.println("Tileset generated successfully: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error generating tileset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void drawShape(Graphics2D g2d, String shape, int x, int y) {
        int padding = 8;
        int size = TILE_SIZE - (padding * 2);
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        switch (shape.toLowerCase()) {
            case "circle":
                g2d.fillOval(x + padding, y + padding, size, size);
                break;

            case "square":
                g2d.fillRect(x + padding, y + padding, size, size);
                break;

            case "triangle":
                int[] xTriangle = {centerX, x + padding, x + TILE_SIZE - padding};
                int[] yTriangle = {y + padding, y + TILE_SIZE - padding, y + TILE_SIZE - padding};
                g2d.fillPolygon(xTriangle, yTriangle, 3);
                break;

            case "star":
                drawStar(g2d, centerX, centerY, size / 2, size / 4, 5);
                break;

            case "hexagon":
                drawRegularPolygon(g2d, centerX, centerY, size / 2, 6);
                break;

            case "pentagon":
                drawRegularPolygon(g2d, centerX, centerY, size / 2, 5);
                break;

            case "diamond":
                int[] xDiamond = {centerX, x + TILE_SIZE - padding, centerX, x + padding};
                int[] yDiamond = {y + padding, centerY, y + TILE_SIZE - padding, centerY};
                g2d.fillPolygon(xDiamond, yDiamond, 4);
                break;

            case "octagon":
                drawRegularPolygon(g2d, centerX, centerY, size / 2, 8);
                break;

            case "road-h":   drawRoadHorizontal(g2d, x, y);         break;
            case "road-v":   drawRoadVertical(g2d, x, y);           break;
            case "road-ne":  drawRoadCorner(g2d, x, y, "ne");       break;
            case "road-nw":  drawRoadCorner(g2d, x, y, "nw");       break;
            case "road-se":  drawRoadCorner(g2d, x, y, "se");       break;
            case "road-sw":  drawRoadCorner(g2d, x, y, "sw");       break;
            case "road-n-t": drawRoadTJunction(g2d, x, y, "n");     break;
            case "road-s-t": drawRoadTJunction(g2d, x, y, "s");     break;
            case "road-e-t": drawRoadTJunction(g2d, x, y, "e");     break;
            case "road-w-t": drawRoadTJunction(g2d, x, y, "w");     break;
            case "road-4way": drawRoad4Way(g2d, x, y);              break;
        }
    }

    private static void drawStar(Graphics2D g2d, int centerX, int centerY, int outerRadius, int innerRadius, int points) {
        Path2D.Double star = new Path2D.Double();
        double angleStep = Math.PI / points;

        for (int i = 0; i < points * 2; i++) {
            double angle = i * angleStep - Math.PI / 2;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double px = centerX + radius * Math.cos(angle);
            double py = centerY + radius * Math.sin(angle);

            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        g2d.fill(star);
    }

    private static void drawRegularPolygon(Graphics2D g2d, int centerX, int centerY, int radius, int sides) {
        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];

        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides - Math.PI / 2;
            xPoints[i] = (int) (centerX + radius * Math.cos(angle));
            yPoints[i] = (int) (centerY + radius * Math.sin(angle));
        }

        g2d.fillPolygon(xPoints, yPoints, sides);
    }

    private static final int ROAD_WIDTH = 20;

    private static void drawRoadHorizontal(Graphics2D g2d, int x, int y) {
        int roadY = y + (TILE_SIZE - ROAD_WIDTH) / 2;
        g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH);
    }

    private static void drawRoadVertical(Graphics2D g2d, int x, int y) {
        int roadX = x + (TILE_SIZE - ROAD_WIDTH) / 2;
        g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE);
    }

    private static void drawRoadCorner(Graphics2D g2d, int x, int y, String direction) {
        int roadX = x + (TILE_SIZE - ROAD_WIDTH) / 2;
        int roadY = y + (TILE_SIZE - ROAD_WIDTH) / 2;
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        switch (direction) {
            case "ne":
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2);
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "nw":
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2);
                g2d.fillRect(x, roadY, centerX - x + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "se":
                g2d.fillRect(roadX, centerY - ROAD_WIDTH / 2, ROAD_WIDTH, y + TILE_SIZE - centerY + ROAD_WIDTH / 2);
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "sw":
                g2d.fillRect(roadX, centerY - ROAD_WIDTH / 2, ROAD_WIDTH, y + TILE_SIZE - centerY + ROAD_WIDTH / 2);
                g2d.fillRect(x, roadY, centerX - x + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
        }
    }

    private static void drawRoadTJunction(Graphics2D g2d, int x, int y, String direction) {
        int roadX = x + (TILE_SIZE - ROAD_WIDTH) / 2;
        int roadY = y + (TILE_SIZE - ROAD_WIDTH) / 2;
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        switch (direction) {
            case "n":
                g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH);
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2);
                break;
            case "s":
                g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH);
                g2d.fillRect(roadX, centerY - ROAD_WIDTH / 2, ROAD_WIDTH, y + TILE_SIZE - centerY + ROAD_WIDTH / 2);
                break;
            case "e":
                g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE);
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "w":
                g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE);
                g2d.fillRect(x, roadY, centerX - x + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
        }
    }

    private static void drawRoad4Way(Graphics2D g2d, int x, int y) {
        int roadX = x + (TILE_SIZE - ROAD_WIDTH) / 2;
        int roadY = y + (TILE_SIZE - ROAD_WIDTH) / 2;
        g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH);
        g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE);
    }
}
