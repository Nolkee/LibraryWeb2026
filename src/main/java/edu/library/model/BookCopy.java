package edu.library.model;

import edu.homework.jdbc.annotation.*;
import java.time.*;
import java.util.*;

public class BookCopy extends Base {
  private String bookId;

  public String getBookId() {
    return bookId;
  }

  public void setBookId(String value) {
    this.bookId = value;
  }

  private transient Book book;

  public Book getBook() {
    return book;
  }

  public void setBook(Book value) {
    this.book = value;
  }

  private boolean isBorrowed;

  public boolean getIsBorrowed() {
    return isBorrowed;
  }

  public void setIsBorrowed(boolean value) {
    this.isBorrowed = value;
  }
}
