package edu.library;

import static org.junit.jupiter.api.Assertions.*;

import edu.library.model.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidationTest {
  @Test
  void passwordHashIsSaltedAndSupportsBackupAccounts() {
    String a = ApiServlet.hash("secret123"), b = ApiServlet.hash("secret123");
    assertNotEquals(a, b);
    assertTrue(ApiServlet.matches("secret123", a));
    assertFalse(ApiServlet.matches("wrong", a));
    assertTrue(ApiServlet.matches("admin123", "admin123"));
  }

  @Test
  void rejectsFractionalStockAndInvalidPrice() {
    assertThrows(
        ApiServlet.Problem.class,
        () -> ApiServlet.integer(Map.of("stock", "1.5"), "stock", 1, 1000));
    assertThrows(
        ApiServlet.Problem.class, () -> ApiServlet.integer(Map.of("stock", "0"), "stock", 1, 1000));
    assertThrows(ApiServlet.Problem.class, () -> ApiServlet.price(Map.of("price", "NaN")));
    assertEquals(3, ApiServlet.integer(Map.of("stock", 3.0), "stock", 1, 1000));
  }

  @Test
  void rolesHaveSpecifiedCapabilities() {
    assertArrayEquals(new String[] {"borrow", "return", "mine"}, new Student().hasPermission());
    assertTrue(java.util.List.of(new Operator().hasPermission()).contains("copies"));
    assertFalse(java.util.List.of(new Teacher().hasPermission()).contains("review"));
    assertTrue(java.util.List.of(new Admin().hasPermission()).contains("review"));
  }
}
