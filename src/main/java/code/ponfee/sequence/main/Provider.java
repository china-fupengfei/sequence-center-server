package code.ponfee.sequence.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * main
 * @author fupf
 */
public class Provider {

    private static ClassPathXmlApplicationContext context;

    public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext(new String[] { "META-INF/spring/*.xml" });
        context.start();
        System.out.println("==========================序列生成中心已启动=========================");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
