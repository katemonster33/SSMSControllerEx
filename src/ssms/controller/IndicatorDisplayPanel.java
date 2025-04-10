package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;

import lunalib.lunaUI.elements.LunaElement;
import lunalib.lunaUI.elements.LunaSpriteElement;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;

import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.inputScreens.InputScreen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class IndicatorDisplayPanel extends LunaBaseCustomPanelPlugin {
    static LazyFont defaultFont;
    static float textLineHeight;

    final float lineHeight = 25, spacing = 8f, textWidth = 200f;
    protected List<Pair<Indicators, String>> indicators;
    List<Pair<SpriteAPI, Vector2f>> sprites;
    CustomPanelAPI subpanel;
    List<Pair<LazyFont.DrawableString, Vector2f>> indicatorTexts;
    public IndicatorDisplayPanel(List<Pair<Indicators, String>> args) throws FontException, IllegalArgumentException {
        indicators = new ArrayList<>(args);
        if(defaultFont == null) {
            defaultFont = LazyFont.loadFont("graphics/fonts/insignia21LTaa.fnt");
            textLineHeight = defaultFont.createText("A").getHeight();
        }

        if ( indicators == null || indicators.isEmpty() ) {
            throw new IllegalArgumentException("indicators list cannot be empty!");
        }
        subpanel = Global.getSettings().createCustom(400, 25 * indicators.size(), null);

        //subpanel.getPosition().rightOfMid(null, 0);
        var viewport = Global.getSector().getViewport();
        float yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth());
        //float y = subpanel.getPosition().getY(), x = subpanel.getPosition().getX();
        
        float x = xMax - textWidth - lineHeight - spacing - spacing, y = yMin + indicators.size() * (lineHeight + spacing) + spacing;
        
        subpanel.getPosition().setLocation(x, yMin);
        subpanel.getPosition().setSize(400, 40 * indicators.size());
        var rectElem = subpanel.createUIElement(400, 25 * indicators.size(), false);
        rectElem.getPosition().setLocation(x, y);
        rectElem.getPosition().setSize(400, 25 * indicators.size());
        var rect = rectElem.createRect(Global.getSettings().getDarkPlayerColor(), 5.f);
        rect.getPosition().setLocation(x, y);
        rect.getPosition().setSize(400, 25 * indicators.size());
        //rect.getPosition().setLocation(yMin, xMax);
        subpanel.addComponent(rect);

        sprites = new ArrayList<>();
        indicatorTexts = new ArrayList<>();
        for ( Pair<Indicators,String> e : indicators ) {
            if ( e.one != null ) {
                SpriteAPI sprite = SSMSControllerModPluginEx.defaultIndicators.get(e.one);
                if ( sprite != null ) {
                    //sprite.render(x, y);
                    sprites.add(new Pair<>(sprite, new Vector2f(x, y)));
                }
                //UIUtil.getInstance().renderText(e.two, Color.white, x + spacing + lineHeight, y, textWidth, lineHeight, Alignment.LMID);

                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth, lineHeight);
                str.setAlignment(LazyFont.TextAlignment.LEFT);
                str.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
                indicatorTexts.add(new Pair<>(str, new Vector2f(x + spacing + lineHeight, y + textLineHeight + 2)));
                y -= lineHeight + spacing;
            } else {
                LunaSpriteElement
                //UIUtil.getInstance().renderText(e.two, Color.white, x, y, textWidth + spacing + lineHeight, textLineHeight, Alignment.LMID);
                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth + spacing + lineHeight, lineHeight);
                str.setAlignment(LazyFont.TextAlignment.LEFT);
                str.setAnchor(LazyFont.TextAnchor.CENTER_LEFT);
                //str.draw(x, y - textLineHeight);
                indicatorTexts.add(new Pair<>(str, new Vector2f(x, y - textLineHeight)));

                y -= textLineHeight + spacing;
            }
        }
        // LunaElement elem  = new LunaElement(null, x, y);
        // elem.addText(null, null, null, null);
        initFromScript(subpanel);
    }
    // @Override
    // public void positionChanged(PositionAPI position) {

    // }

    // @Override
    // public void renderBelow(float alphaMult) {

    // }

    @Override
    public void render(float alphaMult) {
        //subpanel.render(alphaMult);
        for(var spritePair  : sprites) {
            spritePair.one.setWidth(lineHeight);
            spritePair.one.setHeight(lineHeight);
            spritePair.one.render(spritePair.two.x, spritePair.two.y);
        }
        for(var strPair : indicatorTexts) {
            strPair.one.draw(strPair.two);
        }
        super.render(alphaMult);
    }

    // @Override
    // public void advance(float amount) {

    // }

    // @Override
    // public void processInput(List<InputEventAPI> events) {

    // }
    
    @Override
    public void init() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'init'");
    }
}
