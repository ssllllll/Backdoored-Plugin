package me.ego.ezbd.lib.fo.remain;

class SneakyThrow {
    SneakyThrow() {
    }

    public static void sneaky(Throwable t) {
        throw (RuntimeException)superSneaky(t);
    }

    private static <T extends Throwable> T superSneaky(Throwable t) throws T {
        throw t;
    }
}