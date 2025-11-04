/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.codex2.CodexDialog;
import com.fs.starfarer.coreui.AptitudeRow;
import com.fs.state.AppDriver;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.campaign.*;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.*;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenBase {

    DirectionalUINavigator directionalUINavigator;
    MapInputHandler mapInputHandler;
    public static final String ID = "NoScreen";
    public static final String SCOPES = InputScopeBase.ID;
    protected List<Pair<Indicators, String>> indicators;
    protected EnumMap<LogicalButtons, ButtonPressHandler> buttonHandlers;
    protected EnumMap<LogicalButtons, ButtonChangeHandler> buttonExHandlers;
    protected EnumMap<Joystick, DigitalJoystickHandler> digitalJoystickHandlers;
    protected EnumMap<Joystick, AnalogJoystickHandler> analogJoystickHandlers;
    protected HandlerController controller;
    protected EnumMap<Joystick, Vector2f> lastFrameJoystickVal;

    public InputScreenBase() {
        buttonHandlers = new EnumMap<>(LogicalButtons.class);
        buttonExHandlers = new EnumMap<>(LogicalButtons.class);
        indicators = new ArrayList<>();
        digitalJoystickHandlers = new EnumMap<>(Joystick.class);
        analogJoystickHandlers = new EnumMap<>(Joystick.class);
        lastFrameJoystickVal = new EnumMap<>(Joystick.class);
        controller = SSMSControllerModPluginEx.controller;
    }

    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    public void deactivate() {
        directionalUINavigator = null;
        mapInputHandler = null;
    }

    public void activate(Object ...args) throws Throwable {
    }

    public void renderInWorld(ViewportAPI viewport) {
    }

    public void renderUI(ViewportAPI viewport) {
        if(directionalUINavigator != null) {
            directionalUINavigator.render();
        }

        if(mapInputHandler != null) {
            mapInputHandler.render();
        }
    }

    public void preInput(float advance) {
    }

    public void processInputEvents(List<InputEventAPI> events) {
    }

    public void processInputEvents(List<InputEventAPI> normalEvents, List<Pair<LogicalButtons, Boolean>> buttonEvents, List<Pair<AxisMapping, Float>> axisEvents) {
    }

    public final void processControllerEvents(float advance, List<Pair<LogicalButtons, Boolean>> buttonEvents, List<Pair<AxisMapping, Float>> axisEvents) {
        for (var btnEvent : buttonEvents) {
            if (!btnEvent.two) {
                var btnHandler = buttonHandlers.get(btnEvent.one);
                if (btnHandler != null) btnHandler.performAction(advance);
            }
            var btnExHandler = buttonExHandlers.get(btnEvent.one);
            if (btnExHandler != null) btnExHandler.performAction(advance, btnEvent.two);

            if (btnEvent.two) {
                Joystick joystick = Joystick.fromButton(btnEvent.one);
                var btnAxis = AxisMapping.fromButton(btnEvent.one);
                if (joystick != null && btnAxis != null) {
                    var handler = digitalJoystickHandlers.get(joystick);
                    if (handler != null) {
                        if (btnAxis == joystick.getXAxisMapping()) {
                            if (btnEvent.one == btnAxis.getLowerLimitButton()) handler.performLeftAction(advance);
                            else if (btnEvent.one == btnAxis.getUpperLimitButton()) handler.performRightAction(advance);
                        } else if (btnAxis == joystick.getYAxisMapping()) {
                            if (btnEvent.one == btnAxis.getLowerLimitButton()) handler.performUpAction(advance);
                            else if (btnEvent.one == btnAxis.getUpperLimitButton()) handler.performDownAction(advance);
                        }
                    }
                }
            }
        }
        EnumMap<Joystick, Boolean> joystickHandlersToFire = new EnumMap<>(Joystick.class);
        for (var axisEvent : axisEvents) {
            Joystick joystick = Joystick.fromAxisMapping(axisEvent.one);
            if (joystick != null) {
                joystickHandlersToFire.put(joystick, true);
            }
        }
        for (Joystick joystick : joystickHandlersToFire.keySet()) {
            var handler = analogJoystickHandlers.get(joystick);
            if (handler != null){
                var joystickVal = controller.getJoystick(joystick);
                var lastFrameJoystick = lastFrameJoystickVal.get(joystick);
                if(lastFrameJoystick == null || joystickVal.getX() != lastFrameJoystick.getX() || joystickVal.getY() != lastFrameJoystick.getY()) {
                    handler.performAction(advance, joystickVal);
                    lastFrameJoystickVal.put(joystick, joystickVal);
                }
            }
        }
        for(var handler : buttonExHandlers.values()) {
            if(handler instanceof ButtonPressOrHoldHandler buttonPressOrHoldHandler) {
                buttonPressOrHoldHandler.advance(advance);
            }
        }

        if(directionalUINavigator != null && directionalUINavigator.isContextChanged()) {
            refreshIndicators();
        }
    }

    public void postInput(float advance) {
    }

    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }

    protected void addButtonPressHandler(String msg, LogicalButtons logicalButtons, ButtonPressHandler btnPressHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonHandlers.put(logicalButtons, btnPressHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected void addButtonChangeHandler(String msg, LogicalButtons logicalButtons, ButtonChangeHandler btnChangeHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonExHandlers.put(logicalButtons, btnChangeHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected  void addButtonPressOrHoldHandler(String pressMsg, String holdMsg, LogicalButtons logicalButtons, ButtonPressOrHoldHandler btnPressHoldHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonExHandlers.put(logicalButtons, btnPressHoldHandler);
        indicators.add(new Pair<>(indicator, "(Quick press) " + pressMsg));
        indicators.add(new Pair<>(indicator, "(Press & Hold) " + holdMsg));
    }

    protected void addDigitalJoystickHandler(String msg, Joystick joystick, DigitalJoystickHandler digitalJoystickHandler) {
        Indicators joystickIndicator = Indicators.fromJoystick(joystick);
        if(joystickIndicator != null) {
            indicators.add(new Pair<>(joystickIndicator, msg));
        }
        digitalJoystickHandlers.put(joystick, digitalJoystickHandler);
        if(digitalJoystickHandler instanceof DirectionalUINavigator directionalUINavigator1) {
            this.directionalUINavigator = directionalUINavigator1;
        }
    }

    protected void addDirectionalUINavigator(DirectionalUINavigator directionalUINavigator) {
        this.directionalUINavigator = directionalUINavigator;

        indicators.add(new Pair<>(Indicators.DPad, "Navigate"));
        digitalJoystickHandlers.put(Joystick.DPad, directionalUINavigator);

        indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));

        switch(directionalUINavigator.getCurContext()) {
            case Map -> {
                indicators.add(new Pair<>(Indicators.RightStick, "Move map"));
                indicators.add(new Pair<>(Indicators.LeftTrigger, "Zoom out"));
                indicators.add(new Pair<>(Indicators.RightTrigger, "Zoom in"));
            }
            case Scroller -> indicators.add(new Pair<>(Indicators.RightStick, "Scroll"));
        }

        addButtonChangeHandler("Select", LogicalButtons.A, directionalUINavigator::handleAButton);

        directionalUINavigator.setJoystickEnabled(true);
    }

    protected void addAnalogJoystickHandler(String msg, Joystick joystick, AnalogJoystickHandler analogJoystickHandler) {
        Indicators joystickIndicator = Indicators.fromJoystick(joystick);
        if(joystickIndicator != null) {
            indicators.add(new Pair<>(joystickIndicator, msg));
        }
        analogJoystickHandlers.put(joystick, analogJoystickHandler);
    }

    protected MapInputHandler addMapHandler(UIComponentAPI mapComponent) {
        mapInputHandler = new MapInputHandler(mapComponent);
        addAnalogJoystickHandler("Move cursor", Joystick.Left, mapInputHandler::handleLeftJoystick);
        addAnalogJoystickHandler("Move map", Joystick.Right, mapInputHandler::handleRightJoystick);
        addButtonChangeHandler("Select", LogicalButtons.A, mapInputHandler::handleAButton);
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Zoom out"));
        indicators.add(new Pair<>(Indicators.RightTrigger, "Zoom in"));
        mapInputHandler.centerMousePos();
        return mapInputHandler;
    }

    protected boolean isComponentVisible(UIComponentAPI comp) {
        var pos = comp.getPosition();
        return comp.getOpacity() == 1.f && pos.getX() >= 0 && pos.getX() <= Display.getWidth() &&
                pos.getY() >= 0 && pos.getY() <= Display.getHeight();
    }


    boolean tryOpenScreen(Object screenUi, String screenId) {
        if (screenUi != null) {
            return InputScreenManager.getInstance().transitionToScreen(screenId, screenUi);
        }
        return false;
    }

    protected boolean openCoreUiTab(CoreUIAPI coreUI) {
        BorderedPanelReflector borderedPanelReflector = null;
        for(var coreuiChild : new UIPanelReflector((UIPanelAPI) coreUI).getChildPanels()) {
            var borderedPanel = BorderedPanelReflector.TryGet(coreUI, coreuiChild);
            if (borderedPanel != null) {
                if(borderedPanelReflector == null) {
                    borderedPanelReflector = borderedPanel;
                } else {
                    return false; // more than 1 bordered panel, skip it
                }
            }
        }
        if(borderedPanelReflector != null) {
            boolean output = switch (Global.getSector().getCampaignUI().getCurrentCoreTab()) {
                case CARGO -> tryOpenScreen(TradeUiReflector.TryGet(coreUI, borderedPanelReflector), TradeScreen.ID);
                case CHARACTER -> tryOpenScreen(CharacterSheetReflector.TryGet(coreUI, borderedPanelReflector), CharacterTabUI.ID);
                case FLEET -> tryOpenScreen(FleetTabReflector.TryGet(coreUI, borderedPanelReflector), FleetTabUI.ID) ||
                            tryOpenScreen(FleetMarketTabUI.FleetMarketTabReflector.TryGet(coreUI, borderedPanelReflector), FleetMarketTabUI.ID);
                case INTEL -> tryOpenScreen(IntelTabReflector.TryGet(coreUI, borderedPanelReflector), IntelTabUI.ID);
                case MAP -> tryOpenScreen(MapReflector.TryGet(coreUI, borderedPanelReflector), MapTabUI.ID);
                case REFIT -> tryOpenScreen(borderedPanelReflector.getInnerPanel(), RefitTabUI.ID);
                case OUTPOSTS -> tryOpenScreen(borderedPanelReflector.getInnerPanel(), CommandTabUI.ID);
            };
            return output;
        }
        return false;
    }

    protected void getPanelNavigatables(UIPanelReflector pnl, List<DirectionalUINavigator.NavigationObject> directionalObjects, List<ScrollPanelReflector> scrollers) {
        if( ScrollPanelAPI.class.isAssignableFrom(pnl.getPanel().getClass())) {
            getScrollerNavigatables(new ScrollPanelReflector((ScrollPanelAPI) pnl.getPanel()), directionalObjects, scrollers);
        } else {
            for (var item : pnl.getChildItems()) {
                if (ButtonAPI.class.isAssignableFrom(item.getClass()) && isComponentVisible((ButtonAPI)item)) {
                    directionalObjects.add(new DirectionalUINavigator.NavigationObject((ButtonAPI) item));
                } else if (UIPanelAPI.class.isAssignableFrom(item.getClass())) {
                    UIPanelReflector reflectorTmp = new UIPanelReflector((UIPanelAPI) item);
                    if (reflectorTmp.getFader().getBrightness() == 1.f && isComponentVisible(reflectorTmp.getPanel())) {
                        getPanelNavigatables(reflectorTmp, directionalObjects, scrollers);
                    }
                }
            }
        }
    }

    protected void getScrollerNavigatables(ScrollPanelReflector scroller, List<DirectionalUINavigator.NavigationObject> directionalObjects, List<ScrollPanelReflector> scrollers) {
        scrollers.add(scroller);
        UIPanelReflector container = new UIPanelReflector(scroller.getContentContainer());
        if(scroller.getFader().getBrightness() != 1.f || !isComponentVisible(scroller.getPanel())) {
            return;
        }
        for(var item : container.getChildItems()) {
            if(UIPanelAPI.class.isAssignableFrom(item.getClass()) && (TagDisplayAPI.class.isAssignableFrom(item.getClass()) || AptitudeRow.class.isAssignableFrom(item.getClass()))) {
                if(isComponentVisible((UIComponentAPI) item)) {
                    getPanelNavigatables(new UIPanelReflector((UIPanelAPI) item), directionalObjects, scrollers);
                }
            } else if(UIComponentAPI.class.isAssignableFrom(item.getClass())) {
                if(isComponentVisible((UIComponentAPI)item)) {
                    directionalObjects.add(new DirectionalUINavigator.NavigationObject((UIComponentAPI) item, scroller));
                }
            }
        }
        for(var item : scroller.getChildItems()) {
            if(UIComponentAPI.class.isAssignableFrom(item.getClass()) && item != container.getPanel()) {
                UIComponentReflector comp = new UIComponentReflector((UIComponentAPI) item);
                if(((UIComponentAPI)item).getPosition().getWidth() > 0 && comp.getFader().getBrightness() == 1.f && isComponentVisible((UIComponentAPI)item)) {
                    directionalObjects.add(new DirectionalUINavigator.NavigationObject((UIComponentAPI)item));
                }
            }
        }
    }

    protected boolean isCodexOpen() {
        if (Global.getSettings().isShowingCodex()) return true;

        if (Global.getCurrentState() == GameState.CAMPAIGN && Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.FLEET) {
            var coreUI = CampaignStateReflector.GetInstance().getCoreUI();
            for(var coreUiChild : new UIPanelReflector((UIPanelAPI) coreUI).getChildPanels()) {
                if (coreUiChild instanceof CodexDialog) {
                    return true;
                }
            }
            var interactDialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
            if(interactDialog != null) {
                var dialogCoreUi = new InteractionDialogReflector(interactDialog).getCoreUI(interactDialog);
                for(var coreUiChild : new UIPanelReflector((UIPanelAPI) dialogCoreUi).getChildPanels()) {
                    if (coreUiChild instanceof CodexDialog) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected CodexDialog tryGetCodexDialog() {
        if (Global.getSettings().isShowingCodex()) { //isShowingCodex does not work in all cases as of 0.98
            var state = AppDriver.getInstance().getCurrentState();
            var stateReflector = new ClassReflector(state.getClass());
            if (Global.getCurrentState() == GameState.COMBAT) {
                //Combat F2 with ship selected, simulator ship F2.
                var getRibbon = stateReflector.getDeclaredMethod("getRibbon");
                if (getRibbon != null) {
                    var ribbon = new UIPanelReflector((UIPanelAPI) getRibbon.invoke(state));
                    var temp = new UIPanelReflector(ribbon.getParent());
                    for(var child : temp.getChildPanels()) {
                        if(child instanceof CodexDialog codexDialog) {
                            return codexDialog;
                        }
                    }
                }
                //Note that the codex that opens from clicking the combat "More Info" question mark button appears in the below and not the above
            }

            //F2 press, and in some other places
            var getOverlayPanelForCodex = stateReflector.getDeclaredMethod("getOverlayPanelForCodex");
            var codexOverlayPanel = new UIPanelReflector((UIPanelAPI) getOverlayPanelForCodex.invoke(state));

            for(var child : codexOverlayPanel.getChildPanels()) {
                if(child instanceof CodexDialog codexDialog) {
                    return codexDialog;
                }
            }
        }
        switch(Global.getCurrentState()) {
            case CAMPAIGN -> {
                for (var child : new UIPanelReflector((UIPanelAPI) CampaignStateReflector.GetInstance().getCoreUI()).getChildPanels()) {
                    if (child instanceof CodexDialog codexDialog) {
                        return codexDialog;
                    }
                }
                var interactDialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                if(interactDialog != null) {
                    var dialogCoreUi = new InteractionDialogReflector(interactDialog).getCoreUI(interactDialog);
                    for(var coreUiChild : new UIPanelReflector((UIPanelAPI) dialogCoreUi).getChildPanels()) {
                        if (coreUiChild instanceof CodexDialog codexDialog) {
                            return codexDialog;
                        }
                    }
                }
            }
            case TITLE -> {
                for(var child : new UIPanelReflector(new TitleScreenStateReflector().getScreenPanel()).getChildPanels()) {
                    if(child instanceof CodexDialog codexDialog) {
                        return codexDialog;
                    }
                }
            }
        }
        return null;
    }

    public void refreshIndicators() {
        indicators = null;
        clearHandlers();
        InputScreenManager.getInstance().refreshIndicators();
    }

    protected void clearHandlers() {
        buttonHandlers.clear();
        buttonExHandlers.clear();
        digitalJoystickHandlers.clear();
        analogJoystickHandlers.clear();
        indicators = null;
    }

    public UIPanelAPI getPanelForIndicators() {
        return switch (Global.getCurrentState()) {
            case TITLE -> new TitleScreenStateReflector().getScreenPanel();
            case CAMPAIGN -> CampaignStateReflector.GetInstance().getScreenPanel();
            case COMBAT -> CombatStateReflector.GetInstance().getWidgetPanel();
        };
    }

    public Alignment getIndicatorsAlignment() {
        return switch (Global.getCurrentState()) {
            case TITLE -> Alignment.BL;
            case CAMPAIGN -> Alignment.RMID;
            case COMBAT -> Alignment.BR;
        };
    }
}
