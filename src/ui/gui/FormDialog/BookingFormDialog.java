package ui.gui.FormDialog;

import entity.DichVu;
import event.EventDatPhong;
import dao.KhuyenMai_DAO;
import entity.KhuyenMai;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingFormDialog extends JDialog {

    private final EventDatPhong controller;
    private final List<Map<String, Object>> selectedRoomDetails;
    private final List<DichVu> dsDichVu;
    private final boolean isCheckinNow;

    private JTextField txtTenKhachHang, txtEmail, txtSoDienThoai;
    private JTextField txtMaKhuyenMai;
    private JButton btnApplyKm;
    private JLabel lblKmInfo;
    private JTextArea txtYeuCau;
    private List<JCheckBox> chkDichVuList;
    private JPanel suggestedPromosPanel; // Panel chứa các mã khuyến mãi gợi ý

    public BookingFormDialog(Frame owner, List<Map<String, Object>> selectedRoomDetails, List<DichVu> dsDichVu,
            EventDatPhong controller, boolean isCheckinNow) {
        super(owner, "Xác nhận thông tin Đặt phòng", true);
        this.controller = controller;
        this.selectedRoomDetails = selectedRoomDetails;
        this.dsDichVu = dsDichVu;
        this.isCheckinNow = isCheckinNow;

        setSize(800, 700); // Tăng chiều cao để có không gian cho gợi ý
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setOpaque(false);

        // Left panel for customer info and services
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(createCustomerInfoPanel());
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(createServiceSelectionPanel());

        // Right panel for booking summary
        JPanel rightPanel = createBookingSummaryPanel();

        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Bottom button panel
        JPanel buttonPanel = createButtonPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        setupEventListeners();
        updateSuggestedPromos(); // Hiển thị gợi ý ngay khi mở dialog
    }

    private JPanel createCustomerInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(createTitledBorder("Thông tin khách hàng"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Họ và tên *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTenKhachHang = new JTextField(20);
        panel.add(txtTenKhachHang, gbc);

        // Row 2: Phone Number
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Số điện thoại *"), gbc);
        gbc.gridx = 1;
        txtSoDienThoai = new JTextField();
        panel.add(txtSoDienThoai, gbc);

        // Row 3: Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email *"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField();
        panel.add(txtEmail, gbc);

        return panel;
    }

    private JPanel createServiceSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10)); // 1 cột
        panel.setOpaque(false);
        panel.setBorder(createTitledBorder("Dịch vụ đi kèm"));

        chkDichVuList = new ArrayList<>();
        if (dsDichVu != null) {
            for (DichVu service : dsDichVu) {
                String text = String.format("<html><b>%s</b> - %,.0f đ<br><i style='color:gray'>%s</i></html>",
                        service.getTenDV(), service.getGiaTien(), service.getMoTa());
                JCheckBox chk = new JCheckBox(text);
                chk.setOpaque(false);
                chk.setName(service.getMaDV());
                chkDichVuList.add(chk);
                panel.add(chk);
            }
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBookingSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("Tóm tắt đặt phòng"));
        panel.setPreferredSize(new Dimension(320, 0));

        // Selected rooms
        JPanel roomsPanel = new JPanel();
        roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));
        roomsPanel.setOpaque(false);
        roomsPanel.setBorder(new EmptyBorder(5, 10, 15, 10));
        roomsPanel.add(new JLabel("<html><b>Phòng đã chọn:</b></html>"));
        roomsPanel.add(Box.createVerticalStrut(5));

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        for (Map<String, Object> room : selectedRoomDetails) {
            String roomInfo = String.format("• %s (%s) - %s, %d người",
                    room.get("maPhong"),
                    room.get("tenLoaiPhong"),
                    currencyFormatter.format(room.get("giaTien")),
                    room.get("soChua"));
            JLabel roomLabel = new JLabel(roomInfo);
            roomsPanel.add(roomLabel);
        }
        panel.add(roomsPanel);

        // Special request
        panel.add(new JLabel("<html><b>Yêu cầu đặc biệt:</b></html>"));
        txtYeuCau = new JTextArea(4, 20);
        txtYeuCau.setLineWrap(true);
        txtYeuCau.setWrapStyleWord(true);
        JScrollPane scrollYeuCau = new JScrollPane(txtYeuCau);
        panel.add(scrollYeuCau);
        panel.add(Box.createVerticalStrut(15));

        // Promo code
        panel.add(new JLabel("<html><b>Mã khuyến mãi:</b></html>"));

        JPanel promoInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        promoInputPanel.setOpaque(false);
        txtMaKhuyenMai = new JTextField(10); // Thu nhỏ ô nhập liệu
        btnApplyKm = new JButton("Áp dụng");
        promoInputPanel.add(txtMaKhuyenMai);
        promoInputPanel.add(btnApplyKm);
        panel.add(promoInputPanel);

        lblKmInfo = new JLabel(" ");
        lblKmInfo.setFont(lblKmInfo.getFont().deriveFont(Font.ITALIC, 11f));
        panel.add(lblKmInfo);

        // Suggested promos panel
        suggestedPromosPanel = new JPanel();
        suggestedPromosPanel.setLayout(new BoxLayout(suggestedPromosPanel, BoxLayout.Y_AXIS));
        suggestedPromosPanel.setOpaque(false);

        JScrollPane promoScrollPane = new JScrollPane(suggestedPromosPanel);
        promoScrollPane.setBorder(BorderFactory.createTitledBorder("Gợi ý"));
        promoScrollPane.setOpaque(false);
        promoScrollPane.getViewport().setOpaque(false);
        promoScrollPane.setPreferredSize(new Dimension(0, 120));

        panel.add(promoScrollPane);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnCancel = new JButton("Hủy");
        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
        btnConfirm.setForeground(Color.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> {
            try {
                confirmBooking();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        return buttonPanel;
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                GUI_NhanVienLeTan.ACCENT_BLUE);
    }

    private void setupEventListeners() {
        btnApplyKm.addActionListener(e -> {
            if (btnApplyKm.getText().equals("Áp dụng")) {
                applyPromoCode();
            } else {
                removePromoCode();
            }
        });
        txtMaKhuyenMai.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestedPromos();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestedPromos();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestedPromos();
            }
        });
    }

    private void updateSuggestedPromos() {
        suggestedPromosPanel.removeAll();
        try {
            KhuyenMai_DAO kmDao = new KhuyenMai_DAO();
            List<KhuyenMai> allPromos = kmDao.getAllKhuyenMai();
            String currentInput = txtMaKhuyenMai.getText().trim().toLowerCase();

            // Lọc chỉ hiển thị các mã còn hiệu lực:
            // 1. Đã bắt đầu (today >= ngayBatDau)
            // 2. Chưa hết hạn (today <= ngayKetThuc)
            // 3. Còn lượt sử dụng
            java.time.LocalDate today = java.time.LocalDate.now();

            List<KhuyenMai> availablePromos = allPromos.stream()
                    .filter(km -> {
                        // Kiểm tra đã bắt đầu chưa
                        boolean hasStarted = (km.getNgayBatDau() == null ||
                                !today.isBefore(km.getNgayBatDau().toLocalDate()));
                        // Kiểm tra còn hiệu lực không
                        boolean notExpired = (km.getNgayKetThuc() == null ||
                                !today.isAfter(km.getNgayKetThuc().toLocalDate()));
                        // Kiểm tra còn lượt sử dụng
                        boolean hasUsages = km.getLuotSuDung() > 0;

                        return hasStarted && notExpired && hasUsages;
                    })
                    .filter(km -> km.getMaKhuyenMai().toLowerCase().contains(currentInput)
                            || km.getTenKhuyenMai().toLowerCase().contains(currentInput))
                    .collect(Collectors.toList());

            if (!availablePromos.isEmpty()) {
                for (KhuyenMai km : availablePromos) {
                    JPanel promoPanel = createPromoSuggestionPanel(km);
                    suggestedPromosPanel.add(promoPanel);
                    suggestedPromosPanel.add(Box.createVerticalStrut(5));
                }
            } else {
                suggestedPromosPanel.add(new JLabel("Không có mã khuyến mãi phù hợp."));
            }
        } catch (SQLException ex) {
            suggestedPromosPanel.add(new JLabel("Lỗi tải khuyến mãi."));
        }
        suggestedPromosPanel.revalidate();
        suggestedPromosPanel.repaint();
    }

    private JPanel createPromoSuggestionPanel(KhuyenMai km) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 240)),
                new EmptyBorder(8, 12, 8, 12)));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(km.getTenKhuyenMai());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(GUI_NhanVienLeTan.ACCENT_BLUE);

        String details = String.format("Mã: %s | Giảm: %.0f%%", km.getMaKhuyenMai(), km.getChietKhau());
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailsLabel.setForeground(Color.GRAY);

        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(detailsLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtMaKhuyenMai.setText(km.getMaKhuyenMai());
                applyPromoCode();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(220, 230, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(new Color(240, 245, 255));
            }
        });

        return panel;
    }

    private void applyPromoCode() {
        String code = txtMaKhuyenMai.getText().trim();
        if (code.isEmpty()) {
            lblKmInfo.setText("Vui lòng nhập mã.");
            lblKmInfo.setForeground(Color.RED);
            return;
        }
        try {
            KhuyenMai_DAO kmDao = new KhuyenMai_DAO();
            KhuyenMai km = kmDao.getKhuyenMaiById(code);

            if (km == null) {
                lblKmInfo.setText("Mã không tồn tại.");
                lblKmInfo.setForeground(Color.RED);
                return;
            }

            // Kiểm tra tất cả điều kiện
            java.time.LocalDate today = java.time.LocalDate.now();

            // Kiểm tra ngày bắt đầu
            boolean hasNotStarted = false;
            if (km.getNgayBatDau() != null) {
                hasNotStarted = today.isBefore(km.getNgayBatDau().toLocalDate());
            }

            // Kiểm tra ngày kết thúc
            boolean isExpired = false;
            if (km.getNgayKetThuc() != null) {
                isExpired = today.isAfter(km.getNgayKetThuc().toLocalDate());
            }

            // Kiểm tra lượt sử dụng
            boolean noUsagesLeft = km.getLuotSuDung() <= 0;

            // Xử lý các trường hợp lỗi với thông báo cụ thể
            if (hasNotStarted) {
                lblKmInfo.setText("Mã chưa bắt đầu (từ " + km.getNgayBatDau() + ")");
                lblKmInfo.setForeground(Color.RED);
            } else if (isExpired) {
                lblKmInfo.setText("Mã đã hết hạn.");
                lblKmInfo.setForeground(Color.RED);
            } else if (noUsagesLeft) {
                lblKmInfo.setText("Mã đã hết lượt sử dụng.");
                lblKmInfo.setForeground(Color.RED);
            } else {
                // Tất cả điều kiện đều OK - áp dụng thành công
                String info = String.format("Áp dụng: %s (-%.0f%%)", km.getTenKhuyenMai(), km.getChietKhau());
                lblKmInfo.setText(info);
                lblKmInfo.setForeground(new Color(0, 120, 0));
                txtMaKhuyenMai.setEnabled(false);
                btnApplyKm.setText("Xóa");
                System.out.println("DEBUG applyPromoCode - Áp dụng thành công: " + km.getMaKhuyenMai() + " - "
                        + km.getChietKhau() + "%");
            }
        } catch (SQLException ex) {
            lblKmInfo.setText("Lỗi khi kiểm tra mã.");
            lblKmInfo.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void removePromoCode() {
        txtMaKhuyenMai.setEnabled(true);
        txtMaKhuyenMai.setText("");
        btnApplyKm.setText("Áp dụng");
        lblKmInfo.setText(" ");
        suggestedPromosPanel.setVisible(true);
        updateSuggestedPromos();
    }

    private void confirmBooking() throws SQLException {
        String tenKH = txtTenKhachHang.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();

        if (tenKH.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin khách hàng.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date fromDate = controller.getView().getFromDateChooser().getDate();
        Date toDate = controller.getView().getToDateChooser().getDate();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày nhận và ngày trả phòng.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> selectedServiceIds = new ArrayList<>();
        if (chkDichVuList != null) {
            for (JCheckBox chk : chkDichVuList) {
                if (chk.isSelected()) {
                    selectedServiceIds.add(chk.getName());
                }
            }
        }

        List<String> selectedRoomIdsList = new ArrayList<>();
        for (Map<String, Object> room : selectedRoomDetails) {
            selectedRoomIdsList.add(room.get("maPhong").toString());
        }

        Map<String, Object> bookingInfo = new HashMap<>();
        bookingInfo.put("tenKH", tenKH);
        bookingInfo.put("email", email);
        bookingInfo.put("sdt", sdt);
        bookingInfo.put("ngayNhan", fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        bookingInfo.put("ngayTra", toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        bookingInfo.put("yeuCau", txtYeuCau.getText().trim());
        bookingInfo.put("dichVuIds", selectedServiceIds);
        bookingInfo.put("phongIds", selectedRoomIdsList);
        // Logic lấy mã: Nếu nút là "Xóa" tức là đã áp dụng thành công -> lấy text. Nếu
        // không thì null.
        String maKM = null;
        if (btnApplyKm.getText().equals("Xóa") && !txtMaKhuyenMai.getText().trim().isEmpty()) {
            maKM = txtMaKhuyenMai.getText().trim();
        }

        // DEBUG: Log mã khuyến mãi được truyền
        System.out.println("DEBUG BookingFormDialog - Nút áp dụng text: " + btnApplyKm.getText());
        System.out.println("DEBUG BookingFormDialog - Mã KM textfield: '" + txtMaKhuyenMai.getText().trim() + "'");
        System.out.println("DEBUG BookingFormDialog - Mã KM sẽ truyền: " + (maKM != null ? maKM : "NULL"));

        bookingInfo.put("maKhuyenMai", maKM);
        bookingInfo.put("isCheckinNow", this.isCheckinNow);

        controller.handleConfirmBooking(bookingInfo);
        dispose();
    }
}