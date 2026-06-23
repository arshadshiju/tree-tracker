import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import com.toedter.calendar.JDateChooser;

/**
 * AdminPage.java
 * Builds and manages the Administrator home page.
 *
 * File-path references use ASSETS_DIR constant.
 * DB credentials come from DBConfig; no passwords are hard-coded here.
 */
public class AdminPage {

    private static final String ASSETS_DIR = "/path/to/assets/"; // <-- update this path

    private JFrame frame;
    private DefaultTableModel plantTableModel;
    private DefaultTableModel studentTableModel;
    private JTable studentTable;
    private JTable plantTable;

    private boolean isAlphabeticalOrder = false;
    private List<Object[]> originalOrder;

    private final RainforestRestorationLogin mainApp;

    public AdminPage(RainforestRestorationLogin mainApp) {
        this.mainApp = mainApp;
        show();
    }

    // ------------------------------------------------------------------
    // Main frame
    // ------------------------------------------------------------------
    public void show() {
        if (frame != null) frame.dispose();
        frame = new JFrame("Rainforest Restoration Administration Homepage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel adminPanel = new JPanel();
        adminPanel.setBackground(new Color(220, 240, 220));
        adminPanel.setLayout(null);
        adminPanel.setPreferredSize(new Dimension(780, 1270));

        // Title
        JLabel title = new JLabel(
            "<html><b>Tree Trackr. Administrator HomePage</b><br>"
            + "<span style='font-family:Times New Roman;font-style:italic;font-size:12px;'>"
            + "Your guide to navigate and track the Rainforest Nursery!</span></html>");
        title.setFont(new Font("Arial", Font.PLAIN, 24));
        title.setBounds(20, 10, 600, 50);
        adminPanel.add(title);

        // Logo
        ImageIcon logoIcon = new ImageIcon(ASSETS_DIR + "Rainforest Logo.png");
        JLabel logoLabel = new JLabel(new ImageIcon(
            logoIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH)));
        logoLabel.setBounds(620, 10, 150, 100);
        adminPanel.add(logoLabel);

        // Student list
        JPanel studentPanel = buildStudentPanel();
        studentPanel.setBounds(20, 120, 360, 400);
        adminPanel.add(studentPanel);

        // Plant data
        JPanel plantPanel = buildPlantPanel();
        plantPanel.setBounds(400, 120, 360, 400);
        adminPanel.add(plantPanel);

        // Access Nursery button
        JButton nurseryBtn = darkGreenButton("Access the Rainforest Restoration Home Page");
        nurseryBtn.setFont(new Font("Arial", Font.BOLD, 18));
        nurseryBtn.setBounds(20, 530, 600, 50);
        nurseryBtn.addActionListener(e -> { frame.dispose(); mainApp.showNurseryPage(); });
        adminPanel.add(nurseryBtn);

        // Home (back to login) button
        JButton homeBtn = darkGreenButton("Home");
        homeBtn.setFont(new Font("Arial", Font.BOLD, 18));
        homeBtn.setBounds(630, 530, 130, 50);
        homeBtn.addActionListener(e -> { frame.dispose(); mainApp.createGUI(); });
        adminPanel.add(homeBtn);

        // Dead-plants table
        JPanel deadPanel = buildDeadPlantsPanel();
        deadPanel.setBounds(20, 590, 740, 200);
        adminPanel.add(deadPanel);

        // Last-fertilised table
        JPanel fertPanel = buildLastFertilisedPanel();
        fertPanel.setBounds(20, 800, 740, 200);
        adminPanel.add(fertPanel);

        // Species filter
        JPanel speciesPanel = buildSpeciesPanel();
        speciesPanel.setBounds(20, 1010, 740, 250);
        adminPanel.add(speciesPanel);

        mainPanel.add(adminPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        frame.getContentPane().add(scrollPane);
        frame.setSize(800, 650);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ------------------------------------------------------------------
    // Sub-panels
    // ------------------------------------------------------------------
    private JPanel buildStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(titledBorder("Student List"));

        String[][] data = retrieveStudentData();
        studentTableModel = new DefaultTableModel(
            data, new String[]{"UsernameID", "First Name", "Last Name", "Email"});
        studentTable = new JTable(studentTableModel);
        studentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        studentTable.setRowHeight(24);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {100, 100, 100, 150};
        for (int i = 0; i < widths.length; i++)
            studentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(studentTable);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);

        ImageIcon delIcon = scaled(ASSETS_DIR + "Delete.png", 16, 16);
        ImageIcon addIcon = scaled(ASSETS_DIR + "Add.png",    16, 16);
        JButton delBtn  = new JButton(delIcon);
        JButton addBtn  = new JButton(addIcon);
        JButton editBtn = new JButton("Update Student Details");
        delBtn .addActionListener(e -> deleteRecord(studentTable, studentTableModel));
        addBtn .addActionListener(e -> mainApp.showAddUserPanel());
        editBtn.addActionListener(e -> editSelectedStudent());

        JPanel buttons = new JPanel();
        buttons.add(addBtn); buttons.add(delBtn); buttons.add(editBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        JButton sortBtn = new JButton("Toggle Alphabetical Order");
        sortBtn.setForeground(new Color(0, 100, 0));
        sortBtn.setFont(sortBtn.getFont().deriveFont(Font.BOLD));
        sortBtn.addActionListener(e -> toggleAlphabeticalOrder());
        panel.add(sortBtn, BorderLayout.NORTH);

        return panel;
    }

    private JPanel buildPlantPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        plantTableModel = mainApp.createTableModel();
        plantTable = new JTable(plantTableModel);
        plantTable.setFont(new Font("Arial", Font.PLAIN, 14));
        plantTable.setRowHeight(24);
        plantTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {100, 150, 100, 120, 120, 120, 140, 100, 120, 140, 120, 100};
        for (int i = 0; i < Math.min(widths.length, plantTable.getColumnCount()); i++)
            plantTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(plantTable);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setBorder(titledBorder("All About the Rainforest Nursery"));
        panel.add(sp, BorderLayout.CENTER);

        ImageIcon delIcon = scaled(ASSETS_DIR + "Delete.png", 16, 16);
        ImageIcon addIcon = scaled(ASSETS_DIR + "Add.png",    16, 16);
        JButton delBtn     = new JButton(delIcon);
        JButton addBtn     = new JButton(addIcon);
        JButton refreshBtn = darkGreenButton("Refresh");
        delBtn    .addActionListener(e -> deleteSelectedRecord());
        addBtn    .addActionListener(e -> mainApp.showAddPlantDialogFromNurseryPage());
        refreshBtn.addActionListener(e -> refreshPlantTable());

        JPanel buttons = new JPanel();
        buttons.add(addBtn); buttons.add(delBtn); buttons.add(refreshBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildDeadPlantsPanel() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"RecordID", "PlantSpecies", "TableNameID", "Quantity", "PlantStatus"}, 0);
        JTable table = styledTable(model);
        JScrollPane sp = scrolled(table, titledBorder("Dead Plants"));

