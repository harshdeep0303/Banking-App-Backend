package happy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class App {

        private static final String DB_URL = "jdbc:mysql://localhost:3306/banking_db";
        private static final String DB_USER = "root"; // your database username
        private static final String DB_PASSWORD = "Harshdeep@0303"; // your database password

        private Connection connection;

        public App() {
            try {
                // Establish a database connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Connected to database!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Securely hash the user's password
        public String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(password.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        // Register a new user in the database
        public void registerUser(String username, String password) {
            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashPassword(password));
                pstmt.executeUpdate();
                System.out.println("User registered successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Authenticate user login
        public int loginUser(String username, String password) {
            String sql = "SELECT user_id, password_hash FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (storedHash.equals(hashPassword(password))) {
                        System.out.println("Login successful!");
                        return rs.getInt("user_id");
                    } else {
                        System.out.println("Invalid password!");
                    }
                } else {
                    System.out.println("User not found!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1; // return -1 if login failed
        }

        // Create a bank account for the logged-in user
        public void createBankAccount(int userId, double initialBalance) {
            String sql = "INSERT INTO accounts (user_id, balance) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setDouble(2, initialBalance);
                pstmt.executeUpdate();
                System.out.println("Bank account created successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Get balance for the logged-in user's account
        public double getBalance(int userId) {
            String sql = "SELECT balance FROM accounts WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        // Deposit money into the user's account
        public void depositMoney(int userId, double amount) {
            String sql = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
                System.out.println("Deposited: $" + amount);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Withdraw money from the user's account
        public void withdrawMoney(int userId, double amount) {
            double currentBalance = getBalance(userId);
            if (currentBalance >= amount) {
                String sql = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                    System.out.println("Withdrew: $" + amount);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Insufficient funds!");
            }
        }

            public static void main(String[] args) {
                App app = new App();
                Scanner scanner = new Scanner(System.in);
            
                while (true) { // Loop to keep the application running
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit"); // Option to exit the application
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
            
                    int userId = -1;
                    if (choice == 1) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        app.registerUser(username, password);
                    } else if (choice == 2) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        userId = app.loginUser(username, password);
                    } else if (choice == 3) {
                        System.out.println("Exiting application.");
                        break; // Exit the loop
                    } else {
                        System.out.println("Invalid choice! Please try again.");
                        continue; // Continue to the next iteration of the loop
                    }
            
                    // If login was successful (userId is valid)
                    if (userId != -1) {
                        while (true) { // Loop for the user options after login
                            System.out.println("1. Create Account");
                            System.out.println("2. Deposit Money");
                            System.out.println("3. Withdraw Money");
                            System.out.println("4. Check Balance");
                            System.out.println("5. Logout"); // Option to log out
                            int action = scanner.nextInt();
            
                            if (action == 1) {
                                System.out.print("Enter initial balance: ");
                                double balance = scanner.nextDouble();
                                app.createBankAccount(userId, balance);
                            } else if (action == 2) {
                                System.out.print("Enter deposit amount: ");
                                double amount = scanner.nextDouble();
                                app.depositMoney(userId, amount);
                            } else if (action == 3) {
                                System.out.print("Enter withdrawal amount: ");
                                double amount = scanner.nextDouble();
                                app.withdrawMoney(userId, amount);
                            } else if (action == 4) {
                                System.out.println("Current Balance: $" + app.getBalance(userId));
                            } else if (action == 5) {
                                System.out.println("Logging out...");
                                break; // Exit the inner loop to return to the main menu
                            } else {
                                System.out.println("Invalid action! Please try again.");
                            }
                        }
                    }
                }
            
                scanner.close(); // Close the scanner
            }
            
                
        }