package com.tfjybj.framework.auth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListHelper {
    public static <T> List<T> filter(List<T> list, ListFilter<T> filter) {
        ArrayList<T> r = new ArrayList<T>();
        for (T t : list) {
            if (filter.filter(t)) {
                r.add(t);
            }
        }
        r.trimToSize();
        return r;
    }

    public static <T> List<T> filter(Set<T> list, ListFilter<T> filter) {
        List<T> r = new ArrayList<>();
        for (T t : list) {
            if (filter.filter(t)) {
                r.add(t);
            }
        }
        return r;
    }
}
