package code.ponfee.sequence.seq;

import code.ponfee.sequence.exception.SequenceIsOverException;
import code.ponfee.sequence.exception.SequenceNotFoundException;

/**
 * Sequence
 * @author fupf
 */
public interface Sequence {
    long nextValue() throws SequenceNotFoundException, SequenceIsOverException;
}
