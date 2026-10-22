package com.nimbo.launcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Package-name based ordering, independent of Android views and installed-app flags. */
final class AppOrder {
    static List<String> visible(Collection<String> available, Set<String> hidden, String savedOrder) {
        Set<String> remaining = new LinkedHashSet<>(available);
        remaining.removeAll(hidden);
        List<String> result = new ArrayList<>();
        for (String pkg : savedOrder.split(",")) if (remaining.remove(pkg)) result.add(pkg);
        result.addAll(remaining);
        return result;
    }

    static List<String> move(List<String> visible, String pkg, int position) {
        List<String> result = new ArrayList<>(visible);
        if (result.remove(pkg)) result.add(Math.max(0, Math.min(position, result.size())), pkg);
        return result;
    }

    static String save(List<String> visible, String previousOrder) {
        Set<String> all = new LinkedHashSet<>(visible);
        for (String pkg : previousOrder.split(",")) if (!pkg.isEmpty()) all.add(pkg);
        return String.join(",", all);
    }
}
