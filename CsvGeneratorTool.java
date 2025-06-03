package com.huawei;

import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;

// 假设McpServerEndpoint和ToolMapping注解已在项目中定义
@McpServerEndpoint(sseEndpoint = "8080")
@ToolMapping(description = "根据用户输入的文件路径和内容，生成一个CSV格式的表格文件并保存到指定路径。输入参数包括：文件保存路径（如 D:\\output\\data.csv），CSV内容（如每行用逗号分隔的字符串列表）。")
public class CsvGeneratorTool {
    /**
     * 生成CSV文件
     * @param filePath 文件保存路径，如 D:\\output\\data.csv
     * @param csvLines 每行CSV内容，已按逗号分隔
     * @return 生成结果信息
     */
    public String generateCsv(String filePath, List<String> csvLines) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF'); // 写入UTF-8 BOM
            for (String line : csvLines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            return "CSV文件已成功生成：" + filePath;
        } catch (IOException e) {
            return "生成CSV文件失败：" + e.getMessage();
        }
    }

    public static void main(String[] args) {
        CsvGeneratorTool tool = new CsvGeneratorTool();
        String filePath = "test.csv";
        java.util.List<String> csvLines = java.util.Arrays.asList(
            "姓名,年龄,城市",
            "张三,28,北京",
            "李四,30,上海",
            "王五,25,广州"
        );
        String result = tool.generateCsv(filePath, csvLines);
        System.out.println(result);
        // 读取文件内容验证中文
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            System.out.println("\n文件内容:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("读取文件失败: " + e.getMessage());
        }
    }
} 