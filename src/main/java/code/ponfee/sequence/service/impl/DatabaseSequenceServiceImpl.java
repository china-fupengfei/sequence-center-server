package code.ponfee.sequence.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import code.ponfee.sequence.dao.ISequenceDao;
import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.model.SequenceRange;
import code.ponfee.sequence.seq.DefaultSequence;
import code.ponfee.sequence.seq.Sequence;
import code.ponfee.sequence.service.ISequenceService;

/**
 * sequence service
 * @author fupf
 */
@Service("databaseSequenceService")
public class DatabaseSequenceServiceImpl implements ISequenceService {
    private final static Lock LOCK = new ReentrantLock();
    /** 保存序列（一个数据序列对应一个sequence）*/
    private static final Map<String, Sequence> SEQUENCE_HOLDER = new ConcurrentHashMap<>();
    private static final int MAX_SIZE = 1000;

    private @Value("${sequence.range.size:100}") int seqRangeSize;
    private @Resource ISequenceDao sequenceDao;

    @Override
    public long nextValue(String name) throws SequenceIsOverException, SequenceNotFoundException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("seq name not allow blank");
        }

        // 获取序列
        Sequence sequence = SEQUENCE_HOLDER.get(name);
        if (sequence == null) {
            LOCK.lock();
            try {
                sequence = SEQUENCE_HOLDER.get(name);
                if (sequence == null) {
                    sequence = new DefaultSequence(name, sequenceDao, seqRangeSize);
                    SEQUENCE_HOLDER.put(name, sequence);
                }
            } finally {
                LOCK.unlock();
            }
        }

        try {
            // 获取序列值
            return sequence.nextValue();
        } catch (SequenceNotFoundException e) {
            SEQUENCE_HOLDER.remove(name);
            throw e;
        }
    }

    @Override
    public SequenceRange nextRange(String name, int size) throws SequenceIsOverException, SequenceNotFoundException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("seq name not allow blank");
        }
        if (size > MAX_SIZE) size = MAX_SIZE;
        return sequenceDao.nextRange(name, size);
    }

    @Override
    public long nextSerial(String name) throws SequenceIsOverException, SequenceNotFoundException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("seq name not allow blank");
        }
        return sequenceDao.nextRange(name, 1).current();
    }
}
