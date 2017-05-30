package code.ponfee.sequence.dao.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * mapper
 * @author fupf
 */
public interface SequenceMapper {

    Map<String, Object> nextRange(@Param("seq") String seq, @Param("size") int size);
}
