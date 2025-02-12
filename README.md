
## About this Plugin
This Plugin aims to add useful commands that are useable inside of datapacks using commandAPI.
Its mostly for people (like me) who like to make things with datapacks, but are annoyed by the limitations they have.

## Current Features
- **/motion @target <x> <y> <z>**
  Flings the player with x y z motion
- **/motionscore @target <xObjective> <yObjective> <zObjective>** 
  Flings the player with x y z motion, using the number from the targets scoreboard value for each given objective.
  
- **/slot @target(max 1 player) <number>**
  Sets the players slot (0-8)
  
- **/slot @target(max 1 player)**
  Returns the players Current Selected Slot
  
- **/tagremoveall @target <excludedtag1> <excludedtag2> ....**
  removes all tags apart from the ones exlcuded
  
## **!IMPORTANT!**
 - any custom commands can not be in tagged functions (usually the tick function and the load function), that will cause the function not to run properly. If you want to use it in such functions, make a new function and add the custom command into it, and call that function from the tagged function.
- Ignore any erros from functions that have a custom command inside of them.





