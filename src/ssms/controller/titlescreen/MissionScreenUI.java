package ssms.controller.titlescreen;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
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

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object... args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        missionWidgetReflector = new MissionWidgetReflector(titleScreenStateReflector.getMissionWidget());

        var missionDetail = missionWidgetReflector.getMissionDetail();
        var missionList = missionWidgetReflector.getMissionList();
        var missionListScrollPanel = new ScrollPanelReflector((ScrollPanelAPI) missionList.getChildItems().get(0));
        //var missionListItems = missionList.getChildPanels();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for (var item : missionWidgetReflector.getMissionItems()) {
            directionalObjects.add(new Pair<>(item, missionListScrollPanel));
        }
        for (var btn : missionDetail.getChildButtons(true)) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects) {
            @Override
            public void onSelect(Pair<UIComponentAPI, Object> directionalObject) {
                super.onSelect(directionalObject);
                if (directionalObject.two instanceof ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(directionalObject.one);
                }
            }
        };
        indicators = new ArrayList<>();
        addDigitalJoystickHandler("Navigate", Joystick.Left, directionalUINavigator);
        addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
    }

    @Override
    public void preInput(float advance) {
        if (missionWidgetReflector.getFader().isFadedOut()) {
            InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
        }
    }

    static class MissionWidgetReflector extends UIPanelReflector {
        MethodReflector getMissionDetail;
        MethodReflector getMissionList;
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
    }
}
