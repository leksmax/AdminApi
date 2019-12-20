package com.konka.kksdtr069.constant;

import java.util.HashMap;

public class NetConstant {

    public static final String XID_CONF = "SPEED_TEST";

    public static final String DEFAULT_DOWNLOAD_URL = "http://223.99.188" +
            ".49:33600/UPGRADE/jsp/SpeedTest.zip";

    public static final String DOWNLOAD_URL = "http://weixin.sd.chinamobile" +
            ".com:9081/networkspeedtest/paramurl/paramTest.do";

    public static final String FILE_PATH = "/data/";

    public static final String FILE_NAME = "SpeedTest.zip";

    public static final HashMap<String, Integer> AREA_CODE = new HashMap<String, Integer>();

    static {
        AREA_CODE.put("jinan", 531);
        AREA_CODE.put("qingdao", 532);
        AREA_CODE.put("zibo", 533);
        AREA_CODE.put("zaozhuang", 632);
        AREA_CODE.put("dongying", 546);
        AREA_CODE.put("yantai", 535);
        AREA_CODE.put("weifang", 536);
        AREA_CODE.put("jining", 537);
        AREA_CODE.put("taian", 538);
        AREA_CODE.put("weihai", 631);
        AREA_CODE.put("rizhao", 633);
        AREA_CODE.put("laiwu", 634);
        AREA_CODE.put("linyi", 539);
        AREA_CODE.put("dezhou", 534);
        AREA_CODE.put("liaocheng", 635);
        AREA_CODE.put("binzhou", 543);
        AREA_CODE.put("heze", 530);
    }
}
