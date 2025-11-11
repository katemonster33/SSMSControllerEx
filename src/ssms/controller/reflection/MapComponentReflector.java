package ssms.controller.reflection;

import com.fs.starfarer.campaign.comms.v2.EventsPanel;

public class MapComponentReflector {
    static Class<?> mapComponentCls;

    static {
        ClassReflector eventsCls = new ClassReflector(EventsPanel.class);

        var mapTabCls = eventsCls.getDeclaredMethod("getMap").getReturnType();

        ClassReflector mapTabReflector = new ClassReflector(mapTabCls);

        mapComponentCls = mapTabReflector.getDeclaredMethod("getMap").getReturnType();
    }

    public static boolean isAssignableFrom(Class<?> cls) {
        return mapComponentCls.isAssignableFrom(cls);
    }
}
