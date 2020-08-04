package com.pavel.queueorganizer;

import java.sql.Timestamp;

public class PeriodAndWorkHours {

    /**Сверяет дату и указанные рабочие часы
     * @param date дата, которую необходимо проверить
     * @param workhours строка с рабочими часами очереди
     * @return true если переданное время совпадает с указанными рабочими часами
     */
    public static boolean checkWorkHours(Timestamp date, String workhours){
        return true;
    }


    /**Парсит полученную строку с периодом работы очереди во время начала работы очереди
     * @param period строка с периодом работы очереди
     * @return время начала работы очереди
     */
    public static Timestamp parseStartTime(String period){
        return null;
    }
    /**Парсит полученную строку с периодом работы очереди во время заверешения работы очереди
     * @param period строка с периодом работы очереди
     * @return время завершения работы очереди
     */
    public static Timestamp parseEndTime(String period){
        return null;
    }

}
