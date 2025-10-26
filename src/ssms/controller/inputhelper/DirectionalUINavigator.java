package ssms.controller.inputhelper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.CrosshairRenderer;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.ScrollPanelReflector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DirectionalUINavigator implements DigitalJoystickHandler {
    List<NavigationObject> navigationObjects;
    NavigationObject lastFrameSelected;
    float selectedItemX, selectedItemY;
    int curIndex = -1;
    DirectionalUiReticle reticle;
    boolean leftStickActive = false, rightStickActive = false;
    Vector2f desiredMousePos, lastFrameMousePos;
    final float mouseMoveFactor = 4.f;
    CrosshairRenderer headingIndicator;
    UIComponentAPI mapComponent;
    List<ScrollPanelReflector> scrollPanels;
    ScrollPanelReflector activeScroller;
    DirectionalUIContext lastFrameContext, curContext;
    boolean isMovingMap = false;
    boolean joystickEnabled = false;
    float mapZoomRate = 5.f;
    float mapZoomCounter = 0.f;
    float scrollRate = 100.f;

    public DirectionalUINavigator(List<NavigationObject> navigationObjects)
    {
        setNavigationObjects(navigationObjects);
        reticle = new DirectionalUiReticle();
        headingIndicator = new CrosshairRenderer();
        headingIndicator.setSize(32, 32);
        curContext = lastFrameContext = DirectionalUIContext.Other;
        scrollPanels = new ArrayList<>();
        lastFrameMousePos = new Vector2f();
    }

    public void setJoystickEnabled(boolean enabled) {
        this.joystickEnabled = enabled;
    }

    public void setMapComponent(UIComponentAPI mapReflector) {
        this.mapComponent = mapReflector;
    }

    public void addScrollPanel(ScrollPanelReflector scrollPanelAPI) {
        scrollPanels.add(scrollPanelAPI);
    }

    public void clearScrollPanels() {
        scrollPanels.clear();
    }

    public DirectionalUIContext getCurContext() {
        return curContext;
    }

    public boolean isContextChanged() {
        return lastFrameContext != curContext;
    }

    public void onSelect(NavigationObject selectedNav) {
        desiredMousePos = null;
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
            return b.getCenterY() - a.getCenterY();
        } else {
            return -1;
        }
    }

    public void setNavigationObjects(List<NavigationObject> navigationObjects) {

        if (navigationObjects.isEmpty()) {
            this.navigationObjects = new ArrayList<>(navigationObjects);
            curIndex = -1;
            return;
        }
        NavigationObject curObj = null;
        if (curIndex != -1 && curIndex < this.navigationObjects.size()) {
            curObj = this.navigationObjects.get(curIndex);
        }
        this.navigationObjects = new ArrayList<>(navigationObjects);
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
        } else if(desiredMousePos == null) {
            curIndex = 0;
            onSelect(this.navigationObjects.get(curIndex));
        }
    }

    public void moveSelection(Comparator<NavigationObject> getDeltaA, Comparator<NavigationObject> getDeltaB) {
        if(curIndex == -1 && desiredMousePos == null) {
            if(!navigationObjects.isEmpty()) {
                curIndex = 0;
                onSelect(navigationObjects.get(0));
            }
            return;
        }

        NavigationObject selectedObj = desiredMousePos != null ? new NavigationObject(null, desiredMousePos.x, desiredMousePos.x, desiredMousePos.y, desiredMousePos.y) : navigationObjects.get(curIndex);
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

    public void handleAButton(float advance, boolean buttonVal) {
        if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
            if (buttonVal) {
                InputShim.mouseDown(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
            } else {
                InputShim.mouseUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
            }
        }
    }

    boolean isStickActive(ReadableVector2f stick) {
        return stick.getX() != 0 || stick.getY() != 0;
    }

    public void handleLeftJoystick(float advance, Vector2f joystickVal) {
        leftStickActive = isStickActive(joystickVal);

        if(leftStickActive) {
            if(desiredMousePos == null) {
                if(curIndex != -1 && curIndex < navigationObjects.size()) {
                    desiredMousePos = new Vector2f(navigationObjects.get(curIndex).getCenterX(), navigationObjects.get(curIndex).getCenterY());
                } else {
                    desiredMousePos = new Vector2f();
                }
                curIndex = -1;
            }
            desiredMousePos.set(desiredMousePos.getX() + (joystickVal.getX() * mouseMoveFactor), desiredMousePos.getY() - (joystickVal.getY() * mouseMoveFactor));
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
    }

    public void handleRightJoystick(float advance, Vector2f joystickVal) {
        rightStickActive = isStickActive(joystickVal);

        if (leftStickActive) return;

        if(curContext == DirectionalUIContext.Map) {
            if(desiredMousePos == null) {
                desiredMousePos = new Vector2f(mapComponent.getPosition().getCenterX(), mapComponent.getPosition().getCenterY());
            }
            if (rightStickActive) {
                if (!isMovingMap) {
                    centerMousePosOnMap();
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                    isMovingMap = true;
                } else {
                    desiredMousePos.set(desiredMousePos.getX() - (joystickVal.getX() * mouseMoveFactor), desiredMousePos.getY() + (joystickVal.getY() * mouseMoveFactor));
                    InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
                }
            } else if (isMovingMap) {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                centerMousePosOnMap();
                isMovingMap = false;
            }
        } else if(curContext == DirectionalUIContext.Scroller && activeScroller != null) {
            if(joystickVal.getY() != 0.f) {
                activeScroller.scrollToY(activeScroller.getScrollPanel().getYOffset() + (joystickVal.getY() * advance * 300));
            }
            if(joystickVal.getX() != 0.f) {
                activeScroller.scrollToX(activeScroller.getScrollPanel().getXOffset() + (joystickVal.getX() * advance * 300));
            }
        }
    }

    public void centerMousePosOnMap() {
        desiredMousePos = new Vector2f(mapComponent.getPosition().getCenterX(), mapComponent.getPosition().getCenterY());
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
    }

    public NavigationObject getSelected() {
        if(curIndex != -1 && curIndex < navigationObjects.size()) {
            return navigationObjects.get(curIndex);
        } else {
            return null;
        }
    }

    boolean mousePosIntersects(UIComponentAPI comp) {
        var pos = comp.getPosition();
        return desiredMousePos.x >= pos.getX() && desiredMousePos.x < pos.getX() + pos.getWidth() &&
                desiredMousePos.y >= pos.getY() && desiredMousePos.y < pos.getY() + pos.getHeight();
    }

    public void advance(float amount) {
        lastFrameContext = curContext;
        var selected = getSelected();
        if(selected != null) {
            if(selected != lastFrameSelected) {
                if (mapComponent != null && getSelected().component == mapComponent) {
                    curContext = DirectionalUIContext.Map;
                } else if ((activeScroller = scrollPanels.stream().filter(pnl -> pnl.getPanel() == selected.component).findFirst().orElse(null)) != null) {
                    curContext = DirectionalUIContext.Scroller;
                } else {
                    curContext = DirectionalUIContext.Other;
                }
            }
        } else if(desiredMousePos != null) {
            if (mapComponent != null && mousePosIntersects(mapComponent)) {
                curContext = DirectionalUIContext.Map;
            } else if((activeScroller = scrollPanels.stream().filter(pnl -> mousePosIntersects(pnl.getPanel())).findFirst().orElse(null)) != null) {
                curContext = DirectionalUIContext.Scroller;
            } else {
                curContext = DirectionalUIContext.Other;
            }
        }
        if(InputShim.hasMouseControl()) {
            for(var obj : navigationObjects) {
                obj.updatePos();
            }
            if(selected != null && (selected.x1 != selectedItemX || selected.y1 != selectedItemY)) {
                onSelect(selected);
            }
        }
        if(joystickEnabled) {
            handleLeftJoystick(amount, SSMSControllerModPluginEx.controller.getJoystick(Joystick.Left));
            handleRightJoystick(amount, SSMSControllerModPluginEx.controller.getJoystick(Joystick.Right));
            if (curContext == DirectionalUIContext.Map && desiredMousePos != null) {
                var controller = SSMSControllerModPluginEx.controller;
                if (controller.isButtonPressed(LogicalButtons.LeftTrigger)) {
                    mapZoomCounter -= mapZoomRate * amount;
                    if(mapZoomCounter < -1.f) {
                        InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), -1);
                        mapZoomCounter = 0.f;
                    }
                } else if (controller.isButtonPressed(LogicalButtons.RightTrigger)) {
                    mapZoomCounter += mapZoomRate * amount;
                    if(mapZoomCounter >= 1.f) {
                        InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), 1);
                        mapZoomCounter = 0.f;
                    }
                } else {
                    mapZoomCounter = 0.f;
                }
            }
        }
    }

    public void render() {
        if(InputShim.hasMouseControl()) {
            if (curIndex != -1) {
                reticle.render(getSelected());
            } else if(desiredMousePos != null) {
                headingIndicator.setMousePos(desiredMousePos.x, desiredMousePos.y);
                headingIndicator.render();
            }
        }
    }

    public enum DirectionalUIContext {
        Map,
        Scroller,
        Other
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
