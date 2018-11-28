package org.net.fantai.slqhepler;

//import com.mysql.jdbc.Connection;

import org.net.fantai.model.DataFormat;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.mysql.jdbc.PreparedStatement;


/**
 * Created by fantai-xyz on 2017/9/12.
 */
public class SqlDao {
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static String dbUrl = "jdbc:mysql://localhost:3306/ft_case?autoReconnect=true&amp;autoReconnectForPools=true";// 数据库地址
    private static String dbUser = "root";// 用户名
    private static String dbPass = "test123"; // 用户密码
    static Connection connection = null;


    /**
     * @brief关闭数据库
     */
    public static void closeConnection() {
        if (connection != null) {
            try {

                connection.close();
//                this.isClose = true;
            } catch (SQLException e) {

            }
        }
    }


    /**
     * author fantai-xyz
     * brief 连接数据库
     * params
     * time 2017/9/12 10:24
     * return
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
//            Class.forName(dbDriver);
//            conn = (Connection) DriverManager.getConnection(dbUrl, dbUser, dbPass);//
            conn = C3P0cnn.getConnection();
        } catch (Exception e) {
//            System.out.println("connecdt is error");
        }
        return conn;
    }

    public static int updataConnectType(String code, int type) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "update sy_deviceinfo set sy_di_con_type = ? where sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, type);
        pst.setString(2, code);
        return pst.executeUpdate();

    }


    public static int updateLock(String code, byte[] src) throws SQLException {

        /*施封 2a 5a 7b 82 0b 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 42 0f 08 63 ff 20 17 10 14 09 51 26 4d 13 00 00 00 22 0e
         *    ‘*’    0
         *    5a    1
         *    00    协议id    2
         *    00    设备状态   3
         *    00 00 00 00 00 00 00 00  本地id  4 5 6 7 8 9 10 11
         *    00 00 00 00 00 00 00 00  目标di  12 13 14 15 16 17 18 19
         *    00 指令代码  20
         *    00 参数长度  21
         *
         *   施封
         *    //参数内容
         *    00 施封结果 22
         *    00 电压状态 23
         *    00 成功标志  24
         *
         *    00 00 00 00 00 00 00 00 操作时间  25 26 27 28 29 30 31 32
         *    00 00 00 00 计数器  33 34 35 36
         *    00 00 16位校验  37 38
         *
         *
         *
         *
         *    00 00 00 00 00 00 时间  39 40 41 42 43 44
         *    ‘#’   45
         *
         *
         *
         *    解封
         *     00 接封结果 22
         *    00 电压状态 23
         *    00 成功标志  24
         *
         *    00 00 00 00 00 00 00 00 操作时间  25 26 27 28 29 30 31 32
         *    00 报警次数    33
         *    00 报警种类    34
         *    00 00 00 00 00 00 00 00 报警时间  35 36 37 38 39 40 41 42
         *    00 00 00 00 计数器 43 44 45 46
         *     00 00  47 48
         *      00 00 00 00 00 00 时间  49 50 51 52 53 54
         *    ‘#’   55
         *
         *
         *    验封
         *        00 验封结果 22
         *    00 电压状态 23
         *    00 成功标志  24
         *
         *    00 00 00 00 00 00 00 00 操作时间  25 26 27 28 29 30 31 32
         *    00 报警次数  33
         *    00 报警种类 34
         *    00 00 00 00 00 00 00 00 报警时间 35 36 37 38 39 40 41 42
         *    00  扩展数据总包数 43
         *    00 00  44 45
         *    00 00 00 00 00 00 时间  46 47 48 49 50 51
         *    ‘#’   52
         *
         *
         * 报警
         *    00 报警结果 22
         *    00 电压状态 23
         *    00 00 00 00 00 00 00 00 操作时间  24 25 26 27 28 29 30 31
         *    00 00 32 33
         *    00 00 00 00 00 00 时间  34 35 36 37 38 39
         *    ‘#’   40
         *
         *
         *
         * */
        int contentLen = (src[21] & 0xFF) + 31;
     //   System.out.println(src.length + ";" + contentLen);
        if (src.length == ((src[21] & 0xFF) + 31)) {
          //  System.out.println("智能锁回复");
            if (src[0] == (byte) '*' && src[1] == (byte) 0x5A && src[src.length - 1] == (byte) '#') {
                if (connection == null || connection.isClosed())
                    connection = getConn();
                if (src[2] == (byte) 0x7B) {
                    int state = -1;
                    String stateValue = "";
                    switch (src[20]) {
                        case (byte) 0x42: //施封
                            if (src[24] == 0x01) {
                                state = 0;
                                stateValue = "施封";
                            }
                            break;
                        case (byte) 0x44: //解封
                            if (src[24] == 0x01) {
                                stateValue = "解封";
                                state = 1;
                            }
                            break;
                        case (byte) 0x46: //验封
                            if (src[24] == 0x01) {
                                state = 2;
                                stateValue = "验封";
                            }
                            break;
                        case (byte) 0x48: //解除报警
                            switch (src[22]) {
                                case 0x01:
                                    state = 3;
                                    stateValue = "解除报警";
                                    break;
                                case 0x02:

                                    break;
                            }
                            break;


                    }
                    if (state != -1) {
                        byte[] fromCode = new byte[4];
                        byte[] time = new byte[7];
                        //20 17 09 30 12 14 25
                        switch (state) {
                            case 0:
                            case 1:
                            case 2:
                                System.arraycopy(src, 25, time, 0, 7);
                                break;

                            case 3:
                                System.arraycopy(src, 24, time, 0, 7);
                                break;
                        }
                        System.arraycopy(src, 16, fromCode, 0, 4);
                        String strFromCode = DataFormat.bytes2HexString(fromCode).toUpperCase(); //来自平台ID 吗

                        if (!strFromCode.substring(0, 2).equals("13")) {
                            strFromCode = "平台操作";
                        } else {
                            strFromCode = "PDA操作：" + strFromCode;
                        }
                        String strTime = DataFormat.bytes2HexString(time);
                        String timeValue = String.format("%s-%s-%s %s:%s:%s", strTime.substring(0, 4), strTime.substring(4, 6),
                                strTime.substring(6, 8), strTime.substring(8, 10), strTime.substring(10, 12), strTime.substring(12, 14));  //截取时间数值
                      //  System.out.println("timeValue:" + timeValue);
                        double V = DataFormat.getV(src[23]);
                        String sql = "update sl_lockinfo set sl_li_status = ?,sl_li_electric = ?,sl_li_time = ? where  sy_di_code = ?";
                        PreparedStatement pstLockInfo = connection.prepareStatement(sql);
                        pstLockInfo.setInt(1, state);
                        pstLockInfo.setDouble(2, V);
                        pstLockInfo.setTimestamp(3, Timestamp.valueOf(timeValue));
                        pstLockInfo.setString(4, code);
                        if (pstLockInfo.executeUpdate() > 0) {
                            String sqlInsert = "insert into sl_controlrecord(sl_cr_time,sy_di_code,sl_cr_action,sl_cr_electric,sl_cr_cont_code) values(?,?,?,?,?)";
                            PreparedStatement pst = connection.prepareStatement(sqlInsert);
                            pst.setTimestamp(1, Timestamp.valueOf(timeValue));
                            pst.setString(2, code);
                            pst.setString(3, stateValue);
                            pst.setDouble(4, V);
                            pst.setString(5, strFromCode);
                            return pst.executeUpdate();
                        }
                    }


                }


            }


        }
        return 0;


    }


    /**
     * author fantai-xyz
     * brief 心跳命令检测插入历史记录
     * params
     * time 2017/10/14 10:27
     * return
     */
    public static int insHeartRecord(String code, String time, String singal, String statlite) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "insert into sc_heartbeatrecord(sy_di_code,sc_hbr_time,sc_hbr_signal,sc_hbr_satellite) values(?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        pst.setTimestamp(2, Timestamp.valueOf(time));
        pst.setString(3, singal);
        pst.setString(4, statlite);
        return pst.executeUpdate();
    }


    /**
     * author fantai-xyz
     * brief  更新gps信息
     * params
     * time 2017/9/12 10:33
     * return
     */
    public static int updateGps(String code, byte[] src) throws Exception {
//        FF E8 FC 07  00 00     时间
//        77 15 33 22        纬度
//        35 25 40 11 00      经度   14
//        39               速度 15
//        1E               方向东西经、南北纬  16
//        0C              定位状态以及天线状态操作者编码 17
//        1D 00 00         里程   18-20
//        xx              异或取反校验 21
        if (connection == null || connection.isClosed())
            connection = getConn();
        if (src.length >= 21) {
            byte[] time = new byte[6]; //时间
            byte[] longtitude = new byte[5];  //经度
            byte[] latitude = new byte[4];  //唯独


            System.arraycopy(src, 0, time, 0, time.length);
            String strTime = DataFormat.bytes2HexString(time);  //时间转化
            String timeValue = String.format("20%s-%s-%s %s:%s:%s", strTime.substring(10, 12), strTime.substring(8, 10)
                    , strTime.substring(6, 8), strTime.substring(4, 6), strTime.substring(2, 4), strTime.substring(0, 2));  //截取时间数值
            System.arraycopy(src, 6, latitude, 0, 4);  //截取wei度
            System.arraycopy(src, 10, longtitude, 0, 5);  //截取jing度

            String strLongtitude = DataFormat.bytes2HexString(longtitude);   //转成字符串进行拆分
            String strLatitude = DataFormat.bytes2HexString(latitude);  //转化成字符串进行拆分
            String latitudeValue = String.valueOf(Integer.parseInt(strLatitude.substring(6, 8)) + Double.parseDouble(strLatitude.substring(4, 6) + "." + strLatitude.substring(2, 4) + strLatitude.substring(0, 2)) / 60.0); //纬度解析

            String longtitudeValue = String.valueOf(Integer.parseInt(strLongtitude.substring(8, 10) + strLongtitude.substring(6, 7)) +
                    Double.parseDouble(strLongtitude.substring(7, 8) + strLongtitude.substring(4, 5) + "." +
                            strLongtitude.substring(5, 6) + strLongtitude.substring(2, 4) + strLongtitude.substring(0, 1)) / 60.0); //经度解析

            String speedValue = String.valueOf(src[15] & 0xFF);  //速度值
            byte t = (byte) ((src[16] >> 6) & 0x03);  //经纬度
            String titude = "";
            switch (t) {
                case 0:
                    titude = "北纬东经";
                    break;
                case 1:
                    titude = "北纬西经";
                    break;
                case 2:
                    titude = "南纬东经";
                    break;
                case 3:
                    titude = "南纬西经";
                    break;
            }

            String distance = String.valueOf((src[18] & 0xFF) + (src[19] & 0xFF) * 256 + (src[20] & 0xFF) * 256 * 256);
            String dirct = String.valueOf((((src[16] << 2) & 0xFF) >> 2) & 0x3F);  //方向
            //经纬度，纬度，经度，速度，方向值，里程碑
            String value = titude + "," + new DecimalFormat("0.0000").format(new BigDecimal(longtitudeValue)) + "," + new DecimalFormat("0.0000").format(new BigDecimal(latitudeValue));
            String other = speedValue + "," + Integer.valueOf(dirct) * 10 + "," + distance;


            String sql = "update sy_deviceinfo set sy_di_gps_value = ? ,sy_di_gps_other= ?,sy_di_gps_time = ? where sy_di_code = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, value);
            pst.setString(2, other);
            pst.setTimestamp(3, Timestamp.valueOf(timeValue));
            pst.setString(4, code);
            if (pst.executeUpdate() > 0) {

                String insertSql = "insert into sc_gpsrecord(sc_gr_time,sy_di_code,sc_gr_value,sc_gr_other) values(?,?,?,?)";
                PreparedStatement pstInser = connection.prepareStatement(insertSql);
                pstInser.setTimestamp(1, Timestamp.valueOf(timeValue));
                pstInser.setString(2, code);
                pstInser.setString(3, value);
                pstInser.setString(4, other);
                return pstInser.executeUpdate();
            }
        }
        return 0;


    }

    /**
     * @param
     * @author　xuyongzhe
     * @brief 基坑项目更新数据库
     **/
    public static int updateSensorHistory(String code, byte[] src, boolean kind) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String tem = "", damp = "", press = "", timeValue = "", strId = "环境终端";
        short highV = 0;
        String water = "", elect = "";
        if (kind) {  //传感器定时基本信息上传
            if (src.length >= 10) {
                tem = String.format("%d.%d", src[1] & 0xFF, src[0] & 0xFF);//温度
                damp = String.format("%d.%d", src[3] & 0xFF, src[2] & 0xFF);//湿度
                press = String.format("%d.%d", src[5] & 0xFF, src[4] & 0xFF);//气压
                elect = DataFormat.getVRange(src[6]);//电压
                highV = (short) ((src[7] & 0xFF) * 256 + (src[8] & 0xFF));//３８０动力电压
                if (src[9] == 0x01) {
                    water = "无积水";
                } else if (src[9] == (byte) 0xFF) {
                    water = "有积水";
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                timeValue = df.format(new Date());  //时间
            }
        } else {//ｌｏｒａ上传信息

            /*
             *    ‘*’    0
             *    5a    1
             *    00    协议id    2
             *    00    指令类型   3
             *    00 00 00 00 00 00 00 00  本地id  4 5 6 7 8 9 10 11
             *    00 00 00 00 00 00 00 00  目标di  12 13 14 15 16 17 18 19
             *    00 指令代码  20
             *    00 参数长度  21
             *
             *    //参数内容
             *    00   电压      22
             *    00   报警种类   23
             *    00 00 00 00 00 00 00
             *    传感器值  24 25 26 27 28 29　30
             *    00 00 16位校验  31 32
             *    00 00 00 00 00 00    33 34 35 36 37 38
             *    ‘#’   39
             * */


            if (src[0] == (byte) '*' && src[39] == (byte) '#') {
                byte[] time = new byte[6];
                System.arraycopy(src, 33, time, 0, 6);
                String strTime = DataFormat.bytes2HexString(time);
                timeValue = String.format("20%s-%s-%s %s:%s:%s", strTime.substring(0, 2), strTime.substring(2, 4),
                        strTime.substring(4, 6), strTime.substring(6, 8), strTime.substring(8, 10), strTime.substring(10, 12));  //截取时间数值
                elect = String.valueOf(DataFormat.getV(src[22]));
                if (((src[24] >> 7) & 0x01) == 0x00)
                    tem = String.valueOf((src[25] & 0xFF) / 10.0);
                else
                    tem = "-" + String.valueOf((src[25] & 0xFF) / 10.0);
                double d_damp = ((src[27] & 0xFF) + (src[26] & 0xFF) * 256) / 10.0;
                damp = String.valueOf(d_damp);  //湿度
               // System.out.println("damp:" + damp);
                double d_press = ((src[29] & 0xFF) + (src[28] & 0xFF) * 256) / 10.0;
                press = String.valueOf(d_press);   //气压
             //   System.out.println("press:" + press);
                if (src[30] == 0x01) {
                    water = "无积水";
                } else if (src[30] == (byte) 0x0FF) {
                    water = "有积水";
                }
                byte[] id = new byte[8];
                System.arraycopy(src, 8, id, 0, 4);
                strId = "PDA:" + DataFormat.bytes2HexString(id);
               // System.out.println("strId" + strId);
            }
        }

        if (!tem.equals("") && !damp.equals("") && !press.equals("") && !timeValue.equals("")) {
            String insertSql = "insert into se_historyrecord(se_hr_time,sy_di_code,se_hr_tem," +
                    "se_hr_hum,se_hr_pressure,se_hr_water,se_hr_380,se_hr_electric,se_hr_cont_code) values(?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstInser = connection.prepareStatement(insertSql);
            pstInser.setTimestamp(1, Timestamp.valueOf(timeValue));
            pstInser.setString(2, code);
            pstInser.setDouble(3, Double.valueOf(tem));
            pstInser.setDouble(4, Double.valueOf(damp));
            pstInser.setDouble(5, Double.valueOf(press));
            pstInser.setString(6, water);
            pstInser.setInt(7, highV);
            pstInser.setString(8, elect);
            pstInser.setString(9, strId);
            if (pstInser.executeUpdate() > 0) {
               // System.out.println(Double.valueOf(tem) + "," + Double.valueOf(damp) + "," + Double.valueOf(press) + "," + water + "," + highV + "," + elect + "," + code);
                if(!Double.valueOf(tem).equals(0.0) && !Double.valueOf(damp).equals(0.0)) {
                    String sql = "update se_sensorinfo set se_si_tem = ?," +
                            "se_si_hum=? ,se_si_pressure = ? ,se_si_time = ?" +
                            " ,se_si_water = ?,se_si_380=? ,se_si_electric = ?  " +
                            " where sy_di_code = ?";
                    PreparedStatement pst = connection.prepareStatement(sql);
                    pst.setDouble(1, Double.valueOf(tem));
                    pst.setDouble(2, Double.valueOf(damp));
                    pst.setDouble(3, Double.valueOf(press));
                    pst.setTimestamp(4, Timestamp.valueOf(timeValue));
                    pst.setString(5, water);
                    pst.setInt(6, highV);
                    pst.setString(7, elect);
                    pst.setString(8, code);
                    return pst.executeUpdate();
                }else{
                    String sql1 = "update se_sensorinfo set " +
                            "se_si_time = ?" +
                            " ,se_si_water = ?,se_si_electric = ?  " +
                            " where sy_di_code = ?";
                    PreparedStatement pst = connection.prepareStatement(sql1);
                    pst.setTimestamp(1, Timestamp.valueOf(timeValue));
                    pst.setString(2, water);
                    pst.setString(3, elect);
                    pst.setString(4, code);
                    return pst.executeUpdate();
                }
            }
        }
        return 0;
    }


    /**
     * author fantai-xyz
     * brief   采集传感器信息
     * params
     * <p> 数字id
     * <p> 传感器数组
     * time 2017/9/12 16:33
     * return
     */
    public static int updateSensorValue(String code, byte[] src, boolean kind) throws SQLException {

//        2字节温度，2字节湿度，2字节数字气压；1byte电量
        if (connection == null || connection.isClosed())
            connection = getConn();
        String tem = "", damp = "", press = "", timeValue = "", strId = "环境终端";
        double elect = 0.0;
        if (kind) {  //基本信息上传
            if (src.length > 6) {
                tem = String.format("%d.%d", src[1] & 0xFF, src[0] & 0xFF);//温度
                damp = String.format("%d.%d", src[3] & 0xFF, src[2] & 0xFF);//湿度
                press = String.format("%d.%d", src[5] & 0xFF, src[4] & 0xFF);//气压
                elect = DataFormat.getV(src[6]);//电压
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                timeValue = df.format(new Date());  //时间
            }
        } else {
            /*
             *    ‘*’    0
             *    5a    1
             *    00    协议id    2
             *    00    指令类型   3
             *    00 00 00 00 00 00 00 00  本地id  4 5 6 7 8 9 10 11
             *    00 00 00 00 00 00 00 00  目标di  12 13 14 15 16 17 18 19
             *    00 指令代码  20
             *    00 参数长度  21
             *
             *    //参数内容
             *    00   电压      22
             *    00   报警种类   23
             *    00 00 00 00 00 00
             *    传感器值  24 25 26 27 28 29
             *    00 00 16位校验  30 31
             *    00 00 00 00 00 00 32 33 34 35 36 37
             *    ‘#’   38
             * */
        //    System.out.println("传感器pda1");
            if (src.length >= 39) {
                //System.out.println("传感器pda");
                if (src[0] == (byte) '*' && src[38] == (byte) '#') {
                    byte[] time = new byte[6];
                    System.arraycopy(src, 32, time, 0, 6);
                    String strTime = DataFormat.bytes2HexString(time);
                    timeValue = String.format("20%s-%s-%s %s:%s:%s", strTime.substring(0, 2), strTime.substring(2, 4),
                            strTime.substring(4, 6), strTime.substring(6, 8), strTime.substring(8, 10), strTime.substring(10, 12));  //截取时间数值
                    elect = DataFormat.getV(src[22]);
                    if (((src[24] >> 7) & 0x01) == 0x00)
                        tem = String.valueOf((src[25] & 0xFF) / 10.0);
                    else
                        tem = "-" + String.valueOf((src[25] & 0xFF) / 10.0);
                    double d_damp = ((src[27] & 0xFF) + (src[26] & 0xFF) * 256) / 10.0;
                    damp = String.valueOf(d_damp);  //湿度
                  //  System.out.println("damp:" + damp);
                    double d_press = ((src[29] & 0xFF) + (src[28] & 0xFF) * 256) / 10.0;
                    press = String.valueOf(d_press);   //气压
                  //  System.out.println("press:" + press);
                    byte[] id = new byte[8];
                    System.arraycopy(src, 8, id, 0, 4);
                    strId = "PDA:" + DataFormat.bytes2HexString(id);
                  //  System.out.println("strId" + strId);
                }
            }

        }
        if (!tem.equals("") && !damp.equals("") && !press.equals("") && !timeValue.equals("")) {
            String insertSql = "insert into se_historyrecord(se_hr_time,sy_di_code,se_hr_tem,se_hr_hum,se_hr_pressure,se_hr_electric,se_hr_cont_code) values(?,?,?,?,?,?,?)";
            PreparedStatement pstInser = connection.prepareStatement(insertSql);
            pstInser.setTimestamp(1, Timestamp.valueOf(timeValue));
            pstInser.setString(2, code);
            pstInser.setString(3, tem);
            pstInser.setString(4, damp);
            pstInser.setString(5, press);
            pstInser.setDouble(6, elect);
            pstInser.setString(7, strId);
            if (pstInser.executeUpdate() > 0) {
                String sql = "update se_sensorinfo set se_si_tem = ?," +
                        "se_si_hum=? ,se_si_pressure = ? ,se_si_electric = ?,se_si_time = ? where sy_di_code = ?";

                PreparedStatement pst = connection.prepareStatement(sql);
                pst.setString(1, tem);
                pst.setString(2, damp);
                pst.setString(3, press);
                pst.setDouble(4, elect);
                pst.setTimestamp(5, Timestamp.valueOf(timeValue));
                pst.setString(6, code);
                return pst.executeUpdate();
            }
        }


        return 0;

    }

    /**
     * author fantai-xyz
     * brief   报警记录信息更新
     * params
     * time 2017/9/12 17:52
     * return
     */
    public static int updateAlarmValue(String code, byte[] src) throws SQLException {

        //‘*’    1                      0
        // 0x01  1                     1
        // 见报警类型表  1                2
        // 温湿度  ４                    3 4 5 6
        // 电池电压  1     　　　　　　　　　　　　7
        // ３８０动力电压	  1                ８
        // 积水　　　１　　　　９
        // 时间  6                10 11 12 13 14 15
        // #  1                   16
        if (connection == null || connection.isClosed())
            connection = getConn();

        if (src.length >= 17) {
          //  System.out.println("报警更新数据库");
            if (src[0] == (byte) '*' && src[src.length - 1] == (byte) '#')  //判断终止符和起始符
            {
                String alramType = "", electValue = "", state = "", machineState = "", highV = "", water = "";
                String tem = "", damp = "";
                if (src[1] == 0x01) {
                    //报警类型
                    {
                        if ((src[2] & 0x01) == 0x01) {
                            alramType += "电池欠压报警 ";
                        }
                        if (((src[2] >> 1) & 0x01) == 0x01) {
                            alramType += "高温报警 ";
                        }
                        if (((src[2] >> 2) & 0x01) == 0x01) {
                            alramType += "低温报警 ";
                        }
                        if (((src[2] >> 3) & 0x01) == 0x01) {
                            alramType += "高湿报警 ";
                        }
                        if (((src[2] >> 4) & 0x01) == 0x01) {
                            alramType += "干燥报警 ";
                        }
                        if (((src[2] >> 5) & 0x01) == 0x01) {
                            alramType += "380v动力电压报警 ";
                        }
                        if (((src[2] >> 6) & 0x01) == 0x01) {
                            alramType += "积水报警 ";
                        }

                    }

                    //温湿度
                    {
                        tem = String.format("%d.%d", src[4] & 0xFF, src[3] & 0xFF); //温度
                        damp = String.format("%d.%d", src[6] & 0xFF, src[5] & 0xFF);//湿度
                    }

                    //电池电压
                    electValue = DataFormat.getVRange(src[7]);
                    //380v电压
                    switch (src[8]) {
                        case 0x40:
                            highV = "小于380v的10%";
                            break;
                        case 0x41:
                            highV = "大于380v的10%";
                            break;
                    }
                    //积水情况
                    switch (src[9]) {
                        case 0x01:
                            water = "无积水";
                            break;
                        case (byte) 0xFF:
                            water = "有积水";
                            break;
                    }


                    byte[] bCode = DataFormat.hexStringToBytes(code);
                    byte[] time = new byte[6];
                    System.arraycopy(src, 10, time, 0, 6);
                  //  System.out.println("time" + DataFormat.bytes2HexString(time));
                    String timeStr = DataFormat.bytes2HexString(time);

                    String timeValue = String.format("20%s-%s-%s %s:%s:%s", timeStr.substring(0, 2),
                            timeStr.substring(2, 4), timeStr.substring(4, 6), timeStr.substring(6, 8),
                            timeStr.substring(8, 10), timeStr.substring(10, 12));
                    int iAlartType = bCode[0] & 0xFF;
//                    sc_ar_id int(11) PK
//                    sc_ar_time datetime
//                    sy_di_code varchar(20)
//                    sy_di_type smallint(6)
//                    sc_ar_type varchar(20)
//                    sc_ar_status varchar(20)
//                    sc_ar_electric varc
                    String sql = "insert into sc_alarmrecord (sc_ar_time,sy_di_code," +
                            "sy_di_type,sc_ar_type,sc_ar_tem,sc_ar_hum,sc_ar_water,sc_ar_380,sc_ar_electric) " +
                            " values(?,?,?,?,?,?,?,?,?)";

                    PreparedStatement pst = connection.prepareStatement(sql);
                    pst.setTimestamp(1, Timestamp.valueOf(timeValue));  //时间
                    pst.setString(2, code);  //标识码
                    pst.setInt(3, iAlartType);  //类型
                    pst.setString(4, alramType);  //报警类型
                    pst.setString(5, tem);  //温度
                    pst.setString(6, damp);  //湿度
                    pst.setString(7, water);  //积水
                    pst.setString(8, highV);  //动力电压
                    pst.setString(9, electValue);  //电压值范围
                  //  System.out.println("数据操作成功");
                    return pst.executeUpdate();

                }
            }


        }
        return 0;

    }


    /**
     * author fantai-xyz
     * brief 更新code ip地址
     * params
     * time 2017/9/26 15:38
     * return
     */
    public static int UpdateCodeIP(String code, String ip, String port) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "update sy_deviceinfo set sy_di_ip = ?, sy_di_port = ? where sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, ip);
        pst.setString(2, port);
        pst.setString(3, code);
        return pst.executeUpdate();

    }

    /**
     * author fantai-xyz
     * brief  获取卡号的当前的数据库状态以及，余额额
     * params  1，入库，2出库
     * time 2017/9/26 16:58
     * return
     */
    public static int getCarInfoState(String code, String cardInfo) throws SQLException {


        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select st_ci_status from st_carinfo where sy_di_code = ? and st_ci_nfc = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        pst.setString(2, cardInfo);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    /**
     * author fantai-xyz
     * brief  获取余额
     * params
     * time 2017/9/26 17:05
     * return
     */
    public static int getCarInfoMoney(String code, String cardInfo) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select st_ci_money from st_carinfo where sy_di_code = ? and st_ci_nfc = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        pst.setString(2, cardInfo);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next())
            return (int) resultSet.getDouble(1);
        return 0;
    }

    /**
     * author fantai-xyz
     * brief 出库是修改电量跟余额,状态
     * params
     * time 2017/9/27 9:50
     * return
     */
    public static int updateCarInfoMonEle(int state, double money, double ele, String code, String carInfo) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "UPDATE st_carinfo set st_ci_status = ? , st_ci_money = ?,st_ci_electric = ?  where sy_di_code = ? and st_ci_nfc = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, state);
        pst.setDouble(2, money);
        pst.setDouble(3, ele);
        pst.setString(4, code);
        pst.setString(5, carInfo);
        return pst.executeUpdate();
    }

    /**
     * author fantai-xyz
     * brief 入库是修改电量,状态
     * params
     * time 2017/9/27 10:19
     * return
     */
    public static int updateCarInfoEle(int state, double ele, String code, String carInfo) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "update st_carinfo set st_ci_status = ?, st_ci_electric = ?  where sy_di_code = ? and st_ci_nfc = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, state);
        pst.setDouble(2, ele);
        pst.setString(3, code);
        pst.setString(4, carInfo);
        return pst.executeUpdate();
    }

    /**
     * author fantai-xyz
     * brief   查询车牌号
     * params
     * time 2017/9/27 15:25
     * return
     */
    public static String getCarNum(String code) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select st_ci_carnum from st_carinfo where sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next())
            return resultSet.getString(1);
        return "";


    }

    /**
     * author fantai-xyz
     * brief 插入消费记录
     * params
     * time 2017/10/13 10:43
     * return
     */
    public static int insConsumerRecord(String code, String time, double money) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String carNum = getCarNum(code);
        String sql = "insert into st_consumerecord(st_ci_carnum,st_cr_time,st_cr_num) values(?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, carNum);
        pst.setTimestamp(2, Timestamp.valueOf(time));
        pst.setDouble(3, money);
        return pst.executeUpdate();
    }


    /**
     * author fantai-xyz
     * brief  插入停车记录
     * params
     * time 2017/9/27 15:41
     * return
     */
    public static int insertIcHistory(String code, String startTime, int state) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String carNum = getCarNum(code);
       // System.out.println("carNum" + carNum);
        String sql = "insert into st_parkrecord(st_ci_carnum,st_pr_stime,st_pr_status) values(?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, carNum);
        pst.setTimestamp(2, Timestamp.valueOf(startTime));
        pst.setInt(3, state);
        return pst.executeUpdate();

    }

    /**
     * author fantai-xyz
     * brief 查询 卡号id
     * params
     * time 2017/9/27 16:49
     * return
     */
    public static int selectICId(String code) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String carNum = getCarNum(code);
        String sql = "select max(st_pr_id) from st_parkrecord where st_ci_carnum = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, carNum);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next())
            return resultSet.getInt(1);
        return 0;

    }

    /**
     * author fantai-xyz
     * brief  更新ic卡号的数据信息
     * params
     * time 2017/9/28 7:37
     * return
     */
    public static int updateIcHistory(String code, String endTime, String time, int state, int num) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        int id = selectICId(code);
        String sql = "update st_parkrecord set  st_pr_etime = ? ,st_pr_time = ?,st_pr_status = ?,st_pr_num = ? where st_pr_id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setTimestamp(1, Timestamp.valueOf(endTime));
        pst.setString(2, time);
        pst.setInt(3, state);
        pst.setInt(4, num);
        pst.setInt(5, id);
        return pst.executeUpdate();
    }

    /**
     * author fantai-xyz
     * brief 获取ic卡的开始时间
     * params
     * time 2017/9/28 15:22
     * return
     */
    public static String getIcStartTime(String code) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = getConn();
        }
        int id = selectICId(code);  //查询id号码
        String sql = "select st_pr_stime from st_parkrecord where st_pr_id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next()) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            return df.format(resultSet.getTimestamp(1));
        }
        return "";

    }


    /**
     * author fantai-xyz
     * brief 根据ip地址获取code码
     * params
     * time 2017/10/16 10:40
     * return
     */
    public static String selDeviceCode(String ip) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select  sy_di_code from  sy_deviceinfo  where  sy_di_ip = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, ip);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return "";

    }


    /**
     * author fantai-xyz
     * brief  根据ip地址更改在线状态
     * params
     * time 2017/10/16 11:27
     * return
     */
    public static void updateDeviceState(String ip, int state) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String code = selDeviceCode(ip);
        if (!code.equals("") && code.length() > 2) {
            String codeId = code.substring(0, 2).toUpperCase();
            String sql = "";
            if (codeId.equals("0B")) {
                sql = "update sl_lockinfo set sl_li_online = ? where sy_di_code = ?";
            } else if (codeId.equals("33")) {
                sql = "update se_sensorinfo set se_si_online = ? where sy_di_code = ?";
            } else if (codeId.equals("12")) {
                sql = "update st_carinfo set st_ci_online = ? where sy_di_code = ?";
            }
            PreparedStatement pst = connection.prepareStatement(sql);

            pst.setInt(1, state);
            pst.setString(2, code);
            pst.executeUpdate();
         //   System.out.println(code + ":数据库更改状态" + state);
        }

    }


    /**
     * author fantai-xyz
     * brief  更改设备状态
     * params
     * time 2017/9/28 13:34
     * return
     */
    public static void updateDeviceState(byte codeId, String code, int state) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "";
        switch (codeId) {
            case (byte) 0x0B: // 锁状态信息
                sql = "update sl_lockinfo set sl_li_online = ? where sy_di_code = ?";
                break;
            case (byte) 0x33:  //农业信息
                sql = "update se_sensorinfo set se_si_online = ? where sy_di_code = ?";
                break;
            case (byte) 0x12:  //停车场信息
                sql = "update st_carinfo set st_ci_online = ? where sy_di_code = ?";
                break;
        }
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, state);
        pst.setString(2, code);
        pst.executeUpdate();
       // System.out.println(code + ":数据库更改状态" + state);
    }

    /**
     * author fantai-xyz
     * brief 更新信号和星
     * params
     * time 2017/9/28 14:45
     * return
     */
    public static int updateDeviceSignal(String code, String signal, String satellite) throws SQLException {

        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "update sy_deviceinfo set sy_di_gprs_signal = ?,sy_di_gps_satellite = ? where  sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, signal);
        pst.setString(2, satellite);
        pst.setString(3, code);
        return pst.executeUpdate();
    }

    /**
     * author fantai-xyz
     * brief 获取心跳检测的时间
     * params
     * time 2017/10/16 16:41
     * return
     */
    public static Timestamp getHeartTimeByCode(String code) throws SQLException {
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select max(sc_hbr_time) from  sc_heartbeatrecord where sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next()) {
            return resultSet.getTimestamp(1);
        }
        return null;


    }


    /**
     * author fantai-xyz
     * brief  查询数据库的心跳时间间隔
     * params
     * time 2017/10/17 10:40
     * return
     */
    public static int getDeivceHeartNumByCode(String code) throws SQLException {
        int heartNum = 10;
        if (connection == null || connection.isClosed())
            connection = getConn();
        String sql = "select  sy_di_heartbeat_num  from sy_deviceinfo where sy_di_code = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, code);
        ResultSet resultSet = pst.executeQuery();
        if (resultSet.next())
            heartNum = resultSet.getInt(1);
        return heartNum;
    }


}
