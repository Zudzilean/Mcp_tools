package com.example.tool;

import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Param;
import noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import noear.solon.ai.mcp.server.annotation.ToolMapping;
import com.example.service.LocIOMTerminalDetailService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@McpServerEndpoint(sseEndpoint = "/mcp/CsvGeneratorTool/sse3")
public class CsvGeneratorTool {

    @Inject
    private LocIOMTerminalDetailService terminalDetailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @ToolMapping(description = "生成CSV文件" +
            "\n参数说明：" +
            "\nstartDate：开始日期时间（格式：yyyy-MM-dd HH:mm:ss）" +
            "\nendDate：结束日期时间（格式：yyyy-MM-dd HH:mm:ss）" +
            "\noutputPath：CSV文件保存路径" +
            "\n返回值：成功时返回CSV文件路径及数据统计信息，失败时返回错误信息")
    public String generateCsv(
        @Param(name = "startDate", description = "开始日期时间（格式：yyyy-MM-dd HH:mm:ss）") String startDate,
        @Param(name = "endDate", description = "结束日期时间（格式：yyyy-MM-dd HH:mm:ss）") String endDate,
        @Param(name = "outputPath", description = "CSV文件保存路径") String outputPath
    ) {
        try {
            System.out.println("开始生成CSV文件，参数：startDate=" + startDate + ", endDate=" + endDate + ", outputPath=" + outputPath);
            
            // 转换日期字符串为LocalDateTime对象
            LocalDateTime start = LocalDateTime.parse(startDate, DATE_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate, DATE_FORMATTER);
            
            // 调用Service导出CSV
            String result = terminalDetailService.exportToCsv(start, end, outputPath);
            
            System.out.println("CSV文件生成成功：" + result);
            return result;
        } catch (Exception e) {
            String errorMsg = "生成CSV文件失败：" + e.getMessage();
            System.out.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
        }
    }

    @ToolMapping(description = "筛选CSV数据" +
            "\n参数说明：" +
            "\nsourceCsvPath：源CSV文件路径" +
            "\nconditions：筛选条件（JSON格式，例如：{\"region\": 1, \"materialCode\": 12345}）" +
            "\noutputPath：输出CSV文件路径" +
            "\n返回值：成功时返回CSV文件路径及数据统计信息，失败时返回错误信息")
    public String filterData(
        @Param(name = "sourceCsvPath", description = "源CSV文件路径") String sourceCsvPath,
        @Param(name = "conditions", description = "筛选条件（JSON格式）") String conditions,
        @Param(name = "outputPath", description = "输出CSV文件路径") String outputPath
    ) {
        try {
            System.out.println("开始筛选CSV数据，参数：sourceCsvPath=" + sourceCsvPath + ", conditions=" + conditions + ", outputPath=" + outputPath);
            
            // 调用Service筛选CSV
            String result = terminalDetailService.filterCsvData(sourceCsvPath, conditions, outputPath);
            
            System.out.println("CSV数据筛选成功：" + result);
            return result;
        } catch (Exception e) {
            String errorMsg = "筛选CSV数据失败：" + e.getMessage();
            System.out.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
        }
    }
} 