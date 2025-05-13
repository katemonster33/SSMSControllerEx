package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.AptitudeRow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterSheetReflector {
    UIPanelAPI characterSheetObj;
    static MethodHandle getAptitudeRows;
    static Class<?> characterSheetCls;

    static MethodHandle getButtonsMap;

    private CharacterSheetReflector(UIPanelAPI characterSheetObj) {
        this.characterSheetObj = characterSheetObj;
    }

    public CharacterSheetReflector TryGet(BorderedPanelReflector borderedPanelReflector) {
        var parentPanel = borderedPanelReflector.getPanel();
        if(characterSheetCls == null) {
            if(UIPanelAPI.class.isAssignableFrom(parentPanel.getClass())) {
                try {
                    getAptitudeRows = MethodHandles.lookup().findVirtual(parentPanel.getClass(), "getAptitudeRows", MethodType.methodType(List.class));

                    characterSheetCls = parentPanel.getClass();

                    getButtonsMap = MethodHandles.lookup().findVirtual(AptitudeRow.class, "getButtonsMap", MethodType.methodType(Map.class));
                } catch(Throwable ex) {
                    Global.getLogger(CharacterSheetReflector.class).fatal("Couldn't reflect CharacterSheet UI object!", ex);
                }
            }
        } else if(characterSheetCls.isAssignableFrom(parentPanel.getClass())) {
            return new CharacterSheetReflector(borderedPanelReflector.getPanel());
        }
        return null;
    }

    public List<List<ButtonAPI>> getButtonRows() {
        List<List<ButtonAPI>> output = new ArrayList<>();
        try {
            var aptitudeRows = (List<AptitudeRow>)getAptitudeRows.invoke(characterSheetObj);

            for(AptitudeRow row : aptitudeRows) {
                List<ButtonAPI> buttons = new ArrayList<>();
                Map<?,?> buttonMap = (Map<?,?>) getButtonsMap.invoke(row);
                for(Object obj : buttonMap.keySet()) {
                    if(ButtonAPI.class.isAssignableFrom(obj.getClass())) {
                        buttons.add((ButtonAPI) obj);
                    }
                }
                if(!buttons.isEmpty()) {
                    output.add(buttons);
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't fetch the button rows!", ex);
        }
        return output;
    }
}
