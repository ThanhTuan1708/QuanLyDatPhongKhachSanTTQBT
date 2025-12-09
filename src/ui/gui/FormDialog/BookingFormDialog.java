package ui.gui.FormDialog; // Đặt đúng package của bạn

import entity.DichVu;
import event.EventDatPhong; // Import lớp Event Controller
import dao.KhuyenMai_DAO;
import entity.KhuyenMai;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.*; // Import Map, List, Set, HashMap, ArrayList
import java.util.List;

/**
 * JDialog hiển thị form nhập thông tin đặt phòng chi tiết.
 */
public class BookingFormDialog extends JDialog {

    private EventDatPhong controller; // Tham chiếu đến lớp xử lý sự kiện
    private List<Map<String, Object>> selectedRoomDetails; // Danh sách phòng đã chọn
    private List<DichVu> dsDichVu;
    private boolean isCheckinNow;

    // Components của form
    private JTextField txtTenKhachHang, txtEmail, txtSoDienThoai;
    private JTextField txtMaKhuyenMai;
    private JButton btnApplyKm;
    private JLabel lblKmInfo;
    private JSpinner spinnerSoKhach;
    private JTextField txtNgayNhan, txtNgayTra; // !!! Tạm dùng JTextField, NÊN DÙNG JDateChooser !!!
    private JTextArea txtYeuCau;
    private List<JCheckBox> chkDichVuList; // Danh sách checkbox dịch vụ

    /**
     * Constructor cho BookingFormDialog.
     * @param owner Frame cha (JFrame chính)
     * @param selectedRoomDetails Danh sách thông tin chi tiết các phòng đã chọn
     * @param controller Tham chiếu đến lớp EventDatPhong
     */
    public BookingFormDialog(Frame owner, List<Map<String, Object>> selectedRoomDetails, List<DichVu> dsDichVu, EventDatPhong controller, boolean isCheckinNow){
        super(owner, "Thông tin đặt phòng", true); // true = Modal Dialog
        this.controller = controller;
        this.selectedRoomDetails = selectedRoomDetails;
        this.dsDichVu = dsDichVu;
        this.isCheckinNow = isCheckinNow;

        setSize(700, 650); // Điều chỉnh kích thước nếu cần
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(GUI_NhanVienLeTan.COLOR_WHITE); // Nền trắng
        // Thêm lề cho toàn bộ dialog
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Phần chính của Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        formPanel.setOpaque(false); // Lấy nền trắng từ content pane

        // 1. Hiển thị các phòng đã chọn
        formPanel.add(createSelectedRoomsPanel());
        formPanel.add(Box.createVerticalStrut(15));

        // 2. Form nhập thông tin khách hàng và ngày tháng
        formPanel.add(createInputFormPanel());
        formPanel.add(Box.createVerticalStrut(15));

        // 3. Chọn dịch vụ tùy chọn
        formPanel.add(createServiceSelectionPanel());

        // --- Nút bấm dưới cùng (Hủy, Xác nhận) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnCancel = new JButton("Hủy");
        JButton btnConfirm = new JButton("Xác nhận đặt phòng");
        btnConfirm.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE); // Màu xanh dương
        btnConfirm.setForeground(Color.WHITE);
        // Định dạng nút bấm cho đẹp hơn
        btnCancel.setFocusPainted(false);
        btnConfirm.setFocusPainted(false);
        btnCancel.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnConfirm.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Gắn sự kiện
        btnCancel.addActionListener(e -> dispose()); // Đóng dialog khi nhấn Hủy
        btnConfirm.addActionListener(e -> {
            try {
                confirmBooking();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }); // Gọi hàm xử lý xác nhận

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);

