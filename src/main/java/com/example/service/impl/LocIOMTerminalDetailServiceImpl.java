package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.LocIOMTerminalDetail;
import com.example.mapper.LocIOMTerminalDetailMapper;
import com.example.service.LocIOMTerminalDetailService;
import org.springframework.stereotype.Service;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Param;
import noear.solon.ai.mcp.server.annotation.ToolMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocIOMTerminalDetailServiceImpl extends ServiceImpl<LocIOMTerminalDetailMapper, LocIOMTerminalDetail> implements LocIOMTerminalDetailService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_QUOTE = "\"";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public List<LocIOMTerminalDetail> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return baseMapper.getByDateRange(startDate, endDate);
    }
    
    @Override
    public String exportToCsv(LocalDateTime startDate, LocalDateTime endDate, String outputPath) throws Exception {
        try {
            System.out.println("开始导出CSV文件，参数：startDate=" + startDate + ", endDate=" + endDate + ", outputPath=" + outputPath);
            
            // 获取数据
            List<LocIOMTerminalDetail> dataList = getByDateRange(startDate, endDate);
            
            // 写入CSV
            List<String> lines = new ArrayList<>();
            
            // 添加表头
            String header = String.join(CSV_DELIMITER, 
                "ID", "Region", "MaterialCode", "Description", "CreateTime", "UpdateTime");
            lines.add(header);
            
            // 添加数据行
            for (LocIOMTerminalDetail data : dataList) {
                String line = String.join(CSV_DELIMITER,
                    String.valueOf(data.getId()),
                    String.valueOf(data.getRegion()),
                    String.valueOf(data.getMaterialCode()),
                    CSV_QUOTE + data.getDescription() + CSV_QUOTE,
                    data.getCreateTime().format(DATE_FORMATTER),
                    data.getUpdateTime().format(DATE_FORMATTER)
                );
                lines.add(line);
            }
            
            // 写入文件
            Files.write(Paths.get(outputPath), lines, StandardCharsets.UTF_8);
            
            // 返回结果
            String result = String.format("导出完成：\n文件路径：%s\n数据统计：%d行 x 6列", 
                outputPath, dataList.size());
            
            System.out.println(result);
            return result;
        } catch (Exception e) {
            String errorMsg = "导出CSV文件失败：" + e.getMessage();
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
            "\n返回值：成功时返回CSV文件路径及数据统计信息（行数、列数），失败时返回错误信息")
    @Override
    public String filterCsvData(String sourceCsvPath, String conditions, String outputPath) throws Exception {
        try {
            System.out.println("开始筛选CSV数据，参数：sourceCsvPath=" + sourceCsvPath + ", conditions=" + conditions + ", outputPath=" + outputPath);
            
            // 解析条件
            Map<String, Object> conditionMap = objectMapper.readValue(conditions, Map.class);
            
            // 读取所有行
            List<String> lines = Files.readAllLines(Paths.get(sourceCsvPath), StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                throw new Exception("CSV文件为空");
            }

            // 解析表头
            String[] headers = parseCsvLine(lines.get(0));
            int columnCount = headers.length;
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }

            // 筛选数据
            List<String> filteredLines = lines.stream()
                .filter(line -> {
                    if (line.equals(lines.get(0))) return true; // 保留表头
                    
                    String[] values = parseCsvLine(line);
                    return conditionMap.entrySet().stream().allMatch(condition -> {
                        String field = condition.getKey();
                        Object value = condition.getValue();
                        
                        Integer fieldIndex = headerMap.get(field);
                        if (fieldIndex == null) {
                            System.out.println("警告：字段 " + field + " 不存在于CSV文件中");
                            return false;
                        }
                        
                        String fieldValue = values[fieldIndex].trim();
                        
                        if (value instanceof String) {
                            return fieldValue.contains((String) value);
                        } else if (value instanceof Number) {
                            try {
                                if (field.equals("region") ||
                                    field.equals("materialCode") || field.equals("MaterialCode")) {
                                    int recordValue = Integer.parseInt(fieldValue);
                                    int conditionValue = ((Number) value).intValue();
                                    return recordValue == conditionValue;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("警告：字段 " + field + " 的值 " + fieldValue + " 不是有效的数字");
                                return false;
                            }
                        } else if (value instanceof LocalDateTime) {
                            try {
                                LocalDateTime recordTime = LocalDateTime.parse(fieldValue, DATE_FORMATTER);
                                return recordTime.equals(value);
                            } catch (Exception e) {
                                System.out.println("警告：字段 " + field + " 的值 " + fieldValue + " 不是有效的日期时间");
                                return false;
                            }
                        }
                        return false;
                    });
                })
                .collect(Collectors.toList());

            // 写入结果
            Files.write(Paths.get(outputPath), filteredLines, StandardCharsets.UTF_8);
            
            // 返回结果
            int rowCount = filteredLines.size() - 1; // 减去表头
            String result = String.format("筛选完成：\n文件路径：%s\n数据统计：%d行 x %d列", 
                outputPath, rowCount, columnCount);
            
            System.out.println(result);
            return result;
        } catch (Exception e) {
            String errorMsg = "筛选CSV数据失败：" + e.getMessage();
            System.out.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
        }
    }
    
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == CSV_QUOTE.charAt(0)) {
                inQuotes = !inQuotes;
            } else if (c == CSV_DELIMITER.charAt(0) && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }
} 