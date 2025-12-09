package ui.gui;

// ---------------------------
// Chú thích metadata (comment)
// Người code: Đỗ Nguyễn Thanh Bình
// Mô tả: Thêm nhãn chú thích hiển thị tên người chịu trách nhiệm / hoàn thiện phần giao diện Quản lý phòng
// Mục đích: Quản lý code, dễ dàng liên hệ khi cần chỉnh sửa
// Ngày tạo: 23/10/2025
// Giờ tạo: 01:55
// Lưu ý: cập nhật thời gian/ người sửa khi chỉnh sửa tiếp
// ---------------------------

// DAO, Entity, Event
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import dao.*;
import entity.*;
import event.*;
import entity.Phong;

// SWING, AWT
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.*;


// SQL (chỉ cần cho try-catch)
import java.sql.SQLException;
import java.text.DecimalFormat;
// TIME
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;


import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GUI_NhanVienLeTan extends JFrame {

    // --- Các hằng số màu sắc ---
    public static final Color SIDEBAR_BG = new Color(240, 248, 255);
    public static final Color MAIN_BG = new Color(242, 245, 250);
    public static final Color CARD_BORDER = new Color(222, 226, 230);
    public static final Color ACCENT_BLUE = new Color(24, 90, 219);
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_GREEN = new Color(50, 168, 82);
    public static final Color COLOR_RED = new Color(217, 30, 24);
    public static final Color COLOR_ORANGE = new Color(245, 124, 0);
    public static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    public static final Color COLOR_DISABLED_BG = new Color(220, 220, 220);
    public static final Color COLOR_SELECTED = new Color(138, 43, 226); // Màu tím khi chọn
    public static final Color COLOR_DISABLED_FG = new Color(150, 150, 150); // Chữ xám
    public static final Color COLOR_DISABLED_BORDER = new Color(180, 180, 180);
    public static final Color COLOR_AVATAR_BG = new Color(230, 230, 255);
    public static final Color STAT_BG_1 = new Color(218, 240, 255);
    public static final Color STAT_BG_2 = new Color(230, 235, 255);
    public static final Color STAT_BG_3 = new Color(255, 235, 240);

    public static final Color STATUS_GREEN_BG = new Color(225, 255, 230);
    public static final Color STATUS_GREEN_FG = new Color(30, 150, 50);
    public static final Color STATUS_RED_BG = new Color(255, 225, 225);
    public static final Color STATUS_RED_FG = new Color(180, 50, 50);
    public static final Color STATUS_ORANGE_BG = new Color(255, 240, 220);
    public static final Color STATUS_ORANGE_FG = new Color(245, 124, 0);
    public static final Color STATUS_YELLOW_BG = new Color(255, 250, 225);
    public static final Color STATUS_YELLOW_FG = new Color(180, 150, 0);

    private CardLayout cardLayout;
    private JPanel contentPanelContainer;

    private JButton btnDashboard;
    private JButton btnDatPhong;
    private JButton btnKhachHang;
    private JButton btnDichVu;
    private JButton btnPhong;
    private JButton btnCheckInCheckOut;

    // highlight-start
    private NhanVien nhanVienHienTai; // Biến lưu nhân viên đăng nhập
    private static final String CHECK_IN_OUT_CONTENT = "CHECK_IN_OUT_CONTENT";

    // highlight-end

    // highlight-start
    /**
     * Sửa đổi Constructor:
     * - Chấp nhận NhanVien đăng nhập.
     * - Bỏ 'throws SQLException'.
     */
    public GUI_NhanVienLeTan(NhanVien nhanVienDangNhap) {
        this.nhanVienHienTai = nhanVienDangNhap; // Lưu nhân viên
        // highlight-end

        setTitle("Quản lý Khách sạn TBQTT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Tạo Sidebar cố định và đặt vào WEST
        add(createStaticSidebar(), BorderLayout.WEST);

        // 2. Tạo Panel chứa CardLayout cho nội dung chính
        cardLayout = new CardLayout();
        contentPanelContainer = new JPanel(cardLayout);
        contentPanelContainer.setBackground(MAIN_BG);


        // 3. Tạo các Panel nội dung riêng biệt
        PanelLeTanContent panelLeTanContent = new PanelLeTanContent(nhanVienDangNhap);
        // highlight-start
        // Truyền nhanVienHienTai vào PanelDatPhongContent
        PanelDatPhongContent panelDatPhongContent = new PanelDatPhongContent(this.nhanVienHienTai);
        EventDatPhong datPhongController = panelDatPhongContent.controller;
        // highlight-end
        PanelKhachHangContent panelKhachHangContent = new PanelKhachHangContent();
        PanelDichVuContent panelDichVuContent = new PanelDichVuContent();
        PanelPhongContent panelPhongContent = new PanelPhongContent();
        PanelCheckInCheckOut panelCheckInCheckOut = new PanelCheckInCheckOut(this, this.nhanVienHienTai, datPhongController);


        // 4. Thêm các Panel nội dung vào CardLayout
        contentPanelContainer.add(panelLeTanContent, "LE_TAN_CONTENT");
        contentPanelContainer.add(panelDatPhongContent, "DAT_PHONG_CONTENT");
        contentPanelContainer.add(panelKhachHangContent, "KHACH_HANG_CONTENT");
        contentPanelContainer.add(panelDichVuContent, "DICH_VU_CONTENT");
        contentPanelContainer.add(panelPhongContent, "PHONG_CONTENT");
        contentPanelContainer.add(panelCheckInCheckOut, "CHECK_IN_OUT_CONTENT");


        // 5. Thêm Panel CardLayout vào CENTER của JFrame
        add(contentPanelContainer, BorderLayout.CENTER);

        // Hiển thị nội dung Lễ tân đầu tiên
        showContentPanel("LE_TAN_CONTENT");
    }

    /**
     * Tạo Sidebar cố định (chỉ gọi 1 lần khi khởi tạo JFrame)
     */
    /**
     * Tạo Sidebar cố định (chỉ gọi 1 lần khi khởi tạo JFrame)
     * (Viết lại, đã fix lỗi 'sidebar' và đồng bộ với 'nhanVienHienTai')
     */
    private JPanel createStaticSidebar() {
        // highlight-start
        // Đây là dòng code bị thiếu trong ảnh chụp màn hình lỗi của bạn
        JPanel sidebar = new JPanel(new BorderLayout());
        // highlight-end
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(SIDEBAR_BG);

        // Khu vực Logo
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

        // Khu vực Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(12, 12, 12, 12));
        menu.setOpaque(false);

        // Tạo các nút menu (lưu lại tham chiếu)
        btnDashboard = createNavButton("Dashboard");
        btnDatPhong = createNavButton("Đặt phòng");
        btnCheckInCheckOut = createNavButton("Check In/Out");
        btnKhachHang = createNavButton("Khách hàng");
        btnDichVu = createNavButton("Dịch vụ");
        btnPhong = createNavButton("Phòng");

        // Gắn ActionListener để chuyển đổi content panel
        btnDashboard.addActionListener(e -> showContentPanel("LE_TAN_CONTENT"));
        btnDatPhong.addActionListener(e -> showContentPanel("DAT_PHONG_CONTENT"));
        btnCheckInCheckOut.addActionListener(e -> showContentPanel("CHECK_IN_OUT_CONTENT"));
        btnPhong.addActionListener(e -> showContentPanel("PHONG_CONTENT"));
        btnKhachHang.addActionListener(e -> showContentPanel("KHACH_HANG_CONTENT"));
        btnDichVu.addActionListener(e -> showContentPanel("DICH_VU_CONTENT"));

        // Thêm nút vào menu
        menu.add(btnDashboard);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnDatPhong);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnCheckInCheckOut);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnKhachHang);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnPhong);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnDichVu);
        menu.add(Box.createVerticalStrut(8));

        sidebar.add(menu, BorderLayout.CENTER);

        // Khu vực Profile & Logout
        JPanel profile = new JPanel();
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(12, 12, 12, 12));
        profile.setOpaque(false);

        // highlight-start
        // --- SỬA: Lấy thông tin từ biến nhanVienHienTai ---
        // (Biến này được truyền vào từ constructor của GUI_NhanVienLeTan)
        JLabel user = new JLabel(nhanVienHienTai.getTenNV() != null ? nhanVienHienTai.getTenNV() : "Chưa đăng nhập");
        user.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel role = new JLabel(
                nhanVienHienTai.getChucVu() != null
                        ? nhanVienHienTai.getChucVu().getLabel()
                        : "Khách"
        );
        // --- Kết thúc sửa ---
        // highlight-end

        role.setFont(new Font("SansSerif", Font.PLAIN, 12));
        role.setForeground(Color.GRAY);
        JButton logout = new JButton("Đăng xuất");
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setForeground(new Color(220, 50, 50));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton exitButton = new JButton("Thoát Ứng Dụng");
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setForeground(new Color(200, 0, 0));
        exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));

        profile.add(user);
        profile.add(role);
        profile.add(Box.createVerticalStrut(10));
        profile.add(logout);
        sidebar.add(profile, BorderLayout.SOUTH);

        // Đặt trạng thái active ban đầu cho nút Dashboard
        setActiveButton(btnDashboard);

        return sidebar;
    }

    /**
     * Helper: Tạo một nút điều hướng chuẩn
     */
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        return btn;
    }

    /**
     * Helper: Đặt trạng thái active cho nút được chọn và reset các nút khác
     */
    private void setActiveButton(JButton activeButton) {
        JButton[] allButtons = {btnDashboard, btnDatPhong, btnKhachHang, btnDichVu, btnPhong, btnCheckInCheckOut};
        for (JButton btn : allButtons) {
            if (btn == activeButton) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(ACCENT_BLUE);
                btn.setOpaque(true);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_BLUE, 2, true),
                        new EmptyBorder(6, 12, 6, 12)));
            } else if (btn != null) {
                btn.setForeground(Color.BLACK);
                btn.setOpaque(false);
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
        if (panelName.equals("LE_TAN_CONTENT")) {
            setActiveButton(btnDashboard);
        } else if (panelName.equals("DAT_PHONG_CONTENT")) {
            setActiveButton(btnDatPhong);
        }
        else if (panelName.equals("CHECK_IN_OUT_CONTENT")) {
            setActiveButton(btnCheckInCheckOut);
        }else if (panelName.equals("PHONG_CONTENT")) {
            setActiveButton(btnPhong);
        } else if (panelName.equals("KHACH_HANG_CONTENT")) {
            setActiveButton(btnKhachHang);
        } else if (panelName.equals("DICH_VU_CONTENT")) {
            setActiveButton(btnDichVu);
        }
    }

    // Phương thức main để chạy ứng dụng
    // Phương thức main để chạy ứng dụng
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Ứng dụng đang tắt, đóng kết nối CSDL...");
            // Nhớ import connectDB.ConnectDB;
            // ConnectDB.disconnect();
        }));

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // highlight-start
            // Bỏ try-catch SQLException
            try {
                // TODO: Thay thế bằng logic Login thực tế để lấy NhanVien
                NhanVien nhanVienLogin = new NhanVien();
                nhanVienLogin.setMaNV("NV001");
                nhanVienLogin.setTenNV("Nguyễn Văn Lễ Tân");

                new GUI_NhanVienLeTan(nhanVienLogin).setVisible(true);
            } catch (Exception e) { // Bắt Exception chung
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Không thể khởi động ứng dụng: " + e.getMessage(), "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
            }
            // highlight-end
        });
    }


    // =================================================================================
