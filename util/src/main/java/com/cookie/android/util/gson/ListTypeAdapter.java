package com.cookie.android.util.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListTypeAdapter implements JsonDeserializer<List<?>> {

    @SuppressWarnings("unchecked")
    @Override
    public List<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray())
            return new ArrayList<>();
        JsonArray array = json.getAsJsonArray();
        Type itemType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
        List list = new ArrayList();
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            Object item = context.deserialize(element, itemType);
            if (item != null)
                list.add(item);
        }
        return list;
    }
}
