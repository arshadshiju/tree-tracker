import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * TableEditor.java
 * Abstract base panel for grid-cell editors (A1, A2, … F4).
 * Concrete subclasses (A1TableEditor, A2TableEditor, DefaultTableEditor)
 * live below – all in a single file for convenience, but can be split if desired.
 *
 * File-path references have been replaced with ASSETS_DIR.
 * DB credentials are read from DBConfig; no passwords are hard-coded here.
 */
public abstract class TableEditor extends JPanel {

    private static final String ASSETS_DIR = "/path/to/assets/"; // <-- update this path

    protected final String gridLocation;
    protected DefaultTableModel tableModel;
    protected JTable table;
    private final RainforestRestorationLogin mainApp;

    public TableEditor(String gridLocation, RainforestRestorationLogin mainApp) {
        this.gridLocation = gridLocation;
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    // ------------------------------------------------------------------
    // Component setup
    // ------------------------------------------------------------------
    protected void initializeComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 100, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setOpaque(false);

        JButton notesButton = styledButton("Notes", Color.WHITE, new Color(0, 100, 0));
        notesButton.setFont(notesButton.getFont().deriveFont(Font.BOLD));
        notesButton.addActionListener(e -> showNotesDialog());
        leftPanel.add(notesButton);

        JLabel titleLabel = new JLabel("TreeTrackr. Table " + gridLocation + " Editor");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 19));
        leftPanel.add(titleLabel);

        ImageIcon logoIcon  = new ImageIcon(ASSETS_DIR + "Rainforest Logo.png");
        Image     logoImage = logoIcon.getImage().getScaledInstance(60, 40, Image.SCALE_SMOOTH);
        JLabel    logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(logoLabel,  BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = createTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {80, 150, 100, 100, 120, 120, 80, 100, 100, 100, 80, 80};
        for (int i = 0; i < Math.min(colWidths.length, table.getColumnCount()); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton addBtn    = styledButton("Add Plant");
        JButton editBtn   = styledButton("Edit Plant");
        JButton deleteBtn = styledButton("Delete Plant");
        addBtn   .addActionListener(e -> addPlant());
        editBtn  .addActionListener(e -> editPlant());
        deleteBtn.addActionListener(e -> deletePlant());
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        add(actionPanel, BorderLayout.SOUTH);
    }

    /** Subclasses supply the query-specific table model. */
    protected abstract DefaultTableModel createTableModel();

    // ------------------------------------------------------------------
    // CRUD actions
    // ------------------------------------------------------------------
    protected void addPlant()    { mainApp.showAddPlantDialogFromNurseryPage(); }

    protected void editPlant() {
        int row = table.getSelectedRow();
        if (row == -1) { showWarn("Please select a plant to edit."); return; }

        String[] cols   = new String[table.getColumnCount()];
        Object[] values = new Object[table.getColumnCount()];
        for (int i = 0; i < cols.length; i++) {
            cols[i]   = table.getColumnName(i);
            values[i] = table.getValueAt(row, i);
        }
        showPlantEditDialog(cols, values);
    }

    protected void deletePlant() {
        int row = table.getSelectedRow();
        if (row == -1) { showWarn("Please select a plant to delete."); return; }

        setPane(new Color(139, 0, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><font color='white'>Are you sure you want to delete this plant?</font></html>",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        resetPane();
        if (confirm == JOptionPane.YES_OPTION) tableModel.removeRow(row);
    }

    // ------------------------------------------------------------------
    // Edit dialog
    // ------------------------------------------------------------------
    protected void showPlantEditDialog(String[] columnNames, Object[] values) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Edit Plant", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 100, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField[] fields = new JTextField[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            JLabel lbl = new JLabel(columnNames[i]);
            lbl.setForeground(Color.WHITE);
            panel.add(lbl, gbc);
            gbc.gridx = 1;
            fields[i] = new JTextField(values[i].toString(), 20);
            panel.add(fields[i], gbc);
            gbc.gridx = 0; gbc.gridy++;
        }

        JScrollPane sp = new JScrollPane(panel);
        sp.setPreferredSize(new Dimension(400, 300));
        sp.getViewport().setBackground(new Color(220, 240, 220));
        dialog.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(0, 100, 0));
        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        btnPanel.add(ok); btnPanel.add(cancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            if (updateDatabase(columnNames, fields)) {
                refreshTable(); dialog.dispose();
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateDatabase(String[] columnNames, JTextField[] fields) {
        StringBuilder sql = new StringBuilder("UPDATE PLANT_SPECIES_RECORD SET ");
        for (int i = 1; i < columnNames.length; i++) {
            sql.append(columnNames[i]).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE ").append(columnNames[0]).append(" = ?");

        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 1; i < fields.length; i++) ps.setString(i, fields[i].getText());
            ps.setString(fields.length, fields[0].getText());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Record updated successfully.");
                return true;
            }
            JOptionPane.showMessageDialog(this, "No records were updated.");
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
            return false;
        }
    }

    protected void refreshTable() {
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM PLANT_SPECIES_RECORD WHERE TableNameID = ?")) {
            ps.setString(1, gridLocation);
            try (ResultSet rs = ps.executeQuery()) {
                DefaultTableModel m = (DefaultTableModel) table.getModel();
                m.setRowCount(0);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 1; i <= cols; i++) row[i - 1] = rs.getObject(i);
                    m.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Refresh error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Notes dialog
    // ------------------------------------------------------------------
    protected void showNotesDialog() {
        String current = fetchNotes();
        JTextArea area = new JTextArea(current, 10, 30);
        area.setWrapStyleWord(true); area.setLineWrap(true);
        area.setBackground(Color.WHITE); area.setForeground(Color.BLACK);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 100, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("Notes for " + gridLocation);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        setPane(new Color(0, 100, 0));
        int r = JOptionPane.showConfirmDialog(this, panel, "Notes",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        resetPane();
        if (r == JOptionPane.OK_OPTION) updateNotes(area.getText());
    }

    private String fetchNotes() {
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT Notes FROM RAINFOREST_NURSERY_TABLE WHERE TableNameID = ?")) {
            ps.setString(1, gridLocation);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("Notes");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    private void updateNotes(String notes) {
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE RAINFOREST_NURSERY_TABLE SET Notes = ? WHERE TableNameID = ?")) {
            ps.setString(1, notes); ps.setString(2, gridLocation);
            if (ps.executeUpdate() > 0) {
                setPane(new Color(0, 100, 0));
                JOptionPane.showMessageDialog(this,
                    "<html><font color='white'>Notes updated!</font></html>",
                    "Update Complete", JOptionPane.INFORMATION_MESSAGE);
                resetPane();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ------------------------------------------------------------------
    // Utility helpers
    // ------------------------------------------------------------------
    private void showWarn(String msg) {
        setPane(new Color(139, 0, 0));
        JOptionPane.showMessageDialog(this,
            "<html><font color='white'>" + msg + "</font></html>",
            "Warning", JOptionPane.WARNING_MESSAGE);
        resetPane();
    }

    private JButton styledButton(String text) {
        return styledButton(text, new Color(0, 100, 0), Color.WHITE);
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setForeground(fg); b.setBackground(bg);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        return b;
    }

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

// ==========================================================================
// Concrete sub-editors
// ==========================================================================

/** Editor for nursery table A1. */
class A1TableEditor extends TableEditor {
    public A1TableEditor(RainforestRestorationLogin mainApp) { super("A1", mainApp); }

    @Override
    protected DefaultTableModel createTableModel() {
        return buildModelForLocation("A1");
    }
}

/** Editor for nursery table A2. */
class A2TableEditor extends TableEditor {
    public A2TableEditor(RainforestRestorationLogin mainApp) { super("A2", mainApp); }

    @Override
    protected DefaultTableModel createTableModel() {
        return buildModelForLocation("A2");
    }
}

/** Generic editor used for all other grid locations. */
class DefaultTableEditor extends TableEditor {
    public DefaultTableEditor(String loc, RainforestRestorationLogin mainApp) {
        super(loc, mainApp);
    }

    @Override
    protected DefaultTableModel createTableModel() {
        return buildModelForLocation(gridLocation);
    }
}

// --------------------------------------------------------------------------
// Shared factory method (package-private static so all sub-editors can call it)
// --------------------------------------------------------------------------
/** Queries PLANT_SPECIES_RECORD filtered by TableNameID and returns a read-only model. */
static DefaultTableModel buildModelForLocation(String location) {
    DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    String sql = "SELECT * FROM PLANT_SPECIES_RECORD WHERE TableNameID = ?";
    try (Connection conn = DriverManager.getConnection(
                DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, location);
        try (ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) model.addColumn(meta.getColumnName(i));
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 1; i <= cols; i++) row[i - 1] = rs.getObject(i);
                model.addRow(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading table " + location + ": " + e.getMessage());
    }
    return model;
}
