package ui.gui.FormDialog;

// SỬA: Import các lớp DAO và Entity cần thiết
import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO; // Chỉ cần DAO chính và DAO chi tiết
import dao.KhuyenMai_DAO; // Thêm DAO Khuyến mãi
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
     * 
     * @param owner    Frame cha
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
            JOptionPane.showMessageDialog(owner, "Lỗi khởi tạo DAO: " + e.getMessage(), "Lỗi nghiêm trọng",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // --- Tải dữ liệu từ CSDL ---
        if (!loadBillData()) {
            JOptionPane.showMessageDialog(owner, "Không thể tải dữ liệu cho hóa đơn " + maHoaDon, "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
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
        LocalDateTime ngayLap = (hoaDon != null && hoaDon.getNgayLap() != null) ? hoaDon.getNgayLap()
                : LocalDateTime.now();
        // Sử dụng Locale tiếng Việt
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy",
                java.util.Locale.forLanguageTag("vi-VN"));
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
        customerPanel
                .add(createInfoRow("Địa chỉ", ": " + (kh != null && kh.getDiaChi() != null ? kh.getDiaChi() : "...")));
        customerPanel.add(createInfoRow("Điện thoại", ": " + (kh != null ? kh.getSoDT() : "...")));
        customerPanel.add(createInfoRow("Mã số thuế", ": ...")); // Schema không có

        // Lấy danh sách mã phòng từ chi tiết hóa đơn
        StringBuilder maPhongStr = new StringBuilder(": ");
        if (dsChiTietPhong != null && !dsChiTietPhong.isEmpty()) {
            for (ChiTietHoaDon_Phong ctPhong : dsChiTietPhong) {
                if (ctPhong.getPhieuDatPhong() != null && ctPhong.getPhieuDatPhong().getPhong() != null) {
                    maPhongStr.append(ctPhong.getPhieuDatPhong().getPhong().getMaPhong()).append(", ");
                }
            }
            if (maPhongStr.length() > 2)
                maPhongStr.setLength(maPhongStr.length() - 2);
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
                if (soDem == 0 && ngayDen.toLocalTime().isBefore(ngayDi.toLocalTime()))
                    soDem = 1;
                if (soDem <= 0)
                    soDem = 1; // Đảm bảo ít nhất 1 đêm
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

    // --- UI Logic MỚI: Thêm TextField nhập mã khuyến mãi ---
    // (Đã xóa các biến input giảm giá interactive)

    private KhuyenMai_DAO khuyenMaiDAO; // Thêm DAO Khuyến mãi

    private JPanel createBillingDetailsPanel() {
        // Khởi tạo DAO Khuyến mãi
        try {
            if (khuyenMaiDAO == null) khuyenMaiDAO = new KhuyenMai_DAO();
        } catch (Exception e) { e.printStackTrace(); }

        JPanel detailsPanel = new JPanel(new BorderLayout(0, 10));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // --- 1. Cấu trúc bảng hiện đại ---
        String[] columnNames = { "STT", "Nội dung", "ĐVT", "SL", "Đơn giá", "Thành tiền" };

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        double subTotalCalculated = 0; // Tính tổng lại để hiển thị
        int stt = 1;

        // --- 2. Xử lý Chi tiết Phòng (Dùng Logic ngược để đảm bảo khớp tiền) ---
        if (dsChiTietPhong != null) {
            for (ChiTietHoaDon_Phong ctPhong : dsChiTietPhong) {
                PhieuDatPhong pdp = ctPhong.getPhieuDatPhong();
                Phong phong = (pdp != null) ? pdp.getPhong() : null;

                if (pdp != null && phong != null) {
                    String tenPhong = "Phòng " + phong.getMaPhong();
                    String loaiPhong = (phong.getLoaiPhong() != null) ? phong.getLoaiPhong().getTenLoaiPhong() : "";

                    // LẤY TIỀN THẬT TỪ DB
                    double thanhTienPhong = ctPhong.getThanhTien(); // Số tiền đã lưu trong DB
                    double donGia = ctPhong.getDonGiaLucDat();      // Giá lúc đặt

                    // LOGIC NGƯỢC: Tính số đêm từ tiền (để đảm bảo khớp 100%)
                    long soDem = 1;
                    if (donGia > 0) {
                        soDem = Math.round(thanhTienPhong / donGia);
                    }
                    if (soDem <= 0) soDem = 1;

                    // Format ngày tháng hiển thị
                    LocalDateTime den = pdp.getNgayNhanPhong();
                    LocalDateTime di = pdp.getNgayTraPhong();
                    String timeRange = "";
                    if (den != null && di != null) {
                        timeRange = String.format("(%s - %s)",
                                den.format(DateTimeFormatter.ofPattern("dd/MM")),
                                di.format(DateTimeFormatter.ofPattern("dd/MM")));
                    }

                    // Thêm dòng
                    tableModel.addRow(new Object[] {
                            stt++,
                            "<html><b>" + tenPhong + "</b> - " + loaiPhong + "<br><i style='color:gray;font-size:10px'>" + timeRange + "</i></html>",
                            "Đêm",
                            soDem, // Hiển thị đúng số đêm tính từ tiền
                            MONEY_FORMAT.format(donGia),
                            MONEY_FORMAT.format(thanhTienPhong)
                    });

                    subTotalCalculated += thanhTienPhong;
                }
            }
        }

        // --- 3. Xử lý Chi tiết Dịch vụ ---
        if (dsChiTietDichVu != null) {
            for (ChiTietHoaDon_DichVu ctDV : dsChiTietDichVu) {
                DichVu dv = ctDV.getDichVu();
                if (dv != null) {
                    double thanhTienDV = ctDV.getThanhTien();
                    double donGia = ctDV.getDonGia(); // Lấy giá lúc đặt
                    int soLuong = ctDV.getSoLuong();

                    // Fallback: Nếu thành tiền = 0 (khuyến mãi hoặc lỗi), vẫn hiện
                    if (thanhTienDV == 0 && soLuong > 0 && donGia > 0) {
                        thanhTienDV = soLuong * donGia;
                    }

                    tableModel.addRow(new Object[] {
                            stt++,
                            dv.getTenDV(),
                            "Lần", // Hoặc lấy dv.getDonViTinh() nếu có
                            soLuong,
                            MONEY_FORMAT.format(donGia),
                            MONEY_FORMAT.format(thanhTienDV)
                    });
                    subTotalCalculated += thanhTienDV;
                }
            }
        }

        // --- 4. Tùy chỉnh giao diện bảng ---
        JTable table = new JTable(tableModel);
        table.setRowHeight(35); // Dòng cao hơn
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));

        // Header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));

        // Column Widths & Alignment
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setMaxWidth(40); // STT
        tcm.getColumn(1).setPreferredWidth(250); // Nội dung
        tcm.getColumn(2).setMaxWidth(50); // ĐVT
        tcm.getColumn(3).setMaxWidth(50); // SL
        tcm.getColumn(4).setPreferredWidth(100); // Đơn giá
        tcm.getColumn(5).setPreferredWidth(100); // Thành tiền

        // Renderers
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tcm.getColumn(0).setCellRenderer(centerRenderer);
        tcm.getColumn(2).setCellRenderer(centerRenderer);
        tcm.getColumn(3).setCellRenderer(centerRenderer);

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setBorder(new EmptyBorder(0,0,0,10)); // Padding phải
        tcm.getColumn(4).setCellRenderer(rightRenderer);
        tcm.getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        scrollPane.setPreferredSize(new Dimension(0, 250)); // Chiều cao cố định

        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 5. Phần Tổng tiền (Footer) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Tính toán lại các khoản
        double discountAmount = 0;
        KhuyenMai km = (hoaDon != null) ? hoaDon.getKhuyenMai() : null;
        if (km != null) {
            double chietKhau = km.getChietKhau() / 100.0;
            discountAmount = subTotalCalculated * chietKhau;
            if (discountAmount > 0) {
                bottomPanel.add(createTotalRow("Giảm giá (" + km.getTenKhuyenMai() + "):",
                        "- " + MONEY_FORMAT.format(discountAmount) + " đ", false));
            }
        }

        double vatRate = (hoaDon != null) ? hoaDon.getVat() / 100.0 : 0.1;
        double tienTruocThue = subTotalCalculated - discountAmount;
        double vatAmount = tienTruocThue * vatRate;
        double finalTotal = tienTruocThue + vatAmount;

        // Ưu tiên lấy tổng tiền từ DB nếu khớp (để tránh sai số làm tròn 1-2 đồng)
        if (hoaDon != null && Math.abs(hoaDon.getTongTien() - finalTotal) < 1000) {
            finalTotal = hoaDon.getTongTien();
        }

        bottomPanel.add(createTotalRow("Cộng tiền hàng:", MONEY_FORMAT.format(subTotalCalculated) + " đ", false));
        bottomPanel.add(createTotalRow("Thuế VAT (" + (int)(vatRate*100) + "%):", MONEY_FORMAT.format(vatAmount) + " đ", false));

        // Dòng kẻ ngang
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(sep);
        bottomPanel.add(Box.createVerticalStrut(5));

        // Tổng cộng to đậm
        bottomPanel.add(createTotalRow("TỔNG CỘNG:", MONEY_FORMAT.format(finalTotal) + " đ", true));

        // Chữ ký
        JPanel signaturePanel = new JPanel(new GridLayout(1, 2));
        signaturePanel.setOpaque(false);
        signaturePanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel lblThuNgan = new JLabel("<html><center><b>Thu ngân</b><br><i>(Ký, họ tên)</i></center></html>", SwingConstants.CENTER);
        JLabel lblKhach = new JLabel("<html><center><b>Khách hàng</b><br><i>(Ký, họ tên)</i></center></html>", SwingConstants.CENTER);

        signaturePanel.add(lblThuNgan);
        signaturePanel.add(lblKhach);
        bottomPanel.add(signaturePanel);

        detailsPanel.add(bottomPanel, BorderLayout.SOUTH);
        return detailsPanel;
    }

    /** Helper tạo dòng tổng tiền */
    private JPanel createTotalRow(String label, String value, boolean isBold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblName = new JLabel(label);
        JLabel lblVal = new JLabel(value);

        if (isBold) {
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblVal.setForeground(new Color(220, 53, 69)); // Màu đỏ cho tổng tiền
        } else {
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        lblVal.setPreferredSize(new Dimension(150, 20));
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblName, BorderLayout.CENTER);
        row.add(lblVal, BorderLayout.EAST);

        // Căn lề phải toàn bộ panel con
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrapper.setOpaque(false);
        wrapper.add(row);

        return row; // Add trực tiếp row vào BoxLayout sẽ giãn, add wrapper nếu muốn căn phải chặt
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
    }

    private void downloadPdf() {
        System.out.println("Placeholder: Xử lý tải PDF hóa đơn " + maHoaDon);
        JOptionPane.showMessageDialog(this, "Chức năng Tải PDF đang được phát triển.");
    }

} // Kết thúc lớp BillDialog