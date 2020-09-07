package com.cookie.android.util.gson;

import com.cookie.android.util.FormatUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * IntTypeAdapter
 * Author: ZhangLingfei
 * Date : 2019/5/15 0015
 */
public class IntTypeAdapter implements JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
            return json.getAsJsonPrimitive().getAsInt();
        else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String value = json.getAsJsonPrimitive().getAsString();
            int number = FormatUtils.toInt(value, -1);
            float number0 = FormatUtils.toFloat(value);
            if (number == -1)
                return (int) number0;
            else
                return number;
        }
        return -1;
    }
//
//    @Override
//    public void write(JsonWriter out, Integer value) throws IOException {
//        if (value == null)
//            out.value(0);
//        else
//            out.value(value);
//    }
//
//    @Override
//    public Integer read(JsonReader in) throws IOException {
//        if (in.peek() == JsonToken.NUMBER) {
//            return in.nextInt();
//        } else if (in.peek() == JsonToken.NUMBER || in.peek() == JsonToken.STRING) {
//            String value = in.nextString();
//            int number = FormatUtils.toInt(value, 0);
//            float number0 = FormatUtils.toFloat(value);
//            if (number == 0)
//                return (int) number0;
//            else
//                return number;
//        } else {
//            return 0;
//        }
//    }
}
