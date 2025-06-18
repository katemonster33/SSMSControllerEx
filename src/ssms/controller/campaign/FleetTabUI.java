package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.FleetTabReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class FleetTabUI extends InputScreenBase {
    public static final String ID = "FleetTab";
    int numCols = -1;
    int curRow = -1, curCol = -1;
    int itemCount = -1;
    Vector2f itemSize;
    Vector2f topLeftPos;
    Vector2f mousePos;
    FleetTabReflector fleetTabReflector;
    float buttonXDownTime = 0.f;
    float buttonYDownTime = 0.f;
    float buttonADownTime = 0.f;
    int itemInfoOffset = -1;
    List<Vector2f> itemInfoOffsets;
    boolean allRepairsSuspended = false;
    DirectionalUINavigator directionalUINavigator;

    @Override
    public String getId() {
        return ID;
    }

    void hoverCurrentItem() {
        if (itemInfoOffset != -1 && itemInfoOffset < itemInfoOffsets.size()) {
            mousePos.setX(topLeftPos.x + (curCol * itemSize.x) + itemInfoOffsets.get(itemInfoOffset).getX());
            mousePos.setY(topLeftPos.y - (curRow * itemSize.y) + itemInfoOffsets.get(itemInfoOffset).getY());
        } else {
            mousePos.set(topLeftPos.x + (curCol * itemSize.x) + (itemSize.x / 2),
                    topLeftPos.y - (curRow * itemSize.y) - (itemSize.y / 2));
        }

        InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate ships"));
            addButtonPressHandler("Select next ship info", LogicalButtons.RightTrigger, (float advance) ->
                    incrementItemInfoOffset());
            if(itemInfoOffset == -1) {
                addButtonPressHandler("Move ship", LogicalButtons.A, (float advance) -> InputShim.mouseDownUp((int) mousePos.getX(), (int) mousePos.getY(), InputEventMouseButton.LEFT));
            } else if(itemInfoOffset == 0) {
                addButtonPressHandler("Open codex", LogicalButtons.A, new KeySender(Keyboard.KEY_A, 'a'));
            } else if(itemInfoOffset == 1) {
                addButtonPressOrHoldHandler("Assign commander", "Re-assign commander", LogicalButtons.A, new ButtonPressOrHoldHandler() {
                    @Override
                    public void performHoldAction(float advance) {
                        InputShim.mouseDownUp((int) mousePos.getX(), (int) mousePos.getY(), InputEventMouseButton.RIGHT);
                    }

                    @Override
                    public void performPressAction(float advance) {
                        InputShim.mouseDownUp((int) mousePos.getX(), (int) mousePos.getY(), InputEventMouseButton.LEFT);
                    }
                });
            } else if(itemInfoOffset == 2) {
                addButtonPressHandler("Show more info", LogicalButtons.A, new KeySender(Keyboard.KEY_F1));
            }
            addButtonPressHandler("Close Dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
//            indicators.add(new Pair<>(Indicators.X, "Suspend/Resume repairs (current ship)"));
//            indicators.add(new Pair<>(Indicators.X, "(hold) Suspend/Resume repairs (all ships)"));
//            indicators.add(new Pair<>(Indicators.Y, "Mothball ship"));
//            indicators.add(new Pair<>(Indicators.Y, "(hold) Scuttle ship"));
//            indicators.add(new Pair<>(Indicators.Select, "Refit"));
            addButtonPressHandler("Select character tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_C, 'c'));
            addButtonPressHandler("Select refit tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_R, 'r'));

        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        this.fleetTabReflector = (FleetTabReflector) args[0];

        itemSize = new Vector2f(fleetTabReflector.getItemWidth(), fleetTabReflector.getItemHeight());
        numCols = fleetTabReflector.getColumns();
        mousePos = new Vector2f(0.f, 0.f);

        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : fleetTabReflector.getButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects){
            @Override
            public void onSelect(Pair<UIComponentAPI, Object> obj) {
                super.onSelect(obj);
                itemInfoOffset = -1;
                refreshIndicators();
                mousePos.set(obj.one.getPosition().getCenterX(), obj.one.getPosition().getCenterY());
            }
        };
        var items = fleetTabReflector.getItems();
        itemCount = items.size();
        itemInfoOffsets = new ArrayList<>();
        if (itemCount > 0 && UIPanelAPI.class.isAssignableFrom(items.get(0).getClass())) {
            UIPanelAPI itemPnl = (UIPanelAPI) items.get(0);
            topLeftPos = new Vector2f(itemPnl.getPosition().getX(), itemPnl.getPosition().getY() + itemPnl.getPosition().getHeight());

            var itemChildren = UIPanelReflector.getChildItems(itemPnl);
            if(itemChildren.size() > 4 && UIPanelAPI.class.isAssignableFrom(itemChildren.get(4).getClass())) {
                UIPanelAPI buttonPanel = (UIPanelAPI) itemChildren.get(4);

                var buttons = UIPanelReflector.getChildItems(buttonPanel);
                if (buttons.size() > 4 && UIComponentAPI.class.isAssignableFrom(buttons.get(4).getClass())) {
                    var pos = ((UIComponentAPI) buttons.get(4)).getPosition();
                    itemInfoOffsets.add(new Vector2f(pos.getCenterX() - topLeftPos.getX(), pos.getCenterY() - topLeftPos.getY()));
                }
            }
            if (itemChildren.size() > 3 && UIPanelAPI.class.isAssignableFrom(itemChildren.get(3).getClass())) {
                var itemInfoPanel = (UIPanelAPI) itemChildren.get(3);
                var itemInfoItems = UIPanelReflector.getChildItems(itemInfoPanel);
                for (var itemInfo : itemInfoItems) {
                    if (UIComponentAPI.class.isAssignableFrom(itemInfo.getClass())) {
                        var pos = ((UIComponentAPI) itemInfo).getPosition();
                        itemInfoOffsets.add(new Vector2f(pos.getCenterX() - topLeftPos.getX(), pos.getCenterY() - topLeftPos.getY()));
                    }
                }
            }
        } else {
            topLeftPos = new Vector2f(-1.f, -1.f);
        }
        buttonXDownTime = buttonYDownTime = buttonADownTime = -1.f;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.FLEET) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        }

        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : fleetTabReflector.getButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
        if (controller.getButtonEvent(LogicalButtons.LeftStickLeft) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curCol > 0) {
                curCol--;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(LogicalButtons.LeftStickRight) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curCol < (numCols - 1) && ((curRow * numCols) + curCol) < (itemCount - 1)) {
                curCol++;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(LogicalButtons.LeftStickUp) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curRow > 0) {
                curRow--;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(LogicalButtons.LeftStickDown) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if ((((curRow + 1) * numCols) + curCol) < itemCount) {
                curRow++;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(LogicalButtons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if (controller.getButtonEvent(LogicalButtons.Select) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_R, 'r');
        }
        if(itemInfoOffset == -1) {
            if (controller.getButtonEvent(LogicalButtons.A) == 1) {
                InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            }
        } else if(itemInfoOffset == 0) {
            if (controller.getButtonEvent(LogicalButtons.A) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
            }
        } else if(itemInfoOffset == 1) {
            if(controller.isButtonPressed(LogicalButtons.A)) {
                if(buttonADownTime != -1.f) {
                    buttonADownTime += amount;
                }
                if(buttonADownTime > 1.f) {
                    InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
                    buttonADownTime = -1.f;
                }
            } else {
                if(buttonADownTime > 0.f) {
                    InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                }
                buttonADownTime = 0.f;
            }
        } else if(itemInfoOffset == 2) {
            if (controller.getButtonEvent(LogicalButtons.A) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_F1, '\0');
            }
        }
        if (controller.getButtonEvent(LogicalButtons.LeftStickButton) == 1) {
            incrementItemInfoOffset();
        }
        if(controller.isButtonPressed(LogicalButtons.X)) {
            if(buttonXDownTime != -1.f) {
                buttonXDownTime += amount;
            }
            if(buttonXDownTime > 1.f) {
                if(allRepairsSuspended) {
                    InputShim.keyDownUp(Keyboard.KEY_W, 'W');
                } else {
                    InputShim.keyDownUp(Keyboard.KEY_Q, 'Q');
                }
                allRepairsSuspended = !allRepairsSuspended;
                buttonXDownTime = -1.f;
            }
        } else {
            if(buttonXDownTime > 0.f) {
                InputShim.keyDownUp(Keyboard.KEY_G, 'g');
            }
            buttonXDownTime = 0.f;
        }
        if(controller.isButtonPressed(LogicalButtons.Y)) {
            if(buttonYDownTime != -1.f) {
                buttonYDownTime += amount;
            }
            if(buttonYDownTime > 1.f) {
                InputShim.keyDownUp(Keyboard.KEY_U, 'u');
                buttonYDownTime = -1.f;
            }
        } else {
            if(buttonYDownTime > 0.f) {
                InputShim.keyDownUp(Keyboard.KEY_M, 'm');
            }
            buttonYDownTime = 0.f;
        }
        if(controller.getButtonEvent(LogicalButtons.BumperLeft) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_C, 'c');
        } else if(controller.getButtonEvent(LogicalButtons.BumperRight) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_R, 'r');
        }
    }

    private void incrementItemInfoOffset() {
        if(itemInfoOffset != -1 && itemInfoOffset < (itemInfoOffsets.size() - 1)) {
            itemInfoOffset++;
        } else {
            itemInfoOffset = 0;
        }
        indicators = null;
        InputScreenManager.getInstance().refreshIndicators();
        hoverCurrentItem();
    }
}
