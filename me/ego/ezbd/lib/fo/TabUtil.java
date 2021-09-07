package me.ego.ezbd.lib.fo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class TabUtil {
    @SafeVarargs
    public static <T> List<String> complete(String partialName, T... all) {
        List<String> clone = new ArrayList();
        if (all != null) {
            Object[] var3 = all;
            int var4 = all.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                T s = var3[var5];
                if (s != null) {
                    if (s instanceof Iterable) {
                        Iterator var12 = ((Iterable)s).iterator();

                        while(var12.hasNext()) {
                            Object iterable = var12.next();
                            clone.add(iterable instanceof Enum ? iterable.toString().toLowerCase() : SerializeUtil.serialize(iterable).toString());
                        }
                    } else if (s instanceof Enum[]) {
                        Enum[] var11 = (Enum[])((Enum[])((Enum[])s))[0].getClass().getEnumConstants();
                        int var13 = var11.length;

                        for(int var9 = 0; var9 < var13; ++var9) {
                            Object iterable = var11[var9];
                            clone.add(iterable.toString().toLowerCase());
                        }
                    } else {
                        boolean lowercase = s instanceof Enum;
                        String parsed = SerializeUtil.serialize(s).toString();
                        if (!"".equals(parsed)) {
                            clone.add(lowercase ? parsed.toLowerCase() : parsed);
                        }
                    }
                }
            }
        }

        return complete(partialName, (Iterable)clone);
    }

    public static List<String> complete(String partialName, Iterable<String> all) {
        ArrayList<String> tab = new ArrayList();
        Iterator iterator = all.iterator();

        String val;
        while(iterator.hasNext()) {
            val = (String)iterator.next();
            tab.add(val);
        }

        partialName = partialName.toLowerCase();
        iterator = tab.iterator();

        while(iterator.hasNext()) {
            val = (String)iterator.next();
            if (!val.toLowerCase().startsWith(partialName)) {
                iterator.remove();
            }
        }

        Collections.sort(tab);
        return tab;
    }

    private TabUtil() {
    }
}