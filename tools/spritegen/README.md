# Sprite Generator

A simple Java tool to generate sprite textures for Godot Sprite2D using basic shapes and colors.

## Features

- Generates a sprite sheet with 10 different sprite types
- Each sprite type comes in 8 different colors
- 64x64 pixel sprites (matching tileset size)
- Anti-aliased rendering for smooth graphics
- PNG output with transparency support

## Sprite Types

1. **Blob** - Simple circular creature with eyes
2. **Robot** - Rectangular character with antenna
3. **Ghost** - Classic wavy-bottom ghost
4. **Alien** - Head with large eyes
5. **Gem** - Diamond/crystal shape with facets
6. **Coin** - Circular collectible with design
7. **Player** - Simple player character
8. **Enemy** - Menacing spiked character
9. **NPC** - Friendly character with smile
10. **Powerup** - Star shape with glow effect

## Colors

- Red, Blue, Green, Yellow, Magenta, Cyan, Orange, Purple

## Usage

Build and run the generator:

```bash
mvn clean compile exec:java
```

This will generate `spritesheet.png` in the project root directory.

## Using in Godot

1. Copy the generated `spritesheet.png` to your Godot project
2. Create a Sprite2D node in your scene
3. Set the texture to the sprite sheet
4. Configure the Region settings:
   - Enable Region
   - Set Region Rect to the sprite you want (e.g., x:0, y:0, w:64, h:64 for the first sprite)
5. Each sprite is 64x64 pixels, arranged in a grid (10 columns x 8 rows)

## Customization

Edit `src/main/java/com/robmayhew/App.java` to:
- Add new sprite types
- Modify existing sprite designs
- Change colors
- Adjust sprite size (change `SPRITE_SIZE` constant)
- Add animation frames

## Requirements

- Java 17 or higher
- Maven 3.x
