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
    private String host;
    private int port;
    private EventLoopGroup workGroup;

    public static void start(String host, int port) {
        NettyClient client = new NettyClient();
        client.host = host;
        client.port = port;

        client.workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(client.workGroup)
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
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush("hello");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            client.workGroup.shutdownGracefully();
        }
    }
}
