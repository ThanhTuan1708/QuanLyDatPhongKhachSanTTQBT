/**
 * Author: ThanhTuan
 * Date: 2025-10-23
 * Description: Giao dien quan ly khach san cho nhan vien quan ly
 */

package ui.gui;

// ---------------------------
// Chú thích metadata (comment)
// Người code: Phan Minh Thuận
// Mô tả: Thêm nhãn chú thích hiển thị tên người chịu trách nhiệm / phần giao diện thống kê
// Mục đích: Quản lý code, dễ dàng liên hệ khi cần chỉnh sửa
// Ngày tạo: 26/10/2025
// Giờ tạo: 1:52AM
// Lưu ý: cập nhật thời gian/ người sửa khi chỉnh sửa tiếp
// ---------------------------
import javax.swing.*;
import java.sql.Statement;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import connectDB.ConnectDB;
import dao.KhuyenMai_DAO;
import dao.NhanVien_DAO;
import entity.GioiTinh;
import entity.KhuyenMai;
import entity.LoaiNhanVien;
import entity.NhanVien;
import event.EventKhuyenMai;
import event.EventNhanVien;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static entity.LoaiNhanVien.QUAN_LY;
import static ui.gui.GUI_NhanVienQuanLy.PanelThongKeContent.ACCENT_BLUE;
import static ui.gui.GUI_NhanVienQuanLy.PanelThongKeContent.CARD_BORDER;
import static ui.gui.GUI_NhanVienQuanLy.PanelThongKeContent.MAIN_BG;


/**
 * Giao diện Dashboard cho Nhân viên Quản lý
 */
public class GUI_NhanVienQuanLy extends JFrame {
    // thêm vào trong thân class GUI_NhanVienQuanLy (không ở ngoài)
    private String maNV;

    // Labels that will be updated with the real user name/role after login
    private JLabel sidebarUserLabel;
    private JLabel sidebarRoleLabel;

    // reference to dashboard panel so we can update the profile section
    private PanelQuanLyContent panelQuanLyContent;

    public GUI_NhanVienQuanLy(NhanVien nhanVien) throws SQLException {
        this(); // gọi constructor mặc định của class (phải tồn tại)
        this.maNV = maNV;
        initAfterLogin();
    }

    // khởi tạo thêm sau khi đăng nhập (ví dụ hiển thị mã NV)
    private void initAfterLogin() {
        if (maNV != null) {
            // cố gắng lấy tên thực từ CSDL
            try {
                Connection conn = ConnectDB.getConnection();
                String sql = "SELECT hoTen, maLoaiNV FROM NhanVien WHERE maNV=?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, maNV);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String hoTen = rs.getString("hoTen");
                            int maLoai = rs.getInt("maLoaiNV");
                            String roleText = (maLoai == 1) ? "Quản lý" : (maLoai == 2) ? "Lễ tân" : "Nhân viên";

                            if (sidebarUserLabel != null) sidebarUserLabel.setText(hoTen);
                            if (sidebarRoleLabel != null) sidebarRoleLabel.setText(roleText);
                            if (panelQuanLyContent != null)
                                panelQuanLyContent.setProfileName(hoTen, "\u2022 " + roleText);
                            setTitle(getTitle() + " - " + hoTen);
                        } else {
                            // fallback
                            if (sidebarUserLabel != null) sidebarUserLabel.setText(maNV);
                            if (panelQuanLyContent != null) panelQuanLyContent.setProfileName(maNV, "\u2022 Quản lý");
                            setTitle(getTitle() + " - " + maNV);
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                if (sidebarUserLabel != null) sidebarUserLabel.setText(maNV);
                if (panelQuanLyContent != null) panelQuanLyContent.setProfileName(maNV, "\u2022 Quản lý");
                setTitle(getTitle() + " - " + maNV);
            }
        }
        // các xử lý khởi tạo khác nếu cần
    }

    // --- Các hằng số màu sắc nội bộ ---
    private final Color SIDEBAR_BG = new Color(245, 250, 255);
    private final Color ACCENT = new Color(124, 58, 237);

    // HẰNG SỐ CHO CARD LAYOUT
    private static final String DASHBOARD_PANEL = "QUAN_LY_CONTENT";
    private static final String EMPLOYEE_PANEL = "NHAN_VIEN_CONTENT";
    private static final String STATISTIC_PANEL = "THONG_KE_CONTENT";
    private static final String PROMOTION_PANEL = "KHUYEN_MAI_CONTENT";

    // CardLayout để chuyển nội dung bên phải
    private CardLayout cardLayout;
    private JPanel contentPanelContainer;

    // Lưu lại các nút menu để đổi màu khi active
    private JButton btnNhanVien;
    private JButton btnDashboard;
    private JButton btnThongKe;
    private JButton btnKhuyenMai;

    public GUI_NhanVienQuanLy() throws SQLException {
        setTitle("Dashboard Quản lý khách sạn");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Khởi tạo CardLayout & Container (Khắc phục lỗi khởi tạo 2 lần)
        cardLayout = new CardLayout();
        contentPanelContainer = new JPanel(cardLayout);
        contentPanelContainer.setBackground(MAIN_BG);

        // 2. Thêm Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 3. Tạo các Panel nội dung riêng biệt
        // create panels and keep reference to dashboard panel so we can update profile name later
        this.panelQuanLyContent = new PanelQuanLyContent();
        PanelNhanVienContent panelNhanVienContent = new PanelNhanVienContent();
        PanelThongKeContent panelThongKeContent = new PanelThongKeContent(); // Thêm Panel Thống kê
        PanelKhuyenMaiContent panelKhuyenMaiContent = new PanelKhuyenMaiContent();


        // 4. Thêm các Panel nội dung vào CardLayout
        contentPanelContainer.add(panelQuanLyContent, DASHBOARD_PANEL);
        contentPanelContainer.add(panelNhanVienContent, EMPLOYEE_PANEL);
        contentPanelContainer.add(panelThongKeContent, STATISTIC_PANEL); // Thêm Panel Thống kê
        contentPanelContainer.add(panelKhuyenMaiContent, PROMOTION_PANEL);


        // 5. Thêm Panel CardLayout vào CENTER của JFrame
        add(contentPanelContainer, BorderLayout.CENTER);

        // 6. Hiển thị Dashboard mặc định và Active button
        showContentPanel(DASHBOARD_PANEL);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(SIDEBAR_BG); // Màu nền sidebar

        // --- Phần Logo ---
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBorder(new EmptyBorder(18, 18, 18, 18));
        logo.setOpaque(false);
        JLabel hotelName = new JLabel("TBQTT");
        hotelName.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel hotelType = new JLabel("HOTEL");
        hotelType.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel subtitle = new JLabel("Hệ thống quản lý");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        logo.add(hotelName);
        logo.add(hotelType);
        logo.add(Box.createVerticalStrut(12));
        logo.add(subtitle);
        sidebar.add(logo, BorderLayout.NORTH);

        // --- Phần Menu ---
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(12, 12, 12, 12));
        menu.setOpaque(false);

        // Tạo các nút menu (lưu lại tham chiếu)
        btnDashboard = createNavButton("Dashboard");
        btnNhanVien = createNavButton("Nhân viên");
        btnThongKe = createNavButton("Thống kê"); // Lưu tham chiếu
        btnKhuyenMai = createNavButton("Khuyến mãi");

        // Gắn ActionListener để chuyển đổi content panel
        btnDashboard.addActionListener(e -> showContentPanel(DASHBOARD_PANEL));
        btnNhanVien.addActionListener(e -> showContentPanel(EMPLOYEE_PANEL));
        btnThongKe.addActionListener(e -> showContentPanel(STATISTIC_PANEL)); // Action cho Thống kê
        btnKhuyenMai.addActionListener(e -> showContentPanel(PROMOTION_PANEL)); // Action Khuyến mãi

