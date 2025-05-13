package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;

public class ControllerCrosshairRenderer {

    static SpriteAPI indicTL = Global.getSettings().getSprite("ui","sortIcon");
    static SpriteAPI indicTR = Global.getSettings().getSprite("ui","sortIcon");
    static SpriteAPI indicBL = Global.getSettings().getSprite("ui","sortIcon");
    static SpriteAPI indicBR = Global.getSettings().getSprite("ui","sortIcon");
    static Color indicColor = new Color(0xFFD200);
    static int width = 58;
    static final int maxWidth = 58;
    static boolean shouldDraw = true;
    static float origWidth = indicTL.getWidth(), origHeight = indicTL.getHeight();

    static boolean init = false;

    public static void setSize(int width) {
        ControllerCrosshairRenderer.width = width;
        float ratio = width / (float)maxWidth;
        indicTL.setSize(origWidth * ratio / 1.5f, origHeight * ratio / 1.5f);
        indicTR.setSize(indicTL.getWidth(), indicTL.getHeight());
        indicBL.setSize(indicTL.getWidth(), indicTL.getHeight());
        indicBR.setSize(indicTL.getWidth(), indicTL.getHeight());
    }

    public static void disable() {
        shouldDraw = false;
    }

    public static void enable() {
        shouldDraw = true;
    }

    public static void setColors()
    {
        indicTL.setColor(indicColor);
        indicTR.setColor(indicColor);
        indicBL.setColor(indicColor);
        indicBR.setColor(indicColor);
    }

    public static void AttemptRender(ViewportAPI viewport, int mouseX, int mouseY)
    {
        if(!shouldDraw) {
            return;
        }
        if (!init)
        {
            init = true;
            indicTL.setSize(origWidth / 1.5f, origHeight / 1.5f);
            indicTR.setSize(indicTL.getWidth(), indicTL.getHeight());
            indicBL.setSize(indicTL.getWidth(), indicTL.getHeight());
            indicBR.setSize(indicTL.getWidth(), indicTL.getHeight());
            indicTL.setAngle(45);
            indicTR.setAngle(-45);
            indicBL.setAngle(135);
            indicBR.setAngle(225);
        }

        setColors();
        float x = mouseX - (width / 2.f);
        float y = mouseY + (width / 2.f);
        indicTL.render(x, y);
        indicTR.render(x+width, y);
        indicBL.render(x, y-width);
        indicBR.render(x+width, y-width);
    }
}
