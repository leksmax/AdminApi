package com.konka.kksdtr069.constant;

public class SysLogConstant {

    public final static int LEVEL_ALL = 0;// 不过滤，输出所有级别
    public final static int LEVEL_ERROR = 3;// 输出Error
    public final static int LEVEL_INFO = 6;// 输出Info
    public final static int LEVEL_DEBUG = 7;// 输出Debug

    public final static int TYPE_ALL = 0;// 不过滤，输出所有类型
    public final static int TYPE_OPERATE = 16;// 操作日志
    public final static int TYPE_KERNEL = 17;// 运行日志
    public final static int TYPE_SECURITY = 19;// 安全日志
    public final static int TYPE_USER = 20;// 用户日志

    public final static int OUTPUTTYPE_CLOSE = 0;// 关闭日志功能
    public final static int OUTPUTTYPE_SFTP = 1;// 仅发送SFTP
    public final static int OUTPUTTYPE_SYSLOG = 2;// 仅发送实时日志
    public final static int OUTPUTTYPE_BOTH = 3;// SFTP和实时日志均发送

}
