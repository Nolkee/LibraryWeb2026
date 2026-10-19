package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
@Table(name="user")
public class Admin extends User {
 private Integer permissionLevel;
 public Integer getPermissionLevel(){return permissionLevel;}
 public void setPermissionLevel(Integer value){this.permissionLevel=value;}
 private LocalDate entryTime;
 public LocalDate getEntryTime(){return entryTime;}
 public void setEntryTime(LocalDate value){this.entryTime=value;}
 public String[] hasPermission(){return new String[]{"users","loans","stats","borrow","return","review"};}
}