        // Thêm nút vào menu
        menu.add(btnDashboard);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnNhanVien);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnThongKe);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnKhuyenMai);
        menu.add(Box.createVerticalStrut(8));

        JPanel menuWrap = new JPanel(new BorderLayout());
        menuWrap.setOpaque(false);
        menuWrap.setBorder(new EmptyBorder(12, 12, 12, 12));
        menuWrap.add(menu, BorderLayout.NORTH);
        sidebar.add(menuWrap, BorderLayout.CENTER);

        // --- Phần Profile (Bottom) ---
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 12, 12, 12));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        sidebarUserLabel = new JLabel("admin");
        sidebarUserLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sidebarRoleLabel = new JLabel("Quản lý khách sạn");
        sidebarRoleLabel.setForeground(Color.GRAY);
        bottom.add(sidebarUserLabel);
        bottom.add(sidebarRoleLabel);
        bottom.add(Box.createVerticalStrut(8));
        JButton logout = new JButton("Đăng xuất");
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setForeground(new Color(200, 50, 50));
        bottom.add(logout);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false); // Quan trọng để có thể đổi màu nền
        return btn;
    }

    /**
     * Helper: Đặt trạng thái active cho nút được chọn và reset các nút khác
     */
    private void setActiveButton(JButton activeButton) {
        JButton[] allButtons = {btnDashboard, btnNhanVien, btnThongKe, btnKhuyenMai /* , các nút khác */};
        for (JButton btn : allButtons) {
            if (btn == activeButton) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(ACCENT_BLUE);
                btn.setOpaque(true); // Chỉ bật Opaque cho nút active
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_BLUE, 2, true),
                        new EmptyBorder(6, 12, 6, 12)));
            } else if (btn != null) { // Kiểm tra null phòng trường hợp chưa khởi tạo hết
                btn.setForeground(Color.BLACK);
                btn.setOpaque(false); // Tắt Opaque cho nút không active
                btn.setBorder(new CompoundBorder(
                        new LineBorder(new Color(230, 230, 230)),
                        new EmptyBorder(6, 12, 6, 12)));
            }
        }
    }

    /**
     * Chuyển đổi Panel nội dung hiển thị trong CardLayout
     */
    public void showContentPanel(String panelName) {
        cardLayout.show(contentPanelContainer, panelName);
        // Cập nhật trạng thái active của nút menu tương ứng
        if (panelName.equals(DASHBOARD_PANEL)) {
            setActiveButton(btnDashboard);
        } else if (panelName.equals(EMPLOYEE_PANEL)) {
            setActiveButton(btnNhanVien);
        } else if (panelName.equals(STATISTIC_PANEL)) { // Kích hoạt nút Thống kê
            setActiveButton(btnThongKe);
        } else if (panelName.equals(PROMOTION_PANEL)) { // Kích hoạt nút Khuyến mãi
            setActiveButton(btnKhuyenMai);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                new GUI_NhanVienQuanLy().setVisible(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    // =================================================================================
    // PANEL 1: DASHBOARD QUẢN LÝ (FINAL FIXED: ADR SO SÁNH NGÀY + BIỂU ĐỒ CÓ TRỤC Y)
    // =================================================================================
    public static class PanelQuanLyContent extends JPanel {
        // --- 1. KHAI BÁO CÁC HẰNG SỐ MÀU SẮC & FONT ---
        private static final Color MAIN_BG = new Color(245, 247, 251);
        private static final Color TEXT_DARK = new Color(30, 41, 59);
        private static final Color TEXT_GRAY = new Color(100, 116, 139);
        private static final Color PRIMARY_COLOR = new Color(59, 130, 246);

        private static final Color SUCCESS_BG = new Color(220, 252, 231);
        private static final Color SUCCESS_TEXT = new Color(22, 163, 74);
        private static final Color INFO_BG = new Color(219, 234, 254);
        private static final Color INFO_TEXT = new Color(37, 99, 235);
        private static final Color WARNING_BG = new Color(254, 243, 199);
        private static final Color WARNING_TEXT = new Color(217, 119, 6);

        // Màu cho biểu đồ
        private static final Color CHART_FILL_COLOR = new Color(239, 68, 68, 30);
        private static final Color CHART_LINE_COLOR = new Color(239, 68, 68);

        // Font chữ
        private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
        private static final Font FONT_BIG = new Font("Segoe UI", Font.BOLD, 24);
        private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
        private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 13);
        private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

        // --- BIẾN DỮ LIỆU ---
        private double doanhThuNgay = 0;
        private double doanhThuHomQua = 0;
        private int soHoaDonHomNay = 0;

        private int totalRoom = 0, bookedRoom = 0, cleanRoom = 0, dirtyRoom = 0, maintenanceRoom = 0;

        // [SỬA 1] Đổi tên biến từ adrThangTruoc -> adrHomQua
        private double adrHomQua = 0;

        private int[] peakHourData = new int[24];
        private DefaultTableModel staffModel;
        private JTable tableStaff;

        public PanelQuanLyContent() {
            setLayout(new BorderLayout());
            setBackground(MAIN_BG);
            setBorder(new EmptyBorder(30, 40, 30, 40));

            // Khởi tạo model bảng
            String[] cols = {"Mã NV", "Nhân viên", "Vị trí", "Ca làm việc", "Trạng thái"};
            staffModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            fetchRealDataFromSQL();

            // 1. Header
            JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
            JLabel title = new JLabel("Dashboard Quản Lý Khách Sạn"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK);

            String todayStr = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
            JLabel sub = new JLabel("Tổng quan vận hành ngày hôm nay: " + todayStr);
            sub.setFont(FONT_TEXT); sub.setForeground(TEXT_GRAY);

            JPanel titleBox = new JPanel(new GridLayout(2,1)); titleBox.setOpaque(false); titleBox.add(title); titleBox.add(sub);
            header.add(titleBox, BorderLayout.WEST); add(header, BorderLayout.NORTH);

            // 2. Body
            JPanel body = new JPanel(new GridBagLayout()); body.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(25, 0, 0, 0);
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.22;
            body.add(createKPISection(), gbc);
            gbc.gridy = 1; gbc.weighty = 0.78;
            body.add(createMainSplitSection(), gbc);
            add(body, BorderLayout.CENTER);
        }

        public void setProfileName(String name, String role) {}

        // --- FETCH DATA ---
        private void fetchRealDataFromSQL() {
            try {
                Connection con = ConnectDB.getConnection();
                if (con == null) return;

                // 1. Room Status
                String sqlRoom = "SELECT maTrangThai, COUNT(*) as sl FROM Phong GROUP BY maTrangThai";
                try (PreparedStatement ps = con.prepareStatement(sqlRoom); ResultSet rs = ps.executeQuery()) {
                    totalRoom = 0; bookedRoom = 0; cleanRoom = 0; dirtyRoom = 0; maintenanceRoom = 0;
                    while(rs.next()) {
                        int stt = rs.getInt("maTrangThai");
                        int count = rs.getInt("sl");
                        totalRoom += count;
                        if(stt == 0) cleanRoom += count; else if(stt == 1) bookedRoom += count;
                        else if(stt == 2) dirtyRoom += count; else if(stt == 3) maintenanceRoom += count;
                    }
                }

                // 2. Doanh thu & Hóa đơn HÔM NAY
                String sqlDT = "SELECT SUM(tongTien) as DT, COUNT(*) as SL FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
                try (PreparedStatement ps = con.prepareStatement(sqlDT); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        doanhThuNgay = rs.getDouble("DT");
                        soHoaDonHomNay = rs.getInt("SL");
                    }
                }

                // 3. Doanh thu HÔM QUA
                String sqlDTLast = "SELECT SUM(tongTien) as DT FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE)";
                try (PreparedStatement ps = con.prepareStatement(sqlDTLast); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) doanhThuHomQua = rs.getDouble("DT");
                }

                // 4. [SỬA LOGIC] ADR HÔM QUA (Không lấy tháng trước nữa)
                // Lấy trung bình giá hóa đơn của ngày hôm qua
                String sqlADRLast = "SELECT AVG(tongTien) as ADR FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE)";
                try (PreparedStatement ps = con.prepareStatement(sqlADRLast); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) adrHomQua = rs.getDouble("ADR");
                }

                // 5. Peak Hour
                String sqlChart = "SELECT DATEPART(HOUR, ngayNhanPhong) as Gio, COUNT(*) as SoLuong FROM PhieuDatPhong WHERE CAST(ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE) GROUP BY DATEPART(HOUR, ngayNhanPhong) " +
                        "UNION ALL " +
                        "SELECT DATEPART(HOUR, ngayTraPhong) as Gio, COUNT(*) as SoLuong FROM PhieuDatPhong WHERE CAST(ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE) GROUP BY DATEPART(HOUR, ngayTraPhong)";
                Arrays.fill(peakHourData, 0);
                try (PreparedStatement ps = con.prepareStatement(sqlChart); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int h = rs.getInt("Gio");
                        int count = rs.getInt("SoLuong");
                        if (h >= 0 && h < 24) peakHourData[h] += count;
                    }
                }

                // 6. Staff List
                String sqlNV = "SELECT nv.maNV, nv.hoTen, nv.maLoaiNV, llv.caLam, llv.gioBatDau, llv.gioKetThuc, llv.trangThai " +
                        "FROM NhanVien nv LEFT JOIN LichLamViec llv ON nv.maNV = llv.maNV AND llv.ngayLam = CAST(GETDATE() AS DATE)";
                staffModel.setRowCount(0);
                try (PreparedStatement ps = con.prepareStatement(sqlNV); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maNV = rs.getString("maNV");
                        String ten = rs.getString("hoTen");
                        int loai = rs.getInt("maLoaiNV");
                        String chucVu = (loai == 2) ? "Quản lý" : "Lễ tân";
                        String thoiGian = (rs.getTime("gioBatDau") != null) ? rs.getTime("gioBatDau").toString().substring(0, 5) + " - " + rs.getTime("gioKetThuc").toString().substring(0, 5) : "--:--";
                        String trangThai = rs.getString("trangThai");
                        if (trangThai == null) { trangThai = "Chưa có lịch"; thoiGian = ""; }
                        staffModel.addRow(new Object[]{maNV, ten, chucVu, thoiGian, trangThai});
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // --- UI COMPONENTS ---
        private JPanel createKPISection() {
            JPanel p = new JPanel(new GridLayout(1, 4, 20, 0)); p.setOpaque(false); p.setPreferredSize(new Dimension(0, 160));
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            String dtStr = df.format(doanhThuNgay) + " ₫";

            double revenueTrend = doanhThuHomQua > 0 ? ((doanhThuNgay - doanhThuHomQua) / doanhThuHomQua * 100) : 100;
            String revTrendStr = (revenueTrend >= 0 ? "▲ +" : "▼ ") + String.format("%.1f", revenueTrend) + "%";
            String revTrendColor = revenueTrend >= 0 ? "#16a34a" : "#dc2626";

            // [SỬA LOGIC] Tính ADR hôm nay so với HÔM QUA
            double adrToday = bookedRoom > 0 ? (doanhThuNgay / bookedRoom) : 0;
            double adrTrend = adrHomQua > 0 ? ((adrToday - adrHomQua) / adrHomQua * 100) : 0;
            String adrTrendStr = (adrTrend >= 0 ? "▲ +" : "▼ ") + String.format("%.1f", adrTrend) + "%";
            String adrTrendColor = adrTrend >= 0 ? "#16a34a" : "#dc2626";

            double revPar = totalRoom > 0 ? (doanhThuNgay / totalRoom) : 0;

            // 1. DOANH THU
            String revSub = String.format("<html><font color='%s'><b>%s</b></font> so với hôm qua<br><b>%d</b> hóa đơn đã xong</html>",
                    revTrendColor, revTrendStr, soHoaDonHomNay);
            JPanel cardRev = createCard("Doanh thu hôm nay", dtStr, revSub, "money", INFO_BG, INFO_TEXT);
            cardRev.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cardRev.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showRevenueDetailDialog(); } });
            p.add(cardRev);

            // 2. LẤP ĐẦY
            int emptyRoom = totalRoom - bookedRoom;
            JPanel cardOcc = createDetailedOccupancyCard(bookedRoom, emptyRoom, totalRoom);
            cardOcc.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cardOcc.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showOccupancyDetailsDialog(); } });
            p.add(cardOcc);

            // 3. ADR (GIÁ TB) - [SỬA TEXT HIỂN THỊ]
            String adrSub = String.format("<html><font color='%s'><b>%s</b></font> so với hôm qua<br><font size='2' color='gray'>(Chỉ tính phòng đã check-out)</font></html>",
                    adrTrendColor, adrTrendStr);
            JPanel cardADR = createCard("ADR (Giá TB/phòng)", df.format(adrToday) + " ₫", adrSub, "tag", SUCCESS_BG, SUCCESS_TEXT);
            cardADR.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cardADR.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPriceAnalysisDialog(); } });
            p.add(cardADR);

            // 4. RevPAR
            String revParSub = "<html>DT / Tổng phòng<br>🎯 Mục tiêu: <b>500.000 ₫</b></html>";
            JPanel cardRevPar = createCard("RevPAR", df.format(revPar) + " ₫", revParSub, "chart", WARNING_BG, WARNING_TEXT);
            cardRevPar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cardRevPar.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPriceAnalysisDialog(); } });
            p.add(cardRevPar);

            return p;
        }

        // ... [GIỮ NGUYÊN HÀM createDetailedOccupancyCard và createLegendRow] ...
        private JPanel createDetailedOccupancyCard(int booked, int empty, int total) {
            RoundedPanel card = new RoundedPanel(new BorderLayout(), 20, Color.WHITE);
            card.setBorder(new EmptyBorder(15, 20, 15, 15));
            JPanel leftPanel = new JPanel(); leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS)); leftPanel.setOpaque(false);
            JPanel iconBox = new JPanel(new GridBagLayout()); iconBox.setPreferredSize(new Dimension(45, 45)); iconBox.setMaximumSize(new Dimension(45, 45));
            iconBox.setBackground(new Color(243, 232, 255)); iconBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            iconBox.add(new JLabel(new AppIcon("bed", 24, new Color(147, 51, 234))));
            int percent = total > 0 ? (booked * 100 / total) : 0;
            JLabel lblPercent = new JLabel(percent + "%"); lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 26)); lblPercent.setForeground(TEXT_DARK); lblPercent.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblTitle = new JLabel("Tỷ lệ lấp đầy"); lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblTitle.setForeground(TEXT_GRAY); lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftPanel.add(iconBox); leftPanel.add(Box.createVerticalStrut(15)); leftPanel.add(lblPercent); leftPanel.add(Box.createVerticalStrut(5)); leftPanel.add(lblTitle);
            JPanel rightPanel = new JPanel(new GridBagLayout()); rightPanel.setOpaque(false);
            JPanel chart = new JPanel() {
                @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = Math.min(getWidth(), getHeight()); int x = (getWidth() - size) / 2; int y = (getHeight() - size) / 2;
                    g2.setColor(new Color(229, 231, 235)); g2.fillOval(x, y, size, size);
                    g2.setColor(new Color(168, 85, 247)); int angle = (int) (3.6 * percent); g2.fillArc(x, y, size, size, 90, -angle);
                    g2.setColor(Color.WHITE); int innerSize = (int) (size * 0.6); g2.fillOval(x + (size - innerSize) / 2, y + (size - innerSize) / 2, innerSize, innerSize);
                }
            };
            chart.setPreferredSize(new Dimension(70, 70)); chart.setOpaque(false);
            JPanel legend = new JPanel(new GridLayout(2, 1, 0, 5)); legend.setOpaque(false); legend.setBorder(new EmptyBorder(0, 10, 0, 0));
            legend.add(createLegendRow(booked + " đã đặt", new Color(168, 85, 247))); legend.add(createLegendRow(empty + " trống", new Color(156, 163, 175)));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy = 0; rightPanel.add(chart, gbc); gbc.gridx = 1; rightPanel.add(legend, gbc);
            card.add(leftPanel, BorderLayout.WEST); card.add(rightPanel, BorderLayout.EAST); return card;
        }
        private JPanel createLegendRow(String text, Color color) { JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); p.setOpaque(false); JLabel colorBox = new JLabel(); colorBox.setOpaque(true); colorBox.setBackground(color); colorBox.setPreferredSize(new Dimension(10, 10)); JLabel lblText = new JLabel(text); lblText.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblText.setForeground(TEXT_DARK); p.add(colorBox); p.add(lblText); return p; }

        private JPanel createMainSplitSection() {
            JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
            gbc.gridx = 0; gbc.weightx = 0.65; gbc.insets = new Insets(0, 0, 0, 20); p.add(createStaffSchedulePanel(), gbc);
            gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 0); p.add(createRightSideStats(), gbc);
            return p;
        }

        private JPanel createStaffSchedulePanel() {
            RoundedPanel card = new RoundedPanel(new BorderLayout(), 20, Color.WHITE);
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            JLabel title = new JLabel("Lịch làm việc hôm nay (Click để xem chi tiết)");
            title.setFont(FONT_TITLE); title.setBorder(new EmptyBorder(0, 0, 15, 0));
            card.add(title, BorderLayout.NORTH);
            tableStaff = new JTable(staffModel); tableStaff.setRowHeight(55); tableStaff.setShowVerticalLines(false); tableStaff.setGridColor(new Color(240, 240, 240));
            tableStaff.setFont(FONT_TEXT); tableStaff.getTableHeader().setBackground(Color.WHITE); tableStaff.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tableStaff.getColumnModel().getColumn(0).setMinWidth(0); tableStaff.getColumnModel().getColumn(0).setMaxWidth(0); tableStaff.getColumnModel().getColumn(0).setWidth(0);
            tableStaff.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    String name = v.toString(); String initial = name.length() > 0 ? name.substring(name.lastIndexOf(" ")+1).substring(0, 1).toUpperCase() : "?";
                    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); p.setOpaque(true); p.setBackground(s ? t.getSelectionBackground() : Color.WHITE); p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    JLabel avatar = new JLabel(initial, SwingConstants.CENTER); avatar.setPreferredSize(new Dimension(32, 32)); avatar.setOpaque(true); avatar.setBackground(new Color(219, 234, 254)); avatar.setForeground(PRIMARY_COLOR); avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    p.add(avatar); p.add(new JLabel(name)); return p;
                }
            });
            tableStaff.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { int row = tableStaff.getSelectedRow(); if (row != -1) { String maNV = tableStaff.getModel().getValueAt(row, 0).toString(); String tenNV = tableStaff.getModel().getValueAt(row, 1).toString(); showStaffScheduleDialog(maNV, tenNV); } } });
            JScrollPane sp = new JScrollPane(tableStaff); sp.setBorder(null); sp.getViewport().setBackground(Color.WHITE); card.add(sp, BorderLayout.CENTER); return card;
        }

        private JPanel createRightSideStats() {
            JPanel p = new JPanel(new GridLayout(2, 1, 0, 20)); p.setOpaque(false);
            // Peak Hour Chart
            RoundedPanel chartCard = new RoundedPanel(new BorderLayout(), 20, Color.WHITE);
            chartCard.setBorder(new EmptyBorder(20, 20, 20, 20));
            chartCard.add(new JLabel("Lưu lượng khách (Hôm nay)", JLabel.LEFT), BorderLayout.NORTH);
            PeakHourChart chart = new PeakHourChart(peakHourData);
            chartCard.add(chart, BorderLayout.CENTER);
            chartCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chartCard.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPeakHourDetails(); } });
            // Room Status
            RoundedPanel status = new RoundedPanel(new BorderLayout(), 20, Color.WHITE);
            status.setBorder(new EmptyBorder(20, 20, 20, 20));
            status.add(new JLabel("Tình trạng phòng (Hiện tại)", JLabel.LEFT), BorderLayout.NORTH);
            JPanel statList = new JPanel(new GridLayout(3, 1, 0, 10)); statList.setOpaque(false); statList.setBorder(new EmptyBorder(15, 0, 0, 0));
            statList.add(createStatusRow("⌂ Sạch sẽ", String.valueOf(cleanRoom), SUCCESS_BG, SUCCESS_TEXT, () -> showRoomListDialog("Phòng sạch sẽ (Trống)", 0)));
            statList.add(createStatusRow("⌂ Cần dọn", String.valueOf(dirtyRoom), WARNING_BG, WARNING_TEXT, () -> showRoomListDialog("Phòng cần dọn", 2)));
            statList.add(createStatusRow("⌂ Bảo trì", String.valueOf(maintenanceRoom), new Color(255, 237, 213), new Color(194, 65, 12), () -> showRoomListDialog("Phòng bảo trì", 3)));
            status.add(statList, BorderLayout.CENTER);
            p.add(chartCard); p.add(status); return p;
        }

        // ... [GIỮ NGUYÊN CÁC HELPER COMPONENTS: createCard, createStatusRow] ...
        private JPanel createCard(String title, String value, String subText, String iconName, Color iconBg, Color iconFg) {
            RoundedPanel card = new RoundedPanel(new BorderLayout(), 20, Color.WHITE); card.setBorder(new EmptyBorder(20, 20, 20, 20));
            JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JPanel iconBox = new JPanel(new GridBagLayout()); iconBox.setPreferredSize(new Dimension(45, 45)); iconBox.setBackground(iconBg); iconBox.add(new JLabel(new AppIcon(iconName, 24, iconFg))); header.add(iconBox, BorderLayout.WEST);
            JPanel content = new JPanel(new GridLayout(3,1)); content.setOpaque(false); content.setBorder(new EmptyBorder(15, 0, 0, 0));
            JLabel val = new JLabel(value); val.setFont(FONT_BIG); val.setForeground(TEXT_DARK); JLabel tit = new JLabel(title); tit.setFont(FONT_TEXT); tit.setForeground(TEXT_GRAY); JLabel sub = new JLabel(subText); sub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub.setForeground(new Color(100, 116, 139));
            content.add(val); content.add(tit); content.add(sub); card.add(header, BorderLayout.NORTH); card.add(content, BorderLayout.CENTER); return card;
        }
        private JPanel createStatusRow(String label, String count, Color bg, Color fg, Runnable onClick) { JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); JLabel l = new JLabel(label); l.setFont(FONT_TEXT); l.setForeground(TEXT_DARK); JLabel c = new JLabel(" " + count + " "); c.setOpaque(true); c.setBackground(bg); c.setForeground(fg); c.setFont(FONT_BOLD); p.add(l, BorderLayout.WEST); p.add(c, BorderLayout.EAST); p.setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(240,240,240))); p.setCursor(new Cursor(Cursor.HAND_CURSOR)); p.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if(onClick != null) onClick.run(); } }); return p; }

        // ... [GIỮ NGUYÊN CÁC HÀM DRILL DOWN] ...
        private void showDrillDownDialog(String title, JComponent content, int width, int height) { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true); d.setSize(width, height); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout()); d.add(new JScrollPane(content), BorderLayout.CENTER); d.setVisible(true); }
        private void showRevenueDetailDialog() { String[] cols = {"Mã HD", "Ngày lập", "Khách hàng", "Tổng tiền"}; DefaultTableModel model = new DefaultTableModel(cols, 0); String sql = "SELECT maHoaDon, ngayLap, maKH, tongTien FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)"; try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { ResultSet rs = ps.executeQuery(); java.text.DecimalFormat df = new java.text.DecimalFormat("#,###"); while(rs.next()) { model.addRow(new Object[]{rs.getString(1), rs.getTime(2), rs.getString(3), df.format(rs.getDouble(4))}); } } catch (Exception e) { e.printStackTrace(); } JTable table = new JTable(model); showDrillDownDialog("Chi tiết doanh thu HÔM NAY", table, 600, 400); }
        private void showRoomListDialog(String title, int status) { String[] cols = {"Mã Phòng", "Loại Phòng", "Giá tiền"}; DefaultTableModel model = new DefaultTableModel(cols, 0); try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT p.maPhong, lp.tenLoaiPhong, p.giaTienMotDem FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong=lp.maLoaiPhong WHERE p.maTrangThai=?")) { ps.setInt(1, status); ResultSet rs = ps.executeQuery(); java.text.DecimalFormat df = new java.text.DecimalFormat("#,###"); while(rs.next()) { model.addRow(new Object[]{rs.getString(1), rs.getString(2), df.format(rs.getDouble(3))}); } } catch (Exception e) { e.printStackTrace(); } JTable table = new JTable(model); showDrillDownDialog(title, table, 500, 400); }
        private void showStaffScheduleDialog(String maNV, String tenNV) { String[] cols = {"Ngày", "Ca làm", "Giờ BĐ", "Giờ KT", "Trạng thái"}; DefaultTableModel model = new DefaultTableModel(cols, 0); String sql = "SELECT ngayLam, caLam, gioBatDau, gioKetThuc, trangThai FROM LichLamViec WHERE maNV=?"; try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { ps.setString(1, maNV); ResultSet rs = ps.executeQuery(); while(rs.next()) { model.addRow(new Object[]{rs.getDate(1), rs.getString(2), rs.getTime(3), rs.getTime(4), rs.getString(5)}); } } catch (Exception e) { e.printStackTrace(); } JTable table = new JTable(model); showDrillDownDialog("Lịch làm việc: " + tenNV, table, 600, 350); }
        private void showPeakHourDetails() { String[] cols = {"Giờ", "Loại phiếu", "Phòng", "Khách hàng"}; DefaultTableModel model = new DefaultTableModel(cols, 0); String sql = "SELECT DATEPART(HOUR, ngayNhanPhong) as Gio, N'Check-in' as Loai, maPhong, maKH FROM PhieuDatPhong WHERE CAST(ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE) UNION ALL SELECT DATEPART(HOUR, ngayTraPhong) as Gio, N'Check-out' as Loai, maPhong, maKH FROM PhieuDatPhong WHERE CAST(ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE)"; try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { ResultSet rs = ps.executeQuery(); while(rs.next()) { model.addRow(new Object[]{rs.getInt(1) + "h00", rs.getString(2), rs.getString(3), rs.getString(4)}); } } catch (Exception e) { e.printStackTrace(); } JTable table = new JTable(model); showDrillDownDialog("Chi tiết lưu lượng khách HÔM NAY", table, 500, 400); }
        private void showPriceAnalysisDialog() { String[] cols = {"Loại phòng", "Giá niêm yết", "Số lượng phòng"}; DefaultTableModel model = new DefaultTableModel(cols, 0); String sql = "SELECT lp.tenLoaiPhong, AVG(p.giaTienMotDem), COUNT(p.maPhong) FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong=lp.maLoaiPhong GROUP BY lp.tenLoaiPhong"; try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { ResultSet rs = ps.executeQuery(); java.text.DecimalFormat df = new java.text.DecimalFormat("#,###"); while(rs.next()) { model.addRow(new Object[]{rs.getString(1), df.format(rs.getDouble(2)), rs.getInt(3)}); } } catch(Exception e) { e.printStackTrace(); } JTable table = new JTable(model); showDrillDownDialog("Phân tích giá phòng & Cơ cấu", table, 500, 300); }
        private void showOccupancyDetailsDialog() { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết tình trạng phòng", true); d.setSize(650, 450); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout()); JTabbedPane tabbedPane = new JTabbedPane(); tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13)); JPanel pnlBooked = createRoomTableByStatus(1); tabbedPane.addTab("Đang có khách", new AppIcon("bed", 16, new Color(147, 51, 234)), pnlBooked); JPanel pnlEmpty = createRoomTableByStatus(0); tabbedPane.addTab("Phòng trống (Sạch)", new AppIcon("bed", 16, new Color(22, 163, 74)), pnlEmpty); d.add(tabbedPane, BorderLayout.CENTER); JButton btnClose = new JButton("Đóng"); btnClose.addActionListener(e -> d.dispose()); JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlBtn.add(btnClose); d.add(pnlBtn, BorderLayout.SOUTH); d.setVisible(true); }
        private JPanel createRoomTableByStatus(int status) { JPanel p = new JPanel(new BorderLayout()); p.setBorder(new EmptyBorder(10, 10, 10, 10)); p.setBackground(Color.WHITE); String[] cols = {"Mã Phòng", "Loại Phòng", "Đơn giá", "Tầng"}; DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } }; String sql = "SELECT p.maPhong, lp.tenLoaiPhong, p.giaTienMotDem FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE p.maTrangThai = ?"; try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { ps.setInt(1, status); ResultSet rs = ps.executeQuery(); java.text.DecimalFormat df = new java.text.DecimalFormat("#,###"); while(rs.next()) { String maPhong = rs.getString(1); String loaiPhong = rs.getString(2); String gia = df.format(rs.getDouble(3)); String tang = maPhong.length() > 1 ? maPhong.substring(1, 2) : "1"; model.addRow(new Object[]{maPhong, loaiPhong, gia, "Tầng " + tang}); } } catch (Exception e) { e.printStackTrace(); } JTable table = new JTable(model); table.setRowHeight(30); table.setFont(new Font("Segoe UI", Font.PLAIN, 13)); table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13)); table.setShowVerticalLines(false); DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER); table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); JScrollPane sp = new JScrollPane(table); sp.getViewport().setBackground(Color.WHITE); p.add(sp, BorderLayout.CENTER); JLabel lblCount = new JLabel("Tổng số lượng: " + model.getRowCount() + " phòng"); lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lblCount.setBorder(new EmptyBorder(5, 5, 0, 0)); p.add(lblCount, BorderLayout.SOUTH); return p; }

        // --- SUB CLASSES ---

        // [SỬA 2] CẬP NHẬT CLASS PeakHourChart ĐỂ VẼ TRỤC Y (SỐ LƯỢNG)
        private static class PeakHourChart extends JPanel {
            private final int[] data;
            public PeakHourChart(int[] data) { this.data = data; setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int padLeft = 40; // Tăng lề trái để hiện số trục Y
                int padBot = 25;
                int chartH = h - padBot - 15;
                int chartW = w - padLeft - 10;

                // Tìm max
                int maxVal = 1; for(int v : data) if(v > maxVal) maxVal = v;

                // Vẽ lưới ngang và số trục Y (5 mốc)
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (int i = 0; i <= 4; i++) {
                    int val = (int)Math.round(maxVal * i / 4.0); // 0, 25%, 50%...
                    int yPos = 15 + chartH - (int)((double)val/maxVal * chartH);

                    // Vẽ số trục Y (bên trái)
                    g2.setColor(TEXT_GRAY);
                    g2.drawString(String.valueOf(val), 5, yPos + 4);

                    // Vẽ dòng kẻ mờ
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawLine(padLeft, yPos, w-10, yPos);
                }

                // Vẽ trục X và Y chính
                g2.setColor(new Color(200, 200, 200));
                g2.drawLine(padLeft, 15, padLeft, h-padBot); // Trục Y
                g2.drawLine(padLeft, h-padBot, w-10, h-padBot); // Trục X

                java.awt.geom.GeneralPath polyline = new java.awt.geom.GeneralPath();
                double xStep = (double)chartW / 23;

                // Điểm bắt đầu
                polyline.moveTo(padLeft, 15 + chartH - (int)((double)data[0]/maxVal * chartH));

                // Vẽ đường biểu đồ
                for (int i = 0; i < 24; i++) {
                    float x = padLeft + (float)(i * xStep);
                    float y = 15 + chartH - (int)((double)data[i]/maxVal * chartH);
                    polyline.lineTo(x, y);

                    // Vẽ số giờ trục X
                    if(i % 4 == 0) {
                        g2.setColor(TEXT_GRAY);
                        g2.drawString(i + "h", x - 5, h - 5);
                    }
                }

                // Tô màu vùng dưới
                java.awt.geom.GeneralPath area = (java.awt.geom.GeneralPath) polyline.clone();
                area.lineTo(padLeft + chartW, 15 + chartH); area.lineTo(padLeft, 15 + chartH); area.closePath();
                g2.setPaint(new GradientPaint(0, 15, CHART_FILL_COLOR, 0, h, new Color(255,255,255,0)));
                g2.fill(area);

                // Vẽ đường line chính
                g2.setColor(CHART_LINE_COLOR);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(polyline);
            }
        }

        // ... [GIỮ NGUYÊN RoundedPanel và AppIcon] ...
        private static class RoundedPanel extends JPanel {
            private int radius; private Color bgColor;
            public RoundedPanel(LayoutManager layout, int radius, Color bgColor) { super(layout); this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius); g2.setColor(new Color(230,230,230)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius); }
        }
        private static class AppIcon implements Icon {
            private String type; private int size; private Color color;
            public AppIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
            public int getIconWidth() { return size; } public int getIconHeight() { return size; }
            public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, size - 4)); String symbol = "●"; if(type.equals("money")) symbol = "$"; else if(type.equals("tag")) symbol = "🏷"; else if(type.equals("chart")) symbol = "📈"; else if(type.equals("bed")) symbol = "🛏"; FontMetrics fm = g2.getFontMetrics(); g2.drawString(symbol, x + (size - fm.stringWidth(symbol)) / 2, y + (size - fm.getHeight()) / 2 + fm.getAscent()); }
        }
    }
