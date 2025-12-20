package ui.gui.FormDialog;

// SỬA: Import các lớp DAO và Entity cần thiết
import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO; // Chỉ cần DAO chính và DAO chi tiết
import entity.*; // Import tất cả entity
import ui.gui.GUI_NhanVienLeTan; // Cần cho các hằng số màu

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.SQLException; // Cần cho việc gọi DAO
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * JDialog để hiển thị hóa đơn thanh toán chi tiết dựa trên maHoaDon.
 * (ĐÃ SỬA: Lấy dữ liệu từ DAO và Entity)
 */
public class BillDialog extends JDialog {

    // Định dạng ngày giờ, tiền tệ (Giữ nguyên)
    private static final DateTimeFormatter DATE_TIME_FORMATTER_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private String maHoaDon;

    // --- DAO Dependencies ---
    private HoaDon_DAO hoaDonDAO;
    private ChiTietHoaDon_DAO chiTietHoaDonDAO;

    // --- Dữ liệu Entity lấy từ CSDL ---
    private HoaDon hoaDon; // Chứa KhachHang, NhanVien, KhuyenMai
    private List<ChiTietHoaDon_Phong> dsChiTietPhong;
    private List<ChiTietHoaDon_DichVu> dsChiTietDichVu;

    /**
     * Constructor cho BillDialog.
     * @param owner Frame cha
     * @param maHoaDon Mã hóa đơn cần hiển thị
     */
    public BillDialog(Frame owner, String maHoaDon) {
        super(owner, "Hóa Đơn Thanh Toán - Mã: " + maHoaDon, true);
        this.maHoaDon = maHoaDon;

        // --- Khởi tạo DAO ---
        try {
            this.hoaDonDAO = new HoaDon_DAO();
            this.chiTietHoaDonDAO = new ChiTietHoaDon_DAO();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Lỗi khởi tạo DAO: " + e.getMessage(), "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // --- Tải dữ liệu từ CSDL ---
        if (!loadBillData()) {
            JOptionPane.showMessageDialog(owner, "Không thể tải dữ liệu cho hóa đơn " + maHoaDon, "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // --- Thiết lập giao diện ---
        setSize(700, 750);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        add(createTopButtonPanel(), BorderLayout.NORTH);

        JPanel mainBillPanel = new JPanel(new BorderLayout(10, 20));
        mainBillPanel.setOpaque(false);
        mainBillPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        mainBillPanel.add(createHeaderSection(), BorderLayout.NORTH);
        mainBillPanel.add(createCustomerBookingInfoPanel(), BorderLayout.CENTER);
        mainBillPanel.add(createBillingDetailsPanel(), BorderLayout.SOUTH);

        add(mainBillPanel, BorderLayout.CENTER);
    }

    /** Tải tất cả dữ liệu cần thiết cho hóa đơn từ CSDL */
    private boolean loadBillData() {
        System.out.println("Đang tải dữ liệu cho hóa đơn: " + maHoaDon);
        try {
            // 1. Lấy Hóa đơn chính (đã bao gồm KH, NV, KM nếu có)
            this.hoaDon = hoaDonDAO.getHoaDonById(maHoaDon);
            if (this.hoaDon == null) {
                System.err.println("Không tìm thấy hóa đơn với mã: " + maHoaDon);
                return false; // Hóa đơn không tồn tại
            }

            // 2. Lấy danh sách chi tiết phòng và dịch vụ từ DAO chi tiết
            // (getHoaDonById nên đã làm việc này, nhưng gọi lại để chắc)
            this.dsChiTietPhong = chiTietHoaDonDAO.getChiTietPhongByMaHD(maHoaDon);
            this.dsChiTietDichVu = chiTietHoaDonDAO.getChiTietDichVuByMaHD(maHoaDon);

            // Gán lại vào đối tượng HoaDon
            this.hoaDon.setDsChiTietPhong(this.dsChiTietPhong);
            this.hoaDon.setDsChiTietDichVu(this.dsChiTietDichVu);

            System.out.println("Tải dữ liệu hóa đơn thành công.");
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tải dữ liệu hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải dữ liệu hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Tạo panel chứa nút In và Tải PDF */
    private JPanel createTopButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE); // Nền trắng

        JButton btnPrint = new JButton(" In Hóa Đơn");
        // Sử dụng hằng số màu từ GUI_NhanVienLeTan
        btnPrint.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
        btnPrint.setForeground(Color.WHITE); // Chữ trắng cho dễ đọc
        btnPrint.setFocusPainted(false);
        btnPrint.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnPrint.addActionListener(e -> printBill()); // Gọi hàm in

        JButton btnDownload = new JButton(" Tải PDF");
        // Sử dụng hằng số màu từ GUI_NhanVienLeTan
        btnDownload.setBackground(GUI_NhanVienLeTan.COLOR_GREEN);
        btnDownload.setForeground(Color.WHITE); // Chữ trắng
        btnDownload.setFocusPainted(false); // Sửa lại: setFocusPainted
        btnDownload.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnDownload.addActionListener(e -> downloadPdf()); // Gọi hàm tải PDF

        buttonPanel.add(btnPrint);
        buttonPanel.add(btnDownload);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(buttonPanel, BorderLayout.CENTER);
        topWrapper.setBackground(buttonPanel.getBackground());

        return topWrapper;
    }

    /** Tạo phần header chứa thông tin khách sạn và tiêu đề hóa đơn */
    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);

        JPanel hotelInfoPanel = new JPanel();
        hotelInfoPanel.setLayout(new BoxLayout(hotelInfoPanel, BoxLayout.Y_AXIS));
        hotelInfoPanel.setOpaque(false);
        // Thêm các JLabel thông tin khách sạn
        JLabel logo = new JLabel(" [LOGO] TBQTT Hotel", SwingConstants.LEFT);
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        hotelInfoPanel.add(logo);
        hotelInfoPanel.add(new JLabel("Địa chỉ: 68/18 Lữ Gia - Phường 15 - Quận 11- TP. Hồ Chí Minh"));
        hotelInfoPanel.add(new JLabel("Điện thoại: (028) 3964 1828"));
        hotelInfoPanel.add(new JLabel("Website: tbqtthotel.vn"));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel mainTitle = new JLabel("HÓA ĐƠN THANH TOÁN DỊCH VỤ", SwingConstants.CENTER);
        mainTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setOpaque(false);
        // Lấy ngày lập hóa đơn từ dữ liệu đã load
        LocalDateTime ngayLap = (hoaDon != null && hoaDon.getNgayLap() != null) ? hoaDon.getNgayLap() : LocalDateTime.now();
        // Sử dụng Locale tiếng Việt
    DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy", java.util.Locale.forLanguageTag("vi-VN"));
        subHeader.add(new JLabel(ngayLap.format(dayFormatter)), BorderLayout.WEST);
        subHeader.add(new JLabel("Số HĐ: " + maHoaDon), BorderLayout.EAST);

        titlePanel.add(mainTitle, BorderLayout.CENTER);
        titlePanel.add(subHeader, BorderLayout.SOUTH);

        headerPanel.add(hotelInfoPanel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.SOUTH);
        return headerPanel;
    }

    /** Tạo panel chứa thông tin khách hàng và đặt phòng */
    private JPanel createCustomerBookingInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // --- Cột trái: Thông tin khách hàng ---
        JPanel customerPanel = new JPanel();
        customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
        customerPanel.setOpaque(false);
        KhachHang kh = (hoaDon != null) ? hoaDon.getKhachHang() : null;
        customerPanel.add(createInfoRow("Tên", ": " + (kh != null ? kh.getTenKH() : "N/A")));
        customerPanel.add(createInfoRow("Địa chỉ", ": " + (kh != null && kh.getDiaChi() != null ? kh.getDiaChi() : "...")));
        customerPanel.add(createInfoRow("Điện thoại", ": " + (kh != null ? kh.getSoDT() : "...")));
        customerPanel.add(createInfoRow("Mã số thuế", ": ...")); // Schema không có

        // Lấy danh sách mã phòng từ chi tiết hóa đơn
        StringBuilder maPhongStr = new StringBuilder(": ");
        if (dsChiTietPhong != null && !dsChiTietPhong.isEmpty()) {
            for(ChiTietHoaDon_Phong ctPhong : dsChiTietPhong) {
                if (ctPhong.getPhieuDatPhong() != null && ctPhong.getPhieuDatPhong().getPhong() != null) {
                    maPhongStr.append(ctPhong.getPhieuDatPhong().getPhong().getMaPhong()).append(", ");
                }
            }
            if (maPhongStr.length() > 2) maPhongStr.setLength(maPhongStr.length() - 2);
        } else {
            maPhongStr.append("N/A");
        }
        customerPanel.add(createInfoRow("Phòng", maPhongStr.toString()));


        // --- Cột phải: Thông tin đặt phòng ---
        JPanel bookingPanel = new JPanel();
        bookingPanel.setLayout(new BoxLayout(bookingPanel, BoxLayout.Y_AXIS));
        bookingPanel.setOpaque(false);
        String ngayDenStr = "N/A";
        String ngayDiStr = "N/A";
        long soDem = 0;
        PhieuDatPhong pdp = null;
        if (dsChiTietPhong != null && !dsChiTietPhong.isEmpty()) {
            pdp = dsChiTietPhong.get(0).getPhieuDatPhong(); // Lấy phiếu đầu tiên
        }

        if (pdp != null) {
            LocalDateTime ngayDen = pdp.getNgayNhanPhong();
            LocalDateTime ngayDi = pdp.getNgayTraPhong();
            ngayDenStr = (ngayDen != null) ? ngayDen.format(DATE_TIME_FORMATTER_VN) : "N/A";
            ngayDiStr = (ngayDi != null) ? ngayDi.format(DATE_TIME_FORMATTER_VN) : "N/A";
            if (ngayDen != null && ngayDi != null) {
                soDem = ChronoUnit.DAYS.between(ngayDen.toLocalDate(), ngayDi.toLocalDate());
                if (soDem == 0 && ngayDen.toLocalTime().isBefore(ngayDi.toLocalTime())) soDem = 1;
                if(soDem <= 0) soDem = 1; // Đảm bảo ít nhất 1 đêm
            } else {
                soDem = 1; // Mặc định 1 đêm nếu thiếu ngày
            }
        }
        bookingPanel.add(createInfoRow("Ngày đến", ": " + ngayDenStr));
        bookingPanel.add(createInfoRow("Ngày đi", ": " + ngayDiStr));
        bookingPanel.add(createInfoRow("Tổng số đêm", ": " + soDem));
        NhanVien nv = (hoaDon != null) ? hoaDon.getNhanVien() : null;
        bookingPanel.add(createInfoRow("Thu ngân", ": " + (nv != null ? nv.getTenNV() : "N/A")));
        bookingPanel.add(createInfoRow("Thanh toán", ": " + (hoaDon != null ? hoaDon.getHinhThucThanhToan() : "...")));


        infoPanel.add(customerPanel);
        infoPanel.add(bookingPanel);
        return infoPanel;
    }

