package com.tfjybj.framework.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by will on 12/07/2017.
 */
public class GsonWrapper {

    public static String toJson(Object obj) {
        if (obj == null) {
            return "";
        }

        Gson gson = new Gson();
        String str = gson.toJson(obj);
        return str;
    }

    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return "";
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String str = gson.toJson(obj);
        return str;
    }
}
