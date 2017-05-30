package cn.test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.model.SequenceRange;
import code.ponfee.sequence.service.ISequenceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:dubbo-consumer.xml" })
public class TestDatabaseSequence {
    private static final Set<Long> SET = new HashSet<>();

    @Resource(name = "sequenceService")
    private ISequenceService service;

    @Test
    public void testSequence() throws InterruptedException {
        int threadCount = 20;
        //StopWatch watch = new StopWatch();
        //final CountDownLatch latch = new CountDownLatch(threadCount);

        //watch.start();
        for (int j = 0; j < threadCount; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Long key;
                        try {
                            key = service.nextSerial("seq_user");
                            if (key < 1) {
                                //latch.countDown();
                                //break;
                            } else if (!SET.add(key)) {
                                System.err.println(key);
                            } else {
                                System.out.println(key);
                            }
                        } catch (SequenceIsOverException | SequenceNotFoundException e) {
                            //e.printStackTrace();
                            return;
                        }
                    }
                }
            }).start();

        }
        //latch.await();
        //watch.stop();
        Thread.sleep(15000);
        System.out.println("======================================================" + SET.size());
    }

    @Test
    public void testNextValue() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(true);
        for (int j = 0; j < 10; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long key;
                    while (true && flag.get()) {
                        try {
                            key = service.nextValue("seq_order");
                            if (!SET.add(key)) System.err.println(key);
                        } catch (SequenceIsOverException | SequenceNotFoundException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }).start();
        }
        Thread.sleep(10000);
        // 237205 (step:1000)
        flag.set(false);
        System.out.println(SET.size());
        Thread.sleep(1000);
    }

    @Test
    public void testNextSerial() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(true);
        for (int j = 0; j < 100; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true && flag.get()) {
                        Long key;
                        try {
                            key = service.nextSerial("seq_order");
                            if (!SET.add(key)) System.err.println(key);
                        } catch (SequenceIsOverException | SequenceNotFoundException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }).start();

        }
        Thread.sleep(10000);
        // 3242
        System.out.println(SET.size());
        flag.set(false);
        Thread.sleep(1000);
    }

    @Test
    public void testNextRange() throws InterruptedException, SequenceIsOverException, SequenceNotFoundException {
        SequenceRange r = service.nextRange("seq_order", 100);
        System.out.println(r.current());
    }

    @Test
    public void testNextRange2() throws SequenceIsOverException, SequenceNotFoundException {
        System.out.println(service.nextValue("seq_order"));
    }

}
