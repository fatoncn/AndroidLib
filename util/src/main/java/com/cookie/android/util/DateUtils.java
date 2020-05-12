package com.cookie.android.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DateUtils
 * Author: ZhangLingfei
 * Date : 2019/1/6 0006
 */
public class DateUtils {

    private static Map<String, ThreadLocal<SimpleDateFormat>> sDateFormatPool = new ConcurrentHashMap<>();

    /**
     * 采用ThreadLocal实现可复用并且线程安全的SimpleDateFormat对象
     * 根据不同的format保存在Map中
     *
     * @param format
     * @return
     */
    public static SimpleDateFormat getDateFormat(final String format) {
        if (sDateFormatPool.containsKey(format))
            return sDateFormatPool.get(format).get();

        ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(format, Locale.getDefault());
            }
        };
        sDateFormatPool.put(format, threadLocal);
        return threadLocal.get();
    }

    public static String getSimpleDate(long date) {
        return getDateFormat("yyyy-MM-dd").format(date);
    }

    public static boolean isToday(long time) {
        return DateUtils.getSimpleDate(time).equals(DateUtils.getSimpleDate(new Date()));
    }

    public static String getSimpleDate(Date date) {
        return getDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 格式化    时间
     *
     * @param date   long 类型的   事件数据
     * @param format 需要格式化 格式  比如 yyyy-mm-dd
     * @return 返回格式化后的数据
     */
    public static String getFormatTimeStr(Long date, String format) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        try {
            String string = sdf.format(new Date(date));
            sb.append(string);
        } catch (Exception e) {
            Logger.i("事件格式化异常");
            e.printStackTrace();
        }
        return sb.toString();
    }



    public static long firstDayOfMonth(Date date) {//获取当月第一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        //将小时至0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        calendar.set(Calendar.MINUTE, 0);
        //将秒至0
        calendar.set(Calendar.SECOND, 0);
        //将毫秒至0
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
    public static long lastDayOfMonth(Date date) {//获取当月最后一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH,  calendar.getActualMaximum(Calendar.DAY_OF_MONTH));//设置为当月最后一天
        //将小时至23
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        calendar.set(Calendar.MINUTE, 59);
        //将秒至59
        calendar.set(Calendar.SECOND, 59);
        //将毫秒至999
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }
}
