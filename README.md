# Map Editor

A map editor created with JavaFX. The editor is for another project I am working on, which is a retro throwback first person shooter. The shooter is pending a rewrite and is not yet available.

## Building

Requires JavaFX. Contains pom.xml for building with Maven.

## Current Features

- Map is divided into roughly 3m x 3m x 3m sectors, which can be connected ("adjoined") horizontally or vertically to create spaces of varying shapes and sizes
  - The ceilings and floors of a sector can be adjusted in 5 independent points (corners and center) to add further detail to level design
- Switch between three primary modes: Sector Mode, Surface Mode, Texture Mode
- In all three modes, the UI presents a top-down view of the current floor, with visual indicators of which sectors are adjoined, and where doors are
  - The view can be panned by clicking and dragging, and zoomed with the scroll wheel
  - Panning follows the mouse exactly for all levels of zoom
  - Depending on the mode, sectors or walls are selected by clicking them in the UI
  - Sectors are outlined in bright red. Adjoined sectors do not have borders between them. Grid lines can be toggled to see boundaries between adjoined sectors, or empty parts of the map 
- Bar at the bottom of the UI displays the mouse's position in map coordinates
- Bar also indicates if changes have been saved since last modification (saving not implemented yet)

### Sector Mode

- Sectors are selected in the GUI by clicking them in the display of the map
- Create a new sector in the grid by clicking an empty spot in the map and clicking "New Sector"
- The currently selected sector is highlighted in the GUI
- Sector Mode menu allows changing the properties of each wall of the sector. Buttons exist to toggle the currently selected wall, and multiple walls may be selected to apply settings to all of them at once
- Wall settings:
  - The texture can be changed with a file browser. The current texture is shown in a text box, and the texture can also be changed by manually entering the file name into the text box
  - The texture can be removed to create a transparent wall
  - The texture can be aligned so that its top is at the sector's ceiling, or its bottom is at the sector's floor (for sectors with different floor or ceiling heights)
  - The texture can be horizontally or vertically flipped
  - Adjoin the sector to adjacent sectors on the other side of the selected wall
  - Select if the wall between adjoined sectors blocks the player and enemies from moving or shooting through it
  - All settings for adjoined walls are automatically applied to the corresponding wall in the adjoined sector, except for textures
  - An additional "Adjoin Texture" can be applied to adjoined walls if the other sector has a floor or ceiling of a different height
- Simillar options for ceiling and floor textures and adjoining
  - Ceiling may be set as a sky, which will make the texture be rendered like a skybox
- Any sector can have a door
  - Options to orient the door, change its position in the sector, change the manner in which it opens and its speed
  - Each side can have an independent texture
  - Make a door require a key with a dialog that allows you to create a new key or choose an existing one
  - Toggle if the door may be opened by shooting it

### Surface Mode

- Sector selection works the same as Sector Mode
- Menu contains buttons for adjusting the height of each corner and the center of the floor and ceiling
- Changes are displayed by a 3D sub-scene which is updated automatically with any changes made
- The sector shown in the sub-scene may be rotated by clicking and dragging, allowing users to view it at any angle

### Texture Mode

- Convenient mode for applying textures to multiple walls at once in any number of sectors
- Unlike the other modes, clicking in the map display will select the closest wall to the cursor
- Holding shift allows multiple walls to be selected
- Holding shift and clicking a selected sector deselects it without resetting other selections
- Selected walls are highlighted in white, and walls that are on opposite sides of the boundary between adjacent sectors are differentiated by a normal vector being drawn from the wall towards the centre of the sector
- Both the main texture and adjoin textures may be changed in this mode, with either a file browser or manual file name entry

## Planned Features

- Saving and loading maps. This has not been implemented yet because of how rapidly the features have been changing, and every change to the data that is stored for the map would necessitate changing the code for saving and loading. It is also low priority because it will only be needed once the map editor is completed and I am ready to move on to working on the game again. Additionally, the process of developing how the game loads a level may lead to changes in the file format.
- Map mode for placing Actors (enemies, pickups, props, etc)
- Texture mapping in the Surface Mode 3D sub-scene and allowing ceiling and floor textures to be changed without switching to sector mode
