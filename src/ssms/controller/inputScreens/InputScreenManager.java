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
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
//import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.lwjgl.opengl.GL11;
import ssms.controller.*;
//import ssms.qol.ui.UIUtil;

import org.lazywizard.lazylib.ui.*;
import org.lazywizard.lazylib.ui.LazyFont.TextAlignment;
import org.lazywizard.lazylib.ui.LazyFont.TextAnchor;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenManager {
    static private volatile InputScreenManager instance;
    LazyFont defaultFont = null;
    //protected AlignmentHorizontal horizontalAlignment = AlignmentHorizontal.right;
    protected TextAlignment horizontaAlignment = TextAlignment.RIGHT;
    private Map<String,InputScreen> screens;//screen can belong to multiple scopes
    private Map<String,InputScope> scopes;
    protected Map<Indicators,SpriteAPI> indicatorSprites;
    private InputScreen currentScreen;
    private InputScope currentScope;
    private Transition nextScreen;
    private long indicatorTimeout;
    float textLineHeight;
    IndicatorDisplayPanel displayPanel;
    private class Transition {
        protected String id;
        protected Object[] args;

        private Transition(String id, Object[] args) {
            this.id = id;
            this.args = args;
        }
    }
    
    final public boolean updateIndicators() {

        HandlerController controller = SSMSControllerModPluginEx.controller;
        ControllerMapping mapping = controller != null ? controller.mapping : null;
        indicatorSprites.clear();
        boolean ok = true;
        for ( Indicators ind : Indicators.values() ) {
            SpriteAPI img = mapping != null ? mapping.indicators.get(ind): SSMSControllerModPluginEx.defaultIndicators.get(ind);
            if ( img != null ) {
                img.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                indicatorSprites.put(ind, img);
            } else {
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
        try {
            defaultFont = LazyFont.loadFont("graphics/fonts/insignia21LTaa.fnt");
            textLineHeight = defaultFont.createText("A").getHeight();
        } catch(FontException ex) {
            Global.getLogger(getClass()).log(Level.FATAL, "Failed to load insignia21LTaa.fnt! " + ex);
        }
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
    
    public InputScreen registerScreen(InputScreen screen) {
        return screens.put(screen.getId(), screen);
    }
    
    public InputScope registerScope(InputScope scope) {
        return scopes.put(scope.getId(), scope);
    }
    
    public InputScreen getCurrentScreen() {
        return currentScreen;
    }
    
    public InputScope getCurrentScope() {
        return currentScope;
    }
    
    public boolean transitionDelayed(String id, Object ...args) {
        if ( screens.containsKey(id) ) {
            var screen = screens.get(id);
            if ( screenAllowsScope(screen, currentScope.getId()) ) {
                nextScreen = new Transition(id,args);
                return true;
            } else Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Screen id \""+id+"\" is not allowed in current scope \""+currentScope.getId()+"\".");
        } else Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Unregistered sceen id \""+id+"\" ignoring transition.");
        return false;
    }
    
    private boolean screenAllowsScope(InputScreen screen, String scopeId) {
        String[] scopeIds = screen.getScopes();
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
        displayPanel = null;
        if ( scopes.containsKey(scopeId) ) {
            InputScope scope = scopes.get(scopeId);
            return transitionToScope(scopeId, args, scope.getDefaultScreen(), null);
        } else {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Scope \""+scopeId+"\" is not registered!");
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
                        Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to deactivate scope, ignoring: "+currentScope, t);
                    }
                    scope.activate(scopeArgs);
                    currentScope = scope;
                    try {
                        if ( currentScreen != null ) {
                            currentScreen.deactivate();
                        }
                    } catch ( Throwable t ) {
                        Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to deactivate screen, ignoring: "+currentScreen, t);
                    }
                    //horizontalAlignment = AlignmentHorizontal.right;
                    screen.activate(screenArgs);
                    currentScreen = screen;
                    refreshIndicatorTimeout();
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to activate scope skipping transition: "+scopeId, t);
                    currentScope = oldScope;
                }
                return true;
            } else {
                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Screen \""+screenId+"\" is not allowed in scope \""+scopeId+"\"!");
            }
        } else {
            if ( !screens.containsKey(screenId) ) Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Screen \""+screenId+"\" is not registered!");
            if ( !scopes.containsKey(scopeId) ) Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Scope \""+scopeId+"\" is not registered!");
        }
        return false;
    }
    
    public boolean isInScope(String scopeId) {
        return currentScope.getId().equals(scopeId);
    }
    
    public void startFrame() {
        if ( nextScreen != null ) {
            InputScreen screen = screens.get(nextScreen.id);
            try {
                //horizontalAlignment = AlignmentHorizontal.right;
                screen.activate(nextScreen.args);
                try {
                    if ( currentScreen != null ) {
                        currentScreen.deactivate();
                    }
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to deactivate screen, ignoring: "+currentScreen);
                }
                currentScreen = screen;
                refreshIndicatorTimeout();
            } catch ( Throwable t ) {
                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to activate screen, skipping transition: "+nextScreen.id);
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
        //if ( indicatorTimeout > System.currentTimeMillis() ) renderIndicators(viewport);
        renderIndicators(viewport);
    }
    
    public void refreshIndicatorTimeout() {
        indicatorTimeout = System.currentTimeMillis() + 10000;
    }
    
    private void renderIndicators(ViewportAPI viewport) {
        
        InputScreen screen = getCurrentScreen();
        if(screen != null) {
            if(displayPanel == null) {
                try {
                    displayPanel = new IndicatorDisplayPanel(screen.getIndicators());
                } catch(FontException | IllegalArgumentException ex) {
                    Global.getLogger(getClass()).fatal("Could not create the panel for displaying indicator sprites!", ex);
                }
            }
            if(displayPanel != null) {
                displayPanel.render(viewport.getAlphaMult());
            }
        }
//        List<Pair<Indicators, String>> indicators = screen.getIndicators();
//        if ( indicators == null || indicators.isEmpty() ) return;
//
//        float yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), xMax = 0;
//        xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth());
//
//        final float lineHeight = 25, spacing = 8f, textWidth = 200f;
//        float x = xMax - textWidth - lineHeight - spacing - spacing, y = yMin + indicators.size() * (lineHeight + spacing) + spacing;
//        for ( Pair<Indicators,String> e : indicators ) {
//            if ( e.one != null ) {
//                SpriteAPI sprite = indicatorSprites.get(e.one);
//                if ( sprite != null ) {
//                    sprite.setWidth(lineHeight);
//                    sprite.setHeight(lineHeight);
//                    sprite.render(x, y);
//                }
//                //UIUtil.getInstance().renderText(e.two, Color.white, x + spacing + lineHeight, y, textWidth, lineHeight, Alignment.LMID);
//
//                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth, lineHeight);
//                str.setAlignment(TextAlignment.LEFT);
//                str.setAnchor(TextAnchor.TOP_LEFT);
//                str.draw(x + spacing + lineHeight, y + textLineHeight + 2);
//                y -= lineHeight + spacing;
//            } else {
//                //UIUtil.getInstance().renderText(e.two, Color.white, x, y, textWidth + spacing + lineHeight, textLineHeight, Alignment.LMID);
//                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth + spacing + lineHeight, lineHeight);
//                str.setAlignment(TextAlignment.LEFT);
//                str.setAnchor(TextAnchor.CENTER_LEFT);
//                str.draw(x, y - textLineHeight);
//
//                y -= textLineHeight + spacing;
//            }
//        }
    }
    
    public void stopFrame() {
        
    }
}
