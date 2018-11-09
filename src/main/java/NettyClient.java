import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyClient {
    private static String host;
    private static int port;
    private static EventLoopGroup workGroup;

    public static void start(String host, int port) {
        NettyClient.host = host;
        NettyClient.port = port;

        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer() {

                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline()
                                .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast("frameEncoder", new LengthFieldPrepender(4))
                                .addLast("decider", new StringDecoder(CharsetUtil.UTF_8))
                                .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new ClientChannelHandler());
                    }
                });
        try {
            System.out.println("client start ..");

            for (int i=0; i<10000; ++i) {
                ChannelFuture future = bootstrap.connect(host, port).sync();
                future.channel().writeAndFlush("hello"+i);
                future.channel().closeFuture().sync();
            }
            System.out.println("client start");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
