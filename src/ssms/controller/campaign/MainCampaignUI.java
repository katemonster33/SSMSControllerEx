package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.InputScreenBase;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.Indicators;
import ssms.controller.reflection.BorderedPanelReflector;
import ssms.controller.reflection.CampaignStateReflector;
import ssms.controller.reflection.CharacterSheetReflector;
import ssms.controller.reflection.TradeUiReflector;

public class MainCampaignUI extends InputScreenBase {
    public static final String ID = "MainCampaign";
    CampaignScope campaignScope;
    Vector2f lastHeading = null;
    Vector2f mousePos = new Vector2f(-1.f, -1.f);
    HandlerController handler;
    boolean isShiftDown = false;
    ControllerCrosshairRenderer hotbarIndicatorRenderer;
    int currentHotkeyGroup = 0, currentHotkey = 0;

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
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        // render hotbar indicator
        int x = 277 + currentHotkey * 59 + 29, y = 103;
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
        var output = new ArrayList<>(indicators);
        return output;
    }

    boolean isMouseDown = false;
    boolean isMoving = false;
    @Override
    public void preInput(float advance) {
        float zoom = CampaignStateReflector.GetInstance().getZoomFactor();
        ControllerCrosshairRenderer.getControllerRenderer().setSize((int)(58 / zoom));
        if(Global.getSector().getCampaignUI().isShowingDialog()) {
            if(Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{ }, DialogUI.ID, new Object[]{ });
            } else if(Global.getSector().getCampaignUI().getCurrentCoreTab() != null) {
                var coreui = CampaignStateReflector.GetInstance().getCoreUI();
                if(coreui != null) {
                    var borderedPanel = BorderedPanelReflector.TryGet(coreui);
                    if(borderedPanel != null) {
                        switch (Global.getSector().getCampaignUI().getCurrentCoreTab()) {
                            case CARGO -> {
                                var tradeui = TradeUiReflector.TryGet(coreui, borderedPanel);
                                if (tradeui != null) {
                                    InputScreenManager.getInstance().transitionToScreen(TradeScreen.ID, tradeui);
                                }
                            }
                            case CHARACTER -> {
                                var charUi = CharacterSheetReflector.TryGet(coreui, borderedPanel);
                                if(charUi != null) {
                                    InputScreenManager.getInstance().transitionDelayed(CharacterTabUI.ID, charUi);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(mousePos.x != -1.f && mousePos.y != -1.f) {
            if (handler.isButtonAPressed() && !isMouseDown) {
                InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, 0);
                isMouseDown = true;
            } else if (!handler.isButtonAPressed() && isMouseDown) {
                InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, 0);
                isMouseDown = false;
            }
        }
        if(handler.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
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
        if(handler.isButtonXPressed()) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            playerFleet.goSlowOneFrame();
        }
        if(handler.getButtonEvent(HandlerController.Buttons.Start) == 1) {
            Global.getSector().setPaused(!Global.getSector().isPaused());
        }
        if(handler.getButtonEvent(HandlerController.Buttons.Select) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(handler.getButtonEvent(HandlerController.Buttons.LeftTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_C, 'c');
        } else if(handler.getButtonEvent(HandlerController.Buttons.RightStickUp) == 1) {
            if(currentHotkeyGroup > 0) {
                currentHotkeyGroup--;
                InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup - 1, (char)('0' + currentHotkeyGroup));
                InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup - 1, (char)('0' + currentHotkeyGroup));
            }
        } else if(handler.getButtonEvent(HandlerController.Buttons.RightStickDown) == 1) {
            if(currentHotkeyGroup < 4) {
                currentHotkeyGroup++;
                InputShim.keyDown(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyDown(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
                InputShim.keyUp(Keyboard.KEY_LCONTROL, '\0');
                InputShim.keyUp(Keyboard.KEY_1 + currentHotkeyGroup, (char)('1' + currentHotkeyGroup));
            }
        } else if(handler.getButtonEvent(HandlerController.Buttons.RightStickLeft) == 1) {
            if(currentHotkey > 0) {
                currentHotkey--;
            }
        } else if(handler.getButtonEvent(HandlerController.Buttons.RightStickRight) == 1) {
            if(currentHotkey < 9) {
                currentHotkey++;
            }
        } else if(handler.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDown(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey));
            InputShim.keyUp(Keyboard.KEY_1 + currentHotkey, (char)('1' + currentHotkey));
        }
        if(!isShiftDown && handler.isButtonBumperRightPressed()) {
            InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
            isShiftDown = true;
        } else if(isShiftDown && !handler.isButtonBumperRightPressed()) {
            InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
            isShiftDown = false;
        }

        if(handler.isDpadRight()) {
            //Global.getSector().
        }
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
