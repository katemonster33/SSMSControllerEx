package ssms.controller.inputhelper;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class DirectionalUINavigator {
    List<Pair<UIComponentAPI, Object>> navigationObjects;
    int curIndex = -1;
    public DirectionalUINavigator(List<Pair<UIComponentAPI, Object>> navigationObjects)
    {
        this.navigationObjects = new ArrayList<>(navigationObjects);
        curIndex = 0;
    }

    public void onSelect(Pair<UIComponentAPI, Object> selectedPair) {
        PositionAPI pos = selectedPair.one.getPosition();
        InputShim.mouseMove((int) pos.getCenterX(),(int) pos.getCenterY());
    }

    public abstract void onConfirm(Pair<UIComponentAPI, Object> selectedPair);

    void moveSelection(Comparator<UIComponentAPI> comparator) {
        var newPair = getClosest(comparator);
        if(newPair != null) {
            onSelect(newPair);
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

    public boolean handleInput(float advance) {
        if(SSMSControllerModPluginEx.controller.getButtonEvent(LogicalButtons.DpadLeft) == 1) {
            moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                    (int)((o1.getPosition().getX() - o2.getPosition().getX()) *
                            getDeltaOrOne(o1.getPosition().getY(), o2.getPosition().getY())));
            return true;
        } else if(SSMSControllerModPluginEx.controller.getButtonEvent(LogicalButtons.DpadRight) == 1) {
            moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                    (int)((o2.getPosition().getX() - o1.getPosition().getX()) *
                            getDeltaOrOne(o1.getPosition().getY(), o2.getPosition().getY())));
            return true;
        } else if(SSMSControllerModPluginEx.controller.getButtonEvent(LogicalButtons.DpadUp) == 1) {
            moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                    (int)((o2.getPosition().getY() - o1.getPosition().getY()) *
                            getDeltaOrOne(o1.getPosition().getX(), o2.getPosition().getX())));
            return true;
        } else if(SSMSControllerModPluginEx.controller.getButtonEvent(LogicalButtons.DpadDown) == 1) {
            moveSelection((UIComponentAPI o1, UIComponentAPI o2) ->
                    (int)((o2.getPosition().getY() - o1.getPosition().getY()) *
                            getDeltaOrOne(o1.getPosition().getX(), o2.getPosition().getX())));
            return true;
        } else if(SSMSControllerModPluginEx.controller.getButtonEvent(LogicalButtons.A) == 1) {
            if(curIndex != -1) {
                onConfirm(navigationObjects.get(curIndex));
            }
            return true;
        }
        return false;
    }
}
