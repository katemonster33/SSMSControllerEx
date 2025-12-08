package ssms.controller.reflection;

import com.fs.starfarer.campaign.fleet.FleetMember;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FleetMemberViewReflector {
    static Class<?> fleetMemberViewCls;

    private FleetMemberViewReflector() {
    }

    public static Class<?> getFleetMemberViewClass() {
        return fleetMemberViewCls;
    }

    public static boolean tryInit(Class<?> cls) {
        try {
            var lookup = MethodHandles.lookup();

            var showScuttleDialog = lookup.findVirtual(cls, "showScuttleDialog", MethodType.methodType(void.class, FleetMember.class));

            var showCaptainPickerDialog = lookup.findVirtual(cls, "showCaptainPickerDialog", MethodType.methodType(void.class, FleetMember.class));

            var showBuyConfirmDialog = lookup.findVirtual(cls, "showBuyConfirmDialog", MethodType.methodType(void.class, FleetMember.class));

            var showSellConfirmDialog = lookup.findVirtual(cls, "showSellConfirmDialog", MethodType.methodType(void.class, FleetMember.class));

            if(showScuttleDialog != null && showCaptainPickerDialog != null && showBuyConfirmDialog != null && showSellConfirmDialog != null) {
                fleetMemberViewCls = cls;
                return true;
            }
        } catch(NoSuchMethodException | IllegalAccessException ignored) {
        }
        return false;
    }
}
