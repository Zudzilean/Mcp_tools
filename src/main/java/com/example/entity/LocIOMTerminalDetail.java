package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("Loc_IOM_terminal_detail")
public class LocIOMTerminalDetail {
    
    @TableField("REGION")
    private String region;
    
    @TableField("REGIONCODE")
    private String regionCode;
    
    @TableField("COUNTRY")
    private String country;
    
    @TableField("IMEI")
    private String imei;
    
    @TableField("MATERIALCODE")
    private String materialCode;
    
    @TableField("TERMINALNAME")
    private String terminalName;
    
    @TableField("TERMINALKINCODE")
    private String terminalKinCode;
    
    @TableField("TERMINAL TYPE")
    private String terminalType;
    
    @TableField("CATEGORY")
    private String category;
    
    @TableField("STAUS")
    private String status;
    
    @TableField("STATUSDATE")
    private LocalDateTime statusDate;
    
    @TableField("IOMINTIME")
    private LocalDateTime iomInTime;
    
    @TableField("FIXTIME")
    private LocalDateTime fixTime;
    
    @TableField("USETYPE")
    private String useType;
    
    @TableField("USERNAME")
    private String userName;
    
    @TableField("USEDEPARTMENT")
    private String useDepartment;
    
    @TableField("BOSSINTIOME")
    private LocalDateTime bossInTime;
    
    @TableField("FILENAME")
    private String fileName;
} 