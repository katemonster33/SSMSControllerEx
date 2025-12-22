package ssms.controller.campaign;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.save.LoadGameDialog;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;

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
    SectorEntityToken focusedEntity;

    boolean gameCurrentlyPaused = false;
    CrosshairRenderer headingIndicator;
    ViewportAPI sectorViewport;
    UIPanelReflector campaignPanelReflector;
    UIPanelReflector coreUiPanelReflector;
    ControllerCrosshairRenderer hotbarIndicatorRenderer;
    int currentHotkeyGroup = 0, currentHotkey = 0;
    final float mouseMoveFactor = 400.f;

    int selectedHotkey, selectedHotkeyGroup;
    int selectedTab;

    boolean cameraControlMode = false;

    DirectionalUINavigator shipInfoNavigator;

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        indicators = new ArrayList<>();
        gameCurrentlyPaused = Global.getSector().isPaused();
        addButtonPressHandler("Pause", LogicalButtons.Start, (float advance) -> {
            Global.getSector().setPaused(!Global.getSector().isPaused());
            if(!Global.getSector().isPaused()) {
                setFreeLook(false);
                cameraControlMode = false;
            }
        });
        addButtonPressHandler("Open menu", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Open character sheet", LogicalButtons.Select, new KeySender(Keyboard.KEY_C, 'c'));
        addButtonChangeHandler("(hold) Speed up time", LogicalButtons.RightTrigger, (float advance, boolean btnState) -> {
            if (btnState) InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
            else InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
        });
        if(cameraControlMode || Global.getSector().isPaused()) {
            indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));
        } else {
            indicators.add(new Pair<>(Indicators.LeftStick, "Move ship"));
        }
        addButtonChangeHandler("Move to cursor", LogicalButtons.A, (float advance, boolean btnState) -> {
            if(btnState) InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            else InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            if(cameraControlMode) {
                setFreeLook(false);
                cameraControlMode = false;
                Global.getSector().setPaused(false);
                refreshIndicators();
            }
        });
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Go Slow"));
        focusedEntity = null;
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
            //directionalObjects.addAll(coreUiReflector.getChildPanels(3).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            directionalObjects.addAll(new UIPanelReflector(coreUiReflector.getAbilityBar()).getChildPanels().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            shipInfoNavigator = new DirectionalUINavigator(directionalObjects);
            addDigitalJoystickHandler("Navigate ship information / hotkeys", Joystick.DPad, shipInfoNavigator);
            addButtonPressHandler("Select active hotkey/button", LogicalButtons.A, (float advance) -> {
                if(shipInfoNavigator.getSelected() != null) {
                    InputShim.mouseMove((int) shipInfoNavigator.getSelected().getCenterX(), (int) shipInfoNavigator.getSelected().getCenterY());
                    InputShim.mouseDownUp((int) shipInfoNavigator.getSelected().getCenterX(), (int) shipInfoNavigator.getSelected().getCenterY(), InputEventMouseButton.LEFT);
                }
            });
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
            addButtonPressOrHoldHandler("Prev Hotkey", "Prev Hotkey Group", LogicalButtons.BumperLeft, new ButtonPressOrHoldHandler() {
                @Override
                public void performHoldAction(float advance) {
                    if(currentHotkeyGroup > 0) currentHotkeyGroup--;
                    else currentHotkeyGroup = 4;
                    switchToHotkeyGroup();
                }

                @Override
                public void performPressAction(float advance) {
                    if(currentHotkey > 0)  currentHotkey--;
                    else currentHotkey = 9;
                }
            });
            addButtonPressOrHoldHandler("Next Hotkey", "Next Hotkey Group", LogicalButtons.BumperRight, new ButtonPressOrHoldHandler() {
                @Override
                public void performHoldAction(float advance) {
                    if(currentHotkeyGroup < 4) currentHotkeyGroup++;
                    else currentHotkeyGroup = 0;
                    switchToHotkeyGroup();
                }

                @Override
                public void performPressAction(float advance) {
                    if(currentHotkey < 9) currentHotkey++;
                    else currentHotkey = 0;
                }
            });
            addButtonPressHandler("Use hotkey", LogicalButtons.Y, (float advance) -> InputShim.keyDownUp(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey)));
            mousePos.x = InputShim.getScaledScreenWidth() / 2.f;
            mousePos.y = InputShim.getScaledScreenHeight() / 2.f;
            InputShim.mouseMove((int)mousePos.x, (int)mousePos.y); // move the fake pointer to the center to keep the focus on the ship
        }
        addButtonPressHandler("Zoom out", LogicalButtons.RightStickDown, (float advance) -> InputShim.mouseWheel((int) mousePos.x, (int) mousePos.y, -5));
        addButtonPressHandler("Zoom in", LogicalButtons.RightStickUp, (float advance) -> InputShim.mouseWheel((int) mousePos.x, (int) mousePos.y, 5));
        addButtonPressHandler("Toggle camera mode", LogicalButtons.LeftStickButton, (float advance) -> {
            cameraControlMode = !cameraControlMode;
            setFreeLook(cameraControlMode);
            Global.getSector().setPaused(cameraControlMode);
            refreshIndicators();
        });
        return indicators;
    }

    public void setFreeLook(boolean value) {
        if(value != CampaignStateReflector.GetInstance().getIsFreeLookOn()) {
            InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
        }
    }

    @Override
    public Alignment getIndicatorsAlignment() {
        return Alignment.TR;
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

    boolean isCompatibleEntity(SectorEntityToken ent) {
        if(ent == Global.getSector().getPlayerFleet()) return false;
        if(CampaignFleetAPI.class.isAssignableFrom(ent.getClass())) {
            return true;
        }
        if(ent.getTags() == null || ent.getTags().isEmpty()) return false;
        for(var tag : ent.getTags()) {
            switch(tag) {
                case Entities.ABYSSAL_LIGHT, Entities.BASE_CONSTELLATION_LABEL, Entities.DEBRIS_FIELD_SHARED,
                     Entities.EXPLOSION, "orbital_junk" -> {
                    return false;
                }
            }
        }
        return true;
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
        if(InputShim.hasMouseControl() && (cameraControlMode || Global.getSector().isPaused() || focusedEntity != null)) {
            headingIndicator.setMousePos(mousePos.x, mousePos.y);
            headingIndicator.render();
        }
    }

    boolean setMousePosFromStick(Vector2f stickVal, float advance) {
        if(stickVal.length() < controller.joystickDeadzone) {
            return false;
        }
        mousePos.x += stickVal.x * mouseMoveFactor * advance;
        mousePos.y -= stickVal.y * mouseMoveFactor * advance;
        InputShim.mouseMove((int) mousePos.x, (int) mousePos.y);
        return true;
    }

    boolean movedShipLastFrame = false;
    boolean moveShipWithStick(Vector2f stickVal) {
        var pf = Global.getSector().getPlayerFleet();
        if(pf == null) {
            return false;
        }
        if ( stickVal.getX() == 0 && stickVal.getY() == 0) {
            if(movedShipLastFrame) {
                pf.setMoveDestinationOverride(pf.getLocation().getX(), pf.getLocation().getY());
                movedShipLastFrame = false;
            }
            return false;
        }
        var shipPos = new Vector2f(pf.getLocation());
        if(stickVal.getX() != 0) shipPos.setX(shipPos.getX() + (stickVal.getX() * 1000.f));
        if(stickVal.getY() != 0) shipPos.setY(shipPos.getY() + (stickVal.getY() * -1000.f));
        if(stickVal.lengthSquared() < 0.3f) {
            Global.getSector().getPlayerFleet().goSlowOneFrame();
        }
        pf.setMoveDestinationOverride(shipPos.getX(), shipPos.getY());
        if(!movedShipLastFrame) {
            InputShim.mouseMove(-9999, -9999);
            InputShim.mouseDownUp(-9999, -9999, InputEventMouseButton.LEFT);
            InputShim.mouseMove((int) InputShim.getScaledScreenWidth() / 2, (int) InputShim.getScaledScreenHeight() / 2);
        }
        movedShipLastFrame = true;
        return true;
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
        if(!gameCurrentlyPaused && !cameraControlMode) {
            moveShipWithStick(controller.getJoystick(Joystick.Left));
            focusedEntity = null;
            float closestEntityDist = Float.MAX_VALUE;
            var pf = Global.getSector().getPlayerFleet();
            Vector2f fleetPos = pf.getLocation();
            for (var ent : Global.getSector().getCurrentLocation().getAllEntities()) {
                if (Global.getSector().getViewport().isNearViewport(ent.getLocation(), 10.f) && isCompatibleEntity(ent)) {
                    float dist = new Vector2f(ent.getLocation().x - fleetPos.x, ent.getLocation().y - fleetPos.y).length();
                    dist -= ent.getRadius();
                    dist -= pf.getRadius();
                    if (dist < 50.f && dist < closestEntityDist) {
                        closestEntityDist = dist;
                        focusedEntity = ent;
                    }
                }
            }
            if (focusedEntity != null) {
                mousePos.setX(sectorViewport.convertWorldXtoScreenX(focusedEntity.getLocation().x));
                mousePos.setY(sectorViewport.convertWorldYtoScreenY(focusedEntity.getLocation().y));
                InputShim.mouseMove((int) mousePos.x, (int) mousePos.y);
            }
        } else {
            setMousePosFromStick(controller.getJoystick(Joystick.Left), advance);
        }
        if (controller.isButtonPressed(LogicalButtons.LeftTrigger)) {
            Global.getSector().getPlayerFleet().goSlowOneFrame();
        }
        if(shipInfoNavigator != null && gameCurrentlyPaused) {
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
            return openCoreUiTab(new CoreUIReflector(coreUI));
        }
        return false;
    }

    @Override
    public String getId() {
        return ID;
    }
}
