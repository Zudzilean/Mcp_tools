package com.test;

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
@ToolMapping(description = "生成CSV文件，需提供文件保存路径filePath和CSV内容csvLines。" +
        "filePath：CSV文件保存路径，如 D:/output/data.csv。" +
        "csvLines：CSV内容，每行为一个用英文逗号分隔的字符串，如 '姓名,年龄,城市'。")
public class CsvGeneratorTool {
    /**
     * 生成CSV文件
     * @param filePath 参数1：CSV文件保存路径，必须为可写入的完整路径
     * @param csvLines 参数2：CSV内容，每行为一个字符串，内容用英文逗号分隔
     * @return 生成结果信息
     */
    public String generateCsv(String filePath, List<String> csvLines) {
        // 参数判定
        if (filePath == null || filePath.trim().isEmpty()) {
            return "参数错误：filePath不能为空";
        }
        if (csvLines == null || csvLines.isEmpty()) {
            return "参数错误：csvLines不能为空";
        }
        System.out.println("[调试] generateCsv 被调用，filePath: " + filePath + ", csvLines: " + csvLines);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF'); // 写入UTF-8 BOM
            for (String line : csvLines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            String msg = "CSV文件已成功生成：" + filePath;
            System.out.println("[调试] " + msg);
            return msg;
        } catch (IOException e) {
            String err = "生成CSV文件失败：" + e.getMessage();
            System.out.println("[调试] " + err);
            return err;
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