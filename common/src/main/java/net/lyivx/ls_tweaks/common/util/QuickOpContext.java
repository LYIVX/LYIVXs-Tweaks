package net.lyivx.ls_tweaks.common.util;

public final class QuickOpContext {
    private static final ThreadLocal<Boolean> QUICK_ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void enter() { QUICK_ACTIVE.set(Boolean.TRUE); }
    public static void exit() { QUICK_ACTIVE.set(Boolean.FALSE); }
    public static boolean isActive() { return QUICK_ACTIVE.get(); }

    private QuickOpContext() {}
}


