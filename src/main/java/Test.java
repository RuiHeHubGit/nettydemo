import java.util.Arrays;

public class Test {
    public static void f(Object...o) {
        System.out.println(o.length);
        System.out.println(Arrays.toString(o));
    }
    public static void main(String[] args) {
        new Thread(()->{
            NettyServer.start();
        }).start();

        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            NettyClient.start("127.0.0.1", 6666);
        }).start();

        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}
