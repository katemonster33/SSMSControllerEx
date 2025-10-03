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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Level;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.LogicalButtons;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenManager {
    static private volatile InputScreenManager instance;
    private Map<String, InputScreenBase> screens;//screen can belong to multiple scopes
    private Map<String, InputScopeBase> scopes;
    private InputScreenBase currentScreen;
    private InputScopeBase currentScope;
    private Transition nextScreen;
    IndicatorDisplayPanel displayPanel;
    static class Transition {
        protected String id;
        protected Object[] args;

        private Transition(String id, Object[] args) {
            this.id = id;
            this.args = args;
        }
    }
    
    private InputScreenManager() {
        screens = new HashMap<>();
        scopes = new HashMap<>();
        currentScope = new InputScopeBase(); currentScope.activate();
        currentScreen = new InputScreenBase();
        try {
            currentScreen.activate();
        } catch(Throwable ex) { }
    }

    public IndicatorDisplayPanel getDisplayPanel() {
        return displayPanel;
    }
    
    static public InputScreenManager getInstance() {
        InputScreenManager localInstance = InputScreenManager.instance;
        if ( localInstance == null ) {
            synchronized(InputScreenManager.class) {
                localInstance = InputScreenManager.instance;
                if ( localInstance == null ) {
                    InputScreenManager.instance = localInstance = new InputScreenManager();
                }
            }
        }
        return localInstance;
    }
    
    public void registerScreen(InputScreenBase screen) {
        screens.put(screen.getId(), screen);
    }
    
    public void registerScope(InputScopeBase scope) {
        scopes.put(scope.getId(), scope);
    }
    
    public InputScreenBase getCurrentScreen() {
        return currentScreen;
    }
    
    public InputScopeBase getCurrentScope() {
        return currentScope;
    }
    
    public boolean transitionDelayed(String id, Object ...args) {
        InputShim.clearAll();
        if ( screens.containsKey(id) ) {
            var screen = screens.get(id);
            if ( screenAllowsScope(screen, currentScope.getId()) ) {
                nextScreen = new Transition(id,args);
                return true;
            } else Global.getLogger(SSMSControllerModPluginEx.class).error("Screen id \""+id+"\" is not allowed in current scope \""+currentScope.getId()+"\".");
        } else Global.getLogger(SSMSControllerModPluginEx.class).error("Unregistered screen id \""+id+"\" ignoring transition.");
        return false;
    }
    
    private boolean screenAllowsScope(InputScreenBase screen, String scopeId) {
        String[] scopeIds = screen.getScopes();
        if ( scopeIds == null ) return true;
        for ( String scopeIdAllowed : scopeIds ) {
            if (scopeIdAllowed.equals(scopeId)) {
                return true;
            }
        }
        return false;
    }
    
    public InputScopeBase getScope(String scopeId) {
        return scopes.get(scopeId);
    }
    
    public InputScreenBase getScreen(String screenId) {
        return screens.get(screenId);
    }
    
    public boolean transitionToScope(String scopeId, Object ...args) {
        //moving to scope sets the screen id for the input screen that has the initial annoation and a matching scope
        //scope can hold variables like the engine for combat
        //transitions into the same scope are legal, they happen if no other scope is active between two seperate scope entries
        if ( scopes.containsKey(scopeId) ) {
            InputScopeBase scope = scopes.get(scopeId);
            return transitionToScope(scopeId, args, scope.getDefaultScreen(), null);
        } else {
            Global.getLogger(SSMSControllerModPluginEx.class).error("Scope \""+scopeId+"\" is not registered!");
        }
        return false;
    }


    public boolean transitionToScreen(String screenId, Object ... screenArgs) {
        InputShim.clearAll();
        refreshIndicators();
        if ( screens.containsKey(screenId) ) {
            InputScreenBase screen = screens.get(screenId);
            String defaultScope = screen.getScopes()[0];
            if ( scopes.containsKey(defaultScope) ) {
                InputScopeBase oldScope = currentScope;
                InputScopeBase scope = scopes.get(defaultScope);
                try {
                    try {
                        if ( currentScope != null ) {
                            currentScope.deactivate();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPluginEx.class).warn("Failed to deactivate scope, ignoring: "+currentScope, t);
                    }
                    scope.activate();
                    currentScope = scope;
                    try {
                        if ( currentScreen != null ) {
                            currentScreen.deactivate();
                            currentScreen.clearHandlers();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPluginEx.class).warn("Failed to deactivate screen, ignoring: "+currentScreen, t);
                    }
                    //horizontalAlignment = AlignmentHorizontal.right;
                    screen.activate(screenArgs);
                    currentScreen = screen;
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to activate scope skipping transition: " + defaultScope, t);
                    currentScope = oldScope;
                }
                return true;
            } else {
                Global.getLogger(SSMSControllerModPluginEx.class).error("Screen \""+screenId+"\" is not allowed in scope \""+defaultScope+"\"!");
            }
        } else {
            if ( !screens.containsKey(screenId) ) Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Screen \""+screenId+"\" is not registered!");
        }
        return false;
    }
    
    public boolean transitionToScope(String scopeId, Object[] scopeArgs, String screenId, Object[] screenArgs) {
        InputShim.clearAll();
        refreshIndicators();
        if ( scopes.containsKey(scopeId) && screens.containsKey(screenId) ) {
            InputScreenBase screen = screens.get(screenId);
            if ( screenAllowsScope(screen, scopeId) ) {
                InputScopeBase oldScope = currentScope;
                InputScopeBase scope = scopes.get(scopeId);
                try {
                    try {
                        if ( currentScope != null ) {
                            currentScope.deactivate();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPluginEx.class).warn("Failed to deactivate scope, ignoring: "+currentScope, t);
                    }
                    scope.activate(scopeArgs);
                    currentScope = scope;
                    try {
                        if ( currentScreen != null ) {
                            currentScreen.deactivate();
                            currentScreen.clearHandlers();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPluginEx.class).warn("Failed to deactivate screen, ignoring: "+currentScreen, t);
                    }
                    //horizontalAlignment = AlignmentHorizontal.right;
                    screen.activate(screenArgs);
                    currentScreen = screen;
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to activate scope skipping transition: "+scopeId, t);
                    currentScope = oldScope;
                }
                return true;
            } else {
                Global.getLogger(SSMSControllerModPluginEx.class).error("Screen \""+screenId+"\" is not allowed in scope \""+scopeId+"\"!");
            }
        } else {
            if ( !screens.containsKey(screenId) ) Global.getLogger(SSMSControllerModPluginEx.class).error("Screen \""+screenId+"\" is not registered!");
            if ( !scopes.containsKey(scopeId) ) Global.getLogger(SSMSControllerModPluginEx.class).error("Scope \""+scopeId+"\" is not registered!");
        }
        return false;
    }

    public void refreshIndicators() {
        if(displayPanel != null) {
            displayPanel.cleanup();
            displayPanel = null;
        }
    }
    
    public boolean isInScope(String scopeId) {
        return currentScope.getId().equals(scopeId);
    }
    
    public void startFrame() {
        if ( nextScreen != null ) {
            InputScreenBase screen = screens.get(nextScreen.id);
            try {
                //horizontalAlignment = AlignmentHorizontal.right;
                screen.activate(nextScreen.args);
                try {
                    if ( currentScreen!= null ) {
                        currentScreen.deactivate();
                        currentScreen.clearHandlers();
                    }
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPluginEx.class).warn("Failed to deactivate screen, ignoring: "+currentScreen);
                }
                currentScreen = screen;
            } catch ( Throwable t ) {
                Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to activate screen, skipping transition: "+nextScreen.id);
            }
            refreshIndicators();
            nextScreen = null;
        }
    }
    
    public void preInput(float advance) {
        currentScreen.preInput(advance);
    }

    public void processInputEvents(float advance, List<InputEventAPI> events) {
        currentScreen.processInputEvents(advance, events);
    }

    public void processControllerEvents(float advance, List<Pair<LogicalButtons, Boolean>> buttonEvents, List<Pair<AxisMapping, Float>> axisEvents) {
        currentScreen.processControllerEvents(advance, buttonEvents, axisEvents);
    }
    
    public void postIntput(float advance) {
        currentScreen.postInput(advance);
    }
    
    public void renderInWorld(ViewportAPI viewport) {
        currentScreen.renderInWorld(viewport);
    }
    
    public void renderUI(ViewportAPI viewport) {
        currentScreen.renderUI(viewport);
        renderIndicators(viewport);
    }
    
    private void renderIndicators(ViewportAPI viewport) {
        InputScreenBase screen = getCurrentScreen();
        if(displayPanel == null && screen != null && screen.getIndicators() != null && !screen.getIndicators().isEmpty()) {
            try {
                var mainPanel = screen.getPanelForIndicators();
                var displayPanelAlignment = screen.getIndicatorsAlignment();
                if(mainPanel != null) {
                    displayPanel = new IndicatorDisplayPanel(mainPanel, screen.getIndicators(), displayPanelAlignment);
                }
            } catch(IllegalArgumentException ex) {
                Global.getLogger(getClass()).fatal("Could not create the panel for displaying indicator sprites!", ex);
            }
        }
    }
    
    public void stopFrame() {
        
    }
}
