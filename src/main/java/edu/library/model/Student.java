package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
@Table(name="user")
public class Student extends User {
 private String grade;
 public String getGrade(){return grade;}
 public void setGrade(String value){this.grade=value;}
 private String college;
 public String getCollege(){return college;}
 public void setCollege(String value){this.college=value;}
 public String[] hasPermission(){return new String[]{"borrow","return","mine"};}
}
