package com.cookie.android.util.log;

import com.cookie.android.util.DateUtils;
import com.cookie.android.util.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cookie.android.util.log.LogConfig.LINE_SEPARATOR;

public class FileLog {

    private static final String SECTION_START = "====section_start================" + LINE_SEPARATOR;
    private static final String SECTION_END = "====section_end==================" + LINE_SEPARATOR + LINE_SEPARATOR;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static ExecutorService sExecutors = Executors.newCachedThreadPool();


    public static void printFile(final String tag, final File targetDirectory, String fileName
            , final String headString, final String msg) {
        Date now = new Date();
        final String name = (fileName == null) ? getLogFileName(now) : fileName;
        if (!sExecutors.isShutdown() && !sExecutors.isTerminated())
            sExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    if (save(targetDirectory, name, msg)) {
                        Logger.d(tag, headString + " save log success ! location is >>>" + targetDirectory.getAbsolutePath() + "/" + name);
                    } else {
                        Logger.e(tag, headString + "save log fails !");
                    }
                }
            });
    }

    public static void printFile(String msg) {
        printFile("FileLog", LogConfig.getDefaultDir(), null, "", msg);
    }

    private static boolean save(File dic, String fileName, String msg) {
        File file = new File(dic, fileName);

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file, true);
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(String.format("[%s]", DateUtils
                    .getDateFormat(DATE_TIME_FORMAT).format(new Date())) + LINE_SEPARATOR);
            outputStreamWriter.write(msg);
            outputStreamWriter.write(LINE_SEPARATOR);
            outputStreamWriter.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Set<String> fileNames = new HashSet<>();
        Collections.addAll(fileNames, getLatestFileNames(new Date()));
        File[] logFiles = dic.listFiles();
        if (logFiles != null)
            for (File file1 : logFiles)
                if (!fileNames.contains(file1.getName()))
                    file1.deleteOnExit();

        return true;
    }


    /**
     * 保存数据库数据
     *
     * @param dic
     * @param fileName
     * @param msg
     * @return
     */
    public static boolean saveDataBase(File dic, String fileName, String msg) {
        File file = new File(dic, fileName);

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file, true);
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(msg);
            outputStreamWriter.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<File> getLatestLogFile(Date target) {
        String[] files = getLatestFileNames(target);
        ArrayList<File> result = new ArrayList<>();
        for (String path : files) {
            File file = new File(LogConfig.getDefaultDir().getPath() + File.separator + path);
            if (file.exists())
                result.add(file);
        }
        return result;
    }

    private static String[] getLatestFileNames(Date target) {
        Date[] dates = new Date[LogConfig.fileDays];
        dates[0] = target;
        for (int i = 1; i < dates.length; i++) {
            Date date = (Date) dates[i - 1].clone();
            date.setTime(date.getTime() - 24 * 3600 * 1000L);
            dates[i] = date;
        }
        String[] result = new String[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = getLogFileName(dates[i]);
        }
        return result;
    }

    private static String getLogFileName(Date date) {
        return "ZwztLog_" + DateUtils.getDateFormat(DATE_FORMAT).format(date) + ".log";
    }

}
