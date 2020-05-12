package com.cookie.android.util.log;

import android.text.TextUtils;

import com.cookie.android.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class HttpLog {

    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final int TOP_AND_BOTTOM_NORMAL_LINE_NUM = 30;
    private static final int NEED_TO_CUT_LINE_NUM = 70;
    private static final int MAX_TOP_AND_BOTTOM_LINE_NUM = 50;

    private static StringBuilder sFileBuffer;
    //等待打印队列
    private final static Queue<String> waitingList0 = new ConcurrentLinkedQueue<>();
    private final static Queue<HttpLogModel> waitingList1 = new ConcurrentLinkedQueue<>();
    private final static Queue<String> waitingList2 = new ConcurrentLinkedQueue<>();
    //打印日志非阻塞锁，避免http日志混乱
    private static boolean isPrinting;

    public static void printHttp(String tag, HttpLogModel httpLogModel, String headerString) {
        if (httpLogModel == null) {
            return;
        }
        if (isPrinting) {
            waitingList0.add(tag);
            waitingList1.add(httpLogModel);
            waitingList2.add(headerString);
            return;
        }
        synchronized (HttpLog.class) {
            if (isPrinting) {
                waitingList0.add(tag);
                waitingList1.add(httpLogModel);
                waitingList2.add(headerString);
                return;
            }
            isPrinting = true;
        }
        if (LogConfig.isLogToFile())
            sFileBuffer = new StringBuilder();
        logTopBorder(tag);

        log(tag, HORIZONTAL_DOUBLE_LINE + " " + headerString);
        log(tag, HORIZONTAL_DOUBLE_LINE + " " + httpLogModel.getMethod() + " : " + httpLogModel.getUrl());
        log(tag, HORIZONTAL_DOUBLE_LINE + " " + httpLogModel.getCost() + " ms");

        logDivider(tag);

        logHeader(tag, "Request Params:", httpLogModel.getParams());

        logDivider(tag);

        logHeader(tag, "Request Headers: ", httpLogModel.getRequestHeaders());

        logDivider(tag);

        logHeader(tag, "Response Headers: ", httpLogModel.getResponseHeaders());

        logDivider(tag);

        if (httpLogModel.getThrowable() == null) {
            if (TextUtils.isEmpty(httpLogModel.getContent())) {
                printJson(tag, "Empty Response");
            } else {
                printJson(tag, httpLogModel.getContent());
            }
        } else {
            printJson(tag, "Request Error: " + httpLogModel.getThrowable().toString());
        }

        logBottomBorder(tag);
        if (LogConfig.isLogToFile() && sFileBuffer != null) {
            FileLog.printFile(sFileBuffer.toString());
            sFileBuffer = null;
        }
        isPrinting = false;
        if (waitingList0.size() > 0) {
            String tagItem;
            HttpLogModel modelItem;
            String headerItem;
            tagItem = waitingList0.poll();
            modelItem = waitingList1.poll();
            headerItem = waitingList2.poll();
            if (tagItem != null)
                printHttp(tagItem, modelItem, headerItem);
        }
    }

    private static void printJson(String tag, String msg) {

        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        //原始日志内容
        String[] lines = message.split(LogConfig.LINE_SEPARATOR);
        //做一个行缩进
        ArrayList<String> result = tryCutLogLines(lines);
        //把缩进后的结果打印出来
        logTopAndBottom(result, tag);

        //把原始日志内容添加到文件buffer
        for (String line : lines)
            appendLogFile(line);
    }

    private static void logTopAndBottom(ArrayList<String> result, String tag) {
        for (int i = 0; i < result.size(); i++) {
            if (i == MAX_TOP_AND_BOTTOM_LINE_NUM && result.size() > MAX_TOP_AND_BOTTOM_LINE_NUM * 2)
                Logger.d(tag, HORIZONTAL_DOUBLE_LINE + " ……(too long to show)");
            else if (i < MAX_TOP_AND_BOTTOM_LINE_NUM || i > result.size() - MAX_TOP_AND_BOTTOM_LINE_NUM)
                Logger.d(tag, HORIZONTAL_DOUBLE_LINE + " " + result.get(i));
        }
    }

    private static ArrayList<String> tryCutLogLines(String[] lines) {
        ArrayList<String> result = new ArrayList<>();
        if (lines.length > NEED_TO_CUT_LINE_NUM) {
            LinkedList<StringBuilder> operationList = new LinkedList<>();
            for (String s : lines)
                if (!TextUtils.isEmpty(s))
                    operationList.add(new StringBuilder(s));
            for (int i = 0; i < TOP_AND_BOTTOM_NORMAL_LINE_NUM; i++)
                result.add(operationList.removeFirst().toString());
            while (operationList.size() > TOP_AND_BOTTOM_NORMAL_LINE_NUM) {
                while (operationList.getFirst().length() + operationList.get(1).length() < 4 * 1024
                        && operationList.size() > TOP_AND_BOTTOM_NORMAL_LINE_NUM) {
                    operationList.getFirst().append(operationList.remove(1));
                }
                if (operationList.getFirst().length() + operationList.get(1).length() >= 4 * 1024)
                    result.add(operationList.removeFirst().toString());
            }
            for (StringBuilder sb : operationList)
                result.add(sb.toString());
        } else {
            Collections.addAll(result, lines);
        }
        return result;
    }


    private static void logHeader(String tag, String headerName, Map<String, String> headers) {

        log(tag, HORIZONTAL_DOUBLE_LINE + " " + headerName);
        if (headers == null || headers.isEmpty()) {
            log(tag, HORIZONTAL_DOUBLE_LINE + " empty headers");
        } else {
            Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                log(tag, HORIZONTAL_DOUBLE_LINE + " " + String.format("%s = %s", next.getKey(), next.getValue()));
            }
        }
    }

    private static void logTopBorder(String tag) {
        log(tag, TOP_BORDER);
    }

    private static void logBottomBorder(String tag) {
        log(tag, BOTTOM_BORDER);
    }

    private static void logDivider(String tag) {
        log(tag, MIDDLE_BORDER);
    }

    private static void log(String tag, String msg) {
        Logger.d(tag, msg);
        appendLogFile(msg);
    }

    private static void appendLogFile(String msg) {
        if (LogConfig.isLogToFile() && sFileBuffer != null)
            sFileBuffer.append(msg).append("\n");
    }
}
