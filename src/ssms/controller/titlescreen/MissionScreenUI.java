package ssms.controller.titlescreen;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.ui.impl.FleetGrid;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.AnalogJoystickHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class MissionScreenUI extends InputScreenBase {
    public static String ID = "MissionScreen";
    MissionWidgetReflector missionWidgetReflector;
    TitleScreenStateReflector titleScreenStateReflector;
    DirectionalUINavigator directionalUINavigator;
    List<UIComponentAPI> missions;
    UIPanelReflector missionDetail;
    UIPanelReflector missionList;
    ScrollPanelReflector textScroller;
    boolean scrollerSkipFrame = false;
    AnalogJoystickHandler scrollerHandler;
    int highlightedMission = -1;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object... args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        missionWidgetReflector = new MissionWidgetReflector(titleScreenStateReflector.getMissionWidget());

        missionDetail = missionWidgetReflector.getMissionDetail();
        missionList = missionWidgetReflector.getMissionList();
        //var missionListItems = missionList.getChildPanels();
        missions = missionWidgetReflector.getMissionItems();

        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
            @Override
            public void onSelect(Pair<UIComponentAPI, Object> directionalObject) {
                super.onSelect(directionalObject);
                if (directionalObject.two instanceof ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(directionalObject.one);
                }
            }
        };
        highlightedMission = missionWidgetReflector.getMissionIndexSelected();
        refreshDirectionalObjects();
        indicators = new ArrayList<>();
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
        scrollerHandler = (float advance, Vector2f joystickVal) -> {
            if(textScroller != null) {
                //textScroller.getScrollPanel().setYOffset(textScroller.getScrollPanel().getYOffset() + (joystickVal.getY() * advance * 100));
                textScroller.scrollToY(textScroller.getScrollPanel().getYOffset() + (joystickVal.getY() * advance * 100));
                scrollerSkipFrame = true; // only allows 1 scroll action to execute per frame
            }
        };
        addAnalogJoystickHandler("Scroll Mission Description", Joystick.Right, scrollerHandler);
        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
            if(directionalUINavigator.getSelected() != null) {
                var pos = directionalUINavigator.getSelected().one.getPosition();
                InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
            }
        });
        addButtonPressHandler("More Info", LogicalButtons.X, new KeySender(Keyboard.KEY_F1));
        addButtonPressHandler("Open Codex", LogicalButtons.Y, new KeySender(Keyboard.KEY_F2));
        addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
    }

    void refreshDirectionalObjects() {
        var missionListScrollPanel = new ScrollPanelReflector((ScrollPanelAPI) missionList.getChildItems().get(0));
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for (var item : missions) {
            directionalObjects.add(new Pair<>(item, missionListScrollPanel));
        }
        for (var btn : missionWidgetReflector.getChildButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        for (var btn : missionDetail.getChildButtons(true)) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        textScroller = new ScrollPanelReflector((ScrollPanelAPI) missionDetail.getChildItems().get(2));
        for(var fleetMember : missionDetail.getChildItems(4, 1, 2, 1, 0)) {
            directionalObjects.add(new Pair<>((UIComponentAPI) fleetMember, null));
        }
        for(var fleetMember : missionDetail.getChildItems(4, 1, 3, 1, 0)) {
            directionalObjects.add(new Pair<>((UIComponentAPI) fleetMember, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public void preInput(float advance) {
        if (missionWidgetReflector.getFader().isFadedOut()) {
            InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
        }
        int curMissionSelected = missionWidgetReflector.getMissionIndexSelected();
        if(curMissionSelected != highlightedMission) {
            refreshDirectionalObjects();
            highlightedMission = curMissionSelected;
        }
        if(!scrollerSkipFrame) {
            var rightJoystick = controller.getJoystick(Joystick.Right);
            if (rightJoystick.getY() != 0.0f) {
                scrollerHandler.performAction(advance, rightJoystick);
            }
        }
        if(scrollerSkipFrame) {
            //textScroller.clampOffset();
        }
        scrollerSkipFrame = false;
        directionalUINavigator.advance(advance);
    }

    static class MissionWidgetReflector extends UIPanelReflector {
        MethodReflector getMissionDetail;
        MethodReflector getMissionList;
        FieldReflector isMissionSelected;
        FieldReflector missionItemList;
        public MissionWidgetReflector(UIPanelAPI missionWidget) {
            super(missionWidget);
            ClassReflector missionReflector = new ClassReflector(missionWidget.getClass());
            this.getMissionDetail = missionReflector.getDeclaredMethod("getMissionDetail");
            this.getMissionList = missionReflector.getDeclaredMethod("getMissionList");

            ClassReflector missionListReflector = new ClassReflector(getMissionList.getReturnType());
            for(var missionListField : missionListReflector.getDeclaredFields()) {
                if(List.class.isAssignableFrom(missionListField.getType())) {
                    missionItemList = missionListField;
                }
            }
        }

        public UIPanelReflector getMissionDetail() {
            return new UIPanelReflector((UIPanelAPI) getMissionDetail.invoke(getPanel()));
        }

        public ScrollPanelReflector getMissionDetailScrolledText() {
            for(var pnl : getMissionDetail().getChildPanels()) {
                if(pnl instanceof ScrollPanelAPI scrollPanelAPI) {
                    return new ScrollPanelReflector(scrollPanelAPI);
                }
            }
            return null;
        }

        public UIPanelReflector getMissionList() {
            return new UIPanelReflector((UIPanelAPI) getMissionList.invoke(getPanel()));
        }

        public List<UIComponentAPI> getMissionItems() {
            List<?> missionUiItems = (List<?>) missionItemList.get(getMissionList.invoke(getPanel()));
            List<UIComponentAPI> output = new ArrayList<>();
            for(var item : missionUiItems) {
                if(item instanceof UIComponentAPI componentAPI) {
                    output.add(componentAPI);
                }
            }
            return output;
        }

        public int getMissionIndexSelected() {
            var lst = getMissionItems();
            int index = 0;
            for(var item : lst) {
                if(isMissionSelected == null) {
                    ClassReflector missionCls = new ClassReflector(item.getClass());
                    for(var field : missionCls.getDeclaredFields()) {
                        if(field.getType() == boolean.class) {
                            isMissionSelected = field;
                            break;
                        }
                    }
                }
                if(((boolean)isMissionSelected.get(item))) {
                    return index;
                }
                index++;
            }
            return -1;
        }
    }
}
