package com.robmayhew;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {

    private static final int TILE_SIZE = 64;

    public static void main(String[] args) {
        try {
            // Define shapes and colors
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

            // Create image with transparency support
            BufferedImage tileset = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tileset.createGraphics();

            // Enable anti-aliasing for smooth shapes
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int row = 0;

            for(Color color : colors)
            {
                int col = 0;
                for(String shape : shapeNames)
                {
                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE;
                    col++;
                    g2d.setColor(color);
                    drawShape(g2d, shape, x, y);
                }
                row++;
            }


            g2d.dispose();

            // Save the tileset
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

            // Road tiles
            case "road-h":
                drawRoadHorizontal(g2d, x, y);
                break;

            case "road-v":
                drawRoadVertical(g2d, x, y);
                break;

            case "road-ne":
                drawRoadCorner(g2d, x, y, "ne");
                break;

            case "road-nw":
                drawRoadCorner(g2d, x, y, "nw");
                break;

            case "road-se":
                drawRoadCorner(g2d, x, y, "se");
                break;

            case "road-sw":
                drawRoadCorner(g2d, x, y, "sw");
                break;

            case "road-n-t":
                drawRoadTJunction(g2d, x, y, "n");
                break;

            case "road-s-t":
                drawRoadTJunction(g2d, x, y, "s");
                break;

            case "road-e-t":
                drawRoadTJunction(g2d, x, y, "e");
                break;

            case "road-w-t":
                drawRoadTJunction(g2d, x, y, "w");
                break;

            case "road-4way":
                drawRoad4Way(g2d, x, y);
                break;
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

    // Road tile drawing methods
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
            case "ne": // North-East corner
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2);
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "nw": // North-West corner
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2);
                g2d.fillRect(x, roadY, centerX - x + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "se": // South-East corner
                g2d.fillRect(roadX, centerY - ROAD_WIDTH / 2, ROAD_WIDTH, y + TILE_SIZE - centerY + ROAD_WIDTH / 2);
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH);
                break;
            case "sw": // South-West corner
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
            case "n": // T with opening to north (roads go W, E, N)
                g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH); // Horizontal
                g2d.fillRect(roadX, y, ROAD_WIDTH, centerY - y + ROAD_WIDTH / 2); // North
                break;
            case "s": // T with opening to south (roads go W, E, S)
                g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH); // Horizontal
                g2d.fillRect(roadX, centerY - ROAD_WIDTH / 2, ROAD_WIDTH, y + TILE_SIZE - centerY + ROAD_WIDTH / 2); // South
                break;
            case "e": // T with opening to east (roads go N, S, E)
                g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE); // Vertical
                g2d.fillRect(centerX - ROAD_WIDTH / 2, roadY, x + TILE_SIZE - centerX + ROAD_WIDTH / 2, ROAD_WIDTH); // East
                break;
            case "w": // T with opening to west (roads go N, S, W)
                g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE); // Vertical
                g2d.fillRect(x, roadY, centerX - x + ROAD_WIDTH / 2, ROAD_WIDTH); // West
                break;
        }
    }

    private static void drawRoad4Way(Graphics2D g2d, int x, int y) {
        int roadX = x + (TILE_SIZE - ROAD_WIDTH) / 2;
        int roadY = y + (TILE_SIZE - ROAD_WIDTH) / 2;
        // Draw horizontal and vertical roads crossing
        g2d.fillRect(x, roadY, TILE_SIZE, ROAD_WIDTH);
        g2d.fillRect(roadX, y, ROAD_WIDTH, TILE_SIZE);
    }
}
