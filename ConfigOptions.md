# Tower configuration help

## Display
### Show: true/false
If tower should have a name above it.

### Display Name: '&dExample Tower'
Display supports color codes.

## Projectile
### Per Shot: 1
How many projectiles should be launched per shot cycle.

### Gap: 0
Time in ticks between projectiles in Per Shot. ex. if 'Per Shot' is 20, and 'Gap' is 1, it would 20 ticks to shoot all 20 projectiles.

### Gravity: true/false
If projectiles should be affected by gravity. Does not affect all projectile types.

### Damage: 2.0
Amount of damage the projectiles do. Projectiles may do more damage than expected.

### Speed: 1.0
Speed of projectiles, higher values means the projectiles will go further, faster.

### Accuracy: 10.0
Spread of projectiles. 1 is very accurate, 10 is a good default

### Visual Fire: true/false
If the projectile should be on fire. Affects some projectile types.

### Fire Ticks: 0
How many ticks the hit entity should be set on fire for.

### Bounces: 0
How many times a projectile will bounce.

### Type: ARROW
Projectile type. Available types; ARROW ITEM LARGE_FIREBALL SMALL_FIREBALL TRIDENT WITHER_SKULL

### Material: COAL_BLOCK
If the projectile type is ITEM. The item will look like this material.
Find more materials here; https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

### Piercing: 0
Piercing level an arrow has. AKA how many entities the arrow will go through. This option only works on arrow and trident projectile types.

### Knockback: 0
Knockback level of an arrow. This option only works on arrow and trident projectile types.

## Tail
        Toggle: true/false
        Red: 1-256
        Green: 1-256
        Blue: 1-256
        Size: 0.0 - 2.0

If the projectile should have colored particles follow it.

The size must be within 0 and 2.

## Critical
### Chance: 0.3
30% chance of an projectile being marked as critical

### Multiplier: 2.0
Damage multiplier if an projectile is a critical arrow

### Accuracy: 0.5
A random number between negative accuracy and positive accuracy is added to damage multiplier.

## Range:
### Target: 10
How far away the tower can target.

### Pickup: 1.0
How far away the tower can pickup ammunition.

## Silent:
### Tower: false
If true, tower wont 'click' when empty.

### Projectile: false
If true, projectiles won't make sounds.

## Tower
### Consumption: 1
How much ammunition is consumed per shot.

### Delay: 20
Time in ticks between shots.

### Max Ammo: 0
Maximum ammo this type of tower can hold. 0 is infinite.

### Offset: 0.55
How far the turret item should be from the base item in-game.

### Name Offset: 1.85
How far the name will be from the turret item.

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



This contains the turret part of a tower item stack. This should not be edited unless you know what you're doing. Use the command /dt turret <turret name> to set this in-game.

If you want to make your own texture for a tower; https://www.minecraftskins.com/skin-editor/

Download your skin and goto https://mineskin.org

Select the skin file you want to use and hit generate.

Copy the 'Texture Value'

replace the last line shown above with your new texture value

### Base:
    Base:
    ==: org.bukkit.inventory.ItemStack
    v: 2975
    type: RED_GLAZED_TERRACOTTA
This contains the base part of a tower item stack. This should not be edited unless you know what you're doing. Use the command /dt base <turret name> to set this in-game.

### Ammunition:
    Ammunition Item:
    ==: org.bukkit.inventory.ItemStack
    v: 2975
    type: POPPY
This contains the ammunition item information. This should not be edited unless you know what you're doing. Use the command /dt ammunition to set this in-game.

## Blacklist:
    - LIST
    - OF
    - ENTITY TYPES

## Whitelist:
    - LIST
    - OF
    - ENTITY TYPES

Find more entity types here https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html

### Potion Effects:
    EFFECT_NAME:
      Amplifier: 1
      Duration: 40
      Is Ambient: true
      Has Particles: true
      Has Icon: true
  
  Many effects can be added to each tower.
  
  Effect types can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html
  
