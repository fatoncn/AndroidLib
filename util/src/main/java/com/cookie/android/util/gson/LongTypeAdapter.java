package com.cookie.android.util.gson;

import com.cookie.android.util.FormatUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


/**
 * LongTypeAdapter
 * Author: ZhangLingfei
 * Date : 2019/5/15 0015
 */
public class LongTypeAdapter implements JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
            return json.getAsJsonPrimitive().getAsLong();
        else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String value = json.getAsJsonPrimitive().getAsString();
            long number = FormatUtils.toLong(value, 0);
            float number0 = FormatUtils.toFloat(value);
            if (number == 0L)
                return (long) number0;
            else
                return number;
        }
        return 0L;
    }
//    @Override
//    public void write(JsonWriter out, Long value) throws IOException {
//        if (value == null)
//            out.value(0);
//        else
//            out.value(value);
//    }
//
//    @Override
//    public Long read(JsonReader in) throws IOException {
//        if (in.peek() == JsonToken.NUMBER) {
//            return in.nextLong();
//        } else if (in.peek() == JsonToken.STRING) {
//            String value = in.nextString();
//            long number = FormatUtils.toLong(value, 0);
//            float number0 = FormatUtils.toFloat(value);
//            if (number == 0L)
//                return (long) number0;
//            else
//                return number;
//        } else {
//            return 0L;
//        }
//    }
}
