package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.LocIOMTerminalDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LocIOMTerminalDetailMapper extends BaseMapper<LocIOMTerminalDetail> {
    
    /**
     * 根据日期范围查询数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 数据列表
     */
    List<LocIOMTerminalDetail> getByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
} 