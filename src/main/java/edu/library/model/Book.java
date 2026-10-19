package edu.library.model;
import java.time.*;
import java.util.*;
import edu.homework.jdbc.annotation.*;
public class Book extends Base {
 private String title;
 public String getTitle(){return title;}
 public void setTitle(String value){this.title=value;}
 private String author;
 public String getAuthor(){return author;}
 public void setAuthor(String value){this.author=value;}
 private String type;
 public String getType(){return type;}
 public void setType(String value){this.type=value;}
 private String publisher;
 public String getPublisher(){return publisher;}
 public void setPublisher(String value){this.publisher=value;}
 private Integer days;
 public Integer getDays(){return days;}
 public void setDays(Integer value){this.days=value;}
 private Double price;
 public Double getPrice(){return price;}
 public void setPrice(Double value){this.price=value;}
 private Integer stock;
 public Integer getStock(){return stock;}
 public void setStock(Integer value){this.stock=value;}
 private String status;
 public String getStatus(){return status;}
 public void setStatus(String value){this.status=value;}
 private String introduction;
 public String getIntroduction(){return introduction;}
 public void setIntroduction(String value){this.introduction=value;}
 private transient List<BookCopy> bookCopies;
 public List<BookCopy> getBookCopies(){return bookCopies;}
 public void setBookCopies(List<BookCopy> value){this.bookCopies=value;}
}
