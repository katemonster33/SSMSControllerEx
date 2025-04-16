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
package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import java.util.*;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import ssms.controller.*;
import ssms.controller.reflection.CombatStateReflector;

/**
 *f5
 * @author Malte Schulze
 */
public class BattleMenuScreen extends InputScreenBase {
    public static final String ID = "BattleMenu";
    public static final String SCOPES = "Battle";
    protected HandlerController handler;
    protected BattleScope scope;
    protected CombatEngineAPI engine;
    protected List<Pair<Indicators, String>> indicators;
    BattleMenuUI currentMenu;

    public abstract static class BattleMenuUI extends LunaBaseCustomPanelPlugin {
        int currentSelectedIndex = -1;
        UIPanelAPI parentPanel;
        CustomPanelAPI subpanel;
        final float spacing = 8;
        List<String> buttonTexts;
        List<ButtonAPI> buttons;
        public BattleMenuUI(UIPanelAPI parentPanel, String[] buttonTexts) {
            this.buttonTexts = new ArrayList<>();
            Collections.addAll(this.buttonTexts, buttonTexts);
            this.parentPanel = parentPanel;

            subpanel = Global.getSettings().createCustom(200, 30 * buttonTexts.length + (spacing * 2), this);
            subpanel.getPosition().inMid();
            initFromScript(subpanel);
        }
        @Override
        public void init() {
            ButtonAPI prevBtn = null;
            buttons = new ArrayList<>();
            //for(int index = 0; index < buttonTexts.length; index++) {
            var btnElem = subpanel.createUIElement(180, 30 * buttonTexts.size(), false);
            btnElem.getPosition().inMid();
            for(var str : buttonTexts) {
                var btn = btnElem.addButton(str, str, 180, 28, 0.f);
                if(prevBtn == null) {
                    btn.getPosition().inTMid(0.f);
                } else {
                    btn.getPosition().belowMid(prevBtn, 2.f);
                }
                prevBtn = btn;
                buttons.add(btn);
            }
            if(!buttons.isEmpty()) {
                currentSelectedIndex = 0;
                buttons.get(0).highlight();
            }
            subpanel.addUIElement(btnElem);
            show();
        }

        public void show() {
            parentPanel.addComponent(subpanel);
        }

        public void hide() {
            parentPanel.removeComponent(subpanel);
        }

        public void buttonSelected(int index) {
            if(currentSelectedIndex != -1) {
                buttons.get(index).unhighlight();
            }
            if(index >= 0 && index < buttons.size()) {
                buttons.get(index).highlight();
                currentSelectedIndex = index;
            }
        }

        public abstract void onClicked(int index);

        public void handleInput(HandlerController controller) throws IllegalArgumentException {
            if(buttons.isEmpty()) {
                throw new IllegalArgumentException("Cannot handle inputs on empty BattleMenuUI!");
            }
            if ( controller.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1) {
                buttons.get(currentSelectedIndex).unhighlight();
                currentSelectedIndex--;
                if(currentSelectedIndex < 0) currentSelectedIndex = buttons.size() - 1;
                buttons.get(currentSelectedIndex).highlight();
            } else if(controller.getButtonEvent(HandlerController.Buttons.BumperRight) == 1) {
                buttons.get(currentSelectedIndex).unhighlight();
                currentSelectedIndex++;
                if(currentSelectedIndex >= buttons.size()) currentSelectedIndex = 0;
                buttons.get(currentSelectedIndex).highlight();
            }
            if ( controller.getButtonEvent(HandlerController.Buttons.Select) == 1 ) {
                if(currentSelectedIndex != -1) {
                    onClicked(currentSelectedIndex);
                }
            }
        }
    };

