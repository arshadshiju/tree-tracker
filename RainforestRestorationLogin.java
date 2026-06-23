import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;
import com.toedter.calendar.JDateChooser;

/**
 * RainforestRestorationLogin.java
 * Application entry point and shared-utility hub.
 *
 * Responsibilities:
 *   - main() bootstraps the login screen.
 *   - Shared DB helper methods (createTableModel, addPlant, etc.) are here so
 *     AdminPage, NurseryPage, and TableEditor can call them without coupling
 *     to each other.
 *
 * All DB credentials are read from DBConfig.
 * All file paths use the ASSETS_DIR constant below – update it once.
 */
public class RainforestRestorationLogin {

    // -----------------------------------------------------------------------
    // Asset root – update this once
    // -----------------------------------------------------------------------
    static final String ASSETS_DIR = "/path/to/assets/"; // <-- update this path

    // Fields kept for backward compatibility with inner-class references
    private DefaultTableModel plantTableModel;
    private String currentUserRole;

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RainforestRestorationLogin().createGUI());
    }

    // -----------------------------------------------------------------------
    // GUI entry: login screen
    // -----------------------------------------------------------------------
    public void createGUI() {
        new LoginPanel(userType -> {
            currentUserRole = userType;
            if ("Administrator".equals(userType)) showAdminPage();
            else                                   showNurseryPage();
        });
    }

    // -----------------------------------------------------------------------
    // Page launchers
    // -----------------------------------------------------------------------
    public void showAdminPage()   { new AdminPage(this);                  }
    public void showNurseryPage() { new NurseryPage(this, isAdministrator()); }

    public boolean isAdministrator() {
        return currentUserRole != null && currentUserRole.equals("Administrator");
    }

    // -----------------------------------------------------------------------
    // Grid editor launcher
    // -----------------------------------------------------------------------
    public void openGridEditor(String loc) {
        TableEditor editor;
        switch (loc) {
            case "A1": editor = new A1TableEditor(this); break;
            case "A2": editor = new A2TableEditor(this); break;
            default:   editor = new DefaultTableEditor(loc, this);
        }
        JFrame f = new JFrame("TreeTrackr. Table " + loc + " Editor");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(editor);
        f.pack();
        f.setVisible(true);
    }

    // -----------------------------------------------------------------------
    // Shared DB helpers
    // -----------------------------------------------------------------------

    /** Builds a read-only DefaultTableModel from PLANT_SPECIES_RECORD. */
    public DefaultTableModel createTableModel() {
        String[] cols = {"Record ID","Plant Species","Location","Total Quantity",
            "Soil Moisture","Sunlight Exposure","Status","Plant Stage",
            "Plant Type","Last Fertilised","Plant Height","Plant Width"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT * FROM PLANT_SPECIES_RECORD";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) model.addRow(new Object[]{
                rs.getInt("RecordID"),        rs.getString("PlantSpecies"),
                rs.getString("TableNameID"),  rs.getInt("Quantity"),
                rs.getString("SoilMoisture"), rs.getString("SunlightExposure"),
                rs.getString("PlantStatus"),  rs.getString("PlantStage"),
                rs.getString("PlantType"),    rs.getDate("LastFertilised"),
                rs.getFloat("PlantHeight"),   rs.getFloat("PlantWidth")});
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading plant data: " + e.getMessage());
        }
        return model;
    }

    /** Refreshes an existing plant table model in place. */
    public void refreshPlantTable(DefaultTableModel model, JFrame parent) {
        model.setRowCount(0);
        DefaultTableModel fresh = createTableModel();
        for (int i = 0; i < fresh.getRowCount(); i++)
            model.addRow(fresh.getDataVector().elementAt(i));
        model.fireTableDataChanged();

        setPane(new Color(0, 100, 0));
        JOptionPane.showMessageDialog(parent,
            "<html><font color='white'>Plant data refreshed!</font></html>",
            "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        resetPane();
    }

    /** Populates a combo box with distinct plant species from the DB. */
    public void populateSpeciesComboBox(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT DISTINCT PlantSpecies FROM PLANT_SPECIES_RECORD")) {
            while (rs.next()) combo.addItem(rs.getString("PlantSpecies"));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading species: " + ex.getMessage());
        }
    }

    /** Updates a JTable to show all records for the given species. */
    public void updateSpeciesTable(JTable table, String species) {
        String sql = "SELECT * FROM PLANT_SPECIES_RECORD WHERE PlantSpecies = ?";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, species);
            try (ResultSet rs = ps.executeQuery()) {
                table.setModel(buildTableModel(rs));
                int[] widths = {80,100,100,80,180,120,100,100,100,120,100,100};
                for (int i = 0; i < Math.min(widths.length, table.getColumnCount()); i++)
                    table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading species data: " + ex.getMessage());
        }
    }

    /** Converts a ResultSet into a DefaultTableModel. */
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= cols; i++) colNames.add(meta.getColumnName(i));
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
            data.add(row);
        }
        return new DefaultTableModel(data, colNames);
    }

    // -----------------------------------------------------------------------
    // Add-plant dialog
    // -----------------------------------------------------------------------
    public void showAddPlantDialogFromNurseryPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 100, 0));

        JTextField usernameIDField    = new JTextField(10);
        JComboBox<String> tableNameID = new JComboBox<>(new String[]{
            "A1","A2","A3","A4","B1","B2","B3","B4","C1","D1","E1","E2","E3","E4","F1","F2","F3","F4"});
        JTextField plantIndexField   = new JTextField(10);
        JTextField plantSpeciesField = new JTextField(10);
        JTextField quantityField     = new JTextField(10);
        JComboBox<String> soilMoist  = new JComboBox<>(new String[]{"Low","Medium","High"});
        JComboBox<String> sunlight   = new JComboBox<>(new String[]{"Full Sun","Partial Shade","Full Shade"});
        JComboBox<String> status     = new JComboBox<>(new String[]{"Excellent","Good","Fair","Poor","Dead"});
        JComboBox<String> stage      = new JComboBox<>(new String[]{"Sprout","Seedling","Vegetative","Budding","Flowering","Ripening","Senescence"});
        JComboBox<String> type       = new JComboBox<>(new String[]{"Ornamental Flower","Fruit","Leaf","Stem","Root"});
        JDateChooser lastFert        = new JDateChooser(); lastFert.setDate(new Date());
        JTextField widthField        = new JTextField(10);
        JTextField heightField       = new JTextField(10);

        String[] labels = {"UsernameID:","TableNameID:","PlantIndex:","PlantSpecies:",
            "Quantity:","SoilMoisture:","SunlightExposure:","PlantStatus:","PlantStage:",
            "PlantType:","LastFertilised:","PlantWidth:","PlantHeight:"};
        JComponent[] comps = {usernameIDField, tableNameID, plantIndexField, plantSpeciesField,
            quantityField, soilMoist, sunlight, status, stage, type, lastFert, widthField, heightField};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,5,2,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]); l.setForeground(Color.WHITE);
            panel.add(l, gbc); panel.add(comps[i], gbc);
        }

        setPane(new Color(0, 100, 0));
        int r = JOptionPane.showConfirmDialog(null, panel, "Add New Plant",
            JOptionPane.OK_CANCEL_OPTION);
        resetPane();

        if (r == JOptionPane.OK_OPTION) {
            try {
                addPlantToDatabase(
                    usernameIDField.getText(),
                    (String) tableNameID.getSelectedItem(),
                    plantIndexField.getText(),
                    plantSpeciesField.getText(),
                    Integer.parseInt(quantityField.getText()),
                    (String) soilMoist.getSelectedItem(),
                    (String) sunlight.getSelectedItem(),
                    (String) status.getSelectedItem(),
                    (String) stage.getSelectedItem(),
                    (String) type.getSelectedItem(),
                    lastFert.getDate(),
                    Float.parseFloat(widthField.getText()),
                    Float.parseFloat(heightField.getText())
                );
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,
                    "Please enter valid numbers for quantity, height, and width.");
            }
        }
    }

    private void addPlantToDatabase(
            String uid, String tableNameID, String idx, String species,
            int qty, String soil, String sun, String plantStatus,
            String plantStage, String plantType, Date lastFert,
            float width, float height) {
        String sql = "INSERT INTO PLANT_SPECIES_RECORD "
            + "(UsernameID,TableNameID,PlantIndex,PlantSpecies,Quantity,"
            + "SoilMoisture,SunlightExposure,PlantStatus,PlantStage,PlantType,"
            + "LastFertilised,PlantWidth,PlantHeight) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, uid);  ps.setString(2, tableNameID); ps.setString(3, idx);
            ps.setString(4, species); ps.setInt(5, qty);
            ps.setString(6, soil); ps.setString(7, sun); ps.setString(8, plantStatus);
            ps.setString(9, plantStage); ps.setString(10, plantType);
            ps.setTimestamp(11, lastFert != null ? new java.sql.Timestamp(lastFert.getTime()) : null);
            ps.setFloat(12, width); ps.setFloat(13, height);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    setPane(new Color(0, 100, 0));
                    JOptionPane.showMessageDialog(null,
                        "<html><font color='white'>Plant added. Record ID: "
                        + keys.getInt(1) + "</font></html>",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    resetPane();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "DB error: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Add-student dialog
    // -----------------------------------------------------------------------
    public void showAddUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 100, 0));

        JTextField  unField  = new JTextField(10);
        JTextField  fnField  = new JTextField(10);
        JTextField  lnField  = new JTextField(10);
        JTextField  emField  = new JTextField(10);
        JPasswordField pwField = new JPasswordField(10);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,5,2,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        for (String[] pair : new String[][]{
                {"Username:", ""}, {"First Name:", ""}, {"Last Name:", ""},
                {"Email:", ""}, {"Password:", ""}}) {
            JLabel l = new JLabel(pair[0]); l.setForeground(Color.WHITE); panel.add(l, gbc);
        }
        // Re-add fields in order (re-use same gbc)
        panel.removeAll();
        addRow(panel, gbc, "Username:",   unField);
        addRow(panel, gbc, "First Name:", fnField);
        addRow(panel, gbc, "Last Name:",  lnField);
        addRow(panel, gbc, "Email:",      emField);
        addRow(panel, gbc, "Password:",   pwField);

        setPane(new Color(0, 100, 0));
        int r = JOptionPane.showConfirmDialog(null, panel, "Add New Student",
            JOptionPane.OK_CANCEL_OPTION);
        resetPane();

        if (r == JOptionPane.OK_OPTION) {
            String sql = "INSERT INTO student (UsernameID,FirstName,LastName,Email,Password)"
                       + " VALUES (?,?,?,?,?)";
            try (Connection conn = DriverManager.getConnection(
                        DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, unField.getText());
                ps.setString(2, fnField.getText());
                ps.setString(3, lnField.getText());
                ps.setString(4, emField.getText());
                ps.setString(5, new String(pwField.getPassword()));
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error adding student: " + ex.getMessage());
            }
        }
    }

    private void addRow(JPanel p, GridBagConstraints gbc, String labelText, JComponent field) {
        JLabel l = new JLabel(labelText); l.setForeground(Color.WHITE);
        p.add(l, gbc); p.add(field, gbc);
    }

    // -----------------------------------------------------------------------
    // Graph panel (login page decorative bar chart)
    // -----------------------------------------------------------------------
    public JPanel createGraphPanel() {
        JPanel panel = new JPanel() {
            private String selectedSpecies = null;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        selectedSpecies = getClickedSpecies(e.getX(), e.getY(), getWidth());
                        repaint();
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g, selectedSpecies, getWidth());
            }
        };
        panel.setBackground(new Color(240, 255, 240));
        return panel;
    }

    private void drawGraph(Graphics2D g, String selected, int width) {
        Map<String, PlantInfo> data = getPlantData();
        int maxVal  = data.values().stream().mapToInt(PlantInfo::getQuantity).max().orElse(1);
        int height  = 150;
        int yOffset = 10;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK); g.setStroke(new BasicStroke(2));
        g.drawLine(40, 25 + yOffset, 40, height - 40 + yOffset);
        g.drawLine(40, height - 40 + yOffset, width - 10, height - 40 + yOffset);

        List<Map.Entry<String, PlantInfo>> sorted = new ArrayList<>(data.entrySet());
        sorted.sort((a, b) -> b.getValue().getQuantity().compareTo(a.getValue().getQuantity()));
        sorted = sorted.subList(0, Math.min(13, sorted.size()));

        int barW = Math.min(30, (width - 50) / Math.max(1, sorted.size()));
        int x = 45;
        g.setFont(new Font("Arial", Font.PLAIN, 8));

        for (Map.Entry<String, PlantInfo> e : sorted) {
            int bh = (int)((e.getValue().getQuantity() * (height - 70)) / (double) maxVal);
            g.setColor(new Color(34, 139, 34));
            g.fillRect(x, height - 40 - bh + yOffset, barW - 2, bh);
            g.setColor(Color.BLACK);
            g.drawRect(x, height - 40 - bh + yOffset, barW - 2, bh);
            g.drawString(String.valueOf(e.getValue().getQuantity()), x + barW/2 - 5, height - 45 - bh + yOffset);
            g.setFont(new Font("Arial", Font.ITALIC, 6));
            g.setColor(new Color(101, 67, 33));
            g.drawString("Click", x + barW/2 - 8, height - 30 + yOffset);
            x += barW + 2;
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("A Snapshot of the Rainforest Nursery Records", width/2 - 110, 25);

        if (selected != null) {
            PlantInfo info = data.get(selected);
            if (info != null) {
                g.setColor(new Color(6, 64, 43));
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString(selected + " (ID: " + info.getRecordIDs() + ")", 45, height + yOffset - 8);
            }
        }
    }

    private String getClickedSpecies(int mx, int my, int width) {
        Map<String, PlantInfo> data = getPlantData();
        List<Map.Entry<String, PlantInfo>> sorted = new ArrayList<>(data.entrySet());
        sorted.sort((a,b) -> b.getValue().getQuantity().compareTo(a.getValue().getQuantity()));
        sorted = sorted.subList(0, Math.min(13, sorted.size()));
        int barW = Math.min(30, (width - 50) / Math.max(1, sorted.size()));
        int x = 45;
        for (Map.Entry<String, PlantInfo> e : sorted) {
            if (mx >= x && mx < x + barW - 2 && my >= 110) return e.getKey();
            x += barW + 2;
        }
        return null;
    }

    private Map<String, PlantInfo> getPlantData() {
        Map<String, PlantInfo> data = new HashMap<>();
        String sql = "SELECT PlantSpecies, SUM(Quantity) AS TotalQuantity, "
            + "GROUP_CONCAT(DISTINCT TableNameID) AS Locations, "
            + "GROUP_CONCAT(DISTINCT RecordID) AS RecordIDs "
            + "FROM PLANT_SPECIES_RECORD GROUP BY PlantSpecies";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) data.put(rs.getString("PlantSpecies"),
                new PlantInfo(rs.getInt("TotalQuantity"),
                    rs.getString("Locations"), rs.getString("RecordIDs")));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return data;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private void setPane(Color bg) {
        UIManager.put("OptionPane.background",        bg);
        UIManager.put("Panel.background",             bg);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
    }
    private void resetPane() {
        UIManager.put("OptionPane.background",        null);
        UIManager.put("Panel.background",             null);
        UIManager.put("OptionPane.messageForeground", null);
    }
}
