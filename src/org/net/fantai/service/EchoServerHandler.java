package org.net.fantai.service;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.net.fantai.model.DataMsg;
import org.net.fantai.slqhepler.SqlDao;

public class EchoServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    DataMsg dataMsg = new DataMsg();

    //	SqlDao sqlDao = new SqlDao();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        try {
            ByteBuf buf = packet.copy().content();
            byte[] buffer = new byte[buf.readableBytes()];
            buf.readBytes(buffer);
            String body = new String(buffer);
            dataMsg.RecieveDataMsg(buffer, buffer.length, ctx, 1, packet);
        } catch (Exception e) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(new byte[]{(byte)0xEE,(byte)0xEE,(byte)0xEE,(byte)0xEE});
            ctx.writeAndFlush(new DatagramPacket(byteBuf,packet.sender()));
              e.printStackTrace();
            //SqlDao.closeConnection();
        }
    }


}
