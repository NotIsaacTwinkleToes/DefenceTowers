# DefenceTowers Setup
1. Extract 'DefenceTowers.zip' to reveal 'DefenceTowers.jar' and 'Towers' folder.
2. Drag and drop DefenceTowers.jar into plugins folder.

3. **If** you want the example towers that I created, **Create** a new folder called 'DefenceTowers' and drop the 'Towers' folder provided inside.

4. DONE!

Upon plugin load, an 'Example Tower' will be created, if it doesn't exist, that will contain the most up-to-date default configuration options.

# Using DefenceTowers
## Creating a new tower

1. Create a new **YAML** file. (file extension: .yml). ex. 'New Tower.yml'

2. Copy and paste Example Tower.yml's content into your new tower.

3. Change options to your liking and save.

4. Restart server.
**Creating a tower file and not restarting may cause strange tower behavior**

Now doing /dt list will display your new tower.
## Editing a tower
 Editing towers dont require a server restart, instead, you can reload the tower's configuration with the command **/dt reload [tower name]**.
 
 **/dt reload** will reload all towers.
 
## Removing towers
 
### From World
Players with the correct permissions (defencetowers.bypassblacklist) or are blacklisted by a tower, can Shift + Right Click a tower to pick it up.
  only players in survival will force a tower to drop items.
  
### Removing Tower File
Removing a tower file requires a server restart for the existing towers to be removed. Doing so will send warnings to the console 'WARN [Defence Towers] Your Tower Name file does not exist, and could not be loaded!'

## Opening Tower
  Placing a tower automatically puts the players name into the towers blacklist.
  Clicking the tower will open the towers gui containing; radius display toggle, blacklist options, ammunition, and take controll button.
  
  **Players not within the blacklist or do not have permission; defencetowers.bypassblacklist. Cannot manipulate a tower.**
  
  ## Ammunition In Towers
  ### Adding
  - Player holding arrows clicks on tower. Player must be blacklisted by tower, or have permission 'defencetowers.addblockedarrows' to add arrows.
  - Shift clicking arrows in players inventory while in tower's gui.
  - Dropping arrows onto ammunition slot in tower's gui.
  ### Removing
  - Left Clicking ammunition slot will give a stack of arrows from the tower.
  - Shift + Left Clicking will move a stack of arrows to the players inventory from the tower.
  - Right Click will give 1 arrow to the player.

## Tower Blacklist

Clicking the blacklist item in the tower's gui will toggle blacklist editing for that tower.

### Adding A Player
  After enabling the item, chat the players name you want to blacklist.
### Removing A Player
  After enabling the item, chat 'remove playersName' to remove the player from the blacklist.
  
  chat 'cancel' or click any blacklist item in a tower's gui to disable editing a blacklist
