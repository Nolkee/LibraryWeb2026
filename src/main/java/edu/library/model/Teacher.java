package edu.library.model;

import edu.homework.jdbc.annotation.*;
import java.time.*;
import java.util.*;

@Table(name = "user")
public class Teacher extends User {
  private String college;

  public String getCollege() {
    return college;
  }

  public void setCollege(String value) {
    this.college = value;
  }

  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String[] hasPermission() {
    return new String[] {"borrow", "return", "mine", "recommend"};
  }
}