    /** Phương thức phụ trợ tạo một hàng thông tin (Label: Value) */
    private Box createInfoRow(String label, String value) {
        Box row = Box.createHorizontalBox();
        JLabel lblLabel = new JLabel(label);
        // Đặt kích thước cố định cho nhãn để căn chỉnh
        lblLabel.setPreferredSize(new Dimension(100, 20));
        lblLabel.setMinimumSize(new Dimension(100, 20));
        lblLabel.setMaximumSize(new Dimension(100, 20));
        lblLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.PLAIN, 12));
        row.add(lblLabel);
        row.add(lblValue);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Giới hạn chiều cao của Box
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return row;
    }

    /** Tạo phần chi tiết hóa đơn (bảng) và tổng cộng */
    private JPanel createBillingDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 15));
        detailsPanel.setOpaque(false);

        // --- Bảng chi tiết ---
        String[] columnNames = {"NGÀY", "CHI TIẾT", "SỐ TIỀN"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        double subTotal = 0; // Tính tổng tiền hàng (chưa VAT, KM)

        // Thêm chi tiết phòng vào bảng
        if (dsChiTietPhong != null) {
            for (ChiTietHoaDon_Phong ctPhong : dsChiTietPhong) {
                PhieuDatPhong pdp = ctPhong.getPhieuDatPhong();
                Phong phong = (pdp != null) ? pdp.getPhong() : null;
                LoaiPhongEntity lp = (phong != null) ? phong.getLoaiPhong() : null;

                if (pdp != null && phong != null) {
                    String maPhong = phong.getMaPhong();
                    String tenLoaiPhong = (lp != null) ? lp.getTenLoaiPhong() : "";
                    LocalDateTime ngayDen = pdp.getNgayNhanPhong();
                    LocalDateTime ngayDi = pdp.getNgayTraPhong();
                    String ngayStr = (ngayDen != null) ? ngayDen.format(DATE_FORMATTER_VN) : "N/A";

                    long soDem = 0;
                    if (ngayDen != null && ngayDi != null) {
                        soDem = ChronoUnit.DAYS.between(ngayDen.toLocalDate(), ngayDi.toLocalDate());
                        if (soDem == 0 && ngayDen.toLocalTime().isBefore(ngayDi.toLocalTime())) soDem = 1;
                        if(soDem <= 0) soDem = 1; // Đảm bảo ít nhất 1 đêm
                    } else {
                        soDem = 1; // Mặc định 1 đêm nếu thiếu ngày
                    }

                    String chiTiet = "Tiền phòng " + maPhong + " - " + tenLoaiPhong + " (" + soDem + " đêm)";
                    double donGiaLucDat = ctPhong.getDonGiaLucDat();
                    double thanhTienPhong = donGiaLucDat * soDem;
                    subTotal += thanhTienPhong;
                    tableModel.addRow(new Object[]{ngayStr, chiTiet, MONEY_FORMAT.format(thanhTienPhong)});
                }
            }
        }

        // Thêm chi tiết dịch vụ vào bảng
        if (dsChiTietDichVu != null) {
            for (ChiTietHoaDon_DichVu ctDV : dsChiTietDichVu) {
                DichVu dv = ctDV.getDichVu();
                if (dv != null) {
                    String tenDV = dv.getTenDV();
                    int soLuong = ctDV.getSoLuong();
                    String chiTiet = tenDV + (soLuong > 1 ? " (SL: " + soLuong + ")" : "");
                    double thanhTienDV = ctDV.getThanhTien(); // Lấy thành tiền đã tính
                    subTotal += thanhTienDV;
                    String ngayStr = (hoaDon != null && hoaDon.getNgayLap() != null) ? hoaDon.getNgayLap().format(DATE_FORMATTER_VN) : "N/A";
                    tableModel.addRow(new Object[]{ngayStr, chiTiet, MONEY_FORMAT.format(thanhTienDV)});
                }
            }
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(120); // Cột Ngày
        tcm.getColumn(1).setPreferredWidth(350); // Cột Chi tiết
        tcm.getColumn(2).setPreferredWidth(100); // Cột Số tiền
        // Căn phải cột tiền
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tcm.getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        // Điều chỉnh chiều cao bảng dựa trên số dòng (tối đa ~10 dòng)
        int preferredHeight = Math.min(table.getRowCount() * table.getRowHeight() + table.getTableHeader().getPreferredSize().height + 5, 300);
        scrollPane.setPreferredSize(new Dimension(0, preferredHeight));

        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Phần Tổng cộng, Thuế, Khuyến mãi, Chữ ký ---
        JPanel bottomPanel = new JPanel(); bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); bottomPanel.setOpaque(false);

        bottomPanel.add(createTotalRow("Tổng tiền hàng:", MONEY_FORMAT.format(subTotal) + " đ"));

        double discountAmount = 0;
        KhuyenMai km = (hoaDon != null) ? hoaDon.getKhuyenMai() : null;
        if (km != null) {
            double chietKhau = km.getChietKhau() / 100.0;
            discountAmount = subTotal * chietKhau;
            String kmName = km.getTenKhuyenMai();
            if (discountAmount > 0) {
                bottomPanel.add(createTotalRow(kmName + " (" + String.format("%.0f", chietKhau * 100) + "%):", "- " + MONEY_FORMAT.format(discountAmount) + " đ"));
            }
        }

        double subTotalAfterDiscount = subTotal - discountAmount;
        bottomPanel.add(createTotalRow("Thành tiền:", MONEY_FORMAT.format(subTotalAfterDiscount) + " đ"));

        double vatRate = (hoaDon != null) ? hoaDon.getVat() / 100.0 : 0.0;
        double vatAmount = subTotalAfterDiscount * vatRate;
        if (vatAmount > 0) {
            bottomPanel.add(createTotalRow("Thuế VAT (" + String.format("%.0f", vatRate * 100) + "%):", MONEY_FORMAT.format(vatAmount) + " đ"));
        }

        double finalTotal = subTotalAfterDiscount + vatAmount;
        JPanel finalTotalPanel = createTotalRow("Tổng cộng:", MONEY_FORMAT.format(finalTotal) + " đ");
        for(Component c : finalTotalPanel.getComponents()) {
            if (c instanceof JLabel) ((JLabel) c).setFont(((JLabel) c).getFont().deriveFont(Font.BOLD, 14f));
        }
        bottomPanel.add(finalTotalPanel);

        JLabel vatLabel = new JLabel("Đã bao gồm thuế VAT"); vatLabel.setFont(vatLabel.getFont().deriveFont(Font.ITALIC, 11f)); vatLabel.setForeground(Color.GRAY); vatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(vatLabel);
        bottomPanel.add(Box.createVerticalStrut(30));

        JPanel signaturePanel = new JPanel(new GridLayout(1, 2, 100, 0)); signaturePanel.setOpaque(false); signaturePanel.setBorder(new EmptyBorder(0, 50, 0, 50));
        JLabel thuNganLabel = new JLabel("Thu ngân", SwingConstants.CENTER); JLabel khachLabel = new JLabel("Khách", SwingConstants.CENTER);
        signaturePanel.add(thuNganLabel); signaturePanel.add(khachLabel); signaturePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(signaturePanel);

        detailsPanel.add(bottomPanel, BorderLayout.SOUTH);
        return detailsPanel;
    }

    /** Phương thức phụ trợ tạo một hàng trong phần tổng tiền */
    private JPanel createTotalRow(String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblValue.setPreferredSize(new Dimension(150, 20));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        rowPanel.add(lblLabel);
        rowPanel.add(lblValue);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return rowPanel;
    }

    // --- Hàm xử lý sự kiện nút bấm ---
    private void printBill() {
        System.out.println("Placeholder: Xử lý in hóa đơn " + maHoaDon);
        JOptionPane.showMessageDialog(this, "Chức năng In đang được phát triển.");
        // Thêm code sử dụng Java Print Service hoặc thư viện JasperReports
    }

    private void downloadPdf() {
        System.out.println("Placeholder: Xử lý tải PDF hóa đơn " + maHoaDon);
        JOptionPane.showMessageDialog(this, "Chức năng Tải PDF đang được phát triển.");
        // Thêm code sử dụng thư viện iTextPDF hoặc Apache PDFBox
    }

} // Kết thúc lớp BillDialog