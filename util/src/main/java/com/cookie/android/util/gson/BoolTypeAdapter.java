package com.cookie.android.util.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import com.cookie.android.util.FormatUtils;

/**
 * BoolTypeAdapter
 * Author: ZhangLingfei
 * Date : 2019/5/15 0015
 */
public class BoolTypeAdapter implements JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
            return json.getAsBoolean();
        } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            int number = json.getAsInt();
            return number > 0;
        } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String value = json.getAsString();
            int number = FormatUtils.toInt(value, 0);
            return !"false".equalsIgnoreCase(value) && ("true".equalsIgnoreCase(value) || number > 0);
        }
        return false;
    }
//    @Override
//    public void write(JsonWriter out, Boolean value) throws IOException {
//        if (value == null) {
//            out.value(false);
//        } else {
//            out.value(value);
//        }
//    }
//
//    @Override
//    public Boolean read(JsonReader in) throws IOException {
//        if (in.peek() == JsonToken.BOOLEAN) {
//            return in.nextBoolean();
//        } else if (in.peek() == JsonToken.NUMBER) {
//            int number = FormatUtils.toInt(in.nextString());
//            return number > 0;
//        } else if (in.peek() == JsonToken.STRING) {
//            String value = in.nextString();
//            return true;
//        }
//    }
}
