/**
 * DBConfig.java
 * Centralised database configuration.
 * IMPORTANT: Replace the placeholder values below with your actual credentials
 * before running. Do NOT commit real credentials to version control.
 */
public class DBConfig {
    public static final String DB_URL =
        "jdbc:mysql://localhost:3306/rainforest?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String USER     = "root";
    public static final String PASSWORD = "REDACTED"; // <-- replace with actual password
}
