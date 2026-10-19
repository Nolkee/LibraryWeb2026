package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
public abstract class User extends Base {
 private String name;
 public String getName(){return name;}
 public void setName(String value){this.name=value;}
 private String password;
 public String getPassword(){return password;}
 public void setPassword(String value){this.password=value;}
 private String phone;
 public String getPhone(){return phone;}
 public void setPhone(String value){this.phone=value;}
 private String role;
 public String getRole(){return role;}
 public void setRole(String value){this.role=value;}
 private String major;
 public String getMajor(){return major;}
 public void setMajor(String value){this.major=value;}
 private LocalDateTime loginTime;
 public LocalDateTime getLoginTime(){return loginTime;}
 public void setLoginTime(LocalDateTime value){this.loginTime=value;}
 public abstract String[] hasPermission();
}