// PANEL NỘI DUNG 1: MÀN HÌNH DASHBOARD 
// =================================================================================
    class PanelLeTanContent extends JPanel {
        private NhanVien nhanVien;
        private final Color STAT_BG_1 = new Color(218, 240, 255);
        private final Color STAT_BG_2 = new Color(230, 235, 255);
        private final Color STAT_BG_3 = new Color(255, 235, 240);

        public PanelLeTanContent(NhanVien nhanVienDangNhap) {
            this.nhanVien = nhanVienDangNhap; // Lưu nhân viên đăng nhập
            // --- Thiết lập cho JPanel này ---
            setLayout(new BorderLayout());
            setBackground(GUI_NhanVienLeTan.MAIN_BG);
            setBorder(new EmptyBorder(18, 18, 18, 18));

            // --- Chỉ thêm Header và Content Panel ---
            add(createHeader(), BorderLayout.NORTH);
            add(createContentPanel(), BorderLayout.CENTER);

        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("Dashboard Nhân viên Lễ tân");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy");
            JLabel date = new JLabel(fmt.format(LocalDate.now()));
            date.setForeground(Color.GRAY);
            header.add(title, BorderLayout.WEST);
            header.add(date, BorderLayout.EAST);
            return header;
        }

        private JPanel createContentPanel() {
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);

            JPanel topZone = createTopProfileCard();
            topZone.setPreferredSize(new Dimension(Integer.MAX_VALUE, 100));
            content.add(topZone, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(12, 0, 12, 0);
            gc.fill = GridBagConstraints.BOTH;
            gc.gridwidth = 1;
            gc.gridx = 0;
            gc.weightx = 1.0;

            gc.gridy = 0;
            gc.weighty = 0.4;
            JPanel schedulePanel = createSchedulePanel(nhanVien.getMaNV());
            schedulePanel.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(14, 14, 14, 14)));
            schedulePanel.setBackground(Color.WHITE);
            centerPanel.add(schedulePanel, gc);

            gc.gridy = 1;
            gc.weighty = 0.6;
            JPanel bottomSection = new JPanel(new GridBagLayout());
            bottomSection.setOpaque(false);

            GridBagConstraints gc2 = new GridBagConstraints();
            gc2.fill = GridBagConstraints.BOTH;
            gc2.insets = new Insets(12, 0, 12, 12);

            gc2.gridx = 0;
            gc2.gridy = 0;
            gc2.weightx = 0.65;
            gc2.weighty = 1.0;
            bottomSection.add(createTasksPanel(), gc2);

            gc2.gridx = 1;
            gc2.weightx = 0.35;
            gc2.insets = new Insets(12, 0, 12, 0);
            bottomSection.add(createStatsPanel(), gc2);

            centerPanel.add(bottomSection, gc);
            content.add(centerPanel, BorderLayout.CENTER);
            return content;
        }

        private JPanel createTopProfileCard() {

            JPanel card = new JPanel(new BorderLayout(20, 0));
            card.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(14, 14, 14, 14)));
            card.setBackground(Color.WHITE);

            // ===== LEFT: Avatar + Info =====
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
            left.setOpaque(false);

            // --- Lấy thông tin từ nhân viên ---
            String tenNV = nhanVien != null ? nhanVien.getTenNV() : "Không rõ tên";
            String email = nhanVien != null ? nhanVien.getEmail() : "Không có email";
            String sdt = nhanVien != null ? nhanVien.getSoDT() : "Không có SĐT";
            String maNV = nhanVien != null ? nhanVien.getMaNV() : "N/A";

            // --- Tạo avatar bằng ký tự đầu tên ---
            String initials = "LT";
            if (tenNV != null && !tenNV.isBlank()) {
                String[] parts = tenNV.trim().split(" ");
                initials = parts[parts.length - 1].substring(0, 1).toUpperCase();
            }

            JLabel avatar = new JLabel(initials);
            avatar.setPreferredSize(new Dimension(64, 64));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(new Color(120, 150, 255));
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("SansSerif", Font.BOLD, 20));
            avatar.setBorder(new LineBorder(new Color(100, 120, 220), 2, true));
            left.add(avatar);

            // --- Tạo phần thông tin ---
            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(tenNV);
            name.setFont(new Font("SansSerif", Font.BOLD, 16));

            JLabel details = new JLabel(String.format("<html>%s • %s • Mã NV: %s</html>", email, sdt, maNV));
            details.setForeground(Color.GRAY);
            details.setFont(new Font("SansSerif", Font.PLAIN, 12));

            info.add(name);
            info.add(Box.createVerticalStrut(4));
            info.add(details);
            left.add(info);

            // ===== RIGHT: Stats =====
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            right.setOpaque(false);

            LichLamViec_DAO dao = new LichLamViec_DAO();
            int tongGioTuan = dao.tinhTongGioLamTrongTuan(nhanVien.getMaNV());
            JPanel boxHours = createStatBox(String.valueOf(tongGioTuan), "Giờ tuần này", STAT_BG_1);
            JPanel boxCheckIn = createStatBox("12", "Check-in", STAT_BG_2);
            JPanel boxCheckOut = createStatBox("10", "Check-out", STAT_BG_3);

            // --- Gắn sự kiện click ---
            EventDashBoardLeTan.addStatBoxClickEvent(boxCheckIn, "checkin");
            EventDashBoardLeTan.addStatBoxClickEvent(boxCheckOut, "checkout");

            right.add(boxHours);
            right.add(boxCheckIn);
            right.add(boxCheckOut);

            // ===== Combine =====
            card.add(left, BorderLayout.WEST);
            card.add(right, BorderLayout.EAST);

            return card;
        }

        private JPanel createSchedulePanel(String maNV) {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);

            JLabel title = new JLabel("Lịch làm việc tuần này");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            wrap.add(title, BorderLayout.NORTH);

            JPanel grid = new JPanel();
            grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
            grid.setOpaque(false);

            LichLamViec_DAO dao = new LichLamViec_DAO();
            List<LichLamViec> list = dao.getLichLamTheoMaNV(maNV);

            LocalDate today = LocalDate.now();

            for (LichLamViec llv : list) {
                LocalDate ngayLam = llv.getNgayLam().toLocalDate();
                DayOfWeek dayOfWeek = ngayLam.getDayOfWeek();

                // Tạo panel cho từng ngày
                JPanel dayRow = new JPanel(new BorderLayout(15, 0));
                dayRow.setOpaque(true);
                dayRow.setBackground(Color.WHITE);
                dayRow.setBorder(new CompoundBorder(
                        new EmptyBorder(8, 8, 8, 8),
                        new LineBorder(new Color(240, 240, 245))
                ));
                dayRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

                // --- Cột ngày ---
                JPanel dayCol = new JPanel();
                dayCol.setLayout(new BoxLayout(dayCol, BoxLayout.Y_AXIS));
                dayCol.setOpaque(false);
                dayCol.setPreferredSize(new Dimension(100, 40));

                JLabel dayName = new JLabel(formatDayName(dayOfWeek)); // "Thứ Hai", "Thứ Ba"...
                dayName.setFont(new Font("SansSerif", Font.BOLD, 12));
                JLabel dateLabel = new JLabel(ngayLam.format(DateTimeFormatter.ofPattern("dd/MM")));
                dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
                dateLabel.setForeground(Color.GRAY);
                dayCol.add(dayName);
                dayCol.add(Box.createVerticalStrut(2));
                dayCol.add(dateLabel);
                dayRow.add(dayCol, BorderLayout.WEST);

                // --- Cột ca làm ---
                JPanel shiftCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                shiftCol.setOpaque(false);
                String timeRange = llv.getGioBatDau() + " - " + llv.getGioKetThuc();
                JLabel shift = new JLabel(llv.getCaLam() + " (" + timeRange + ")");
                shift.setFont(new Font("SansSerif", Font.PLAIN, 12));
                JLabel hours = new JLabel("  " + llv.getGioCong());
                hours.setFont(new Font("SansSerif", Font.PLAIN, 11));
                hours.setForeground(Color.GRAY);
                shiftCol.add(shift);
                shiftCol.add(hours);
                dayRow.add(shiftCol, BorderLayout.CENTER);

                // --- Cột trạng thái + tăng ca ---
                JPanel statusCol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                statusCol.setOpaque(false);
                statusCol.setPreferredSize(new Dimension(300, 40));

                JLabel tangCaLabel = new JLabel();
                if (llv.getTangCa() != null && !llv.getTangCa().isEmpty()) {
                    tangCaLabel.setText("Tăng ca: " + llv.getTangCa());
                    tangCaLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    tangCaLabel.setForeground(new Color(200, 90, 10));
                    statusCol.add(tangCaLabel);
                }

                JLabel status = new JLabel();
                status.setOpaque(true);
                status.setFont(new Font("SansSerif", Font.PLAIN, 11));
                status.setBorder(new EmptyBorder(4, 10, 4, 10));

                String tt = llv.getTrangThai();

                // Lấy thứ trong tuầnNVNV

                // 🔹 Nếu là Thứ 7 hoặc Chủ nhật → Nghỉ
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    tt = "Nghỉ";
                } else {
                    // 🔹 Nếu không có trạng thái thì tự động xác định theo ngày
                    if (tt == null || tt.isEmpty()) {
                        if (ngayLam.isBefore(today)) {
                            tt = "Hoàn thành";
                        } else if (ngayLam.equals(today)) {
                            tt = "Đang làm";
                        } else {
                            tt = "Chưa làm";
                        }
                    }
                }

                status.setText(tt);

                // 🔹 Đặt màu theo trạng thái
                switch (tt) {
                    case "Hoàn thành" -> {
                        status.setBackground(new Color(220, 255, 230));
                        status.setForeground(new Color(25, 120, 50));
                    }
                    case "Đang làm" -> {
                        status.setBackground(new Color(230, 245, 255));
                        status.setForeground(new Color(10, 90, 180));
                    }
                    case "Chưa làm" -> {
                        status.setBackground(new Color(255, 245, 230));
                        status.setForeground(new Color(180, 110, 20));
                    }
                    case "Nghỉ" -> {
                        status.setBackground(new Color(245, 245, 245));
                        status.setForeground(new Color(130, 130, 130));
                    }
                }

                statusCol.add(status);
                dayRow.add(statusCol, BorderLayout.EAST);

                // --- Tô viền cho ngày hiện tại ---
                if (ngayLam.equals(today)) {
                    dayRow.setBorder(new CompoundBorder(
                            new EmptyBorder(8, 8, 8, 8),
                            new LineBorder(new Color(100, 150, 255), 2, true)
                    ));
                }

                grid.add(dayRow);
                grid.add(Box.createVerticalStrut(6));
            }

            JScrollPane scroll = new JScrollPane(grid);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            wrap.add(scroll, BorderLayout.CENTER);

            return wrap;
        }

        private String formatDayName(DayOfWeek day) {
            switch (day) {
                case MONDAY: return "Thứ Hai";
                case TUESDAY: return "Thứ Ba";
                case WEDNESDAY: return "Thứ Tư";
                case THURSDAY: return "Thứ Năm";
                case FRIDAY: return "Thứ Sáu";
                case SATURDAY: return "Thứ Bảy";
                case SUNDAY: return "Chủ Nhật";
                default: return "";
            }
        }

        private JPanel createTasksPanel() {
            JPanel tasks = new JPanel(new BorderLayout());
            tasks.setBackground(Color.WHITE);
            tasks.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(14, 14, 14, 14)));

            JLabel title = new JLabel("Nhiệm vụ & Thống kê hôm nay");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            tasks.add(title, BorderLayout.NORTH);

            // --- Lấy dữ liệu từ SQL ---
            LichLamViec_DAO dao = new LichLamViec_DAO();
            List<LichLamViec> list = dao.getLichLamTheoMaNV(nhanVien.getMaNV());

            String[] columns = {"Thời gian", "Nhiệm vụ", "Trạng thái", "Ghi chú"};
            Object[][] data = new Object[list.size()][4];

            for (int i = 0; i < list.size(); i++) {
                LichLamViec llv = list.get(i);
                data[i][0] = llv.getThoiGianNV();
                data[i][1] = llv.getNhiemVu();
                data[i][2] = llv.getTrangThaiNhiemVu();
                data[i][3] = llv.getGhiChu();
            }

            JTable table = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setRowHeight(40);
            table.getTableHeader().setReorderingAllowed(false);

            // Renderer trạng thái
            table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setBorder(new EmptyBorder(4, 8, 4, 8));
                    if (value != null) {
                        String status = value.toString();
                        if (status.equals("Hoàn thành")) {
                            label.setBackground(new Color(220, 255, 230));
                            label.setForeground(new Color(25, 120, 50));
                        } else if (status.equals("Đang làm")) {
                            label.setBackground(new Color(230, 245, 255));
                            label.setForeground(new Color(10, 90, 180));
                        } else if (status.equals("Chưa xong")) {
                            label.setBackground(new Color(255, 245, 230));
                            label.setForeground(new Color(180, 110, 20));
                        }else if (status.equals("Chờ xử lý")) {
                            label.setBackground(new Color(246, 239, 189));
                            label.setForeground(new Color(168, 139, 0));
                        }
                    }
                    return label;
                }
            });

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(null);
            tasks.add(scroll, BorderLayout.CENTER);
            return tasks;
        }

        private JPanel createStatsPanel() {
            JPanel stats = new JPanel(new GridLayout(3, 2, 12, 12));
            stats.setOpaque(false);
            LichLamViec_DAO lichLamViecDAO = new LichLamViec_DAO();
            //đếm số giờ làm
            double tongGio = lichLamViecDAO.tinhTongGioLamTrongTuan(nhanVien.getMaNV());

            stats.add(createStatCard(String.format("%.0fh", tongGio), "Tổng giờ làm", STAT_BG_1));

            //thống kê ca làm
            int[] thongKe = lichLamViecDAO.getThongKeCaTuan(nhanVien.getMaNV());
            int caHoanThanh = thongKe[0];
            int tongCa = thongKe[1];

            stats.add(createStatCard(caHoanThanh + "/" + tongCa, "Ca hoàn thành", new Color(220, 255, 230)));
            stats.add(createStatCard("12", "Check-in hôm nay", new Color(245, 235, 255)));
            stats.add(createStatCard("10", "Check-out hôm nay", new Color(255, 240, 230)));
            int soGhiChu = lichLamViecDAO.getSoGhiChu("NV001");
            stats.add(createStatCard(String.valueOf(soGhiChu), "Yêu cầu", new Color(255, 235, 245)));

            int tongGioTangCa = lichLamViecDAO.getTongGioTangCaInt("NV001");
            stats.add(createStatCard(tongGioTangCa + "h", "Tăng ca", new Color(250, 245, 230)));
            return stats;
        }

        private JPanel createStatBox(String value, String label, Color bg) {
            JPanel box = new JPanel(new BorderLayout());
            box.setPreferredSize(new Dimension(110, 60));
            box.setBackground(bg);
            box.setBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER));
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel textLabel = new JLabel(label);
            textLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            textLabel.setForeground(Color.GRAY);
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);
            box.add(valueLabel, BorderLayout.CENTER);
            box.add(textLabel, BorderLayout.SOUTH);
            return box;
        }

        private JPanel createStatCard(String value, String label, Color bg) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(bg);
            card.setBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER));
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel textLabel = new JLabel(label);
            textLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            textLabel.setForeground(Color.GRAY);
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(valueLabel, BorderLayout.CENTER);
            card.add(textLabel, BorderLayout.SOUTH);
            return card;
        }

        private DayOfWeek getDayOfWeek(String dayName) {
            switch (dayName) {
                case "Thứ Hai":
                    return DayOfWeek.MONDAY;
                case "Thứ Ba":
                    return DayOfWeek.TUESDAY;
                case "Thứ Tư":
                    return DayOfWeek.WEDNESDAY;
                case "Thứ Năm":
                    return DayOfWeek.THURSDAY;
                case "Thứ Sáu":
                    return DayOfWeek.FRIDAY;
                case "Thứ Bảy":
                    return DayOfWeek.SATURDAY;
                case "Chủ Nhật":
                    return DayOfWeek.SUNDAY;
                default:
                    return null;
            }
        }
    }
