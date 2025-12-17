package event;

import connectDB.ConnectDB;
import ui.gui.GUI_NhanVienLeTan.PanelDatPhongContent;
import ui.gui.FormDialog.BillDialog;
import ui.gui.FormDialog.BookingFormDialog;
import ui.gui.GUI_NhanVienLeTan;
import entity.*;
import dao.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID; // <-- THÊM IMPORT NÀY

/**
 * Lớp Controller xử lý sự kiện cho PanelDatPhongContent.
 * (ĐÃ SỬA LỖI TẠO HÓA ĐƠN VÀ THÊM TÍNH NĂNG MỚI)
 */
public class EventDatPhong {

    // --- HẰNG SỐ TRẠNG THÁI ---
    private static final int TT_PHONG_SAN_SANG = 0;
    private static final int TT_PHONG_DA_THUE = 1;
    private static final int TT_PHONG_DANG_DON = 2;
    private static final int TT_PHONG_BAO_TRI = 3;

    // --- HẰNG SỐ MÀU SẮC ---
    private static final Color ACCENT_BLUE = new Color(24, 90, 219);
    private static final Color COLOR_SELECTED = new Color(138, 43, 226);
    private static final Color COLOR_DISABLED_BG = new Color(220, 220, 220);
    private static final Color COLOR_DISABLED_FG = new Color(150, 150, 150);
    private static final Color COLOR_DISABLED_BORDER = new Color(180, 180, 180);

    // Tham chiếu đến View và các DAO
    private PanelDatPhongContent view;
    private PhieuDatPhong_DAO phieuDatPhongDAO;
    private Phong_DAO phongDAO;
    private KhachHang_DAO khachHangDAO;
    private HoaDon_DAO hoaDonDAO;
    private DichVu_DAO dichVuDAO;
    private KhuyenMai_DAO khuyenMaiDAO;
    private ChiTietHoaDon_DAO chiTietHoaDonDAO;
    private TrangThaiPhong_DAO trangThaiPhongDAO;

