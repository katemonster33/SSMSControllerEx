package ssms.controller.inputhelper;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.InputShim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DirectionalUINavigator implements DigitalJoystickHandler {
    List<Pair<UIComponentAPI, Object>> navigationObjects;
    int curIndex = -1;
    public DirectionalUINavigator(List<Pair<UIComponentAPI, Object>> navigationObjects)
    {
        this.navigationObjects = new ArrayList<>(navigationObjects);
        curIndex = 0;
        onSelect(navigationObjects.get(curIndex));
    }

    public void onSelect(Pair<UIComponentAPI, Object> selectedPair) {
        PositionAPI pos = selectedPair.one.getPosition();
        InputShim.mouseMove((int) pos.getCenterX(),(int) pos.getCenterY());
    }

    void moveSelection(Comparator<UIComponentAPI> comparator) {
        var newPair = getClosest(comparator);
        if(newPair != null) {
            onSelect(newPair);
        }
    }

    public void setNavigationObjects(List<Pair<UIComponentAPI, Object>> navigationObjects) {
        PositionAPI curObjPos = null;
        if(curIndex != -1 && curIndex < navigationObjects.size()) {
            curObjPos = navigationObjects.get(curIndex).one.getPosition();
        }
        this.navigationObjects = new ArrayList<>(navigationObjects);
        if(curObjPos != null) {
            for(curIndex = 0; curIndex < navigationObjects.size(); curIndex++) {
                UIComponentAPI comp = navigationObjects.get(curIndex).one;
                if (comp.getPosition().getX() == curObjPos.getX() && comp.getPosition().getY() == curObjPos.getY()) {
                    break;
                }
            }
            if(curIndex >= navigationObjects.size()) {
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

    public void performAction(float advance, Vector2f joystickVal) {
    }

    public void performLeftAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o1.getPosition().getX() - o2.getPosition().getX()) *
                        getDeltaOrOne(o1.getPosition().getY(), o2.getPosition().getY())));
    }

    public void performRightAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o2.getPosition().getX() - o1.getPosition().getX()) *
                        getDeltaOrOne(o1.getPosition().getY(), o2.getPosition().getY())));
    }

    public void performUpAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o2.getPosition().getY() - o1.getPosition().getY()) *
                        getDeltaOrOne(o2.getPosition().getX(), o1.getPosition().getX())));
    }

    public void performDownAction(float advance) {
        moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                (int) ((o1.getPosition().getY() - o2.getPosition().getY()) *
                        getDeltaOrOne(o1.getPosition().getX(), o2.getPosition().getX())));
    }

    public Pair<UIComponentAPI, Object> getSelected() {
        return navigationObjects.get(curIndex);
    }
}
