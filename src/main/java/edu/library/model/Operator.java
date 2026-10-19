package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
@Table(name="user")
public class Operator extends User {
 private String area;
 public String getArea(){return area;}
 public void setArea(String value){this.area=value;}
 private String duty;
 public String getDuty(){return duty;}
 public void setDuty(String value){this.duty=value;}
 public String[] hasPermission(){return new String[]{"books","bookAdd","bookEdit","bookDiscard","copies","borrow","return","mine"};}
}
