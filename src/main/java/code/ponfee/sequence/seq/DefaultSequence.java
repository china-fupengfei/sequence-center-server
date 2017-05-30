package code.ponfee.sequence.seq;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import code.ponfee.sequence.dao.ISequenceDao;
import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.model.SequenceRange;

/**
 * default sequence
 * @author fupf
 */
public class DefaultSequence implements Sequence {

    private final Lock lock = new ReentrantLock();
    private final String name;
    private final ISequenceDao sequenceDao;
    private final int size;
    private volatile SequenceRange range;

    public DefaultSequence(String name, ISequenceDao keyCenterDao) {
        this(name, keyCenterDao, 1000);
    }

    public DefaultSequence(String name, ISequenceDao keyCenterDao, int size) {
        this.name = name;
        this.sequenceDao = keyCenterDao;
        this.size = size;
        range = new SequenceRange(0, -1);
    }

    public long nextValue() throws SequenceNotFoundException, SequenceIsOverException {
        // 获取序列并自增
        long value = range.next();
        if (value == -1) {
            // 当溢出时，重新向数据库申请
            lock.lock();
            try {
                for (;;) {
                    if (range.isOver()) {
                        range = sequenceDao.nextRange(name, size);
                    }
                    value = range.next();
                    if (value == -1) {
                        continue;
                    } else {
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return value;
    }

}
