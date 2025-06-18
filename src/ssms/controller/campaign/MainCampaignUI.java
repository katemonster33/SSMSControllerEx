package ssms.controller.campaign;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.InputScreenBase;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.enums.Indicators;
import ssms.controller.combat.BattleScope;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DigitalJoystickHandler;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

public class MainCampaignUI extends InputScreenBase {
    public static final String ID = "MainCampaign";
    Vector2f lastHeading = null;
    Vector2f mousePos = new Vector2f(-1.f, -1.f);

    CrosshairRenderer headingIndicator;
    ControllerCrosshairRenderer hotbarIndicatorRenderer;
    int currentHotkeyGroup = 0, currentHotkey = 0;
    int lastFrameNumChildren = -1;

    int selectedHotkey, selectedHotkeyGroup;
    int selectedTab;

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.A, "Move to cursor"));
        addDigitalJoystickHandler("Navigate hotkeys", Joystick.DPad, new DigitalJoystickHandler() {
            @Override
            public void performUpAction(float advance) {
                if(currentHotkeyGroup < 4) currentHotkeyGroup++;
                switchToHotkeyGroup();
            }

            @Override
            public void performDownAction(float advance) {
                if(currentHotkeyGroup > 0) currentHotkeyGroup--;
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
        indicators.add(new Pair<>(Indicators.B, "Use/assign hotkey"));

        addButtonPressHandler("Pause", LogicalButtons.Start, (float advance) -> Global.getSector().setPaused(!Global.getSector().isPaused()));
        addButtonPressHandler("Open menu", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Open character sheet", LogicalButtons.Select, new KeySender(Keyboard.KEY_C, 'c'));
        indicators.add(new Pair<>(Indicators.LeftStick, "Set ship heading"));
        indicators.add(new Pair<>(Indicators.RightStick, "Free look"));
        addButtonChangeHandler("(hold) Speed up time", LogicalButtons.BumperRight, (float advance, boolean btnState) -> {
            if(btnState) {
                InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
            } else {
                InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
            }
        });
        indicators.add(new Pair<>(Indicators.RightTrigger, "(hold) Go slow"));
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        selectedHotkey = selectedHotkeyGroup = selectedTab = -1;
        currentHotkeyGroup = currentHotkey = 0;
        hotbarIndicatorRenderer = new ControllerCrosshairRenderer(58);
        lastFrameNumChildren = UIPanelReflector.getChildItems(getPanelForIndicators()).size();
        headingIndicator = new CrosshairRenderer();
        ControllerCrosshairRenderer.getControllerRenderer().disable();
    }

    @Override
    public void deactivate() {
        ControllerCrosshairRenderer.getControllerRenderer().enable();
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        // render hotbar indicator
        int x = 277 + currentHotkey * 59 + 29, y = 103 - 29;
        hotbarIndicatorRenderer.AttemptRender(viewport, x, y);

        var pf = Global.getSector().getPlayerFleet();
        if(pf == null) {
            return;
        }
        ReadableVector2f desiredHeading = controller.getJoystick(Joystick.Left);
        if ( desiredHeading.getX() == 0 && desiredHeading.getY() == 0) {
            if(lastHeading != null && (desiredHeading.getX() != lastHeading.getX() || desiredHeading.getY() != lastHeading.getY())) {
                mousePos.x = pf.getLocation().getX();
                mousePos.y = pf.getLocation().getY();
                InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
                lastHeading = null;
            }
            return;
        }
        if(lastHeading == null) {
            lastHeading = new Vector2f(desiredHeading.getX(), desiredHeading.getY());
        } else {
            lastHeading.set(desiredHeading.getX(), desiredHeading.getY());
        }
        float minX = viewport.getLLX(), minY = viewport.getLLY();
        float maxX = viewport.getVisibleWidth() + minX, maxY = viewport.getVisibleHeight() + minY;
        var shipPos = pf.getLocation();
        float xpos, ypos;
        if(desiredHeading.getX() < 0) {
            xpos = shipPos.x + desiredHeading.getX() * (shipPos.x - minX);
        } else {
            xpos = shipPos.x + desiredHeading.getX() * (maxX - shipPos.x);
        }
        mousePos.x = viewport.convertWorldXtoScreenX(xpos);
        if(desiredHeading.getY() < 0) {
            ypos = shipPos.y - desiredHeading.getY() * (shipPos.y - minY);
        } else {
            ypos = shipPos.y - desiredHeading.getY() * (maxY - shipPos.y);
        }
        mousePos.y = viewport.convertWorldYtoScreenY(ypos);
        InputShim.mouseMove((int)mousePos.x, (int)mousePos.y);
        headingIndicator.setMousePos(mousePos.x, mousePos.y);
        headingIndicator.render();
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
            }
        }
        if (isMessageBoxShown()) return;

        if(mousePos.x != -1.f && mousePos.y != -1.f) {
            if (controller.getButtonEvent(LogicalButtons.A) == 1) {
                InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            } else if (controller.getButtonEvent(LogicalButtons.A) == -1) {
                InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            }
        }
        if(controller.getButtonEvent(LogicalButtons.LeftStickButton) == 1) {
            if(mousePos.x == -1.f || mousePos.y == -1.f) {
                if(Global.getSector().getPlayerFleet() != null) {
                    var shipLoc = Global.getSector().getPlayerFleet().getLocation();
                    mousePos.x = shipLoc.getX();
                    mousePos.y = shipLoc.getY();
                }
            }
            InputShim.mouseMove((int) mousePos.x, (int) mousePos.y);
            InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
        }
        if(controller.isButtonPressed(LogicalButtons.RightTrigger)) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            playerFleet.goSlowOneFrame();
        }
        if(controller.getButtonEvent(LogicalButtons.Select) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_C, 'c');
        } else if(controller.getButtonEvent(LogicalButtons.RightStickUp) == 1) {
            if(currentHotkeyGroup > 0) {
                currentHotkeyGroup--;
                switchToHotkeyGroup();
            }
        } else if(controller.getButtonEvent(LogicalButtons.RightStickDown) == 1) {
            if(currentHotkeyGroup < 4) {
                currentHotkeyGroup++;
                switchToHotkeyGroup();
            }
        } else if(controller.getButtonEvent(LogicalButtons.RightStickLeft) == 1) {
            if(currentHotkey > 0) {
                currentHotkey--;
            }
        } else if(controller.getButtonEvent(LogicalButtons.RightStickRight) == 1) {
            if(currentHotkey < 9) {
                currentHotkey++;
            }
        } else if(controller.getButtonEvent(LogicalButtons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey));
        }
    }

    private void switchToHotkeyGroup() {
        InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
        InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
        InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
        InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
    }

    private boolean isMessageBoxShown() {
        var children = UIPanelReflector.getChildItems(getPanelForIndicators());
        int numChildren = children.size();
        if(numChildren > lastFrameNumChildren) {
            for(int i = lastFrameNumChildren; i < numChildren; i++ ) {
                var child = children.get(i);
                if(UIPanelAPI.class.isAssignableFrom(child.getClass()) && InputScreenManager.getInstance().getDisplayPanel() != null && child != InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                    var msgBox = MessageBoxReflector.TryGet((UIPanelAPI) child);
                    if(msgBox != null) {
                        InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, msgBox, MainCampaignUI.ID);
                        return true;
                    }
                }
            }
        }
        lastFrameNumChildren = numChildren;
        return false;
    }

    boolean tryOpenScreen(Object screenUi, String screenId) {
        if (screenUi != null) {
            return InputScreenManager.getInstance().transitionToScreen(screenId, screenUi);
        }
        return false;
    }

    boolean openScreenForCoreTab() {
        var coreUI = CampaignStateReflector.GetInstance().getCoreUI();
        if (coreUI != null) {
            var borderedPanel = BorderedPanelReflector.TryGet(coreUI);
            if (borderedPanel != null) {
                return switch (Global.getSector().getCampaignUI().getCurrentCoreTab()) {
                    case CARGO ->       tryOpenScreen(TradeUiReflector.TryGet(coreUI, borderedPanel), TradeScreen.ID);
                    case CHARACTER ->   tryOpenScreen(CharacterSheetReflector.TryGet(coreUI, borderedPanel), CharacterTabUI.ID);
                    case FLEET ->       tryOpenScreen(FleetTabReflector.TryGet(coreUI, borderedPanel), FleetTabUI.ID);
                    case INTEL ->       tryOpenScreen(IntelTabReflector.TryGet(coreUI, borderedPanel), IntelTabUI.ID);
                    case MAP ->         tryOpenScreen(MapReflector.TryGet(coreUI, borderedPanel), MapTabUI.ID);
                    case REFIT ->       tryOpenScreen(borderedPanel.getPanel(), RefitTabUI.ID);
                    case OUTPOSTS ->    tryOpenScreen(borderedPanel.getPanel(), CommandTabUI.ID);
                };
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return ID;
    }
}
