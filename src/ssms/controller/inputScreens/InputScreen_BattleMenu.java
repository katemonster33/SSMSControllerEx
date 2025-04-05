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
package ssms.controller.inputScreens;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.coreui.w;
import com.fs.state.AppDriver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import lunalib.backend.ui.components.base.LunaUIButton;
import lunalib.lunaUI.LunaUIUtils;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.apache.log4j.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.CombatStateReflector;
import ssms.qol.ui.AlignmentHorizontal;
import ssms.qol.ui.AlignmentVertical;
import ssms.qol.ui.UIComponentFactory;
import ssms.qol.ui.UIComponentParentFactory;
import ssms.qol.ui.UIComponent_Button;
import ssms.qol.ui.UIComponent_Column;
import ssms.qol.ui.UIComponent_Parent;
import ssms.qol.ui.UIComponent_Row;
import ssms.qol.ui.UIContext;
import ssms.qol.ui.UIUtil;

/**
 *f5
 * @author Malte Schulze
 */
public class InputScreen_BattleMenu implements InputScreen {
    public static final String ID = "BattleMenu";
    public static final String SCOPES = "Battle";
    protected HandlerController handler;
    protected InputScope_Battle scope;
    protected CombatEngineAPI engine;
    protected int selectedBtnIndex = -1;
    protected UIComponent_Parent root;
    protected boolean prevMenuEntry, nextMenuEntry, selectMenuEntry;
    protected List<Pair<Indicators, String>> indicators;

