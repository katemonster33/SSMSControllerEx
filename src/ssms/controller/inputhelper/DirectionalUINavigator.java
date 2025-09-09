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
import java.util.function.Function;

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

    public float getDeltaX(PositionAPI a, PositionAPI b) {
       float ax2 = a.getX() + a.getWidth(), bx2 = b.getX() + b.getWidth();
        if ((a.getX() >= b.getX() && a.getX() < bx2) ||
                (ax2 > b.getX() && ax2 <= bx2) ||
                (b.getX() >= a.getX() && b.getX() < ax2) ||
                (bx2 > a.getX() && bx2 <= ax2)) {
            return 0;
        } else if (ax2 <= b.getX()) {
            return b.getCenterX() - a.getCenterX();
        } else {
            return -1;
        }
    }

    public float getDeltaY(PositionAPI a, PositionAPI b) {
        float ay2 = a.getY() + a.getHeight(), by2 = b.getY() + b.getHeight();
        if ((a.getY() >= b.getY() && a.getY() < by2) ||
                (ay2 > b.getY() && ay2 <= by2) ||
                (b.getY() >= a.getY() && b.getY() < ay2) ||
                (by2 > a.getY() && by2 <= ay2)) {
            return 0;
        } else if (ay2 <= b.getY()) {
            return b.getCenterY() - a.getCenterY();
        } else {
            return -1;
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

    public void moveSelection(Comparator<PositionAPI> getDeltaA, Comparator<PositionAPI> getDeltaB) {
        if(curIndex == -1) {
            if(!navigationObjects.isEmpty()) {
                curIndex = 0;
                onSelect(navigationObjects.get(0));
            }
            return;
        }
        UIComponentAPI selectedComp = navigationObjects.get(curIndex).one;
        Pair<UIComponentAPI, Object> closestPairPrime = null, closestPairSecondary = null;
        float distPrime = Float.MAX_VALUE, distSecondary = Float.MAX_VALUE;
        int tmpIndex = -1, newIndexPrime = -1, newIndexSecondary = -1;
        for (Pair<UIComponentAPI, Object> other : navigationObjects) {
            ++tmpIndex;
            if (other.one == selectedComp) {
                continue;
            }
            float deltaA = getDeltaA.compare(selectedComp.getPosition(), other.one.getPosition());
            float deltaB = getDeltaB.compare(selectedComp.getPosition(), other.one.getPosition());
            if(deltaA > 0) {
                if (deltaB == 0) {
                    if (deltaA < distPrime) {
                        distPrime = deltaA;
                        closestPairPrime = other;
                        newIndexPrime = tmpIndex;
                    }
                } else {
                    float dist = (float) Math.sqrt(Math.pow(Math.abs(deltaA), 2) +
                            Math.pow(Math.abs(deltaB), 2));
                    if (dist < distSecondary) {
                        distSecondary = dist;
                        closestPairSecondary = other;
                        newIndexSecondary = tmpIndex;
                    }
                }
            } else if(deltaA == 0 && deltaB == 0) {
                float distBackup = (float) Math.sqrt(Math.pow(Math.abs(other.one.getPosition().getCenterX() - selectedComp.getPosition().getCenterX()), 2) +
                        Math.pow(Math.abs(other.one.getPosition().getCenterY() - selectedComp.getPosition().getCenterY()), 2));
                if (distBackup < distPrime) {
                    distPrime = distBackup;
                    closestPairPrime = other;
                    newIndexPrime = tmpIndex;
                }
            }
        }

        if (closestPairPrime != null) {
            curIndex = newIndexPrime;
            onSelect(closestPairPrime);
        } else if (closestPairSecondary != null) {
            curIndex = newIndexSecondary;
            onSelect(closestPairSecondary);
        }
    }

    public void performLeftAction(float advance) {
        moveSelection((PositionAPI orig, PositionAPI other) -> (int) getDeltaX(other, orig),
                (PositionAPI orig, PositionAPI other) -> (int) getDeltaY(orig, other));
    }

    public void performRightAction(float advance) {
        moveSelection((PositionAPI orig, PositionAPI other) -> (int) getDeltaX(orig, other),
                (PositionAPI orig, PositionAPI other) -> (int) getDeltaY(orig, other));
    }

    public void performUpAction(float advance) {
        moveSelection((PositionAPI orig, PositionAPI other) -> (int) getDeltaY(orig, other),
                (PositionAPI orig, PositionAPI other) -> (int) getDeltaX(orig, other));
    }

    public void performDownAction(float advance) {
        moveSelection((PositionAPI orig, PositionAPI other) -> (int) getDeltaY(other, orig),
                (PositionAPI orig, PositionAPI other) -> (int) getDeltaX(orig, other));
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