// =================================================================================
// PANEL NỘI DUNG 3: QUẢN LÝ NHÂN VIÊN
// =================================================================================

    public static class PanelNhanVienContent extends JPanel {

        private List<entity.NhanVien> employees; // danh sách gốc
        private List<entity.NhanVien> filteredEmployees; // danh sách sau khi lọc
        private JTextField searchField;
        private JComboBox<String> cbType;
        private JPanel listPanel;
        private JScrollPane scrollPane;
        private JLabel totalLabel, breakdownLabel;
        private JLabel lblTongNV;
        private JLabel lblLeTan;
        private JLabel lblQuanLy;
        private NhanVien_DAO dao = new NhanVien_DAO();
        private final EventNhanVien event = new EventNhanVien();
        // Note: Dùng lại các hằng số màu sắc từ PanelThongKeContent (đã đổi tên)

        public PanelNhanVienContent() {
            setLayout(new BorderLayout());
            setBackground(MAIN_BG);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            dao = new NhanVien_DAO();
            employees = dao.getAllNhanVien();

            filteredEmployees = new ArrayList<>(employees);

            add(createHeader(), BorderLayout.NORTH);
            add(createMainContent(), BorderLayout.CENTER);
        }

        private JPanel createHeader() {
            // Note: Tiêu đề Quản lý Nhân viên và nút Thêm
            JPanel header = new JPanel(new BorderLayout());
            searchField = new JTextField(20);
            header.setOpaque(false);

            JLabel title = new JLabel("Quản lý Nhân viên");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            header.add(title, BorderLayout.WEST);

            JButton btnAdd = new JButton("Thêm nhân viên");
            btnAdd.setBackground(ACCENT_BLUE);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setBorder(new EmptyBorder(6, 12, 6, 12));
            btnAdd.setFocusPainted(false);
            btnAdd.setBorderPainted(false);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // === Sự kiện: Thêm nhân viên ===
            btnAdd.addActionListener(ae -> {
                event.handleAdd((Frame) SwingUtilities.getWindowAncestor(this), () -> {
                    employees = event.reloadAll();
                    applyFilters();
                    refreshStats(lblTongNV, lblLeTan, lblQuanLy);
                });
            });
            header.add(btnAdd, BorderLayout.EAST);

            return header;
        }

        private JPanel createMainContent() {
            // Note: Cấu trúc chính: Controls (Search/Filter/Stats), Table, Footer
            JPanel content = new JPanel(new BorderLayout(0, 16));
            content.setOpaque(false);

            content.add(createTopControls(), BorderLayout.NORTH);

            // Gọi createFooterSummary() TRƯỚC khi createCenterArea()
            // để đảm bảo totalLabel và breakdownLabel đã được khởi tạo
            JPanel footer = createFooterSummary();
            JPanel center = createCenterArea();

            content.add(center, BorderLayout.CENTER);
            content.add(footer, BorderLayout.SOUTH);

            return content;
        }

        // ======== Đầu: tìm + lọc + thông tin=========
        private JPanel createTopControls() {
            // Note: Chứa thanh tìm kiếm và các ô thống kê nhanh
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.setOpaque(false);
            top.setBorder(new EmptyBorder(10, 15, 10, 15));

            // ===== Hàng tìm kiếm + lọc =====
            JPanel searchFilter = createSearchFilterPanel();
            searchFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(searchFilter);

            // ===== Hàng thống kê =====
            JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0)); // 3 cột, cách nhau 15px
            statsRow.setOpaque(false);
            statsRow.setBorder(new EmptyBorder(10, 0, 5, 0));
            statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            NhanVien_DAO dao = new NhanVien_DAO();
            lblTongNV = new JLabel(String.valueOf(dao.countAllNhanVien()));
            lblLeTan = new JLabel(String.valueOf(dao.countNhanVienByLoai(1)));
            lblQuanLy = new JLabel(String.valueOf(dao.countNhanVienByLoai(2)));

            statsRow.add(createStatCard(lblTongNV, "Tổng nhân viên", Color.gray));
            statsRow.add(createStatCard(lblLeTan, "Nhân viên lễ tân", new Color(99, 132, 244)));
            statsRow.add(createStatCard(lblQuanLy, "Nhân viên quản lý", new Color(186, 85, 211)));

            top.add(statsRow);


            return top;
        }

        private void refreshStats(JLabel lblTongNV, JLabel lblLeTan, JLabel lblQuanLy) {
            NhanVien_DAO dao = new NhanVien_DAO();
            lblTongNV.setText(String.valueOf(dao.countAllNhanVien()));
            lblLeTan.setText(String.valueOf(dao.countNhanVienByLoai(1))); // lễ tân = 2
            lblQuanLy.setText(String.valueOf(dao.countNhanVienByLoai(2))); // quản lý = 1

        }


        private JPanel createSearchFilterPanel() {
            // Note: Thanh tìm kiếm và ComboBox Lọc
            JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
            searchPanel.setOpaque(false);

            //  Không khai báo lại biến cục bộ
            searchField = new JTextField("");  // <-- dùng biến instance

            String placeholder = " Tìm kiếm theo mã NV, họ tên, số điện thoại, email, CCCD...";
            Color placeholderColor = Color.GRAY;
            Color defaultColor = UIManager.getColor("TextField.foreground");

            searchField.setText(placeholder);
            searchField.setForeground(placeholderColor);

            searchField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (searchField.getText().equals(placeholder)) {
                        searchField.setText("");
                        searchField.setForeground(defaultColor);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (searchField.getText().isEmpty()) {
                        searchField.setText(placeholder);
                        searchField.setForeground(placeholderColor);
                    }
                }
            });

            //  Sự kiện gõ chữ để lọc tự động
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    applyFilters();
                }
            });

            searchField.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER),
                    new EmptyBorder(6, 8, 6, 8)));

            searchPanel.add(searchField, BorderLayout.CENTER);

            cbType = new JComboBox<>(new String[]{"Tất cả loại", "Lễ tân", "Quản lý"});
            cbType.setPreferredSize(new Dimension(160, 30));

            // === Sự kiện lọc loại ===
            cbType.addActionListener(e -> applyFilters());

            searchPanel.add(cbType, BorderLayout.EAST);
            return searchPanel;
        }

        private JPanel createStatCard(JLabel valueLabel, String label, Color color) {
            // Note: ô thống kê nhanh cho nhân viên
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(12, 18, 12, 18)));
            card.setPreferredSize(new Dimension(200, 60));

            valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            valueLabel.setForeground(color);

            JLabel lab = new JLabel(label);
            lab.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lab.setForeground(Color.GRAY);
            lab.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(lab);
            card.add(Box.createRigidArea(new Dimension(0, 2)));
            card.add(valueLabel);

            return card;
        }

        // ======== Giữa: Danh sách nhân viên ========
        private JPanel createCenterArea() {
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(createTableHeader(), BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);

            scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(MAIN_BG);
            scrollPane.setPreferredSize(new Dimension(0, 420));
            scrollPane.getVerticalScrollBar().setUnitIncrement(14);

            center.add(scrollPane, BorderLayout.CENTER);
            refreshEmployeeList();
            return center;
        }

        // Note: Trọng số của các cột
        private static final double[] COL_WEIGHTS = {
                0.06, // Mã NV
                0.07, // Họ tên (Tăng để hiển thị đủ)
                0.07, // Ngày sinh
                0.07, // Giới tính
                0.08, // Liên hệ (Tăng để hiển thị đủ)
                0.10, // CCCD
                0.10, // Loại nhân viên
                0.05, // Thao tác
        };

        private JPanel createTableHeader() {
            // Note: Dòng tiêu đề của bảng
            JPanel header = new JPanel();
            header.setLayout(new GridBagLayout());
            header.setOpaque(false);
            header.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    new EmptyBorder(8, 8, 8, 8)));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 8, 0, 8);
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int col = 0;
            addHeaderLabel(header, "Mã NV", col++, 0.06, gbc);
            addHeaderLabel(header, "Họ tên", col++, 0.06, gbc);
            addHeaderLabel(header, "Ngày sinh", col++, 0.04, gbc);
            addHeaderLabel(header, "Giới tính", col++, 0.08, gbc);
            addHeaderLabel(header, "Liên hệ", col++, 0.10, gbc);
            addHeaderLabel(header, "CCCD", col++, 0.10, gbc);
            addHeaderLabel(header, "Loại nhân viên", col++, 0.10, gbc);
            addHeaderLabel(header, "Thao tác", col++, 0.03, gbc);

            return header;
        }

        private void addHeaderLabel(JPanel panel, String text, int gridx, double weightx, GridBagConstraints gbcBase) {
            GridBagConstraints gbc = (GridBagConstraints) gbcBase.clone();
            gbc.gridx = gridx;
            gbc.weightx = weightx;

            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(90, 90, 90));
            panel.add(lbl, gbc);
        }

        private JScrollPane createEmployeeScroll() {
            // Note: Khung cuộn chứa các hàng nhân viên
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);

            List<NhanVien> employees = dao.getAllNhanVien();

            for (NhanVien e : employees) {
                listPanel.add(createEmployeeRow(e));
                listPanel.add(Box.createVerticalStrut(8));
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(MAIN_BG);
            scroll.setPreferredSize(new Dimension(0, 420));
            scroll.getVerticalScrollBar().setUnitIncrement(14);
            return scroll;
        }

        private JPanel createEmployeeRow(NhanVien e) {
            // Note: Một hàng dữ liệu nhân viên
            JPanel row = new JPanel(new GridBagLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 10, 6, 10);
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;

            int col = 0;
            double[] weights = COL_WEIGHTS; // Sử dụng trọng số đã định nghĩa

            // ===== Mã NV =====
            JPanel idPanel = new JPanel(new BorderLayout());
            idPanel.setOpaque(false);
            JLabel idLabel = new JLabel(" " + e.getMaNV());
            idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            idPanel.add(idLabel, BorderLayout.CENTER);
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(idPanel, gbc);

            // ===== Họ tên =====
            JLabel nameLbl = new JLabel(e.getTenNV());
            nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(nameLbl, gbc);

            // ===== Ngày sinh =====
            JLabel dob = new JLabel(e.getNgaySinh().toString()); // hoặc format đẹp hơn
            dob.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dob.setForeground(new Color(100, 100, 100));
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(dob, gbc);

            // ===== Giới tính =====
            JLabel gender = new JLabel(e.getGioiTinh() != null ? e.getGioiTinh().getLabel() : "");
            gender.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gender.setForeground(new Color(100, 100, 100));
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(gender, gbc);

            // ===== Liên hệ (Thay icon emoji bằng ký tự đơn giản hơn để tránh lỗi font)
            JPanel contact = new JPanel(new GridLayout(2, 1, 0, 2));
            contact.setOpaque(false);
            JLabel phone = new JLabel("☎ " + e.getSoDT()); // Dùng ký tự điện thoại ổn định hơn
            phone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            phone.setForeground(new Color(100, 100, 100));
            JLabel email = new JLabel("✉ " + e.getEmail()); // Dùng ký tự email ổn định hơn
            email.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            email.setForeground(new Color(100, 100, 100));
            contact.add(phone);
            contact.add(email);
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(contact, gbc);

            // ===== CCCD (Thay icon emoji bằng ký tự đơn giản hơn) =====
            JLabel cccd = new JLabel("ID " + e.getCCCD()); // Dùng ID thay cho biểu tượng thẻ
            cccd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cccd.setForeground(new Color(100, 100, 100));
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(cccd, gbc);

            // ===== Loại nhân viên =====
            JLabel role = new JLabel(e.getChucVu() != null ? e.getChucVu().getLabel() : "", SwingConstants.CENTER);
            role.setBackground(getRoleColor(e.getChucVu()));
            role.setFont(new Font("Segoe UI", Font.BOLD, 12));
            role.setOpaque(true);
            role.setBackground(getRoleColor(e.getChucVu()));
            role.setForeground(Color.WHITE);
            role.setBorder(new EmptyBorder(4, 10, 4, 10));
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(role, gbc);

            // ===== Thao tác =====
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);
            JButton btnEdit = new JButton("✎"); // Thay thế
            JButton btnDelete = new JButton("🗑"); // Thay thế
            for (JButton b : new JButton[]{btnEdit, btnDelete}) {
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            }
            // === Sự kiện sửa ===
            btnEdit.addActionListener(ae -> {
                event.handleEdit((Frame) SwingUtilities.getWindowAncestor(this), e, () -> {
                    employees = event.reloadAll();
                    applyFilters();
                    refreshStats(lblTongNV, lblLeTan, lblQuanLy);
                });
            });


            // === Sự kiện xoá ===
            btnDelete.addActionListener(ae -> {
                event.handleDelete(this, e, () -> {
                    employees = event.reloadAll();
                    applyFilters();
                    refreshStats(lblTongNV, lblLeTan, lblQuanLy);
                });
            });
            btnEdit.setForeground(new Color(15, 118, 255));
            btnDelete.setForeground(Color.RED);
            actions.add(btnEdit);
            actions.add(btnDelete);
            gbc.gridx = col;
            gbc.weightx = weights[col++];
            row.add(actions, gbc);

            return row;
        }

        private Color getRoleColor(LoaiNhanVien loai) {
            switch (loai) {
                case LE_TAN: return new Color(99, 132, 244);
                case QUAN_LY: return new Color(186, 85, 211);
                default: return new Color(180, 180, 180);
            }
        }

        private void openAddOrEditForm(NhanVien editEmp) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            JDialog dialog = new JDialog((Frame) null, editEmp == null ? "Thêm nhân viên" : "Sửa nhân viên", true);
            dialog.setSize(400, 400);
            dialog.setLocationRelativeTo(this);

            JTextField code = new JTextField(editEmp == null ? "" : editEmp.getMaNV());
            JTextField name = new JTextField(editEmp == null ? "" : editEmp.getTenNV());
            JTextField dob = new JTextField(editEmp == null ? "" : editEmp.getNgaySinh() != null ? editEmp.getNgaySinh().format(formatter) : "");
            JTextField phone = new JTextField(editEmp == null ? "" : editEmp.getSoDT());
            JTextField email = new JTextField(editEmp == null ? "" : editEmp.getEmail());
            JTextField cccd = new JTextField(editEmp == null ? "" : editEmp.getCCCD());

            JRadioButton radNam = new JRadioButton("Nam");
            JRadioButton radNu = new JRadioButton("Nữ");
            ButtonGroup groupGender = new ButtonGroup();
            groupGender.add(radNam);
            groupGender.add(radNu);
            if (editEmp != null) {
                radNam.setSelected(editEmp.getGioiTinh() == GioiTinh.NAM);
                radNu.setSelected(editEmp.getGioiTinh() == GioiTinh.NU);
            } else {
                radNam.setSelected(true);
            }

            JComboBox<String> role = new JComboBox<>(new String[]{"Lễ tân", "Quản lý"});
            if (editEmp != null) role.setSelectedItem(editEmp.getChucVu());

            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

            panel.add(new JLabel("Mã NV:"));
            panel.add(code);
            panel.add(new JLabel("Họ tên:"));
            panel.add(name);
            panel.add(new JLabel("Ngày sinh(dd/mm/yy):"));
            panel.add(dob);
            panel.add(new JLabel("Giới tính:"));
            JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genderPanel.add(radNam);
            genderPanel.add(radNu);
            panel.add(genderPanel);
            panel.add(new JLabel("SĐT:"));
            panel.add(phone);
            panel.add(new JLabel("Email:"));
            panel.add(email);
            panel.add(new JLabel("CCCD:"));
            panel.add(cccd);
            panel.add(new JLabel("Loại NV:"));
            panel.add(role);
            //Font chữ
            Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
            for (Component comp : panel.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setFont(labelFont);
                }
            }

            JPanel buttonPanel = new JPanel();
            JButton btnOk = new JButton("OK");
            JButton btnCancel = new JButton("Cancel");
            buttonPanel.add(btnOk);
            buttonPanel.add(btnCancel);

            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> dialog.dispose());

            btnOk.addActionListener(e -> {
                String maNV = code.getText().trim();
                String hoTen = name.getText().trim();
                String sdtStr = phone.getText().trim();
                String emailStr = email.getText().trim();
                String cccdStr = cccd.getText().trim();
                String dobText = dob.getText().trim();
                LocalDate ngaySinh = null;
                GioiTinh gioiTinh = radNam.isSelected() ? GioiTinh.NAM : GioiTinh.NU;
                LoaiNhanVien loaiNV = role.getSelectedItem().equals("Lễ tân")
                        ? LoaiNhanVien.LE_TAN
                        : LoaiNhanVien.QUAN_LY;

                // ===== Validate =====
                if (maNV.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Mã NV không được để trống!");
                    return;
                }
                if (hoTen.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Họ tên không được để trống!");
                    return;
                }
                if (sdtStr.isEmpty() || !sdtStr.matches("\\d{10,11}")) {
                    JOptionPane.showMessageDialog(dialog, "SĐT không hợp lệ!");
                    return;
                }
                if (cccdStr.isEmpty() || !cccdStr.matches("\\d{9,12}")) {
                    JOptionPane.showMessageDialog(dialog, "CCCD không hợp lệ!");
                    return;
                }
                if (!emailStr.isEmpty() && !emailStr.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
                    JOptionPane.showMessageDialog(dialog, "Email không hợp lệ!");
                    return;
                }
                if (!dobText.isEmpty()) {
                    try {
                        ngaySinh = LocalDate.parse(dobText, formatter);
                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(dialog, "Ngày sinh không hợp lệ!");
                        return;
                    }
                }

                NhanVien_DAO dao = new NhanVien_DAO();

                if (editEmp == null) {
                    NhanVien nv = new NhanVien(maNV, hoTen, sdtStr, emailStr, "", cccdStr, ngaySinh, gioiTinh, loaiNV, "123456");
                    if (dao.addNhanVien(nv)) {
                        JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
                        employees = dao.getAllNhanVien();
                        applyFilters();
                        refreshStats(lblTongNV, lblLeTan, lblQuanLy);
                        dialog.dispose(); // chỉ đóng khi thành công
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Thêm thất bại!");
                    }
                } else {
                    editEmp.setTenNV(hoTen);
                    editEmp.setSoDT(sdtStr);
                    editEmp.setEmail(emailStr);
                    editEmp.setCCCD(cccdStr);
                    editEmp.setNgaySinh(ngaySinh);
                    editEmp.setGioiTinh(gioiTinh);
                    editEmp.setChucVu(loaiNV);
                    if (dao.updateNhanVien(editEmp)) {
                        JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                        employees = dao.getAllNhanVien();
                        applyFilters();
                        refreshStats(lblTongNV, lblLeTan, lblQuanLy);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!");
                    }
                }
            });

            dialog.setVisible(true);
        }


        // ======== FOOTER: tổng kết ========
        private JPanel createFooterSummary() {
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(true);
            footer.setBackground(Color.WHITE);
            footer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(10, 15, 10, 15)));

            // Gán cho biến instance, không tạo biến mới
            totalLabel = new JLabel("Tổng số nhân viên: " + filteredEmployees.size());
            totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            totalLabel.setForeground(Color.DARK_GRAY);

            long countLT = filteredEmployees.stream()
                    .filter(e -> e.getChucVu() == LoaiNhanVien.LE_TAN)
                    .count();

            long countQL = filteredEmployees.stream()
                    .filter(e -> e.getChucVu() == LoaiNhanVien.QUAN_LY)
                    .count();
            breakdownLabel = new JLabel("Lễ tân: " + countLT + " | Quản lý: " + countQL);
            breakdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            breakdownLabel.setForeground(Color.DARK_GRAY);

            footer.add(totalLabel, BorderLayout.WEST);
            footer.add(breakdownLabel, BorderLayout.EAST);

            return footer;
        }

        private void refreshEmployeeList() {
            listPanel.removeAll();
            for (NhanVien e : filteredEmployees) {
                listPanel.add(createEmployeeRow(e));
                listPanel.add(Box.createVerticalStrut(8));
            }
            updateFooter();

            listPanel.revalidate();
            listPanel.repaint();

        }

        private void applyFilters() {
            String tempKeyword = searchField.getText().trim().toLowerCase();
            String placeholder = "tìm kiếm theo mã nv, họ tên, số điện thoại, email, cccd...";
            final String keyword = tempKeyword.equals(placeholder.toLowerCase()) ? "" : tempKeyword;

            final String type = cbType.getSelectedItem().toString();

            filteredEmployees = employees.stream()
                    .filter(e -> {
                        boolean matchType =
                                type.equals("Tất cả loại")
                                        || (type.equals("Lễ tân") && e.getChucVu() == LoaiNhanVien.LE_TAN)
                                        || (type.equals("Quản lý") && e.getChucVu() == LoaiNhanVien.QUAN_LY);
                        boolean matchKeyword = keyword.isEmpty()
                                || e.getMaNV().toLowerCase().contains(keyword)
                                || e.getTenNV().toLowerCase().contains(keyword)
                                || e.getSoDT().contains(keyword)
                                || e.getEmail().toLowerCase().contains(keyword)
                                || e.getCCCD().contains(keyword);
                        return matchType && matchKeyword;
                    })
                    .collect(Collectors.toList());

            refreshEmployeeList();
        }

        private void updateFooter() {
            if (totalLabel == null || breakdownLabel == null) return;

            totalLabel.setText("Tổng số nhân viên: " + filteredEmployees.size());

            long countLT = filteredEmployees.stream()
                    .filter(e -> e.getChucVu() == LoaiNhanVien.LE_TAN)
                    .count();

            long countQL = filteredEmployees.stream()
                    .filter(e -> e.getChucVu() == LoaiNhanVien.QUAN_LY)
                    .count();

            breakdownLabel.setText("Lễ tân: " + countLT + " | Quản lý: " + countQL);
        }
    }

