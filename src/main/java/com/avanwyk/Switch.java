package com.avanwyk;

public class Switch {

  public static String switchExpression(String day) {
    var dayType = switch (day) { // switch expressions yield values.
      case "MON", "TUE", "WED", "THUR", "FRI" -> { // Arrow means we need no break, it will only match this case.
        System.out.println("Checking Week Day");
        yield "Work day";  // if the case is a block, we can use yield to supply the value.
      }
      case "SAT", "SUN" -> "Weekend day"; // yield is not required for a single expression.
      default -> throw new IllegalArgumentException("Unknown day");
    };
    return dayType;
  }

  public static String switchPatterns(Object o) {
    return switch (o) {
      case Integer i -> "Integer type: " + i;
      case Boolean b -> "Boolean type: " + b;
      case String s -> "String type: " + s;
      default -> "Unknown type";
    };
  }

  public static void main(String[] args) {
    System.out.println(switchExpression("MON"));
    System.out.println(switchPatterns(1));
  }
}
