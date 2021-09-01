package com.avanwyk;

import com.avanwyk.Authentication.Admin;
import com.avanwyk.Authentication.ExternalSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

public sealed interface Principal permits User, Admin, ExternalSystem { // Sealed interface

  String name();
}

final class Authentication {

  static boolean authenticate(Principal p) {
    var name = p.name(); // local type inference
    System.out.printf("Authenticating principal %s%n", name);

    return switch (p) { // pattern matching switch
      case AnonymousUser u -> true;
      case RegisteredUser r -> checkCredentials(r); // no casting required
      case Admin a -> checkCredentials(a);
      case ExternalSystem s -> checkCredentials(s);
    }; // compiler knows cases are exhaustive due to sealed interface/class
  }

  static boolean isAuthenticatedAdmin(Principal p) {
    if (p instanceof Admin a) { // matches type pattern (Admin a), and casts with a local variable
      return checkCredentials(a);
    }
    return false;
  }

  record Admin(String name, String adminCredentials) implements Principal {

  } // Records are implicitly final (required to implement sealed interface)

  record ExternalSystem(String name, String systemKey, Admin owner) implements Principal {

  } // Implicitly implements name() from interface

  static ExternalSystem readJSON() throws JsonProcessingException {
    var objectMapper = new ObjectMapper();
    var json = """
        {
          "name": "ext",
          "systemKey": "1234568",
          "owner": {
            "name": "admin",
            "adminCredentials": "abcdef"
          }
        }
        """;
    return objectMapper.readValue(json, ExternalSystem.class); // Record support needs Jackson 2.12+
  }

  // Records work well as DTOs, and can be used for JSON interop.
  static String outputJSON(ExternalSystem externalSystem) throws JsonProcessingException {
    var objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(externalSystem);
  }

  private static boolean checkCredentials(AnonymousUser user) {
    System.out.println("Checking credentials for anonymous user " + user);
    return true;
  }

  private static boolean checkCredentials(RegisteredUser user) {
    System.out.println("Checking credentials for registered user " + user);
    return true;
  }

  private static boolean checkCredentials(Admin admin) {
    System.out.println("Checking credentials for admin " + admin);
    return true;
  }

  private static boolean checkCredentials(ExternalSystem system) {
    System.out.println("Checking credentials for system " + system);
    return true;
  }

  public static void main(String[] args) throws JsonProcessingException {
    boolean result = Authentication.authenticate(
        new ExternalSystem("ext", "`12345678", new Admin("admin", "abcdef")));
    System.out.println("Authenticated: " + result);

    final var externalSystem = readJSON();
    System.out.println("System: " + externalSystem.name());
    System.out.println("Owner: " + externalSystem.owner());
    System.out.println("System JSON: " + Authentication.outputJSON(externalSystem));
  }
}

abstract sealed class User implements Principal permits AnonymousUser, RegisteredUser {

  private final String username;

  protected User(String username) {
    this.username = username;
  }

  @Override
  public String name() {
    return username;
  }
}

final class AnonymousUser extends User {

  public static final String ANONYMOUS = "ANONYMOUS";

  AnonymousUser() {
    super(ANONYMOUS);
  }
}

final class RegisteredUser extends User {

  private final String password;

  RegisteredUser(String username, String password) {
    super(username);
    this.password = password;
  }

  public String password() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof RegisteredUser r) &&
        name().equals(r.name()) &&
        password().equals(r.password());
  }

  @Override
  public int hashCode() {
    return Objects.hash(password());
  }
}
