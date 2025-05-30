package ssms.controller.reflection;

import com.fs.starfarer.combat.CombatState;

public class ZoomTrackerReflector {
    Object zoomAField;
    Object zoomBField;
    Object zoomMinField;
    Object zoomMaxField;
    Object zoomTrackerObj;
    // we're basically crossing our fingers here and hoping the order of the fields never changes. it probably will eventually. can't do much about it.
    public ZoomTrackerReflector(Object combatStateObj) throws Throwable {
        Object zoomTrackerField = ClassReflector.GetInstance().getDeclaredField(CombatState.class, "zoomTracker");
        zoomTrackerObj = FieldReflector.GetInstance().GetVariable(zoomTrackerField, combatStateObj);
        Class<?> zoomTrackerType = FieldReflector.GetInstance().GetVariableType(zoomTrackerField);

        var fields = ClassReflector.GetInstance().getDeclaredFields(zoomTrackerType);
        int numFloatFields = 0;
        for(int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
            if(FieldReflector.GetInstance().GetVariableType(fields[fieldIndex]) == float.class) {
                switch(numFloatFields) {
                    case 0: zoomAField = fields[fieldIndex]; break;
                    case 1: zoomBField = fields[fieldIndex]; break;
                    case 2: zoomMinField = fields[fieldIndex]; break;
                    case 3: zoomMaxField = fields[fieldIndex]; break;
                }
                if(++numFloatFields == 4) break;
            }
        }
    }

    public void SetZoom(Object combatStateObj, float desiredZoom) throws Throwable
    {
        FieldReflector.GetInstance().SetVariable(zoomMaxField, zoomTrackerObj, desiredZoom);
        FieldReflector.GetInstance().SetVariable(zoomMinField, zoomTrackerObj, desiredZoom);
    }
}
