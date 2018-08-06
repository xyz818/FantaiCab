package org.net.fantai.slqhepler;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Created by fantai-xyz on 2017/10/17.
 */
public class C3P0cnn {
    private static ComboPooledDataSource cpds;
    static {
        initDataSource();
    }

    // 配置数据源
    public static void initDataSource() {
        cpds = new ComboPooledDataSource();
        cpds.setDataSourceName("acms01");
        cpds.setJdbcUrl("jdbc:mysql://www.ftiotcloud.cn:3306/ft_case?autoReconnect=true&amp;autoReconnectForPools=true");//连接url
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } //数据库驱动
        cpds.setUser("root");//用户名
        cpds.setPassword("test123");//密码
        cpds.setMaxPoolSize(100);//连接池中保留的最大连接数
        cpds.setMinPoolSize(10);//连接池中保留的最小连接数
        cpds.setAcquireIncrement(10);//一次性创建新连接的数目
        cpds.setInitialPoolSize(10);//初始创建
        cpds.setMaxIdleTime(200);//最大空闲时间

    }//*/

    private C3P0cnn(){}

    // 从连接池中获得连接对象
    public static Connection  getConnection(){
        try {
            return cpds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
