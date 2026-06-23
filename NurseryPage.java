import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;

/**
 * NurseryPage.java
 * Builds the Rainforest Nursery grid page (shown to both Students and Admins).
 *
 * File-path references use ASSETS_DIR constant.
 * DB credentials come from DBConfig; no passwords are hard-coded here.
 */
public class NurseryPage {

    private static final String ASSETS_DIR = "/path/to/assets/"; // <-- update this path

    private JFrame nurseryFrame;
    private final RainforestRestorationLogin mainApp;
    private final boolean isAdmin;

    public NurseryPage(RainforestRestorationLogin mainApp, boolean isAdmin) {
        this.mainApp = mainApp;
        this.isAdmin = isAdmin;
        show();
    }

    public void show() {
        if (nurseryFrame != null) nurseryFrame.dispose();
        nurseryFrame = new JFrame("Tree Trackr. Rainforest Nursery");
        nurseryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel nurseryPanel = new JPanel(new BorderLayout());
        nurseryPanel.setBackground(new Color(220, 240, 220));

        // ---- Menu bar ----
        Color darkGreen = new Color(0, 100, 0);
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(darkGreen);
        menuBar.setOpaque(true);

        JMenu menu = new JMenu("Menu");
        menu.setForeground(Color.WHITE);
        menu.setBackground(darkGreen);
        menu.setOpaque(true);
        menu.setIcon(menuHamburgerIcon(darkGreen));

        JMenu gridMenu = new JMenu("Grid Editors");
        styleMenuItem(gridMenu, darkGreen);

        for (char row = 'A'; row <= 'F'; row++) {
            for (int col = 1; col <= 4; col++) {
                if ((row == 'C' || row == 'D') && col > 1) continue;
                String loc = row + "" + col;
                JMenuItem item = new JMenuItem(loc);
                styleMenuItem(item, darkGreen);
                item.addActionListener(e -> mainApp.openGridEditor(loc));
                gridMenu.add(item);
            }
        }
        menu.add(gridMenu);

        JMenuItem loginItem = new JMenuItem("Login Page");
        styleMenuItem(loginItem, darkGreen);
        loginItem.addActionListener(e -> { nurseryFrame.dispose(); mainApp.createGUI(); });
        if (!isAdmin) {
            // Students don't need a direct login-page link in the menu
        } else {
            menu.add(loginItem);
            JMenuItem adminItem = new JMenuItem("Admin Page");
            styleMenuItem(adminItem, darkGreen);
            adminItem.addActionListener(e -> { nurseryFrame.dispose(); mainApp.showAdminPage(); });
            menu.add(adminItem);
        }

        menuBar.add(menu);
        nurseryFrame.setJMenuBar(menuBar);

        // ---- Title / control panel ----
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(220, 240, 220));
        JLabel titleLabel = new JLabel("Tree Trackr. Rainforest Nursery", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel subLabel = new JLabel("Your guide to navigate and track the Rainforest Nursery", JLabel.CENTER);
        subLabel.setFont(new Font("Times New Roman", Font.ITALIC, 16));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subLabel,   BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(darkGreen); loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false); loginBtn.setOpaque(true); loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> { nurseryFrame.dispose(); mainApp.createGUI(); });

        JPanel ctrlPanel = new JPanel(new BorderLayout());
        ctrlPanel.setBackground(new Color(220, 240, 220));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRow.setBackground(new Color(220, 240, 220));
        btnRow.add(loginBtn);
        ctrlPanel.add(btnRow,       BorderLayout.EAST);
        ctrlPanel.add(titlePanel,   BorderLayout.CENTER);
        nurseryPanel.add(ctrlPanel, BorderLayout.NORTH);

        // ---- Grid panel ----
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(new Color(220, 240, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        Color[] cellColors = {Color.RED, Color.ORANGE,
            new Color(0, 0, 255), new Color(128, 0, 128)};

        ImageIcon eyeIcon = new ImageIcon(new ImageIcon(ASSETS_DIR + "Eye Icon.png")
            .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        ImageIcon addIcon = new ImageIcon(new ImageIcon(ASSETS_DIR + "Add.png")
            .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));

        for (char row = 'A'; row <= 'F'; row++) {
            gbc.gridy = row - 'A';
            for (int col = 1; col <= 4; col++) {
                if ((row == 'C' || row == 'D') && col > 1) continue;
                gbc.gridx = col - 1;

                JPanel cell = new JPanel(new BorderLayout());
                cell.setPreferredSize(new Dimension(150, 60));
                cell.setBackground(cellColors[col - 1]);

                String loc = row + "" + col;
                JLabel lbl = new JLabel(loc, JLabel.CENTER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Arial", Font.BOLD, 16));
                cell.add(lbl, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                btnPanel.setOpaque(false);

                JButton viewBtn = new JButton(eyeIcon);
                viewBtn.setPreferredSize(new Dimension(30, 30));
                viewBtn.setBackground(darkGreen);
                viewBtn.setOpaque(true); viewBtn.setBorderPainted(false);
                viewBtn.addActionListener(e -> mainApp.openGridEditor(loc));
                btnPanel.add(viewBtn);

                JButton addBtn2 = new JButton(addIcon);
                addBtn2.setPreferredSize(new Dimension(30, 30));
                addBtn2.setBackground(darkGreen);
                addBtn2.setOpaque(true); addBtn2.setBorderPainted(false);
                addBtn2.addActionListener(e -> mainApp.showAddPlantDialogFromNurseryPage());
                btnPanel.add(addBtn2);

                cell.add(btnPanel, BorderLayout.EAST);
                gridPanel.add(cell, gbc);
            }
        }

        // ---- Plant data table ----
        DefaultTableModel plantModel = mainApp.createTableModel();
        JTable plantTable = new JTable(plantModel);
        plantTable.setFont(new Font("Arial", Font.PLAIN, 14));
        plantTable.setRowHeight(24);
        plantTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {100,150,100,120,120,120,140,100,120,140,120,100};
        for (int i = 0; i < Math.min(colWidths.length, plantTable.getColumnCount()); i++)
            plantTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        JScrollPane plantScroll = new JScrollPane(plantTable);
        plantScroll.setPreferredSize(new Dimension(450, 200));
        plantScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0,100,0)),
            "All About the Rainforest Nursery",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14), Color.BLACK));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0,100,0));
        refreshBtn.setForeground(Color.BLACK);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> mainApp.refreshPlantTable(plantModel, nurseryFrame));

        JPanel plantDataPanel = new JPanel(new BorderLayout());
        plantDataPanel.add(plantScroll, BorderLayout.CENTER);
        JPanel refreshRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshRow.setBackground(new Color(220, 240, 220));
        refreshRow.add(refreshBtn);
        plantDataPanel.add(refreshRow, BorderLayout.SOUTH);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.gridwidth = 3; gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gridPanel.add(plantDataPanel, gbc);

        nurseryPanel.add(gridPanel, BorderLayout.CENTER);

        // ---- Status bar ----
        JTextField statusBar = new JTextField(
            "Click on the eye icon to view further details and the green add button to add plants.");
        statusBar.setEditable(false);
        statusBar.setBackground(new Color(0,100,0));
        statusBar.setForeground(Color.WHITE);
        nurseryPanel.add(statusBar, BorderLayout.SOUTH);

        nurseryFrame.add(nurseryPanel);
        nurseryFrame.setSize(600, 400);
        nurseryFrame.setLocationRelativeTo(null);
        nurseryFrame.setVisible(true);
    }

    // ------------------------------------------------------------------
    // Utilities
    // ------------------------------------------------------------------
    private void styleMenuItem(JMenuItem item, Color bg) {
        item.setForeground(Color.WHITE);
        item.setBackground(bg);
        item.setOpaque(true);
    }

    private Icon menuHamburgerIcon(Color bg) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(bg);
                g.fillRect(x, y, 16, 16);
                g.setColor(Color.WHITE);
                g.fillRect(x+3, y+3,  10, 2);
                g.fillRect(x+3, y+7,  10, 2);
                g.fillRect(x+3, y+11, 10, 2);
            }
            public int getIconWidth()  { return 16; }
            public int getIconHeight() { return 16; }
        };
    }
}
