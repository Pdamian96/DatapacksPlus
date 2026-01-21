
## About this Plugin
This Plugin aims to add useful commands that are useable inside of datapacks using commandAPI.
Its mostly for people (like me) who like to make things with datapacks, but are annoyed by the limitations they have.

## Current Features
- **/motion @targets < x > < y > < z >**
  Flings the player with x y z motion
- **/motionscore @targets < xObjective > < yObjective > < zObjective >** 
  Flings the player with x y z motion, using the number from the targets scoreboard value for each given objective.
  
- **/relativemotion @targets <horizontal> <vertical> <forward> <ignore_vertical_rotation>**
  Flings the player with relative motion
- **/slot @target(max 1 player) < number >**
  Sets the players slot (0-8)
  
- **/slot @target(max 1 player)**
  Returns the players Current Selected Slot
  
- **/tagremoveall @target < excludedtag1 > < excludedtag2 > ....**
  removes all tags apart from the ones exlcuded

- **/heal @targets amount**
  Heals targets by amount
  
- **/food @targets amount**
  Sets food of targets to amount
  
- **/saturation @targets amount**
  Sets saturation of targets to amount
  
- **/enderchest @targets**
  Forces @targets to view their enderchest
  Incredibly useful for GUIs
  
- **/closeinventory**
  Forces Executing entity to close the inventory they currently have open
  
- **/repeat amount delay run <command>**
  Executes <command> amount of times, with a delay of delay
  
- **/serverboard**
Shows a personal scoreboard on the side of the screen. To configure, look in the config file for this plugin. 
Available replacements:
  {player} -> Returns Player Name
  {health} -> Returns Player Health
  {objective.NAME} -> Returns the Players value for scoreboard NAME

Commands for this:

- **/serverboard show BOARD_NAME true/false @targets**
  Shows/Hides a specific serverboard for @targets (if you hide a serverboard for anyone, none will be shown)
  
- **/serverboard update @targets**
  Updates the serverboard for @targets
  (VALUES IN THE SERVERBOARD DO NOT AUTOMATICALLY UPDATE, THIS COMMAND IS REQUIRED)
  
- **/serverboard configreload**
  Updates the config and everyones serverboard
  

  //Experimental and not fleshed out:
- **/eval**
  perform a caclulation 
  NOT DONE YET, STILL NEED TO ADD SCOREBOARD VARIABLE SUPPORT
  
- **/waypointset x y z @targets tag color icon_id**
  Creates a Waypoint at POS, showing only for TARGETS. The armorstand has the tag TAG, COLOR, and an ICON_ID for sprite purposes
  Waypoint ALWAYS has tag: datapacksplus_waypoint
  
- **/waypointsclear entity selector**
  Argument needs to be @e[], Waypoints are always armor stands, any other restriction may be used (like distance, etc)
  
# NEW TAGS
There are a number of tags you can give a player, to give them certain properties:

**plugin.block.interact**
- prevents player from doing right/left click on any block

**prevent.block.destroy**
- prevents player from destroying blocks

**prevent.block.placing**
- prevents player from placing blocks

**prevent.attack.all**
- prevents player from attacking any entity

**prevent.attack.player**
- prevents player from attacking any player

**prevent.attack.-player**
- prevents player from attacking JUST players
  (Not needed due to /gamerule pvp, but still here due to legacy reasons)
  
**prevent.attack.armorstand**
- prevents player from attacking armorstands

**prevent.attack.painting**
- prevents player from attacking paintings
- 
**prevent.attack.itemframe**
- prevents player from attacking itemframes

**prevent.drop**
- prevents player from dropping items

# Custom Scoreboards
**plugin.opengui**
- Players get added 1 when they enter a GUI
- 
**plugin.closegui**
- Players get added 1 when they leave a GUI


  
  
## **!IMPORTANT!**
 - any custom commands can not be in tagged functions (usually the tick function and the load function), that will cause the function not to run properly. If you want to use it in such functions, make a new function and add the custom command into it, and call that function from the tagged function.
- Custom Commands no longer throw errors in the console in datapacks!





