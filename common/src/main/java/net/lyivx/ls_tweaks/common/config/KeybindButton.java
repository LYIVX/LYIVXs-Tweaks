package net.lyivx.ls_tweaks.common.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Inline keybind button that captures next key press and updates mapping + config. */
public class KeybindButton extends AbstractButton {
    /** Limited set of keys for capture/persistence and polling. */
    public enum KeyOption {
        UNKNOWN(GLFW.GLFW_KEY_UNKNOWN, "Unbound"),
        // Digits
        D0(GLFW.GLFW_KEY_0, "0"), D1(GLFW.GLFW_KEY_1, "1"), D2(GLFW.GLFW_KEY_2, "2"), D3(GLFW.GLFW_KEY_3, "3"),
        D4(GLFW.GLFW_KEY_4, "4"), D5(GLFW.GLFW_KEY_5, "5"), D6(GLFW.GLFW_KEY_6, "6"), D7(GLFW.GLFW_KEY_7, "7"),
        D8(GLFW.GLFW_KEY_8, "8"), D9(GLFW.GLFW_KEY_9, "9"),
        // Letters
        A(GLFW.GLFW_KEY_A, "A"), B(GLFW.GLFW_KEY_B, "B"), C(GLFW.GLFW_KEY_C, "C"), D(GLFW.GLFW_KEY_D, "D"),
        E(GLFW.GLFW_KEY_E, "E"), F(GLFW.GLFW_KEY_F, "F"), G(GLFW.GLFW_KEY_G, "G"), H(GLFW.GLFW_KEY_H, "H"),
        I(GLFW.GLFW_KEY_I, "I"), J(GLFW.GLFW_KEY_J, "J"), K(GLFW.GLFW_KEY_K, "K"), L(GLFW.GLFW_KEY_L, "L"),
        M(GLFW.GLFW_KEY_M, "M"), N(GLFW.GLFW_KEY_N, "N"), O(GLFW.GLFW_KEY_O, "O"), P(GLFW.GLFW_KEY_P, "P"),
        Q(GLFW.GLFW_KEY_Q, "Q"), R(GLFW.GLFW_KEY_R, "R"), S(GLFW.GLFW_KEY_S, "S"), T(GLFW.GLFW_KEY_T, "T"),
        U(GLFW.GLFW_KEY_U, "U"), V(GLFW.GLFW_KEY_V, "V"), W(GLFW.GLFW_KEY_W, "W"), X(GLFW.GLFW_KEY_X, "X"),
        Y(GLFW.GLFW_KEY_Y, "Y"), Z(GLFW.GLFW_KEY_Z, "Z"),
        // Function keys
        F1(GLFW.GLFW_KEY_F1, "F1"), F2(GLFW.GLFW_KEY_F2, "F2"), F3(GLFW.GLFW_KEY_F3, "F3"), F4(GLFW.GLFW_KEY_F4, "F4"),
        F5(GLFW.GLFW_KEY_F5, "F5"), F6(GLFW.GLFW_KEY_F6, "F6"), F7(GLFW.GLFW_KEY_F7, "F7"), F8(GLFW.GLFW_KEY_F8, "F8"),
        F9(GLFW.GLFW_KEY_F9, "F9"), F10(GLFW.GLFW_KEY_F10, "F10"), F11(GLFW.GLFW_KEY_F11, "F11"), F12(GLFW.GLFW_KEY_F12, "F12"),
        // Navigation/modifiers/common symbols
        ESCAPE(GLFW.GLFW_KEY_ESCAPE, "Escape"), TAB(GLFW.GLFW_KEY_TAB, "Tab"), CAPS_LOCK(GLFW.GLFW_KEY_CAPS_LOCK, "Caps Lock"),
        LEFT_SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT, "Left Shift"), RIGHT_SHIFT(GLFW.GLFW_KEY_RIGHT_SHIFT, "Right Shift"),
        LEFT_CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL, "Left Ctrl"), RIGHT_CONTROL(GLFW.GLFW_KEY_RIGHT_CONTROL, "Right Ctrl"),
        LEFT_ALT(GLFW.GLFW_KEY_LEFT_ALT, "Left Alt"), RIGHT_ALT(GLFW.GLFW_KEY_RIGHT_ALT, "Right Alt"),
        SPACE(GLFW.GLFW_KEY_SPACE, "Space"), ENTER(GLFW.GLFW_KEY_ENTER, "Enter"), BACKSPACE(GLFW.GLFW_KEY_BACKSPACE, "Backspace"),
        INSERT(GLFW.GLFW_KEY_INSERT, "Insert"), DELETE(GLFW.GLFW_KEY_DELETE, "Delete"), HOME(GLFW.GLFW_KEY_HOME, "Home"), END(GLFW.GLFW_KEY_END, "End"),
        PAGE_UP(GLFW.GLFW_KEY_PAGE_UP, "Page Up"), PAGE_DOWN(GLFW.GLFW_KEY_PAGE_DOWN, "Page Down"),
        ARROW_UP(GLFW.GLFW_KEY_UP, "Up"), ARROW_DOWN(GLFW.GLFW_KEY_DOWN, "Down"), ARROW_LEFT(GLFW.GLFW_KEY_LEFT, "Left"), ARROW_RIGHT(GLFW.GLFW_KEY_RIGHT, "Right"),
        GRAVE(GLFW.GLFW_KEY_GRAVE_ACCENT, "`"), MINUS(GLFW.GLFW_KEY_MINUS, "-"), EQUALS(GLFW.GLFW_KEY_EQUAL, "="),
        LEFT_BRACKET(GLFW.GLFW_KEY_LEFT_BRACKET, "["), RIGHT_BRACKET(GLFW.GLFW_KEY_RIGHT_BRACKET, "]"), BACKSLASH(GLFW.GLFW_KEY_BACKSLASH, "\\"),
        SEMICOLON(GLFW.GLFW_KEY_SEMICOLON, ";"), APOSTROPHE(GLFW.GLFW_KEY_APOSTROPHE, "'"), COMMA(GLFW.GLFW_KEY_COMMA, ","),
        PERIOD(GLFW.GLFW_KEY_PERIOD, "."), SLASH(GLFW.GLFW_KEY_SLASH, "/");

        private final int glfwKeyCode;
        private final String display;
        KeyOption(int glfwKeyCode, String display) { this.glfwKeyCode = glfwKeyCode; this.display = display; }
        public int code() { return glfwKeyCode; }
        @Override public String toString() { return display; }
    }
    private final String labelKey; // e.g., "config.ls_tweaks.keybind_sort_key"
    private final KeyMapping mapping;
    private final net.lyivx.ls_core.common.config.CustomConfigSpec spec;
    private final String specEnumKey; // e.g., "keybind_sort_key"
    private boolean capturing = false;
    private int currentColor = 0xFFFFFF;

    public KeybindButton(int x, int y, int w, int h,
                         String labelKey,
                         KeyMapping mapping,
                         net.lyivx.ls_core.common.config.CustomConfigSpec spec,
                         String specEnumKey) {
        super(x, y, w, h, Component.empty());
        this.labelKey = labelKey;
        this.mapping = mapping;
        this.spec = spec;
        this.specEnumKey = specEnumKey;
        setTooltip(Tooltip.create(Component.translatable(labelKey + ".tooltip")));
        updateMessage();
    }

    @Override
    public void onPress() {
        capturing = true;
        currentColor = 0xFFFF55; // gold while capturing
        this.setFocused(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!capturing) return super.keyPressed(keyCode, scanCode, modifiers);
        if (applyKey(keyCode)) {
            capturing = false;
            updateMessage();
        }
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Ignore char input; we rely on keyPressed GLFW codes to bind
        return capturing || super.charTyped(codePoint, modifiers);
    }

    private boolean applyKey(int keyCode) {
        if (mapping == null) return true;
        // Disallow ESC and SUPER (Windows/Command) keys
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER) {
            return false;
        }
        try {
            InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            try {
                var m = KeyMapping.class.getDeclaredMethod("setKey", InputConstants.Key.class);
                m.setAccessible(true);
                m.invoke(mapping, key);
            } catch (NoSuchMethodException nsme) {
                var f = KeyMapping.class.getDeclaredField("key");
                f.setAccessible(true);
                f.set(mapping, key);
            }
            KeyMapping.resetMapping();
            // Persist choice in our enum store (best-effort map)
            KeyOption opt = mapCodeToKeyOption(keyCode);
            spec.setEnum(specEnumKey, opt);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static KeyOption mapCodeToKeyOption(int keyCode) {
        for (KeyOption o : KeyOption.values()) if (o.code() == keyCode) return o;
        return KeyOption.UNKNOWN;
    }

    private static int currentKeyValue(KeyMapping m) {
        try {
            var f = KeyMapping.class.getDeclaredField("key");
            f.setAccessible(true);
            InputConstants.Key k = (InputConstants.Key) f.get(m);
            if (k != null) return k.getValue();
        } catch (Throwable ignored) {
        }
        return m.getDefaultKey().getValue();
    }

    private static boolean hasConflict(KeyMapping self, int keyCode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null || mc.options.keyMappings == null) return false;
        for (KeyMapping other : mc.options.keyMappings) {
            if (other == null || other == self) continue;
            int ov = currentKeyValue(other);
            if (ov == keyCode && keyCode != InputConstants.UNKNOWN.getValue()) return true;
        }
        return false;
    }

    private static Component keyName(int keyCode) {
        return InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName();
    }

    private void updateMessage() {
        int val = mapping != null ? currentKeyValue(mapping) : InputConstants.UNKNOWN.getValue();
        boolean conflict = mapping != null && hasConflict(mapping, val);
        currentColor = conflict ? 0xFF5555 : 0xFFFFFF;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Cancel capture if we lost focus (clicked elsewhere)
        if (capturing && !this.isFocused()) {
            capturing = false;
            updateMessage();
        }
        if (capturing) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                long win = mc.getWindow().getWindow();
                for (KeyOption o : KeyOption.values()) {
                    int code = o.code();
                    if (code != InputConstants.UNKNOWN.getValue() && InputConstants.isKeyDown(win, code)) {
                        if (applyKey(code)) {
                            capturing = false;
                            updateMessage();
                        }
                        break;
                    }
                }
            }
        }
        super.renderWidget(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderString(GuiGraphics g, net.minecraft.client.gui.Font font, int ignoredColor) {
        int val = mapping != null ? currentKeyValue(mapping) : InputConstants.UNKNOWN.getValue();
        boolean conflict = mapping != null && hasConflict(mapping, val);
        boolean capturingNow = this.capturing;

        String label = Component.translatable(labelKey).getString();
        String bracketInner = capturingNow
                ? Component.translatable("config.ls_tweaks.keybind_capture_prompt").getString()
                : keyName(val).getString();
        String bracketOpen = " [";
        String bracketClose = "]";

        int labelWidth = capturingNow ? 0 : font.width(label);
        int bracketOpenWidth = font.width(bracketOpen);
        int bracketInnerWidth = font.width(bracketInner);
        int bracketCloseWidth = font.width(bracketClose);
        int totalWidth = labelWidth + bracketOpenWidth + bracketInnerWidth + bracketCloseWidth;

        int avail = this.width - 8;
        int baselineY = getY() + (this.height - 8) / 2;

        // Scissor to the button's inner rect to avoid overflow
        int sx1 = getX() + 4, sy1 = getY() + 1, sx2 = getX() + this.width - 4, sy2 = getY() + this.height - 1;
        g.enableScissor(sx1, sy1, sx2, sy2);

        int scrollOffset = 0;
        if (totalWidth > avail) {
            int overflow = totalWidth - avail;
            // Back-and-forth with pauses at ends
            double speedPxPerSec = 5.0;   // a bit faster
            double pauseMs = 5000.0;         // pause at each end
            double travelMs = (overflow / speedPxPerSec) * 1000.0;
            double cycleMs = pauseMs + travelMs + pauseMs + travelMs; // left pause → right travel → right pause → left travel
            double nowMs = System.currentTimeMillis() % (long)cycleMs;

            if (nowMs < pauseMs) {
                scrollOffset = 0; // left pause
            } else if (nowMs < pauseMs + travelMs) {
                double dt = nowMs - pauseMs;
                scrollOffset = (int)Math.round((dt / 1000.0) * speedPxPerSec);
            } else if (nowMs < pauseMs + travelMs + pauseMs) {
                scrollOffset = overflow; // right pause
            } else {
                double dt = nowMs - (pauseMs + travelMs + pauseMs);
                scrollOffset = overflow - (int)Math.round((dt / 1000.0) * speedPxPerSec);
            }
        }

        int startX = getX() + 4 + (avail - Math.min(totalWidth, avail)) / 2 - scrollOffset;

        // Draw label (always white) unless capturing (hide label)
        int x = startX;
        if (!capturingNow) {
            g.drawString(font, label, x, baselineY, 0xFFFFFF, true);
            x += labelWidth;
        }

        // Draw bracketed part; color: gold if capturing, else red if conflict, else white
        int bracketColor = capturingNow ? 0xFFFF55 : (conflict ? 0xFF5555 : 0xFFFFFF);
        g.drawString(font, bracketOpen, x, baselineY, bracketColor, true); x += bracketOpenWidth;
        g.drawString(font, bracketInner, x, baselineY, bracketColor, true); x += bracketInnerWidth;
        g.drawString(font, bracketClose, x, baselineY, bracketColor, true);

        g.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }
}