// =================================================================================
// PANEL NỘI DUNG 2: ĐẶT PHÒNG
// =================================================================================

    public static class PanelDatPhongContent extends JPanel {
        // --- Biến tham chiếu DAO ---
        private PhieuDatPhong_DAO phieuDatPhongDAO;
        private Phong_DAO phongDAO;

        // --- Biến tham chiếu UI ---
        private JTextField searchField;
        private JComboBox<String> bookingFilterComboBox;
        private JTextField fromDate; // Từ ngày
        private JTextField toDate;   // Đến ngày
        private JPanel cardListPanelContainer;
        private JPanel roomGridPanel;
        private JPanel filterButtonsPanel;
        private ButtonGroup typeGroup;
        private ButtonGroup peopleGroup;
        private ButtonGroup floorGroup;
        private ButtonGroup statusGroup;
        private JButton btnContinueBooking;
        // New action buttons for booking (top-right on Đặt phòng)
        private JButton btnBookLater;
        private JButton btnBookAndCheckin;

        // --- Biến tham chiếu Controller ---
        public EventDatPhong controller;

        // --- Biến trạng thái ---
        private Set<String> selectedRoomIds = new HashSet<>();

        /**
         * Sửa Constructor:
         * - Chấp nhận NhanVien đăng nhập.
         * - Bỏ 'throws SQLException'.
         * - Bọc các lệnh gọi DAO trong try-catch.
         */
        public PanelDatPhongContent(NhanVien nhanVienDangNhap) {
            // Khởi tạo Controller và DAO (Giữ nguyên)
            try {
                this.phieuDatPhongDAO = new PhieuDatPhong_DAO();
                this.phongDAO = new Phong_DAO();
                this.controller = new EventDatPhong(this, nhanVienDangNhap);
            } catch (Exception e) { /* ... */ }

            // Thiết lập layout và nền (Giữ nguyên)
            setLayout(new BorderLayout()); /* ... */

            searchField = new JTextField();
            bookingFilterComboBox = new JComboBox<>(new String[]{"Tất cả", "Đã xác nhận", "Đã nhận phòng"});
            cardListPanelContainer = new JPanel();
            roomGridPanel = new JPanel();
            filterButtonsPanel = new JPanel();

            // Initialize the buttons first
            btnContinueBooking = new JButton("Tiếp tục (0 phòng)");
            btnContinueBooking.setEnabled(false);
            btnBookLater = new JButton("Đặt phòng trước (0)");
            btnBookLater.setEnabled(false);
            btnBookAndCheckin = new JButton("Đặt & Check-in ngay (0)");
            btnBookAndCheckin.setEnabled(false);

            // Add UI components
            add(createHeader(), BorderLayout.NORTH);
            try {
                add(createMainContent(), BorderLayout.CENTER);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Attach listeners
            if (this.controller != null) {
                this.controller.initListeners();
            }
            updateContinueButton();
        }

        // --- Các Getter cho Controller ---
        public JTextField getSearchField() { return searchField; }
        public JComboBox<String> getBookingFilterComboBox() { return bookingFilterComboBox; }
        public JPanel getCardListPanelContainer() { return cardListPanelContainer; }
        public JPanel getRoomGridPanel() { return roomGridPanel; }
        public JPanel getFilterButtonsPanel() { return filterButtonsPanel; }
        public ButtonGroup getTypeGroup() { return typeGroup; }
        public ButtonGroup getPeopleGroup() { return peopleGroup; }
        public ButtonGroup getFloorGroup() { return floorGroup; }
        public ButtonGroup getStatusGroup() { return statusGroup; }
        public Set<String> getSelectedRoomIds() { return selectedRoomIds; }
        public JTextField getFromDate() { return fromDate; }
        public JTextField getToDate() { return toDate; }
        public JButton getBtnBookLater() {
            return btnBookLater;
        }

        public JButton getBtnBookAndCheckin() {
            return btnBookAndCheckin;
        }

        // --- Các hàm tạo giao diện (Sửa: Bỏ 'throws SQLException') ---
        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            // Consistent bottom padding
            header.setBorder(new EmptyBorder(0, 0, 15, 0)); // Add/Adjust this line

            // Consistent Title Font
            JLabel title = new JLabel("Đặt Phòng"); // Change title accordingly
            title.setFont(new Font("SansSerif", Font.BOLD, 20)); // Consistent font
            header.add(title, BorderLayout.WEST);

            // Optional: Add consistent date label if needed (like in Dashboard)
            // DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy");
            // JLabel date = new JLabel(fmt.format(LocalDate.now()));
            // date.setForeground(Color.GRAY);
            // header.add(date, BorderLayout.EAST);

            return header;
        }

        private JPanel createMainContent() { // Bỏ 'throws SQLException'
            JPanel content = new JPanel(new BorderLayout(0, 25));
            content.setOpaque(false);
            content.setBorder(new EmptyBorder(0, 5, 5, 5));
            // Chỉ hiển thị panel chọn phòng
            content.add(createRoomSelectionPanel(), BorderLayout.CENTER);
            return content;
        }

        private JPanel createBookingsListPanel() { // Bỏ 'throws SQLException'
            JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
            mainPanel.setOpaque(false);
            mainPanel.add(createSearchFilterPanel(), BorderLayout.NORTH);

            // Bọc hàm có thể ném lỗi
            try {
                mainPanel.add(createCardListScrollPane(), BorderLayout.CENTER);
            } catch (Exception e) {
                e.printStackTrace();
                mainPanel.add(new JLabel("Lỗi tải danh sách đặt phòng."), BorderLayout.CENTER);
            }
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(mainPanel, BorderLayout.NORTH);
            return wrapper;
        }

        private JPanel createSearchFilterPanel() {
            JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
            searchPanel.setOpaque(false);
            searchField = new JTextField();
            String placeholder = " Tìm kiếm...";
            // ... (Code placeholder y như cũ)
            searchField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (searchField.getText().equals(placeholder)) {
                        searchField.setText("");
                        searchField.setForeground(UIManager.getColor("TextField.foreground"));
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (searchField.getText().isEmpty()) {
                        searchField.setText(placeholder);
                        searchField.setForeground(Color.GRAY);
                    }
                }
            });
            searchField.setBorder(new CompoundBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER), new EmptyBorder(5, 8, 5, 8)));
            // ... (Code UI ComboBox y như cũ)
            searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            return searchPanel;
        }

        private JScrollPane createCardListScrollPane() { // Bỏ 'throws SQLException'
            cardListPanelContainer = new JPanel();
            cardListPanelContainer.setLayout(new BoxLayout(cardListPanelContainer, BoxLayout.Y_AXIS));
            cardListPanelContainer.setOpaque(false);
            // Hàm getAllBookingData() đã được bọc try-catch
            populateBookingCards(getAllBookingData()); // Load dữ liệu ban đầu
            JScrollPane scrollPane = new JScrollPane(cardListPanelContainer);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            scrollPane.setPreferredSize(new Dimension(0, 250));
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
            return scrollPane;
        }

        /**
         * Xóa thẻ cũ và vẽ lại thẻ đặt phòng.
         */
        public void populateBookingCards(Object[][] data) {
            cardListPanelContainer.removeAll();
            if (data != null && controller != null) { // Thêm kiểm tra controller != null
                for (Object[] rowData : data) {
                    try {
                        // Mapping Object[] từ DAO:
                        // {hoTen, sdt, maPhong, ngayNhanStr, ngayTraStr, maPhieu, tenTrangThai(Phong), status(UI), maKH}
                        String name = (String) rowData[0];
                        String phone = (String) rowData[1];
                        String roomNum = (String) rowData[2];
                        String ngayNhan = (String) rowData[3];
                        String ngayTra = (String) rowData[4];
                        String bookingId = (String) rowData[5]; // maPhieu
                        // String tenTrangThaiPhong = (String) rowData[6]; // Trạng thái phòng (Sẵn sàng, Đã thuê...)
                        int statusUI = (int) rowData[7]; // Trạng thái UI (1=Xác nhận, 2=Nhận phòng)
                        // String maKH = (String) rowData[8];

                        // --- Tính toán giá tiền đơn giản ---
                        String priceStr = "N/A";
                        try {
                            // Lấy giá phòng gốc từ CSDL (cần truy vấn lại hoặc DAO trả về)
                            // Tạm thời lấy giá phòng trực tiếp (có thể chậm)
                            Phong phong = phongDAO.getPhongById(roomNum);
                            double pricePerNight = (phong != null) ? phong.getGiaTienMotDem() : 0;

                            // Tính số đêm
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate checkin = LocalDate.parse(ngayNhan, dtf);
                            LocalDate checkout = LocalDate.parse(ngayTra, dtf);
                            long durationDays = ChronoUnit.DAYS.between(checkin, checkout);
                            if(durationDays == 0) durationDays = 1; // Ít nhất 1 đêm

                            priceStr = String.format("%,.0f đ", pricePerNight * durationDays);
                        } catch (Exception eCalc) {
                            System.err.println("Lỗi tính giá tiền cho thẻ đặt phòng: " + eCalc.getMessage());
                        }
                        // ------------------------------------

                        // Gọi hàm tạo thẻ với dữ liệu đã map
                        cardListPanelContainer.add(createBookingCard(
                                name, phone, roomNum,
                                "Nhận: " + ngayNhan, // line1_sub
                                "Trả: " + ngayTra,    // line2_sub
                                "Mã: " + bookingId,   // line3_sub
                                priceStr, statusUI, bookingId, controller
                        ));
                        cardListPanelContainer.add(Box.createVerticalStrut(10));
                    } catch (Exception e) {
                        System.err.println("Lỗi nghiêm trọng khi tạo thẻ đặt phòng: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else if (controller == null) {
                System.err.println("Lỗi: Controller chưa được khởi tạo trong PanelDatPhongContent!");
            }
            cardListPanelContainer.revalidate();
            cardListPanelContainer.repaint();
        }

        /**
         * Tạo thẻ đặt phòng (gắn listener gọi controller).
         */
        private JPanel createBookingCard(String name, String phone, String roomNum, String line1_sub, String line2_sub, String line3_sub, String price, int status, String bookingId, EventDatPhong ctrl) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER), new EmptyBorder(12, 8, 12, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(createVerticalInfoPanel(name, phone, 180));
            card.add(Box.createHorizontalStrut(10));
            card.add(createVerticalInfoPanel(roomNum, line1_sub, 120)); // Hiển thị mã phòng và ngày nhận
            card.add(Box.createHorizontalStrut(10));
            card.add(createVerticalInfoPanel(line2_sub, line3_sub, 120)); // Hiển thị ngày trả và mã phiếu
            card.add(Box.createHorizontalStrut(10));
            card.add(createVerticalInfoPanel(price, "", 120));
            card.add(Box.createHorizontalGlue());
            JLabel statusLabel = new JLabel();
            JButton actionButton = new JButton();
            actionButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            actionButton.setFocusPainted(false);
            actionButton.setBorderPainted(false);
            actionButton.setOpaque(true);
            actionButton.setContentAreaFilled(true);
            actionButton.setForeground(GUI_NhanVienLeTan.COLOR_WHITE);
            Color buttonColor;

            // Dùng mã trạng thái (TT_PDP_...)
            if (status == 1) { // 1 = Đã xác nhận
                statusLabel.setText("Đã xác nhận");
                statusLabel.setForeground(GUI_NhanVienLeTan.COLOR_GREEN);
                actionButton.setText("In");
                actionButton.setBackground(GUI_NhanVienLeTan.COLOR_GREEN);
                buttonColor = GUI_NhanVienLeTan.COLOR_GREEN;
                actionButton.addActionListener(e -> {
                    ctrl.handleCheckIn(bookingId, name, roomNum);
                });
            } else if (status == 2) { // 2 = Đã nhận phòng
                statusLabel.setText("Đã nhận phòng");
                statusLabel.setForeground(GUI_NhanVienLeTan.COLOR_ORANGE);
                actionButton.setText("Out");
                actionButton.setBackground(GUI_NhanVienLeTan.COLOR_ORANGE);
                buttonColor = GUI_NhanVienLeTan.COLOR_ORANGE;
                actionButton.addActionListener(e -> {
                    ctrl.handleCheckOut(bookingId, name, roomNum);
                });
            } else { // Các trạng thái khác
                statusLabel.setText("Đã xử lý"); // Ví dụ: Đã trả, đã hủy
                statusLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
                actionButton.setText("Xem");
                actionButton.setBackground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
                buttonColor = GUI_NhanVienLeTan.COLOR_TEXT_MUTED;
                actionButton.addActionListener(e -> {
                    ctrl.handleShowBill(bookingId); // Ví dụ: chỉ cho xem bill
                });
            }

            Border lineBorder = new LineBorder(buttonColor.darker(), 1);
            Border paddingBorder = new EmptyBorder(5, 15, 5, 15);
            actionButton.setBorder(new CompoundBorder(lineBorder, paddingBorder));
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            card.add(statusLabel);
            card.add(Box.createHorizontalStrut(15));
            card.add(actionButton);
            card.add(Box.createHorizontalStrut(5));
            JButton btnEdit = createCardActionButton("📄", Color.BLUE, "Xem hóa đơn", bookingId, ctrl::handleShowBill);
            JButton btnView = createCardActionButton("👁️", new Color(108, 117, 125), "Xem chi tiết", bookingId, ctrl::handleViewBooking);
            JButton btnDelete = createCardActionButton("🗑️", Color.RED, "Xóa đặt phòng", bookingId, ctrl::handleDeleteBooking);
            card.add(btnEdit);
            card.add(btnView);
            card.add(btnDelete);
            return card;
        }

        /**
         * Helper tạo nút biểu tượng
         */
        private JButton createCardActionButton(String unicodeChar, Color color, String tooltip, String id, java.util.function.Consumer<String> action) {
            JButton button = new JButton(unicodeChar);
            button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            button.setForeground(color);
            button.setToolTipText(tooltip);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setMargin(new Insets(2, 2, 2, 2));
            button.addActionListener(e -> action.accept(id));
            return button;
        }

        /**
         * Helper tạo panel thông tin 2 dòng
         */
        private JPanel createVerticalInfoPanel(String line1, String line2, int width) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            JLabel l1 = new JLabel(line1);
            l1.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l1.setForeground(Color.BLACK);
            panel.add(l1);
            JLabel l2 = new JLabel(line2);
            l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l2.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            panel.add(l2);
            Dimension d = new Dimension(width, 50);
            panel.setPreferredSize(d);
            panel.setMinimumSize(d);
            panel.setMaximumSize(d);
            panel.setAlignmentY(Component.TOP_ALIGNMENT);
            return panel;
        }

        /**
         * Tạo panel chọn phòng (với nút Tiếp tục)
         */
        private JPanel createRoomSelectionPanel() { // Bỏ 'throws SQLException'
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setOpaque(false);
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            JLabel title = new JLabel("Chọn phòng để đặt");
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            headerPanel.add(title, BorderLayout.WEST);

            // Top controls: date range + big action buttons
            JPanel topControls = new JPanel(new BorderLayout(8, 0));
            topControls.setOpaque(false);

            // Panel chọn ngày
            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            datePanel.setOpaque(false);

            // Text field từ ngày
            fromDate = new JTextField("dd/MM/yyyy");
            fromDate.setPreferredSize(new Dimension(120, 28));
            fromDate.setForeground(Color.GRAY);
            fromDate.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (fromDate.getText().equals("dd/MM/yyyy")) {
                        fromDate.setText("");
                        fromDate.setForeground(Color.BLACK);
                    }
                }

                @Override 
                public void focusLost(FocusEvent e) {
                    if (fromDate.getText().isEmpty()) {
                        fromDate.setText("dd/MM/yyyy");
                        fromDate.setForeground(Color.GRAY);
                    }
                }
            });

            // Text field đến ngày
            toDate = new JTextField("dd/MM/yyyy");
            toDate.setPreferredSize(new Dimension(120, 28));
            toDate.setForeground(Color.GRAY);
            toDate.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (toDate.getText().equals("dd/MM/yyyy")) {
                        toDate.setText("");
                        toDate.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (toDate.getText().isEmpty()) {
                        toDate.setText("dd/MM/yyyy");
                        toDate.setForeground(Color.GRAY);
                    }
                }
            });

            // Thêm nút lọc
            JButton filterButton = new JButton("Lọc");
            filterButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            filterButton.setBackground(new Color(0, 150, 136));
            filterButton.setForeground(Color.WHITE);
            filterButton.setBorderPainted(false);
            filterButton.setFocusPainted(false);
            filterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            filterButton.addActionListener(e -> {
                if (controller != null) {
                    controller.filterRooms(); // Gọi hàm lọc khi nhấn nút
                }
            });

            datePanel.add(new JLabel("Từ ngày"));
            datePanel.add(fromDate);
            datePanel.add(new JLabel("Đến ngày"));
            datePanel.add(toDate);
            datePanel.add(filterButton);

            topControls.add(datePanel, BorderLayout.WEST);

            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            actionsPanel.setOpaque(false);

            btnBookLater = new JButton("Đặt phòng trước (0)");
            btnBookLater.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnBookLater.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
            btnBookLater.setForeground(Color.WHITE);
            btnBookLater.setBorderPainted(false);
            btnBookLater.setFocusPainted(false);
            btnBookLater.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBookLater.setBorder(new EmptyBorder(8, 15, 8, 15));
            btnBookLater.setEnabled(false);
            btnBookAndCheckin = new JButton("Đặt & Check-in ngay (0)");
            btnBookAndCheckin.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnBookAndCheckin.setBackground(GUI_NhanVienLeTan.COLOR_GREEN);
            btnBookAndCheckin.setForeground(Color.WHITE);
            btnBookAndCheckin.setBorderPainted(false);
            btnBookAndCheckin.setFocusPainted(false);
            btnBookAndCheckin.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBookAndCheckin.setBorder(new EmptyBorder(8, 15, 8, 15));
            btnBookAndCheckin.setEnabled(false);
            actionsPanel.add(btnBookLater);
            actionsPanel.add(btnBookAndCheckin);
            topControls.add(actionsPanel, BorderLayout.EAST);

            headerPanel.add(topControls, BorderLayout.SOUTH);
            panel.add(headerPanel, BorderLayout.NORTH);
            JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
            centerPanel.setOpaque(false);
            centerPanel.add(createRoomFilterPanel(), BorderLayout.NORTH);

            // Bọc hàm có thể ném lỗi
            try {
                centerPanel.add(createRoomGridScrollPane(), BorderLayout.CENTER);
            } catch (Exception e) {
                e.printStackTrace();
                centerPanel.add(new JLabel("Lỗi tải danh sách phòng."), BorderLayout.CENTER);
            }
            panel.add(centerPanel, BorderLayout.CENTER);
            return panel;
        }

        /**
         * Tạo panel lọc phòng (với ButtonGroup)
         */
        private JPanel createRoomFilterPanel() {
            filterButtonsPanel = new JPanel();
            filterButtonsPanel.setLayout(new BoxLayout(filterButtonsPanel, BoxLayout.Y_AXIS));
            filterButtonsPanel.setOpaque(false);
            
            // Panel for room type filters
            JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            typePanel.setOpaque(false);
            typeGroup = new ButtonGroup();
            JToggleButton btnAllTypes = createFilterToggleButton("Tất cả", typeGroup, true, controller);
            JToggleButton btnStandard = createFilterToggleButton("Tiêu chuẩn", typeGroup, false, controller);
            JToggleButton btnDeluxe = createFilterToggleButton("Deluxe", typeGroup, false, controller);
            JToggleButton btnView = createFilterToggleButton("View biển", typeGroup, false, controller);
            JToggleButton btnFamily = createFilterToggleButton("Gia đình", typeGroup, false, controller);
            JToggleButton btnPresident = createFilterToggleButton("Tổng thống", typeGroup, false, controller);
            styleActiveTypeButton(btnAllTypes);
            
            // Panel for capacity filters
            JPanel peoplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            peoplePanel.setOpaque(false);
            peopleGroup = new ButtonGroup();
            JToggleButton btnAllPeople = createFilterToggleButton("Tất cả ", peopleGroup, true, controller);
            JToggleButton btn1People = createFilterToggleButton("1 người", peopleGroup, false, controller);
            JToggleButton btn2People = createFilterToggleButton("2 người", peopleGroup, false, controller);
            JToggleButton btn3People = createFilterToggleButton("3 người", peopleGroup, false, controller);
            JToggleButton btn4People = createFilterToggleButton("4+ người", peopleGroup, false, controller);
            styleActivePeopleButton(btnAllPeople);
            
            // Panel for floor filters
            JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            floorPanel.setOpaque(false);
            floorGroup = new ButtonGroup();
            JToggleButton btnAllFloors = createFilterToggleButton("Tất cả tầng", floorGroup, true, controller);
            JToggleButton btnFloor1 = createFilterToggleButton("Tầng 1", floorGroup, false, controller);
            JToggleButton btnFloor2 = createFilterToggleButton("Tầng 2", floorGroup, false, controller);
            JToggleButton btnFloor3 = createFilterToggleButton("Tầng 3", floorGroup, false, controller);
            JToggleButton btnFloor4 = createFilterToggleButton("Tầng 4", floorGroup, false, controller);
            styleActiveFloorButton(btnAllFloors);

            // Panel for status filters
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            statusPanel.setOpaque(false);
            statusGroup = new ButtonGroup();
            JToggleButton btnAllStatus = createFilterToggleButton("Tất cả trạng thái", statusGroup, true, controller);
            JToggleButton btnAvailable = createFilterToggleButton("Sẵn sàng", statusGroup, false, controller);
            JToggleButton btnOccupied = createFilterToggleButton("Đã thuê", statusGroup, false, controller);
            JToggleButton btnReserved = createFilterToggleButton("Đã đặt", statusGroup, false, controller);
            JToggleButton btnMaintenance = createFilterToggleButton("Bảo trì", statusGroup, false, controller);
            JToggleButton btnCleaning = createFilterToggleButton("Đang dọn", statusGroup, false, controller);
            styleActiveStatusButton(btnAllStatus);

            // Add buttons to each panel
            typePanel.add(btnAllTypes);
            typePanel.add(btnStandard);
            typePanel.add(btnDeluxe);
            typePanel.add(btnView);
            typePanel.add(btnFamily);
            typePanel.add(btnPresident);

            peoplePanel.add(btnAllPeople);
            peoplePanel.add(btn1People);
            peoplePanel.add(btn2People);
            peoplePanel.add(btn3People);
            peoplePanel.add(btn4People);

            floorPanel.add(btnAllFloors);
            floorPanel.add(btnFloor1);
            floorPanel.add(btnFloor2);
            floorPanel.add(btnFloor3);
            floorPanel.add(btnFloor4);

            statusPanel.add(btnAllStatus);
            statusPanel.add(btnAvailable);
            statusPanel.add(btnOccupied);
            statusPanel.add(btnReserved);
            statusPanel.add(btnMaintenance);
            statusPanel.add(btnCleaning);

            // Add panels to main filter panel
            filterButtonsPanel.add(typePanel);
            filterButtonsPanel.add(Box.createVerticalStrut(5));
            filterButtonsPanel.add(peoplePanel);
            filterButtonsPanel.add(Box.createVerticalStrut(5));
            filterButtonsPanel.add(floorPanel);
            filterButtonsPanel.add(Box.createVerticalStrut(5));
            filterButtonsPanel.add(statusPanel);

            return filterButtonsPanel;

            
        }

        /**
         * Tạo JScrollPane chứa lưới phòng
         */
        private JScrollPane createRoomGridScrollPane() { // Bỏ 'throws SQLException'
            roomGridPanel = new JPanel(new GridLayout(0, 4, 15, 15));
            roomGridPanel.setOpaque(false);
            roomGridPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            // Hàm getAllRoomData() đã được bọc try-catch
            populateRoomCards(getAllRoomData()); // Load dữ liệu ban đầu
            JScrollPane scrollPane = new JScrollPane(roomGridPanel);
            scrollPane.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            scrollPane.setPreferredSize(new Dimension(0, 600)); // Tăng chiều cao lên 600px
            scrollPane.setBorder(new EmptyBorder(10, 0, 0, 0));
            return scrollPane;
        }

        /**
         * Vẽ lại lưới phòng
         * (SỬA: Truyền moTa và soChua vào createRoomCard)
         * @param data Danh sách phòng (List<Phong>) từ DAO
         */
        public void populateRoomCards(List<Phong> data) {
            roomGridPanel.removeAll();
            if (data != null && controller != null) {
                for (Phong phong : data) {
                    try {
                        String roomNum = phong.getMaPhong();
                        String roomType = (phong.getLoaiPhong() != null) ? phong.getLoaiPhong().getTenLoaiPhong() : "N/A";
                        // highlight-start
                        // Lấy moTa và soChua từ entity Phong
                        String moTa = (phong.getMoTa() != null) ? phong.getMoTa() : "";
                        int soNguoi = phong.getSoChua(); // Lấy trực tiếp
                        // highlight-end
                        String price = String.format("%,.0f đ", phong.getGiaTienMotDem());

                        String tenTrangThai = "Không xác định";
                        boolean isAvailable = false;
                        if (phong.getTrangThaiPhong() != null) {
                            tenTrangThai = phong.getTrangThaiPhong().getTenTrangThai();
                            isAvailable = tenTrangThai.equalsIgnoreCase("Sẵn sàng");
                        }
                        String roomId = phong.getMaPhong();

                        // highlight-start
                        // Gọi hàm createRoomCard với tham số mới
                        JPanel card = createRoomCard(roomNum, roomType, moTa, soNguoi, price, isAvailable, roomId, tenTrangThai, controller);
                        // highlight-end
                        roomGridPanel.add(card);
                    } catch (Exception e) {
                        System.err.println("Lỗi populateRoomCards (List<Phong>): " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else if (controller == null) {
                System.err.println("Lỗi: Controller chưa được khởi tạo trong PanelDatPhongContent!");
            }
            roomGridPanel.revalidate();
            roomGridPanel.repaint();
        }

        /**
         * Tạo thẻ phòng (gắn listener gọi controller)
         * (ĐÃ SỬA: Hiển thị Mô tả + Số người thay vì Tầng/Diện tích)
         *
         * @param roomNum     Mã phòng (ví dụ: "P101")
         * @param roomType    Loại phòng (ví dụ: "Tiêu chuẩn")
         * @param moTa        Mô tả chi tiết phòng
         * @param soNguoi     Số người ở tối đa
         * @param price       Giá phòng đã định dạng (ví dụ: "500.000 đ")
         * @param isAvailable Phòng có sẵn sàng không (true/false)
         * @param roomId      Mã phòng (dùng cho sự kiện)
         * @param tenTrangThai Tên trạng thái (ví dụ: "Sẵn sàng")
         * @param ctrl        Controller xử lý sự kiện (hoặc this nếu tích hợp)
         * @return JPanel hiển thị thông tin phòng
         */
        // Sửa lại tham số đầu vào của hàm
        private JPanel createRoomCard(String roomNum, String roomType, String moTa, int soNguoi, String price, boolean isAvailable, String roomId, String tenTrangThai, EventDatPhong ctrl) {
            JPanel card = new JPanel(new BorderLayout(0, 8)); // Giảm khoảng cách dọc
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            // Giảm padding và border nếu cần
            card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER), new EmptyBorder(10, 10, 10, 10)));
            // --- Selected state ---
            boolean isSelected = selectedRoomIds.contains(roomId);

            // --- Phần trên: Mã phòng và Trạng thái --- (có badge "Đã chọn" khi selected)
            JPanel topPanel = new JPanel(new BorderLayout()); topPanel.setOpaque(false);
            JLabel numLabel = new JLabel(roomNum);
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            leftWrap.setOpaque(false);
            leftWrap.add(numLabel);

            JLabel selectedBadge = new JLabel("✓ Đã chọn");
            selectedBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            selectedBadge.setOpaque(true);
            selectedBadge.setBackground(new Color(138, 43, 226));
            selectedBadge.setForeground(Color.WHITE);
            selectedBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
            selectedBadge.setVisible(isSelected && isAvailable);
            leftWrap.add(selectedBadge);

            JLabel statusLabel = new JLabel(tenTrangThai);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            StatusColors colors = getStatusColors(tenTrangThai); // Giả sử hàm này tồn tại
            statusLabel.setForeground(colors.fg);
            statusLabel.setOpaque(true);
            statusLabel.setBackground(colors.bg);
            statusLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

            topPanel.add(leftWrap, BorderLayout.WEST);
            topPanel.add(statusLabel, BorderLayout.EAST);
            card.add(topPanel, BorderLayout.NORTH);

            // --- Phần giữa: Loại phòng, Mô tả, Số người ---
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);

            JLabel typeLabel = new JLabel(roomType);
            typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            centerPanel.add(typeLabel);
            centerPanel.add(Box.createVerticalStrut(3)); // Khoảng cách nhỏ

            // Hiển thị Mô tả + Số người
            String moTaHienThi = (moTa != null && !moTa.isEmpty()) ? moTa : "Không có mô tả";
            // highlight-start
            // Tạo chuỗi details mới sử dụng moTa và soNguoi từ tham số
            String detailsText = String.format("%s<br>• %d người", moTaHienThi, soNguoi);
            JLabel detailsLabel = new JLabel("<html>" + detailsText + "</html>");
            // highlight-end
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            detailsLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED); // Giả sử màu này tồn tại
            centerPanel.add(detailsLabel);

            card.add(centerPanel, BorderLayout.CENTER);

            // --- Phần dưới: Giá và Nút chọn --- (Giữ nguyên)
            JPanel bottomPanel = new JPanel(new BorderLayout()); bottomPanel.setOpaque(false);
            JLabel priceLabel = new JLabel("<html><b>" + price + "</b> /đêm</html>");
            priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JButton selectButton = new JButton();
            selectButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            selectButton.setFocusPainted(false);
            selectButton.setOpaque(true);
            selectButton.setContentAreaFilled(true);
            updateRoomButtonAppearance(selectButton, isSelected, isAvailable); // Hàm style nút
            selectButton.addActionListener(e -> ctrl.handleRoomSelectionToggle(roomId, (JButton) e.getSource()));
            bottomPanel.add(priceLabel, BorderLayout.CENTER);
            bottomPanel.add(selectButton, BorderLayout.EAST);
            card.add(bottomPanel, BorderLayout.SOUTH);

            // --- Sự kiện Click cho toàn bộ Card --- (Giữ nguyên)
            if (isAvailable) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        if (!(evt.getSource() instanceof JButton)) {
                            ctrl.handleRoomSelectionToggle(roomId, selectButton);
                        }
                    }
                });
            } else {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            // Thêm viền tím nếu được chọn (Giữ nguyên)
            if (isSelected && isAvailable) {
                card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(138, 43, 226), 2), new EmptyBorder(11, 11, 11, 11)));
            }

            return card;
        }

        /**
         * Cập nhật giao diện của nút chọn phòng (màu nền, chữ, viền)
         * (Sửa lại: Đảm bảo màu chữ trắng rõ ràng trên nền xanh/tím)
         */
        private void updateRoomButtonAppearance(JButton button, boolean isSelected, boolean isAvailable) {
            // --- Định nghĩa màu (Đảm bảo các màu này đậm) ---
            // Lấy từ hằng số hoặc định nghĩa trực tiếp
            Color ACCENT_BLUE_COLOR = GUI_NhanVienLeTan.ACCENT_BLUE; // new Color(24, 90, 219);
            // Color for selected (purple) is available as literal below when needed
            Color COLOR_DISABLED_BG_COLOR = new Color(220, 220, 220);
            Color COLOR_DISABLED_FG_COLOR = new Color(150, 150, 150);
            Color COLOR_DISABLED_BORDER_COLOR = new Color(180, 180, 180);
            Color COLOR_RED_COLOR = GUI_NhanVienLeTan.COLOR_RED; // Màu đỏ cho nút Bỏ chọn

            Border paddingBorder = new EmptyBorder(5, 15, 5, 15);
            Border lineBorder;

            // --- Logic đặt màu ---
            if (!isAvailable) { // Phòng không sẵn sàng
                button.setEnabled(false);
                button.setText("Chọn");
                button.setBackground(COLOR_DISABLED_BG_COLOR);
                button.setForeground(COLOR_DISABLED_FG_COLOR);
                lineBorder = new LineBorder(COLOR_DISABLED_BORDER_COLOR, 1);
            } else if (isSelected) { // Sẵn sàng và ĐÃ chọn -> Nút Bỏ chọn
                button.setEnabled(true);
                button.setText("Bỏ chọn");
                button.setBackground(COLOR_RED_COLOR); // Nền đỏ
                button.setForeground(Color.WHITE);
                lineBorder = new LineBorder(COLOR_RED_COLOR.darker(), 1);
            } else { // Sẵn sàng và CHƯA chọn -> Nút Chọn
                button.setEnabled(true);
                button.setText("Chọn");
                button.setBackground(ACCENT_BLUE_COLOR); // Nền xanh đậm
                button.setForeground(Color.WHITE);
                lineBorder = new LineBorder(ACCENT_BLUE_COLOR.darker(), 1);
            }

            // --- Áp dụng Style ---
            button.setBorder(new CompoundBorder(lineBorder, paddingBorder));
            // Đảm bảo màu nền được vẽ đúng cách
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            // Yêu cầu vẽ lại nút ngay lập tức
            button.repaint();
        }

        /** Lớp Helper cho màu Trạng thái */
        private static class StatusColors {
            Color bg;
            Color fg;
            StatusColors(Color bg, Color fg) { this.bg = bg; this.fg = fg; }
        }

        /** Logic lấy màu trạng thái */
        private StatusColors getStatusColors(String status) {
            if (status == null) status = ""; // Tránh NullPointerException
            switch (status) {
                case "Sẵn sàng":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_GREEN_BG, GUI_NhanVienLeTan.STATUS_GREEN_FG);
                case "Đã thuê":
                case "Đã đặt": // Thêm "Đã đặt"
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_RED_BG, GUI_NhanVienLeTan.STATUS_RED_FG);
                case "Bảo trì":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_ORANGE_BG, GUI_NhanVienLeTan.STATUS_ORANGE_FG);
                case "Đang dọn":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_YELLOW_BG, GUI_NhanVienLeTan.STATUS_YELLOW_FG);
                default:
                    return new StatusColors(new Color(240, 240, 240), GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            }
        }

        /**
         * Helper: Tạo JToggleButton lọc phòng
         */
        private JToggleButton createFilterToggleButton(String text, ButtonGroup group, boolean selected, EventDatPhong ctrl) {
            JToggleButton button = new JToggleButton(text, selected);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                // SỬA: Hàm filterRooms() của controller đã tự bắt lỗi
                ctrl.filterRooms();
            }); // Gắn listener
            if (group != null) {
                group.add(button);
            }
            return button;
        }

        /** Helper: Định dạng nút active nhóm Loại phòng */
        public void styleActiveTypeButton(JToggleButton button) {
            button.setForeground(GUI_NhanVienLeTan.COLOR_WHITE);
            button.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
        }

        /** Helper: Định dạng nút active nhóm Số người */
        public void styleActivePeopleButton(JToggleButton button) {
            button.setForeground(GUI_NhanVienLeTan.COLOR_WHITE);
            button.setBackground(GUI_NhanVienLeTan.COLOR_GREEN);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
        }

        /** Helper: Reset định dạng nút lọc */
        public void resetButtonStyle(JToggleButton button) {
            button.setForeground(Color.BLACK);
            button.setBackground(UIManager.getColor("Button.background"));
            button.setOpaque(false);
            button.setContentAreaFilled(true);
            button.setBorderPainted(true);
        }

        /** Helper: Định dạng nút active nhóm Tầng */
        public void styleActiveFloorButton(JToggleButton button) {
            button.setForeground(GUI_NhanVienLeTan.COLOR_WHITE);
            button.setBackground(new Color(100, 149, 237)); // Cornflower Blue
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
        }

        /** Helper: Định dạng nút active nhóm Trạng thái */
        public void styleActiveStatusButton(JToggleButton button) {
            button.setForeground(GUI_NhanVienLeTan.COLOR_WHITE);
            button.setBackground(new Color(147, 112, 219)); // Medium Purple
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
        }

        /** Cập nhật nút "Tiếp tục" */
        public void updateContinueButton() {
            int count = selectedRoomIds.size();
            if (count > 0) {
                btnContinueBooking.setText("Tiếp tục (" + count + " phòng)");
                btnContinueBooking.setEnabled(true);
            } else {
                btnContinueBooking.setText("Tiếp tục (0 phòng)");
                btnContinueBooking.setEnabled(false);
            }
            // Update new top-right action buttons if present
            if (btnBookLater != null) {
                btnBookLater.setText("Đặt phòng trước (" + count + ")");
                btnBookLater.setEnabled(count > 0);
            }
            if (btnBookAndCheckin != null) {
                btnBookAndCheckin.setText("Đặt & Check-in ngay (" + count + ")");
                btnBookAndCheckin.setEnabled(count > 0);
            }
        }

        // --- HÀM LẤY DỮ LIỆU TỪ DAO ---

        /**
         * Lấy dữ liệu đặt phòng TỪ CSDL.
         * SỬA: Bọc trong try-catch và trả về mảng rỗng nếu lỗi.
         */
        public Object[][] getAllBookingData() { // Bỏ 'throws SQLException'
            System.out.println("--- Lấy dữ liệu đặt phòng TỪ CSDL ---");
            try {
                // SỬA: Gọi hàm lọc với tham số mặc định
                // Điều này đảm bảo nó chỉ tải "Đã xác nhận" lúc đầu (nếu bạn muốn)
                // Hoặc gọi hàm lọc với "Tất cả"
                List<Object[]> dataList = phieuDatPhongDAO.getFilteredBookingData("", "Tất cả");

                if (dataList == null) {
                    return new Object[0][0]; // Trả về mảng rỗng nếu lỗi
                }
                return dataList.toArray(new Object[0][]);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải danh sách đặt phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return new Object[0][0]; // Trả về mảng rỗng
            }
        }

        /**
         * Lấy dữ liệu phòng TỪ CSDL.
         * SỬA: Bọc trong try-catch và trả về List rỗng nếu lỗi.
         */
        public List<Phong> getAllRoomData() { // Bỏ 'throws SQLException'
            System.out.println("--- Lấy dữ liệu phòng TỪ CSDL ---");
            try {
                // SỬA: Gọi hàm lọc với tham số mặc định "Tất cả"
                List<Phong> dataList = phongDAO.getFilteredPhong(null, -1, null, null, null, null);

                if (dataList == null) {
                    return new ArrayList<Phong>();
                }
                return dataList;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải danh sách phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return new ArrayList<Phong>(); // Trả về List rỗng
            }
        }
    }

