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

import com.fs.graphics.Sprite;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.lwjgl.opengl.GL11;
import ssms.controller.ControllerMapping;
import ssms.controller.HandlerController;
import ssms.controller.SSMSControllerModPlugin;
import static ssms.controller.SSMSControllerModPlugin.defaultIndicators;
import ssms.controller.UtilObfuscation;
import ssms.qol.ui.UIUtil;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenManager {
    static private volatile InputScreenManager instance;
    private Map<String,InputScreen> screens;//screen can belong to multiple scopes
    private Map<String,InputScope> scopes;
    private Map<Indicators,Sprite> indicatorSprites;
    private InputScreen currentScreen;
    private InputScope currentScope;
    private Transition nextScreen;
    private long indicatorTimeout;
    private class Transition {
        protected String id;
        protected Object[] args;

        private Transition(String id, Object[] args) {
            this.id = id;
            this.args = args;
        }
    }
    
    final public boolean updateIndicators() {
        HandlerController controller = SSMSControllerModPlugin.controller;
        ControllerMapping mapping = controller != null ? controller.mapping : null;
        indicatorSprites.clear();
        boolean ok = true;
        for ( Indicators ind : Indicators.values() ) {
            String id = "Indicator "+ind.name();
            String img = mapping != null ? mapping.indicators.get(ind) : null;
            if ( img != null ) {
                img = "graphics/indicators/" + img;
                ok = UtilObfuscation.RegisterTextureWithID(id, img);
                Sprite sprite = new Sprite(id);
                sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                indicatorSprites.put(ind, sprite);
            } else {
                UtilObfuscation.TryRemoveTextureWithID(id);
                indicatorSprites.put(ind, null);
            }
        }
        
        return ok;
    }
    
    private InputScreenManager() {
        screens = new HashMap<>();
        scopes = new HashMap<>();
        currentScope = new InputScope_360(); currentScope.activate();
        currentScreen = new InputScreen_Bluescreen(); currentScreen.activate();
        indicatorSprites = new EnumMap<>(Indicators.class);
        updateIndicators();
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
    
    public InputScreen registerScreen(Class<? extends InputScreen> screen) {
        InputScreenOption_ID annoId = screen.getAnnotation(InputScreenOption_ID.class);
        if ( annoId != null ) {
            try {
                return screens.put(annoId.value(),screen.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to create instance for input screen class: "+screen, ex);
            }
        } else Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Class for input screen is missing id annoation: "+screen);
        
        return null;
    }
    
    public InputScope registerScope(Class<? extends InputScope> scope) {
        InputScopeOption_ID annoId = scope.getAnnotation(InputScopeOption_ID.class);
        if ( annoId != null && annoId.value() != null ) {
            if ( scope.getAnnotation(InputScopeOption_DefaultScreen.class) != null ) {
                try {
                    return scopes.put(annoId.value(),scope.newInstance());
                } catch (InstantiationException | IllegalAccessException ex) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to create instance for input scope class: "+scope, ex);
                }
            } else Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Class for input scope is missing default screen annoation: "+scope);
        } else Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Class for input scope is missing id annoation: "+scope);
        
        return null;
    }
    
    public boolean isScreenIdRegistered(Class<? extends InputScreen> screen) {
        InputScreenOption_ID annoId = screen.getAnnotation(InputScreenOption_ID.class);
        if ( annoId != null ) {
            return screens.containsKey(annoId.value());
        }
        return false;
    }
    
    public boolean isScopeIdRegistered(Class<? extends InputScope> scope) {
        InputScopeOption_ID annoId = scope.getAnnotation(InputScopeOption_ID.class);
        if ( annoId != null ) {
            return scopes.containsKey(annoId.value());
        }
        return false;
    }
    
    public InputScreen getCurrentScreen() {
        return currentScreen;
    }
    
    public InputScope getCurrentScope() {
        return currentScope;
    }
    
    public boolean transitionDelayed(String id, Object ...args) {
        if ( screens.containsKey(id) ) {
            if ( screenAllowsScope(screens.get(id), currentScope.getClass().getAnnotation(InputScopeOption_ID.class).value()) ) {
                nextScreen = new Transition(id,args);
                return true;
            } else Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Screen id \""+id+"\" is not allowed in current scope \""+currentScope.getClass().getAnnotation(InputScopeOption_ID.class).value()+"\".");
        } else Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Unregistered sceen id \""+id+"\" ignoring transition.");
        return false;
    }
    
    private boolean screenAllowsScope(InputScreen screen, String scopeId) {
        String[] scopeIds = screen.getClass().getAnnotation(InputScreenOption_Scopes.class).value();
        if ( scopeIds == null ) return true;
        for ( String scopeIdAllowed : scopeIds ) {
            if (scopeIdAllowed.equals(scopeId)) {
                return true;
            }
        }
        return false;
    }
    
    public InputScope getScope(String scopeId) {
        return scopes.get(scopeId);
    }
    
    public InputScreen getScreen(String screenId) {
        return screens.get(screenId);
    }
    
    public boolean transitionToScope(String scopeId, Object ...args) {
        //moving to scope sets the screen id for the input screen that has the initial annoation and a matching scope
        //scope can hold variables like the engine for combat
        //transitions into the same scope are legal, they happen if no other scope is active between two seperate scope entries
        if ( scopes.containsKey(scopeId) ) {
            InputScope scope = scopes.get(scopeId);
            return transitionToScope(scopeId, args, scope.getClass().getAnnotation(InputScopeOption_DefaultScreen.class).value(), null);
        } else {
            Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Scope \""+scopeId+"\" is not registered!");
        }
        return false;
    }
    
    public boolean transitionToScope(String scopeId, Object[] scopeArgs, String screenId, Object[] screenArgs) {
        if ( scopes.containsKey(scopeId) && screens.containsKey(screenId) ) {
            InputScreen screen = screens.get(screenId);
            if ( screenAllowsScope(screen, scopeId) ) {
                InputScope oldScope = currentScope;
                InputScope scope = scopes.get(scopeId);
                try {
                    try {
                        if ( currentScope != null ) {
                            currentScope.deactivate();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPlugin.class).log(Level.WARN, "Failed to deactivate scope, ignoring: "+currentScope, t);
                    }
                    scope.activate(scopeArgs);
                    currentScope = scope;
                    try {
                        if ( currentScreen != null ) {
                            currentScreen.deactivate();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPlugin.class).log(Level.WARN, "Failed to deactivate screen, ignoring: "+currentScreen, t);
                    }
                    screen.activate(screenArgs);
                    currentScreen = screen;
                    refreshIndicatorTimeout();
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to activate scope skipping transition: "+scopeId, t);
                    currentScope = oldScope;
                }
                return true;
            } else {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Screen \""+screenId+"\" is not allowed in scope \""+scopeId+"\"!");
            }
        } else {
            if ( !screens.containsKey(screenId) ) Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Screen \""+screenId+"\" is not registered!");
            if ( !scopes.containsKey(scopeId) ) Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Scope \""+scopeId+"\" is not registered!");
        }
        return false;
    }
    
    public boolean isInScope(String scopeId) {
        return currentScope.getClass().getAnnotation(InputScopeOption_ID.class).value().equals(scopeId);
    }
    
    public void startFrame() {
        if ( nextScreen != null ) {
            InputScreen screen = screens.get(nextScreen.id);
            try {
                screen.activate(nextScreen.args);
                try {
                    if ( currentScreen != null ) {
                        currentScreen.deactivate();
                    }
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.WARN, "Failed to deactivate screen, ignoring: "+currentScreen);
                }
                currentScreen = screen;
                refreshIndicatorTimeout();
            } catch ( Throwable t ) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to activate screen, skipping transition: "+nextScreen.id);
            }
            nextScreen = null;
        }
    }
    
    public void preInput(float advance) {
        currentScreen.preInput(advance);
    }
    
    public void postIntput(float advance) {
        currentScreen.postInput(advance);
    }
    
    public void renderInWorld(ViewportAPI viewport) {
        currentScreen.renderInWorld(viewport);
    }
    
    public void renderUI(ViewportAPI viewport) {
        currentScreen.renderUI(viewport);
        if ( indicatorTimeout > System.currentTimeMillis() ) renderIndicators(viewport);
    }
    
    public void refreshIndicatorTimeout() {
        indicatorTimeout = System.currentTimeMillis() + 10000;
    }
    
    private void renderIndicators(ViewportAPI viewport) {
        InputScreen screen = getCurrentScreen();
        List<Pair<Indicators, String>> indicators = screen.getIndicators();
        if ( indicators == null || indicators.isEmpty() ) return;
        
        float yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), 
            xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth());
        
        final float lineHeight = 25, textLineHeight = UIUtil.getInstance().getTextHeight("A"), spacing = 8f, textWidth = 200f;
        float x = xMax - textWidth - lineHeight - spacing - spacing, y = yMin + indicators.size() * (lineHeight + spacing) + spacing;
        for ( Pair<Indicators,String> e : indicators ) {
            if ( e.one != null ) {
                Sprite sprite = indicatorSprites.get(e.one);
                if ( sprite != null ) {
                    sprite.setWidth(lineHeight);
                    sprite.setHeight(lineHeight);
                    sprite.render(x, y);
                }
                UIUtil.getInstance().renderText(e.two, Color.white, x + spacing + lineHeight, y, textWidth, lineHeight, Alignment.LMID);
                y -= lineHeight + spacing;
            } else {
                UIUtil.getInstance().renderText(e.two, Color.white, x, y, textWidth + spacing + lineHeight, textLineHeight, Alignment.LMID);
                y -= textLineHeight + spacing;
            }
        }
    }
    
    public void stopFrame() {
        
    }
}
