package org.net.fantai.model;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.net.fantai.slqhepler.SqlDao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by fantai-xyz on 2017/9/6.
 */
public class DataMsg {
    private final static int FuncData = 3;  //子功能数据长度大小
    private final static int RandomRange = 1000; //随机数范围获取
    private static Map<String,HeartCheck> m_CodeIdMap = new HashMap<>();


    private void HandleHeart(byte codeId,String code,int time)
    {
        if(m_CodeIdMap.containsKey(code)) //判断是否含有该codeId
        {
            m_CodeIdMap.get(code).setRunning(false);
            m_CodeIdMap.get(code).interrupt();

            HeartCheck heartCheck = new HeartCheck(codeId,code,time);
            heartCheck.start();
            m_CodeIdMap.put(code,heartCheck);
        }
        else
        {
            HeartCheck heartCheck = new HeartCheck(codeId,code,time);
            heartCheck.start();
            m_CodeIdMap.put(code,heartCheck);

        }

    }


//    private SqlDao sqlDao = new SqlDao();

    /**
     * author fantai-xyz
     * brief tcp udp 数据发送信息
     * params  参数信息
     * <p> addr 发送地址
     * <p> func 功能
     * <p>radom 随机数
     * <p> content 数据内容
     * time 2017/9/7 16:32
     * return byte数组格式
     */
    public byte[] sendControlMsg(byte addr, byte func, byte[] content) {
        byte[] buf = null;
        try {
            // FE(1) + 地址位（1） + 功能码（1） + 数据量（1） + 子功能内容（1+2）+ 数据长度（1） + 数据内容（n） + crc(1) + FF(1)
            byte crc = 0x00;
            int len = 7 + FuncData;
            if (content != null)
                len = 7 + FuncData + content.length;
            System.out.println("时间：" + DataFormat.bytes2HexString(content));
            buf = new byte[len];
            buf[0] = (byte) 0xDE;
            buf[1] = addr; //地址
            buf[2] = func;  //功能码
            buf[3] = FuncData;  //子功能码数据长度  默认 3
            byte[] random = buildRandom(RandomRange);
            buf[5] = random[0]; //随机数
            buf[6] = random[1]; //随机数
            buf[7] = (byte) (len - 2);  //数据长度位
            switch (func) {
                case TransFunc.GPSUPDATE: //gps手动更新
                    buf[4] = 0x02; //终端需要回复内容信息
                    break;
                case TransFunc.LOCK: //电磁锁操作
//                    buf[4] = (byte) 0x2D;
//                    System.arraycopy(content, 0, buf, 8, content.length);  //内容数据复制
//                    break;
                case TransFunc.GPSTIME://gps上传时间
                case TransFunc.GIVETIME://平台授时
                case TransFunc.LOGIN:  //平台登录
                case TransFunc.ICACCREDIT:  //ic卡授权
                case TransFunc.SENSORTIME: //传感器间隔信息设置
                    buf[4] = 0x01;  //回复ack即可
                    System.arraycopy(content, 0, buf, 8, content.length);  //内容数据复制
                    break;


            }
            buf[len - 1 - 1] = CRC8.calcCrc8(buf, 1, len - 3, crc);
            buf[len - 1] = (byte) 0xDF;
        } catch (Exception e) {
        }
        return buf;
    }




    /**
     * author fantai-xyz
     * brief ic卡授权
     * params
     * time 2017/9/27 14:35
     * return
     */
    public byte[] getIcContent(byte result, byte state, int consume, int money) {

        byte[] content = new byte[6];
        content[0] = result;
        content[1] = state;
        content[2] = (byte) (consume >> 8);
        content[3] = (byte) consume;
        content[4] = (byte) (money >> 8);
        content[5] = (byte) money;
        return content;
    }


    /**
     * author fantai-xyz
     * brief  gps 传感器上传时间设置
     * params  时间
     * time 2017/9/8 16:10
     * return content 内容数组
     */
    public byte[] getGpsOrSensorTime(int time) {
        byte[] content = new byte[2];
        content[0] = (byte) time;
        content[1] = (byte) (time >> 8); // 高位
        return content;

    }


