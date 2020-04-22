package com.cookie.android.util.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * StringTypeAdapter
 * Author: ZhangLingfei
 * Date : 2019/5/15 0015
 */
public class StringTypeAdapter implements JsonDeserializer<String> {
//    @Override
//    public void write(JsonWriter out, String value) throws IOException {
//        if (value == null) {
//            value = "";
//        }
//        out.value(value);
//    }

//    @Override
//    public String read(JsonReader in) throws IOException {
//        StringBuilder result = new StringBuilder();
//        if (in.peek() == JsonToken.STRING) {
//            result.append(in.nextString());
//        } else if (in.peek() == JsonToken.NULL) {
//            in.nextNull();
//            result.append("");
//        } else if (in.peek() == JsonToken.NUMBER) {
//            result.append(in.nextString());
//        } else if (in.peek() == JsonToken.BOOLEAN) {
//            result.append(in.nextBoolean());
//        } else if (in.peek() == JsonToken.BEGIN_ARRAY) {
//            in.beginArray();
//            result.append("[");
//            while (in.hasNext()) {
//                result.append(read(in));
//                result.append(",");
//            }
//            in.endArray();
//            if (result.length() > 1)
//                result.deleteCharAt(result.length() - 1);
//            result.append("]");
//        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
//            in.beginObject();
//            result.append("{");
//            while (in.hasNext()) {
//                result.append("\"");
//                result.append(in.nextName());
//                result.append("\":");
//                result.append(read(in));
//                result.append(",");
//            }
//            in.endObject();
//            if (result.length() > 1)
//                result.deleteCharAt(result.length() - 1);
//            result.append("}");
//        }
//        return result.toString();
//    }

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
            return json.getAsString();
        if (json.isJsonNull())
            return "";
        return json.toString();
    }
}
