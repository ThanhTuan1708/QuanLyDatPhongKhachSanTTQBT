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
// PANEL NỘI DUNG 2: DASHBOARD QUẢN LÝ
// =================================================================================

    public static class PanelQuanLyContent extends JPanel {
        // Note: Màu sắc STAT_BG này chỉ dùng nội bộ trong Dashboard Quản lý
        private final Color STAT_BG_1 = new Color(218, 240, 255);
        private final Color STAT_BG_2 = new Color(230, 235, 255);
        private final Color STAT_BG_3 = new Color(255, 235, 240);

        // profile labels that can be updated from outer class
        private JLabel profileNameLabel;
        private JLabel profileDetailsLabel;

        public PanelQuanLyContent() {
            // --- Thiết lập cho JPanel này ---
            setLayout(new BorderLayout());
            setBackground(MAIN_BG); // Sử dụng MAIN_BG đã import
            setBorder(new EmptyBorder(18, 18, 18, 18)); // Lề cho nội dung

            // --- Chỉ thêm Header và Content Panel ---
            add(createHeader(), BorderLayout.NORTH);
            add(createContentPanel(), BorderLayout.CENTER);
        }

        private JPanel createHeader() {
            // Note: Phần tiêu đề và ngày tháng
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("Dashboard Quản lý Khách sạn");
            title.setFont(new Font("SansSerif", Font.BOLD, 18));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy");
            JLabel date = new JLabel(fmt.format(LocalDate.now()));
            date.setForeground(Color.GRAY);

            header.add(title, BorderLayout.WEST);
            header.add(date, BorderLayout.EAST);
            return header;
        }

        private JPanel createContentPanel() {
            // Note: Cấu trúc chính của Dashboard (Top Zone, Schedule, Tasks, Stats)
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);

            // Zone 1 - Profile + quick stats (TOP)
            JPanel zone1 = createTopZone();
            zone1.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));
            content.add(zone1, BorderLayout.NORTH);

            // Middle container (Zone2 + Zone3 stacked)
            JPanel middle = new JPanel(new GridBagLayout());
            middle.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 0, 8, 0);
            gc.fill = GridBagConstraints.BOTH;
            gc.gridx = 0;
            gc.weightx = 1.0;

            // Zone 2: Schedule (chiều rộng ngang với zone1)
            gc.gridy = 0;
            gc.weighty = 0.38;
            JPanel scheduleCard = createCardWrapper(createSchedulePanel());
            middle.add(scheduleCard, gc);

            // Zone 3: Tasks + stats (Chia 2 cột)
            gc.gridy = 1;
            gc.weighty = 0.62;
            JPanel bottom = new JPanel(new GridBagLayout());
            bottom.setOpaque(false);
            GridBagConstraints gc2 = new GridBagConstraints();
            gc2.insets = new Insets(12, 12, 12, 12);
            gc2.fill = GridBagConstraints.BOTH;

            gc2.gridx = 0;
            gc2.gridy = 0;
            gc2.weightx = 0.66;
            gc2.weighty = 1.0;
            bottom.add(createCardWrapper(createTasksPanel()), gc2);

            gc2.gridx = 1;
            gc2.weightx = 0.34;
            bottom.add(createCardWrapper(createStatsPanel()), gc2);

            middle.add(bottom, gc);
            content.add(middle, BorderLayout.CENTER);

            return content;
        }

        // Allow outer classes to update the profile info shown in the top-left area
        public void setProfileName(String name, String details) {
            if (profileNameLabel != null) profileNameLabel.setText(name);
            if (profileDetailsLabel != null) profileDetailsLabel.setText(details);
        }

        // wrapper để có border/padding giống các card
        private JPanel createCardWrapper(JPanel inner) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER), new EmptyBorder(14, 14, 14, 14)));
            card.add(inner, BorderLayout.CENTER);
            return card;
        }

        private JPanel createTopZone() {
            // Note: Phần hiển thị Profile chi tiết và 3 ô quick stats
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER), new EmptyBorder(12, 12, 12, 12)));

            // left profile
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
            left.setOpaque(false);
            JLabel avatar = new JLabel("QL");
            avatar.setPreferredSize(new Dimension(64, 64));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(new Color(160, 110, 255));
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("SansSerif", Font.BOLD, 20));
            left.add(avatar);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            profileNameLabel = new JLabel("Trần Văn Quản Lý");
            profileNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            profileDetailsLabel = new JLabel("<html>quanly@tbqtthotel.vn • +84 (0) 987-654-321 • Ban Giám đốc</html>");
            profileDetailsLabel.setForeground(Color.GRAY);
            profileDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            info.add(profileNameLabel);
            info.add(Box.createVerticalStrut(6));
            info.add(profileDetailsLabel);
            left.add(info);

            // right quick stats
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            right.setOpaque(false);
            right.add(createStatBox("27h", "Giờ tuần này", STAT_BG_1));
            right.add(createStatBox("12", "Cuộc họp", STAT_BG_2));
            right.add(createStatBox("14", "Công việc", STAT_BG_3));

            card.add(left, BorderLayout.WEST);
            card.add(right, BorderLayout.EAST);

            return card;
        }

        private JPanel createSchedulePanel() {
            // Note: Bảng Lịch làm việc/Cuộc họp
            JPanel wrap = new JPanel(new BorderLayout(8, 8));
            wrap.setOpaque(false);
            JLabel title = new JLabel("Lịch làm việc & Cuộc họp tuần này");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            wrap.add(title, BorderLayout.NORTH);

            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setOpaque(false);

            String[][] data = {
                    {"Thứ Hai", "15/1/2024", "Hành chính", "8:00 - 17:00", "9 giờ", "7:55", "17:15", "Hoàn thành"},
                    {"Thứ Ba", "16/1/2024", "Hành chính", "8:00 - 17:00", "9 giờ", "8:00", "17:00", "Hoàn thành"},
                    {"Thứ Tư", "17/1/2024", "Hành chính", "8:00 - 17:00", "9 giờ", "7:58", "", "Đang làm"},
                    {"Thứ Năm", "18/1/2024", "Hành chính", "8:00 - 17:00", "9 giờ", "", "", ""},
                    {"Thứ Sáu", "19/1/2024", "Hành chính", "8:00 - 17:00", "9 giờ", "", "", ""}
            };

            DayOfWeek today = LocalDate.now().getDayOfWeek();

            for (String[] row : data) {
                JPanel r = new JPanel(new BorderLayout(8, 0));
                r.setOpaque(true);
                r.setBackground(Color.WHITE);
                r.setBorder(new CompoundBorder(new EmptyBorder(8, 8, 8, 8), new LineBorder(new Color(241, 241, 246))));
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

                JPanel left = new JPanel();
                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
                JLabel dn = new JLabel(row[0]);
                dn.setFont(new Font("SansSerif", Font.BOLD, 12));
                JLabel ddate = new JLabel(row[1]);
                ddate.setForeground(Color.GRAY);
                ddate.setFont(new Font("SansSerif", Font.PLAIN, 11));
                left.add(dn);
                left.add(ddate);
                r.add(left, BorderLayout.WEST);

                JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 12));
                mid.setOpaque(false);
                mid.add(new JLabel(row[2] + "  (" + row[3] + ")"));
                mid.add(new JLabel(row[4]));
                r.add(mid, BorderLayout.CENTER);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
                right.setOpaque(false);
                if (!row[5].isEmpty())
                    right.add(new JLabel("⏱ " + row[5]));
                if (!row[6].isEmpty())
                    right.add(new JLabel("⏲ " + row[6]));
                if (!row[7].isEmpty()) {
                    JLabel st = new JLabel(row[7]);
                    st.setOpaque(true);
                    st.setBorder(new EmptyBorder(4, 8, 4, 8));
                    if ("Hoàn thành".equals(row[7])) {
                        st.setBackground(new Color(220, 255, 230));
                        st.setForeground(new Color(20, 110, 40));
                    } else {
                        st.setBackground(new Color(230, 245, 255));
                        st.setForeground(new Color(10, 90, 180));
                    }
                    right.add(st);
                }
                r.add(right, BorderLayout.EAST);

                // highlight today
                DayOfWeek rowDay = getDayOfWeek(row[0]);
                if (rowDay == today) {
                    r.setBorder(new CompoundBorder(new EmptyBorder(8, 8, 8, 8),
                            new LineBorder(new Color(200, 160, 255), 2, true)));
                }

                list.add(r);
                list.add(Box.createVerticalStrut(6));
            }

            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(null);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            wrap.add(sp, BorderLayout.CENTER);

            return wrap;
        }

        private JPanel createTasksPanel() {
            // Note: Bảng Công việc quan trọng hôm nay (Task Table)
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel title = new JLabel("Công việc quan trọng hôm nay");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            p.add(title, BorderLayout.NORTH);

            DefaultTableModel model = new DefaultTableModel(
                    new Object[][]{
                            {"09:00", "Họp ban giám đốc", "Hoàn thành", "Phòng họp A"},
                            {"10:30", "Duyệt báo cáo doanh thu", "Hoàn thành", ""},
                            {"14:00", "Phỏng vấn ứng viên", "Chưa xong", ""},
                            {"15:30", "Kiểm tra chất lượng dịch vụ", "Chưa xong", ""}
                    },
                    new String[]{"Thời gian", "Công việc", "Trạng thái", "Ghi chú"}) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setRowHeight(40);
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
            table.getColumnModel().getColumn(1).setPreferredWidth(240);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setBorder(new EmptyBorder(6, 8, 6, 8));
                    String st = v == null ? "" : v.toString();
                    if ("Hoàn thành".equals(st)) {
                        lbl.setBackground(new Color(220, 255, 230));
                        lbl.setForeground(new Color(20, 110, 40));
                    } else if ("Chưa xong".equals(st)) {
                        lbl.setBackground(new Color(255, 245, 230));
                        lbl.setForeground(new Color(170, 110, 20));
                    } else {
                        lbl.setBackground(new Color(240, 240, 245));
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                    lbl.setOpaque(true);
                    return lbl;
                }
            });

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(null);
            p.add(sp, BorderLayout.CENTER);
            return p;
        }

        private JPanel createStatsPanel() {
            // Note: 6 ô thống kê nhanh (3x2 grid)
            JPanel p = new JPanel(new GridLayout(3, 2, 12, 12));
            p.setOpaque(false);
            p.add(createStatCard("27h", "Tuần này", STAT_BG_1));
            p.add(createStatCard("12", "Cuộc họp", STAT_BG_2));
            p.add(createStatCard("24", "Nhân viên", new Color(230, 255, 245)));
            p.add(createStatCard("450.000.000đ", "Doanh thu tuần", new Color(230, 255, 230)));
            p.add(createStatCardWithArrow("2h", "Tăng ca", new Color(255, 250, 230), true));
            p.add(createStatCard("★", "Đánh giá", new Color(255, 245, 245)));
            return p;
        }

        private JPanel createStatBox(String big, String small, Color bg) {
            // Note: ô thống kê trên Top Zone
            JPanel c = new JPanel(new BorderLayout());
            c.setBackground(bg);
            c.setBorder(new LineBorder(CARD_BORDER));
            JLabel bigLabel = new JLabel(big, SwingConstants.CENTER); // Đổi tên biến để rõ ràng hơn
            bigLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            c.add(bigLabel, BorderLayout.CENTER);
            JLabel lb = new JLabel(small, SwingConstants.CENTER);
            lb.setForeground(Color.DARK_GRAY);
            c.add(lb, BorderLayout.SOUTH);
            return c;
        }

        private JPanel createStatCard(String value, String label, Color bg) {
            // Note: ô thống kê trong Stats Panel
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(bg);
            card.setBorder(new LineBorder(CARD_BORDER));
            JLabel v = new JLabel(value, SwingConstants.CENTER);
            v.setFont(new Font("SansSerif", Font.BOLD, 18));
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setForeground(Color.DARK_GRAY);
            card.add(v, BorderLayout.CENTER);
            card.add(l, BorderLayout.SOUTH);
            return card;
        }

        private JPanel createStatCardWithArrow(String value, String label, Color bg, boolean up) {
            // Note: ô thống kê có mũi tên tăng/giảm
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(bg);
            card.setBorder(new LineBorder(CARD_BORDER));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            top.setOpaque(false);
            JLabel v = new JLabel(value);
            v.setFont(new Font("SansSerif", Font.BOLD, 16));
            JLabel arr = new JLabel(up ? "▲" : "▼");
            arr.setForeground(up ? new Color(0, 140, 0) : new Color(200, 50, 50));
            top.add(v);
            top.add(arr);
            card.add(top, BorderLayout.CENTER);
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setForeground(Color.DARK_GRAY);
            card.add(l, BorderLayout.SOUTH);
            return card;
        }

        private DayOfWeek getDayOfWeek(String vnName) {
            // Note: Chuyển tên tiếng Việt sang DayOfWeek
            switch (vnName) {
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
// PANEL NỘI DUNG 5: THỐNG KÊ & BÁO CÁO (GUI_ThongKeBaoCao cũ)
// =================================================================================

    public static class PanelThongKeContent extends JPanel {

        // --- Các hằng số màu sắc (Được sử dụng làm nguồn cho các lớp khác) ---
        public static final Color MAIN_BG = new Color(242, 245, 250);
        public static final Color CARD_BORDER = new Color(222, 226, 230);
        public static final Color ACCENT_BLUE = new Color(24, 90, 219);
        public static final Color COLOR_WHITE = Color.WHITE;
        public static final Color COLOR_GREEN = new Color(50, 168, 82);
        public static final Color COLOR_RED = new Color(217, 30, 24);
        public static final Color COLOR_PURPLE = new Color(153, 51, 204);
        public static final Color COLOR_ORANGE = new Color(255, 140, 0);
        public static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);

        // Màu sắc trong biểu đồ
        private static final Color CHART_COLOR_REVENUE = new Color(70, 130, 180); // Steel Blue
        private static final Color CHART_COLOR_BOOKING = new Color(46, 204, 113); // Emerald
        private static final Color CHART_COLOR_RATE = new Color(255, 179, 0); // Orange

        public PanelThongKeContent() {
            // Note: Panel chính chứa toàn bộ nội dung thống kê
            setLayout(new BorderLayout());

            // Tạo Panel chứa toàn bộ nội dung chính
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(MAIN_BG);
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Thêm Header và Content
            mainPanel.add(createHeader(), BorderLayout.NORTH);
            mainPanel.add(createContentPanel(), BorderLayout.CENTER);
            mainPanel.add(createSummaryFooter(), BorderLayout.SOUTH);

            add(mainPanel, BorderLayout.CENTER);
        }

        private JPanel createHeader() {
            // Note: Phần tiêu đề, phụ đề và nút Xuất/Lọc
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 0, 15, 0));

            JLabel title = new JLabel("Thống kê & Báo cáo");
            title.setFont(new Font("SansSerif", Font.BOLD, 20));

            JLabel subtitle = new JLabel("Phân tích hiệu suất hoạt động khách sạn");
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            subtitle.setForeground(COLOR_TEXT_MUTED);

            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            titlePanel.setOpaque(false);
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.add(title);
            titlePanel.add(subtitle);

            header.add(titlePanel, BorderLayout.WEST);

            // Nút Xuất báo cáo và Lọc năm
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setOpaque(false);

            JComboBox<String> yearFilter = new JComboBox<>(new String[]{"Năm 2024", "Năm 2023", "Năm 2022"});
            rightPanel.add(yearFilter);

            JButton btnExport = new JButton("Xuất báo cáo");
            btnExport.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnExport.setBackground(ACCENT_BLUE);
            btnExport.setForeground(COLOR_WHITE);
            btnExport.setFocusPainted(false);
            btnExport.setBorderPainted(false);
            btnExport.setBorder(new EmptyBorder(8, 15, 8, 15));
            rightPanel.add(btnExport);

            header.add(rightPanel, BorderLayout.EAST);

            return header;
        }

        private JPanel createContentPanel() {
            // Note: Cấu trúc chính 2 hàng (Stat Cards và Biểu đồ)
            JPanel content = new JPanel(new BorderLayout(15, 15));
            content.setOpaque(false);

            // --- Hàng 1: Stat Cards ---
            content.add(createStatCardsPanel(), BorderLayout.NORTH);

            // --- Hàng 2: Biểu đồ (GridBagLayout 2x2)
            JPanel centerGrid = new JPanel(new GridBagLayout());
            centerGrid.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 15, 15); // Khoảng cách giữa các panel

            // 1. Cột Trái trên (Doanh thu)
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.70;
            gbc.weighty = 0.5;
            centerGrid.add(createRevenueChartPanel(), gbc);

            // 2. Cột Phải trên (Phân loại phòng)
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.30;
            gbc.weighty = 0.5;
            gbc.insets = new Insets(0, 0, 15, 0);
            centerGrid.add(createRoomTypeChartPanel(), gbc);

            // 3. Cột Trái Dưới (Booking)
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.70;
            gbc.weighty = 0.5;
            gbc.insets = new Insets(0, 0, 0, 15);
            centerGrid.add(createBookingChartPanel(), gbc);

            // 4. Cột Phải Dưới (Rate)
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 0.30;
            gbc.weighty = 0.5;
            gbc.insets = new Insets(0, 0, 0, 0);
            centerGrid.add(createRateChartPanel(), gbc);

            content.add(centerGrid, BorderLayout.CENTER);

            return content;
        }

        // =================================================================================
        // CÁC HÀM TẠO COMPONENT DASHBOARD
        // =================================================================================

        private JPanel createStatCardsPanel() {
            // Note: 4 thẻ thống kê ở hàng trên cùng
            JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0)); // 1 hàng, 4 cột
            panel.setOpaque(false);

            // Stat 1: Tổng doanh thu ($ - Icon đại diện)
            panel.add(createStatCard("Tổng doanh thu", "13.700.000.000 ₫", "+12.5%", true, COLOR_GREEN.darker().darker(),
                    "$"));
            // Stat 2: Tổng booking (B - Icon đại diện)
            panel.add(createStatCard("Tổng booking", "2,435", "+5.3%", true, CHART_COLOR_BOOKING, "B"));
            // Stat 3: Tỷ lệ lấp đầy (P - Icon đại diện)
            panel.add(createStatCard("Tỷ lệ lấp đầy", "90.0%", "-2.1%", false, COLOR_PURPLE, "P"));
            // Stat 4: DT trung bình/tháng (A - Icon đại diện)
            panel.add(createStatCard("DT trung bình/tháng", "1.141.666.667 ₫", "+5.8%", true, COLOR_ORANGE, "A"));

            return panel;
        }

        /**
         * Helper tạo một thẻ thống kê (Stat Card)
         */
        private JPanel createStatCard(String title, String value, String change, boolean isPositive, Color accentColor,
                                      String iconChar) {
            // Note: Thiết kế thẻ thống kê với màu nền nhạt cho icon
            JPanel card = new JPanel(new BorderLayout(10, 5));
            card.setBackground(COLOR_WHITE);
            card.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(15, 15, 15, 15)));

            // Top: Title and Icon
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            titleLabel.setForeground(COLOR_TEXT_MUTED);
            topPanel.add(titleLabel, BorderLayout.WEST);

            // Simple Icon Placeholder (đã sửa để hiển thị ổn định)
            JLabel icon = new JLabel(iconChar, SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(30, 30));
            icon.setFont(new Font("SansSerif", Font.BOLD, 18));
            icon.setForeground(accentColor);
            icon.setOpaque(true);
            // Thiết lập màu nền icon nhạt hơn (dùng alpha 30 để tạo nền nhạt)
            icon.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));

            topPanel.add(icon, BorderLayout.EAST);

            // Center: Value
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
            card.add(valueLabel, BorderLayout.CENTER);

            // Bottom: Change (Tăng/giảm)
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);

            JLabel changeLabel = new JLabel(change);
            changeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            changeLabel.setForeground(isPositive ? COLOR_GREEN : COLOR_RED);
            bottomPanel.add(changeLabel, BorderLayout.WEST);

            card.add(bottomPanel, BorderLayout.SOUTH);

            return card;
        }

        /**
         * Tạo Panel chứa biểu đồ Doanh thu theo tháng
         */
        private JPanel createRevenueChartPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_WHITE);
            panel.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(15, 15, 15, 15)));

            JLabel title = new JLabel("Doanh thu theo tháng");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            panel.add(title, BorderLayout.NORTH);

            // Placeholder cho biểu đồ đường
            JPanel chart = new ChartPlaceholder("LINE", CHART_COLOR_REVENUE);
            panel.add(chart, BorderLayout.CENTER);

            return panel;
        }

        /**
         * Tạo Panel chứa biểu đồ Phân bổ loại phòng
         */
        private JPanel createRoomTypeChartPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_WHITE);
            panel.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(15, 15, 15, 15)));

            JLabel title = new JLabel("Phân bổ loại phòng");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            panel.add(title, BorderLayout.NORTH);

            // Placeholder cho biểu đồ tròn (Pie Chart)
            JPanel chart = new PieChartPlaceholder();
            panel.add(chart, BorderLayout.CENTER);

            return panel;
        }

        /**
         * Tạo Panel chứa biểu đồ Số lượng booking
         */
        private JPanel createBookingChartPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_WHITE);
            panel.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(15, 15, 15, 15)));

            JLabel title = new JLabel("Số lượng booking");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            panel.add(title, BorderLayout.NORTH);

            // Placeholder cho biểu đồ cột
            JPanel chart = new ChartPlaceholder("BAR_GREEN", CHART_COLOR_BOOKING);
            panel.add(chart, BorderLayout.CENTER);

            return panel;
        }

        /**
         * Tạo Panel chứa biểu đồ Tỷ lệ lấp đầy theo ngày
         */
        private JPanel createRateChartPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_WHITE);
            panel.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(15, 15, 15, 15)));

            JLabel title = new JLabel("Tỷ lệ lấp đầy theo ngày");
            title.setFont(new Font("SansSerif", Font.BOLD, 14));
            panel.add(title, BorderLayout.NORTH);

            // Placeholder cho biểu đồ cột (màu cam)
            JPanel chart = new ChartPlaceholder("BAR_ORANGE", CHART_COLOR_RATE);
            panel.add(chart, BorderLayout.CENTER);

            return panel;
        }

        // =================================================================================
        // FOOTER TÓM TẮT HIỆU SUẤT
        // =================================================================================

        private JPanel createSummaryFooter() {
            // Note: Thanh tóm tắt hiệu suất ở cuối trang
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
            footer.setBackground(COLOR_WHITE);
            footer.setBorder(new CompoundBorder(
                    new LineBorder(CARD_BORDER, 1),
                    new EmptyBorder(10, 0, 10, 0)));

            footer.add(createSummaryItem("+12.5%", "Tăng trưởng doanh thu", COLOR_GREEN));
            footer.add(createSummaryItem("89.2", "Tỷ lệ lấp đầy trung bình", ACCENT_BLUE));
            footer.add(createSummaryItem("4.8/5", "Đánh giá khách hàng", COLOR_PURPLE));

            return footer;
        }

        private JPanel createSummaryItem(String value, String label, Color color) {
            // Note: Một mục trong thanh tóm tắt
            JPanel item = new JPanel();
            item.setOpaque(false);
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            valueLabel.setForeground(color);
            valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel labelLabel = new JLabel(label);
            labelLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            labelLabel.setForeground(COLOR_TEXT_MUTED);
            labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            item.add(valueLabel);
            item.add(labelLabel);
            return item;
        }

        // =================================================================================
        // LỚP CUSTOM CHỈ ĐỂ VẼ PLACEHOLDER CHO BIỂU ĐỒ (ĐÃ SỬA LỖI TRỤC X)
        // =================================================================================

        /**
         * Lớp Placeholder cho biểu đồ cột và biểu đồ đường
         */
        private static class ChartPlaceholder extends JPanel {
            private final String type;
            private final Color color;

            public ChartPlaceholder(String type, Color color) {
                this.type = type;
                this.color = color;
                setPreferredSize(new Dimension(500, 250));
                setBackground(COLOR_WHITE);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 30;
                int labelPadding = 15;
                int yAxisLabelWidth = 50;

                int chartHeight = height - 2 * padding;
                // Đã sửa lỗi T12
                int chartWidth = width - (padding * 2) - yAxisLabelWidth;

                // Draw Y axis
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(padding + yAxisLabelWidth, height - padding, padding + yAxisLabelWidth, padding);

                // Draw X axis
                g2d.drawLine(padding + yAxisLabelWidth, height - padding, padding + yAxisLabelWidth + chartWidth,
                        height - padding);

                // Draw X-axis labels
                g2d.setColor(Color.GRAY);
                String[] xLabels = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                int numPoints = xLabels.length;

                if (type.equals("BAR_ORANGE")) {
                    xLabels = new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                    numPoints = 7;
                }

                int spacing;
                if (numPoints > 1) {
                    spacing = numPoints - 1;
                } else {
                    spacing = 1;
                }

                for (int i = 0; i < xLabels.length; i++) {
                    if (type.equals("BAR_ORANGE") && i >= numPoints)
                        break;

                    int x;
                    x = padding + yAxisLabelWidth + i * chartWidth / spacing;

                    String label = xLabels[i];
                    g2d.drawString(label, x - g2d.getFontMetrics().stringWidth(label) / 2, height - padding + labelPadding);
                }

                // Draw Y-axis labels
                g2d.setColor(Color.GRAY);
                if (type.startsWith("LINE")) {
                    String[] yLabels = {"0.0", "0.4", "0.8", "1.2", "1.6B"};
                    int numYLabels = yLabels.length;

                    for (int i = 0; i < numYLabels; i++) {
                        int y = height - padding - i * (chartHeight / (numYLabels - 1));
                        String label = yLabels[i];

                        if (i > 0) {
                            g2d.setColor(new Color(240, 240, 240));
                            g2d.drawLine(padding + yAxisLabelWidth, y, padding + yAxisLabelWidth + chartWidth, y);
                            g2d.setColor(Color.GRAY);
                        }
                        g2d.drawString(label, padding, y + g2d.getFontMetrics().getHeight() / 4);
                    }
                } else if (type.equals("BAR_GREEN")) {
                    String[] yLabels = {"0", "65", "130", "195", "260"};
                    int numYLabels = yLabels.length;
                    for (int i = 0; i < numYLabels; i++) {
                        int y = height - padding - i * (chartHeight / (numYLabels - 1));
                        String label = yLabels[i];
                        g2d.drawString(label, padding, y + g2d.getFontMetrics().getHeight() / 4);
                    }
                } else if (type.equals("BAR_ORANGE")) {
                    String[] yLabels = {"0", "25", "50", "75", "100"};
                    int numYLabels = yLabels.length;
                    for (int i = 0; i < numYLabels; i++) {
                        int y = height - padding - i * (chartHeight / (numYLabels - 1));
                        String label = yLabels[i];
                        g2d.drawString(label, padding, y + g2d.getFontMetrics().getHeight() / 4);
                    }
                }

                // Draw placeholder data
                g2d.setColor(color);

                if (type.startsWith("LINE")) {
                    double[] revenueData = {0.88, 0.92, 0.83, 1.15, 1.25, 1.45, 1.55, 1.5, 1.25, 1.15, 0.95, 1.35};
                    double maxVal = 1.6;
                    double minVal = 0.0;
                    double range = maxVal - minVal;

                    g2d.setStroke(new BasicStroke(2));
                    for (int i = 0; i < xLabels.length; i++) {
                        int pX1 = padding + yAxisLabelWidth + i * chartWidth / spacing;
                        int pY1 = height - padding - (int) ((revenueData[i] - minVal) * chartHeight / range);

                        if (i < xLabels.length - 1) {
                            int pX2 = padding + yAxisLabelWidth + (i + 1) * chartWidth / spacing;
                            int pY2 = height - padding - (int) ((revenueData[i + 1] - minVal) * chartHeight / range);

                            g2d.drawLine(pX1, pY1, pX2, pY2);
                        }
                        g2d.fillOval(pX1 - 3, pY1 - 3, 6, 6);
                    }
                } else {
                    int barWidth = chartWidth / spacing / 3;

                    for (int i = 0; i < numPoints; i++) {
                        double val = 0;
                        double chartMax = 1;

                        if (type.equals("BAR_GREEN")) {
                            double[] bookingData = {150, 135, 190, 180, 220, 240, 255, 250, 200, 195, 170, 230};
                            val = bookingData[i];
                            chartMax = 260;
                        } else if (type.equals("BAR_ORANGE")) {
                            double[] rateData = {85, 78, 92, 90, 95, 98, 99};
                            val = rateData[i];
                            chartMax = 100;
                        }

                        if (val > 0) {
                            int barHeight = (int) (val * chartHeight / chartMax);
                            int xCenter = padding + yAxisLabelWidth + i * chartWidth / spacing;
                            int x = xCenter - (barWidth / 2);
                            int y = height - padding - barHeight;
                            g2d.fillRect(x, y, barWidth, barHeight);
                        }
                    }
                }
            }
        }

        /**
         * Lớp Placeholder cho biểu đồ tròn (Pie Chart)
         */
        private static class PieChartPlaceholder extends JPanel {
            private final Color[] colors = {
                    new Color(70, 130, 180), // Standard 35%
                    new Color(46, 204, 113), // Deluxe 30%
                    new Color(255, 179, 0), // Suite 20%
                    new Color(217, 30, 24) // Presidential 15%
            };
            private final int[] percentages = {35, 30, 20, 15};
            private final String[] labels = {"Standard 35%", "Deluxe 30%", "Suite 20%", "Presidential 15%"};

            public PieChartPlaceholder() {
                setPreferredSize(new Dimension(500, 250));
                setBackground(COLOR_WHITE);
                setLayout(new GridBagLayout());

                // Replicate the legend arrangement (Right side of the pie)
                JPanel legendPanel = new JPanel();
                legendPanel.setOpaque(false);
                legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
                legendPanel.setBorder(new EmptyBorder(0, 20, 0, 0));

                for (int i = 0; i < labels.length; i++) {
                    JLabel label = new JLabel(labels[i]);
                    label.setForeground(colors[i]);
                    label.setFont(new Font("SansSerif", Font.BOLD, 12));
                    legendPanel.add(label);
                }

                GridBagConstraints gbc = new GridBagConstraints();

                // Add Pie Chart (Custom Paint)
                gbc.gridx = 0;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                add(new PiePanel(), gbc);

                // Add Legend
                gbc.gridx = 1;
                gbc.weightx = 0.5;
                gbc.fill = GridBagConstraints.NONE;
                add(legendPanel, gbc);
            }

            private class PiePanel extends JPanel {
                public PiePanel() {
                    setOpaque(false);
                    setPreferredSize(new Dimension(200, 200));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int diameter = Math.min(getWidth(), getHeight()) - 20;
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;

                    int startAngle = 90;
                    for (int i = 0; i < percentages.length; i++) {
                        int angle = (int) (percentages[i] * 3.6);
                        g2d.setColor(colors[i]);
                        g2d.fillArc(x, y, diameter, diameter, startAngle, -angle);
                        startAngle -= angle;
                    }
                }
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

