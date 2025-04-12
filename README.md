# SSMSControllerEx

## Dependencies

LunaLib - https://github.com/Lukas22041/LunaLib
LazyLib - https://github.com/LazyWizard/lazylib

## Features

* Provides controller support for combat controls.
* Orbiting mode that allows circling targets. Supports broadsides.

## FAQ

 - Can this be added to and removed from an ongoing save? **Yes.**
 - Controller stopped working Mid-game?  **The controller must be plugged in when the game starts and can not be disconnected throughout the game.**
 - Can't see my controller? **The current mod attempts to auto-detect the controller using the list of supported controllers in data/config/settings.json . Eventually a better strategy will have to be devised for auto-detecting.**
 - Will you add support for my controller? **Most likely! If your controller has 2 joysticks, A/B/X/Y (or equivalent 4 side buttons) and L1/L2/R1/R2 we should be able to get it to work!**
 - Can I still use keyboard and mouse? **Yes, the controller works alongside them. Joysticks with few buttons will need the keyboard. In order to use the mouse the controller steering can be switched off during combat.**

## Controller Setup

Currently, the mod relies on auto-detection of any connected controller via the settings file in data/config/settings.json . Editing this file directly is currently the best way to add support for a controller that the mod doesn't detect out-of-the-box.

There's a controller auto-mapper for the title screen that should allow you to map your controller using the on-screen prompts. it doesnt save the mapping between game loads though, sorry :(

Images can be installed by any mod and are searched in the sub directory "graphics/indicators/" but must be added to data/config/settings.json. They must be PNG files with the ending ".png" and will be displayed in 
25x25 dimension. The majority of default images come in a resolution of 100x100 and are provided by Nicolae Berbece through his [FREE Keyboard and controllers prompts pack](https://opengameart.org/content/free-keyboard-and-controllers-prompts-pack), the positional stick images are custom derivatives.

### Finding controller buttons and axis

Under windows going to the settings and typing "controllers" will show the option "Setup USB Game Controllers". Click that, select the controller and choose "properties". 
There a "Test" tab is available showing the interactive button mappings and axis names. If the lowest button is listed as 1 the number must be reduced by one when configuring it.
In addition the starsector log file will print out controller information like this.

Found controller: Controller (XBOX 360 For Windows)
with axis: Y Axis
with axis: X Axis
with axis: Y Rotation
with axis: X Rotation
with axis: Z Axis
with button: Button 0
with button: Button 1
...
with button: Button 9

However which axis/button is which must be figured out manually or looked up through google. The framework in use is "DirectInput".

## Default Controls

Weapons are aimed automatically and individually, even if the player fires them as a linked group. Omni shields will be raised in the direction of the broadside(by default front) and are controlled by the AI afterwards. Activating autopilot disables controller steering.

### Directional Steering

![Steering Directional](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Steering_Directional.png)

In this mode the left stick controls the facing of the ship. Triggers control forward and backward acceleration and bumpers allow strafing.

### Orbital Steering (Requires Target)

![Steering Orbital](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Steering_Orbital.png)

The ship will face the target with the selected broadside while accelerating and strafing to reach the desired position automatically. The desired position is marked with a green pentagon. The left stick controls the relative position of the ship to the target. The triggers change the desired distance to the target.

### Targeting

![Battle Targeting](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Targeting.png)

While targeting the combat is paused and the available targets can be cycled through using the bumpers. The targets are ordered by distance to the players ship. After selecting or clearing the target the battle resumes immediately.

### Battle Menu

![Battle Menu](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Menu.png)

Combat is paused when the menu is open. The available menu entries can be cycled through using the bumpers. After selecting an entry the battle resumes immediately.

## Custom Controls

Besides mapping the input it is also possible to add custom steering modes via modding. The mods mod_info.json should contain an entry `"loadAfter":["SSMSController"]`. Then adding this mods jar as a library dependency to the java project gives access to the required classes. The javadoc next to the jar should be linked so that the documentation is available when coding the necessary class. 

The javadoc for the interface "ssms.controller.steering.SteeringController" contains all the documentation on what the class must do and how it can be put into service. In addition the default steering mode classes "ssms.controller.steering.SteeringController_FreeFlight" and "ssms.controller.steering.SteeringController_OrbitTarget" can be viewed in the source files to get an idea on how to query the controller or steer the ship.

###Credits

Many thanks to razuhl (https://github.com/razuhl) for his great work on the original SSMSController mod, most of the work done to this repo uses his work on the UI and controller handling as a base, so far I have simply re-written the original mod's usage of reflection and deleted the in-game settings dialogs.
