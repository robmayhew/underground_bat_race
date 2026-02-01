package com.robmayhew;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Sprite Generator - Creates simple sprite textures for Godot Sprite2D
 */
public class App {

    private static final int SPRITE_SIZE = 64;

    public static void main(String[] args) {
        try {
            // Define sprite types and colors
            String[] spriteTypes = {
                    "blob", "robot", "ghost", "alien", "gem", "coin",
                    "player", "enemy", "npc", "powerup"
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

            int totalSprites = spriteTypes.length * colors.length;
            int rows = (int) Math.ceil((double) totalSprites / spriteTypes.length);
            int imageWidth = spriteTypes.length * SPRITE_SIZE;
            int imageHeight = rows * SPRITE_SIZE;

            // Create image with transparency support
            BufferedImage spriteSheet = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = spriteSheet.createGraphics();

            // Enable anti-aliasing for smooth sprites
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int row = 0;

            for (Color color : colors) {
                int col = 0;
                for (String spriteType : spriteTypes) {
                    int x = col * SPRITE_SIZE;
                    int y = row * SPRITE_SIZE;
                    col++;
                    drawSprite(g2d, spriteType, color, x, y);
                }
                row++;
            }

            g2d.dispose();

            // Save the sprite sheet
            File outputFile = new File("spritesheet.png");
            ImageIO.write(spriteSheet, "PNG", outputFile);
            System.out.println("Sprite sheet generated successfully: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error generating sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void drawSprite(Graphics2D g2d, String spriteType, Color color, int x, int y) {
        int centerX = x + SPRITE_SIZE / 2;
        int centerY = y + SPRITE_SIZE / 2;

        switch (spriteType.toLowerCase()) {
            case "blob":
                drawBlob(g2d, color, centerX, centerY);
                break;

            case "robot":
                drawRobot(g2d, color, centerX, centerY);
                break;

            case "ghost":
                drawGhost(g2d, color, centerX, centerY);
                break;

            case "alien":
                drawAlien(g2d, color, centerX, centerY);
                break;

            case "gem":
                drawGem(g2d, color, centerX, centerY);
                break;

            case "coin":
                drawCoin(g2d, color, centerX, centerY);
                break;

            case "player":
                drawPlayer(g2d, color, centerX, centerY);
                break;

            case "enemy":
                drawEnemy(g2d, color, centerX, centerY);
                break;

            case "npc":
                drawNPC(g2d, color, centerX, centerY);
                break;

            case "powerup":
                drawPowerup(g2d, color, centerX, centerY);
                break;
        }
    }

    // Blob - simple circular creature with eyes
    private static void drawBlob(Graphics2D g2d, Color color, int centerX, int centerY) {
        int size = 40;

        // Body
        g2d.setColor(color);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);

        // Eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 12, centerY - 8, 8, 8);
        g2d.fillOval(centerX + 4, centerY - 8, 8, 8);

        // Pupils
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 9, centerY - 6, 4, 4);
        g2d.fillOval(centerX + 7, centerY - 6, 4, 4);
    }

    // Robot - rectangular body with antenna
    private static void drawRobot(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Body
        g2d.setColor(color);
        g2d.fillRoundRect(centerX - 16, centerY - 12, 32, 28, 4, 4);

        // Head
        g2d.fillRoundRect(centerX - 12, centerY - 20, 24, 16, 4, 4);

        // Antenna
        g2d.fillRect(centerX - 2, centerY - 26, 4, 8);
        g2d.fillOval(centerX - 4, centerY - 30, 8, 8);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillRect(centerX - 10, centerY - 16, 6, 6);
        g2d.fillRect(centerX + 4, centerY - 16, 6, 6);

        // Chest panel
        g2d.setColor(color.darker());
        g2d.fillRect(centerX - 8, centerY - 4, 16, 12);
    }

    // Ghost - wavy bottom classic ghost shape
    private static void drawGhost(Graphics2D g2d, Color color, int centerX, int centerY) {
        Path2D.Double ghost = new Path2D.Double();

        // Top rounded part
        int size = 36;
        ghost.append(new Ellipse2D.Double(centerX - size / 2, centerY - size / 2, size, size), false);

        // Wavy bottom
        ghost.moveTo(centerX - size / 2, centerY + size / 4);
        ghost.curveTo(centerX - size / 2, centerY + size / 2,
                     centerX - size / 4, centerY + size / 2 - 4,
                     centerX - size / 6, centerY + size / 2 + 4);
        ghost.curveTo(centerX, centerY + size / 2 - 4,
                     centerX + size / 6, centerY + size / 2 + 4,
                     centerX + size / 4, centerY + size / 2 - 4);
        ghost.curveTo(centerX + size / 2, centerY + size / 2 + 4,
                     centerX + size / 2, centerY + size / 4,
                     centerX + size / 2, centerY);

        g2d.setColor(color);
        g2d.fill(ghost);

        // Eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 12, centerY - 6, 8, 12);
        g2d.fillOval(centerX + 4, centerY - 6, 8, 12);

        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 9, centerY - 2, 4, 6);
        g2d.fillOval(centerX + 7, centerY - 2, 4, 6);
    }

