package ssms.controller;

import com.fs.starfarer.api.Global;

import java.awt.*;
import java.util.HashSet;
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
    static Integer mouseX, mouseY;
    static List<InputEvent> pendingEvents = new ArrayList<>();
    static HashSet<Integer> keysDown = new HashSet<>();
    static HashSet<Integer> mouseBtnsDown = new HashSet<>();

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

    public static Integer getMouseX() {
        return mouseX;
    }

    public static Integer getMouseY() {
        return mouseY;
    }

    public static boolean hasMouseControl() {
        return mouseX != null || mouseY != null;
    }

    public static void remove() {
        if(instance == null) {
            Global.getLogger(InputShim.class).info("Input shim already installed!");
        } else {
            try {
                var inputImplField = ClassReflector.GetInstance().getDeclaredField(Mouse.class, "implementation");
                var inputImplFieldKb = ClassReflector.GetInstance().getDeclaredField(Keyboard.class, "implementation");
                InputImplementation originalImpl = (InputImplementation) FieldReflector.GetInstance().GetVariable(inputImplField, null);
                InputImplementation originalImplKb = (InputImplementation) FieldReflector.GetInstance().GetVariable(inputImplFieldKb, null);
                if (originalImpl != originalImplKb) {
                    // we probably won't ever hit this, if we do, then ruh-roh.
                    throw new IllegalArgumentException("Can't remove input shim - keyboard/mouse input implementations use different objects!");
                } else {
                    FieldReflector.GetInstance().SetVariable(inputImplField, null, instance.realImpl);
                    FieldReflector.GetInstance().SetVariable(inputImplFieldKb, null, instance.realImpl);
                    instance = null;
                }
            } catch (Throwable ex) {
                Global.getLogger(InputShim.class).fatal("Couldn't remove input shim!", ex);
            }
        }
    }

    public static void mouseMove(int x, int y)
    {
        pendingEvents.add(new InputEvent(x, y));
    }

    public static void mouseDownUp(int x, int y, int btn) {
        mouseDown(x, y, btn);
        mouseUp(x, y, btn);
    }

    public static void mouseDown(int x, int y, int btn)
    {
        pendingEvents.add(new InputEvent(x, y, btn, true));
        mouseBtnsDown.add(btn);
    }

    public static void mouseUp(int x, int y, int btn)
    {
        pendingEvents.add(new InputEvent(x, y, btn, false));
    }

    public static void keyDownUp(int keyCode, char keyChar)
    {
        keyDown(keyCode, keyChar);
        keyUp(keyCode, keyChar);
    }

    public static void keyDown(int keyCode, char keyChar)
    {
        pendingEvents.add(new InputEvent(keyCode, keyChar, true));
        keysDown.add(keyCode & 255);
    }

    public static void keyUp(int keyCode, char keyChar)
    {
        pendingEvents.add(new InputEvent(keyCode, keyChar, false));
    }

    public static void advance(float amount) {
        if(!pendingEvents.isEmpty()) {
            var evt = pendingEvents.get(0);
            if(evt.sent) {
                if (evt.eventType == EventType.KEYBOARD && !evt.state) {
                    keysDown.remove(evt.keyCode & 255);
                } else if(evt.eventType == EventType.MOUSE && evt.mouseBtn != 0xFF && !evt.state) {
                    mouseBtnsDown.remove(evt.mouseBtn);
                }
                pendingEvents.remove(0);
            }
        }
    }

    public static void clearAll()
    {
        pendingEvents.clear();
        keysDown.clear();
        mouseBtnsDown.clear();
        mouseX = mouseY = null;
    }

    InputImplementation realImpl;

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
        for(Integer btn : mouseBtnsDown) {
            if(btn != null) {
                byteBuffer.put((int)btn, (byte)1);
            }
        }
    }

    @Override
    public void readMouse(ByteBuffer byteBuffer) {
        if(pendingEvents.isEmpty() || pendingEvents.get(0).eventType != EventType.MOUSE || pendingEvents.get(0).sent) {
            int origPos = byteBuffer.position();
            realImpl.readMouse(byteBuffer);
            if(byteBuffer.position() != origPos) {
                // a mouse event has come in from the user's mouse after we have stopped pumping fake events, so we will stop sending our fake position
                mouseX = mouseY = null;
            }
        } else {
            if(byteBuffer.remaining() >= 22) {
                var evt = pendingEvents.get(0);
                byteBuffer.put((byte)evt.mouseBtn);
                byteBuffer.put((byte)(evt.state ? 1 : 0));
                byteBuffer.putInt(evt.mouseX);
                byteBuffer.putInt(evt.mouseY);
                byteBuffer.putInt(0);
                byteBuffer.putLong(10000000L);
                mouseX = evt.mouseX;
                mouseY = evt.mouseY;
                Global.getLogger(getClass()).debug("InputShim sending mouse event, pos:[" + evt.mouseX + "," + evt.mouseY + "], btn:" + evt.mouseBtn + ", state:" + evt.state);
                evt.sent = true;
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
        for(Integer keyDown : keysDown) {
            if(keyDown != null) {
                byteBuffer.put(keyDown, (byte)1);
            }
        }
        byteBuffer.position(pos);
    }

    @Override
    public void readKeyboard(ByteBuffer byteBuffer) {
        if(pendingEvents.isEmpty() || pendingEvents.get(0).eventType != EventType.KEYBOARD) {
            realImpl.readKeyboard(byteBuffer);
        } else {
            if(byteBuffer.remaining() >= 18) {
                var evt = pendingEvents.get(0);
                byteBuffer.putInt(evt.keyCode & 255);
                byteBuffer.put((byte)(evt.state ? 1 : 0));
                byteBuffer.putInt((int)evt.ch);
                byteBuffer.putLong(0x10000L);
                byteBuffer.put((byte)0);
                evt.sent = true;
                Global.getLogger(getClass()).debug("InputShim sending key:" + evt.keyCode + ", state:" + evt.state);
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
        boolean sent;
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
            this.sent = false;
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
