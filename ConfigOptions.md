# Tower configuration help

## Display
### Show: true/false
  
If tower should have a name above it
### Display: '&dExample Tower'

display supports color codes

## Bullets
### Per Shot: 1
How many arrows should be launched per shot cycle
### Gap: 0
Time in ticks between arrows in Per Shot. ex. if 'Per Shot' is 20, and 'Gap' is 1, it would 20 ticks to shoot all 20 arrows.
### Gravity: true/false
If arrow should be affected by gravity.
### Damage: 2.0
Amount of damage the arrow does
### Speed: 1.0
Speed of arrow, higher values means the arrow will go further, faster.
### Accuracy: 10.0
Spread of arrows. 1 is very accurate, 10 is a good default
### Piercing: 0
Piercing level the arrow has. AKA how many entities the arrow will go through
### Knockback: 0
Knockback level of an arrow
### Fire
    Fire: false
    Ticks: 20
Fire - If the arrow should be on fire

Ticks - How long an entity should be set on fire for.
### Bounce: 0
How many times an arrow will bounce

## Critical
### Chance: 0.3
30% chance of an arrow being marked as critical
### Multiplier: 2.0
Damage multiplier if an arrow is a critical arrow
### Accuracy: 0.5
A random number between negative accuracy and positive accuracy is added to damage multiplier.
## Shot
### Consumption: 1
How many arrows are consumed per shot
### Delay: 20
Time in ticks between shots
### Max Ammo: 0
Maximum ammo this type of tower can hold. 0 is infinite
## Range:
### Target: 10.0
How far away the tower can target
### Pickup: 1.0
How far away the tower can pickup arrows
## Silent:
### Tower: false
If true, tower wont 'click' when empty
### Arrow: false
If true, arrow wont make sounds

## Color:
    Enable: false
    Red: 0-255
    Green: 0-255
    Blue: 0-255
    
If true, arrows will have particles around them
## Blacklist:
    - LIST
    - OF
    - ENTITY TYPES

Find more entity types here https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html

## Whitelist:
    - LIST
    - OF
    - ENTITY TYPES

Whitelist is not required

## Tower:
### Turret:
      Turret:
    ==: org.bukkit.inventory.ItemStack
    v: 2975
    type: PLAYER_HEAD
    meta:
      ==: ItemMeta
      meta-type: SKULL
      skull-owner:
        ==: PlayerProfile
        uniqueId: 3a9d9d00-0000-005b-ffff-fffffc571d2a
        properties:
        - name: textures
          value: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MxMWEwZDkwYzM3ZWI2OTVjOGE1MjNkODYwMWFhMWM4NWZhZDA5YTRkMjIzMmQwNGVkMjNhYzkwZTQzMjVjMiJ9fX0=



This contains the turret part of a tower item stack. This should not be edited unless you know what you're doing. Use the command /dt turret <turret name> to set this ingame.
  
If you want to make your own texture for a tower; https://www.minecraftskins.com/skin-editor/
  
Download your skin and goto https://mineskin.org
  
Select the skin file you want to use and hit generate.
  
Copy the 'Texture Value'
  
replace the last line shown above with your new texture value
  
### Base:
This contains the base part of a tower item stack. This should not be edited unless you know what you're doing. Use the command /dt base <turret name> to set this ingame.
### Offset: 0.55
How far the turret item should be from the base item ingame.
### Potion Effects:
    EFFECT_NAME:
      Amplifier: 1
      Duration: 40
      Is Ambient: true
      Has Particles: true
      Has Icon: true
  
  Many effects can be added to each tower.
  
  Effect types can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html
  
  
  
  
  
  
  
  
  
  
  
  
