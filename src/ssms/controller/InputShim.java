package ssms.controller;

import com.fs.starfarer.api.Global;

import java.awt.*;
import java.util.List;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.InputImplementation;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.FieldReflector;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class InputShim implements InputImplementation {
    private static InputShim instance;

    public static void install() {
        if(instance != null) {
            Global.getLogger(InputShim.class).info("Input shim already installed!");
        } else {
            try {
                var inputImplField = ClassReflector.GetInstance().getDeclaredField(Mouse.class, "implementation");
                var inputImplFieldKb = ClassReflector.GetInstance().getDeclaredField(Keyboard.class, "implementation");
                InputImplementation originalImpl = (InputImplementation) FieldReflector.GetInstance().GetVariable(inputImplField, null);
                InputImplementation originalImplKb = (InputImplementation) FieldReflector.GetInstance().GetVariable(inputImplFieldKb, null);
                if (originalImpl != originalImplKb) {
                    // we probably won't ever hit this, this is only if we recreate the InputEventReflector and then reinstall the existing shim
                    throw new IllegalArgumentException("Can't create input shim - keyboard/mouse input implementations use different objects!");
                } else {
                    instance = new InputShim(originalImpl);
                    FieldReflector.GetInstance().SetVariable(inputImplField, null, instance);
                    FieldReflector.GetInstance().SetVariable(inputImplFieldKb, null, instance);
                }
            } catch (Throwable ex) {
                Global.getLogger(InputShim.class).fatal("Couldn't install input shim!", ex);
            }
        }
    }

    public static void mouseMove(int x, int y)
    {
        instance.pendingEvents.add(new InputEvent(x, y));
        instance.mouseX = x;
        instance.mouseY = y;
    }

    public static void mouseDown(int x, int y, int btn)
    {
        instance.pendingEvents.add(new InputEvent(x, y, btn, true));
    }

    public static void mouseUp(int x, int y, int btn)
    {
        instance.pendingEvents.add(new InputEvent(x, y, btn, false));
    }

    public static void keyDown(int keyCode, char keyChar)
    {
        instance.pendingEvents.add(new InputEvent(keyCode, keyChar, true));
    }

    public static void keyUp(int keyCode, char keyChar)
    {
        instance.pendingEvents.add(new InputEvent(keyCode, keyChar, false));
    }

    public static void clearAll()
    {
        instance.pendingEvents.clear();
        instance.mouseX = instance.mouseY = null;
    }

    InputImplementation realImpl;
    Integer mouseX, mouseY;
    List<InputEvent> pendingEvents = new ArrayList<>();

    private InputShim(InputImplementation impl) {
        realImpl = impl;
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
        if(pendingEvents.isEmpty() || pendingEvents.get(0).eventType != EventType.MOUSE) {
            realImpl.readMouse(byteBuffer);
        } else {
            if(byteBuffer.remaining() >= 22) {
                var evt = pendingEvents.get(0);
                byteBuffer.put((byte)evt.mouseBtn);
                byteBuffer.put((byte)(evt.state ? 1 : 0));
                byteBuffer.putInt(evt.mouseX);
                byteBuffer.putInt(evt.mouseY);
                byteBuffer.putInt(0);
                byteBuffer.putLong(10000000L);
                pendingEvents.remove(0);
                if(pendingEvents.isEmpty()) {
                    mouseX = mouseY = null;
                }
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
        int pos = byteBuffer.position();
        for (InputEvent pendingEvent : pendingEvents) {
            if (pendingEvent.eventType == EventType.KEYBOARD && pendingEvent.keyCode < byteBuffer.limit()) {
                byteBuffer.put(pendingEvent.keyCode, (byte) (pendingEvent.state ? 1 : 0));
            }
        }
        byteBuffer.position(pos);
    }

    @Override
    public void readKeyboard(ByteBuffer byteBuffer) {
        if(pendingEvents.isEmpty() || pendingEvents.get(0).eventType == EventType.KEYBOARD) {
            realImpl.readKeyboard(byteBuffer);
        } else {
            if(byteBuffer.remaining() >= 18) {
                var evt = pendingEvents.get(0);
                byteBuffer.putInt(evt.keyCode & 255);
                byteBuffer.put((byte)(evt.state ? 1 : 0));
                byteBuffer.putInt((int)evt.ch);
                byteBuffer.putLong(0x10000L);
                byteBuffer.put((byte)0);
            }
        }
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

    private static enum EventType
    {
        MOUSE,
        KEYBOARD
    }

    private static class InputEvent
    {
        EventType eventType;
        int mouseX, mouseY;
        int mouseBtn;
        boolean state;

        int keyCode;
        char ch;

        public InputEvent(int mouseX, int mouseY, int btn, boolean state)
        {
            this.eventType = EventType.MOUSE;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.mouseBtn = btn;
            this.state = state;
        }

        public InputEvent(int mouseX, int mouseY)
        {
            this(mouseX, mouseY, 0xFF, false);
        }

        public InputEvent(int keyCode, char ch, boolean state)
        {
            this.eventType = EventType.KEYBOARD;
            this.keyCode = keyCode;
            this.ch = ch;
            this.state = state;
        }
    }
}