        // Thêm các thành phần vào Dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Sự kiện cho nút Áp dụng mã khuyến mãi ---
        if (btnApplyKm != null) {
            btnApplyKm.addActionListener(e -> {
                String code = (txtMaKhuyenMai != null) ? txtMaKhuyenMai.getText().trim() : "";
                if (code == null || code.isEmpty()) {
                    lblKmInfo.setText("Vui lòng nhập mã");
                    lblKmInfo.setForeground(Color.RED);
                    return;
                }
                try {
                    KhuyenMai_DAO kmDao = new KhuyenMai_DAO();
                    // DEBUG: list available codes to console to verify which codes the app sees
                    java.util.List<KhuyenMai> all = kmDao.getAllKhuyenMai();
                    System.out.println("Available KM count=" + all.size());
                    for (KhuyenMai k : all) System.out.println("KM -> '" + k.getMaKhuyenMai() + "' : '" + k.getTenKhuyenMai() + "'");

                    KhuyenMai km = kmDao.getKhuyenMaiById(code);
                    if (km == null) {
                        lblKmInfo.setText("Mã không tồn tại"); lblKmInfo.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(this, "Mã khuyến mãi không tồn tại.", "Mã KM", JOptionPane.WARNING_MESSAGE);
                        // Show a popup with available promo codes so user can select/see them
                        try {
                            if (all != null && !all.isEmpty()) {
                                showPromoListDialog(all);
                            } else {
                                JOptionPane.showMessageDialog(this, "Không có khuyến mãi đang có sẵn.", "Danh sách KM", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception exDlg) {
                            exDlg.printStackTrace();
                        }
                        return;
                    }
                    boolean valid = true;
                    java.sql.Date nb = km.getNgayBatDau();
                    java.sql.Date nk = km.getNgayKetThuc();
                    java.time.LocalDate today = java.time.LocalDate.now();
                    if (nb != null && today.isBefore(nb.toLocalDate())) valid = false;
                    if (nk != null && today.isAfter(nk.toLocalDate())) valid = false;
                    if (km.getLuotSuDung() <= 0) valid = false;

                    if (!valid) {
                        lblKmInfo.setText("Mã không hợp lệ/đã hết hạn"); lblKmInfo.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(this, "Mã khuyến mãi không hợp lệ hoặc đã hết hạn/đã dùng hết lượt.", "Mã KM", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String info = String.format("%s (%.2f%%) — %s → %s",
                                km.getTenKhuyenMai(), km.getChietKhau(),
                                (km.getNgayBatDau() != null ? km.getNgayBatDau().toString() : "-"),
                                (km.getNgayKetThuc() != null ? km.getNgayKetThuc().toString() : "-")
                        );
                        lblKmInfo.setText(info); lblKmInfo.setForeground(new Color(0, 100, 0));
                        // Disable inputs after successful apply to avoid re-applying accidentally
                        txtMaKhuyenMai.setEnabled(false);
                        btnApplyKm.setEnabled(false);
                        JOptionPane.showMessageDialog(this, "Mã khuyến mãi hợp lệ: " + info, "Mã KM", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblKmInfo.setText("Lỗi kiểm tra"); lblKmInfo.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra mã khuyến mãi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    /**
     * Hiển thị một dialog danh sách các mã khuyến mãi hiện có.
     */
    private void showPromoListDialog(java.util.List<KhuyenMai> promos) {
        if (promos == null) promos = java.util.Collections.emptyList();
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Danh sách mã khuyến mãi", true);
        dlg.setLayout(new BorderLayout(10,10));
        String[] cols = new String[] {"Mã", "Tên", "Chiết khấu(%)", "Ngày bắt đầu", "Ngày kết thúc", "Lượt"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (KhuyenMai k : promos) {
            String nb = (k.getNgayBatDau() != null) ? k.getNgayBatDau().toString() : "-";
            String nk = (k.getNgayKetThuc() != null) ? k.getNgayKetThuc().toString() : "-";
            model.addRow(new Object[] { k.getMaKhuyenMai(), k.getTenKhuyenMai(), k.getChietKhau(), nb, nk, k.getLuotSuDung() });
        }
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(table);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        JButton btnCopy = new JButton("Sao chép mã");
        btnClose.addActionListener(e -> dlg.dispose());
        btnCopy.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                Object code = model.getValueAt(r, 0);
                if (code != null) {
                    String s = code.toString();
                    txtMaKhuyenMai.setText(s);
                    lblKmInfo.setText("Mã đã sao chép: " + s);
                    lblKmInfo.setForeground(new Color(0,100,0));
                    dlg.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn một mã trong danh sách.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            }
        });
        btns.add(btnCopy); btns.add(btnClose);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(700, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Tạo panel hiển thị thông tin tóm tắt các phòng đã chọn.
     * @return JPanel chứa danh sách phòng đã chọn.
     */
    private JPanel createSelectedRoomsPanel() {
        JPanel panel = new JPanel(); // Dùng FlowLayout mặc định, sẽ tự xuống dòng
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5)); // Căn trái, khoảng cách ngang/dọc
        panel.setOpaque(true); // Có nền riêng
        panel.setBackground(new Color(240, 245, 255)); // Nền xanh nhạt
        // Thêm viền tiêu đề
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GUI_NhanVienLeTan.CARD_BORDER), // Viền ngoài
                " Các phòng đã chọn (" + selectedRoomDetails.size() + ") ", // Tiêu đề
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 13), // Font tiêu đề
                GUI_NhanVienLeTan.ACCENT_BLUE)); // Màu tiêu đề

        if (selectedRoomDetails.isEmpty()) {
            panel.add(new JLabel("Chưa có phòng nào được chọn."));
        } else {
            // Hiển thị mỗi phòng như một "chip" nhỏ
            for (Map<String, Object> room : selectedRoomDetails) {
                JPanel roomChip = new JPanel(); // FlowLayout mặc định
                roomChip.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2)); // Khoảng cách nhỏ bên trong chip
                roomChip.setOpaque(false); // Nền trong suốt
                roomChip.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Viền cho chip

                // Icon placeholder (có thể thay bằng ảnh nếu muốn)
                JLabel iconLabel = new JLabel(" P "); // Dùng ký tự đơn giản
                iconLabel.setOpaque(true);
                iconLabel.setBackground(Color.WHITE);
                iconLabel.setForeground(GUI_NhanVienLeTan.ACCENT_BLUE);
                iconLabel.setBorder(new EmptyBorder(1,3,1,3));

                // Thông tin phòng (Mã - Loại)
                JLabel roomLabel = new JLabel(room.get("maPhong") + " (" + room.get("tenLoaiPhong") + ")");
                roomLabel.setFont(roomLabel.getFont().deriveFont(11f)); // Font nhỏ hơn

                roomChip.add(iconLabel);
                roomChip.add(roomLabel);
                panel.add(roomChip); // Thêm chip vào panel chính
            }
        }
        return panel;
    }

    /**
     * Tạo panel chứa các trường nhập liệu thông tin khách hàng và ngày đặt phòng.
     * Sử dụng GridBagLayout để căn chỉnh.
     * @return JPanel chứa form nhập liệu.
     */
    private JPanel createInputFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); // Nền trong suốt
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các thành phần
        gbc.fill = GridBagConstraints.HORIZONTAL; // Kéo dài theo chiều ngang
        gbc.anchor = GridBagConstraints.WEST;     // Căn lề trái

        // --- Hàng 1: Tên Khách Hàng, Email ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; // Cột label
        form.add(new JLabel("Tên khách hàng *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4; // Cột input (chiếm 40%)
        txtTenKhachHang = new JTextField(); form.add(txtTenKhachHang, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; // Cột label
        form.add(new JLabel("Email *"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.4; // Cột input
        txtEmail = new JTextField(); form.add(txtEmail, gbc);

        // --- Hàng 2: Số Điện Thoại, Số Khách ---
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Số điện thoại *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtSoDienThoai = new JTextField(); form.add(txtSoDienThoai, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        form.add(new JLabel("Số khách *"), gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        // JSpinner cho phép chọn số khách, giá trị min dựa trên phòng nhỏ nhất
        spinnerSoKhach = new JSpinner(new SpinnerNumberModel(calculateMinCapacity(), 1, 100, 1));
        form.add(spinnerSoKhach, gbc);

        // --- Hàng 3: Ngày Nhận Phòng, Ngày Trả Phòng ---
        // !!! LƯU Ý: NÊN SỬ DỤNG JDateChooser thay cho JTextField để chọn ngày dễ dàng hơn !!!
        // Cần thêm thư viện JCalendar (jcalendar-....jar) vào project
        // Ví dụ với JDateChooser:
        // com.toedter.calendar.JDateChooser dateChooserNhan = new com.toedter.calendar.JDateChooser();
        // dateChooserNhan.setDateFormatString("dd/MM/yyyy");
        // form.add(dateChooserNhan, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Ngày nhận phòng *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtNgayNhan = new JTextField("dd/MM/yyyy"); form.add(txtNgayNhan, gbc); // Tạm dùng JTextField

        gbc.gridx = 2; gbc.gridy = 2;
        form.add(new JLabel("Ngày trả phòng *"), gbc);
        gbc.gridx = 3; gbc.gridy = 2;
        txtNgayTra = new JTextField("dd/MM/yyyy"); form.add(txtNgayTra, gbc);   // Tạm dùng JTextField

    // --- Hàng 4: Mã khuyến mãi (tùy chọn) ---
    gbc.gridx = 0; gbc.gridy = 3;
    form.add(new JLabel("Mã khuyến mãi"), gbc);
    gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.6; // Cho ô input rộng hơn
    txtMaKhuyenMai = new JTextField(); form.add(txtMaKhuyenMai, gbc);

    gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.1;
    btnApplyKm = new JButton("Áp dụng");
    btnApplyKm.setCursor(new Cursor(Cursor.HAND_CURSOR));
    form.add(btnApplyKm, gbc);

    gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.3;
    lblKmInfo = new JLabel(" "); // sẽ hiển thị tên KM / % giảm khi áp dụng
    lblKmInfo.setForeground(new Color(0, 100, 0));
    form.add(lblKmInfo, gbc);

    // --- Hàng 5: Yêu Cầu Đặc Biệt ---
    gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.1;
    form.add(new JLabel("Yêu cầu đặc biệt"), gbc);
    gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 3; // Kéo dài ô nhập qua 3 cột còn lại
    txtYeuCau = new JTextArea(3, 20); // 3 hàng, 20 cột (chiều rộng sẽ tự điều chỉnh)
    txtYeuCau.setLineWrap(true);      // Tự động xuống dòng
    txtYeuCau.setWrapStyleWord(true); // Xuống dòng tại từ
    JScrollPane scrollYeuCau = new JScrollPane(txtYeuCau); // Thêm thanh cuộn nếu cần
    form.add(scrollYeuCau, gbc);
    gbc.gridwidth = 1; // Reset lại gridwidth cho các thành phần sau

        return form;
    }

    /**
     * Tính số khách tối thiểu cho phép dựa trên phòng có sức chứa nhỏ nhất đã chọn.
     * @return Số khách tối thiểu.
     */
    private int calculateMinCapacity() {
        int min = 1; // Mặc định là 1
        if (selectedRoomDetails != null && !selectedRoomDetails.isEmpty()) {
            min = Integer.MAX_VALUE;
            for (Map<String, Object> room : selectedRoomDetails) {
                try {
                    // Lấy giá trị 'soChua' từ Map, chuyển đổi và so sánh
                    int capacity = (int) room.getOrDefault("soChua", 2); // Giả sử key là 'soChua', mặc định là 2
                    if (capacity < min) {
                        min = capacity;
                    }
                } catch (Exception e) {
                    // Bỏ qua nếu có lỗi chuyển đổi kiểu
                    System.err.println("Lỗi khi đọc sức chứa phòng: " + room.get("maPhong"));
                }
            }
        }
        return Math.max(1, min); // Đảm bảo số khách tối thiểu là 1
    }


    /**
     * Tạo panel chứa danh sách các dịch vụ tùy chọn (dùng JCheckBox).
     * @return JPanel chứa các checkbox dịch vụ.
     */
    private JPanel createServiceSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10)); // Grid 2 cột, tự động thêm hàng
        panel.setOpaque(false); // Nền trong suốt
        panel.setBorder(BorderFactory.createTitledBorder("Chọn dịch vụ (tùy chọn)")); // Viền tiêu đề

        chkDichVuList = new ArrayList<>(); // Khởi tạo list để lưu các checkbox

        // --- Lấy danh sách dịch vụ từ CSDL (Placeholder) ---
        List<Map<String, Object>> services = getAvailableServices();
        // ---------------------------------------------------

        for (Map<String, Object> service : services) {
            // Lấy thông tin dịch vụ
            String name = (String) service.get("ten");
            String price = (String) service.get("gia");
            String duration = (String) service.get("thoiGian");
            String id = (String) service.get("id"); // Cần ID để xác định dịch vụ nào được chọn

            // Tạo JCheckBox với HTML để hiển thị nhiều dòng
            JCheckBox chk = new JCheckBox("<html><b>" + name + "</b><br>" + price + " - " + duration + "</html>");
            chk.setOpaque(false); // Nền trong suốt
            chk.setName(id); // Lưu ID vào thuộc tính 'name' của checkbox để lấy lại khi submit
            chkDichVuList.add(chk); // Thêm vào danh sách để xử lý sau
            panel.add(chk);        // Thêm vào panel lưới
        }

        return panel;
    }

    /**
     * Placeholder: Hàm này cần lấy danh sách dịch vụ từ CSDL.
     * @return Danh sách các Map chứa thông tin dịch vụ (id, ten, gia, thoiGian).
     */
    private List<Map<String, Object>> getAvailableServices() {
        System.out.println("!!! LẤY DANH SÁCH DỊCH VỤ TỪ CSDL !!!");
        // Thay thế bằng code truy vấn CSDL bảng DichVu
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", "DV001", "ten", "Spa & Massage", "gia", "150.000 đ", "thoiGian", "90 phút"));
        list.add(Map.of("id", "DV002", "ten", "Nhà hàng cao cấp", "gia", "250.000 đ", "thoiGian", "120 phút"));
        list.add(Map.of("id", "DV003", "ten", "Xe đưa đón sân bay", "gia", "300.000 đ", "thoiGian", "60 phút"));
        list.add(Map.of("id", "DV004", "ten", "Phòng gym & fitness", "gia", "Miễn phí", "thoiGian", "120 phút")); // Ví dụ miễn phí
        list.add(Map.of("id", "DV005", "ten", "Dịch vụ giặt ủi", "gia", "50.000 đ", "thoiGian", "240 phút"));
        list.add(Map.of("id", "DV006", "ten", "Room service", "gia", "Theo món", "thoiGian", "30 phút")); // Ví dụ giá khác
        return list;
    }

    /**
     * Xử lý sự kiện khi nhấn nút "Xác nhận đặt phòng".
     * Thu thập dữ liệu, validate, đóng gói và gọi Controller.
     */
    private void confirmBooking() throws SQLException {
        // --- Thu thập dữ liệu từ các trường input ---
        String tenKH = txtTenKhachHang.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        int soKhach = (int) spinnerSoKhach.getValue();
        String ngayNhan = txtNgayNhan.getText(); // Cần parse và validate ngày tháng
        String ngayTra = txtNgayTra.getText();   // Cần parse và validate ngày tháng
        String yeuCau = txtYeuCau.getText().trim();

        // Lấy danh sách ID các dịch vụ đã được chọn
        List<String> selectedServiceIds = new ArrayList<>();
        if (chkDichVuList != null) {
            for (JCheckBox chk : chkDichVuList) {
                if (chk.isSelected()) {
                    selectedServiceIds.add(chk.getName()); // Lấy ID đã lưu trong thuộc tính name
                }
            }
        }

        // Lấy danh sách ID các phòng đã chọn (từ selectedRoomDetails)
        List<String> selectedRoomIdsList = new ArrayList<>();
        if (selectedRoomDetails != null) {
            for (Map<String, Object> room : selectedRoomDetails) {
                selectedRoomIdsList.add(room.get("maPhong").toString());
            }
        }

        // --- Validate dữ liệu (Ví dụ đơn giản) ---
        if (tenKH.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc (*).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return; // Dừng lại nếu thiếu
        }
        // !!! Thêm validate chi tiết hơn cho email, sđt, ngày tháng ở đây !!!
        // Ví dụ kiểm tra định dạng email cơ bản:
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Định dạng email không hợp lệ.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Ví dụ kiểm tra số điện thoại (đơn giản):
        if (!sdt.matches("^\\+?[0-9\\s]{10,}$")) {
            JOptionPane.showMessageDialog(this, "Định dạng số điện thoại không hợp lệ.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Kiểm tra ngày nhận/trả (cần parse sang Date/LocalDate để so sánh)


        // --- Đóng gói dữ liệu vào Map để gửi cho Controller ---
        Map<String, Object> bookingInfo = new HashMap<>();
        bookingInfo.put("tenKH", tenKH);
        bookingInfo.put("email", email);
        bookingInfo.put("sdt", sdt);
        bookingInfo.put("soKhach", soKhach);
        bookingInfo.put("ngayNhan", ngayNhan); // Gửi dạng String, Controller/DAO sẽ parse
        bookingInfo.put("ngayTra", ngayTra);   // Gửi dạng String
        bookingInfo.put("yeuCau", yeuCau);
        bookingInfo.put("dichVuIds", selectedServiceIds); // Danh sách ID dịch vụ đã chọn
        bookingInfo.put("phongIds", selectedRoomIdsList); // Danh sách ID phòng đã chọn
        String maKhuyenMai = (txtMaKhuyenMai != null) ? txtMaKhuyenMai.getText().trim() : null;
        bookingInfo.put("maKhuyenMai", (maKhuyenMai == null || maKhuyenMai.isEmpty()) ? null : maKhuyenMai);
        bookingInfo.put("isCheckinNow", this.isCheckinNow);
        // --- Gọi Controller để xử lý việc lưu trữ ---
        controller.handleConfirmBooking(bookingInfo);

        // Đóng dialog sau khi đã gọi controller (thành công hay thất bại controller sẽ thông báo)
        dispose();
    }

} // Kết thúc lớp BookingFormDialog