// =================================================================================
// PANEL NỘI DUNG 3: KHÁCH HÀNG
// =================================================================================

    public class PanelKhachHangContent extends  JPanel {
        private List<entity.KhachHang> customers; // Danh sách gốc
        private List<entity.KhachHang> filteredCustomers; // Sau khi lọc / tìm kiếm
        private JPanel listPanel;
        private JTextField searchField;
        private JComboBox<String> cbTier;
        private KhachHang_DAO dao;
        private JPanel summaryPanel;
        private JLabel lblTongKH, lblTieuDung, lblDanhGia;
        private JLabel lblStandard, lblPlatinum, lblGold, lblSilver, lblBronze;
        private EventKhachHang controller;

        public PanelKhachHangContent() {
            dao = new KhachHang_DAO();

            // highlight-start
            // *** SỬA LỖI: BỌC LẠI BẰNG TRY-CATCH ***
            try {
                customers = dao.getAllKhachHang(); // <-- Dòng 1828
            } catch (SQLException e) {
                e.printStackTrace();
                // Hiển thị lỗi cho người dùng
                JOptionPane.showMessageDialog(this, "Lỗi nghiêm trọng: Không thể tải danh sách khách hàng.\n" + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                customers = new ArrayList<>(); // Khởi tạo danh sách rỗng để tránh lỗi NullPointer sau này
            }
            // *** KẾT THÚC SỬA LỖI ***

            filteredCustomers = new ArrayList<>(customers);
            controller = new EventKhachHang(this, dao); // khởi tạo controller
            setLayout(new BorderLayout());
            setBackground(GUI_NhanVienLeTan.MAIN_BG);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            add(createHeader(), BorderLayout.NORTH);
            add(createMainContent(), BorderLayout.CENTER);
            refreshCustomerList();
            refreshStats();
        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("Quản lý Khách hàng");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            header.add(title, BorderLayout.WEST);
            return header;
        }

        private JPanel createMainContent() {
            JPanel content = new JPanel(new BorderLayout(0, 20));
            content.setOpaque(false);

            content.add(createCustomerListPanel(), BorderLayout.CENTER);
            content.add(createSummarySection(), BorderLayout.SOUTH);

            return content;
        }

        // ===== DANH SÁCH KHÁCH HÀNG =====
        private JPanel createCustomerListPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 15));
            panel.setOpaque(false);

            panel.add(createSearchFilterPanel(), BorderLayout.NORTH);

            // listPanel được khởi tạo trong createCustomerScrollPanel
            JScrollPane scrollPane = createCustomerScrollPanel();
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        // tìm kiếm
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
            return searchPanel;
        }

        private void applyFilters() {
            String tempKeyword = searchField.getText().trim().toLowerCase();
            String placeholder = "Tìm kiếm theo mã nv, họ tên, số điện thoại, email, cccd...";
            final String keyword = tempKeyword.equals(placeholder.toLowerCase()) ? "" : tempKeyword;

            // ✔ FIX NPE: cbTier có thể đang null
            final String type = (cbTier != null)
                    ? cbTier.getSelectedItem().toString()
                    : "Tất cả";

            filteredCustomers = customers.stream()
                    .filter(e -> {
                        boolean matchType = "Tất cả".equals(type)
                                || Objects.equals(e.getHangThanhVien(), type);

                        boolean matchKeyword = keyword.isEmpty()
                                || (e.getMaKH() != null && e.getMaKH().toLowerCase().contains(keyword))
                                || (e.getTenKH() != null && e.getTenKH().toLowerCase().contains(keyword))
                                || (e.getSoDT() != null && e.getSoDT().contains(keyword))
                                || (e.getEmail() != null && e.getEmail().toLowerCase().contains(keyword))
                                || (e.getCCCD() != null && e.getCCCD().contains(keyword));

                        return matchType && matchKeyword;
                    })
                    .collect(Collectors.toList());

            refreshCustomerList();
        }

        private JScrollPane createCustomerScrollPanel() {
            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);

            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollPane.setPreferredSize(new Dimension(0, 350));
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);

            return scrollPane;
        }

        private void refreshCustomerList() {
            // Xóa tất cả card cũ
            listPanel.removeAll();

            // Thêm card cho từng khách hàng trong danh sách đã lọc
            for (KhachHang kh : filteredCustomers) {
                listPanel.add(createCustomerCard(kh));
                listPanel.add(Box.createVerticalStrut(8)); // khoảng cách giữa các card
            }

            // Refresh UI
            listPanel.revalidate();
            listPanel.repaint();
        }

        private JPanel createCustomerCard(KhachHang kh) {
            String name = (kh.getTenKH() != null) ? kh.getTenKH() : "N/A";
            String phone = (kh.getSoDT() != null) ? kh.getSoDT() : "N/A";
            String email = (kh.getEmail() != null) ? kh.getEmail() : "N/A";
            String cccd = (kh.getCCCD() != null) ? kh.getCCCD() : "N/A";
            int stayCount = kh.getSoLanLuuTru();
            String lastStay = (kh.getNgayLuuTruGanNhat() != null) ? kh.getNgayLuuTruGanNhat().toString() : "Chưa lưu trú";
            String totalSpend = formatCurrency(kh.getTongChiTieu());
            String tier = (kh.getHangThanhVien() != null) ? kh.getHangThanhVien() : "Standard"; // (Không cần nữa)
            double rating = kh.getDanhGiaTrungBinh();

            // Tạo card
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(10, 15, 10, 15)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            // --- Avatar ---
            JPanel avatarPanel = new JPanel(new GridBagLayout());
            avatarPanel.setOpaque(false);
            avatarPanel.setPreferredSize(new Dimension(60, 60));
            JLabel avatar = new JLabel(getInitials(name), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int diameter = Math.min(getWidth(), getHeight());
                    g2.setColor(new Color(108, 99, 255));
                    g2.fillOval(0, 0, diameter, diameter);
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int x = (diameter - fm.stringWidth(getText())) / 2;
                    int y = (diameter + fm.getAscent()) / 2 - 2;
                    g2.setColor(Color.WHITE);
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            avatar.setFont(new Font("SansSerif", Font.BOLD, 14));
            avatar.setPreferredSize(new Dimension(45, 45));
            avatarPanel.add(avatar);
            card.add(avatarPanel);

            // --- Info ---
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JLabel phoneLabel = new JLabel(phone);
            phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            phoneLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            infoPanel.add(nameLabel);
            infoPanel.add(phoneLabel);
            infoPanel.setPreferredSize(new Dimension(180, 40));
            card.add(infoPanel);

            // --- Các thông tin khác ---
            card.add(createVerticalInfoPanel(email, cccd, 160));
            card.add(createVerticalInfoPanel(stayCount + " lần lưu trú", "Lần cuối: " + lastStay, 130));
            JLabel ratingLabel = new JLabel(
                    "<html><span style='color:#FFD700;font-size:13px;'>★</span> " + String.format("%.1f/5", rating)
                            + "</html>");
            card.add(createVerticalInfoPanel(totalSpend, ratingLabel, 120));

            JLabel vipLabel = new JLabel(tier, SwingConstants.CENTER);
            vipLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            vipLabel.setForeground(Color.WHITE);
            vipLabel.setOpaque(true);
            vipLabel.setBackground(getTierColor(tier));   // liên quan màu theo hạng
            vipLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
            vipLabel.setPreferredSize(new Dimension(80, 25));

            JPanel vipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            vipPanel.setOpaque(false);
            vipPanel.add(vipLabel);

            card.add(vipPanel);

            // Thêm một khoảng trống thay thế để giữ layout
            card.add(Box.createHorizontalStrut(80));
            // highlight-end

            // --- Nút chức năng ---
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

            // === Gọi 2 nút xoá sửa (ĐÃ THÊM TRY-CATCH) ===
            edit.addActionListener(ae -> {
                try {
                    controller.handleEditKhachHang(kh);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi CSDL khi mở form sửa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            delete.addActionListener(ae -> {
                try {
                    controller.handleDeleteKhachHang(kh);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi CSDL khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            right.add(edit);
            right.add(delete);
            card.add(right);

            return card;
        }

        private String formatCurrency(double money) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(money) + " đ";
        }

        private JPanel createVerticalInfoPanel(Object top, Object bottom, int width) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(width, 40));

            if (top instanceof String) {
                p.add(new JLabel((String) top));
            } else if (top instanceof JLabel) {
                p.add((JLabel) top);
            }
            if (bottom instanceof String) {
                p.add(new JLabel((String) bottom));
            } else if (bottom instanceof JLabel) {
                p.add((JLabel) bottom);
            }
            return p;
        }

        // Logic lấy màu cho hạng thành viên
        // Lấy màu theo hạng VIP, an toàn với null
        private Color getTierColor(String tier) {
            if (tier == null) return Color.GRAY;
            switch (tier) {
                case "Platinum": return new Color(162, 126, 251);
                case "Gold": return new Color(237, 207, 79, 239);
                case "Silver": return new Color(177, 170, 170);
                case "Bronze": return new Color(255, 191, 110);
                case "Standard": return new Color(99, 132, 244);
                default: return Color.GRAY;
            }
        }

        // Lấy chữ cái đầu làm avatar
        private String getInitials(String name) {
            String[] parts = name.split(" ");
            String initials = "";
            for (String p : parts) {
                if (!p.isEmpty())
                    initials += p.charAt(0);
            }
            return initials.length() > 2 ? initials.substring(initials.length() - 2) : initials;
        }

        // ===== PHẦN THÊM KHÁCH HÀNG + TỔNG KẾT =====
        private JPanel createSummarySection() {
            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);

            // Tiêu đề + nút thêm
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("Thêm khách hàng mới");
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            header.add(title, BorderLayout.WEST);

            JButton btnAdd = new JButton("+ Thêm khách hàng");
            btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAdd.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setBorderPainted(false);
            btnAdd.setFocusPainted(false);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.setBorder(new EmptyBorder(8, 15, 8, 15));
            header.add(btnAdd, BorderLayout.EAST);

            // === Sự kiện: Thêm nhân viên ===
            btnAdd.addActionListener(e -> {
                try {
                    controller.handleAddKhachHang();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            container.add(header, BorderLayout.NORTH);

            // Thẻ thống kê có cuộn
            summaryPanel = createSummaryPanel();  // lưu tham chiếu
            JScrollPane scrollSummary = new JScrollPane(summaryPanel);
            scrollSummary.setBorder(null);
            scrollSummary.setPreferredSize(new Dimension(0, 200));
            scrollSummary.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollSummary.getVerticalScrollBar().setUnitIncrement(15);
            container.add(scrollSummary, BorderLayout.CENTER);

            return container;
        }


        private JPanel createSummaryPanel() {
            JPanel summary = new JPanel();
            summary.setOpaque(false);
            summary.setLayout(new GridLayout(2, 3, 15, 15)); // 2 dòng: tổng quan + hạng

            // ====== DÒNG 1: TỔNG QUAN ======
            lblTongKH = new JLabel("0", SwingConstants.CENTER);
            lblTieuDung = new JLabel("0 đ", SwingConstants.CENTER);
            lblDanhGia = new JLabel("0", SwingConstants.CENTER);

            summary.add(createSummaryCard(lblTongKH, "Khách hàng", GUI_NhanVienLeTan.ACCENT_BLUE));
            summary.add(createSummaryCard(lblTieuDung, "Tổng chi tiêu", new Color(60, 179, 113)));
            summary.add(createSummaryCard(lblDanhGia, "Đánh giá trung bình", new Color(255, 215, 0)));

            // ====== DÒNG 2: HẠNG THÀNH VIÊN ======
            lblStandard = new JLabel("0", SwingConstants.CENTER);
            lblPlatinum = new JLabel("0", SwingConstants.CENTER);
            lblGold = new JLabel("0", SwingConstants.CENTER);
            lblSilver = new JLabel("0", SwingConstants.CENTER);
            lblBronze = new JLabel("0", SwingConstants.CENTER);

            summary.add(createSummaryCard(lblStandard, "Standard", new Color(99, 132, 244)));
            summary.add(createSummaryCard(lblPlatinum, "Platinum", new Color(162, 126, 251)));
            summary.add(createSummaryCard(lblGold, "Gold", new Color(237, 207, 79)));
            summary.add(createSummaryCard(lblSilver, "Silver", new Color(177, 170, 170)));
            summary.add(createSummaryCard(lblBronze, "Bronze", new Color(255, 191, 110)));

            return summary;
        }

        private void refreshStats() {
            lblTongKH.setText(String.valueOf(dao.countAllKhachHang()));
            lblTieuDung.setText(NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(dao.sumTongChiTieu()) + " đ");
            lblDanhGia.setText(String.format("%.1f", dao.avgDanhGia()));
            lblStandard.setText(String.valueOf(dao.countKhachHangByTier("Standard")));
            lblPlatinum.setText(String.valueOf(dao.countKhachHangByTier("Platinum")));
            lblGold.setText(String.valueOf(dao.countKhachHangByTier("Gold")));
            lblSilver.setText(String.valueOf(dao.countKhachHangByTier("Silver")));
            lblBronze.setText(String.valueOf(dao.countKhachHangByTier("Bronze")));
        }

        private JPanel createSummaryCard(JLabel valueLabel, String label, Color color) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(color), new EmptyBorder(10, 10, 10, 10)));

            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            valueLabel.setForeground(color);

            JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
            labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            labelLabel.setForeground(Color.GRAY);

            card.add(valueLabel, BorderLayout.CENTER);
            card.add(labelLabel, BorderLayout.SOUTH);

            return card;
        }
        public void reloadData() throws SQLException {
            customers = dao.getAllKhachHang();
            filteredCustomers = new ArrayList<>(customers);
            applyFilters();
            refreshCustomerList();
            refreshStats();
        }
    }

    // =================================================================================
// PANEL NỘI DUNG 4: QUẢN LÝ PHÒNG (ĐÃ THÊM TÍNH NĂNG MỚI)
// =================================================================================
    public static class PanelPhongContent extends JPanel {
        private EventPhong controller;
        private JTextField searchField;
        private JComboBox<String> statusFilter;
        private JComboBox<String> typeFilter;
        private JPanel listPanel; // Panel chứa các thẻ phòng
        private JButton btnAdd;

        // highlight-start
        private EventDatPhong datPhongController; // <-- Thêm biến này

        // *** SỬA: Cập nhật constructor ***
        public PanelPhongContent() {
            this.datPhongController = datPhongController; // <-- Lưu lại
            // highlight-end
            setLayout(new BorderLayout());
            setBackground(GUI_NhanVienLeTan.MAIN_BG);
            setBorder(new EmptyBorder(18, 18, 18, 18));

            // highlight-start
            // *** SỬA: Truyền datPhongController vào EventPhong ***
            this.controller = new EventPhong(this, datPhongController);
            // highlight-end

            add(createHeader(), BorderLayout.NORTH);
            add(createMainContent(), BorderLayout.CENTER);

            controller.loadPhongData();
            controller.initListeners();
        }

        public JButton getBtnAdd() { return btnAdd; }
        public JTextField getSearchField() { return searchField; }
        public JComboBox<String> getStatusFilter() { return statusFilter; }
        public JComboBox<String> getTypeFilter() { return typeFilter; }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 0, 15, 0));
            JLabel title = new JLabel("Quản lý Phòng");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            header.add(title, BorderLayout.WEST);
            return header;
        }

        private JPanel createMainContent() {
            JPanel content = new JPanel(new BorderLayout(0, 20));
            content.setOpaque(false);
            content.add(createSearchFilterBar(), BorderLayout.NORTH);
            content.add(createRoomListPanel(), BorderLayout.CENTER);
            content.add(createRoomSchemaPanelWrapper(), BorderLayout.SOUTH);
            return content;
        }

        private JPanel createSearchFilterBar() {
            JPanel bar = new JPanel(new BorderLayout(10, 0));
            bar.setOpaque(false);
            searchField = new JTextField("");
            String placeholder = " Tìm kiếm phòng...";
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
            searchField.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(5, 8, 5, 8)));
            bar.add(searchField, BorderLayout.CENTER);
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            filterPanel.setOpaque(false);
            statusFilter = new JComboBox<>(new String[]{"Tất cả trạng thái"});
            typeFilter = new JComboBox<>(new String[]{"Tất cả loại"});
            filterPanel.add(statusFilter);
            filterPanel.add(typeFilter);
            bar.add(filterPanel, BorderLayout.EAST);
            bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            return bar;
        }

        private JScrollPane createRoomListPanel() {
            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);
            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            scrollPane.setPreferredSize(new Dimension(0, 450));
            return scrollPane;
        }

        // Sửa chữ ký phương thức
        private JPanel createDetailRoomCard(Phong p) {
            String num = p.getMaPhong();
            String type = p.getLoaiPhong().getTenLoaiPhong();
            String moTa = p.getMoTa() != null && !p.getMoTa().isEmpty() ? p.getMoTa() : "Không có mô tả";
            String specs = moTa + " • " + p.getSoChua() + " người";
            String price = String.format("%,.0f ₫", p.getGiaTienMotDem());
            String status = p.getTrangThaiPhong().getTenTrangThai();

            JPanel card = new JPanel(new GridLayout(1, 5, 10, 0));
            card.setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(12, 12, 12, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            card.setMinimumSize(new Dimension(0, 70));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Column 1: Room Number
            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            JLabel icon = new JLabel();
            icon.setPreferredSize(new Dimension(32, 32));
            icon.setOpaque(true);
            icon.setBackground(new Color(175, 170, 255));
            icon.setBorder(new EmptyBorder(0, 0, 0, 0));
            JPanel numPanel = new JPanel();
            numPanel.setLayout(new BoxLayout(numPanel, BoxLayout.Y_AXIS));
            numPanel.setOpaque(false);
            JLabel numLabel = new JLabel(num);
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            JLabel floorLabel = new JLabel(type);
            floorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            floorLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            numPanel.add(numLabel);
            numPanel.add(floorLabel);
            col1.add(icon);
            col1.add(numPanel);
            card.add(col1);

            // Column 2: Specs (Mô tả & Số người)
            JPanel col2 = new JPanel();
            col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
            col2.setOpaque(false);
            JLabel typeLabel = new JLabel(specs);
            typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JLabel specsLabel = new JLabel(" ");
            specsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            specsLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            col2.add(typeLabel);
            col2.add(specsLabel);
            card.add(col2);

            // Column 3: Price Info
            JPanel col3 = new JPanel();
            col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS));
            col3.setOpaque(false);
            JLabel priceLabel = new JLabel(price);
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JLabel perNight = new JLabel("/ đêm");
            perNight.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            perNight.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            col3.add(priceLabel);
            col3.add(perNight);
            card.add(col3);

            // Column 4: Status
            JPanel col4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            col4.setOpaque(false);
            JLabel statusLabel = new JLabel(status, SwingConstants.CENTER);
            StatusColors colors = getStatusColors(status);
            statusLabel.setOpaque(true);
            statusLabel.setBackground(colors.bg);
            statusLabel.setForeground(colors.fg);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
            statusLabel.setPreferredSize(new Dimension(80, 25));
            JPanel statusWrapper = new JPanel(new GridBagLayout());
            statusWrapper.setOpaque(false);
            statusWrapper.add(statusLabel);
            col4.setLayout(new BoxLayout(col4, BoxLayout.Y_AXIS));
            col4.add(statusWrapper);
            card.add(col4);

            // Column 5: Actions (GẮN SỰ KIỆN)
            JPanel col5 = new JPanel(new GridBagLayout());
            col5.setOpaque(false);
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            buttonsPanel.setOpaque(false);

            // highlight-start
            // === TÍNH NĂNG MỚI: THÊM NÚT SẴN SÀNG ===
            if (status.equals("Đang dọn") || status.equals("Bảo trì")) {
                JButton btnSetAvailable = new JButton("✓ Sẵn sàng");
                btnSetAvailable.setFont(new Font("Segoe UI", Font.BOLD, 10));
                btnSetAvailable.setBackground(GUI_NhanVienLeTan.COLOR_GREEN); // Màu xanh lá
                btnSetAvailable.setForeground(Color.WHITE);
                btnSetAvailable.setFocusPainted(false);
                btnSetAvailable.setBorder(new EmptyBorder(5, 10, 5, 10));
                btnSetAvailable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnSetAvailable.addActionListener(e -> controller.handleMarkRoomAsAvailable(p));
                buttonsPanel.add(btnSetAvailable);
            }
            // === KẾT THÚC TÍNH NĂNG MỚI ===
            // highlight-end

            JButton edit = new JButton("✎");
            JButton view = new JButton("👁");
            JButton delete = new JButton("🗑");
            edit.setForeground(Color.blue);
            view.setForeground(new Color(0, 180, 0));
            delete.setForeground(Color.red);
            for (JButton b : new JButton[]{edit, view, delete}) {
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            edit.addActionListener(e -> controller.handleShowEditForm(p));
            delete.addActionListener(e -> controller.handleDeletePhong(p));

            buttonsPanel.add(edit);
            buttonsPanel.add(view);
            buttonsPanel.add(delete);
            col5.add(buttonsPanel);
            card.add(col5);

            return card;
        }

        // Lớp Helper cho màu Trạng thái
        private static class StatusColors {
            Color bg; Color fg;
            StatusColors(Color bg, Color fg) { this.bg = bg; this.fg = fg; }
        }

        // Logic lấy màu trạng thái
        private StatusColors getStatusColors(String status) {
            switch (status) {
                case "Sẵn sàng":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_GREEN_BG, GUI_NhanVienLeTan.STATUS_GREEN_FG);
                case "Đã thuê":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_RED_BG, GUI_NhanVienLeTan.STATUS_RED_FG);
                case "Bảo trì":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_ORANGE_BG, GUI_NhanVienLeTan.STATUS_ORANGE_FG);
                case "Đang dọn":
                    return new StatusColors(GUI_NhanVienLeTan.STATUS_YELLOW_BG, GUI_NhanVienLeTan.STATUS_YELLOW_FG);
                default:
                    return new StatusColors(new Color(240, 240, 240), GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
            }
        }

        private JPanel createRoomSchemaPanelWrapper() {
            JPanel wrapper = new JPanel(new BorderLayout(0, 15));
            wrapper.setOpaque(false);
            wrapper.add(createRoomSchemaPanelHeader(), BorderLayout.NORTH);
            wrapper.add(createRoomSchemaPanel(), BorderLayout.CENTER);
            return wrapper;
        }

        private JPanel createRoomSchemaPanelHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(10, 0, 0, 0));
            JLabel title = new JLabel("Sơ đồ phòng");
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            header.add(title, BorderLayout.WEST);
            btnAdd = new JButton("+ Thêm phòng");
            btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAdd.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setBorderPainted(false);
            btnAdd.setFocusPainted(false);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.setBorder(new EmptyBorder(8, 15, 8, 15));
            header.add(btnAdd, BorderLayout.EAST);
            return header;
        }

        private JScrollPane createRoomSchemaPanel() {
            JPanel grid = new JPanel(new GridLayout(1, 0, 15, 15));
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(0, 0, 0, 0));
            grid.add(createSchemaCard("101", "Tiêu chuẩn", "2", "1.2M", GUI_NhanVienLeTan.STATUS_GREEN_BG.darker()));
            grid.add(createSchemaCard("201", "Suite cao cấp", "4", "2.8M", GUI_NhanVienLeTan.STATUS_RED_BG.darker()));
            grid.add(createSchemaCard("301", "View biển", "3", "3.5M", GUI_NhanVienLeTan.STATUS_ORANGE_BG.darker()));
            grid.add(createSchemaCard("404", "Gia đình", "6", "4.5M", GUI_NhanVienLeTan.STATUS_YELLOW_BG.darker()));
            grid.add(createSchemaCard("501", "Suite Tổng thống", "4", "9.0M", GUI_NhanVienLeTan.STATUS_GREEN_BG.darker()));
            grid.add(createSchemaCard("102", "Tiêu chuẩn", "2", "1.2M", GUI_NhanVienLeTan.STATUS_GREEN_BG.darker()));

            JScrollPane scroll = new JScrollPane(grid);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scroll.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
            scroll.setBorder(null);
            scroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
            return scroll;
        }

        private JPanel createSchemaCard(String num, String type, String capacity, String price, Color color) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(color);
            card.setBorder(new CompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER, 1),
                    new EmptyBorder(10, 10, 10, 10)));
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel numLabel = new JLabel(num);
            numLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            numLabel.setForeground(Color.BLACK);
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("SansSerif", Font.BOLD, 16));
            dot.setForeground(color.equals(GUI_NhanVienLeTan.STATUS_RED_BG.darker()) ? GUI_NhanVienLeTan.COLOR_RED
                    : GUI_NhanVienLeTan.COLOR_GREEN);
            header.add(numLabel, BorderLayout.WEST);
            header.add(dot, BorderLayout.EAST);
            card.add(header, BorderLayout.NORTH);
            JLabel typeLabel = new JLabel(type);
            typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            typeLabel.setForeground(Color.DARK_GRAY);
            card.add(typeLabel, BorderLayout.CENTER);
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            JLabel capLabel = new JLabel("<html><span style='font-size:10px;'>👤</span> " + capacity + "</html>");
            capLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel priceLabel = new JLabel(price);
            priceLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            priceLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED.darker());
            footer.add(capLabel, BorderLayout.WEST);
            footer.add(priceLabel, BorderLayout.EAST);
            card.add(footer, BorderLayout.SOUTH);
            return card;
        }

        public void populatePhongList(List<Phong> dsPhong) {
            listPanel.removeAll();
            if (dsPhong == null || dsPhong.isEmpty()) {
                listPanel.add(new JLabel("Không tìm thấy phòng nào."));
            } else {
                for (Phong p : dsPhong) {
                    JPanel card = createDetailRoomCard(p);
                    listPanel.add(card);
                    listPanel.add(Box.createVerticalStrut(10));
                }
            }
            listPanel.revalidate();
            listPanel.repaint();
        }
    }

