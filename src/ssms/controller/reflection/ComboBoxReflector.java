package ssms.controller.reflection;

import com.fs.starfarer.api.ui.UIPanelAPI;

import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

public class ComboBoxReflector extends UIPanelReflector {
    static Class<?> cmbType;
    static ConstructorReflector ctor;
    static MethodReflector getSelected;
    static MethodReflector setSelected;

    UIPanelAPI cmbObj;
    public ComboBoxReflector(float width, String text, List<String> items, String selectedItem)  {
        super((UIPanelAPI) ctor.newInstance(width, text, items));
    }

    public static boolean tryInit(Class<?> cls) {
        if(cmbType != null && cls.isAssignableFrom(cmbType)) {
            return true;
        } else if(UIPanelAPI.class.isAssignableFrom(cls)) {
            var typeTmp = new ClassReflector(cls);
            List<String> lst = new ArrayList<>();
            ctor = typeTmp.getDeclaredConstructor(float.class, String.class, List.class);
            if(ctor != null) {
                getSelected = typeTmp.getDeclaredMethod("getSelected");

                setSelected = typeTmp.getDeclaredMethod("setSelected", String.class);
                if(getSelected != null && setSelected != null) {
                    cmbType = cls;
                    return true;
                }
            }
        }
        return false;
    }

    public String getSelected() {
        return (String) getSelected.invoke(getPanel());
    }

    public void setSelected(String str) {
        setSelected.invoke(getPanel(), str);
    }
}