    private NhanVien nhanVienHienTai;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EventDatPhong(PanelDatPhongContent view, NhanVien nhanVienHienTai) {
        this.view = view;
        this.nhanVienHienTai = nhanVienHienTai;

        try {
            this.phieuDatPhongDAO = new PhieuDatPhong_DAO();
            this.phongDAO = new Phong_DAO();
            this.khachHangDAO = new KhachHang_DAO();
            this.hoaDonDAO = new HoaDon_DAO();
            this.dichVuDAO = new DichVu_DAO();
            this.khuyenMaiDAO = new KhuyenMai_DAO();
            this.chiTietHoaDonDAO = new ChiTietHoaDon_DAO();
            this.trangThaiPhongDAO = new TrangThaiPhong_DAO();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khởi tạo DAO: " + e.getMessage(), "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Gắn listener tĩnh
    public void initListeners() {
        if (view.getSearchField() != null) {
            view.getSearchField().getDocument().addDocumentListener(new DocumentListener() {
                private void runFilter() { filterBookings(); }
                @Override public void insertUpdate(DocumentEvent e) { runFilter(); }
                @Override public void removeUpdate(DocumentEvent e) { runFilter(); }
                @Override public void changedUpdate(DocumentEvent e) { runFilter(); }
            });
        }
        if (view.getBookingFilterComboBox() != null) {
            view.getBookingFilterComboBox().addActionListener(e -> filterBookings());
        }
        if (view.getTypeGroup() != null) {
            for (AbstractButton button : Collections.list(view.getTypeGroup().getElements())) {
                if (button instanceof JToggleButton) {
                    button.addActionListener(e -> filterRooms());
                }
            }
        }
        if (view.getPeopleGroup() != null) {
            for (AbstractButton button : Collections.list(view.getPeopleGroup().getElements())) {
                if (button instanceof JToggleButton) {
                    button.addActionListener(e -> filterRooms());
                }
            }
        }

        if (view.getBtnBookLater() != null) {
            view.getBtnBookLater().addActionListener(e -> {
                handleProceedToBooking(false);
            });
        }
        if (view.getBtnBookAndCheckin() != null) {
            view.getBtnBookAndCheckin().addActionListener(e -> {
                handleProceedToBooking(true);
            });
        }
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN NÚT BẤM ---

    public void handleCheckIn(String bookingId, String customerName, String roomNumber) {
        System.out.println("Sự kiện Check In cho mã: " + bookingId);
        boolean checkinSuccess = false;
        try {
            TrangThaiPhongEntity ttDaThue = trangThaiPhongDAO.getTrangThaiPhongByTen("Đã thuê");
            if (ttDaThue == null) {
                JOptionPane.showMessageDialog(view, "Lỗi: Không tìm thấy trạng thái 'Đã thuê' trong CSDL.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maTrangThaiDaThue = ttDaThue.getMaTrangThai();

            checkinSuccess = phieuDatPhongDAO.checkIn(bookingId, maTrangThaiDaThue);
            System.out.println("Đã gọi DAO Check-in cho " + bookingId + ", kết quả: " + checkinSuccess);

            if (checkinSuccess) {
                System.out.println("Check-in thành công, tải lại danh sách...");
                filterBookings();
                filterRooms();
            } else {
                JOptionPane.showMessageDialog(view, "Check-in không thành công cho mã " + bookingId + ".", "Thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi CSDL khi thực hiện Check-in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi không xác định khi Check-in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void handleCheckOut(String bookingId, String customerName, String roomNumber) {
        System.out.println("Sự kiện Check Out cho mã: " + bookingId);
        boolean checkoutSuccess = false;
        try {
            TrangThaiPhongEntity ttDangDon = trangThaiPhongDAO.getTrangThaiPhongByTen("Đang dọn");
            if (ttDangDon == null) {
                JOptionPane.showMessageDialog(view, "Lỗi: Không tìm thấy trạng thái 'Đang dọn' trong CSDL.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maTrangThaiDangDon = ttDangDon.getMaTrangThai();

            checkoutSuccess = phieuDatPhongDAO.checkOut(bookingId, maTrangThaiDangDon);
            System.out.println("Đã gọi DAO Check-out cho " + bookingId + ", kết quả: " + checkoutSuccess);

            if (checkoutSuccess) {
                System.out.println("Check-out thành công, tải lại danh sách và hiển thị hóa đơn...");
                filterBookings();
                filterRooms();
                handleShowBill(bookingId);

            } else {
                JOptionPane.showMessageDialog(view, "Check-out không thành công cho mã " + bookingId + ". Có thể phiếu không tồn tại.", "Thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi CSDL khi thực hiện Check-out: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi không xác định khi Check-out: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void handleShowBill(String bookingId) {
        System.out.println("Sự kiện Xem hóa đơn cho bookingId: " + bookingId);
        String maHoaDonCanTim = null;
        try {
            maHoaDonCanTim = hoaDonDAO.findMaHoaDonByMaPhieu(bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        if (maHoaDonCanTim != null) {
            System.out.println("Tìm thấy mã hóa đơn: " + maHoaDonCanTim);
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
            if (owner != null) {
                BillDialog billDialog = new BillDialog(owner, maHoaDonCanTim);
                billDialog.setVisible(true);
            } else { JOptionPane.showMessageDialog(null, "Không thể hiển thị hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        } else {
            JOptionPane.showMessageDialog(view, "Không thể tải dữ liệu cho hóa đơn của phiếu: " + bookingId, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleMoreOptions(String bookingId) {
        // (Giữ nguyên)
    }

    public void handleEditBooking(String bookingId) { /*(Giữ nguyên)*/ }
    public void handleViewBooking(String bookingId) { /*(Giữ nguyên)*/ }
    public void handleDeleteBooking(String bookingId) { /*(Giữ nguyên)*/ }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN CHỌN/LỌC PHÒNG ---

    public void handleRoomSelectionToggle(String roomId, JButton button) {
        System.out.println("Toggling room: " + roomId);
        Set<String> selectedIds = view.getSelectedRoomIds();
        boolean currentlySelected = selectedIds.contains(roomId);
        boolean isAvailable = true;

        if (!isAvailable) {
            JOptionPane.showMessageDialog(view, "Phòng " + roomId + " không còn sẵn sàng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentlySelected) {
            selectedIds.remove(roomId);
        } else {
            selectedIds.add(roomId);
        }
        updateRoomButtonAppearance(button, !currentlySelected, isAvailable);
        view.updateContinueButton();
        Component card = button.getParent().getParent();
        if(card instanceof JPanel){
            updateRoomCardAppearance((JPanel)card, !currentlySelected);
        }
        System.out.println("Selected rooms: " + selectedIds);
    }

    private void updateRoomButtonAppearance(JButton button, boolean isSelected, boolean isAvailable) {
        // (Giữ nguyên)
        Border paddingBorder = new EmptyBorder(5, 15, 5, 15);
        Border lineBorder;
        if (!isAvailable) {
            button.setEnabled(false); button.setText("Chọn");
            button.setBackground(COLOR_DISABLED_BG); button.setForeground(COLOR_DISABLED_FG);
            lineBorder = new LineBorder(COLOR_DISABLED_BORDER, 1);
        } else if (isSelected) {
            button.setEnabled(true); button.setText("Bỏ chọn");
            button.setBackground(Color.RED);
            button.setForeground(Color.black);
            lineBorder = new LineBorder(Color.RED.darker(), 1);
        } else {
            button.setEnabled(true); button.setText("Chọn");
            button.setBackground(ACCENT_BLUE);
            button.setForeground(Color.black);
            lineBorder = new LineBorder(ACCENT_BLUE.darker(), 1);
        }
        button.setBorder(new CompoundBorder(lineBorder, paddingBorder));
        button.setOpaque(true); button.setContentAreaFilled(true); button.repaint();
    }

    private void updateRoomCardAppearance(JPanel card, boolean isSelected){
        // (Giữ nguyên)
        if (isSelected) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_SELECTED, 2),
                    new EmptyBorder(10, 10, 10, 10)));
        } else {
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GUI_NhanVienLeTan.CARD_BORDER),
                    new EmptyBorder(10, 10, 10, 10)));
        }
        card.revalidate();
        card.repaint();
    }

    public void handleProceedToBooking(boolean isCheckinNow) {
        Set<String> selectedIds = view.getSelectedRoomIds();
        System.out.println("Tiếp tục đặt phòng với các phòng: " + selectedIds);
        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Bạn chưa chọn phòng nào.", "Chưa chọn phòng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        if (owner != null) {
            List<Map<String, Object>> selectedRoomDetails = fetchRoomDetails(selectedIds);
            if (selectedRoomDetails == null || selectedRoomDetails.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Lỗi: Không thể lấy chi tiết các phòng đã chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<DichVu> dsDichVu = null;
            try {
                if (dichVuDAO == null) {
                    throw new SQLException("Lỗi: DichVu_DAO chưa được khởi tạo.");
                }
                dsDichVu = dichVuDAO.getAllDichVu();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách dịch vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                dsDichVu = new ArrayList<>();
            }

            BookingFormDialog bookingDialog = new BookingFormDialog(owner, selectedRoomDetails, dsDichVu, this, isCheckinNow);
            bookingDialog.setVisible(true);
        } else { System.err.println("Không tìm thấy Frame cha!"); }
    }

    private List<Map<String, Object>> fetchRoomDetails(Set<String> roomIds) {
        // (Giữ nguyên)
        System.out.println("--- Lấy chi tiết phòng TỪ CSDL cho mã: " + roomIds + " ---");
        List<Map<String, Object>> details = new ArrayList<>();
        if (roomIds == null || roomIds.isEmpty()) return details;
        try {
            for (String roomId : roomIds) {
                Phong phong = phongDAO.getPhongById(roomId);
                if (phong != null) {
                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("maPhong", phong.getMaPhong());
                    roomInfo.put("tenLoaiPhong", (phong.getLoaiPhong() != null) ? phong.getLoaiPhong().getTenLoaiPhong() : "N/A");
                    roomInfo.put("giaTien", phong.getGiaTienMotDem());
                    roomInfo.put("soChua", phong.getSoChua());
                    details.add(roomInfo);
                } else {
                    System.err.println("Warning: Could not find details for room ID: " + roomId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lấy chi tiết phòng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return details;
    }


    /**
     * Xử lý nghiệp vụ XÁC NHẬN ĐẶT PHÒNG.
     * (ĐÃ SỬA LỖI GIAO DỊCH (TRANSACTION) VÀ LỖI TRÙNG KHÓA CHÍNH)
     */
    public void handleConfirmBooking(Map<String, Object> bookingInfo) {
        System.out.println("Xác nhận đặt phòng với thông tin:");
        System.out.println(bookingInfo);

        boolean success = false;
        Connection con = null;
        KhachHang kh = null;
        HoaDon hd = null;
        List<PhieuDatPhong> danhSachPDPDaTao = new ArrayList<>();

        try {
            // *** BẮT ĐẦU TRANSACTION TỔNG ***
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu giao dịch

            // BƯỚC 1: LẤY/TẠO KHÁCH HÀNG
            String sdt = bookingInfo.get("sdt").toString();
            String tenKH = bookingInfo.get("tenKH").toString();
            String email = bookingInfo.get("email").toString();
            boolean isCheckinNow = (boolean) bookingInfo.get("isCheckinNow");

            kh = khachHangDAO.findKhachHangBySdt(sdt);

            if (kh == null) {
                System.out.println("Khách hàng mới. Đang tạo...");
                String maKH = "KH" + (System.nanoTime() % 100000);
                kh = new KhachHang();
                kh.setMaKH(maKH);
                kh.setTenKH(tenKH);
                kh.setSoDT(sdt);
                kh.setEmail(email);
                kh.setGioiTinh(GioiTinh.KHAC);

                if (!khachHangDAO.addKhachHang(con, kh)) {
                    throw new Exception("Lỗi: Không thể thêm khách hàng mới vào CSDL.");
                }
                System.out.println("Đã tạo khách hàng mới: " + maKH);
            } else {
                System.out.println("Khách hàng đã tồn tại: " + kh.getMaKH());
            }

            // BƯỚC 2: CHUẨN BỊ DỮ LIỆU
            LocalDateTime ngayDat = LocalDateTime.now();
            LocalDateTime ngayNhan = LocalDate.parse(bookingInfo.get("ngayNhan").toString(), dateFormatter).atTime(14, 0);
            LocalDateTime ngayTra = LocalDate.parse(bookingInfo.get("ngayTra").toString(), dateFormatter).atTime(12, 0);
            long soDem = ChronoUnit.DAYS.between(ngayNhan.toLocalDate(), ngayTra.toLocalDate());
            if (soDem <= 0) soDem = 1;

            NhanVien nv = this.nhanVienHienTai;
            if (nv == null) { throw new Exception("Lỗi: Không tìm thấy thông tin nhân viên đăng nhập."); }

            @SuppressWarnings("unchecked")
            List<String> phongIds = (List<String>) bookingInfo.get("phongIds");
            @SuppressWarnings("unchecked")
            List<String> dichVuIds = (List<String>) bookingInfo.get("dichVuIds");
            if (phongIds == null || phongIds.isEmpty()) { throw new Exception("Lỗi: Chưa chọn phòng nào."); }

            // BƯỚC 3: TẠO HÓA ĐƠN VÀ CHI TIẾT

            // SỬA LỖI TRÙNG KEY: Dùng UUID
            String maHD = "HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            hd = new HoaDon(maHD); hd.setKhachHang(kh); hd.setNhanVien(nv); hd.setNgayLap(ngayDat); hd.setHinhThucThanhToan("Chưa thanh toán"); hd.setVat(10.0);
            List<ChiTietHoaDon_Phong> dsCTPhong = new ArrayList<>();
            List<ChiTietHoaDon_DichVu> dsCTDichVu = new ArrayList<>();
            double tongTienHang = 0;

            int maTrangThaiPhongMoi;
            if(isCheckinNow) {
                TrangThaiPhongEntity ttDaThue = trangThaiPhongDAO.getTrangThaiPhongByTen("Đã thuê");
                maTrangThaiPhongMoi = (ttDaThue != null) ? ttDaThue.getMaTrangThai() : TT_PHONG_DA_THUE;
            } else {
                // Nếu chỉ đặt trước, phòng vẫn "Sẵn sàng"
                maTrangThaiPhongMoi = TT_PHONG_SAN_SANG;
            }

            // BƯỚC 4: LẶP QUA PHÒNG VÀ DỊCH VỤ (CHUẨN BỊ LƯU)
            for (String maPhong : phongIds) {
                Phong phong = phongDAO.getPhongById(maPhong);
                if (phong == null) throw new Exception("Lỗi: Không tìm thấy phòng " + maPhong);
                if (phong.getTrangThaiPhong().getMaTrangThai() != TT_PHONG_SAN_SANG) { throw new Exception("Phòng " + maPhong + " không còn sẵn sàng!"); }

                // SỬA LỖI TRÙNG KEY: Dùng UUID
                String maPhieu = "PDP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                // SỬA: Bỏ 'nv' khỏi constructor (vì CSDL không có cột 'maNV')
                PhieuDatPhong pdp = new PhieuDatPhong(maPhieu, ngayDat, ngayNhan, ngayTra, kh, phong);

                danhSachPDPDaTao.add(pdp);

                double donGiaLucDat = phong.getGiaTienMotDem();
                double thanhTienPhong = donGiaLucDat * soDem;
                tongTienHang += thanhTienPhong;
                ChiTietHoaDon_Phong ctp = new ChiTietHoaDon_Phong(hd, pdp, donGiaLucDat, thanhTienPhong);
                dsCTPhong.add(ctp);
            }

            if (dichVuIds != null && !dichVuIds.isEmpty()) {
                for (String maDV : dichVuIds) {
                    DichVu dv = dichVuDAO.getDichVuById(maDV);
                    if (dv != null) {
                        double thanhTienDV = dv.getGiaTien() * 1;
                        ChiTietHoaDon_DichVu ctdv = new ChiTietHoaDon_DichVu(hd, dv, 1, dv.getGiaTien(), thanhTienDV);
                        dsCTDichVu.add(ctdv);
                        tongTienHang += thanhTienDV;
                    } else { System.err.println("Warning: Không tìm thấy dịch vụ mã " + maDV); }
                }
            }
            hd.setDsChiTietPhong(dsCTPhong);
            hd.setDsChiTietDichVu(dsCTDichVu);

            // BƯỚC 5: ÁP DỤNG KHUYẾN MÃI (Giữ nguyên)
            KhuyenMai appliedKm = null;
            try {
                Object maKmObj = bookingInfo.get("maKhuyenMai");
                if (maKmObj != null) {
                    String maKm = maKmObj.toString().trim();
                    if (!maKm.isEmpty() && khuyenMaiDAO != null) {
                        KhuyenMai km = khuyenMaiDAO.getKhuyenMaiById(maKm);
                        if (km != null) {
                            boolean kmValid = true;
                            java.sql.Date nb = km.getNgayBatDau();
                            java.sql.Date nk = km.getNgayKetThuc();
                            java.time.LocalDate today = java.time.LocalDate.now();
                            if (nb != null && today.isBefore(nb.toLocalDate())) kmValid = false;
                            if (nk != null && today.isAfter(nk.toLocalDate())) kmValid = false;
                            if (km.getLuotSuDung() <= 0) kmValid = false;
                            if (kmValid) {
                                double chietKhau = km.getChietKhau();
                                if (chietKhau > 0) {
                                    tongTienHang = tongTienHang * (1 - (chietKhau / 100.0));
                                }
                                appliedKm = km;
                                hd.setKhuyenMai(km);
                            }
                        }
                    }
                }
            } catch (Exception exKm) {
                exKm.printStackTrace();
            }

            double tongTienSauVAT = tongTienHang * (1 + (hd.getVat() / 100.0));
            hd.setTongTien(tongTienSauVAT);

            // BƯỚC 6: THỰC HIỆN LƯU VÀO CSDL (TRONG CÙNG 1 GIAO DỊCH)

            // 1. Lưu Hóa đơn (CHA)
            hoaDonDAO.addHoaDon(con, hd);

            // 2. Lưu Phiếu Đặt Phòng
            for (PhieuDatPhong pdp : danhSachPDPDaTao) {
                phieuDatPhongDAO.addPhieuDatPhong(pdp, con);
            }

            // 3. Lưu Chi Tiết Phòng
            for (ChiTietHoaDon_Phong ctp : hd.getDsChiTietPhong()) {
                chiTietHoaDonDAO.addChiTietPhong(con, ctp);
            }

            // 4. Lưu Chi Tiết Dịch Vụ
            if (hd.getDsChiTietDichVu() != null) {
                for (ChiTietHoaDon_DichVu ctdv : hd.getDsChiTietDichVu()) {
                    chiTietHoaDonDAO.addChiTietDichVu(con, ctdv);
                }
            }

            // 5. Cập nhật trạng thái phòng (NẾU CHECK-IN NGAY)
            if (isCheckinNow) {
                for (String maPhong : phongIds) {
                    phongDAO.updatePhongTrangThai(maPhong, maTrangThaiPhongMoi, con);
                }
            }

            // 6. Cập nhật Khuyến mãi
            if (appliedKm != null) {
                appliedKm.setLuotSuDung(appliedKm.getLuotSuDung() - 1);
                khuyenMaiDAO.updateKhuyenMai(con, appliedKm);
            }

            // *** KẾT THÚC GIAO DỊCH ***
            con.commit();
            success = true;
            System.out.println("DEBUG: Commit giao dịch TỔNG thành công.");

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            try {
                if(con != null) {
                    System.err.println("DEBUG: Giao dịch thất bại. Đang Rollback...");
                    con.rollback(); // <-- HOÀN TÁC LỖI
                }
            } catch(SQLException eRollback) {
                eRollback.printStackTrace();
            }
            JOptionPane.showMessageDialog(view,
                    "Lỗi khi xác nhận đặt phòng:\n" + e.getMessage(),
                    "Lỗi nghiêm trọng",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        finally {
            try {
                if(con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch(SQLException eReset) {
                eReset.printStackTrace();
            }
        }

        // BƯỚC 7: HOÀN TẤT & CẬP NHẬT UI
        if (success) {
            // 1. Thông báo thành công
            StringBuilder successMessage = new StringBuilder();
            successMessage.append("<html><h2>Đặt phòng thành công!</h2>");
            successMessage.append("<p><b>Mã Hóa đơn:</b> ").append(hd.getMaHoaDon()).append("</p>");
            successMessage.append("<p><b>Khách hàng:</b> ").append(kh.getTenKH()).append("</p>");
            JOptionPane.showMessageDialog(
                    view,
                    successMessage.toString(),
                    "Đặt phòng thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 2. Cập nhật UI
            view.getSelectedRoomIds().clear();
            view.updateContinueButton();
            filterRooms();
            filterBookings();

            // highlight-start
            // *** TÍNH NĂNG MỚI: TỰ ĐỘNG HIỂN THỊ HÓA ĐƠN ***
            System.out.println("Tự động hiển thị hóa đơn: " + hd.getMaHoaDon());
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
            if (owner != null) {
                BillDialog billDialog = new BillDialog(owner, hd.getMaHoaDon());
                billDialog.setVisible(true);
            }
            // highlight-end
        }
    }

    // Getter cho KhachHang_DAO (cần cho EventCheckInCheckOut)
    public KhachHang_DAO getKhachHangDAO() {
        return this.khachHangDAO;
    }

    // --- CÁC HÀM LỌC ---
    public void filterBookings() {
        String searchTextRaw = view.getSearchField().getText();
        String selectedFilter = view.getBookingFilterComboBox().getSelectedItem().toString();
        String placeholder = " Tìm kiếm...";
        String searchText = (searchTextRaw == null || searchTextRaw.trim().isEmpty() || searchTextRaw.equals(placeholder)) ? "" : searchTextRaw.trim().toLowerCase();
        System.out.println("Lọc đặt phòng - Tìm: '" + searchText + "' | Trạng thái: '" + selectedFilter + "'");

        try {
            List<Object[]> dataList = phieuDatPhongDAO.getFilteredBookingData(searchText, selectedFilter);
            Object[][] filteredData = dataList.toArray(new Object[0][]);
            System.out.println("Số bản ghi sau khi lọc: " + filteredData.length);
            view.populateBookingCards(filteredData);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lọc danh sách đặt phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void filterRooms() {
        // (Hàm này giữ nguyên, không cần thay đổi)
        // 1. Lấy trạng thái của các nút lọc
        String selectedTypeText = "Tất cả";
        String selectedPeopleText = "Tất cả";
        String selectedFloorText = "Tất cả tầng";
        String selectedStatusText = "Tất cả trạng thái";
        ButtonGroup typeGroup = view.getTypeGroup();
        ButtonGroup peopleGroup = view.getPeopleGroup();
        ButtonGroup floorGroup = view.getFloorGroup();
        ButtonGroup statusGroup = view.getStatusGroup();
        if (typeGroup != null) {
            for (AbstractButton button : Collections.list(typeGroup.getElements())) {
                if (button instanceof JToggleButton) {
                    JToggleButton tb = (JToggleButton) button;
                    if (tb.isSelected()) {
                        selectedTypeText = tb.getText();
                        view.styleActiveTypeButton(tb);
                    } else {
                        view.resetButtonStyle(tb);
                    }
                }
            }
        } else { System.err.println("Lỗi: typeGroup null!"); }
        if (peopleGroup != null) {
            for (AbstractButton button : Collections.list(peopleGroup.getElements())) {
                if (button instanceof JToggleButton) {
                    JToggleButton tb = (JToggleButton) button;
                    if (tb.isSelected()) {
                        selectedPeopleText = tb.getText();
                        view.styleActivePeopleButton(tb);
                    } else {
                        view.resetButtonStyle(tb);
                    }
                }
            }
        } else { System.err.println("Lỗi: peopleGroup null!"); }
        if (floorGroup != null) {
            for (AbstractButton button : Collections.list(floorGroup.getElements())) {
                if (button instanceof JToggleButton) {
                    JToggleButton tb = (JToggleButton) button;
                    if (tb.isSelected()) {
                        selectedFloorText = tb.getText();
                        view.styleActiveFloorButton(tb);
                    } else {
                        view.resetButtonStyle(tb);
                    }
                }
            }
        } else { System.err.println("Lỗi: floorGroup null!"); }
        if (statusGroup != null) {
            for (AbstractButton button : Collections.list(statusGroup.getElements())) {
                if (button instanceof JToggleButton) {
                    JToggleButton tb = (JToggleButton) button;
                    if (tb.isSelected()) {
                        selectedStatusText = tb.getText();
                        view.styleActiveStatusButton(tb);
                    } else {
                        view.resetButtonStyle(tb);
                    }
                }
            }
        } else { System.err.println("Lỗi: statusGroup null!"); }

        // 2. Xử lý ngày và kiểm tra tính hợp lệ
        String fromDateStr = view.getFromDate().getText().trim();
        String toDateStr = view.getToDate().getText().trim();
        java.util.Date tuNgay = null;
        java.util.Date denNgay = null;
        boolean hasDateFilter = !fromDateStr.equals("dd/MM/yyyy") && !toDateStr.equals("dd/MM/yyyy");
        if (hasDateFilter) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                tuNgay = sdf.parse(fromDateStr);
                denNgay = sdf.parse(toDateStr);
                if (tuNgay.after(denNgay)) {
                    JOptionPane.showMessageDialog(view, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(view, "Ngày không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 3. Chuyển đổi các giá trị filter
        String tenLoaiPhongFilter = selectedTypeText.equals("Tất cả") ? null : selectedTypeText;
        int soChuaFilter = -1;
        if (selectedPeopleText.equals("1 người")) {
            soChuaFilter = 1;
        } else if (selectedPeopleText.equals("2 người")) {
            soChuaFilter = 2;
        } else if (selectedPeopleText.equals("3 người")) {
            soChuaFilter = 3;
        } else if (selectedPeopleText.equals("4+ người")) {
            soChuaFilter = 4;
        }
        Integer floorFilter = null;
        if (selectedFloorText.startsWith("Tầng ")) {
            try {
                floorFilter = Integer.parseInt(selectedFloorText.substring(5));
            } catch (NumberFormatException e) {
                System.err.println("Lỗi parse số tầng: " + selectedFloorText);
            }
        }

        Integer statusFilter = null;
        if (!selectedStatusText.equals("Tất cả trạng thái")) {
            try {
                TrangThaiPhongEntity tt = trangThaiPhongDAO.getTrangThaiPhongByTen(selectedStatusText);
                if (tt != null) {
                    statusFilter = tt.getMaTrangThai();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 5. Gọi DAO và cập nhật UI
        try {
            List<Phong> filteredList = phongDAO.getFilteredPhong(
                    tenLoaiPhongFilter,
                    soChuaFilter,
                    floorFilter,
                    statusFilter,
                    hasDateFilter ? tuNgay : null,
                    hasDateFilter ? denNgay : null
            );
            System.out.println("Số phòng sau khi lọc: " + filteredList.size());
            view.populateRoomCards(filteredList);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view,
                    "Lỗi khi lọc danh sách phòng: " + e.getMessage(),
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // --- HÀM HELPER TẠO DIALOG XÁC NHẬN ---
    private JPanel createCheckInConfirmationPanel(String customerName, String roomNumber){
        JPanel checkinPanel = new JPanel(new BorderLayout(10, 10)); checkinPanel.setBorder(new EmptyBorder(10,10,10,10)); JLabel checkinIconLabel = new JLabel("→]", SwingConstants.CENTER); checkinIconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); checkinIconLabel.setForeground(Color.WHITE); checkinIconLabel.setOpaque(true); checkinIconLabel.setBackground(new Color(100, 220, 150)); checkinIconLabel.setPreferredSize(new Dimension(50, 50)); JPanel titlePanel = new JPanel(); titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS)); titlePanel.setOpaque(false); JLabel titleLabel = new JLabel("Xác nhận Check-in"); titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD)); JLabel subtitleLabel = new JLabel("Phòng " + roomNumber + " - " + customerName); subtitleLabel.setForeground(Color.GRAY); titlePanel.add(titleLabel); titlePanel.add(subtitleLabel); JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); headerPanel.setOpaque(false); headerPanel.add(checkinIconLabel); headerPanel.add(titlePanel); checkinPanel.add(headerPanel, BorderLayout.NORTH); checkinPanel.add(new JLabel("Bạn có chắc chắn muốn check-in không? Thao tác này sẽ cập nhật trạng thái đặt phòng."), BorderLayout.CENTER); checkinPanel.setOpaque(false); return checkinPanel;
    }
    private JPanel createCheckOutConfirmationPanel(String customerName, String roomNumber){
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        checkoutPanel.setBorder(new EmptyBorder(10,10,10,10));
        JLabel checkoutIconLabel = new JLabel("→]", SwingConstants.CENTER);
        checkoutIconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        checkoutIconLabel.setForeground(Color.WHITE); checkoutIconLabel.setOpaque(true);
        checkoutIconLabel.setBackground(new Color(255, 180, 100));
        checkoutIconLabel.setPreferredSize(new Dimension(50, 50));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Xác nhận Check-out");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        JLabel subtitleLabel = new JLabel("Phòng " + roomNumber + " - " + customerName);
        subtitleLabel.setForeground(Color.GRAY); titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); headerPanel.setOpaque(false); headerPanel.add(checkoutIconLabel); headerPanel.add(titlePanel); checkoutPanel.add(headerPanel, BorderLayout.NORTH); JPanel autoBillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); autoBillPanel.setBackground(new Color(220, 235, 255)); autoBillPanel.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 250))); JLabel autoBillLabel = new JLabel("📄 Hóa đơn sẽ tự động hiển thị và in sau khi check-out"); autoBillLabel.setForeground(new Color(24, 90, 219)); autoBillPanel.add(autoBillLabel); checkoutPanel.add(autoBillPanel, BorderLayout.CENTER); JLabel confirmLabel = new JLabel("Bạn có chắc chắn muốn check-out không? Thao tác này sẽ cập nhật trạng thái đặt phòng và in hóa đơn thanh toán."); checkoutPanel.add(confirmLabel, BorderLayout.SOUTH); checkoutPanel.setOpaque(false); return checkoutPanel;
    }

}
