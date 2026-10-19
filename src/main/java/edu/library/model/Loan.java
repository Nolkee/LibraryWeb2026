package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
public class Loan extends Base {
 private String bookCopyId;
 public String getBookCopyId(){return bookCopyId;}
 public void setBookCopyId(String value){this.bookCopyId=value;}
 private String userId;
 public String getUserId(){return userId;}
 public void setUserId(String value){this.userId=value;}
 private transient BookCopy bookCopy;
 public BookCopy getBookCopy(){return bookCopy;}
 public void setBookCopy(BookCopy value){this.bookCopy=value;}
 private transient User user;
 public User getUser(){return user;}
 public void setUser(User value){this.user=value;}
 private LocalDateTime borrowDate;
 public LocalDateTime getBorrowDate(){return borrowDate;}
 public void setBorrowDate(LocalDateTime value){this.borrowDate=value;}
 private LocalDateTime returnDate;
 public LocalDateTime getReturnDate(){return returnDate;}
 public void setReturnDate(LocalDateTime value){this.returnDate=value;}
 private LocalDateTime dueDate;
 public LocalDateTime getDueDate(){return dueDate;}
 public void setDueDate(LocalDateTime value){this.dueDate=value;}
 private String status;
 public String getStatus(){return status;}
 public void setStatus(String value){this.status=value;}
}
