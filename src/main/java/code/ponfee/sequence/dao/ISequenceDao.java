package code.ponfee.sequence.dao;

import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;
import code.ponfee.sequence.model.SequenceRange;

/**
 * dao interfafe
 * @author fupf
 */
public interface ISequenceDao {

    SequenceRange nextRange(String name, int size) throws SequenceNotFoundException, SequenceIsOverException;
}