# Full Example:
    Display:
        Show: true # Should the tower have a visible name.
        Display Name: '&dExample Tower'
    Projectile:
        Per Shot: 1 # Projectiles per shot cycle
        Gap: 0 # Time in ticks between each projectile in per shot.
        Gravity: true # If projectile is affected by gravity. Does not effect all projectile types.
        Damage: 2.0 # Projectile damage. Damage is not exact
        Speed: 1.0 # Speed of projectile. Higher means the projectile will go further, faster.
        Accuracy: 10.0 # 1 is very accuracy, 10 is a good defalt
        Visual Fire: false # If the projectile should be on fire. Does not effect all projectile types.
        Fire Ticks: 0 # How long in ticks a hit entity should be set on fire.
        Bounces: 0 # How many times a projectile can bounce. Does not effect all projectile types.
        Type: ARROW # Available projectile types; ARROW ITEM LARGE_FIREBALL SMALL_FIREBALL TRIDENT WITHER_SKULL
        Material: COAL_BLOCK # If type is ITEM, this material will be displayed.
        Piercing: 0 # Only affects ARROW and TRIDENT types
        Knockback: 0 # Only affects ARROW and TRIDENT types
        Tail:
            Toggle: false # If projectiles should have tails
            Red: 256 # Values must be between 1 and 256
            Green: 256
            Blue: 256
            Size: 1.0 # Size must not exceed 2
    Critical:
        Chance: 0.3 # Chance for a critical. 0.3 is 30%.
        Multiplier: 2.0 # Projectile damage is multiplied by this number.
        Accuracy: 0.5 # A random value between -accuracy and +accuracy is chosen and added to multiplier before calculating damage
    Range:
        Target: 10.0 # How far the tower can target
        Pickup Ammunition: 1.0 # How far the tower can pickup ammunition
    Silent:
        Tower: false # If tower makes sounds
        Projectile: false # If projecties make sounds
    Tower:
        Consumption: 1 # How much ammunition is used up per shot cycle
        Delay: 20 # Time in ticks between shots
        Max Ammo: 0 # Maximum ammo that can be stored in this tower. 0 = infinite
        Offset: 0.55 # How far the tower turret will be from the tower base
        Ammunition Item: # DO NOT EDIT
            ==: org.bukkit.inventory.ItemStack
            v: 2975
            type: ARROW
        Turret: # DO NOT EDIT
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
        Base: # DO NOT EDIT
            ==: org.bukkit.inventory.ItemStack
            v: 2975
            type: BEDROCK
    Blacklist: # Entity types the tower will not target
        - AREA_EFFECT_CLOUD
        - ARMOR_STAND
        - ARROW
        - DRAGON_FIREBALL
        - DROPPED_ITEM
        - EGG
        - ENDER_CRYSTAL
        - ENDER_PEARL
        - ENDER_SIGNAL
        - EVOKER_FANGS
        - EXPERIENCE_ORB
        - FALLING_BLOCK
        - FIREBALL
        - SMALL_FIREBALL
        - FIREWORK
        - FISHING_HOOK
        - GLOW_ITEM_FRAME
        - ITEM_FRAME
        - LEASH_HITCH
        - LIGHTNING
        - LLAMA_SPIT
        - MARKER
        - MINECART
        - MINECART_CHEST
        - MINECART_COMMAND
        - MINECART_FURNACE
        - MINECART_HOPPER
        - MINECART_MOB_SPAWNER
        - MINECART_TNT
        - PAINTING
        - PRIMED_TNT
        - SNOWBALL
        - SHULKER_BULLET
        - SPECTRAL_ARROW
        - SPLASH_POTION
        - THROWN_EXP_BOTTLE
        - TRIDENT
        - UNKNOWN
        - WITHER_SKULL
    Whitelist: [] # Entity types the tower will only target
    Potion Effects: # Potion effects
        SLOW:
            Amplifier: 1
            Duration: 40
            Is Ambient: true
            Has Particles: true 
            Has Icon: true

  
  
  
  
  
  
  
  
  
