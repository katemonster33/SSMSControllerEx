package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.AptitudeRow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterSheetReflector {
    CoreUIAPI coreUIAPI;
    UIPanelAPI characterSheetObj;
    static MethodHandle getAptitudeRows;
    static Class<?> characterSheetCls;

    static MethodHandle getButtonsMap;

    private CharacterSheetReflector(CoreUIAPI coreUIAPI, UIPanelAPI characterSheetObj) {
        this.coreUIAPI = coreUIAPI;
        this.characterSheetObj = characterSheetObj;
    }

    public UIPanelAPI getCharacterSheetObj() {
        return characterSheetObj;
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public static CharacterSheetReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        var parentPanel = borderedPanelReflector.getInnerPanel();
        if(characterSheetCls == null) {
            try {
                getAptitudeRows = MethodHandles.lookup().findVirtual(parentPanel.getClass(), "getAptitudeRows", MethodType.methodType(List.class));

                characterSheetCls = parentPanel.getClass();

                getButtonsMap = MethodHandles.lookup().findVirtual(AptitudeRow.class, "getButtonsMap", MethodType.methodType(Map.class));

                return new CharacterSheetReflector(coreUIAPI, borderedPanelReflector.getInnerPanel());
            } catch(Throwable ex) {
                Global.getLogger(CharacterSheetReflector.class).fatal("Couldn't reflect CharacterSheet UI object!", ex);
            }
        } else if(characterSheetCls.isAssignableFrom(parentPanel.getClass())) {
            return new CharacterSheetReflector(coreUIAPI, borderedPanelReflector.getInnerPanel());
        }
        return null;
    }

    public List<List<ButtonAPI>> getButtonRows() {
        List<List<ButtonAPI>> output = new ArrayList<>();
        try {
            var aptitudeRows = (List<?>)getAptitudeRows.invoke(characterSheetObj);

            for(var row : aptitudeRows) {
                List<ButtonAPI> buttons = new ArrayList<>();
                var children = UIPanelReflector.getChildItems((UIPanelAPI) row);
                if(!children.isEmpty()) {
                    var childrenOfChildren = UIPanelReflector.getChildItems((UIPanelAPI) children.get(0));
                    if(!childrenOfChildren.isEmpty() && ButtonAPI.class.isAssignableFrom(childrenOfChildren.get(0).getClass())) {
                        buttons.add((ButtonAPI) childrenOfChildren.get(0));
                    }
                }
                Map<?,?> buttonMap = (Map<?,?>) getButtonsMap.invoke(row);
                for(Object obj : buttonMap.keySet()) {
                    if(ButtonAPI.class.isAssignableFrom(obj.getClass())) {
                        buttons.add((ButtonAPI) obj);
                    }
                }
                if(!buttons.isEmpty()) {
                    buttons.sort((ButtonAPI left, ButtonAPI right) -> (int)(left.getPosition().getX() - right.getPosition().getX()));
                    output.add(buttons);
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't fetch the button rows!", ex);
        }
        return output;
    }
}
