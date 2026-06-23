import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * LoginPanel.java
 * Builds and manages the login screen (username / password / user-type),
 * the "Forgot Password" dialog, and credential validation against the DB.
 *
 * File-path references have been replaced with the constant ASSETS_DIR.
 * DB credentials are sourced from DBConfig; no passwords are hard-coded here.
 */
public class LoginPanel {

    // ---------------------------------------------------------------
    // Asset root – change this once instead of hunting every ImageIcon
    // ---------------------------------------------------------------
    private static final String ASSETS_DIR = "/path/to/assets/"; // <-- update this path

    private JFrame frame;
    private JTextField  usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JButton showPasswordButton;
    private boolean isPasswordVisible = false;

    private List<String[]> users;

    /** Callback invoked after a successful login with the chosen role. */
    public interface LoginCallback {
        void onLoginSuccess(String userType);
    }

    private final LoginCallback callback;

    public LoginPanel(LoginCallback callback) {
        this.callback = callback;
        initializeUsers();
        createGUI();
    }

    // ------------------------------------------------------------------
    // User initialisation (loads from DB; falls back to defaults on error)
    // ------------------------------------------------------------------
    private void initializeUsers() {
        users = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                        DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD)) {

                // Admin credentials
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT UsernameID, Password FROM admin")) {
                    if (rs.next()) {
                        users.add(new String[]{"Admin",
                            rs.getString("UsernameID"),
                            rs.getString("Password"),
                            "Administrator"});
                    }
                }

                // Student credentials
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT UsernameID, Password FROM student")) {
                    int count = 1;
                    while (rs.next()) {
                        users.add(new String[]{"Student" + count++,
                            rs.getString("UsernameID"),
                            rs.getString("Password"),
                            "Student"});
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(LoginPanel.class.getName()).log(Level.SEVERE, null, ex);
            // Fallback – replace these with safe test values only
            users.add(new String[]{"Admin",    "admin",    "REDACTED", "Administrator"});
            users.add(new String[]{"Student1", "student1", "REDACTED", "Student"});
        }
    }

    // ------------------------------------------------------------------
    // GUI construction
    // ------------------------------------------------------------------
    private void createGUI() {
        frame = new JFrame("Tree Trackr. Homepage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(220, 240, 220));

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(220, 240, 220));
        mainPanel.setLayout(null);

        // Title
        JLabel titleLabel = new JLabel(
            "<html><center><b>TreeTrackr.</b></center></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(20, 10, 760, 50);
        mainPanel.add(titleLabel);

        // Logo
        ImageIcon logoIcon = new ImageIcon(ASSETS_DIR + "Rainforest Logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(180, 120, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setBounds(570, 40, 180, 120);
        mainPanel.add(logoLabel);

        // Info text
        JLabel infoLabel = new JLabel(
            "<html><div style='text-align:right;'>"
            + "<b>About the Nursery</b><br>"
            + "Our campus rainforest nursery hosts nearly a thousand endangered and "
            + "native rainforest species, enhancing local biodiversity of flora and fauna."
            + "</div></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoLabel.setBounds(500, 140, 250, 200);
        mainPanel.add(infoLabel);

        // Centre image
        ImageIcon imageIcon = new ImageIcon(ASSETS_DIR + "Rainforest Background Info.png");
        Image image = imageIcon.getImage().getScaledInstance(480, 250, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        imageLabel.setBounds(20, 60, 480, 250);
        mainPanel.add(imageLabel);

        // Login form
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(5, 2, 10, 10));
        loginPanel.setBackground(new Color(220, 240, 220));
        loginPanel.setBounds(500, 320, 280, 180);

        userTypeComboBox = new JComboBox<>(new String[]{"Student", "Administrator"});
        userTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        loginPanel.add(labelRight("User Type:"));
        loginPanel.add(userTypeComboBox);

        usernameField = new JTextField(10);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        loginPanel.add(labelRight("Username:"));
        loginPanel.add(usernameField);

        passwordField = new JPasswordField(10);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        loginPanel.add(labelRight("Password:"));

        // Password row with eye-toggle
        ImageIcon eyeIcon  = new ImageIcon(ASSETS_DIR + "Eye Icon.png");
        ImageIcon scaledEye = new ImageIcon(
            eyeIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        showPasswordButton = new JButton(scaledEye);
        showPasswordButton.setPreferredSize(new Dimension(30, 30));
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorderPainted(false);
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.addActionListener(e -> {
            isPasswordVisible = !isPasswordVisible;
            passwordField.setEchoChar(isPasswordVisible ? (char) 0 : '•');
        });

        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setBackground(new Color(220, 240, 220));
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(showPasswordButton, BorderLayout.EAST);
        loginPanel.add(passwordRow);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 16));
        loginButton.addActionListener(e -> handleLogin());
        loginPanel.add(new JLabel(""));
        loginPanel.add(loginButton);

        // Forgot password button
        JButton forgotButton = new JButton("Forgot Password");
        forgotButton.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotButton.addActionListener(e -> showForgotPasswordDialog());
        loginPanel.add(new JLabel(""));
        loginPanel.add(forgotButton);

        mainPanel.add(loginPanel);

        frame.add(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JLabel labelRight(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        return lbl;
    }

    // ------------------------------------------------------------------
    // Login validation
    // ------------------------------------------------------------------
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeComboBox.getSelectedItem();

        if (validateLogin(username, password, userType)) {
            showSuccess("Login successful!", "Success");
            frame.dispose();
            callback.onLoginSuccess(userType);
        } else {
            showError("Invalid username or password", "Login Error");
        }
    }

    private boolean validateLogin(String username, String password, String userType) {
        String table = "Administrator".equals(userType) ? "admin" : "student";
        String sql   = "SELECT * FROM " + table + " WHERE UsernameID = ? AND Password = ?";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // ------------------------------------------------------------------
    // Forgot-password dialog
    // ------------------------------------------------------------------
    private void showForgotPasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(0, 100, 0));

        addWhiteLabel(panel, "<html><b>Password Recovery</b></html>", 14, true);
        addWhiteLabel(panel,
            "<html>Please contact your teacher administrator to input their "
            + "credentials to retrieve your password.</html>", 12, false);
        panel.add(new JSeparator());

        JTextField studentUsernameField = new JTextField(10);
        JTextField adminUsernameField   = new JTextField(10);
        JPasswordField adminPassField   = new JPasswordField(10);

        addWhiteLabel(panel, "Student Username:", 12, false);
        panel.add(studentUsernameField);
        addWhiteLabel(panel, "Admin Username:", 12, false);
        panel.add(adminUsernameField);
        addWhiteLabel(panel, "Admin Password:", 12, false);
        panel.add(adminPassField);

        setOptionPaneColors(new Color(0, 100, 0));
        int result = JOptionPane.showConfirmDialog(null, panel,
            "Password Recovery", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        resetOptionPaneColors();

        if (result == JOptionPane.OK_OPTION) {
            String adminUser = adminUsernameField.getText();
            String adminPass = new String(adminPassField.getPassword());
            if (validateAdminCredentials(adminUser, adminPass)) {
                String pwd = retrieveStudentPassword(studentUsernameField.getText());
                if (pwd != null) {
                    showSuccess("The password for " + studentUsernameField.getText()
                        + " is: " + pwd, "Password Retrieved");
                } else {
                    showError("No student found with that username.", "Student Not Found");
                }
            } else {
                showError("Invalid admin credentials.", "Authentication Failed");
            }
        }
    }

    private boolean validateAdminCredentials(String username, String password) {
        String sql = "SELECT * FROM admin WHERE UsernameID = ? AND Password = ?";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    private String retrieveStudentPassword(String username) {
        String sql = "SELECT Password FROM student WHERE UsernameID = ?";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("Password");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private void addWhiteLabel(JPanel panel, String text, int size, boolean bold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl);
    }

    private void showSuccess(String msg, String title) {
        setOptionPaneColors(new Color(0, 100, 0));
        JOptionPane.showMessageDialog(frame,
            "<html><font color='white'>" + msg + "</font></html>",
            title, JOptionPane.INFORMATION_MESSAGE);
        resetOptionPaneColors();
    }

    private void showError(String msg, String title) {
        setOptionPaneColors(new Color(139, 0, 0));
        JOptionPane.showMessageDialog(frame,
            "<html><font color='white'>" + msg + "</font></html>",
            title, JOptionPane.ERROR_MESSAGE);
        resetOptionPaneColors();
    }

    private void setOptionPaneColors(Color bg) {
        UIManager.put("OptionPane.background",         bg);
        UIManager.put("Panel.background",              bg);
        UIManager.put("OptionPane.messageForeground",  Color.WHITE);
    }

    private void resetOptionPaneColors() {
        UIManager.put("OptionPane.background",         null);
        UIManager.put("Panel.background",              null);
        UIManager.put("OptionPane.messageForeground",  null);
    }
}