    // Alien - head with large eyes
    private static void drawAlien(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Head
        g2d.setColor(color);
        int[] xPoints = {centerX, centerX + 16, centerX + 12, centerX - 12, centerX - 16};
        int[] yPoints = {centerY - 20, centerY - 8, centerY + 12, centerY + 12, centerY - 8};
        g2d.fillPolygon(xPoints, yPoints, 5);

        // Large eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 16, centerY - 12, 12, 16);
        g2d.fillOval(centerX + 4, centerY - 12, 12, 16);

        // Eye highlights
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 12, centerY - 10, 4, 6);
        g2d.fillOval(centerX + 8, centerY - 10, 4, 6);
    }

    // Gem - diamond/crystal shape
    private static void drawGem(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Main gem body
        int[] xPoints = {centerX, centerX + 16, centerX + 10, centerX - 10, centerX - 16};
        int[] yPoints = {centerY - 16, centerY - 4, centerY + 16, centerY + 16, centerY - 4};

        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 5);

        // Facets
        g2d.setColor(color.brighter());
        int[] xFacet1 = {centerX, centerX + 16, centerX};
        int[] yFacet1 = {centerY - 16, centerY - 4, centerY + 4};
        g2d.fillPolygon(xFacet1, yFacet1, 3);

        // Dark facets
        g2d.setColor(color.darker());
        int[] xFacet2 = {centerX, centerX - 16, centerX};
        int[] yFacet2 = {centerY - 16, centerY - 4, centerY + 4};
        g2d.fillPolygon(xFacet2, yFacet2, 3);
    }

    // Coin - simple circle with design
    private static void drawCoin(Graphics2D g2d, Color color, int centerX, int centerY) {
        int size = 32;

        // Outer circle
        g2d.setColor(color);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);

        // Inner circle
        g2d.setColor(color.darker());
        g2d.fillOval(centerX - size / 2 + 4, centerY - size / 2 + 4, size - 8, size - 8);

        // Center symbol
        g2d.setColor(color);
        g2d.fillOval(centerX - 6, centerY - 6, 12, 12);
    }

    // Player - simple character
    private static void drawPlayer(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Body
        g2d.setColor(color);
        g2d.fillRoundRect(centerX - 12, centerY - 8, 24, 20, 6, 6);

        // Head
        g2d.setColor(new Color(255, 220, 177)); // Skin tone
        g2d.fillOval(centerX - 10, centerY - 20, 20, 20);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 7, centerY - 15, 3, 3);
        g2d.fillOval(centerX + 4, centerY - 15, 3, 3);

        // Legs
        g2d.setColor(color.darker());
        g2d.fillRect(centerX - 10, centerY + 12, 8, 8);
        g2d.fillRect(centerX + 2, centerY + 12, 8, 8);
    }

    // Enemy - menacing character
    private static void drawEnemy(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Body with spikes
        Path2D.Double body = new Path2D.Double();
        body.moveTo(centerX - 16, centerY);
        body.lineTo(centerX - 14, centerY - 8);
        body.lineTo(centerX - 8, centerY - 12);
        body.lineTo(centerX - 6, centerY - 18);
        body.lineTo(centerX, centerY - 14);
        body.lineTo(centerX + 6, centerY - 18);
        body.lineTo(centerX + 8, centerY - 12);
        body.lineTo(centerX + 14, centerY - 8);
        body.lineTo(centerX + 16, centerY);
        body.lineTo(centerX, centerY + 16);
        body.closePath();

        g2d.setColor(color);
        g2d.fill(body);

        // Angry eyes
        g2d.setColor(Color.RED);
        g2d.fillOval(centerX - 10, centerY - 8, 6, 6);
        g2d.fillOval(centerX + 4, centerY - 8, 6, 6);

        // Pupils
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 8, centerY - 6, 3, 3);
        g2d.fillOval(centerX + 6, centerY - 6, 3, 3);
    }

    // NPC - friendly character
    private static void drawNPC(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Body
        g2d.setColor(color);
        g2d.fillRoundRect(centerX - 14, centerY - 10, 28, 24, 8, 8);

        // Head
        g2d.setColor(color.brighter());
        g2d.fillOval(centerX - 12, centerY - 22, 24, 24);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 8, centerY - 16, 4, 4);
        g2d.fillOval(centerX + 4, centerY - 16, 4, 4);

        // Smile
        g2d.setStroke(new BasicStroke(2));
        g2d.drawArc(centerX - 6, centerY - 12, 12, 8, 180, 180);
    }

    // Powerup - star with glow
    private static void drawPowerup(Graphics2D g2d, Color color, int centerX, int centerY) {
        // Outer glow
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        drawStar(g2d, centerX, centerY, 24, 12, 5);

        // Main star
        g2d.setColor(color);
        drawStar(g2d, centerX, centerY, 18, 8, 5);

        // Inner highlight
        g2d.setColor(Color.WHITE);
        drawStar(g2d, centerX, centerY, 8, 4, 5);
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
}
