package ssms.controller;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.InputImplementation;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class InputShim implements InputImplementation {
    InputImplementation realImpl;
    Integer mouseX, mouseY;
    Integer curMouseX, curMouseY;

    public InputShim(InputImplementation impl) {
        realImpl = impl;
    }

    public void stopOverrideMousePos() {
        mouseX = mouseY = curMouseX = curMouseY = null;
    }

    public void overrideMousePos(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    @Override
    public boolean hasWheel() {
        return realImpl.hasWheel();
    }

    @Override
    public int getButtonCount() {
        return realImpl.getButtonCount();
    }

    @Override
    public void createMouse() throws LWJGLException {
        realImpl.createMouse();
    }

    @Override
    public void destroyMouse() {
        realImpl.destroyMouse();
    }

    @Override
    public void pollMouse(IntBuffer intBuffer, ByteBuffer byteBuffer) {
        realImpl.pollMouse(intBuffer, byteBuffer);
        if(mouseX != null) {
            intBuffer.put(0, (int)mouseX);
        }
        if(mouseY != null) {
            intBuffer.put(1, (int)mouseY);
        }
    }

    @Override
    public void readMouse(ByteBuffer byteBuffer) {
        if(mouseX == null || mouseY == null || (mouseX.equals(curMouseX) && mouseY.equals(curMouseY))) {
            realImpl.readMouse(byteBuffer);
        } else {
            if(byteBuffer.remaining() >= 22) {
                byteBuffer.put((byte)0xFF);
                byteBuffer.put((byte)0);
                byteBuffer.putInt((int)mouseX);
                byteBuffer.putInt((int)mouseY);
                byteBuffer.putInt(0);
                byteBuffer.putLong(10000000L);
                curMouseX = mouseX;
                curMouseY = mouseY;
            }
        }
    }

    @Override
    public void grabMouse(boolean b) {
        realImpl.grabMouse(b);
    }

    @Override
    public int getNativeCursorCapabilities() {
        return realImpl.getNativeCursorCapabilities();
    }

    @Override
    public void setCursorPosition(int i, int i1) {
        realImpl.setCursorPosition(i, i1);
    }

    @Override
    public void setNativeCursor(Object o) throws LWJGLException {
        realImpl.setNativeCursor(o);
    }

    @Override
    public int getMinCursorSize() {
        return realImpl.getMinCursorSize();
    }

    @Override
    public int getMaxCursorSize() {
        return realImpl.getMaxCursorSize();
    }

    @Override
    public void createKeyboard() throws LWJGLException {
        realImpl.createKeyboard();
    }

    @Override
    public void destroyKeyboard() {
        realImpl.destroyKeyboard();
    }

    @Override
    public void pollKeyboard(ByteBuffer byteBuffer) {
        realImpl.pollKeyboard(byteBuffer);
    }

    @Override
    public void readKeyboard(ByteBuffer byteBuffer) {
        realImpl.readKeyboard(byteBuffer);
    }

    @Override
    public Object createCursor(int i, int i1, int i2, int i3, int i4, IntBuffer intBuffer, IntBuffer intBuffer1) throws LWJGLException {
        return realImpl.createCursor(i, i1, i2, i3, i4, intBuffer, intBuffer1);
    }

    @Override
    public void destroyCursor(Object o) {
        realImpl.destroyCursor(o);
    }

    @Override
    public int getWidth() {
        return realImpl.getWidth();
    }

    @Override
    public int getHeight() {
        return realImpl.getHeight();
    }

    @Override
    public boolean isInsideWindow() {
        return realImpl.isInsideWindow();
    }
}
