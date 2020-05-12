package com.cookie.android.util.log;


import com.cookie.android.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JsonLog {

    public static void printJson(String tag, String msg, String headString) {

        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(LogConfig.JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(LogConfig.JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        LogUtil.printLine(tag, true);
        message = headString + LogConfig.LINE_SEPARATOR + message;
        String[] lines = message.split(LogConfig.LINE_SEPARATOR);
        for (String line : lines) {
            Logger.d(tag, "║ " + line);
        }
        LogUtil.printLine(tag, false);
    }
}
