package org.net.fantai.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.net.fantai.model.DataMsg;
import org.net.fantai.slqhepler.SqlDao;

import java.util.concurrent.TimeUnit;

/**
 * Created by fantai-xyz on 2017/9/14.
 */
public class TcpServiceThread {





    public static  ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
     DataMsg dataMsg = new DataMsg();
    public  class TcpServerHandler extends ChannelInboundHandlerAdapter {

        private int idle_count=1;
        private int count=1;
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            super.userEventTriggered(ctx, evt);
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (IdleState.READER_IDLE.equals(event.state())) {

                    if (idle_count > 2) {
                        System.out.println("关闭这个不活跃:" + ctx.channel().remoteAddress().toString());

//                        removeDeviceIp(ctx.channel().remoteAddress().toString());
                        channels.remove(ctx.channel());
                        ctx.channel().close();
                        System.out.println("over");
                    }
                    idle_count++;
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }

        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)  {
            try {
                ByteBuf buf = (ByteBuf) msg;
                byte[] buffer = new byte[buf.readableBytes()];
                buf.readBytes(buffer);
                String body = new String(buffer);
                dataMsg.RecieveDataMsg(buffer, buffer.length, ctx, 0, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

            //将发送缓冲区中数据全部写入SocketChannel
            //ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //释放资源
            System.out.println("解析指令错误");
           ByteBuf byteBuf = Unpooled.copiedBuffer(new byte[]{(byte)0xEE,(byte)0xEE,(byte)0xEE,(byte)0xEE});
           ctx.writeAndFlush(byteBuf);
           //SqlDao.closeConnection();
            //ctx.close();
        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel incoming = ctx.channel();
            System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "在线");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Channel incoming = ctx.channel();
            String ip = incoming.remoteAddress().toString().split(":")[0].substring(1);
            SqlDao.updateDeviceState(ip,0);  //tcp更改掉线状态
            System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "掉线");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        Channel incoming = ctx.channel();
//        System.out.println("123");
//        for (Channel channel : channels) {
//            String body = "[SERVER] - " + incoming.remoteAddress() + " 加入\n";
//            ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
//            channel.writeAndFlush(resp);
//        }
            channels.add(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
//            String body = "[SERVER] - " + incoming.remoteAddress() + " 离开\n";
//            ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
//            channel.writeAndFlush(resp);
//        }
            channels.remove(ctx.channel());
        }
    }

    public  class ChildChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new IdleStateHandler(5,0,0, TimeUnit.MINUTES));
            channel.pipeline().addLast("timeServerHandler", new TcpServerHandler());
        }
    }


    private  void bind(int port) throws Exception {
        //配置服务端NIO 线程组


        int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认
        /** 业务出现线程大小 */
        int BIZTHREADSIZE = 4;
            /*
             * NioEventLoopGroup实际上就是个线程池,
		     * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
		     * 每一个NioEventLoop负责处理m个Channel,
		     * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
		     */

        EventLoopGroup boss = new NioEventLoopGroup(BIZGROUPSIZE);
        EventLoopGroup worker = new NioEventLoopGroup(BIZTHREADSIZE);

        ServerBootstrap server = new ServerBootstrap();

        try {
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelInitializer());

            //绑定端口, 同步等待成功
            ChannelFuture future = server.bind(port).sync();

            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
            System.out.println("123");
        } finally {
            //优雅关闭 线程组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * author fantai-xyz
     * brief  开始tcp连接
     * params
     * time 2017/9/14 10:10
     * return
     */
    public  void startTcp() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bind(8001);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }


        ).start();


    }


}
