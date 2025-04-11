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
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.inputScreens.InputScreen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class IndicatorDisplayPanel extends LunaBaseCustomPanelPlugin {
    static LazyFont defaultFont;
    static float textLineHeight;
    LunaElement rect;

    final float lineHeight = 25, spacing = 8f, textWidth = 200f;
    CustomPanelAPI subpanel;
    List<Pair<LazyFont.DrawableString, SpriteAPI>> indicators;
    public IndicatorDisplayPanel(List<Pair<Indicators, String>> args) throws FontException, IllegalArgumentException {
        if(defaultFont == null) {
            defaultFont = LazyFont.loadFont("graphics/fonts/insignia21LTaa.fnt");
            textLineHeight = defaultFont.createText("A").getHeight();
        }

        if ( args == null || args.isEmpty() ) {
            throw new IllegalArgumentException("indicators list cannot be empty!");
        }
        indicators = new ArrayList<>();
        subpanel =  Global.getSettings().createCustom(400, 25 * args.size(), this);

        initFromScript(subpanel);
        //Global.getSettings().cre
        //subpanel.getPosition().rightOfMid(null, 0);
        var viewport = Global.getSector().getViewport();
        float yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth());
        //float y = subpanel.getPosition().getY(), x = subpanel.getPosition().getX();
        
        float x = xMax - textWidth - lineHeight - spacing - spacing, y = yMin + args.size() * (lineHeight + spacing) + spacing;
        subpanel.getPosition().setLocation(x - spacing, yMin + lineHeight);
        subpanel.getPosition().setSize(xMax - x + spacing, y - yMin + spacing);
        for ( Pair<Indicators,String> e : args ) {
            if ( e.one != null ) {
                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth, lineHeight);
                str.setAlignment(LazyFont.TextAlignment.LEFT);
                str.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
                indicators.add(new Pair<>(str, SSMSControllerModPluginEx.defaultIndicators.get(e.one)));
            } else {
                var str = defaultFont.createText(e.two, Color.white, textLineHeight, textWidth + spacing + lineHeight, lineHeight);
                str.setAlignment(LazyFont.TextAlignment.LEFT);
                str.setAnchor(LazyFont.TextAnchor.CENTER_LEFT);
                indicators.add(new Pair<>(str, null));
            }
        }
    }
    // @Override
    // public void positionChanged(PositionAPI position) {

    // }

     @Override
     public void renderBelow(float alphaMult) {
        //super.renderBelow(alphaMult);
        //rect.renderBelow(alphaMult);
     }

    @Override
    public void render(float alphaMult) {
        //subpanel.render(alphaMult);
        GL11.glPushMatrix();

        GL11.glTranslatef(0f, 0f, 0f);
        GL11.glRotatef(0f, 0f, 0f, 1f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        Color c = Global.getSettings().getDarkPlayerColor().darker().darker();
        GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 0.8f * alphaMult);
        GL11.glBegin(GL11.GL_TRIANGLES);
        float minX = subpanel.getPosition().getX(), minY = subpanel.getPosition().getY(),
                maxX = minX + subpanel.getPosition().getWidth(), maxY = minY + subpanel.getPosition().getHeight();
        GL11.glVertex2f(minX, maxY);
        GL11.glVertex2f(minX, minY);
        GL11.glVertex2f(maxX, maxY);
        GL11.glVertex2f(maxX, maxY);
        GL11.glVertex2f(minX, minY);
        GL11.glVertex2f(maxX, minY);
        GL11.glEnd();
        GL11.glPopMatrix();

        float x = subpanel.getPosition().getX() + spacing, y = subpanel.getPosition().getY() + subpanel.getPosition().getHeight() - spacing - lineHeight;
        for ( var indicator : indicators ) {
            if ( indicator.two != null ) {
                indicator.two.setHeight(lineHeight);
                indicator.two.setWidth(lineHeight);
                indicator.two.render(x, y);
            }
            indicator.one.draw(x + spacing + lineHeight, y + textLineHeight + 2);
            y -= lineHeight + spacing;
        }
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
