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
    List<NavigationObject> navigationObjects;
    float selectedItemX, selectedItemY;
    int curIndex = -1;
    DirectionalUiReticle reticle;
    public DirectionalUINavigator(List<NavigationObject> navigationObjects)
    {
        setNavigationObjects(navigationObjects);
        reticle = new DirectionalUiReticle();
    }

    public void onSelect(NavigationObject selectedNav) {
        selectedItemX = selectedNav.x1;
        selectedItemY = selectedNav.y1;
        InputShim.mouseMove((int) selectedNav.getCenterX(),(int) selectedNav.getCenterY());
    }

    public float getDeltaX(NavigationObject a, NavigationObject b) {
        if ((a.x1 >= b.x1 && a.x1 < b.x2) ||
                (a.x2 > b.x1 && a.x2 <= b.x2) ||
                (b.x1 >= a.x1 && b.x1 < a.x2) ||
                (b.x2 > a.x1 && b.x2 <= a.x2)) {
            return 0;
        } else if (a.x2 <= b.x1) {
            return b.getCenterX() - a.getCenterX();
        } else {
            return -1;
        }
    }

    public float getDeltaY(NavigationObject a, NavigationObject b) {
        if ((a.y1 >= b.y1 && a.y1 < b.y2) ||
                (a.y2 > b.y1 && a.y2 <= b.y2) ||
                (b.y1 >= a.y2 && b.y1 < a.y2) ||
                (b.y2 > a.y1 && b.y2 <= a.y2)) {
            return 0;
        } else if (a.y2 <= b.y1) {
            return ((b.y1 + b.y2) / 2) - a.getCenterY();
        } else {
            return -1;
        }
    }

    public void setNavigationObjects(List<NavigationObject> navigationObjects) {
        this.navigationObjects = new ArrayList<>(navigationObjects);

        if (navigationObjects.isEmpty()) {
            curIndex = -1;
            return;
        }
        NavigationObject curObj = null;
        if (curIndex != -1 && curIndex < navigationObjects.size()) {
            curObj = navigationObjects.get(curIndex);
        }
        if (curObj != null) {
            for (curIndex = 0; curIndex < navigationObjects.size(); curIndex++) {
                NavigationObject obj = navigationObjects.get(curIndex);
                if (obj.getCenterX() == curObj.getCenterX() && obj.getCenterY() == curObj.getCenterY()) {
                    break;
                }
            }
            if (curIndex >= navigationObjects.size()) {
                curIndex = 0;
                onSelect(this.navigationObjects.get(curIndex));
            }
        } else {
            curIndex = 0;
            onSelect(this.navigationObjects.get(curIndex));
        }
    }

    public void moveSelection(Comparator<NavigationObject> getDeltaA, Comparator<NavigationObject> getDeltaB) {
        if(curIndex == -1) {
            if(!navigationObjects.isEmpty()) {
                curIndex = 0;
                onSelect(navigationObjects.get(0));
            }
            return;
        }
        NavigationObject selectedObj = navigationObjects.get(curIndex);
        NavigationObject closestNavObj = null, closestNavObjSecondary = null;
        float distPrime = Float.MAX_VALUE, distSecondary = Float.MAX_VALUE;
        int tmpIndex = -1, newIndexPrime = -1, newIndexSecondary = -1;
        for (var other : navigationObjects) {
            ++tmpIndex;
            if (other == selectedObj) {
                continue;
            }
            float deltaA = getDeltaA.compare(selectedObj, other);
            float deltaB = getDeltaB.compare(selectedObj, other);
            if(deltaA > 0) {
                if (deltaB == 0) {
                    if (deltaA < distPrime) {
                        distPrime = deltaA;
                        closestNavObj = other;
                        newIndexPrime = tmpIndex;
                    }
                } else {
                    float dist = (float) Math.sqrt(Math.pow(Math.abs(deltaA), 2) +
                            Math.pow(Math.abs(deltaB), 2));
                    if (dist < distSecondary) {
                        distSecondary = dist;
                        closestNavObjSecondary = other;
                        newIndexSecondary = tmpIndex;
                    }
                }
            } else if(deltaA == 0 && deltaB == 0) {
                float distBackup = (float) Math.sqrt(Math.pow(Math.abs(other.getCenterX() - selectedObj.getCenterX()), 2) +
                        Math.pow(Math.abs(other.getCenterY() - selectedObj.getCenterY()), 2));
                if (distBackup < distPrime) {
                    distPrime = distBackup;
                    closestNavObj = other;
                    newIndexPrime = tmpIndex;
                }
            }
        }

        if (closestNavObj != null) {
            curIndex = newIndexPrime;
            onSelect(closestNavObj);
        } else if (closestNavObjSecondary != null) {
            curIndex = newIndexSecondary;
            onSelect(closestNavObjSecondary);
        }
    }

    public void performLeftAction(float advance) {
        moveSelection((NavigationObject orig, NavigationObject other) -> (int) getDeltaX(other, orig),
                (NavigationObject orig, NavigationObject other) -> (int) getDeltaY(orig, other));
    }

    public void performRightAction(float advance) {
        moveSelection((NavigationObject orig, NavigationObject other) -> (int) getDeltaX(orig, other),
                (NavigationObject orig, NavigationObject other) -> (int) getDeltaY(orig, other));
    }

    public void performUpAction(float advance) {
        moveSelection((NavigationObject orig, NavigationObject other) -> (int) getDeltaY(orig, other),
                (NavigationObject orig, NavigationObject other) -> (int) getDeltaX(orig, other));
    }

    public void performDownAction(float advance) {
        moveSelection((NavigationObject orig, NavigationObject other) -> (int) getDeltaY(other, orig),
                (NavigationObject orig, NavigationObject other) -> (int) getDeltaX(orig, other));
    }

    public NavigationObject getSelected() {
        if(curIndex != -1 && curIndex < navigationObjects.size()) {
            return navigationObjects.get(curIndex);
        } else {
            return null;
        }
    }

    public void advance(float amount) {
        if(curIndex != -1 && InputShim.hasMouseControl()) {
            for(var obj : navigationObjects) {
                obj.updatePos();
            }
            var selected = getSelected();
            if(selected.x1 != selectedItemX || selected.y1 != selectedItemY) {
                onSelect(selected);
            }
        }
    }

    public void render() {
        if(curIndex != -1 && InputShim.hasMouseControl()) {
            reticle.render(getSelected());
        }
    }

    public static class NavigationObject {
        public UIComponentAPI component;
        public float x1,x2,y1,y2;
        public Object tag;
        boolean autoPos = true;

        public NavigationObject(UIComponentAPI comp) {
            this(comp, null);
        }

        public NavigationObject(UIComponentAPI comp, Object tag) {
            this.component = comp;
            this.tag = tag;
            updatePos();
        }

        public NavigationObject(UIComponentAPI comp, float x1, float x2, float y1, float y2) {
            this(comp, x1, x2, y1, y2, null);
            autoPos = false;
        }

        public NavigationObject(UIComponentAPI comp, float x1, float x2, float y1, float y2, Object tag) {
            this.component = comp;
            this.x1 = x1;
            this.x2 = x2;

            this.y1 = y1;
            this.y2 = y2;
            this.tag = tag;
            autoPos = false;
        }

        public void updatePos() {
            if(autoPos) {
                x1 = component.getPosition().getX();
                x2 = component.getPosition().getX() + component.getPosition().getWidth();
                y1 = component.getPosition().getY();
                y2 = component.getPosition().getY() + component.getPosition().getHeight();
            }
        }

        public float getCenterX() {
            return (x1 + x2) / 2.f;
        }

        public float getCenterY() {
            return (y1 + y2) / 2.f;
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

        public void render(NavigationObject navObj) {
            float x1 = navObj.x1 - indicTL.getWidth(), x2 = navObj.x2;
            float y1 = navObj.y2, y2 = navObj.y1 - indicTL.getHeight();
            indicTL.render(x1, y1);
            indicTR.render(x2, y1);
            indicBL.render(x1, y2);
            indicBR.render(x2, y2);
        }
    }

}
