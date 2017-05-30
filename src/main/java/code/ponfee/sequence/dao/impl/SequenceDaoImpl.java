package code.ponfee.sequence.dao.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import code.ponfee.sequence.dao.ISequenceDao;
import code.ponfee.sequence.dao.mapper.SequenceMapper;
import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.model.SequenceRange;

/**
 * dao impl
 * @author fupf
 */
@Repository
public class SequenceDaoImpl implements ISequenceDao {

    private @Resource SequenceMapper mapper;

    @Override
    public SequenceRange nextRange(String seq, int size) throws SequenceNotFoundException, SequenceIsOverException {
        Map<String, Object> result = mapper.nextRange(seq, size);

        if (result == null) {
            throw new SequenceNotFoundException(seq + " not found");
        }
        Long start = (Long) result.get("start");
        Long end = (Long) result.get("end");
        if (start == null || end == null) {
            throw new SequenceNotFoundException("sequence not found:" + seq);
        } else if (start == 0 && end == -1) {
            throw new SequenceIsOverException("sequence is over:" + seq);
        }
        return new SequenceRange(start, end);
    }

}