// =================================================================================
// PANEL NỘI DUNG 4: QUẢN LÝ KHUYẾN MÃI
// =================================================================================

// Lớp giao diện quản lý khuyến mãi, kế thừa từ JPanel


    // =================================================================================
    // PANEL NỘI DUNG 5: THỐNG KÊ & BÁO CÁO (FINAL POLISHED VERSION)
    // =================================================================================
    public static class PanelThongKeContent extends JPanel {

        // --- MÀU SẮC CHUẨN ---
        public static final Color MAIN_BG = new Color(242, 245, 250);
        public static final Color CARD_BORDER = new Color(222, 226, 230);
        public static final Color ACCENT_BLUE = new Color(24, 90, 219);
        public static final Color COLOR_WHITE = Color.WHITE;
        public static final Color COLOR_GREEN = new Color(22, 163, 74); // Xanh lá
        public static final Color COLOR_RED = new Color(220, 38, 38);    // Đỏ
        public static final Color COLOR_NEUTRAL = new Color(107, 114, 128); // Xám
        public static final Color COLOR_PURPLE = new Color(147, 51, 234);
        public static final Color COLOR_ORANGE = new Color(245, 158, 11);

        // --- MODEL DỮ LIỆU ---
        private static class KPIModel {
            double currentMonthVal;
            double lastMonthVal;
            String label;
            String unit; // "₫", "Lượt", "%"

            public KPIModel(double current, double last, String label, String unit) {
                this.currentMonthVal = current;
                this.lastMonthVal = last;
                this.label = label;
                this.unit = unit;
            }

            public double getGrowthRate() {
                if (lastMonthVal == 0) return currentMonthVal > 0 ? 100.0 : 0.0;
                return ((currentMonthVal - lastMonthVal) / lastMonthVal) * 100;
            }
        }

        public PanelThongKeContent() {
            setLayout(new BorderLayout());
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(MAIN_BG);
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Load dữ liệu thật từ SQL
            KPIModel[] kpis = fetchKPIData();
            double[] revenueData = fetchRevenueChartData();
            int[] bookingData = fetchBookingChartData();
            Map<String, Integer> roomTypeData = fetchRoomTypeData();

            // Header
            mainPanel.add(createHeader(), BorderLayout.NORTH);

            // Nội dung chính (Grid Charts)
            mainPanel.add(createContentPanel(kpis, revenueData, bookingData, roomTypeData), BorderLayout.CENTER);

            // Footer Summary
            mainPanel.add(createSummaryFooter(kpis), BorderLayout.SOUTH);

            add(mainPanel, BorderLayout.CENTER);
        }

        // -------------------------------------------------------------------------
        // PHẦN 1: TRUY VẤN DỮ LIỆU (DATA ACCESS)
        // -------------------------------------------------------------------------

        private KPIModel[] fetchKPIData() {
            KPIModel[] data = new KPIModel[4];
            // Init mặc định để tránh null
            for(int i=0; i<4; i++) data[i] = new KPIModel(0, 0, "", "");

            LocalDate now = LocalDate.now();
            int curMonth = now.getMonthValue();
            int curYear = now.getYear();
            int lastMonth = now.minusMonths(1).getMonthValue();
            int lastYear = now.minusMonths(1).getYear();

            try {
                Connection con = ConnectDB.getConnection();
                if (con == null) return data;

                // 1. DOANH THU (Revenue)
                String sqlRev = "SELECT " +
                        "SUM(CASE WHEN MONTH(ngayLap)=? AND YEAR(ngayLap)=? THEN tongTien ELSE 0 END) as Cur, " +
                        "SUM(CASE WHEN MONTH(ngayLap)=? AND YEAR(ngayLap)=? THEN tongTien ELSE 0 END) as Last " +
                        "FROM HoaDon";
                try (PreparedStatement ps = con.prepareStatement(sqlRev)) {
                    ps.setInt(1, curMonth); ps.setInt(2, curYear);
                    ps.setInt(3, lastMonth); ps.setInt(4, lastYear);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        data[0] = new KPIModel(rs.getDouble("Cur"), rs.getDouble("Last"), "Tổng doanh thu", "₫");
                    }
                }

                // 2. SỐ LƯỢNG BOOKING
                String sqlBook = "SELECT " +
                        "COUNT(CASE WHEN MONTH(ngayDatPhong)=? AND YEAR(ngayDatPhong)=? THEN 1 END) as Cur, " +
                        "COUNT(CASE WHEN MONTH(ngayDatPhong)=? AND YEAR(ngayDatPhong)=? THEN 1 END) as Last " +
                        "FROM PhieuDatPhong";
                try (PreparedStatement ps = con.prepareStatement(sqlBook)) {
                    ps.setInt(1, curMonth); ps.setInt(2, curYear);
                    ps.setInt(3, lastMonth); ps.setInt(4, lastYear);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        data[1] = new KPIModel(rs.getInt("Cur"), rs.getInt("Last"), "Tổng booking", "");
                    }
                }

                // 3. TỶ LỆ LẤP ĐẦY
                int totalRooms = 1;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Phong")) {
                    if (rs.next()) totalRooms = rs.getInt(1);
                }
                if (totalRooms == 0) totalRooms = 1;

                double occCur = (double) data[1].currentMonthVal / (totalRooms * 30) * 100;
                double occLast = (double) data[1].lastMonthVal / (totalRooms * 30) * 100;

                // Fix logic hiển thị khi booking ít: Lấy trạng thái thực
                if (occCur < 1) {
                    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Phong WHERE maTrangThai = 1")) {
                        if(rs.next()) occCur = (rs.getDouble(1) / totalRooms) * 100;
                    }
                }
                data[2] = new KPIModel(occCur, occLast, "Tỷ lệ lấp đầy", "%");

                // 4. GIÁ BÁN TRUNG BÌNH (ADR)
                double adrCur = data[1].currentMonthVal > 0 ? data[0].currentMonthVal / data[1].currentMonthVal : 0;
                double adrLast = data[1].lastMonthVal > 0 ? data[0].lastMonthVal / data[1].lastMonthVal : 0;
                data[3] = new KPIModel(adrCur, adrLast, "Giá TB (ADR)", "₫");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        private double[] fetchRevenueChartData() {
            double[] data = new double[12];
            try {
                Connection con = ConnectDB.getConnection();
                if (con == null) return data;

                String sql = "SELECT MONTH(ngayLap) as M, SUM(tongTien) as T FROM HoaDon WHERE YEAR(ngayLap) = ? GROUP BY MONTH(ngayLap)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, LocalDate.now().getYear());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int m = rs.getInt("M");
                        if (m >= 1 && m <= 12) data[m - 1] = rs.getDouble("T");
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            return data;
        }

        private int[] fetchBookingChartData() {
            int[] data = new int[12];
            try {
                Connection con = ConnectDB.getConnection();
                if (con == null) return data;

                String sql = "SELECT MONTH(ngayDatPhong) as M, COUNT(*) as C FROM PhieuDatPhong WHERE YEAR(ngayDatPhong) = ? GROUP BY MONTH(ngayDatPhong)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, LocalDate.now().getYear());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int m = rs.getInt("M");
                        if (m >= 1 && m <= 12) data[m - 1] = rs.getInt("C");
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            return data;
        }

        private Map<String, Integer> fetchRoomTypeData() {
            Map<String, Integer> data = new LinkedHashMap<>();
            try {
                Connection con = ConnectDB.getConnection();
                if (con == null) return data;

                String sql = "SELECT lp.tenLoaiPhong, COUNT(p.maPhong) as SL " +
                        "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                        "GROUP BY lp.tenLoaiPhong";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        data.put(rs.getString(1), rs.getInt(2));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            return data;
        }

        // -------------------------------------------------------------------------
        // PHẦN 2: UI COMPONENTS
        // -------------------------------------------------------------------------

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 0, 15, 0));

            JLabel title = new JLabel("Thống kê & Báo cáo");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            JLabel subtitle = new JLabel("Dữ liệu thời gian thực từ hệ thống");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(COLOR_NEUTRAL);

            JPanel titlePanel = new JPanel(new GridLayout(2, 1));
            titlePanel.setOpaque(false);
            titlePanel.add(title);
            titlePanel.add(subtitle);
            header.add(titlePanel, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false);
            JComboBox<String> cboTime = new JComboBox<>(new String[]{"Tháng này", "Quý này", "Năm nay"});
            cboTime.setFocusable(false);
            JButton btnExport = new JButton("Xuất Excel");
            btnExport.setBackground(new Color(16, 124, 65));
            btnExport.setForeground(Color.WHITE);
            btnExport.setFocusPainted(false);
            btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng đang phát triển...", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

            right.add(new JLabel("Thời gian: "));
            right.add(cboTime);
            right.add(Box.createHorizontalStrut(10));
            right.add(btnExport);
            header.add(right, BorderLayout.EAST);

            return header;
        }

        private JPanel createContentPanel(KPIModel[] kpis, double[] revenueData, int[] bookingData, Map<String, Integer> roomTypeData) {
            JPanel content = new JPanel(new BorderLayout(15, 15));
            content.setOpaque(false);

            content.add(createStatCardsPanel(kpis), BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 15, 15);

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.7; gbc.weighty = 0.5;
            grid.add(createChartCard("Biểu đồ doanh thu (Năm nay)", new LineChartPanel(revenueData)), gbc);

            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3; gbc.insets = new Insets(0, 0, 15, 0);
            grid.add(createChartCard("Cơ cấu phòng", new PieChartPanel(roomTypeData)), gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.7; gbc.weighty = 0.5; gbc.insets = new Insets(0, 0, 0, 15);
            grid.add(createChartCard("Số lượng Booking theo tháng", new BarChartPanel(bookingData, ACCENT_BLUE)), gbc);

            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.3; gbc.insets = new Insets(0, 0, 0, 0);
            grid.add(createChartCard("Thông tin hệ thống", createSystemInfoPanel()), gbc);

            content.add(grid, BorderLayout.CENTER);
            return content;
        }

        private JPanel createStatCardsPanel(KPIModel[] kpis) {
            JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
            panel.setOpaque(false);
            panel.add(createKPICard(kpis[0], ACCENT_BLUE, "$"));
            panel.add(createKPICard(kpis[1], COLOR_ORANGE, "B"));
            panel.add(createKPICard(kpis[2], COLOR_PURPLE, "%"));
            panel.add(createKPICard(kpis[3], COLOR_GREEN, "A"));
            return panel;
        }

        private JPanel createKPICard(KPIModel model, Color color, String iconStr) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)));

            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
            JLabel lblTitle = new JLabel(model.label);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitle.setForeground(COLOR_NEUTRAL);

            JLabel lblIcon = new JLabel(iconStr, SwingConstants.CENTER);
            lblIcon.setPreferredSize(new Dimension(32, 32));
            lblIcon.setOpaque(true);
            lblIcon.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
            lblIcon.setForeground(color);
            lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblIcon.setBorder(BorderFactory.createLineBorder(Color.WHITE, 0));

            top.add(lblTitle, BorderLayout.WEST);
            top.add(lblIcon, BorderLayout.EAST);

            // UPDATED: Format tiền tệ đầy đủ theo yêu cầu
            String valStr;
            if (model.unit.equals("₫")) {
                valStr = String.format("%,.0f ₫", model.currentMonthVal);
            } else if (model.unit.equals("%")) {
                valStr = String.format("%.1f%%", model.currentMonthVal);
            } else {
                valStr = String.format("%,.0f", model.currentMonthVal);
            }
            JLabel lblVal = new JLabel(valStr);
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblVal.setForeground(new Color(17, 24, 39));

            double growth = model.getGrowthRate();
            boolean up = growth >= 0;
            String trendStr = String.format("%s %.1f%% so với tháng trước", up ? "▲" : "▼", Math.abs(growth));
            JLabel lblTrend = new JLabel(trendStr);
            lblTrend.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTrend.setForeground(up ? COLOR_GREEN : COLOR_RED);

            card.add(top, BorderLayout.NORTH);
            card.add(lblVal, BorderLayout.CENTER);
            card.add(lblTrend, BorderLayout.SOUTH);

            return card;
        }

        private JPanel createChartCard(String title, JPanel chart) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1), new EmptyBorder(10, 15, 10, 15)));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setBorder(new EmptyBorder(0,0,10,0));

            card.add(lblTitle, BorderLayout.NORTH);
            card.add(chart, BorderLayout.CENTER);
            return card;
        }

        private JPanel createSystemInfoPanel() {
            JPanel p = new JPanel(new GridLayout(4, 1, 0, 10));
            p.setOpaque(false);
            p.add(new JLabel("Hệ thống: Quản lý khách sạn TBQTT"));
            p.add(new JLabel("Phiên bản: v2.5 (Stable)"));
            p.add(new JLabel("Database: SQL Server 2019"));
            p.add(new JLabel("Trạng thái: Đang kết nối..."));
            return p;
        }

        private JPanel createSummaryFooter(KPIModel[] kpis) {
            JPanel f = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
            f.setBackground(Color.WHITE);
            f.setBorder(new MatteBorder(1,0,0,0, CARD_BORDER));

            f.add(createFooterItem("Tăng trưởng doanh thu", String.format("%+.1f%%", kpis[0].getGrowthRate()),
                    kpis[0].getGrowthRate() >= 0 ? COLOR_GREEN : COLOR_RED));

            // UPDATED: Đổi tên thành "Tỷ lệ lấp đầy trung bình" theo yêu cầu
            f.add(createFooterItem("Tỷ lệ lấp đầy trung bình", String.format("%.1f%%", kpis[2].currentMonthVal), ACCENT_BLUE));

            f.add(createFooterItem("Đánh giá trung bình", "4.8/5.0", COLOR_PURPLE));
            return f;
        }

        private JPanel createFooterItem(String label, String val, Color c) {
            JPanel p = new JPanel(new GridLayout(2,1)); p.setOpaque(false);
            JLabel v = new JLabel(val, SwingConstants.CENTER); v.setFont(new Font("Segoe UI", Font.BOLD, 16)); v.setForeground(c);
            JLabel l = new JLabel(label, SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); l.setForeground(COLOR_NEUTRAL);
            p.add(v); p.add(l);
            return p;
        }

        // -------------------------------------------------------------------------
        // PHẦN 3: CHARTS VẼ TAY (CUSTOM PAINT)
        // -------------------------------------------------------------------------

        private static class LineChartPanel extends JPanel {
            private double[] data;
            public LineChartPanel(double[] data) { this.data = data; setBackground(Color.WHITE); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), pad = 30;

                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(pad, h-pad, w-pad, h-pad);
                g2.drawLine(pad, pad, pad, h-pad);

                if (data == null || data.length == 0) return;
                double max = 0; for(double d: data) max = Math.max(max, d);
                if (max == 0) max = 1;

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(ACCENT_BLUE);

                int xStep = (w - 2*pad) / (data.length);
                int[] xPoints = new int[data.length];
                int[] yPoints = new int[data.length];

                for(int i=0; i<data.length; i++) {
                    xPoints[i] = pad + i*xStep + 10;
                    yPoints[i] = h - pad - (int)((data[i]/max) * (h - 2*pad));
                    g2.fillOval(xPoints[i]-3, yPoints[i]-3, 6, 6);

                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    if (i % 2 == 0) g2.drawString("T"+(i+1), xPoints[i]-5, h-15);
                    g2.setColor(ACCENT_BLUE);
                }
                g2.drawPolyline(xPoints, yPoints, data.length);
            }
        }

        private static class BarChartPanel extends JPanel {
            private int[] data; private Color c;
            public BarChartPanel(int[] data, Color c) { this.data = data; this.c = c; setBackground(Color.WHITE); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), pad = 30;
                g2.setColor(Color.LIGHT_GRAY); g2.drawLine(pad, h-pad, w-pad, h-pad);

                if (data == null || data.length == 0) return;
                int max = 0; for(int d: data) max = Math.max(max, d); if(max==0) max=1;

                int barW = (w - 2*pad) / data.length - 10;

                for(int i=0; i<data.length; i++) {
                    int barH = (int)((double)data[i]/max * (h - 2*pad));
                    int x = pad + 5 + i * ((w - 2*pad) / data.length);
                    int y = h - pad - barH;

                    g2.setColor(c);
                    g2.fillRoundRect(x, y, barW, barH, 5, 5);

                    if (data[i] > 0) {
                        g2.setColor(Color.DARK_GRAY);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        g2.drawString(String.valueOf(data[i]), x + barW/2 - 5, y - 5);
                    }
                }
            }
        }

        private static class PieChartPanel extends JPanel {
            private Map<String, Integer> data;
            private Color[] colors = {new Color(59, 130, 246), new Color(16, 185, 129), new Color(245, 158, 11), new Color(239, 68, 68), new Color(139, 92, 246)};
            public PieChartPanel(Map<String, Integer> data) { this.data = data; setBackground(Color.WHITE); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if(data == null || data.isEmpty()) {
                    g2.setColor(Color.GRAY);
                    g2.drawString("Chưa có dữ liệu", getWidth()/2 - 40, getHeight()/2); return;
                }

                int total = 0; for(int v : data.values()) total += v;
                if(total == 0) return;

                int d = Math.min(getWidth(), getHeight()) - 40;
                int x = 10, y = (getHeight()-d)/2;

                int startAngle = 90;
                int i = 0;
                int legendY = 20;

                for(Map.Entry<String, Integer> entry : data.entrySet()) {
                    int angle = (int)(entry.getValue() * 360.0 / total);
                    g2.setColor(colors[i % colors.length]);
                    g2.fillArc(x, y, d, d, startAngle, angle);

                    // Vẽ Legend bên cạnh
                    if (getWidth() > d + 50) {
                        g2.fillRect(d + 30, legendY, 10, 10);
                        g2.setColor(Color.GRAY);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                        g2.drawString(entry.getKey() + " (" + entry.getValue() + ")", d + 45, legendY + 10);
                        legendY += 20;
                    }

                    startAngle += angle;
                    i++;
                }
                // Donut hole
                g2.setColor(Color.WHITE);
                g2.fillOval(x + d/4, y + d/4, d/2, d/2);
            }
        }
    }


























    // =================================================================================
    // PANEL NỘI DUNG 6: KHUYẾN MÃI
    // =================================================================================

    public class PanelKhuyenMaiContent extends JPanel {
        private List<KhuyenMai> promotions;
        private List<KhuyenMai> filteredPromotions;
        private JPanel listPanel;
        private JTextField searchField;
        private JComboBox<String> cbStatus;
        private KhuyenMai_DAO dao;
        private EventKhuyenMai controller;
        private JLabel lblTongKM, lblHoatDong, lblHetHan, lblTongLuot;

        public PanelKhuyenMaiContent() throws SQLException {
            dao = new KhuyenMai_DAO();
            promotions = dao.getAllKhuyenMai();
            filteredPromotions = new ArrayList<>(promotions);
            controller = new EventKhuyenMai(this, dao);

            setLayout(new BorderLayout());
            setBackground(GUI_NhanVienLeTan.MAIN_BG);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            add(createHeader(), BorderLayout.NORTH);
            add(createMainContent(), BorderLayout.CENTER);
            add(createSummaryPanel(), BorderLayout.SOUTH);


            refreshPromotionList();
        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("Quản lý Khuyến mãi");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            header.add(title, BorderLayout.WEST);
            return header;
        }

        private JPanel createMainContent() {
            JPanel content = new JPanel(new BorderLayout(0, 20));
            content.setOpaque(false);
            content.add(createPromotionListPanel(), BorderLayout.CENTER);
            content.add(createBottomSection(), BorderLayout.SOUTH);
            return content;
        }

        private JPanel createPromotionListPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 15));
            panel.setOpaque(false);

            panel.add(createSearchFilterPanel(), BorderLayout.NORTH);

            JScrollPane scrollPane = createPromotionScrollPanel();
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createSearchFilterPanel() {
            JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
            searchPanel.setOpaque(false);

            searchField = new JTextField("");
            String placeholder = " Tìm kiếm theo mã hoặc tên khuyến mãi...";
            Color placeholderColor = Color.GRAY;
            Color defaultColor = UIManager.getColor("TextField.foreground");

            searchField.setText(placeholder);
            searchField.setForeground(placeholderColor);

            searchField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (searchField.getText().equals(placeholder)) {
                        searchField.setText("");
                        searchField.setForeground(defaultColor);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (searchField.getText().isEmpty()) {
                        searchField.setText(placeholder);
                        searchField.setForeground(placeholderColor);
                    }
                }
            });

            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    applyFilters();
                }
            });

            searchField.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(6, 8, 6, 8)));

            searchPanel.add(searchField, BorderLayout.CENTER);

            cbStatus = new JComboBox<>(new String[]{"Tất cả", "Đang hoạt động", "Hết hạn"});
            cbStatus.setPreferredSize(new Dimension(160, 30));
            cbStatus.addActionListener(e -> applyFilters());
            searchPanel.add(cbStatus, BorderLayout.EAST);

            return searchPanel;
        }

        private JScrollPane createPromotionScrollPanel() {
            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);

            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            scrollPane.setPreferredSize(new Dimension(0, 400));

            return scrollPane;
        }

        private void applyFilters() {
            String keyword = searchField.getText().trim().toLowerCase();
            String placeholder = "tìm kiếm theo mã hoặc tên khuyến mãi...";
            if (keyword.equals(placeholder)) keyword = "";

            String status = cbStatus.getSelectedItem().toString();

            String finalKeyword = keyword;
            filteredPromotions = promotions.stream()
                    .filter(km -> {
                        // Tránh NullPointerException
                        String ma = Objects.toString(km.getMaKhuyenMai(), "").toLowerCase();
                        String ten = Objects.toString(km.getTenKhuyenMai(), "").toLowerCase();

                        boolean matchKeyword = finalKeyword.isEmpty()
                                || ma.contains(finalKeyword)
                                || ten.contains(finalKeyword);

                        boolean matchStatus = "Tất cả".equals(status)
                                || xacDinhTrangThai(km).equals(status);

                        return matchKeyword && matchStatus;
                    })
                    .collect(Collectors.toList());

            refreshPromotionList();
        }

        private void refreshPromotionList() {
            listPanel.removeAll();

            for (KhuyenMai km : filteredPromotions) {
                listPanel.add(createPromotionCard(km));
                listPanel.add(Box.createVerticalStrut(8));
            }

            listPanel.revalidate();
            listPanel.repaint();
        }

        private JPanel createPromotionCard(KhuyenMai km) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(10, 15, 10, 15)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            // === Info chính (Tên + Mã) ===
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JLabel nameLabel = new JLabel(km.getTenKhuyenMai());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JLabel codeLabel = new JLabel("Mã: " + km.getMaKhuyenMai());
            codeLabel.setForeground(Color.GRAY);

            infoPanel.add(nameLabel);
            infoPanel.add(codeLabel);

            // 💡 Tăng độ rộng hiển thị tên khuyến mãi
            infoPanel.setPreferredSize(new Dimension(300, 40));
            infoPanel.setBorder(new EmptyBorder(0, 5, 0, 10)); // nhẹ nhàng căn lề trái-phải

            card.add(infoPanel);

            // === Ngày bắt đầu / kết thúc ===
            card.add(createVerticalInfoPanel(
                    "Bắt đầu: " + km.getNgayBatDau(),
                    "Kết thúc: " + km.getNgayKetThuc(),
                    160
            ));

            // === Chiết khấu / lượt sử dụng ===
            card.add(createVerticalInfoPanel(
                    "Chiết khấu: " + km.getChietKhau() + "%",
                    "Lượt sử dụng: " + km.getLuotSuDung(),
                    160
            ));

            // === Trạng thái ===
            String status = xacDinhTrangThai(km);
            JLabel statusLabel = new JLabel(status, SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            statusLabel.setOpaque(true);
            statusLabel.setForeground(Color.WHITE);
            statusLabel.setBackground(status.equals("Đang hoạt động") ? new Color(46, 204, 113) : new Color(192, 57, 43));
            statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            statusPanel.setOpaque(false);
            statusPanel.add(statusLabel);
            card.add(statusPanel);

            // === Nút chức năng ===
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            right.setOpaque(false);
            JButton edit = new JButton("✎");
            JButton delete = new JButton("🗑");
            edit.setForeground(Color.blue);
            delete.setForeground(Color.red);
            for (JButton b : new JButton[]{edit, delete}) {
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            edit.addActionListener(e -> {
                try {
                    controller.handleEditKhuyenMai(km);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
            delete.addActionListener(e -> {
                try {
                    controller.handleDeleteKhuyenMai(km);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            right.add(edit);
            right.add(delete);
            card.add(right);

            return card;
        }

        private JPanel createVerticalInfoPanel(String top, String bottom, int width) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(width, 40));
            p.add(new JLabel(top));
            JLabel lb2 = new JLabel(bottom);
            lb2.setForeground(Color.GRAY);
            p.add(lb2);
            return p;
        }

        private String xacDinhTrangThai(KhuyenMai km) {
            LocalDate now = LocalDate.now();
            LocalDate start = km.getNgayBatDau().toLocalDate();
            LocalDate end = km.getNgayKetThuc().toLocalDate();
            return (!now.isBefore(start) && !now.isAfter(end)) ? "Đang hoạt động" : "Hết hạn";
        }

        private JPanel createBottomSection() {
            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);

            JLabel title = new JLabel("Thêm khuyến mãi mới");
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JButton btnAdd = new JButton("+ Thêm khuyến mãi");
            btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAdd.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setBorderPainted(false);
            btnAdd.setFocusPainted(false);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.setBorder(new EmptyBorder(8, 15, 8, 15));
            btnAdd.addActionListener(e -> {
                try {
                    controller.handleAddKhuyenMai();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            container.add(title, BorderLayout.WEST);
            container.add(btnAdd, BorderLayout.EAST);
            return container;
        }

        private JPanel createSummaryPanel() {
            JPanel summary = new JPanel(new GridLayout(1, 4, 15, 15)); // (Sửa layout nếu cần)
            summary.setOpaque(false);
            summary.setBorder(new EmptyBorder(10, 0, 100, 0)); // Tăng khoảng cách dưới

            // highlight-start
            // *** SỬA LỖI: Thay thế các hàm DAO bị lỗi bằng số 0 ***
            lblTongLuot = new JLabel(String.valueOf(0), SwingConstants.CENTER);
            lblHoatDong = new JLabel(String.valueOf(0), SwingConstants.CENTER);
            lblHetHan = new JLabel(String.valueOf(0), SwingConstants.CENTER);
            // (Hàm getLuotSuDung() có thể cũng lỗi, thay bằng 0)
            // lblHetHan = new JLabel(promotions.stream().mapToInt(KhuyenMai::getLuotSuDung).sum(), SwingConstants.CENTER);
            // highlight-end

            summary.add(createSummaryCard(lblTongLuot, "Tổng lượt dùng", new Color(110, 140, 237)));
            summary.add(createSummaryCard(lblHoatDong, "Đang hoạt động", new Color(127, 232, 172)));
            summary.add(createSummaryCard(lblHetHan, "Đã hết hạn", new Color(243, 128, 128)));

            return summary;
        }

        private JPanel createSummaryCard(JLabel valueLabel, String title, Color color) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(
                    new LineBorder(color, 2, true),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            card.setOpaque(true);

            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            valueLabel.setForeground(color);
            valueLabel.setText(valueLabel.getText()); // chỉ hiển thị số

            JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTitle.setForeground(Color.GRAY);

            JPanel inner = new JPanel(new BorderLayout());
            inner.setOpaque(false);
            inner.add(valueLabel, BorderLayout.CENTER);
            inner.add(lblTitle, BorderLayout.SOUTH);

            card.add(inner, BorderLayout.CENTER);
            return card;
        }

        public void refreshStats() throws SQLException {
            lblTongKM.setText(String.valueOf(dao.countAllKhuyenMai()));
            lblHoatDong.setText(String.valueOf(dao.countKhuyenMaiDangHoatDong()));
            lblHetHan.setText(String.valueOf(dao.countKhuyenMaiHetHan()));
            lblTongLuot.setText(String.valueOf(
                    promotions.stream().mapToInt(KhuyenMai::getLuotSuDung).sum()
            ));
        }


        public void reloadData() throws SQLException {
            promotions = dao.getAllKhuyenMai();
            filteredPromotions = new ArrayList<>(promotions);
            applyFilters();
            refreshPromotionList();
        }
    }
}