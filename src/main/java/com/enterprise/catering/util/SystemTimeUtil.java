package com.enterprise.catering.util;

import com.enterprise.catering.controller.SystemConfigController;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间相关工具类
 */
public class SystemTimeUtil {


    /**
     * 检查指定日期的订餐截止时间是否已过
     */
    public static boolean isOrderDeadlinePassedForDate(Date orderDate) {
        String deadlineStr = SystemConfigController.getOrderDeadline();
        if (deadlineStr == null || deadlineStr.isEmpty()) {
            deadlineStr = Config.ORDER_DEADLINE;
        }

        String[] parts = deadlineStr.split(":");
        int deadlineHour = Integer.parseInt(parts[0]);
        int deadlineMinute = Integer.parseInt(parts[1]);

        Calendar now = Calendar.getInstance();
        Calendar deadline = Calendar.getInstance();
        deadline.setTime(orderDate);
        deadline.set(Calendar.HOUR_OF_DAY, deadlineHour);
        deadline.set(Calendar.MINUTE, deadlineMinute);
        deadline.set(Calendar.SECOND, 0);
        deadline.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        Calendar orderDay = Calendar.getInstance();
        orderDay.setTime(orderDate);

        boolean isToday = today.get(Calendar.YEAR) == orderDay.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == orderDay.get(Calendar.DAY_OF_YEAR);

        if (isToday) {
            return now.after(deadline);
        } else {
            Calendar tomorrowDeadline = Calendar.getInstance();
            tomorrowDeadline.add(Calendar.DAY_OF_MONTH, 1);
            tomorrowDeadline.set(Calendar.HOUR_OF_DAY, deadlineHour);
            tomorrowDeadline.set(Calendar.MINUTE, deadlineMinute);
            tomorrowDeadline.set(Calendar.SECOND, 0);
            tomorrowDeadline.set(Calendar.MILLISECOND, 0);
            return now.after(tomorrowDeadline);
        }
    }

    /**
     * 检查当前时间是否已到配餐开始时间
     */
    public static boolean isDeliveryStartTimeReached() {
        String startTimeStr = SystemConfigController.getDeliveryStartTime();
        if (startTimeStr == null || startTimeStr.isEmpty()) {
            startTimeStr = Config.DELIVERY_START_TIME;
        }

        String[] parts = startTimeStr.split(":");
        int startHour = Integer.parseInt(parts[0]);
        int startMinute = Integer.parseInt(parts[1]);

        Calendar now = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        return now.after(startTime) || now.equals(startTime);
    }

    /**
     * 获取可以订餐的日期：
     * 如果配餐开始时间已过，可以预订明天；否则预订今天。
     */
    public static Date getAvailableOrderDate() {
        Calendar cal = Calendar.getInstance();
        if (isDeliveryStartTimeReached()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTime();
    }

}


