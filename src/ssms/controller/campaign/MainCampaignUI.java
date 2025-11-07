package ssms.controller.campaign;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.save.LoadGameDialog;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.InputScreenBase;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.enums.Indicators;
import ssms.controller.combat.BattleScope;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DigitalJoystickHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;
import ssms.controller.generic.LoadGameUI;

public class MainCampaignUI extends InputScreenBase {
    public static final String ID = "MainCampaign";
    Vector2f mousePos = new Vector2f(-1.f, -1.f);

    boolean leftStickActive = false, rightStickActive = false;
    boolean gameCurrentlyPaused = false;
    CrosshairRenderer headingIndicator;
    ViewportAPI sectorViewport;
    UIPanelReflector campaignPanelReflector;
    UIPanelReflector coreUiPanelReflector;
    ControllerCrosshairRenderer hotbarIndicatorRenderer;
    int currentHotkeyGroup = 0, currentHotkey = 0;

    int selectedHotkey, selectedHotkeyGroup;
    int selectedTab;

    DirectionalUINavigator shipInfoNavigator;

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        indicators = new ArrayList<>();
        gameCurrentlyPaused = Global.getSector().isPaused();
        addAnalogJoystickHandler("Set ship heading", Joystick.Left, this::handleShipMovement);
        indicators.add(new Pair<>(Indicators.LeftStickButton, "(hold) Go Slow"));
        addAnalogJoystickHandler("Free look", Joystick.Right, this::handleCameraMovement);
        addButtonChangeHandler("Move to cursor", LogicalButtons.A, (float advance, boolean btnState) -> {
            if(btnState) InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            else InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
        });
        if(gameCurrentlyPaused) {
            List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>();
            var coreUiReflector = new CoreUIReflector(CampaignStateReflector.GetInstance().getCoreUI());
            //getPanelNavigatables(coreUiReflector, directionalObjects, new ArrayList<>());
            UIPanelReflector logistics = new UIPanelReflector(coreUiReflector.getLogistics());
            directionalObjects.addAll(logistics.getChildPanels().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            CoursePanelReflector coursePanelReflector = new CoursePanelReflector(coreUiReflector.getCourse());

            if(coursePanelReflector.getInner().getState() == Fader.State.IN) {
                directionalObjects.addAll(coursePanelReflector.getChildButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            }
            directionalObjects.addAll(coreUiReflector.getChildPanels(3).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            shipInfoNavigator = new DirectionalUINavigator(directionalObjects);
            addDigitalJoystickHandler("Navigate ship information / hotkeys", Joystick.DPad, shipInfoNavigator);
            addButtonPressOrHoldHandler("More Info", "Open Codex", LogicalButtons.Y, new ButtonPressOrHoldHandler() {
                @Override
                public void performHoldAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
                }

                @Override
                public void performPressAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F1, '\0');
                }
            });
        } else {
            addDigitalJoystickHandler("Navigate hotkeys", Joystick.DPad, new DigitalJoystickHandler() {
                @Override
                public void performUpAction(float advance) {
                    if(currentHotkeyGroup > 0) currentHotkeyGroup--;
                    switchToHotkeyGroup();
                }

                @Override
                public void performDownAction(float advance) {
                    if(currentHotkeyGroup < 4) currentHotkeyGroup++;
                    switchToHotkeyGroup();
                }

                @Override
                public void performLeftAction(float advance) {
                    if(currentHotkey > 0)  currentHotkey--;
                }

                @Override
                public void performRightAction(float advance) {
                    if(currentHotkey < 9) currentHotkey++;
                }
            });
            addButtonPressHandler("Use hotkey", LogicalButtons.Y, (float advance) -> InputShim.keyDownUp(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey)));
        }
        addButtonPressHandler("Zoom out", LogicalButtons.LeftTrigger, (float advance) -> InputShim.mouseWheel((int) mousePos.x, (int) mousePos.y, -5));
        addButtonPressHandler("Zoom in", LogicalButtons.RightTrigger, (float advance) -> InputShim.mouseWheel((int) mousePos.x, (int) mousePos.y, 5));

        addButtonPressHandler("Pause", LogicalButtons.Start, (float advance) -> Global.getSector().setPaused(!Global.getSector().isPaused()));
        addButtonPressHandler("Open menu", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Open character sheet", LogicalButtons.Select, new KeySender(Keyboard.KEY_C, 'c'));
        addButtonChangeHandler("(hold) Speed up time", LogicalButtons.BumperRight, (float advance, boolean btnState) -> {
            if(btnState) InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
            else InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
        });
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        selectedHotkey = selectedHotkeyGroup = selectedTab = -1;
        currentHotkeyGroup = currentHotkey = 0;
        hotbarIndicatorRenderer = new ControllerCrosshairRenderer(58);
        headingIndicator = new CrosshairRenderer();
        headingIndicator.setSize(32, 32);
        sectorViewport = Global.getSector().getViewport();
        indicators = null;
        campaignPanelReflector = new UIPanelReflector(getPanelForIndicators());
        coreUiPanelReflector = new UIPanelReflector((UIPanelAPI) CampaignStateReflector.GetInstance().getCoreUI());
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        // render hotbar indicator
        if(gameCurrentlyPaused) {
            shipInfoNavigator.render();
        } else {
            int x = 277 + currentHotkey * 59 + 29, y = 103 - 29;
            hotbarIndicatorRenderer.AttemptRender(viewport, x, y);
        }

        if(InputShim.hasMouseControl()) {
            headingIndicator.setMousePos(mousePos.x, mousePos.y);
            headingIndicator.render();
        }
    }

    boolean setMousePosFromStick(Vector2f stickVal) {
        var pf = Global.getSector().getPlayerFleet();
        if(pf == null) {
            return false;
        }
        if ( stickVal.getX() == 0 && stickVal.getY() == 0) {
            mousePos.x = sectorViewport.convertWorldXtoScreenX(pf.getLocation().getX());
            mousePos.y = sectorViewport.convertWorldYtoScreenY(pf.getLocation().getY());
            InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
            return false;
        }
        float minX = sectorViewport.getLLX(), minY = sectorViewport.getLLY();
        float maxX = sectorViewport.getVisibleWidth() + minX, maxY = sectorViewport.getVisibleHeight() + minY;
        var shipPos = pf.getLocation();
        float xpos, ypos;
        if(stickVal.getX() < 0) {
            xpos = shipPos.x + stickVal.getX() * (shipPos.x - minX);
        } else {
            xpos = shipPos.x + stickVal.getX() * (maxX - shipPos.x);
        }
        mousePos.x = sectorViewport.convertWorldXtoScreenX(xpos);
        if(stickVal.getY() < 0) {
            ypos = shipPos.y - stickVal.getY() * (shipPos.y - minY);
        } else {
            ypos = shipPos.y - stickVal.getY() * (maxY - shipPos.y);
        }
        mousePos.y = sectorViewport.convertWorldYtoScreenY(ypos);
        InputShim.mouseMove((int)mousePos.x, (int)mousePos.y);
        return true;
    }

    void handleShipMovement(float advance, Vector2f leftStick) {
        if(rightStickActive) return;

        leftStickActive = setMousePosFromStick(leftStick);
    }

    void handleCameraMovement(float advance, Vector2f rightStick) {
        boolean oldRightStickActive = rightStickActive;
        rightStickActive = rightStick.getX() != 0 || rightStick.getY() != 0;
        if(leftStickActive) return;
        if(oldRightStickActive != rightStickActive) InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
        setMousePosFromStick(rightStick);
    }

    @Override
    public void preInput(float advance) {
        if(Global.getCurrentState() == GameState.COMBAT) {
            InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            return;
        }
        if(Global.getSector().getCampaignUI().isShowingDialog()) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
                InputScreenManager.getInstance().transitionToScreen(DialogUI.ID);
                return;
            } else if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null) {
                if (openScreenForCoreTab()) return;
            } else {
                for(var child : campaignPanelReflector.getChildPanels()) {
                    if(child instanceof LoadGameDialog) {
                        InputScreenManager.getInstance().transitionToScreen(LoadGameUI.ID);
                        return;
                    }
                }
            }
        }
        if (isMessageBoxShown(coreUiPanelReflector)) return;
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        if(Global.getSector().isPaused() != gameCurrentlyPaused) {
            refreshIndicators();
            return;
        }
        if(controller.isButtonPressed(LogicalButtons.LeftStickButton)) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            playerFleet.goSlowOneFrame();
        }
        if(shipInfoNavigator != null) {
            shipInfoNavigator.advance(advance);
        }
    }

    private void switchToHotkeyGroup() {
        InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
        InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
        InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
        InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
    }

    boolean openScreenForCoreTab() {
        var coreUI = CampaignStateReflector.GetInstance().getCoreUI();
        if (coreUI != null) {
            return openCoreUiTab(coreUI);
        }
        return false;
    }

    @Override
    public String getId() {
        return ID;
    }
}
