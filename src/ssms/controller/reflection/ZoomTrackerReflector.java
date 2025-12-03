package ssms.controller.reflection;

public class ZoomTrackerReflector {
    FieldReflector zoomAField;
    FieldReflector zoomBField;
    FieldReflector zoomMinField;
    FieldReflector zoomMaxField;
    Object zoomTrackerObj;
    // we're basically crossing our fingers here and hoping the order of the fields never changes. it probably will eventually. can't do much about it.
    public ZoomTrackerReflector(Object zoomTrackerObj) throws Throwable {
        this.zoomTrackerObj = zoomTrackerObj;
        ClassReflector zoomTrackerReflector = new ClassReflector(zoomTrackerObj.getClass());

        int numFloatFields = 0;
        for(FieldReflector field : zoomTrackerReflector.getDeclaredFields()) {
            if(field.getType() == float.class) {
                switch(numFloatFields) {
                    case 0 -> zoomAField = field;
                    case 1 -> zoomBField = field;
                    case 2 -> zoomMinField = field;
                    case 3 -> zoomMaxField = field;
                }
                if(++numFloatFields == 4) break;
            }
        }
    }

    public void setZoom(float desiredZoom) {
        zoomAField.set(zoomTrackerObj, desiredZoom);
        zoomBField.set(zoomTrackerObj, desiredZoom);
    }

    public float getZoom() {
        return (float)zoomAField.get(zoomTrackerObj);
    }

    public void forceZoom(float desiredZoom) throws Throwable
    {
        zoomMaxField.set(zoomTrackerObj, desiredZoom);
        zoomMinField.set(zoomTrackerObj, desiredZoom);
    }
}