    public InputScreen_BattleMenu() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.BumperRight, "Next"));
        indicators.add(new Pair<>(Indicators.BumperLeft, "Previous"));
        indicators.add(new Pair<>(Indicators.Select, "Select"));
    }
    
    @Override
    public void deactivate() {
        scope.timeDilation(false,"MENU");
        try {
            CombatStateReflector.GetInstance().HideHud();
        } catch (Throwable t) {
            engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to hide HUD!", t);
        }
        
        handler = null;
        scope = null;
        engine = null;
        root.dismis();
    }

    @Override
    public void activate(Object[] args) {
        handler = SSMSControllerModPluginEx.controller;
        scope = (InputScope_Battle)InputScreenManager.getInstance().getCurrentScope();
        engine = scope.engine;
        
        scope.timeDilation(true,"MENU");
        try {
            CombatStateReflector.GetInstance().HideHud();
        } catch (Throwable t) {
            engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to hide HUD, ensure SSMSUnlock is installed!", t);
        }
        root = null;
        selectedBtnIndex = -1;
        
        if ( args != null && args.length > 0 ) {
            if ( args[0] != null ) {
                if ( args[0].getClass() == int.class )
                    selectedBtnIndex = (int)args[0];
                else if ( args[0].getClass() == Integer.class )
                    selectedBtnIndex = (Integer)args[0];
            }
        }
    }
    
    @Override
    public void preInput(float advance) {
        if ( handler.getButtonEvent(HandlerController.Buttons.BumperRight) == 1 ) {
            nextMenuEntry = true;
            prevMenuEntry = false;
        }
        if ( handler.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1 ) {
            prevMenuEntry = true;
            nextMenuEntry = false;
        }
        if ( handler.getButtonEvent(HandlerController.Buttons.Select) == 1 ) {
            selectMenuEntry = true;
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
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        if ( root == null ) {
            root = assembleMenu();
            float xMin = viewport.convertWorldXtoScreenX(viewport.getLLX()), 
                yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), 
                xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth()), 
                yMax = viewport.convertWorldYtoScreenY(viewport.getLLY() + viewport.getVisibleHeight());
            root.resize(new Rectangle((int)xMin, (int)yMin, (int)xMax, (int)yMax));
        }
        
        if ( selectedBtnIndex == -1 && (!nextMenuEntry && !prevMenuEntry) ) {
            selectedBtnIndex = selectNextButton(root, -1);
        }
        
        if ( nextMenuEntry ) {
            selectedBtnIndex = selectNextButton(root, selectedBtnIndex);
            nextMenuEntry = false;
        } else if ( prevMenuEntry ) {
            selectedBtnIndex = selectPrevButton(root, selectedBtnIndex);
            prevMenuEntry = false;
        } else if ( selectMenuEntry ) {
            selectMenuEntry = false;
            List<UIComponent_Button> btns = UIUtil.getInstance().findComponents(root, UIComponent_Button.class);
            if ( this.selectedBtnIndex >= 0 && this.selectedBtnIndex < btns.size() ) {
                btns.get(this.selectedBtnIndex).onClick();
            }
        }
        
        if ( root.isLayoutDirty() ) {
            root.pack();
        }
        root.getContext().pushStyle(UIContext.StyleProperty.alphaFactor, viewport.getAlphaMult());
        root.render();
        root.getContext().popStyle(UIContext.StyleProperty.alphaFactor);
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
    
    protected void closeMenu() {
        InputScreenManager.getInstance().transitionDelayed("BattleSteering");
    }
    
    protected int selectPrevButton(UIComponent_Parent root, int currentlySelectedBtnIndex) {
        if ( root == null ) return -1;
        List<UIComponent_Column> clmns = UIUtil.getInstance().findComponents(root, UIComponent_Column.class);
        UIComponent_Column activeColumn = clmns.get(clmns.size()-1);
        List<UIComponent_Button> btns = UIUtil.getInstance().findComponents(root, UIComponent_Button.class);
        
        int btnsSkipped = 0;
        Iterator<UIComponent_Button> it = btns.iterator();
        while ( it.hasNext() ) {
            UIComponent_Button btn = it.next();
            deselectButton(btn);
            
            UIComponent_Parent p = btn.parentComponent();
            while ( p != null && p != activeColumn ) {
                p = p.parentComponent();
            }
            if ( p == null ) {
                btnsSkipped++;
                it.remove();
            }
        }
        currentlySelectedBtnIndex -= btnsSkipped;
        if ( currentlySelectedBtnIndex < -1 ) currentlySelectedBtnIndex = -1;
        if ( btns.isEmpty() ) {
            return -1;
        } else if ( --currentlySelectedBtnIndex < 0 ) {
            selectButton(btns.get(btns.size()-1));
            return btns.size() - 1 + btnsSkipped;
        } else if ( currentlySelectedBtnIndex < btns.size() ) {
            selectButton(btns.get(currentlySelectedBtnIndex));
            return currentlySelectedBtnIndex + btnsSkipped;
        } else {
            selectButton(btns.get(0));
            return btnsSkipped;
        }
    }
    
    protected int selectNextButton(UIComponent_Parent root, int currentlySelectedBtnIndex) {
        if ( root == null ) return -1;
        List<UIComponent_Column> clmns = UIUtil.getInstance().findComponents(root, UIComponent_Column.class);
        UIComponent_Column activeColumn = clmns.get(clmns.size()-1);
        List<UIComponent_Button> btns = UIUtil.getInstance().findComponents(root, UIComponent_Button.class);
        
        int btnsSkipped = 0;
        Iterator<UIComponent_Button> it = btns.iterator();
        while ( it.hasNext() ) {
            UIComponent_Button btn = it.next();
            deselectButton(btn);
            
            UIComponent_Parent p = btn.parentComponent();
            while ( p != null && p != activeColumn ) {
                p = p.parentComponent();
            }
            if ( p == null ) {
                btnsSkipped++;
                it.remove();
            }
        }
        currentlySelectedBtnIndex -= btnsSkipped;
        if ( currentlySelectedBtnIndex < -1 ) currentlySelectedBtnIndex = -1;
        if ( btns.isEmpty() ) {
            return -1;
        } else if ( ++currentlySelectedBtnIndex < btns.size() ) {
            selectButton(btns.get(currentlySelectedBtnIndex));
            return currentlySelectedBtnIndex + btnsSkipped;
        } else {
            selectButton(btns.get(0));
            return btnsSkipped;
        }
    }
    
    protected void deselectButton(UIComponent_Button btn) {
        btn.addStyle(UIContext.StyleProperty.textColor, UIContext.StyleProperty.textColor.initialValue);
        btn.addStyle(UIContext.StyleProperty.textMouseOverColor, UIContext.StyleProperty.textColor.initialValue);
    }
    
    protected void selectButton(UIComponent_Button btn) {
        btn.addStyle(UIContext.StyleProperty.textColor, UIContext.StyleProperty.textMouseOverColor.initialValue);
        btn.addStyle(UIContext.StyleProperty.textMouseOverColor, UIContext.StyleProperty.textMouseOverColor.initialValue);
    }
    
    protected UIComponent_Parent assembleMenu() {
        UIComponent_Parent menu = UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.top))
                    .addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Pause") {
                                @Override
                                public void onClick() {
                                    engine.setPaused(!engine.isPaused());
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Warroom") {
                                @Override
                                public void onClick() {
                                    CombatStateReflector.GetInstance().ShowWarRoom();
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Autopilot") {
                                @Override
                                public void onClick() {
                                    CombatStateReflector.GetInstance().ToggleAutoPilot();
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Broadside") {
                                @Override
                                public void onClick() {
                                    UIComponent_Parent parent = parentComponent();
                                    while ( parent != null && !UIComponent_Row.class.isAssignableFrom(parent.getClass()) ) {
                                        parent = parent.parentComponent();
                                    }
                                    if ( parent != null ) {
                                        final UIComponent_Parent rowComponent = parent;
                                        rowComponent.addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Front") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setOffsetFacingAngle(0);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Right") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setOffsetFacingAngle(90f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Left") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setOffsetFacingAngle(-90);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .finish());
                                        selectedBtnIndex = selectNextButton(root, -1);
                                    }
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Zoom") {
                                @Override
                                public void onClick() {
                                    UIComponent_Parent parent = parentComponent();
                                    while ( parent != null && !UIComponent_Row.class.isAssignableFrom(parent.getClass()) ) {
                                        parent = parent.parentComponent();
                                    }
                                    if ( parent != null ) {
                                        final UIComponent_Parent rowComponent = parent;
                                        rowComponent.addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("2x") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setZoom(2f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("3x") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setZoom(3f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("4x") {
                                                    @Override
                                                    public void onClick() {
                                                        scope.setZoom(4f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        closeMenu();
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .finish());
                                        selectedBtnIndex = selectNextButton(root, -1);
                                    }
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Full Assault") {
                                @Override
                                public void onClick() {
                                    CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
                                    if ( fleetManager != null ) {
                                        CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                                        taskManager.setFullAssault(!taskManager.isFullAssault());
                                    }
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Full Retreat") {
                                @Override
                                public void onClick() {
                                    CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
                                    if ( fleetManager != null ) {
                                        CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                                        taskManager.orderFullRetreat();
                                    }
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return scope.isControllerSteeringEnabled() ? "Steering Off" : "Steering On";
                                }
                            }) {
                                @Override
                                public void onClick() {
                                    scope.setControllerSteeringEnabled(!scope.isControllerSteeringEnabled());
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Cancel") {
                                @Override
                                public void onClick() {
                                    closeMenu();
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .finish())
                .finish();
        
        //container to center the menu on the screen
        return UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.middle, AlignmentVertical.middle)
                .setGrowHorizontal(true).setGrowVertical(true))
            .addChild(menu).setContext(new UIContext()).finish();
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String[] getScopes() { return new String[]{ SCOPES }; }
}
