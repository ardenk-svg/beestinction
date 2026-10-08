// Main.java — a Java syntax refresher (Java 17+)
// Compile: javac Main.java    Run: java Main

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ---------- Interface (with a default method) ----------
interface Shape {
    double area();

    default String describe() {
        return getClass().getSimpleName() + " with area " + String.format("%.2f", area());
    }
}

// ---------- Records: concise immutable data classes ----------
record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
}

record Rectangle(double width, double height) implements Shape {
    public double area() { return width * height; }
}

// ---------- Abstract class, inheritance, static members ----------
abstract class Animal {
    private static int count = 0;      // shared by all instances
    protected final String name;       // set once, never reassigned

    protected Animal(String name) {
        this.name = name;
        count++;
    }

    public abstract String sound();    // subclasses must implement

    public static int getCount() { return count; }

    @Override
    public String toString() { return name + " says " + sound(); }
}

class Dog extends Animal {
    Dog(String name) { super(name); }

    @Override
    public String sound() { return "Woof"; }
}

class Cat extends Animal {
    Cat(String name) { super(name); }

    @Override
    public String sound() { return "Meow"; }
}

// ---------- Enum with a method ----------
enum Day {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    boolean isWeekend() { return this == SAT || this == SUN; }
}

// ---------- Generic class with a bounded type ----------
class Box<T extends Comparable<T>> {
    private final T value;

    Box(T value) { this.value = value; }

    T getValue() { return value; }

    boolean isBiggerThan(Box<T> other) {
        return value.compareTo(other.value) > 0;
    }
}

// ---------- Custom checked exception ----------
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) { super(message); }
}

class Account {
    private double balance;

    Account(double balance) { this.balance = balance; }

    void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException("Need " + (amount - balance) + " more");
        }
        balance -= amount;
    }

    double getBalance() { return balance; }
}

// ---------- Main class ----------
public class Main {

    // Static method with varargs
    static int sum(int... nums) {
        int total = 0;
        for (int n : nums) total += n;
        return total;
    }

    public static void main(String[] args) {

        // --- Variables and primitives ---
        int age = 30;
        double price = 19.99;
        boolean active = true;
        char grade = 'A';
        long big = 10_000_000_000L;
        final String greeting = "Hello";   // constant
        var inferred = 42;                 // type inference (still an int)
        System.out.println(greeting + ", world! " + age + " " + price + " " + active
                + " " + grade + " " + big + " " + inferred);

        // --- Strings ---
        String s = "Java Syntax";
        System.out.println(s.length() + " " + s.toUpperCase() + " " + s.substring(0, 4)
                + " " + s.contains("Syn") + " " + s.equals("java syntax"));
        String block = """
                Text blocks span
                multiple lines.
                """;
        System.out.print(block);

        // --- Arrays ---
        int[] numbers = {5, 3, 8, 1};
        int[][] grid = new int[2][3];
        grid[1][2] = 7;
        System.out.println(numbers.length + " " + numbers[0] + " " + grid[1][2]);

        // --- Control flow ---
        if (age >= 18) {
            System.out.println("Adult");
        } else if (age >= 13) {
            System.out.println("Teen");
        } else {
            System.out.println("Child");
        }

        String label = (age > 25) ? "over 25" : "25 or under";   // ternary
        System.out.println(label);

        for (int i = 0; i < 3; i++) System.out.print(i + " ");
        for (int n : numbers) System.out.print(n + " ");
        int w = 0;
        while (w < 2) { System.out.print("w" + w + " "); w++; }
        System.out.println();

        // Switch expression (arrow form returns a value)
        Day today = Day.SAT;
        String type = switch (today) {
            case SAT, SUN -> "weekend";
            default -> "weekday";
        };
        System.out.println(today + " is a " + type + " (" + today.isWeekend() + ")");

        // --- Methods ---
        System.out.println(sum() + " " + sum(1, 2, 3));

        // --- Classes and polymorphism ---
        List<Animal> animals = new ArrayList<>();
        animals.add(new Dog("Rex"));
        animals.add(new Cat("Tom"));
        for (Animal a : animals) System.out.println(a);
        System.out.println("Animals created: " + Animal.getCount());

        // --- Interfaces and records ---
        List<Shape> shapes = List.of(new Circle(1.5), new Rectangle(2, 3));
        for (Shape sh : shapes) {
            System.out.println(sh.describe());
            if (sh instanceof Circle c) {            // pattern matching
                System.out.println("  radius = " + c.radius());
            }
        }

        // --- Generics ---
        Box<Integer> a = new Box<>(10);
        Box<Integer> b = new Box<>(4);
        System.out.println("a > b? " + a.isBiggerThan(b));

        // --- Collections ---
        List<String> names = new ArrayList<>(List.of("Zoe", "Adam", "Maya", "Bob"));
        names.add("Liam");
        names.remove("Bob");
        names.sort(null);                            // natural order
        System.out.println(names);

        Map<String, Integer> ages = new HashMap<>();
        ages.put("Adam", 31);
        ages.put("Maya", 27);
        ages.putIfAbsent("Zoe", 22);
        for (Map.Entry<String, Integer> e : ages.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
        System.out.println(ages.getOrDefault("Nobody", -1));

        // --- Lambdas and streams ---
        List<String> shortUpper = names.stream()
                .filter(n -> n.length() <= 4)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println(shortUpper);

        int evenSum = java.util.stream.IntStream.rangeClosed(1, 10)
                .filter(n -> n % 2 == 0)
                .sum();
        System.out.println("Sum of evens 1-10: " + evenSum);

        Runnable r = () -> System.out.println("Running a lambda");
        r.run();

        // --- Exceptions ---
        Account acct = new Account(100);
        try {
            acct.withdraw(30);
            acct.withdraw(100);                      // throws
        } catch (InsufficientFundsException ex) {
            System.out.println("Error: " + ex.getMessage());
        } finally {
            System.out.println("Balance: " + acct.getBalance());
        }

        try {
            int[] tiny = new int[2];
            tiny[5] = 1;                             // unchecked exception
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Caught: " + ex.getMessage());
        }
    }
}