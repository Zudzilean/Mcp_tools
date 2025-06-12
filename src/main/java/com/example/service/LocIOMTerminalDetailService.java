package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.LocIOMTerminalDetail;
import org.noear.solon.annotation.Param;
import noear.solon.ai.mcp.server.annotation.ToolMapping;
import java.time.LocalDateTime;
import java.util.List;

public interface LocIOMTerminalDetailService extends IService<LocIOMTerminalDetail> {
    
    /**
     * 根据日期范围查询终端详情
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 终端详情列表
     */
    List<LocIOMTerminalDetail> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @ToolMapping(description = "导出CSV文件" +
            "\n参数说明：" +
            "\nstartDate：开始日期时间" +
            "\nendDate：结束日期时间" +
            "\noutputPath：CSV文件保存路径" +
            "\n返回值：成功时返回CSV文件路径及数据统计信息，失败时返回错误信息")
    String exportToCsv(
            @Param(name = "startDate", description = "开始日期时间") LocalDateTime startDate,
            @Param(name = "endDate", description = "结束日期时间") LocalDateTime endDate,
            @Param(name = "outputPath", description = "CSV文件保存路径") String outputPath) throws Exception;
    
    @ToolMapping(description = "筛选CSV数据" +
            "\n参数说明：" +
            "\nsourceCsvPath：源CSV文件路径" +
            "\nconditions：筛选条件（JSON格式，例如：{\"region\": 1, \"materialCode\": 12345}）" +
            "\noutputPath：输出CSV文件路径" +
            "\n返回值：成功时返回CSV文件路径及数据统计信息，失败时返回错误信息")
    String filterCsvData(
            @Param(name = "sourceCsvPath", description = "源CSV文件路径") String sourceCsvPath,
            @Param(name = "conditions", description = "筛选条件（JSON格式）") String conditions,
            @Param(name = "outputPath", description = "输出CSV文件路径") String outputPath) throws Exception;
} 