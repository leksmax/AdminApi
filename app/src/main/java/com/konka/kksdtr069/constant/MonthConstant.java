package com.konka.kksdtr069.constant;

import java.util.EnumMap;

public class MonthConstant {

    public static final EnumMap months = new EnumMap(Month.class);

    static {
        months.put(Month.JANUARY, "01");
        months.put(Month.FEBRUARY, "02");
        months.put(Month.MARCH, "03");
        months.put(Month.APRIL, "04");
        months.put(Month.MAY, "05");
        months.put(Month.JUNE, "06");
        months.put(Month.JULY, "07");
        months.put(Month.AUGUST, "08");
        months.put(Month.SEPTEMBER, "09");
        months.put(Month.OCTOBER, "10");
        months.put(Month.NOVEMBER, "11");
        months.put(Month.DECEMBER, "12");
    }

    private enum Month {
        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST,
        SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
    }

}
