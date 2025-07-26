# SSMSControllerEx

## Dependencies

LunaLib - https://github.com/Lukas22041/LunaLib
LazyLib - https://github.com/LazyWizard/lazylib

## Features

* Provides controller support for Starsector
* Allows controlling the player ship with the left joystick, and navigating menus in UIs with the directional pad. Additionally, map elements can be navigated with a combination of the left joystick (crosshair), right joystick (camera), triggers (zoom) and A/Cross button (Selection of element pointed at with crosshair)

## Installation (WINDOWS ONLY)
After unzipping the mod contents to mods\SSMSControllerEx , from the SSMSControllerEx\native\windows folder, copy SSMSControllerExNative.dll to the folder Starsector\starsector-core\native\windows . If your controller worked in the previous version of SSMSControllerEx you shouldn't notice any difference in behavior. This step should ensure that just about any controller with a layout similar to an X-Box controller should work with the mod. To be clear, this is ONLY necessary on windows.

## Installation (STEAM DECK ONLY)
After unzipping the mod contents to mods/SSMSControllerEx, replace the jinput.jar under the starsector folder with the one here https://github.com/katemonster33/SSMSControllerEx/tree/master/dependencies/jinput.jar and replace libjinput-linux64.so under starsector/native/linux with this one https://github.com/katemonster33/SSMSControllerEx/blob/master/dependencies/native/linux/libjinput-linux64.so the version shipped with Starsector has some bugs with interpreting the buttons/axes from the Steam Deck controller.

## FAQ

 - Can this be added to and removed from an ongoing save? **Yes.**
 - Controller stopped working Mid-game?  **The controller must be plugged in when the game starts and can not be disconnected throughout the game.**
 - Can't see my controller? **The current mod attempts to auto-detect the controller using the list of supported controllers in data/config/settings.json . Eventually a better strategy will have to be devised for auto-detecting.**
 - Will you add support for my controller? **Most likely! If your controller has 2 joysticks, A/B/X/Y (or equivalent 4 side buttons) and L1/L2/R1/R2 we should be able to get it to work!**
 - Can I still use keyboard and mouse? **Yes, the controller works alongside them. Joysticks with few buttons will need the keyboard. In order to use the mouse the controller steering can be switched off during combat.**

## Controller Setup

Currently, the mod relies on auto-detection of any connected controller via the settings file in data/config/settings.json . Editing this file directly is currently the best way to add support for a controller that the mod doesn't detect out-of-the-box.

There's a controller auto-mapper for the title screen that should allow you to map your controller using the on-screen prompts. it doesnt save the mapping between game loads though, sorry :(

## Title

Title screen is mostly supported minus the Codex and Settings

### Default Controls

Use the directional pad to move the hovered selection (current selection marked by the 4 arrows as pictured below) and press A, or X to select the current element

### Title Screen Controls

![Title Screen](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/TitleScreenInputs.png)

## Campaign View

### Sector View Controls

Since the Sector View (main campaign view) isn't completely self-explanatory, here is a short video and explanation

![Sector View](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/campaignview.gif)

Pressing the joystick in a direction causes the crosshair to render in the direction from the player's fleet that the joystick is pressed. The further it is pressed, the further out from the player's fleet the reticle appears. The reticle functions as the mouse pointer, but without preventing the use of the user's mouse. If you wish to use your mouse instead of the joystick, simply put the joystick down, and move the mouse over the screen, and note how the crosshair disappears in response.

### Sector View Controls (Paused)

Since there is a periodic need to mouse-over things like the supply consumption rate or other elements from the sector view, the mod provides extra features while the game is paused.
![Sector View Paused](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/CampaignViewInputsPaused.png)
Note how the selection reticle only shows while the game is paused, and functions as the mouse pointer (in terms of mouse-hover showing the tooltips) only while paused. Pay close attention to the binding for the Y/Triangle button here - holding the button has different behavior than press-and-releasing it. This is a common feature in SSMSControllerEx due to the sheer number of controls needed on certain screens.

### Campaign Tab Controls

Much of the behavior within campaign tabs is commonized. The bumpers will always navigate between the tabs. Each tab provides directional pad input for moving the highlighted element, and the A/Cross button to mouse-click the highlighted element.
The character sheet, for example:
![Character Sheet](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/CharacterScreenTab.png)

### Map Tab
![Map Tab](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/MapInputs.png)

The inputs for a map are common between the Map Tab, the Intel Tab, the Planet view (the single star system view that shows a map) and the War-Room view in combat.

The left joystick controls the cursor, the right joystick moves the map itself. The A/Cross button acts like a left-click, but note that pressing the button down only does the mouse down, and the button-up is only sent when the controller button is released. This is how the user is able to lay in a course to a given target, by holding the A/Cross button, moving the cursor over the intended on-screen button, and then releasing the button.

Moving on, the triggers zoom in and out. Note that, on other UI, the triggers move the selected sub-tab, but on the Map Tab itself, these only zoom the map. Changing the selected sub-tab on the Map Tab involves navigating to it with the directional pad and pressing A/Cross.

### Intel Screen
Note that on the Intel screen there is a map, as well as elements selectable with the directional pad. Since the map controls involve the triggers for zooming, and the Intel tab controls also involve using the triggers to change the selected sub-tab within the Intel tab, the map controls only become active when the map itself is navigated to using the directional pad.

### Crew/Cargo Tab, Planetary Market View, etc
This screen can be quite busy if viewing a planet, but only the directional pad, the A/Cross button, and B/Circle button are needed for this screen. Note that, for convenience, when using A/Cross to pick up a partial or full stack of cargo, the items are automatically moved to an empty slot in the opposite cargo grid for placement. The usage of the directional controls to change where the stack is placed can break this easily, so it's highly recommended to only press A/Cross to confirm, or B/Circle to cancel.

## Combat View

### Default Controls

Weapons are aimed automatically and individually, even if the player fires them as a linked group. Omni shields will be raised in the direction of the broadside(by default front) and are controlled by the AI afterwards. Activating autopilot disables controller steering.

#### Directional Steering

![Steering Directional](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/BattleSteeringInputs.png)

In this mode the left stick controls the facing of the ship. Triggers control forward and backward acceleration and bumpers allow strafing.

#### Orbital Steering (Requires Target)

![Steering Orbital](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/OrbitalSteering.png)

The ship will face the target with the selected broadside while accelerating and strafing to reach the desired position automatically. The desired position is marked with a green pentagon. The left stick controls the relative position of the ship to the target. The triggers change the desired distance to the target.

#### Targeting

![Battle Targeting](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/BattleTargeting.png)

While targeting the combat is paused and the available targets can be cycled through using the bumpers. The targets are ordered by distance to the players ship. After selecting or clearing the target the battle resumes immediately.

#### Battle Menu

![Battle Menu](https://raw.githubusercontent.com/katemonster33/SSMSControllerEx/refs/heads/master/images/BattleMenu.png)

Combat is paused when the menu is open. The available menu entries can be cycled through using the bumpers. After selecting an entry the battle resumes immediately.

### Credits

Many thanks to razuhl (https://github.com/razuhl) for his great work on the original SSMSController mod, most of the work done to this repo uses his work on the UI and controller handling as a base, so far I have simply re-written the original mod's usage of reflection and deleted the in-game settings dialogs.
