import org.noear.solon.annotation.Inject;
import noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@McpServerEndpoint(sseEndpoint = "/mcp/CsvGeneratorTool/sse3")
public class CsvGeneratorTool {

    @Inject
    private final LocIOMTerminalDetailService terminalDetailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 生成CSV文件工具
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param outputPath CSV文件输出路径
     * @return CSV文件生成路径
     */
    public String generateCsv(
        @Param(name = "startDate", description = "开始日期 (格式: yyyy-MM-dd HH:mm:ss)") String startDate,
        @Param(name = "endDate", description = "结束日期 (格式: yyyy-MM-dd HH:mm:ss)") String endDate,
        @Param(name = "outputPath", description = "CSV文件保存路径") String outputPath
    ) throws Exception {
        try {
            log.info("开始生成CSV文件，参数：startDate={}, endDate={}, outputPath={}", startDate, endDate, outputPath);
            
            // 转换日期字符串为LocalDateTime对象
            LocalDateTime start = LocalDateTime.parse(startDate, DATE_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate, DATE_FORMATTER);
            
            // 调用Service导出CSV
            String result = terminalDetailService.exportToCsv(start, end, outputPath);
            
            log.info("CSV文件生成成功：{}", result);
            return result;
        } catch (Exception e) {
            log.error("生成CSV文件失败", e);
            throw new Exception("生成CSV文件失败: " + e.getMessage(), e);
        }
    }
} 