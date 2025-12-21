package ui.gui.FormDialog;

import dao.PhieuDatPhong_DAO;
import dao.Phong_DAO;
import entity.KhachHang;
import entity.Phong;
import event.EventDatPhong;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HistoryCheckOutDialog extends JDialog {

    private PhieuDatPhong_DAO phieuDatPhongDAO;
    private Phong_DAO phongDAO;
    private EventDatPhong eventController;

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTongSo;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0 đ");

    public HistoryCheckOutDialog(Frame owner, EventDatPhong controller, PhieuDatPhong_DAO pddDAO, Phong_DAO pDAO) {
        super(owner, "Lịch sử Check Out", true);
        this.eventController = controller;
        this.phieuDatPhongDAO = pddDAO;
        this.phongDAO = pDAO;

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 10));
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        txtSearch = new JTextField(" Tìm theo mã đặt phòng, tên khách, số phòng, SĐT...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().contains("Tìm theo")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(" Tìm theo mã đặt phòng, tên khách, số phòng, SĐT...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        txtSearch.addActionListener(e -> loadData());

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> loadData());

        header.add(txtSearch, BorderLayout.CENTER);
        header.add(btnSearch, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Mã ĐP", "Khách hàng", "Phòng", "Loại phòng", "Check In", "Check Out", "Số khách", "Tổng tiền", "Thao tác"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Chỉ cho sửa cột nút
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false); // Khóa kéo cột

        // Style Header
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(248, 249, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        TableColumnModel tcm = table.getColumnModel();

        // 1. Set độ rộng (Dùng setPreferredWidth)
        tcm.getColumn(0).setPreferredWidth(80);  // Mã ĐP
        tcm.getColumn(1).setPreferredWidth(180); // Khách hàng
        tcm.getColumn(2).setPreferredWidth(60);  // Phòng
        tcm.getColumn(3).setPreferredWidth(100); // Loại phòng
        tcm.getColumn(4).setPreferredWidth(90);  // Check In
        tcm.getColumn(5).setPreferredWidth(90);  // Check Out
        tcm.getColumn(6).setPreferredWidth(70);  // Số khách
        tcm.getColumn(7).setPreferredWidth(110); // Tổng tiền
        tcm.getColumn(8).setPreferredWidth(90);  // Thao tác

        // 2. Tạo Renderer
        HistoryCellRenderer centerRenderer = new HistoryCellRenderer(JLabel.CENTER);
        HistoryCellRenderer leftRenderer = new HistoryCellRenderer(JLabel.LEFT);

        // 3. ÁP DỤNG CĂN LỀ (TẤT CẢ TRÁI - TRỪ SỐ KHÁCH)
        tcm.getColumn(0).setCellRenderer(leftRenderer);   // Mã ĐP -> TRÁI
        tcm.getColumn(1).setCellRenderer(leftRenderer);   // Tên KH -> TRÁI
        tcm.getColumn(2).setCellRenderer(leftRenderer);   // Phòng -> TRÁI (Đã sửa)
        tcm.getColumn(3).setCellRenderer(leftRenderer);   // Loại -> TRÁI
        tcm.getColumn(4).setCellRenderer(leftRenderer);   // Check In -> TRÁI (Đã sửa)
        tcm.getColumn(5).setCellRenderer(leftRenderer);   // Check Out -> TRÁI (Đã sửa)

        tcm.getColumn(6).setCellRenderer(centerRenderer); // Số khách -> GIỮA (Giữ nguyên)

        // Cột Tiền (7): Đã được chỉnh Left trong MoneyCellRenderer ở bước trước
        tcm.getColumn(7).setCellRenderer(new MoneyCellRenderer());

        // Cột Nút (8)
        tcm.getColumn(8).setCellRenderer(new ButtonRenderer());
        tcm.getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new LineBorder(GUI_NhanVienLeTan.CARD_BORDER));
        return scrollPane;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        lblTongSo = new JLabel("Tổng số: 0 đặt phòng đã check-out");
        lblTongSo.setForeground(GUI_NhanVienLeTan.COLOR_TEXT_MUTED);
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        footer.add(lblTongSo, BorderLayout.WEST);
        footer.add(btnClose, BorderLayout.EAST);
        return footer;
    }

    private void loadData() {
        String searchText = txtSearch.getText();
        if (searchText.contains("Tìm theo")) searchText = "";
        tableModel.setRowCount(0);

        try {
            List<Object[]> dataList = phieuDatPhongDAO.getFilteredBookingData(searchText, "Đã trả phòng");
            for (Object[] row : dataList) {
                String maPhong = row[2].toString();
                String maPhieu = row[5].toString();

                // --- SỬA ĐOẠN NÀY: ƯU TIÊN LẤY TIỀN TỪ DATABASE ---
                double tongTien = 0;

                // 1. Lấy tổng tiền thực tế từ Hóa đơn (Index 10 trong DAO)
                if (row.length > 10 && row[10] != null) {
                    try {
                        tongTien = Double.parseDouble(row[10].toString());
                    } catch (NumberFormatException e) {
                        tongTien = 0;
                    }
                }

                // 2. Fallback: Chỉ tính thủ công nếu chưa có hóa đơn (ít khi xảy ra ở màn hình lịch sử)
                if (tongTien <= 0) {
                    Phong phong = null;
                    try { phong = phongDAO.getPhongById(maPhong); } catch (Exception e) {}

                    if (phong != null) {
                        try {
                            LocalDate checkin = LocalDate.parse(row[3].toString(), DATE_FORMAT);
                            LocalDate checkout = LocalDate.parse(row[4].toString(), DATE_FORMAT);
                            long soDem = ChronoUnit.DAYS.between(checkin, checkout);
                            if (soDem <= 0) soDem = 1;
                            tongTien = phong.getGiaTienMotDem() * soDem;
                        } catch (Exception e) {
                            tongTien = phong.getGiaTienMotDem();
                        }
                    }
                }
                // --------------------------------------------------

                tableModel.addRow(new Object[]{
                        maPhieu,
                        row[0].toString(),
                        maPhong,
                        "Phòng " + maPhong, // Hoặc lấy loại phòng nếu có
                        row[3].toString(),
                        row[4].toString(),
                        "1", // Số khách mặc định hoặc lấy từ DAO nếu có
                        tongTien, // <-- SỐ TIỀN KHỚP VỚI BILL
                        maPhieu
                });
            }
            lblTongSo.setText("Tổng số: " + tableModel.getRowCount() + " đặt phòng đã check-out");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- CÁC CLASS RENDERER & EDITOR ĐÃ CHỈNH SỬA ---

    // Renderer chung có hỗ trợ căn lề (Left, Center, Right)
    private static class HistoryCellRenderer extends DefaultTableCellRenderer {
        public HistoryCellRenderer(int alignment) {
            super();
            setHorizontalAlignment(alignment); // Thiết lập căn lề
            // Tạo đường gạch dưới mờ và padding 2 bên để chữ không dính sát mép
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(0, 10, 0, 10) // Padding trái phải 10px
            ));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            // Fix màu chữ khi được chọn
            c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            return c;
        }
    }

    private static class MoneyCellRenderer extends DefaultTableCellRenderer {
        public MoneyCellRenderer() {
            super();
            // --- SỬA TỪ RIGHT -> LEFT ---
            setHorizontalAlignment(SwingConstants.LEFT);

            // Padding trái 10px để chữ không dính sát mép
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(0, 10, 0, 10)
            ));
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof Number) {
                setText(MONEY_FORMAT.format(value));
            } else {
                super.setValue(value);
            }
        }
    }

    // --- 1. RENDERER NÚT MỚI: Xanh dương, Nhỏ gọn ---
    private static class ButtonRenderer extends DefaultTableCellRenderer {
        private JButton button;
        private JPanel panel;

        public ButtonRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2)); // Canh giữa
            panel.setOpaque(true);

            button = new JButton("Xem Bill");
            button.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Font nhỏ
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0, 123, 255)); // Xanh dương Bootstrap

            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);
            button.setOpaque(true);
            button.setPreferredSize(new Dimension(80, 22)); // Kích thước nhỏ

            panel.add(button);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) panel.setBackground(table.getSelectionBackground());
            else panel.setBackground(Color.WHITE);
            return panel;
        }
    }

    // --- 2. EDITOR NÚT MỚI ---
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private JPanel panel;
        private String currentMaPhieu;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
            panel.setOpaque(true);

            button = new JButton("Xem Bill");
            button.setFont(new Font("Segoe UI", Font.BOLD, 10));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0, 123, 255));

            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setPreferredSize(new Dimension(80, 22));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                fireEditingStopped();
                if (currentMaPhieu != null && eventController != null) {
                    System.out.println("Xem bill cho: " + currentMaPhieu);
                    eventController.handleShowBill(currentMaPhieu);
                }
            });
            panel.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentMaPhieu = (value == null) ? "" : value.toString();
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentMaPhieu;
        }
    }
}