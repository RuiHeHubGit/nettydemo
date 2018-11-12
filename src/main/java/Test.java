import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Test {
    public static void f(Object...o) {
        System.out.println(o.length);
        System.out.println(Arrays.toString(o));
    }
    public static void main(String[] args) {
        CountDownLatch cdh = new CountDownLatch(1);
        new Thread(()->{
            NettyServer.start((t)->{
                if(t == null) {
                    System.out.println("server start.");
                }
                cdh.countDown();
            });
        }).start();

        try {
            cdh.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        int count = 1000;
        CountDownLatch countDownLatch2 = new CountDownLatch(count);
        for (int i=0; i< count; ++i) {
            new Thread(()-> {
                NettyClient.start("127.0.0.1", 6666);
                countDownLatch2.countDown();
            }).start();
        }
        try {
            countDownLatch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long costTime = System.currentTimeMillis() - startTime;
        System.out.println(costTime+","+1000000L/costTime);
    }
}
