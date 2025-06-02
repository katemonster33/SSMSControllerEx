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

import ssms.controller.Indicators;
import ssms.controller.combat.BattleScope;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.reflection.*;

public class MainCampaignUI extends InputScreenBase {
    public static final String ID = "MainCampaign";
    CampaignScope campaignScope;
    Vector2f lastHeading = null;
    Vector2f mousePos = new Vector2f(-1.f, -1.f);
    HandlerController handler;
    boolean isShiftDown = false;
    boolean isMouseDown = false;
    boolean isMoving = false;
    ControllerCrosshairRenderer hotbarIndicatorRenderer;
    int currentHotkeyGroup = 0, currentHotkey = 0;
    int lastFrameNumChildren = -1;

    ArrayList<Pair<Indicators, String>> indicators;
    int selectedHotkey, selectedHotkeyGroup;
    int selectedTab;

    public MainCampaignUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.A, "Navigate"));
        indicators.add(new Pair<>(Indicators.B, "Use/assign hotkey"));
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        indicators.add(new Pair<>(Indicators.Select, "Open menu"));
        indicators.add(new Pair<>(Indicators.LeftStick, "Set ship heading"));
        indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle free look"));
        indicators.add(new Pair<>(Indicators.RightStick, "Navigate hotkeys"));
        indicators.add(new Pair<>(Indicators.RightStickButton, "Reassign hotkey"));
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Open character sheet"));
        indicators.add(new Pair<>(Indicators.BumperRight, "(hold) Speed up time"));
        indicators.add(new Pair<>(Indicators.RightTrigger, "(hold) Go slow"));
        //indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }

    @Override
    public void activate(Object... args) {
        handler = SSMSControllerModPluginEx.controller;
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        campaignScope.refreshSelectedIndex();
        selectedHotkey = selectedHotkeyGroup = selectedTab = -1;
        currentHotkeyGroup = currentHotkey = 0;
        isMouseDown = isMoving = isShiftDown = false;
        hotbarIndicatorRenderer = new ControllerCrosshairRenderer(58);
        lastFrameNumChildren = UIPanelReflector.getChildItems(getPanelForIndicators()).size();
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
        ReadableVector2f desiredHeading = handler.getLeftStick();
        if ( desiredHeading.getX() == 0 && desiredHeading.getY() == 0) {
            if(lastHeading != null && (desiredHeading.getX() != lastHeading.getX() || desiredHeading.getY() != lastHeading.getY())) {
                mousePos.x = pf.getLocation().getX();
                mousePos.y = pf.getLocation().getY();
                InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
            } else {
                return;
            }
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
            ypos = shipPos.y + desiredHeading.getY() * (shipPos.y - minY);
        } else {
            ypos = shipPos.y + desiredHeading.getY() * (maxY - shipPos.y);
        }
        mousePos.y = viewport.convertWorldYtoScreenY(ypos);
        InputShim.mouseMove((int)mousePos.x, (int)mousePos.y);
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public void preInput(float advance) {
        if(Global.getCurrentState() == GameState.COMBAT) {
            InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            return;
        }
        float zoom = CampaignStateReflector.GetInstance().getZoomFactor();
        ControllerCrosshairRenderer.getControllerRenderer().setSize((int)(58 / zoom));
        if(Global.getSector().getCampaignUI().isShowingDialog()) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
                InputScreenManager.getInstance().transitionToScreen(DialogUI.ID);
                return;
            } else if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null) {
                if (openScreenForCoreTab()) return;
            }
        }
        var children = UIPanelReflector.getChildItems(getPanelForIndicators());
        int numChildren = children.size();
        if(numChildren > lastFrameNumChildren) {
            for(int i = lastFrameNumChildren; i < numChildren; i++ ) {
                var child = children.get(i);
                if(UIPanelAPI.class.isAssignableFrom(child.getClass()) && InputScreenManager.getInstance().getDisplayPanel() != null && child != InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                    var msgBox = MessageBoxReflector.TryGet((UIPanelAPI) child);
                    if(msgBox != null) {
                        InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, msgBox, MainCampaignUI.ID);
                        return;
                    }
                }
            }
        }
        lastFrameNumChildren = numChildren;

        if(mousePos.x != -1.f && mousePos.y != -1.f) {
            if (handler.isButtonAPressed() && !isMouseDown) {
                InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                isMouseDown = true;
            } else if (!handler.isButtonAPressed() && isMouseDown) {
                InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                isMouseDown = false;
            }
        }
        if(handler.getButtonEvent(Buttons.LeftStickButton) == 1) {
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
        if(handler.isTriggerRight()) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            playerFleet.goSlowOneFrame();
        }
        if(handler.getButtonEvent(Buttons.Start) == 1) {
            Global.getSector().setPaused(!Global.getSector().isPaused());
        }
        if(handler.getButtonEvent(Buttons.Select) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(handler.getButtonEvent(Buttons.LeftTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_C, 'c');
        } else if(handler.getButtonEvent(Buttons.RightStickUp) == 1) {
            if(currentHotkeyGroup > 0) {
                currentHotkeyGroup--;
                InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup, (char)('0' + currentHotkeyGroup));
                InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup, (char)('0' + currentHotkeyGroup));
            }
        } else if(handler.getButtonEvent(Buttons.RightStickDown) == 1) {
            if(currentHotkeyGroup < 4) {
                currentHotkeyGroup++;
                InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
                InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
            }
        } else if(handler.getButtonEvent(Buttons.RightStickLeft) == 1) {
            if(currentHotkey > 0) {
                currentHotkey--;
            }
        } else if(handler.getButtonEvent(Buttons.RightStickRight) == 1) {
            if(currentHotkey < 9) {
                currentHotkey++;
            }
        } else if(handler.getButtonEvent(Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey));
        }
        if(!isShiftDown && handler.isButtonBumperRightPressed()) {
            InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
            isShiftDown = true;
        } else if(isShiftDown && !handler.isButtonBumperRightPressed()) {
            InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
            isShiftDown = false;
        }
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

    @Override
    public String[] getScopes() {
        return new String[]{ CampaignScope.ID };
    }
}