    /**
     * author fantai-xyz
     * brief   xor校验码
     * params
     * time 2017/9/11 17:10
     * return
     */
    public static byte getXor(byte[] datas) {

        byte temp = datas[0];

        for (int i = 1; i < datas.length; i++) {
            temp ^= datas[i];
        }
        return temp;
    }


    /**
     * author fantai-xyz
     * brief 控制电子锁
     * params
     * time 2017/9/8 16:40
     * return
     */
    public byte[] controlLock(byte func, String code) {

//        2A（*） 5A                        0 1
//        7B 02                             2 3
//        43 4E 53 4D 00 00 2A 90( 目标ID)  4-11
//            00 00 00 00 00 00 00 00 (本地ID)  12-19
//            43      20
//        12      21
//        31 32 33 34 35 36 37 38 39 30     22-31
//        14 0F 09 17 0E 24 2F 1 byte(时间前7字节的校验)  32-39
//            xx xx(CRC16)  40  41

        byte[] bCode = DataFormat.hexStringToBytes(code);   // 获取十六进制字符码
        byte[] buf = new byte[42];    //数据内容总长度为42位
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        byte[] time = DataFormat.hexStringToBytes(df.format(new Date()));  //时间
        byte bcd = getXor(time);
        byte[] key = {(byte) 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30};
        buf[0] = (byte) 0x2A;  //*
        buf[1] = (byte) 0x5A;  // 固定值
        buf[2] = (byte) 0x7B;
        buf[3] = (byte) 0x02;
        System.arraycopy(bCode, 0, buf, 8, bCode.length);   //赋值id吗
        System.arraycopy(time, 0, buf, 32, time.length);  //赋值时间码
        System.arraycopy(key, 0, buf, 22, key.length);  //赋值密钥
        buf[39] = bcd;   //时间bcd值校验
        buf[21] = (byte) 0x12;  //参数内容长度
        buf[20] = func; //内容
        byte[] crc = CRC16.crc16(buf, 2, buf.length - 4);  //crc校验
        buf[40] = crc[0];  //crc校验 高位
        buf[41] = crc[1]; //crc校验 低位
        return buf;
    }


    /**
     * author fantai-xyz
     * brief  计算时间信息，时间授时
     * params
     * time 2017/9/11 16:24
     * return
     */
    public byte[] timeCalendar() {
        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd,HH:mm:ss+08");//设置日期格式
        String time = df.format(new Date());// new Date()为获取当前系统时间
        byte[] timeBuf = time.getBytes();
        int len = timeBuf.length + 3;
        byte[] buf = new byte[len];
        System.arraycopy(timeBuf, 0, buf, 0, timeBuf.length);
        buf[len - 1] = 0x00;  //最后三位补充
        buf[len - 2] = 0x00;
        buf[len - 3] = 0x01;  //保留为格式
        return buf;
    }


    private int getMoney(double time)
    {
        int Money = 0;
        int min = (int) (time * 60);
        if(min < 10)
            Money = 1;
        else if(min < 60)
            Money = 2;
        else
            Money = (int) (time * 2.0);
        return Money;
    }