        JButton btn = darkGreenButton("Retrieve Dead Plants");
        btn.setBackground(new Color(100, 100, 0));
        btn.addActionListener(e -> refreshDeadPlants(model));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(btn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);

        refreshDeadPlants(model);
        return panel;
    }

    private JPanel buildLastFertilisedPanel() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"RecordID", "PlantSpecies", "TableNameID", "Quantity", "LastFertilised"}, 0);
        JTable table = styledTable(model);
        JScrollPane sp = scrolled(table, null);

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dateRow.setBackground(new Color(0, 100, 0));
        JLabel lbl = new JLabel("Last Fertilised Before:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 17));
        JDateChooser chooser = new JDateChooser();
        chooser.setPreferredSize(new Dimension(150, 30));
        chooser.setDate(new Date());
        chooser.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName()))
                refreshLastFertilised(model, chooser.getDate());
        });
        dateRow.add(lbl);
        dateRow.add(Box.createHorizontalStrut(10));
        dateRow.add(chooser);

        sp.setBorder(titledBorder("Last Fertilised"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dateRow, BorderLayout.NORTH);
        panel.add(sp,      BorderLayout.CENTER);

        refreshLastFertilised(model, new Date());
        return panel;
    }

    private JPanel buildSpeciesPanel() {
        JPanel selPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selPanel.setBackground(new Color(0, 100, 0));
        JLabel lbl = new JLabel("Select Plant Species:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 17));
        JComboBox<String> combo = new JComboBox<>();
        mainApp.populateSpeciesComboBox(combo);
        combo.setPreferredSize(new Dimension(200, 30));
        selPanel.add(lbl);
        selPanel.add(Box.createHorizontalStrut(10));
        selPanel.add(combo);

        JTable speciesTable = new JTable();
        speciesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        speciesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        speciesTable.setRowHeight(24);
        JScrollPane sp = new JScrollPane(speciesTable);
        sp.setBorder(titledBorder("Selected Species Details"));

        combo.addActionListener(e ->
            mainApp.updateSpeciesTable(speciesTable, (String) combo.getSelectedItem()));
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
            mainApp.updateSpeciesTable(speciesTable, (String) combo.getSelectedItem());
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(selPanel, BorderLayout.NORTH);
        panel.add(sp,       BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // DB operations
    // ------------------------------------------------------------------
    private void refreshDeadPlants(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT RecordID, PlantSpecies, TableNameID, Quantity, PlantStatus "
                   + "FROM PLANT_SPECIES_RECORD WHERE PlantStatus = 'Dead' ORDER BY RecordID";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) model.addRow(new Object[]{
                rs.getInt("RecordID"), rs.getString("PlantSpecies"),
                rs.getString("TableNameID"), rs.getInt("Quantity"), rs.getString("PlantStatus")});
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void refreshLastFertilised(DefaultTableModel model, Date date) {
        model.setRowCount(0);
        String sql = "SELECT RecordID, PlantSpecies, TableNameID, Quantity, LastFertilised "
                   + "FROM PLANT_SPECIES_RECORD WHERE LastFertilised < ? ORDER BY LastFertilised DESC";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) model.addRow(new Object[]{
                    rs.getInt("RecordID"), rs.getString("PlantSpecies"),
                    rs.getString("TableNameID"), rs.getInt("Quantity"),
                    new java.sql.Date(rs.getTimestamp("LastFertilised").getTime())});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void refreshPlantTable() {
        plantTableModel.setRowCount(0);
        DefaultTableModel fresh = mainApp.createTableModel();
        for (int i = 0; i < fresh.getRowCount(); i++)
            plantTableModel.addRow(fresh.getDataVector().elementAt(i));
        plantTableModel.fireTableDataChanged();
    }

    private void deleteSelectedRecord() {
        int row = plantTable.getSelectedRow();
        if (row == -1) { showWarn("Please select a record to delete."); return; }
        int id = (int) plantTableModel.getValueAt(row, 0);
        setPane(new Color(139, 0, 0));
        int ok = JOptionPane.showConfirmDialog(frame,
            "<html><font color='white'>Delete Record ID " + id + "?</font></html>",
            "Confirm", JOptionPane.YES_NO_OPTION);
        resetPane();
        if (ok == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM PLANT_SPECIES_RECORD WHERE RecordID = ?";
            try (Connection conn = DriverManager.getConnection(
                        DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() > 0) plantTableModel.removeRow(row);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void deleteRecord(JTable t, DefaultTableModel m) {
        int row = t.getSelectedRow();
        if (row == -1) { showWarn("Please select a record to delete."); return; }
        setPane(new Color(139, 0, 0));
        int ok = JOptionPane.showConfirmDialog(frame,
            "<html><font color='white'>Delete this record?</font></html>",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        resetPane();
        if (ok == JOptionPane.YES_OPTION) m.removeRow(row);
    }

    private String[][] retrieveStudentData() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT UsernameID, FirstName, LastName, Email FROM student")) {
            while (rs.next()) list.add(new String[]{
                rs.getString("UsernameID"), rs.getString("FirstName"),
                rs.getString("LastName"),   rs.getString("Email")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new String[0][]);
    }

    // ------------------------------------------------------------------
    // Student edit
    // ------------------------------------------------------------------
    private void editSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) { showWarn("Please select a student to edit."); return; }

        String uid   = (String) studentTableModel.getValueAt(row, 0);
        JTextField fnField = new JTextField((String) studentTableModel.getValueAt(row, 1));
        JTextField lnField = new JTextField((String) studentTableModel.getValueAt(row, 2));
        JTextField emField = new JTextField((String) studentTableModel.getValueAt(row, 3));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 100, 0));
        JPanel labels = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel fields = new JPanel(new GridLayout(3, 1, 5, 5));
        labels.setBackground(new Color(0, 100, 0));
        fields.setBackground(new Color(0, 100, 0));
        for (String txt : new String[]{"First Name:", "Last Name:", "Email:"}) {
            JLabel l = new JLabel(txt); l.setForeground(Color.WHITE); labels.add(l);
        }
        fields.add(fnField); fields.add(lnField); fields.add(emField);
        panel.add(labels, BorderLayout.WEST);
        panel.add(fields, BorderLayout.CENTER);

        setPane(new Color(0, 100, 0));
        int r = JOptionPane.showConfirmDialog(null, panel, "Edit Student",
            JOptionPane.OK_CANCEL_OPTION);
        resetPane();

        if (r == JOptionPane.OK_OPTION) {
            studentTableModel.setValueAt(fnField.getText(), row, 1);
            studentTableModel.setValueAt(lnField.getText(), row, 2);
            studentTableModel.setValueAt(emField.getText(), row, 3);
            updateStudentInDB(uid, fnField.getText(), lnField.getText(), emField.getText());
        }
    }

    private void updateStudentInDB(String uid, String fn, String ln, String em) {
        String sql = "UPDATE student SET FirstName=?, LastName=?, Email=? WHERE UsernameID=?";
        try (Connection conn = DriverManager.getConnection(
                    DBConfig.DB_URL, DBConfig.USER, DBConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fn); ps.setString(2, ln);
            ps.setString(3, em); ps.setString(4, uid);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Student updated successfully.");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ------------------------------------------------------------------
    // Alphabetical sort (bubble sort)
    // ------------------------------------------------------------------
    private void toggleAlphabeticalOrder() {
        if (isAlphabeticalOrder) restoreOriginalOrder();
        else { saveOriginalOrder(); bubbleSort(); }
        isAlphabeticalOrder = !isAlphabeticalOrder;
    }

    private void saveOriginalOrder() {
        originalOrder = new ArrayList<>();
        for (int i = 0; i < studentTableModel.getRowCount(); i++) {
            Object[] row = new Object[studentTableModel.getColumnCount()];
            for (int j = 0; j < row.length; j++) row[j] = studentTableModel.getValueAt(i, j);
            originalOrder.add(row);
        }
    }

    private void restoreOriginalOrder() {
        studentTableModel.setRowCount(0);
        for (Object[] r : originalOrder) studentTableModel.addRow(r);
    }

    private void bubbleSort() {
        int n = studentTableModel.getRowCount();
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++) {
                String a = (String) studentTableModel.getValueAt(j,     1);
                String b = (String) studentTableModel.getValueAt(j + 1, 1);
                if (a.compareToIgnoreCase(b) > 0) swapRows(j, j + 1);
            }
        studentTableModel.fireTableDataChanged();
    }

    private void swapRows(int r1, int r2) {
        for (int c = 0; c < studentTableModel.getColumnCount(); c++) {
            Object tmp = studentTableModel.getValueAt(r1, c);
            studentTableModel.setValueAt(studentTableModel.getValueAt(r2, c), r1, c);
            studentTableModel.setValueAt(tmp, r2, c);
        }
    }

    // ------------------------------------------------------------------
    // Utility / factory helpers
    // ------------------------------------------------------------------
    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.setRowHeight(24);
        return t;
    }

    private JScrollPane scrolled(JTable table, Border border) {
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        if (border != null) sp.setBorder(border);
        return sp;
    }

    private TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0)), title,
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14), Color.BLACK);
    }

    private JButton darkGreenButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0, 100, 0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    private ImageIcon scaled(String path, int w, int h) {
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private void showWarn(String msg) {
        setPane(new Color(139, 0, 0));
        JOptionPane.showMessageDialog(frame,
            "<html><font color='white'>" + msg + "</font></html>",
            "Warning", JOptionPane.WARNING_MESSAGE);
        resetPane();
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

    private void styleScrollBar(JScrollBar bar) {
        bar.setUnitIncrement(16);
        bar.setPreferredSize(new Dimension(12, Integer.MAX_VALUE));
        bar.setUI(new BasicScrollBarUI() {
            private final Color THUMB = new Color(0, 100, 0);
            private final Color TRACK = new Color(240, 240, 240);
            @Override protected void configureScrollBarColors() {
                thumbColor = THUMB; trackColor = TRACK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            private JButton zero() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0));
                b.setMinimumSize(new Dimension(0,0));
                b.setMaximumSize(new Dimension(0,0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THUMB);
                g2.fillRoundRect(r.x, r.y, 12, r.height, 12, 12);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TRACK);
                g2.fillRect(r.x, r.y, r.width, r.height);
                g2.dispose();
            }
        });
    }
}
