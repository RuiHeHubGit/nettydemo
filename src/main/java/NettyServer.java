import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public class NettyServer {
    public final static int DEF_PORT = 6666;
    private static int serverPort;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup wokerGroup;
    private static Consumer<Throwable> callback;


    public static void start() {
        start(null);
    }

    public static void start(Consumer<Throwable> callback) {
        start(DEF_PORT, callback);
    }

    public static void start(int port, Consumer<Throwable> callback) {
        serverPort = port;
        NettyServer.callback = callback;
        int aps = Runtime.getRuntime().availableProcessors();
        bossGroup = new NioEventLoopGroup(aps * 2);
        wokerGroup = new NioEventLoopGroup(aps * 2);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, wokerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))
                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new ServerChannelHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            System.out.println("server start ..");
            ChannelFuture future = bootstrap.bind(port).sync();
            if(callback != null) {
                callback.accept(null);
            }
            future.channel().closeFuture().sync();
            System.out.println("server close.");
        } catch (InterruptedException e) {
            if(callback != null) {
                callback.accept(e);
            }
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            wokerGroup.shutdownGracefully();
        }

    }
}