// =================================================================================
// PANEL NỘI DUNG 5: DỊCH VỤ
// =================================================================================

    public static class PanelDichVuContent extends JPanel {
    private EventDichVu controller;

    private JPanel listPanel;
    private JButton btnAdd;
    private JTextField searchField;
    private JButton searchButton;
    public PanelDichVuContent() {
        setLayout(new BorderLayout(15, 15));
        setBackground(GUI_NhanVienLeTan.MAIN_BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        // KHỞI TẠO DAO VÀ CONTROLLER
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.controller = new EventDichVu(this);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        // GẮN LISTENER VÀ TẢI DỮ LIỆU
        controller.initListeners();
        controller.loadDichVuData(); // Tải dữ liệu ban đầu
    }
    public JButton getBtnAdd() { return btnAdd; }
    public JTextField getSearchField() { return searchField; }
    public JButton getSearchButton() { return searchButton; }
    // Tiêu đề + nút thêm dịch vụ
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        // Consistent bottom padding
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel title = new JLabel("Quản lý Dịch vụ");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        return header;
    }

    // Chia làm 2 phần: danh sách dịch vụ và danh mục dịch vụ
    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // THÊM THANH TÌM KIẾM
        content.add(createSearchFilterPanel());
        content.add(Box.createVerticalStrut(15)); // Khoảng cách

        // Danh sách dịch vụ ở trên
        content.add(createServiceListPanel());
        content.add(Box.createVerticalStrut(20)); // khoảng cách

        // Danh mục dịch vụ ở dưới
        content.add(createServiceCategoryPanel());

        return content;
    }

    // THÊM PHƯƠNG THỨC NÀY
    private JPanel createSearchFilterPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField("");
        String placeholder = " Tìm kiếm dịch vụ...";
        // ... (Copy code placeholder từ PanelKhachHangContent) ...
        searchField.setText(placeholder);
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.setBorder(new CompoundBorder(
                new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                new EmptyBorder(5, 8, 5, 8)));

        searchButton = new JButton("Tìm");
        searchButton.setFocusPainted(false);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return searchPanel;
    }

    // Danh sách dịch vụ chi tiết
    private JScrollPane createServiceListPanel() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GUI_NhanVienLeTan.MAIN_BG);
        scroll.setPreferredSize(new Dimension(0, 400));
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }
    /**
     * Vẽ lại danh sách thẻ dịch vụ từ List<DichVu>
     * @param dsDV Danh sách dịch vụ
     */
    public void populateDichVuList(List<DichVu> dsDV) {
        listPanel.removeAll(); // Xóa thẻ cũ

        if (dsDV == null || dsDV.isEmpty()) {
            listPanel.add(new JLabel("Không tìm thấy dịch vụ nào."));
        } else {
            for (DichVu dv : dsDV) {
                // Tạo thẻ mới từ đối tượng DichVu
                JPanel card = createServiceCard(dv); // Gọi hàm đã sửa
                listPanel.add(card);
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // Một thẻ dịch vụ chi tiết
    // Sửa chữ ký phương thức
    private JPanel createServiceCard(DichVu dv) {
        String name = dv.getTenDV();
        String price = String.format("%,.0f đ", dv.getGiaTien()); // Use getGiaTien

        // highlight-start
        // XÓA HOẶC COMMENT OUT CÁC DÒNG NÀY VÌ donViTinh KHÔNG CÒN TỒN TẠI
        // String donVi = dv.getDonViTinh(); // <<< LỖI Ở ĐÂY
        // if(donVi != null && !donVi.isEmpty()){
        //     price += " / " + donVi;
        // }
        // highlight-end

        // (Logic trạng thái "Còn" hay "Hết" cần thêm vào Entity/DAO nếu muốn)
        String status = "Còn";
        String desc = dv.getMoTa();
        double rating = 4.5; // (Tạm thời hard-code)

        // --- BẮT ĐẦU CODE GIAO DIỆN (Giữ nguyên) ---
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(
                new CompoundBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER), new EmptyBorder(10, 10, 10, 10)));

        JLabel title = new JLabel(name);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel priceLabel = new JLabel(price); // Dùng biến price
        priceLabel.setForeground(new Color(0, 128, 0));

        JLabel statusLabel = new JLabel(status); // Dùng biến status
        statusLabel.setForeground(status.equals("Còn") ? GUI_NhanVienLeTan.COLOR_GREEN : GUI_NhanVienLeTan.COLOR_RED);

        JLabel descLabel = new JLabel("<html><i>" + desc + "</i></html>"); // Dùng biến desc
        descLabel.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);

        JLabel ratingLabel = new JLabel("★ " + rating); // Dùng biến rating
        ratingLabel.setForeground(new Color(255, 165, 0));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(title);
        info.add(priceLabel);
        info.add(statusLabel);
        info.add(descLabel);
        info.add(ratingLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        // Sửa các nút này
        JButton edit = createIconButton("✎", Color.BLUE);
        JButton view = createIconButton("👁", new Color(0, 180, 0)); // (Chưa có sự kiện)
        JButton delete = createIconButton("🗑", Color.RED);

        // === GẮN SỰ KIỆN VÀO NÚT ===
        edit.addActionListener(e -> controller.handleShowEditForm(dv));
        delete.addActionListener(e -> controller.handleDeleteDichVu(dv));
        // view.addActionListener(e -> ...);

        actions.add(edit);
        actions.add(view);
        actions.add(delete);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // Danh mục dịch vụ theo nhóm
    private JPanel createServiceCategoryPanel() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15)); // 2 hàng, 3 cột
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thẻ danh mục dịch vụ mô phỏng
        grid.add(createCategoryCard("Spa & Massage", "300000 đ", new Color(255, 230, 230)));
        grid.add(createCategoryCard("Nhà hàng cao cấp", "500000 đ", new Color(230, 255, 230)));
        grid.add(createCategoryCard("Xe đưa đón sân bay", "300000 đ", new Color(230, 240, 255)));
        grid.add(createCategoryCard("Phòng gym & fitness", "300000 đ", new Color(255, 245, 230)));
        grid.add(createCategoryCard("Room service 24/7", "300000 đ", new Color(240, 240, 255)));

        // Tiêu đề + nút thêm
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel title = new JLabel("Danh mục dịch vụ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAdd = new JButton("+ Thêm dịch vụ");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setBorder(new EmptyBorder(8, 15, 8, 15));

        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(btnAdd); // Thêm nút vào wrapper
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(grid); // Thêm lưới vào wrapper

        return wrapper;
    }

    // Một thẻ danh mục dịch vụ
    private JPanel createCategoryCard(String name, String price, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(
                new CompoundBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER), new EmptyBorder(10, 10, 10, 10)));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel priceLabel = new JLabel(price);
        priceLabel.setForeground(Color.DARK_GRAY);

        card.add(nameLabel, BorderLayout.CENTER);
        card.add(priceLabel, BorderLayout.SOUTH);
        return card;
    }

    // Phương thức tạo nút Icon bằng Unicode/Emoji cho Dịch vụ
    private JButton createIconButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

