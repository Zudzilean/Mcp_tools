import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import com.deepexi.mcp.extension.annotation.McpServerEndpoint;
import com.deepexi.mcp.extension.annotation.ToolMapping;
import com.deepexi.mcp.extension.annotation.Param;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LiteflowComponent("csvGenerator")
@McpServerEndpoint(sseEndpoint = "/mcp/sse3")
public class CsvGeneratorTool extends NodeComponent {

    // 数据库连接配置
    private static final String JDBC_URL = "jdbc:oracle:thin:@//{host}:1521/{sid}";
    private static final String IM_HOST = "im-db-host";
    private static final String TBSC_HOST = "tbsc-db-host";
    private static final String DEFAULT_SID = "ORCL";

    /**
     * 生成CSV文件工具
     * 
     * @param dbName     数据库名称 (im 或 tbsc)
     * @param tableName  目标表名
     * @param userQuery  用户查询描述
     * @param outputPath CSV文件输出路径
     * @return CSV文件生成路径
     */
    @ToolMapping(
        description = "根据用户描述从Oracle数据库提取数据生成CSV文件\n\n" +
        "参数说明:\n" +
        "  dbName - 数据库名称，可选值: 'im' 或 'tbsc'\n" +
        "  tableName - 需要提取数据的表名\n" +
        "  userQuery - 用户自然语言描述，示例: '提取最近三个月的客户订单数据，包含订单号、客户名称和订单金额'\n" +
        "  outputPath - CSV文件保存路径，示例: 'D:/output/orders.csv'\n\n" +
        "返回:\n" +
        "  成功生成的CSV文件绝对路径"
    )
    public String generateCsv(
        @Param(name = "dbName", description = "目标数据库名称 (im/tbsc)") String dbName,
        @Param(name = "tableName", description = "需要提取数据的表名") String tableName,
        @Param(name = "userQuery", description = "用户自然语言查询描述") String userQuery,
        @Param(name = "outputPath", description = "CSV文件保存路径") String outputPath
    ) throws Exception {
        
        // 1. 获取数据库连接
        Connection conn = getConnection(dbName);
        
        // 2. 解析表结构
        TableMetaData metaData = extractTableMetaData(conn, tableName);
        
        // 3. 解析用户查询需求
        QueryParams queryParams = parseUserQuery(userQuery, metaData);
        
        // 4. 构建并执行SQL查询
        String sql = buildSelectSql(tableName, queryParams);
        List<String[]> results = executeQuery(conn, sql);
        
        // 5. 生成CSV文件
        generateCsvFile(outputPath, queryParams.selectedColumns, results);
        
        // 6. 清理资源
        conn.close();
        
        return Paths.get(outputPath).toAbsolutePath().toString();
    }

    private Connection getConnection(String dbName) throws SQLException {
        String host = dbName.equalsIgnoreCase("im") ? IM_HOST : TBSC_HOST;
        String url = JDBC_URL.replace("{host}", host).replace("{sid}", DEFAULT_SID);
        return DriverManager.getConnection(url, dbName, dbName);
    }

    private TableMetaData extractTableMetaData(Connection conn, String tableName) throws SQLException {
        TableMetaData metaData = new TableMetaData();
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                ColumnMeta col = new ColumnMeta(
                    rs.getString("COLUMN_NAME"),
                    rs.getString("TYPE_NAME"),
                    rs.getString("REMARKS")
                );
                metaData.addColumn(col);
            }
        }
        return metaData;
    }

    private QueryParams parseUserQuery(String userQuery, TableMetaData metaData) {
        QueryParams params = new QueryParams();
        
        // 1. 解析请求字段
        for (ColumnMeta column : metaData.columns) {
            // 规则1: 检查列名是否出现在用户描述中
            if (userQuery.toLowerCase().contains(column.name.toLowerCase())) {
                params.addSelectedColumn(column.name);
            }
            // 规则2: 检查列注释是否包含用户描述关键词
            else if (column.comment != null && containsAnyKeyword(userQuery, column.comment)) {
                params.addSelectedColumn(column.name);
            }
        }
        
        // 2. 解析时间范围条件 (示例)
        Pattern pattern = Pattern.compile("(最近|过去|近)(\\d+)(天|周|月|年)");
        Matcher matcher = pattern.matcher(userQuery);
        if (matcher.find()) {
            int num = Integer.parseInt(matcher.group(2));
            String unit = matcher.group(3);
            params.timeRange = parseTimeRange(num, unit);
        }
        
        return params;
    }

    private String buildSelectSql(String tableName, QueryParams params) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 添加选择的列
        if (params.selectedColumns.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", params.selectedColumns));
        }
        
        sql.append(" FROM ").append(tableName);
        
        // 添加时间条件
        if (params.timeRange != null) {
            sql.append(" WHERE create_time BETWEEN '")
               .append(params.timeRange.start)
               .append("' AND '")
               .append(params.timeRange.end)
               .append("'");
        }
        
        return sql.toString();
    }

    private List<String[]> executeQuery(Connection conn, String sql) throws SQLException {
        List<String[]> results = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            
            // 添加标题行
            String[] headers = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                headers[i-1] = rsmd.getColumnName(i);
            }
            results.add(headers);
            
            // 添加数据行
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 1; i <= colCount; i++) {
                    row[i-1] = rs.getString(i);
                }
                results.add(row);
            }
        }
        return results;
    }

    private void generateCsvFile(String path, List<String> headers, List<String[]> data) throws Exception {
        try (FileWriter writer = new FileWriter(path)) {
            // 写入标题行
            writer.write(String.join(",", headers) + "\n");
            
            // 写入数据行
            for (String[] row : data) {
                writer.write(String.join(",", row) + "\n");
            }
        }
    }

    //===== 辅助类 =====//
    private static class TableMetaData {
        List<ColumnMeta> columns = new ArrayList<>();
        
        void addColumn(ColumnMeta column) {
            columns.add(column);
        }
    }
    
    private static class ColumnMeta {
        String name;
        String type;
        String comment;
        
        ColumnMeta(String name, String type, String comment) {
            this.name = name;
            this.type = type;
            this.comment = comment;
        }
    }
    
    private static class QueryParams {
        List<String> selectedColumns = new ArrayList<>();
        TimeRange timeRange;
        
        void addSelectedColumn(String colName) {
            if (!selectedColumns.contains(colName)) {
                selectedColumns.add(colName);
            }
        }
    }
    
    private static class TimeRange {
        String start;
        String end;
        
        TimeRange(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }
    
    //===== 工具方法 =====//
    private boolean containsAnyKeyword(String text, String keywords) {
        for (String word : keywords.split("[,，]")) {
            if (text.contains(word.trim())) return true;
        }
        return false;
    }
    
    private TimeRange parseTimeRange(int num, String unit) {
        // 简化实现，实际应使用日期计算
        String end = "2023-12-31";
        String start;
        if ("月".equals(unit)) {
            start = "2023-" + (12 - num) + "-01";
        } else if ("天".equals(unit)) {
            start = "2023-12-" + (31 - num);
        } else {
            start = "2023-01-01";
        }
        return new TimeRange(start, end);
    }
}