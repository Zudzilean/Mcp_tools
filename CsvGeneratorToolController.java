package com.test;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/mcp/sse3")
public class CsvGeneratorToolController {

    // 工具列表接口，供Dify发现
    @GetMapping("/tools")
    public List<Map<String, Object>> getTools() {
        System.out.println("[调试] /mcp/sse3/tools 被访问");
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", "CsvGeneratorTool");
        tool.put("description", "根据用户输入的文件路径和内容，生成一个CSV格式的表格文件并保存到指定路径。输入参数包括：文件保存路径（如 D:\\output\\data.csv），CSV内容（如每行用逗号分隔的字符串列表）。");
        List<Map<String, String>> params = new ArrayList<>();
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "filePath");
        param1.put("type", "string");
        param1.put("description", "文件保存路径");
        params.add(param1);
        Map<String, String> param2 = new HashMap<>();
        param2.put("name", "csvLines");
        param2.put("type", "array");
        param2.put("description", "每行CSV内容");
        params.add(param2);
        tool.put("parameters", params);
        List<Map<String, Object>> toolList = new ArrayList<>();
        toolList.add(tool);
        return toolList;
    }

    // 工具调用接口，供Dify调用
    @PostMapping("/CsvGeneratorTool")
    public Map<String, Object> callCsvGeneratorTool(@RequestBody Map<String, Object> request) {
        System.out.println("[调试] /mcp/sse3/CsvGeneratorTool 被调用，参数: " + request);
        String filePath = (String) request.get("filePath");
        Object csvLinesObj = request.get("csvLines");
        List<String> csvLines = new ArrayList<>();
        if (csvLinesObj instanceof List<?>) {
            for (Object o : (List<?>) csvLinesObj) {
                csvLines.add(String.valueOf(o));
            }
        } else if (csvLinesObj instanceof String) {
            // 兼容单字符串传入
            csvLines.add((String) csvLinesObj);
        }
        CsvGeneratorTool tool = new CsvGeneratorTool();
        String result = tool.generateCsv(filePath, csvLines);
        System.out.println("[调试] CsvGeneratorTool 生成结果: " + result);
        Map<String, Object> resp = new HashMap<>();
        resp.put("result", result);
        resp.put("filePath", filePath);
        return resp;
    }
} 