package org.net.fantai.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by fantai-xyz on 2017/7/13.
 */
public class UdpServiceServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();

        System.out.println("udp服务器启动中");
        UdpServiceThread.StartUdp();
        System.out.println("tcp服务器启动中");
        TcpServiceThread tcpServiceThread = new TcpServiceThread();
        tcpServiceThread.startTcp();
        System.out.println("服务器启动完成");
    }
}