    /**
     * author fantai-xyz
     * brief  数据接收信息处理
     * params
     * time 2017/9/8 17:49
     * return
     */
    public void RecieveDataMsg(byte[] buffer, int len, ChannelHandlerContext ctx, int tranModel, DatagramPacket packet) throws Exception {
//        ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
//        //异步发送应答消息给客户端: 这里并没有把消息直接写入SocketChannel,而是放入发送缓冲数组中
//        ctx.writeAndFlush(resp);
        System.out.println("recMsg:" + DataFormat.bytes2HexString(buffer));
        if (buffer[0] == (byte) 0xDE && buffer[buffer.length - 1] == (byte) 0xDF)  //0位
        {
            byte[] code = new byte[4]; // id码
            System.arraycopy(buffer, 1, code, 0, 4); //取出信息码  到底4位
            System.out.println("code码：" + DataFormat.bytes2HexString(code));
            byte[] Content = null;
            byte[] ack = {(byte) 0xDE, 0x21, 0x01, 0x03, 0x00, 0x00, 0x00, 0x08, 0x00, (byte) 0xDF};

            switch (buffer[5])//地址吗 0x03 0x04
            {
                case (byte) 0x03: //基础消息回复
                    switch (buffer[6])  //功能码
                    {
                        case TransFunc.GPSUPDATE:  //数据手动更新  02
                            Content = new byte[len - 14];
                            System.arraycopy(buffer, 12, Content, 0, len - 14);
                            SqlDao.updateSensorHistory(DataFormat.bytes2HexString(code), Content,true);   //更新gps信息
                            break;
                        case (byte) 0x01:  //基础ack 回复  01
                            System.out.println("控制成功接收");
                            break;
                        case TransFunc.LOCK:  //智能锁信息  2d
                            Content = new byte[len - 14];
                            System.arraycopy(buffer, 12, Content, 0, len - 14);
                            System.out.println("0x2d");
                            if(SqlDao.updateLock(DataFormat.bytes2HexString(code),Content) > 0)
                            {

                            }
                            System.out.println("suo");
                            ack[5] = buffer[9]; //随机数
                            ack[6] = buffer[10]; //随机数
                            ack[8] = CRC8.calcCrc8(ack, 1, 7);
                            ByteBuf ackLockup = Unpooled.copiedBuffer(ack);
                            if (tranModel == 0)
                                ctx.writeAndFlush(ackLockup);
                            else if (tranModel == 1)
                                ctx.writeAndFlush(new DatagramPacket(ackLockup, packet.sender()));
                            System.out.println("功能锁控制成功");
                            break;
                        case TransFunc.PdaSensor:  //pda 传感器上传
                            Content = new byte[len - 11];
                            System.arraycopy(buffer, 9, Content, 0, len - 11);
                            if(SqlDao.updateSensorHistory(DataFormat.bytes2HexString(code), Content,false) > 0)
                            {
                            }
                            ack[5] = buffer[9]; //随机数
                            ack[6] = buffer[10]; //随机数
                            ack[8] = CRC8.calcCrc8(ack, 1, 7);
                            ByteBuf ackSensor = Unpooled.copiedBuffer(ack);
                            if (tranModel == 0)
                                ctx.writeAndFlush(ackSensor);
                            else if (tranModel == 1)
                                ctx.writeAndFlush(new DatagramPacket(ackSensor, packet.sender()));
                            break;
                        case TransFunc.SENSORINTERVAL: //传感器实时上传   07    测试ok，待插入历史记录
                            Content = new byte[len - 11];
                            System.arraycopy(buffer, 9, Content, 0, len - 11);
                            SqlDao.updateSensorHistory(DataFormat.bytes2HexString(code), Content,true);
                            break;
                        case TransFunc.ICUP: //卡号上传   08
                            ack[5] = buffer[9]; //随机数
                            ack[6] = buffer[10]; //随机数
                            ack[8] = CRC8.calcCrc8(ack, 1, 7);
                            ByteBuf ackICup = Unpooled.copiedBuffer(ack);
                            if (tranModel == 0)
                                ctx.writeAndFlush(ackICup);
                            else if (tranModel == 1)
                                ctx.writeAndFlush(new DatagramPacket(ackICup, packet.sender()));
                            System.out.println("卡号接收更新成功");
                            Content = new byte[len - 14];
                            System.arraycopy(buffer, 12, Content, 0, len - 14);
                            byte[] cardInfo = new byte[4];  //卡号
                            double elec = DataFormat.getV(Content[4]);  //电量
//                            switch (Content[4]) {
//                                case (byte) 0x61:
//                                    elec = 3.5;
//                                    break;
//                                case (byte) 0x62:
//                                    elec = 3.85;
//                                    break;
//                                case (byte) 0x63:
//                                    elec = 4.10;
//                                    break;
//                                case (byte) 0x64:
//                                    elec = 4.20;
//                                    break;
//                            }
                            System.arraycopy(Content, 0, cardInfo, 0, 4);
                            String strCode = DataFormat.bytes2HexString(code);  //code吗
                            String strCardInfo = DataFormat.bytes2HexString(cardInfo);
                            int state = SqlDao.getCarInfoState(strCode, strCardInfo);
                            byte isCredit = (byte) 0x01;
                            int money = SqlDao.getCarInfoMoney(strCode, strCardInfo);
                            switch (state)  //判断状态
                            {
                                case 0: //第一次信息录入
                                case 2:  //当=前状态为出库u
                                    if (SqlDao.updateCarInfoEle(1, elec, strCode, strCardInfo) > 0) {
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                        String startTime = df.format(new Date());  //获取当前时间
                                        if (SqlDao.insertIcHistory(strCode, startTime, 1) > 0)
                                            isCredit = 0x02;  //成功
                                    }
                                    ByteBuf byteBufIn = Unpooled.copiedBuffer(sendControlMsg(TransFunc.NOMARLSEND, TransFunc.ICACCREDIT, getIcContent(isCredit, (byte) 0x01, 0, money)));
                                    if (tranModel == 0)
                                        ctx.writeAndFlush(byteBufIn);
                                    else if (tranModel == 1)
                                        ctx.writeAndFlush(new DatagramPacket(byteBufIn, packet.sender()));
                                    break;

                                case 1:  //当前状态为入库
                                    String starTime = SqlDao.getIcStartTime(strCode);
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                    String endTime = df.format(new Date());  //获取当前时间
                                    long milltime = Timestamp.valueOf(endTime).getTime() - Timestamp.valueOf(starTime).getTime();
                                    double hTime = milltime / 1000.0 / 60.0 / 60.0;  //计算小时
                                    int consumer = getMoney(hTime);
                                    /*毫秒时间转化成时分秒*/
                                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                    formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                                    String hms = formatter.format(milltime);
                                    if (money > 5)//1.查询总额
                                    {
                                        //2.查询入库时间
                                        if (SqlDao.updateCarInfoMonEle(2, money - consumer, elec, strCode, strCardInfo) > 0)//更新余额和电量
                                        {
                                            if (SqlDao.updateIcHistory(strCode, endTime, hms, 2,  consumer) > 0) {
                                                if(SqlDao.insConsumerRecord(strCode,endTime,consumer) > 0)//插入消费记录
                                                isCredit = 0x02;
                                                //插入消费
                                            }
                                        }
                                    }
                                    else
                                    {
                                        isCredit = 0x03;  //余额不足
                                    }
                                    ByteBuf byteBufOut = Unpooled.copiedBuffer(sendControlMsg(TransFunc.NOMARLSEND, TransFunc.ICACCREDIT, getIcContent(isCredit, (byte) 0x02, consumer,(money - consumer))));
                                    if (tranModel == 0)
                                        ctx.writeAndFlush(byteBufOut);
                                    else if (tranModel == 1)
                                        ctx.writeAndFlush(new DatagramPacket(byteBufOut, packet.sender()));
                                    break;
                            }
                            break;
                        case TransFunc.GPSINTERVAL: // gps定时上传  测试完成。ok,需要更新一下经纬度转化
                            Content = new byte[len - 11];
                            System.arraycopy(buffer, 9, Content, 0, len - 11);
                            SqlDao.updateGps(DataFormat.bytes2HexString(code), Content);   //更新gps信息
                            break;
                        case TransFunc.ALARMEXCEPTION:  //报警信息更新04   测试ok，完成状态
                            Content = new byte[len - 14];//内容长度
                            System.arraycopy(buffer, 12, Content, 0, len - 14);
                            System.out.println("Content:" + DataFormat.bytes2HexString(Content));
                            if (SqlDao.updateAlarmValue(DataFormat.bytes2HexString(code), Content) > 0)  //更新报警记录
                            {
                            }
                            ack[5] = buffer[9]; //随机数
                            ack[6] = buffer[10]; //随机数
                            ack[8] = CRC8.calcCrc8(ack, 1, 7);
                            ByteBuf ackAlarm = Unpooled.copiedBuffer(ack);
                            if (tranModel == 0)
                                ctx.writeAndFlush(ackAlarm);
                            else if (tranModel == 1)
                                ctx.writeAndFlush(new DatagramPacket(ackAlarm, packet.sender()));
                            System.out.println("报警更新成功");
                            break;
                    }
                    break;   //基础验证

                case (byte) 0x04: //心跳检测
                    Content = new byte[len - 11];//2位数据
                    System.arraycopy(buffer, 9, Content, 0, len - 11);
                    if(Content.length > 1)
                    {
                        String strContent = DataFormat.bytes2HexString(Content);
                       if( SqlDao.updateDeviceSignal(DataFormat.bytes2HexString(code),strContent.substring(0,2),"0") > 0)
                       {
                           SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                           String Time = df.format(new Date());  //获取当前时间
                            SqlDao.insHeartRecord(DataFormat.bytes2HexString(code),Time,strContent.substring(0,2),"0");
                       }
                    }
                    break;    //心跳检测

                case (byte) 0x05: //登陆  更新ip地址和gps信息   测试成功
                    Content = new byte[len - 14];
                    System.arraycopy(buffer, 12, Content, 0, len - 14);
                    SqlDao.updateGps(DataFormat.bytes2HexString(code), Content);   //更新gps信息
                    String socketAddress = "";
                    if (tranModel == 0)
                        socketAddress = ctx.channel().remoteAddress().toString();
                    else if (tranModel == 1) {
                        socketAddress = packet.sender().getAddress().toString() + ":" + packet.sender().getPort();
                    }
                    System.out.println("socketAddress:" + socketAddress);
                    String[] str = socketAddress.split(":");
                    if (SqlDao.UpdateCodeIP(DataFormat.bytes2HexString(code), str[0].substring(1), str[1]) > 0)//更新ip地址
                    {

                        SqlDao.updataConnectType(DataFormat.bytes2HexString(code), tranModel);
                        int heartTime =SqlDao.getDeivceHeartNumByCode(DataFormat.bytes2HexString(code));
                        SqlDao.updateDeviceState(code[0],DataFormat.bytes2HexString(code),1);  //更新状态
                        System.out.println("heartTime:"+heartTime);
                        HandleHeart(code[0],DataFormat.bytes2HexString(code),heartTime);
                        ack[5] = buffer[9];
                        ack[6] = buffer[10];
                        ack[8] = CRC8.calcCrc8(ack, 1, 7);
                        ByteBuf ackIpUp = Unpooled.copiedBuffer(ack);
                        if (tranModel == 0)
                            ctx.writeAndFlush(ackIpUp);
                        else if (tranModel == 1)
                            ctx.writeAndFlush(new DatagramPacket(ackIpUp, packet.sender()));
                        System.out.println("ip地址更新成功");
                        Thread.sleep(1000);
                        ByteBuf sendTime = Unpooled.copiedBuffer(sendControlMsg((byte) 0x21, TransFunc.GIVETIME, timeCalendar()));
                        if (tranModel == 0)
                            ctx.writeAndFlush(sendTime);
                        else if (tranModel == 1)
                            ctx.writeAndFlush(new DatagramPacket(sendTime, packet.sender()));

                    }
                    break;      //登陆验证


            }
        }


    }


    /**
     * author fantai-xyz
     * brief  生成随机数函数
     * params 0-d 的随机数
     * time 2017/9/9 13:35
     * return
     */
    public byte[] buildRandom(int d) {
        int random = (int) (Math.random() * d);
        byte[] buf = new byte[2];
        buf[0] = (byte) random;
        buf[1] = (byte) (random >> 8);  //高位
        return buf;

    }


}
