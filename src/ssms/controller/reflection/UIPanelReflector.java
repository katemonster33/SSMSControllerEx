package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;

public class UIPanelReflector {
    Object thisPanel;

    MethodHandle getChildItemsHandle;
    public UIPanelReflector(Object panel)
    {
        thisPanel = panel;

        try {
            Object[] methods =  ClassReflector.GetInstance().getDeclaredMethods(panel.getClass());
            for(var method : methods) {
                if(MethodReflector.GetInstance().getName(method) == "getChildrenCopy") {
                    getChildItemsHandle = (MethodHandle)method;
                    break;
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).log(Level.WARN, "Can't reflect panel type: " + panel.getClass() + ", " + ex.getMessage() );
        }
    }

    
}