    public BattleMenuScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.BumperRight, "Next"));
        indicators.add(new Pair<>(Indicators.BumperLeft, "Previous"));
        indicators.add(new Pair<>(Indicators.Select, "Select"));
    }
    
    @Override
    public void deactivate() {
        scope.timeDilation(false,"MENU");
//        try {
//            CombatStateReflector.GetInstance().HideHud();
//        } catch (Throwable t) {
//            engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
//            Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to hide HUD!", t);
//        }
        
        handler = null;
        scope = null;
        engine = null;
        if(currentMenu != null) {
            currentMenu.hide();
        }
    }

    @Override
    public void activate(Object ... args) {
        handler = SSMSControllerModPluginEx.controller;
        scope = (BattleScope) InputScreenManager.getInstance().getCurrentScope();
        engine = scope.engine;
        
        scope.timeDilation(true,"MENU");
//        try {
//            CombatStateReflector.GetInstance().HideHud();
//        } catch (Throwable t) {
//            engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
//            Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to hide HUD!", t);
//        }
        if(currentMenu != null) {
            currentMenu.show();
        }
    }
    
    @Override
    public void preInput(float advance) {
        if(currentMenu != null) {
            currentMenu.handleInput(handler);
        }
    }

    @Override
    public void postInput(float advance) {
        
    }

    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }
    
    @Override
    public void renderUI(ViewportAPI viewport) {
        if(currentMenu == null) {
            currentMenu = assembleMenu();
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
    
    protected void closeMenu() {
        if(currentMenu != null) {
            currentMenu.hide();
            currentMenu = null;
        }
        InputScreenManager.getInstance().transitionDelayed("BattleSteering");
    }


    void showBroadsideMenu() {
        if(currentMenu != null) {
            currentMenu.hide();
        }
        String[] broadsideOptions = {"Front", "Right", "Left", };
        currentMenu = new BattleMenuUI(CombatStateReflector.GetInstance().getWidgetPanel(), broadsideOptions) {
            @Override
            public void onClicked(int index) {
                switch (index) {
                    case 0:
                        scope.setZoom(2f);
                        closeMenu();
                        break;
                    case 1:
                        scope.setZoom(3f);
                        closeMenu();
                        break;
                    case 2:
                        scope.setZoom(4f);
                        closeMenu();
                        break;
                }
            }
        };
    }

    void showZoomMenu() {

        String[] zoomOptions = {"2x", "3x", "4x",};
        if(currentMenu != null) {
            currentMenu.hide();
        }
        currentMenu = new BattleMenuUI(CombatStateReflector.GetInstance().getWidgetPanel(), zoomOptions) {
            @Override
            public void onClicked(int index) {
                switch (index) {
                    case 0:
                        scope.setZoom(2f);
                        closeMenu();
                        break;
                    case 1:
                        scope.setZoom(3f);
                        closeMenu();
                        break;
                    case 2:
                        scope.setZoom(4f);
                        closeMenu();
                        break;
                }
            }
        };
    }
    
    protected BattleMenuUI assembleMenu() {
        String[] mainButtonTexts = { "Pause", "Warroom", "Autopilot", "Broadside", "Zoom", "Full Assault", "Full Retreat", "Toggle Steering Mode", "Cancel"};
        return new BattleMenuUI(CombatStateReflector.GetInstance().getWidgetPanel(), mainButtonTexts) {
            @Override
            public void onClicked(int index) {
                switch(index) {
                    case 0:
                        engine.setPaused(!engine.isPaused());
                        closeMenu();
                        break;
                    case 1:
                        CombatStateReflector.GetInstance().ShowWarRoom();
                        closeMenu();
                        break;
                    case 2:
                        CombatStateReflector.GetInstance().ToggleAutoPilot();
                        closeMenu();
                        break;
                    case 3:
                        showBroadsideMenu();
                        break;
                    case 4:
                        showZoomMenu();
                        break;
                    case 5:
                    case 6:
                        CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
                        if ( fleetManager != null ) {
                            CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                            if(index == 5) {
                                taskManager.setFullAssault(!taskManager.isFullAssault());
                            } else if(index == 6) {
                                taskManager.orderFullRetreat();
                            }
                        }
                        closeMenu();
                        break;
                    case 7:
                        scope.setControllerSteeringEnabled(!scope.isControllerSteeringEnabled());
                        break;
                    case 8:
                        closeMenu();
                        break;
                }
            }
        };
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String[] getScopes() { return new String[]{ SCOPES }; }
}
