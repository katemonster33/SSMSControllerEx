
#include <windows.h>
#include <jni.h>
#include "dxversion.h"
#include <dinput.h>

#pragma once
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_ssms_controller_DirectInputDeviceEx_nGetVidPid(JNIEnv *env, jclass unused, jlong address) {
    LPDIRECTINPUTDEVICE8 lpDevice = (LPDIRECTINPUTDEVICE8)(INT_PTR)address;
    DIPROPDWORD dipdw;

    if (!lpDevice) {
        return 0;
    }

    dipdw.diph.dwSize = sizeof(dipdw);
    dipdw.diph.dwHeaderSize = sizeof(dipdw.diph);
    dipdw.diph.dwObj = 0;
    dipdw.diph.dwHow = DIPH_DEVICE;
    dipdw.dwData = 0;

    if (IDirectInputDevice8_GetProperty(lpDevice, DIPROP_VIDPID, &dipdw.diph) != 0) {
        return 0;
    }

	return dipdw.dwData;
}

#ifdef __cplusplus
}
#endif