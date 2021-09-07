package me.ego.ezbd.lib.fo.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;

public final class LagCatcher {
    private static volatile Map<String, Long> startTimesMap = new HashMap();
    private static volatile Map<String, List<Long>> durationsMap = new HashMap();

    public static void start(String section) {
        if (SimpleSettings.LAG_THRESHOLD_MILLIS != -1) {
            startTimesMap.put(section, System.nanoTime());
        }
    }

    public static void end(String section) {
        end(section, false);
    }

    public static void end(String section, boolean rapid) {
        end(section, rapid ? 0 : SimpleSettings.LAG_THRESHOLD_MILLIS, "{section} took {time} ms");
    }

    public static void end(String section, int thresholdMs, String message) {
        double lag = finishAndCalculate(section);
        if (lag > (double)thresholdMs && SimpleSettings.LAG_THRESHOLD_MILLIS != -1) {
            message = (SimplePlugin.hasInstance() ? "[" + SimplePlugin.getNamed() + " " + SimplePlugin.getVersion() + "] " : "") + message.replace("{section}", section).replace("{time}", MathUtil.formatTwoDigits(lag));
            System.out.println(message);
        }

    }

    public static void performanceTest(int cycles, String name, Runnable code) {
        Valid.checkBoolean(cycles > 0, "Cycles must be above 0", new Object[0]);
        start(name + "-whole");
        List<Double> lagMap = new ArrayList();

        for(int i = 0; i < cycles; ++i) {
            start(name);
            code.run();
            lagMap.add(finishAndCalculate(name));
        }

        System.out.println("Test '" + name + "' took " + MathUtil.formatTwoDigits(finishAndCalculate(name + "-whole")) + " ms. Average " + MathUtil.average(lagMap) + " ms");
        if (!durationsMap.isEmpty()) {
            Iterator var12 = durationsMap.entrySet().iterator();

            while(var12.hasNext()) {
                Entry<String, List<Long>> entry = (Entry)var12.next();
                String section = (String)entry.getKey();
                long duration = 0L;

                long sectionDuration;
                for(Iterator var9 = ((List)entry.getValue()).iterator(); var9.hasNext(); duration += sectionDuration) {
                    sectionDuration = (Long)var9.next();
                }

                System.out.println("\tSection '" + section + "' took " + MathUtil.formatTwoDigits((double)duration / 1000000.0D));
            }

            System.out.println("Section measurement ended.");
            durationsMap.clear();
        }

    }

    public static void performancePartStart(String section) {
        List<Long> sectionDurations = (List)durationsMap.get(section);
        if (sectionDurations == null) {
            sectionDurations = new ArrayList();
            durationsMap.put(section, sectionDurations);
        }

        ((List)sectionDurations).add(System.nanoTime());
    }

    public static void performancePartSnap(String section) {
        Valid.checkBoolean(durationsMap.containsKey(section), "Section " + section + " is not measured! Are you calling it from performanceTest?", new Object[0]);
        List<Long> sectionDurations = (List)durationsMap.get(section);
        int index = sectionDurations.size() - 1;
        long nanoTime = (Long)sectionDurations.get(index);
        long duration = System.nanoTime() - nanoTime;
        sectionDurations.set(index, duration);
    }

    public static void took(String section) {
        Long nanoTime = (Long)startTimesMap.get(section);
        String message = section + " took " + MathUtil.formatTwoDigits(nanoTime == null ? 0.0D : (double)(System.nanoTime() - nanoTime) / 1000000.0D) + " ms";
        if (SimplePlugin.hasInstance()) {
            Common.logNoPrefix(new String[]{"[{plugin_name} {plugin_version}] " + message});
        } else {
            System.out.println("[LagCatcher] " + message);
        }

    }

    private static double finishAndCalculate(String section) {
        Long nanoTime = (Long)startTimesMap.remove(section);
        return nanoTime == null ? 0.0D : (double)(System.nanoTime() - nanoTime) / 1000000.0D;
    }

    private LagCatcher() {
    }
}
