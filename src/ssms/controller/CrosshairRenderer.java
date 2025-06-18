package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

public class CrosshairRenderer {
    SpriteAPI crosshairSprite;
    Vector2f mousePos;
    public CrosshairRenderer() {
        crosshairSprite = Global.getSettings().getSprite("graphics/icons/crosshair.png");
        mousePos = new Vector2f(0.f, 0.f);
    }

    public void setSize(float x, float y) {
        crosshairSprite.setSize(x, y);
    }

    public void setMousePos(float x, float y) {
        mousePos.set(x, y);
    }

    public void render() {
        crosshairSprite.render(mousePos.getX() - (crosshairSprite.getWidth() / 2), mousePos.getY() + (crosshairSprite.getHeight() / 2));
    }
}
