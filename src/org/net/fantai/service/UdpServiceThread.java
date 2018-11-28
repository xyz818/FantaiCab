package org.net.fantai.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Created by fantai-xyz on 2017/8/16.
 */
public class UdpServiceThread {
    public static Channel channel = null;

    public static void SendMsg(String ip,int port,byte[] sendMsg)
    {

        try
        {
            if(channel != null)
            {

                channel.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(sendMsg),
                        new InetSocketAddress(ip, port))).sync();
            }
        }
        catch (Exception e)
        {
            System.out.println("send error");

        }

    }

    /**
    * author fantai-xyz
    * brief
    * params
    * time 2017/8/16 11:32
    * return
    */
    public static void StartUdp() {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Bootstrap b = new Bootstrap();
                        EventLoopGroup group = new NioEventLoopGroup();
                        b.group(group)
                                .channel(NioDatagramChannel.class)
                                .handler(new EchoServerHandler());
                        //
                        try {
//                            b.option(ChannelOption.SO_KEEPALIVE,true);
                            channel = b.bind(8001).sync().channel();
                            channel.closeFuture().await();
                            System.out.println("netty start");
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            //e.printStackTrace();
                            System.out.println("netty error");
                        }
                    }
                }
        ).start();


    }


}
