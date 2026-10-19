package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
public class Base {
 @Id
 private String id;
 public String getId(){return id;}
 public void setId(String value){this.id=value;}
 private LocalDateTime createTime;
 public LocalDateTime getCreateTime(){return createTime;}
 public void setCreateTime(LocalDateTime value){this.createTime=value;}
 private LocalDateTime updateTime;
 public LocalDateTime getUpdateTime(){return updateTime;}
 public void setUpdateTime(LocalDateTime value){this.updateTime=value;}
}
