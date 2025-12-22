package ui.gui.FormDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import connectDB.ConnectDB;
import dao.NhanVien_DAO;
import entity.NhanVien;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
// *** (Remove imports for GUI_NhanVienLeTan, GUI_NhanVienQuanLy - Application handles opening them) ***

public class FormDangNhap extends JFrame {
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JButton btnEye;

    // *** Store the full NhanVien object ***
    private NhanVien loggedInNhanVien = null;
    // *** NhanVien_DAO instance ***
    private NhanVien_DAO nhanVienDAO;

    private final String PLACEHOLDER_USER = "Nhập mã nhân viên"; // Updated placeholder
    private final String PLACEHOLDER_PASS = "Nhập mật khẩu";

    public FormDangNhap() {
        try {
            // Initialize DAO
            nhanVienDAO = new NhanVien_DAO();

            // Debug: Print NhanVien table data
            try (Connection conn = ConnectDB.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT maNV, hoTen, matKhau FROM NhanVien")) {

                System.out.println("\nDanh sách nhân viên trong DB:");
                while (rs.next()) {
                    System.out.println("MaNV: " + rs.getString("maNV")
                            + " - HoTen: " + rs.getString("hoTen")
                            + " - MatKhau: " + rs.getString("matKhau"));
                }
            } catch (SQLException e) {
                System.err.println("Lỗi kiểm tra dữ liệu: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo DAO: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi không xác định khi khởi tạo: " + e.getMessage(),
                    "Lỗi nghiêm trọng",
                    JOptionPane.ERROR_MESSAGE);
            dispose(); // Close form
        }

        setTitle("TBQTT Hotel - Đăng nhập");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Dispose closes only this window
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        // --- UI Code (Keep your existing UI creation code here) ---
        // Background with gradient
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        // Login card
        CardPanel card = new CardPanel();
        card.setPreferredSize(new Dimension(520, 520));
        card.setLayout(new BorderLayout());
        bg.add(card);

        // Logo and titles
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel logoWrap = new JPanel(new GridBagLayout());
        logoWrap.setOpaque(false);

        // Load logo if exists
        try {
            // Load logo from image folder
            java.net.URL logoUrl = getClass().getClassLoader().getResource("image/logo.png");
            if (logoUrl != null) {
                ImageIcon logoIcon = new ImageIcon(logoUrl);
                Image img = logoIcon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                logoWrap.add(new JLabel(new ImageIcon(img)));
            } else {
                throw new Exception("Logo image not found in resources.");
            }
        } catch (Exception e) {
            System.err.println("Warning: Logo image load failed: " + e.getMessage() + ". Using text logo.");
            JLabel textLogo = new JLabel("TBQTT", SwingConstants.CENTER);
            textLogo.setFont(new Font("Arial", Font.BOLD, 48));
            logoWrap.add(textLogo);
        }
        top.add(logoWrap, BorderLayout.NORTH);

        JLabel title = new JLabel("TBQTT Hotel", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(40, 44, 52));
        top.add(title, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("Hệ thống quản lý khách sạn", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(110, 118, 130));
        top.add(subtitle, BorderLayout.SOUTH);

        top.setBorder(new EmptyBorder(24, 24, 12, 24));
        card.add(top, BorderLayout.NORTH);

        // Login form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 40, 10, 40)); // Add padding around form elements
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        // Label cho Tên đăng nhập (Mã NV)
        JLabel lblTenDangNhap = new JLabel("Mã nhân viên"); // Changed label text
        lblTenDangNhap.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblTenDangNhap.setForeground(new Color(80, 90, 100));
        form.add(lblTenDangNhap, c);

        c.gridy++;
        txtTenDangNhap = new JTextField();
        styleField(txtTenDangNhap);
        setPlaceholder(txtTenDangNhap, PLACEHOLDER_USER);
        form.add(txtTenDangNhap, c);

        // Label cho Mật khẩu
        c.gridy++;
        JLabel lblMatKhau = new JLabel("Mật khẩu");
        lblMatKhau.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblMatKhau.setForeground(new Color(80, 90, 100));
        form.add(lblMatKhau, c);

        c.gridy++;
        JPanel passWrap = new JPanel(new BorderLayout(6, 0));
        passWrap.setOpaque(false);
        txtMatKhau = new JPasswordField();
        styleField(txtMatKhau);
        setPlaceholder(txtMatKhau, PLACEHOLDER_PASS);

        btnEye = new JButton("\uD83D\uDC41"); // Eye icon Unicode
        btnEye.setPreferredSize(new Dimension(44, 36));
        btnEye.setFocusPainted(false);
        btnEye.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));
        btnEye.setBackground(Color.WHITE);
        btnEye.addActionListener(e -> togglePasswordVisibility());

        passWrap.add(txtMatKhau, BorderLayout.CENTER);
        passWrap.add(btnEye, BorderLayout.EAST);
        form.add(passWrap, c);

        // Nút Đăng nhập
        c.gridy++;
        c.insets = new Insets(15, 8, 8, 8); // Add top margin
        btnDangNhap = new JButton("Đăng nhập");
        final Color BTN_GREEN = new Color(39, 174, 96); // Define color here
        btnDangNhap.setPreferredSize(new Dimension(200, 44));
        btnDangNhap.setBackground(BTN_GREEN);
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setFont(new Font("SansSerif", Font.BOLD, 14)); // Make text bold
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setBorder(BorderFactory.createEmptyBorder());
        btnDangNhap.setOpaque(true);
        btnDangNhap.setContentAreaFilled(true);
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Keep button green on interaction
        btnDangNhap.addMouseListener(new MouseAdapter() {
            Color originalColor = BTN_GREEN;
            Color darkerColor = BTN_GREEN.darker(); // Slightly darker green

            @Override
            public void mousePressed(MouseEvent e) {
                btnDangNhap.setBackground(darkerColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btnDangNhap.setBackground(originalColor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btnDangNhap.setBackground(darkerColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnDangNhap.setBackground(originalColor);
            }
        });

        form.add(btnDangNhap, c);

        // Footer
        c.gridy++;
        c.insets = new Insets(20, 8, 8, 8); // Add top margin
        JLabel footer = new JLabel("© 2025 TBQTT Hotel.", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(150, 160, 175));
        form.add(footer, c);

        card.add(form, BorderLayout.CENTER);

        // --- Event Handlers ---
        btnDangNhap.addActionListener(e -> onLogin());
        // Allow login by pressing Enter in password field
        txtMatKhau.addActionListener(e -> onLogin());
        // Move focus to password field when Enter is pressed in username field
        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());

        // Request focus on username field initially
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtTenDangNhap.requestFocusInWindow();
            }
        });
    }

    // --- (styleField, setPlaceholder, togglePasswordVisibility methods remain the
    // same) ---
    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(320, 36));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setForeground(new Color(30, 30, 35)); // Set default text color
    }

    private void setPlaceholder(JTextComponent comp, String text) {
        comp.setText(text);
        comp.setForeground(new Color(150, 150, 160)); // Placeholder color
        comp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Use equalsIgnoreCase for placeholder text comparison
                String currentText = (comp instanceof JPasswordField)
                        ? String.valueOf(((JPasswordField) comp).getPassword())
                        : comp.getText();
                if (currentText.equalsIgnoreCase(text) && comp.getForeground().equals(new Color(150, 150, 160))) { // Check
                                                                                                                   // if
                                                                                                                   // it's
                                                                                                                   // placeholder
                    comp.setText("");
                    comp.setForeground(new Color(30, 30, 35)); // Normal text color
                    if (comp instanceof JPasswordField) {
                        ((JPasswordField) comp).setEchoChar('\u2022'); // Set echo char for password
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = (comp instanceof JPasswordField)
                        ? String.valueOf(((JPasswordField) comp).getPassword())
                        : comp.getText();
                if (currentText.trim().isEmpty()) {
                    comp.setText(text);
                    comp.setForeground(new Color(150, 150, 160)); // Placeholder color
                    if (comp instanceof JPasswordField) {
                        ((JPasswordField) comp).setEchoChar((char) 0); // Hide echo char for placeholder
                    }
                }
            }
        });
        // Initially hide echo char if it's a password field with placeholder
        if (comp instanceof JPasswordField) {
            ((JPasswordField) comp).setEchoChar((char) 0);
        }
    }

    private void togglePasswordVisibility() {
        // Only toggle if not showing placeholder
        if (!txtMatKhau.getForeground().equals(new Color(150, 150, 160))) {
            if (txtMatKhau.getEchoChar() == (char) 0) { // If currently visible
                txtMatKhau.setEchoChar('\u2022'); // Hide it
                btnEye.setText("\uD83D\uDC41"); // Show eye icon (closed)
            } else { // If currently hidden
                txtMatKhau.setEchoChar((char) 0); // Show it
                btnEye.setText("\uD83D\uDC41\uFE0F"); // Show eye icon (open - might need adjustment based on font)
            }
        }
    }

    /**
     * Handles the login button click.
     * Verifies credentials using NhanVien_DAO and stores the NhanVien object.
     */
    private void onLogin() {
        String user = txtTenDangNhap.getText().trim();
        String pass = String.valueOf(txtMatKhau.getPassword()); // Use String.valueOf

        // Basic validation for placeholders or empty fields
        if (user.isEmpty() || user.equalsIgnoreCase(PLACEHOLDER_USER)) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã nhân viên.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtTenDangNhap.requestFocusInWindow();
            return;
        }
        // Check password against placeholder AND empty string
        if (pass.isEmpty() || (String.valueOf(txtMatKhau.getPassword()).equals(PLACEHOLDER_PASS)
                && txtMatKhau.getForeground().equals(new Color(150, 150, 160)))) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtMatKhau.requestFocusInWindow();
            return;
        }

        try {
            // *** Use NhanVien_DAO to get the NhanVien object ***
            // - Assumes plaintext check for now
            // Ensure getNhanVienKhiDangNhap handles null returns correctly
            NhanVien nv = nhanVienDAO.getNhanVienKhiDangNhap(user, pass);

            if (nv != null) {
                // *** Login successful: Store the object and close the form ***
                this.loggedInNhanVien = nv;
                System.out.println("Login successful for: " + nv.getTenNV()); // Debug
                this.setVisible(false); // Hide the form first
                this.dispose(); // Then dispose it
            } else {
                // *** Login failed ***
                JOptionPane.showMessageDialog(this, "Sai mã nhân viên hoặc mật khẩu.", "Lỗi đăng nhập",
                        JOptionPane.ERROR_MESSAGE);
                // Clear only password field, keep username
                if (!String.valueOf(txtMatKhau.getPassword()).equals(PLACEHOLDER_PASS)) {
                    txtMatKhau.setText(""); // Clear only if it wasn't already placeholder
                }
                setPlaceholder(txtMatKhau, PLACEHOLDER_PASS); // Reset placeholder state
                txtMatKhau.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra đăng nhập: " + ex.getMessage(), "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi không xác định: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Getter for Application to retrieve the logged-in NhanVien object.
     * Returns null if login failed or was cancelled.
     */
    public NhanVien getLoggedInNhanVien() {
        return loggedInNhanVien;
    }

    // *** REMOVE old getters and GUI opening methods ***
    // public String getMaNV() { ... }
    // public String getChucVu() { ... }
    // private void openManagerGUI() { ... }
    // private void openReceptionistGUI() { ... }

    // --- (Custom panels BackgroundPanel and CardPanel remain the same) ---
    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                // Load background image from img folder
                java.net.URL imageUrl = getClass().getClassLoader()
                        .getResource("img/lovepik-hotel-lobby-picture_501493440.jpg");
                if (imageUrl != null) {
                    backgroundImage = new ImageIcon(imageUrl).getImage();
                } else {
                    System.err.println("Warning: Background image not found in resources.");
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to load background image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();

            if (backgroundImage != null) {
                // Scale image to cover the entire panel while maintaining aspect ratio
                int imgW = backgroundImage.getWidth(this);
                int imgH = backgroundImage.getHeight(this);

                double scaleX = (double) w / imgW;
                double scaleY = (double) h / imgH;
                double scale = Math.max(scaleX, scaleY);

                int scaledW = (int) (imgW * scale);
                int scaledH = (int) (imgH * scale);
                int x = (w - scaledW) / 2;
                int y = (h - scaledH) / 2;

                g2.drawImage(backgroundImage, x, y, scaledW, scaledH, this);

                // Add a semi-transparent overlay for better readability
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(0, 0, w, h);
            } else {
                // Fallback to gradient if image not loaded
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 248, 255), 0, h, new Color(252, 254, 255));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
        }
    }

    static class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), arc = 18;
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(6, 8, w - 12, h - 12, arc, arc);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 12, h - 12, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // Optional: Main method for testing just the login form
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FormDangNhap().setVisible(true);
        });
    }
}