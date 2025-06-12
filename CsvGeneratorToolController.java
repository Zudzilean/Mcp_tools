package com.example.controller;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Inject;
import com.example.service.LocIOMTerminalDetailService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.text.SimpleDateFormat;

@Slf4j
@RequiredArgsConstructor
@Controller
@Mapping("/mcp/CsvGeneratorTool")
public class CsvGeneratorToolController {

    @Inject
    private final LocIOMTerminalDetailService terminalDetailService;

    // 工具列表接口，供Manus发现
    @Mapping("/tools")
    public List<Map<String, Object>> getTools() {
        log.info("工具列表接口被访问");
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", "CsvGeneratorTool");
        tool.put("description", "根据日期范围从数据库导出终端详情数据到CSV文件\n\n" +
            "参数说明:\n" +
            "  startDate - 开始日期，格式：yyyy-MM-dd\n" +
            "  endDate - 结束日期，格式：yyyy-MM-dd\n" +
            "  outputPath - CSV文件保存路径，示例：D:/output/terminal_data.csv");
        
        List<Map<String, String>> params = new ArrayList<>();
        
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "startDate");
        param1.put("type", "string");
        param1.put("description", "开始日期 (格式: yyyy-MM-dd)");
        params.add(param1);
        
        Map<String, String> param2 = new HashMap<>();
        param2.put("name", "endDate");
        param2.put("type", "string");
        param2.put("description", "结束日期 (格式: yyyy-MM-dd)");
        params.add(param2);
        
        Map<String, String> param3 = new HashMap<>();
        param3.put("name", "outputPath");
        param3.put("type", "string");
        param3.put("description", "CSV文件保存路径");
        params.add(param3);
        
        tool.put("parameters", params);
        List<Map<String, Object>> toolList = new ArrayList<>();
        toolList.add(tool);
        return toolList;
    }

    // 工具调用接口，供Manus调用
    @Mapping("/execute")
    public Map<String, Object> executeTool(@Body Map<String, Object> request) {
        log.info("工具执行接口被调用，参数: {}", request);
        
        String startDate = (String) request.get("startDate");
        String endDate = (String) request.get("endDate");
        String outputPath = (String) request.get("outputPath");
        
        try {
            String result = terminalDetailService.exportToCsv(
                new SimpleDateFormat("yyyy-MM-dd").parse(startDate),
                new SimpleDateFormat("yyyy-MM-dd").parse(endDate),
                outputPath
            );
            
            log.info("CSV文件生成成功：{}", result);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("result", result);
            resp.put("filePath", outputPath);
            return resp;
            
        } catch (Exception e) {
            log.error("生成CSV文件失败", e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return resp;
        }
    }
} 