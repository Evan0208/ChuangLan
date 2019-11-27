package commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间类型辅助类
 *
 * @author gongqiang
 */
public class DateTimeHelper {

    /**
     * 获取当前时间
     */
    public static long getDateTimeNow() throws Exception {
        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");
        Date date = new Date();
        String dateStr = sdf.format(date);
        return dateToStampLong(dateStr);
    }
    /**
     * 获取今天日期时间
     */
    public static long getDateNow() throws Exception {
        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd 00:00:00 a");
        Date date = new Date();
        String dateStr = sdf.format(date);
        return dateToStampLong(dateStr);
    }

    /**
     * 获取当月一号时间
     */
    public static long getNowMonth() throws Exception {
        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-01 00:00:00 a");
        Date date = new Date();
        String dateStr = sdf.format(date);
        return dateToStampLong(dateStr);
    }

    /**
     * 获取当月下一个月一号时间
     */
    public static long getNowNextMonth() throws Exception {
        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-01 00:00:00 a");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        String dateStr = sdf.format(calendar.getTime());
        return dateToStampLong(dateStr);
    }


    /**
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) throws Exception {
        //设置时间格式，将该时间格式的时间转换为时间戳
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long time = date.getTime();
        res = String.valueOf(time);
        return res;
    }

    /**
     * 将时间转换为时间戳
     */
    public static long dateToStampLong(String s) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long time = date.getTime();

        return time;
    }

    /**
     * 将时间戳转换为时间
     */
    public static String stampToTime(String s) throws Exception {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将时间戳转换为时间
        long lt = new Long(s);
        //将时间调整为yyyy-MM-dd HH:mm:ss时间样式
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 将时间格式化
     */




}
