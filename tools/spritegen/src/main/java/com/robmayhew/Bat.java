package com.robmayhew;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Sprite Generator - Creates simple sprite textures for Godot Sprite2D
 */
public class Bat {

    private static final int SPRITE_SIZE = 64;
    private static final int ANIMATIONS_PER_ROW = 5;
    public static void main(String[] args) {
        try {
            // Define sprite types and colors
            String[] spriteAnimations = new String[]{
                    "fly",
                    "fall",
                    "idle",
                    "idel2"
            };
            int framesPerAnimation = ANIMATIONS_PER_ROW;

            int totalSprites = spriteAnimations.length * framesPerAnimation;
            int rows = spriteAnimations.length;
            int imageWidth = spriteAnimations.length * SPRITE_SIZE;
            int imageHeight = rows * SPRITE_SIZE;

            // Create image with transparency support
            BufferedImage spriteSheet = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = spriteSheet.createGraphics();

            // Enable anti-aliasing for smooth sprites
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int row = 0;

            for (String sprite : spriteAnimations) {

                    drawSprite(g2d, sprite, row);

                row++;
            }

            g2d.dispose();

            // Save the sprite sheet
            File outputFile = new File("bat.png");
            ImageIO.write(spriteSheet, "PNG", outputFile);
            System.out.println("Sprite sheet generated successfully: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error generating sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void drawSprite(Graphics2D g2d, String spriteAnimation, int row) {
        Color color = Color.RED;
        drawBat(g2d, color, row);
    }



    // Bat - flying creature with wings spread
    private static void drawBat(Graphics2D g2d, Color color, int row) {
        // Wing flapping animation cycle: down -> neutral -> up -> neutral
        // Eye direction: looking left (-2), center (0), right (2), slightly up-right (1.5)
        drawBat(g2d, color, row, 0, 0.3f, -2.0f);  // Wings down, looking left
        drawBat(g2d, color, row, 1, 0.0f, 0.0f);   // Wings neutral, looking center
        drawBat(g2d, color, row, 2, -0.3f, 2.0f);  // Wings up, looking right
        drawBat(g2d, color, row, 3, 0.0f, 1.0f);   // Wings neutral, looking slightly right
        drawBat(g2d, color, row, 4, 0.2f, -1.5f);  // Wings mid-down, looking left-center


    }

    private static void drawBat(Graphics2D g2d, Color color, int row, int col, float wingRotation, float eyeDirection) {

    int centerX = col * SPRITE_SIZE + SPRITE_SIZE / 2;
    int centerY = row * SPRITE_SIZE + SPRITE_SIZE / 2;

    // Save the original transform
    Graphics2D g2dWings = (Graphics2D) g2d.create();

    // Body (rounded oval)
    g2d.setColor(color);
    g2d.fillOval(centerX - 10, centerY - 6, 20, 18);

    // Head
    g2d.fillOval(centerX - 8, centerY - 14, 16, 16);

    // Ears (pointed triangles)
    int[] earLeftX = {centerX - 8, centerX - 5, centerX - 2};
    int[] earLeftY = {centerY - 12, centerY - 20, centerY - 12};
    g2d.fillPolygon(earLeftX, earLeftY, 3);

    int[] earRightX = {centerX + 2, centerX + 5, centerX + 8};
    int[] earRightY = {centerY - 12, centerY - 20, centerY - 12};
    g2d.fillPolygon(earRightX, earRightY, 3);

    // Apply wing rotation for left wing
    g2dWings.rotate(-wingRotation, centerX - 10, centerY);

    // Left Wing (bat wing shape with finger bones)
    Path2D.Double leftWing = new Path2D.Double();
    leftWing.moveTo(centerX - 10, centerY);
    leftWing.curveTo(centerX - 15, centerY - 8, centerX - 22, centerY - 6, centerX - 26, centerY - 2);
    leftWing.curveTo(centerX - 28, centerY + 2, centerX - 26, centerY + 6, centerX - 22, centerY + 8);
    leftWing.curveTo(centerX - 18, centerY + 6, centerX - 14, centerY + 4, centerX - 10, centerY + 8);
    leftWing.closePath();
    g2dWings.fill(leftWing);

    // Reset and apply rotation for right wing
    g2dWings.dispose();
    g2dWings = (Graphics2D) g2d.create();
    g2dWings.rotate(wingRotation, centerX + 10, centerY);

    // Right Wing
    Path2D.Double rightWing = new Path2D.Double();
    rightWing.moveTo(centerX + 10, centerY);
    rightWing.curveTo(centerX + 15, centerY - 8, centerX + 22, centerY - 6, centerX + 26, centerY - 2);
    rightWing.curveTo(centerX + 28, centerY + 2, centerX + 26, centerY + 6, centerX + 22, centerY + 8);
    rightWing.curveTo(centerX + 18, centerY + 6, centerX + 14, centerY + 4, centerX + 10, centerY + 8);
    rightWing.closePath();
    g2dWings.fill(rightWing);

    // Wing membrane details (darker shade)
    g2dWings.setColor(color.darker());
    g2dWings.setStroke(new BasicStroke(1.5f));
    // Right wing fingers
    g2dWings.drawLine(centerX + 10, centerY, centerX + 24, centerY - 4);
    g2dWings.drawLine(centerX + 10, centerY + 4, centerX + 20, centerY + 6);

    g2dWings.dispose();

    // Draw left wing fingers with rotation
    g2dWings = (Graphics2D) g2d.create();
    g2dWings.rotate(-wingRotation, centerX - 10, centerY);
    g2dWings.setColor(color.darker());
    g2dWings.setStroke(new BasicStroke(1.5f));
    g2dWings.drawLine(centerX - 10, centerY, centerX - 24, centerY - 4);
    g2dWings.drawLine(centerX - 10, centerY + 4, centerX - 20, centerY + 6);
    g2dWings.dispose();

    // Small feet/claws at bottom
    g2d.setColor(color);
    g2d.fillOval(centerX - 4, centerY + 10, 3, 4);
    g2d.fillOval(centerX + 1, centerY + 10, 3, 4);

    // Eyes (large, looking forward)
    g2d.setColor(Color.WHITE);
    g2d.fillOval(centerX - 6, centerY - 10, 5, 6);
    g2d.fillOval(centerX + 1, centerY - 10, 5, 6);

    // Pupils with eyeDirection applied (horizontal offset)
    int pupilOffsetX = (int) (eyeDirection * 1.5);
    g2d.setColor(Color.RED);
    g2d.fillOval(centerX - 4 + pupilOffsetX, centerY - 8, 2, 3);
    g2d.fillOval(centerX + 3 + pupilOffsetX, centerY - 8, 2, 3);

    // Tiny nose/snout
    g2d.setColor(color.darker());
    g2d.fillOval(centerX - 1, centerY - 5, 2, 2);
    }
}
