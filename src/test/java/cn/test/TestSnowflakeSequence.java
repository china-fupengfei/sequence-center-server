package cn.test;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.service.ISequenceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:dubbo-consumer.xml" })
public class TestSnowflakeSequence {
    private static final Set<Long> SET = new HashSet<>();

    @Resource(name="sequenceService")
    private ISequenceService service;

    @Test
    public void testSequence() throws InterruptedException {
        for (int j = 0; j < 100; j++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Long key;
                        try {
                            key = service.nextValue(null);
                            if (key > 0 && !SET.add(key)) System.err.println(key);
                        } catch(SequenceIsOverException | SequenceNotFoundException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }).start();

        }
        Thread.sleep(10000);
        // 384109
        System.out.println(SET.size());
    }
}