// File: GUI_NhanVienLeTan.java
// (Tìm đến cuối file và THAY THẾ TOÀN BỘ lớp PanelCheckInCheckOut)

    // =================================================================================
// <<< PANEL NỘI DUNG 6: CHECK-IN / CHECK-OUT (ĐÃ SỬA LỖI) >>>
// =================================================================================
    public static class PanelCheckInCheckOut extends JPanel {

        // --- Định dạng ---
        private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0 đ");

        // --- UI Components ---
        private JToggleButton btnToggleCheckIn, btnToggleCheckOut;
        private JTextField txtSearch;
        private JButton btnSearch;
        private JDateChooser dateChooser;
        private JTable table;
        private DefaultTableModel tableModel;
        private TableColumn ngayColumn;
        private JLabel lblDaChon;
        private JButton btnMainAction;
        private JButton btnHistory;
        private JCheckBox chkSelectAll;

        private Frame ownerFrame;

        // --- Controller ---
        private EventCheckInCheckOut controller;

        // highlight-start
        // === SỬA LỖI 1: Sửa Constructor (Hàm khởi tạo) ===
        public PanelCheckInCheckOut(Frame owner, NhanVien nv, EventDatPhong datPhongController) {
            this.ownerFrame = owner;

            // --- 1. Khởi tạo Giao diện ---
            setLayout(new BorderLayout(0, 15));
            setBackground(MAIN_BG);
            setBorder(new EmptyBorder(18, 18, 18, 18));

            add(createHeaderPanel(), BorderLayout.NORTH);
            add(createControlsAndTablePanel(), BorderLayout.CENTER);

            // --- 2. Khởi tạo Controller ---
            this.controller = new EventCheckInCheckOut(this, nv, datPhongController);
            this.controller.initController();
            this.controller.loadData();
        }
        // highlight-end

        /**
         * TẠO HEADER (Gộp Title, Toggles, History)
         */
        private JPanel createHeaderPanel() {
            JPanel header = new JPanel(new BorderLayout(0, 15));
            header.setOpaque(false);
            header.add(createTitlePanel(), BorderLayout.NORTH);
            header.add(createToggleAndHistoryPanel(), BorderLayout.CENTER);
            return header;
        }

        /**
         * Helper: Tạo Panel Tiêu đề
         */
        private JPanel createTitlePanel() {
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            JLabel title = new JLabel("Check In / Check Out");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            JLabel subtitle = new JLabel("Quản lý check-in và check-out khách hàng");
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            subtitle.setForeground(COLOR_TEXT_MUTED);
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(subtitle, BorderLayout.SOUTH);
            return titlePanel;
        }

        /**
         * Helper: Tạo Panel Hàng 2 (Toggles và History)
         */
        private JPanel createToggleAndHistoryPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(createTogglePanel(), BorderLayout.WEST);
            panel.add(createHistoryButtonPanel(), BorderLayout.EAST);
            return panel;
        }

        /**
         * Helper: Tạo Panel 2 nút Toggle (Đã chỉnh sửa giao diện)
         */
        private JPanel createTogglePanel() {
            JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); // Khoảng cách giữa 2 nút là 15
            togglePanel.setOpaque(false);

            // Icon Unicode:
            // Check In: ➔] (Mũi tên vào)
            // Check Out: [➔ (Mũi tên ra)

            btnToggleCheckIn = new JToggleButton("➔]  Check In");
            btnToggleCheckOut = new JToggleButton("[➔  Check Out");

            // Nhóm nút để chỉ chọn được 1 trong 2
            ButtonGroup toggleGroup = new ButtonGroup();
            toggleGroup.add(btnToggleCheckIn);
            toggleGroup.add(btnToggleCheckOut);

            // Style mặc định ban đầu
            styleToggleButton(btnToggleCheckIn, true);  // Mặc định chọn Check In
            styleToggleButton(btnToggleCheckOut, false);

            btnToggleCheckIn.setSelected(true);

            togglePanel.add(btnToggleCheckIn);
            togglePanel.add(btnToggleCheckOut);
            return togglePanel;
        }

        /**
         * Helper: Tạo Panel nút History
         */
        private JPanel createHistoryButtonPanel() {
            JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightButtonsPanel.setOpaque(false);
            btnHistory = new JButton("<html>&#x21BB; Lịch sử Check Out</html>"); // Icon Refresh
            btnHistory.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnHistory.setBackground(COLOR_WHITE);
            btnHistory.setForeground(Color.BLACK);
            btnHistory.setFocusPainted(false);
            btnHistory.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER),
                    new EmptyBorder(8, 15, 8, 15)
            ));
            btnHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rightButtonsPanel.add(btnHistory);
            return rightButtonsPanel;
        }


        // Trong class PanelCheckInCheckOut của GUI_NhanVienLeTan.java

        /**
         * Style cho Toggle Button (Đã sửa lỗi màu nền)
         */
        public void styleToggleButton(JToggleButton btn, boolean active) {
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // --- QUAN TRỌNG: Bắt buộc phải có 2 dòng này để màu nền hiển thị đúng ---
            btn.setOpaque(true);            // Cho phép tô màu nền
            btn.setContentAreaFilled(true); // Tô màu toàn bộ vùng nội dung

            if (active) {
                // Trạng thái ĐANG CHỌN: Nền xanh đậm, chữ trắng, không viền
                btn.setBackground(ACCENT_BLUE); // Sử dụng màu xanh chủ đạo (ACCENT_BLUE)
                btn.setForeground(Color.WHITE);
                // Dùng EmptyBorder để tạo padding và loại bỏ viền nét
                btn.setBorder(new EmptyBorder(8, 20, 8, 20));
            } else {
                // Trạng thái KHÔNG CHỌN: Nền trắng, chữ đen, viền xám
                btn.setBackground(COLOR_WHITE);
                btn.setForeground(Color.BLACK);
                // Viền xám mỏng + padding bên trong
                btn.setBorder(new CompoundBorder(
                        new LineBorder(CARD_BORDER),
                        new EmptyBorder(7, 19, 7, 19)
                ));
            }
        }

        /**
         * Tạo Panel chứa (Controls, Actions, Table)
         */
        private JPanel createControlsAndTablePanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 15));
            panel.setOpaque(false);

            btnMainAction = new JButton("Check In (0)");
            btnMainAction.setBackground(ACCENT_BLUE);
            btnMainAction.setForeground(Color.BLACK);
            btnMainAction.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnMainAction.setBorder(new EmptyBorder(10, 25, 10, 25));
            btnMainAction.setEnabled(false);

            panel.add(createControlsPanel(), BorderLayout.NORTH);

            JPanel tableWrapper = new JPanel(new BorderLayout(0, 0));
            tableWrapper.setOpaque(false);
            tableWrapper.add(createTableActionsPanel(), BorderLayout.NORTH);
            tableWrapper.add(createTablePanel(), BorderLayout.CENTER);

            panel.add(tableWrapper, BorderLayout.CENTER);
            return panel;
        }

        /**
         * Tạo Panel Controls: [Search Group] + [Date & Action]
         * Search Group = [Thanh tìm kiếm có viền] + [Nút Tìm nằm ngoài]
         */
        private JPanel createControlsPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 0)); // Tăng khoảng cách giữa 2 cụm lớn
            panel.setOpaque(false);

            // =================================================================
            // 1. CỤM TÌM KIẾM (BÊN TRÁI)
            // =================================================================

            // a. Tạo khung chứa Icon + Text Field (Cái này sẽ có viền)
            JPanel txtWrapper = new JPanel(new BorderLayout(8, 0));
            txtWrapper.setOpaque(true);
            txtWrapper.setBackground(COLOR_WHITE);
            txtWrapper.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER),
                    new EmptyBorder(5, 10, 5, 10))
            );

            JLabel searchIcon = new JLabel("<html>&#x1F50D;</html>");
            searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
            searchIcon.setForeground(COLOR_TEXT_MUTED);

            txtSearch = new JTextField(" Tìm theo tên, phòng, SĐT, email...");
            txtSearch.setForeground(Color.GRAY);
            txtSearch.setBorder(null);
            txtSearch.setOpaque(false);
            txtSearch.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (txtSearch.getText().equals(" Tìm theo tên, phòng, SĐT, email...")) {
                        txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (txtSearch.getText().isEmpty()) {
                        txtSearch.setText(" Tìm theo tên, phòng, SĐT, email...");
                        txtSearch.setForeground(Color.GRAY);
                    }
                }
            });

            txtWrapper.add(searchIcon, BorderLayout.WEST);
            txtWrapper.add(txtSearch, BorderLayout.CENTER);

            // b. Tạo Nút Tìm (Nằm độc lập)
            btnSearch = new JButton("Tìm");
            btnSearch.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnSearch.setBackground(ACCENT_BLUE);
            btnSearch.setForeground(Color.WHITE);
            // --- THÊM CÁC DÒNG NÀY ĐỂ HIỆN MÀU ---
            btnSearch.setOpaque(true);            // Bắt buộc hiển thị màu nền
            btnSearch.setContentAreaFilled(true); // Cho phép tô màu vùng nội dung
            btnSearch.setBorderPainted(false);    // Bỏ viền mặc định để nút trông phẳng và đẹp hơn
            btnSearch.setFocusPainted(false);     // Bỏ viền khi click
            btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Tạo padding cho nút để trông đầy đặn hơn
            btnSearch.setBorder(new EmptyBorder(8, 20, 8, 20));

            // c. Tạo Panel nhóm: Chứa [txtWrapper] và [btnSearch]
            JPanel searchGroup = new JPanel(new BorderLayout(5, 0)); // 5px khoảng cách giữa thanh và nút
            searchGroup.setOpaque(false);
            searchGroup.add(txtWrapper, BorderLayout.CENTER); // Thanh tìm kiếm giãn hết cỡ
            searchGroup.add(btnSearch, BorderLayout.EAST);    // Nút nằm bên phải

            // Thêm cụm tìm kiếm vào bên trái (CENTER của layout cha để nó đẩy cụm kia sang phải)
            panel.add(searchGroup, BorderLayout.CENTER);


            // =================================================================
            // 2. CỤM NGÀY & NÚT HÀNH ĐỘNG (BÊN PHẢI)
            // =================================================================
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setOpaque(false);

            dateChooser = new JDateChooser();
            dateChooser.setDate(null);
            dateChooser.setDateFormatString("dd/MM/yyyy");
            dateChooser.setPreferredSize(new Dimension(140, 42)); // Chỉnh chiều cao cho khớp nút tìm
            dateChooser.setFont(new Font("SansSerif", Font.PLAIN, 14));

            // Config lịch (Giữ nguyên code cũ)
            try {
                com.toedter.calendar.JCalendar calendar = dateChooser.getJCalendar();
                calendar.setTodayButtonVisible(true);
                calendar.setTodayButtonText("Hôm nay");
                calendar.setNullDateButtonVisible(true);
                calendar.setNullDateButtonText("Xóa");
                JPanel footer = (JPanel) calendar.getComponent(1);
                Component todayButton = footer.getComponent(0);
                Component nullButton = footer.getComponent(1);
                footer.removeAll();
                footer.setLayout(new BorderLayout());
                footer.add(nullButton, BorderLayout.WEST);
                footer.add(todayButton, BorderLayout.EAST);
            } catch (Exception e) { e.printStackTrace(); }

            JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
            editor.setBorder(new CompoundBorder( new LineBorder(CARD_BORDER), new EmptyBorder(10, 10, 10, 10) ));
            editor.setEditable(false);
            String placeholder = " nn/mm/yyyy";
            editor.setText(placeholder);
            editor.setForeground(Color.GRAY);
            dateChooser.addPropertyChangeListener("date", evt -> {
                if (evt.getNewValue() == null) {
                    editor.setText(placeholder);
                    editor.setForeground(Color.GRAY);
                } else {
                    editor.setForeground(Color.BLACK);
                }
            });

            rightPanel.add(dateChooser);
            rightPanel.add(btnMainAction); // Nút Check In/Check Out to bự

            panel.add(rightPanel, BorderLayout.EAST);

            return panel;
        }

        // Panel cho "Chọn tất cả"
        private JPanel createTableActionsPanel() {
            JPanel filterActionPanel = new JPanel(new BorderLayout(10, 0));
            filterActionPanel.setOpaque(false);
            filterActionPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
            JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filterLeft.setOpaque(false);
            chkSelectAll = new JCheckBox("Chọn tất cả (0)");
            chkSelectAll.setOpaque(false);
            lblDaChon = new JLabel("Đã chọn: 0");
            JLabel separator = new JLabel("|");
            separator.setForeground(COLOR_TEXT_MUTED);
            filterLeft.add(chkSelectAll);
            filterLeft.add(separator);
            filterLeft.add(lblDaChon);
            filterActionPanel.add(filterLeft, BorderLayout.WEST);
            return filterActionPanel;
        }

        private JScrollPane createTablePanel() {
            // Tên cột hiển thị (Check-in/Check-out dùng chung cấu trúc)
            String[] columnNames = {"", "MÃ ĐP", "KHÁCH HÀNG", "PHÒNG", "LOẠI PHÒNG", "THỜI GIAN", "KHÁCH", "LIÊN HỆ", "TẠM TÍNH", "NGÀY TRẢ", "MÃ KH"};

            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Boolean.class;
                    return String.class;
                }
                @Override public boolean isCellEditable(int row, int column) {
                    return column == 0; // Chỉ cho phép check box
                }
            };

            table = new JTable(tableModel);

            // --- GIAO DIỆN HIỆN ĐẠI ---
            table.setRowHeight(65); // Dòng cao thoáng
            table.setShowVerticalLines(false);
            table.setShowHorizontalLines(true);
            table.setGridColor(new Color(230, 230, 230)); // Kẻ ngang mờ
            table.setIntercellSpacing(new Dimension(0, 0));

            // Header Style
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setOpaque(false);
            table.getTableHeader().setBackground(new Color(248, 249, 250));
            table.getTableHeader().setForeground(new Color(100, 100, 100));
            table.getTableHeader().setPreferredSize(new Dimension(0, 45));
            table.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());

            // --- CẤU HÌNH CỘT ---
            TableColumnModel tcm = table.getColumnModel();

            // Cột 0: Checkbox
            tcm.getColumn(0).setMaxWidth(40);
            tcm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFoc, int row, int col) {
                    JCheckBox cb = new JCheckBox();
                    cb.setHorizontalAlignment(JLabel.CENTER);
                    cb.setSelected(value != null && (boolean) value);
                    cb.setBackground(isSel ? new Color(235, 243, 255) : Color.WHITE);
                    cb.setOpaque(true);
                    return cb;
                }
            });

            // Cột 1: Mã ĐP (Xanh dương)
            tcm.getColumn(1).setPreferredWidth(90);
            tcm.getColumn(1).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_ID));

            // Cột 2: Khách hàng (Icon người)
            // Cột 2: Khách hàng (Icon người)
            tcm.getColumn(2).setPreferredWidth(220); // Độ rộng mong muốn
            tcm.getColumn(2).setMinWidth(220);       // BẮT BUỘC: Không được nhỏ hơn 220px
            tcm.getColumn(2).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_ICON_TEXT, "👤"));

            // Cột 3: Phòng (Icon giường)
            tcm.getColumn(3).setPreferredWidth(80);
            tcm.getColumn(3).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_ICON_TEXT, "🛏"));

            // Cột 4: Loại phòng (Text thường)
            tcm.getColumn(4).setPreferredWidth(120);
            tcm.getColumn(4).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_TEXT));

            // Cột 5: Thời gian (Icon lịch)
            ngayColumn = tcm.getColumn(5); // Lưu tham chiếu để đổi tên header
            tcm.getColumn(5).setPreferredWidth(110);
            tcm.getColumn(5).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_ICON_TEXT, "📅"));

            // Cột 6: Số khách (Icon nhóm)
            tcm.getColumn(6).setPreferredWidth(70);
            tcm.getColumn(6).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_ICON_TEXT, "👥"));

            // Cột 7: Liên hệ (2 dòng: SĐT + Email)
            tcm.getColumn(7).setPreferredWidth(200);
            tcm.getColumn(7).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_CONTACT));

            // Cột 8: Tiền (Đậm, căn phải)
            tcm.getColumn(8).setPreferredWidth(120);
            tcm.getColumn(8).setCellRenderer(new ModernCellRenderer(ModernCellRenderer.TYPE_MONEY));

            // Ẩn cột dữ liệu phụ (Ngày trả thực, Mã KH)
            tcm.getColumn(9).setMinWidth(0); tcm.getColumn(9).setMaxWidth(0);
            tcm.getColumn(10).setMinWidth(0); tcm.getColumn(10).setMaxWidth(0);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(new LineBorder(new Color(230, 230, 230)));
            return scrollPane;
        }

        // --- CÁC CLASS RENDERER GIAO DIỆN HIỆN ĐẠI ---

        // Renderer cho Header bảng (Đã căn chỉnh lại cột Tiền)
        private static class ModernHeaderRenderer extends DefaultTableCellRenderer {
            public ModernHeaderRenderer() {
                setOpaque(true);
                setBackground(new Color(248, 249, 250)); // Nền xám nhạt
                setForeground(new Color(100, 100, 100)); // Chữ xám đậm
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                String text = (value != null) ? value.toString().toUpperCase() : "";
                setText(text);

                // Tạo viền kẻ dưới mờ chung cho tất cả
                Border bottomLine = new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220));

                // Xử lý căn lề và padding riêng cho từng cột
                if (col == 8) { // Cột TẠM TÍNH
                    setHorizontalAlignment(JLabel.RIGHT);
                    // --- QUAN TRỌNG: Padding phải 30px để khớp với dữ liệu bên dưới ---
                    setBorder(new CompoundBorder(bottomLine, new EmptyBorder(0, 10, 0, 30)));
                }
                else if (col == 0 || col == 6) { // Cột Checkbox & Số khách
                    setHorizontalAlignment(JLabel.CENTER);
                    setBorder(new CompoundBorder(bottomLine, new EmptyBorder(0, 10, 0, 10)));
                }
                else { // Các cột còn lại (Tên, Phòng...)
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(new CompoundBorder(bottomLine, new EmptyBorder(0, 10, 0, 10)));
                }

                return this;
            }
        }

        // 2. Renderer Cell Đa năng (Đã chỉnh sửa Padding và Căn lề)
        private static class ModernCellRenderer extends DefaultTableCellRenderer {
            public static final int TYPE_TEXT = 0;
            public static final int TYPE_ID = 1;
            public static final int TYPE_ICON_TEXT = 2;
            public static final int TYPE_CONTACT = 3;
            public static final int TYPE_MONEY = 4;

            private int type;
            private String iconSymbol;

            public ModernCellRenderer(int type) { this(type, ""); }
            public ModernCellRenderer(int type, String iconSymbol) {
                this.type = type;
                this.iconSymbol = iconSymbol;
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String text = (value != null) ? value.toString() : "";

                // 1. Xử lý màu nền
                if (!isSelected) {
                    setBackground(Color.WHITE);
                    setForeground(new Color(33, 37, 41));
                } else {
                    setBackground(new Color(235, 243, 255));
                    setForeground(Color.BLACK);
                }

                // 2. Căn giữa chiều dọc cho TẤT CẢ các cột (Giúp tên liền mạch, nằm giữa dòng)
                setVerticalAlignment(JLabel.CENTER);

                // 3. Reset Border mặc định (Padding trái 10px, Phải 10px)
                setBorder(new EmptyBorder(0, 10, 0, 10));

                // 4. Xử lý riêng từng loại
                switch (type) {
                    case TYPE_ID:
                        setText("<html><b style='color: rgb(13, 110, 253)'>" + text + "</b></html>");
                        setHorizontalAlignment(JLabel.LEFT);
                        break;

                    case TYPE_ICON_TEXT:
                        // SỬA ĐỔI: Dùng thẻ <nobr> bao quanh toàn bộ nội dung
                        // Thẻ này ép buộc nội dung luôn nằm trên 1 dòng
                        setText("<html><nobr>" +
                                "<span style='color: gray; font-size: 14px'>" + iconSymbol + "</span>" +
                                "&nbsp;&nbsp;" + text +
                                "</nobr></html>");
                        setHorizontalAlignment(JLabel.LEFT);
                        break;

                    case TYPE_CONTACT:
                        String[] parts = text.split("\n");
                        String p1 = parts.length > 0 ? parts[0] : text;
                        String p2 = parts.length > 1 ? parts[1] : "";
                        setText("<html><div style='margin-bottom: 2px'>📞 " + p1 + "</div><div style='color: gray; font-size:10px'>✉ " + p2 + "</div></html>");
                        setHorizontalAlignment(JLabel.LEFT);
                        break;

                    case TYPE_MONEY:
                        setText("<html><b>" + text + "</b></html>");
                        setHorizontalAlignment(JLabel.RIGHT);

                        // --- CHỈNH SỬA QUAN TRỌNG: PADDING PHẢI CHO TIỀN ---
                        // Tham số: (Trên, Trái, Dưới, Phải) -> Phải 30px để không sát lề
                        setBorder(new EmptyBorder(0, 10, 0, 30));
                        break;

                    default: // TYPE_TEXT
                        setText(text);
                        setHorizontalAlignment(JLabel.LEFT);
                        setForeground(Color.GRAY);
                        break;
                }
                return this;
            }
        }

        /**
         * Cập nhật tiêu đề của cột NGÀY (Cột 3)
         */
        public void setNgayColumnHeader(String text) {
            if (ngayColumn != null) {
                ngayColumn.setHeaderValue(text);
                if (table != null && table.getTableHeader() != null) {
                    table.getTableHeader().repaint();
                }
            }
        }

        // --- CÁC GETTER ---
        public JToggleButton getBtnToggleCheckIn() { return btnToggleCheckIn; }
        public JToggleButton getBtnToggleCheckOut() { return btnToggleCheckOut; }
        public JTextField getTxtSearch() { return txtSearch; }
        public JButton getBtnSearch() { return btnSearch; }
        public JDateChooser getDateChooser() { return dateChooser; }
        public JTable getTable() { return table; }
        public DefaultTableModel getTableModel() { return tableModel; }
        public JButton getBtnMainAction() { return btnMainAction; }
        public JButton getBtnHistory() { return btnHistory; }
        public JCheckBox getChkSelectAll() { return chkSelectAll; }
        public Frame getOwnerFrame() { return ownerFrame; }

        // --- CÁC SETTER ---
        public void setLblDaChonText(String text) { lblDaChon.setText(text); }
        public void setBtnMainActionState(String text, boolean enabled) {
            btnMainAction.setText(text);
            btnMainAction.setEnabled(enabled);
        }
        public void updateMainActionButtonColor(boolean isCheckInMode) {
            if (isCheckInMode) {
                btnMainAction.setBackground(ACCENT_BLUE);
                btnMainAction.setForeground(Color.BLACK);
            }
            else {
                btnMainAction.setBackground(COLOR_ORANGE);
                btnMainAction.setForeground(Color.BLACK);
            }
        }

        // --- CÁC LỚP RENDERER ---

        // highlight-start
        // *** SỬA LỖI 2: Thêm 'public' ***
        public static class CustomerInfo {
            public String name, phone;
            public CustomerInfo(String name, String phone) { this.name = name; this.phone = phone; }
        }
        public static class ContactInfo {
            public String phone, email;
            public ContactInfo(String phone, String email) { this.phone = phone; this.email = email; }
        }
        // highlight-end

        private static class CustomerCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                // highlight-start
                // *** SỬA LỖI 3: 'panel' might not have been initialized ***
                if (value instanceof CustomerInfo) {
                    CustomerInfo info = (CustomerInfo) value;
                    JPanel panel = new JPanel(); // <-- Khai báo và khởi tạo bên trong
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    JLabel nameLabel = new JLabel(info.name);
                    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
                    panel.add(nameLabel);
                    panel.setBorder(new EmptyBorder(10, 5, 10, 5));
                    return panel; // <-- Trả về bên trong 'if'
                }
                // Trả về mặc định nếu không phải CustomerInfo
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                // highlight-end
            }
        }

        private static class HtmlIconRenderer extends DefaultTableCellRenderer {
            private String iconHtml;

            // Thêm tham số alignment vào constructor
            public HtmlIconRenderer(String htmlEntity, int alignment) {
                super();
                this.iconHtml = "<html><span style='font-family: SansSerif; font-size: 11pt; color: #6c757d;'>" + htmlEntity + "</span>&nbsp;&nbsp;";
                setHorizontalAlignment(alignment); // Thiết lập căn lề
            }
            // Giữ lại constructor cũ nếu cần (mặc định Left)
            public HtmlIconRenderer(String htmlEntity) {
                this(htmlEntity, JLabel.LEFT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                setBorder(new MatteBorder(0, 0, 1, 0, CARD_BORDER));
                return c;
            }
        }

        private static class ContactCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                // highlight-start
                // *** SỬA LỖI 3: 'panel' might not have been initialized ***
                if (value instanceof ContactInfo) {
                    ContactInfo info = (ContactInfo) value;
                    JPanel panel = new JPanel(); // <-- Khai báo và khởi tạo bên trong
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

                    JLabel phoneLabel = new JLabel("<html>&#x1F4DE; " + info.phone + "</html>");
                    JLabel emailLabel = new JLabel("<html>&#x1F4E7; " + info.email + "</html>");
                    phoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

                    panel.add(phoneLabel);
                    panel.add(emailLabel);
                    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                    return panel; // <-- Trả về bên trong 'if'
                }
                // Trả về mặc định nếu không phải ContactInfo
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                // highlight-end
            }
        }

        private static class MoneyCellRenderer extends DefaultTableCellRenderer {
            public MoneyCellRenderer() { super(); setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) { setText(MONEY_FORMAT.format(value)); }
                else { super.setValue(value); }
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, CARD_BORDER), new EmptyBorder(0, 0, 0, 10)));
            }
        }

        private static class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {
            public BooleanCellRenderer() {
                super(); setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true); setBorder(new MatteBorder(0, 0, 1, 0, CARD_BORDER));
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                setSelected((Boolean) value);
                return this;
            }
        }

        private static class HeaderRenderer extends DefaultTableCellRenderer {
            public HeaderRenderer() {
                super(); setHorizontalAlignment(SwingConstants.LEFT);
                setOpaque(true); setBackground(new Color(248, 249, 250));
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setForeground(COLOR_TEXT_MUTED);
                setBorder(new CompoundBorder( new MatteBorder(0, 0, 2, 0, CARD_BORDER), new EmptyBorder(0, 5, 0, 5)));
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                String headerText = (value == null) ? "" : value.toString().toUpperCase();
                Component c = super.getTableCellRendererComponent(table, headerText, isSelected, hasFocus, row, col);

                if (col == 0) { setHorizontalAlignment(SwingConstants.CENTER); }
                else { setHorizontalAlignment(SwingConstants.LEFT); }
                if (col == 4 || col == 6) { setHorizontalAlignment(SwingConstants.CENTER); }
                if (col == 8) { setHorizontalAlignment(SwingConstants.RIGHT); }

                return c;
            }
        }

        /**
         * LỚP JDIALOG (SỬA LẠI ĐỂ NHẬN 11 THAM SỐ)
         */
        public static class CheckInCustomerDialog extends JDialog {
            private boolean confirmed = false;

            // SỬA: Thêm cccd, diaChi
            public CheckInCustomerDialog(Frame owner, int currentIndex, int totalCount,
                                         String maPhieu, String phongInfo, String thoiGian,
                                         String soKhach, String customerName,
                                         String sdt, String email, String cccd, String diaChi) {

                super(owner, "Xác nhận thông tin khách hàng", true);
                setSize(600, 700);
                setLocationRelativeTo(owner);
                setLayout(new BorderLayout());
                JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
                mainPanel.setBackground(Color.WHITE);
                mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

                mainPanel.add(createDialogHeader(currentIndex, totalCount, maPhieu), BorderLayout.NORTH);
                mainPanel.add(createDialogInfoBox(maPhieu, phongInfo, thoiGian, soKhach), BorderLayout.CENTER);
                mainPanel.add(createDialogForm(customerName, sdt, email, cccd, diaChi), BorderLayout.SOUTH);

                add(mainPanel, BorderLayout.CENTER);
            }

            private JPanel createDialogHeader(int currentIndex, int totalCount, String maPhieu) {
                JPanel header = new JPanel(new BorderLayout(0, 10));
                header.setOpaque(false);
                JLabel title = new JLabel("Xác nhận thông tin khách hàng");
                title.setFont(new Font("SansSerif", Font.BOLD, 18));
                JLabel subtext = new JLabel(String.format("Khách %d / %d - Mã Đặt phòng: %s", currentIndex, totalCount, maPhieu));
                subtext.setFont(new Font("SansSerif", Font.PLAIN, 12));
                subtext.setForeground(COLOR_TEXT_MUTED);
                JProgressBar progressBar = new JProgressBar(0, totalCount);
                progressBar.setValue(currentIndex);
                progressBar.setPreferredSize(new Dimension(0, 6));
                header.add(title, BorderLayout.NORTH);
                header.add(subtext, BorderLayout.CENTER);
                header.add(progressBar, BorderLayout.SOUTH);
                return header;
            }

            private JPanel createDialogInfoBox(String maPhieu, String phongInfo, String thoiGian, String soKhach) {
                JPanel infoPanel = new JPanel(new BorderLayout(0, 15));
                infoPanel.setOpaque(false);
                Color infoBg = new Color(248, 249, 255);
                JPanel box = new JPanel(new GridLayout(2, 2, 20, 10));
                box.setOpaque(true);
                box.setBackground(infoBg);
                box.setBorder(new CompoundBorder(
                        new LineBorder(new Color(222, 226, 240)),
                        new EmptyBorder(15, 15, 15, 15)
                ));
                box.add(createDialogInfoPair("Mã đặt phòng:", maPhieu));
                box.add(createDialogInfoPair("Phòng:", phongInfo));
                box.add(createDialogInfoPair("Thời gian:", thoiGian));
                box.add(createDialogInfoPair("Số khách:", soKhach + " người"));
                infoPanel.add(box, BorderLayout.NORTH);
                return infoPanel;
            }

            // SỬA: Nhận cccd, diaChi
            private JPanel createDialogForm(String customerName, String sdt, String email, String cccd, String diaChi) {
                JPanel formWrapper = new JPanel(new BorderLayout(0, 15));
                formWrapper.setOpaque(false);

                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setOpaque(false);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 5, 8, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                int y = 0;
                gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 4;
                JLabel info = new JLabel("<html><b>ⓘ</b> Vui lòng kiểm tra và cập nhật thông tin khách hàng</html>");
                info.setForeground(ACCENT_BLUE);
                formPanel.add(info, gbc);
                gbc.gridwidth = 1;

                gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 4;
                formPanel.add(createDialogFormPair("Họ và tên *", customerName), gbc);

                gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 4;
                formPanel.add(createDialogFormPair("CMND / CCCD *", cccd), gbc); // <-- SỬA

                gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 0.5;
                formPanel.add(createDialogFormPair("Số điện thoại *", sdt), gbc);

                gbc.gridx = 2; gbc.gridy = y++; gbc.gridwidth = 2; gbc.weightx = 0.5;
                formPanel.add(createDialogFormPair("Email *", email), gbc);
                gbc.gridwidth = 1; gbc.weightx = 0;

                gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 4;
                formPanel.add(createDialogFormPair("Địa chỉ *", diaChi), gbc); // <-- SỬA

                formWrapper.add(formPanel, BorderLayout.CENTER);
                formWrapper.add(createDialogFooter(), BorderLayout.SOUTH);
                return formWrapper;
            }

            private JPanel createDialogFormPair(String label, String value) {
                JPanel panel = new JPanel(new BorderLayout(0, 3));
                panel.setOpaque(false);
                JLabel lbl = new JLabel(label);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                JTextField txt = new JTextField(value);
                txt.setBorder(new CompoundBorder(
                        new LineBorder(CARD_BORDER),
                        new EmptyBorder(8, 8, 8, 8)
                ));
                panel.add(lbl, BorderLayout.NORTH);
                panel.add(txt, BorderLayout.CENTER);
                return panel;
            }

            private JPanel createDialogInfoPair(String label, String value) {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setOpaque(false);
                JLabel lbl = new JLabel(label);
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
                lbl.setForeground(COLOR_TEXT_MUTED);
                JLabel val = new JLabel(value);
                val.setFont(new Font("SansSerif", Font.BOLD, 14));
                panel.add(lbl);
                panel.add(val);
                return panel;
            }

            private JPanel createDialogFooter() {
                JPanel footer = new JPanel(new BorderLayout(10, 0));
                footer.setOpaque(false);
                footer.setBorder(new CompoundBorder(
                        new MatteBorder(1, 0, 0, 0, CARD_BORDER),
                        new EmptyBorder(10, 0, 0, 0)
                ));
                JButton btnBack = new JButton("← Quay lại");
                btnBack.setFont(new Font("SansSerif", Font.PLAIN, 12));
                btnBack.setForeground(Color.BLACK);
                btnBack.setOpaque(false);
                btnBack.setContentAreaFilled(false);
                btnBack.setBorderPainted(false);
                btnBack.setEnabled(false);
                JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                rightButtons.setOpaque(false);
                JButton btnCancel = new JButton("Hủy");
                styleDialogButton(btnCancel, COLOR_WHITE, Color.BLACK);
                btnCancel.addActionListener(e -> dispose());
                JButton btnConfirm = new JButton("Tiếp theo →");
                styleDialogButton(btnConfirm, ACCENT_BLUE, Color.WHITE);
                btnConfirm.addActionListener(e -> {
                    confirmed = true;
                    dispose();
                });
                rightButtons.add(btnCancel);
                rightButtons.add(btnConfirm);
                footer.add(btnBack, BorderLayout.WEST);
                footer.add(rightButtons, BorderLayout.EAST);
                return footer;
            }

            private void styleDialogButton(JButton btn, Color bg, Color fg) {
                btn.setBackground(bg);
                btn.setForeground(fg);
                btn.setFont(new Font("SansSerif", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(bg == COLOR_WHITE ? CARD_BORDER : bg.darker()),
                        new EmptyBorder(8, 15, 8, 15)
                ));
            }

            public boolean isConfirmed() { return confirmed; }
        }

    } // <-- Dấu } kết thúc lớp PanelCheckInCheckOut
    //danh sach check in
    public static class GUI_CheckIn extends JFrame {
        public GUI_CheckIn() {
            setTitle("Màn hình Check-in");
            setSize(400, 300);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel label = new JLabel("Thực hiện Check-in khách hàng", SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 16));

            add(label, BorderLayout.CENTER);
        }
    }

    //danh sach check out
    public static class GUI_CheckOut extends JFrame {
        public GUI_CheckOut() {
            setTitle("Màn hình Check-out");
            setSize(400, 300);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel label = new JLabel("Thực hiện Check-out khách hàng", SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 16));

            add(label, BorderLayout.CENTER);
        }
    }
}


