package org.net.fantai.model;

/**
 * Created by fantai-xyz on 2017/9/6.
 */
public class TransFunc {
    public final static byte LOGIN = 0x01; //平台登录
    public final static byte GIVETIME = 0x05;  //平台授时

    public final static byte GPSUPDATE = 0x02; //gps信息更新 ，手动刷新
    public final static byte GPSTIME = 0x03; //gps时间间隔信息
    public final static byte SENSORTIME = 0x06;  //传感器间隔时间信息设置
    public final static byte GPSINTERVAL = 0x05;  //GPS定时上传信息
    public final static byte SENSORINTERVAL = 0x07;  //传感器定时上传信息
    public final static byte ALARMEXCEPTION = 0x04;  //终端异常报警信息上传
    public final static byte LOCK = (byte)0x2D;  //电磁锁操作,空调控制操作
    public final static byte ICUP = 0x08; //卡号上传信息

    public final static byte ICACCREDIT = 0x09; //卡 accredit号授权信息
    public final static byte NOMARLSEND = (byte)0x21; // 正常发送到终端
    public final static byte MOTORSEND = (byte)0x81;  //机动发送终端

    public final static byte LockSealing  = 0x41; //施封
    public final static byte LockDeblocking = 0x43; //解封
    public final static  byte LockCheck = (byte)0x45; //验封
    public final static byte AlarmRelease = (byte)0x47; //解除报警
    public final static byte Bicker = 0x49; //抬杠
    public final static byte SensorCollectSet = (byte)0x6D; //传感器模式设置
    public final static byte PdaSensor = (byte)0x10;//pda 或者积水上传

}
