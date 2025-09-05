package ssms.controller.inputhelper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.ControllerCrosshairRenderer;
import ssms.controller.InputShim;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.Collections;

public class DirectionalUINavigator implements DigitalJoystickHandler {
    List<Pair<UIComponentAPI, Object>> navigationObjects;
    float selectedItemX, selectedItemY;
    int curIndex = -1;
    DirectionalUiReticle reticle;
    public DirectionalUINavigator(List<Pair<UIComponentAPI, Object>> navigationObjects)
    {
        this.navigationObjects = new ArrayList<>(navigationObjects);
        if(!navigationObjects.isEmpty()) {
            curIndex = 0;
            onSelect(navigationObjects.get(curIndex));
        }
        reticle = new DirectionalUiReticle();
    }

    public void onSelect(Pair<UIComponentAPI, Object> selectedPair) {
        PositionAPI pos = selectedPair.one.getPosition();
        selectedItemX = pos.getX();
        selectedItemY = pos.getY();
        InputShim.mouseMove((int) pos.getCenterX(),(int) pos.getCenterY());
    }

    void moveSelection(Comparator<UIComponentAPI> comparator) {
        if(navigationObjects.isEmpty()) {
            return;
        }
        var newPair = getClosest(comparator);
        if(newPair != null) {
            onSelect(newPair);
        }
    }

    public void setNavigationObjects(List<Pair<UIComponentAPI, Object>> navigationObjects) {
        PositionAPI curObjPos = null;
        if (curIndex != -1 && curIndex < navigationObjects.size()) {
            curObjPos = navigationObjects.get(curIndex).one.getPosition();
        }
        this.navigationObjects = new ArrayList<>(navigationObjects);

        if (navigationObjects.isEmpty()) {
            curIndex = -1;
            return;
        }
        if (curObjPos != null) {
            for (curIndex = 0; curIndex < navigationObjects.size(); curIndex++) {
                UIComponentAPI comp = navigationObjects.get(curIndex).one;
                if (comp.getPosition().getCenterX() == curObjPos.getCenterX() && comp.getPosition().getCenterY() == curObjPos.getCenterY()) {
                    break;
                }
            }
            if (curIndex >= navigationObjects.size()) {
                curIndex = 0;
                onSelect(navigationObjects.get(curIndex));
            }
        } else {
            curIndex = 0;
            onSelect(navigationObjects.get(curIndex));
        }
    }

    Pair<UIComponentAPI, Object> getClosest(Comparator<UIComponentAPI> comparator) {
        UIComponentAPI selectedComp = navigationObjects.get(curIndex).one;
        int minComparison = Integer.MAX_VALUE;
        Pair<UIComponentAPI, Object> closestPair = null;
        int newIndex = -1;
        for (Pair<UIComponentAPI, Object> other : navigationObjects) {
            ++newIndex;
            if(other.one.equals(selectedComp)) {
                continue;
            }
            int compVal = comparator.compare(selectedComp, other.one);
            if(compVal < minComparison && compVal > 0) {
                curIndex = newIndex;
                minComparison = compVal;
                closestPair = other;
            }
        }
        return closestPair;
    }

    float getDeltaOrOne(float a, float b) {
        return Math.max(1.f, Math.abs(a - b));
    }

    float getLeastDeltaOrOne(float a1, float a2, float b1, float b2) {
        if((a1 >= b1 && a1 <= b2) ||
            (a2 >= b1 && a2 <= b2)) {
            return 1;
        } else {
            float delta = Math.min(Math.min(Math.abs(a1 - b1), Math.abs((a1 - b2))),
                    Math.min(Math.abs(a2 - b1), Math.abs((a2 - b2))));
            delta += 1;
            return delta;
        }
    }

    public void performLeftAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o1.getPosition().getCenterX() - o2.getPosition().getCenterX()) *
                        getLeastDeltaOrOne(o1.getPosition().getY(), o1.getPosition().getY() + o1.getPosition().getHeight(),
                                o2.getPosition().getY(), o2.getPosition().getY() + o2.getPosition().getHeight())));
    }

    public void performRightAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o2.getPosition().getCenterX() - o1.getPosition().getCenterX()) *
                        getLeastDeltaOrOne(o1.getPosition().getY(), o1.getPosition().getY() + o1.getPosition().getHeight(),
                                o2.getPosition().getY(), o2.getPosition().getY() + o2.getPosition().getHeight())));
    }

    public void performUpAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o2.getPosition().getCenterY() - o1.getPosition().getCenterY()) *
                        getLeastDeltaOrOne(o1.getPosition().getX(), o1.getPosition().getX() + o1.getPosition().getWidth(),
                                o2.getPosition().getX(), o2.getPosition().getX() + o2.getPosition().getWidth())));
    }

    public void performDownAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o1.getPosition().getCenterY() - o2.getPosition().getCenterY()) *
                        getLeastDeltaOrOne(o1.getPosition().getX(), o1.getPosition().getX() + o1.getPosition().getWidth(),
                                o2.getPosition().getX(), o2.getPosition().getX() + o2.getPosition().getWidth())));
    }

    public Pair<UIComponentAPI, Object> getSelected() {
        if(curIndex != -1 && curIndex < navigationObjects.size()) {
            return navigationObjects.get(curIndex);
        } else {
            return null;
        }
    }

    public void advance(float amount) {
        if(curIndex != -1 && InputShim.hasMouseControl()) {
            var selected = getSelected();
            var selectedCompPos = selected.one.getPosition();
            if(selectedCompPos.getX() != selectedItemX || selectedCompPos.getY() != selectedItemY) {
                onSelect(selected);
            }
        }
    }

    public void render() {
        if(curIndex != -1 && InputShim.hasMouseControl()) {
            var selected = getSelected();
            var selectedCompPos = selected.one.getPosition();
            reticle.render(selectedCompPos);
        }
    }


    public static class DirectionalUiReticle {
        SpriteAPI indicTL = Global.getSettings().getSprite("ui","sortIcon");
        SpriteAPI indicTR = Global.getSettings().getSprite("ui","sortIcon");
        SpriteAPI indicBL = Global.getSettings().getSprite("ui","sortIcon");
        SpriteAPI indicBR = Global.getSettings().getSprite("ui","sortIcon");
        Color indicColor = new Color(0xFFD200);

        public DirectionalUiReticle() {
            indicTL.setSize(20, 20);
            indicTR.setSize(20, 20);
            indicBL.setSize(20, 20);
            indicBR.setSize(20, 20);
            indicTL.setAngle(45);
            indicTR.setAngle(-45);
            indicBL.setAngle(135);
            indicBR.setAngle(225);
            indicTL.setColor(indicColor);
            indicTR.setColor(indicColor);
            indicBL.setColor(indicColor);
            indicBR.setColor(indicColor);
        }

        public void render(PositionAPI pos) {
            float x1 = pos.getX() - indicTL.getWidth(), x2 = pos.getX() + pos.getWidth();
            float y1 = pos.getY() + pos.getHeight(), y2 = pos.getY() - indicTL.getHeight();
            indicTL.render(x1, y1);
            indicTR.render(x2, y1);
            indicBL.render(x1, y2);
            indicBR.render(x2, y2);
        }
    }

}
