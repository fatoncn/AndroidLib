package com.cookie.android.util.gson;

import com.cookie.android.util.Utils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * CompatibleTypeAdapter
 * Author: ZhangLingfei
 * Date : 2019/5/15 0015
 */
public class CompatibleTypeAdapter implements JsonDeserializer<GsonCompatible> {

    @Override
    public GsonCompatible deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Class clz = Utils.getRawType(typeOfT);
        GsonCompatible result;
        try {
            result = (GsonCompatible) clz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            Utils.fillJson(result, object);
        }
        if (json.isJsonNull())
            return null;
        return result;
    }
}
