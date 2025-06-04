package ssms.controller;

final class DirectInputDeviceEx {
    private static native long nGetVidPid(long address);

    public static long GetVidPid(long address) {
        return nGetVidPid(address);
    }

    static {
        System.loadLibrary("native");
    }
}
