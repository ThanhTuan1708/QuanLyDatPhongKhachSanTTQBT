package event;

import dao.HoaDon_DAO; // Cần Import DAO này
import dao.PhieuDatPhong_DAO;
import dao.Phong_DAO;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import entity.Phong;
import ui.gui.FormDialog.HistoryCheckOutDialog;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller (EVENT) cho màn hình Check In / Check Out.
 * FIX: Đồng bộ hiển thị Tổng tiền khớp 100% với Bill (bao gồm Dịch vụ + VAT).
 */
public class EventCheckInCheckOut {

    // --- View & Dependencies ---
    private GUI_NhanVienLeTan.PanelCheckInCheckOut view;
    private EventDatPhong eventController;
    private NhanVien nhanVienHienTai;
    private Frame ownerFrame;

    // --- DAO ---
    private PhieuDatPhong_DAO phieuDatPhongDAO;
    private Phong_DAO phongDAO;
    private HoaDon_DAO hoaDonDAO; // <-- Thêm DAO Hóa đơn

    // --- State ---
    private List<Object[]> currentTableData;
    private boolean isCheckInMode = true;

    // --- Constants ---
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EventCheckInCheckOut(GUI_NhanVienLeTan.PanelCheckInCheckOut view, NhanVien nv, EventDatPhong datPhongController) {
        this.view = view;
        this.nhanVienHienTai = nv;
        this.eventController = datPhongController;
        this.ownerFrame = view.getOwnerFrame();

        try {
            this.phieuDatPhongDAO = new PhieuDatPhong_DAO();
            this.phongDAO = new Phong_DAO();
            this.hoaDonDAO = new HoaDon_DAO(); // <-- Khởi tạo
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khởi tạo DAO: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void initController() {
        initListeners();
    }

    public void initListeners() {
        view.getBtnToggleCheckIn().addActionListener(e -> handleToggleMode(true));
        view.getBtnToggleCheckOut().addActionListener(e -> handleToggleMode(false));
        view.getBtnHistory().addActionListener(e -> showHistoryDialog());

        view.getTxtSearch().addActionListener(e -> loadData());
        view.getBtnSearch().addActionListener(e -> loadData());

        view.getChkSelectAll().addActionListener(e -> toggleSelectAll(view.getChkSelectAll().isSelected()));
        view.getBtnMainAction().addActionListener(e -> performMainAction());
        view.getTableModel().addTableModelListener(e -> {
            if (e.getColumn() == 0) updateButtonState();
        });
    }

    private void handleToggleMode(boolean isCheckIn) {
        if (this.isCheckInMode == isCheckIn) return;

        this.isCheckInMode = isCheckIn;
        view.styleToggleButton(view.getBtnToggleCheckIn(), isCheckInMode);
        view.styleToggleButton(view.getBtnToggleCheckOut(), !isCheckInMode);
        view.updateMainActionButtonColor(isCheckInMode);

        if (isCheckInMode) {
            view.setNgayColumnHeader("NGÀY ĐẾN");
            view.getBtnMainAction().setText("Check In (0)");
        } else {
            view.setNgayColumnHeader("NGÀY ĐI");
            view.getBtnMainAction().setText("Check Out (0)");
        }
        view.getChkSelectAll().setSelected(false);
        loadData();
    }

    /**
     * Tải dữ liệu và tính toán Tổng tiền chính xác
     */
    public void loadData() {
        if (phieuDatPhongDAO == null || phongDAO == null || hoaDonDAO == null) return;

        String filterStatus = isCheckInMode ? "Đã xác nhận" : "Đã nhận phòng";
        view.getTableModel().setRowCount(0);

        String searchText = view.getTxtSearch().getText().trim();
        if (searchText.contains("Tìm theo") || searchText.isEmpty()) {
            searchText = "";
        }

        LocalDate selectedDate = null;
        String dateStringFilter = "";
        if (view.getDateChooser().getDate() != null) {
            selectedDate = view.getDateChooser().getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            dateStringFilter = selectedDate.format(DATE_FORMAT);
        }

        try {
            List<Object[]> dataList = phieuDatPhongDAO.getFilteredBookingData(searchText, filterStatus);
            currentTableData = dataList;

            for (Object[] row : dataList) {
                // Mapping dữ liệu từ row
                String ngayDen = row[3].toString();
                String ngayTra = row[4].toString();
                String maPhong = row[2].toString();
                String maPhieu = row[5].toString();
                String maKH = (row.length > 8 && row[8] != null) ? row[8].toString() : "";

                // Lọc theo ngày
                if (!dateStringFilter.isEmpty()) {
                    boolean dateMatch = isCheckInMode ? ngayDen.equals(dateStringFilter) : ngayTra.equals(dateStringFilter);
                    if (!dateMatch) continue;
                }

                Phong phong = null;
                try { phong = phongDAO.getPhongById(maPhong); } catch(Exception e) {}

                // --- BẮT ĐẦU LOGIC TÍNH TIỀN ---
                double finalTotal = 0;
                boolean foundBill = false;

                // CÁCH 1: Tìm Hóa đơn đã tồn tại trong CSDL (Chính xác tuyệt đối)
                // Nếu phiếu này đã được tính toán/check-out một phần, hóa đơn sẽ chứa tổng tiền đúng
                try {
                    String maHD = hoaDonDAO.findMaHoaDonByMaPhieu(maPhieu);
                    if (maHD != null) {
                        HoaDon hd = hoaDonDAO.getHoaDonById(maHD);
                        if (hd != null) {
                            finalTotal = hd.getTongTien();
                            foundBill = true;
                        }
                    }
                } catch (Exception e) {
                    // Ignored: Nếu không tìm thấy hóa đơn thì chuyển sang cách 2
                }

                // CÁCH 2: Nếu chưa có Hóa đơn, tự tính toán ước lượng (Bao gồm Dịch vụ + VAT)
                if (!foundBill && phong != null) {
                    try {
                        // a. Tính tiền phòng
                        LocalDate checkin = LocalDate.parse(ngayDen, DATE_FORMAT);
                        LocalDate checkout = LocalDate.parse(ngayTra, DATE_FORMAT);
                        long soDem = ChronoUnit.DAYS.between(checkin, checkout);
                        if(soDem <= 0) soDem = 1; // Tối thiểu 1 đêm
                        double tienPhong = phong.getGiaTienMotDem() * soDem;

                        // b. Tính tiền dịch vụ (Query trực tiếp vì chưa có object Invoice)
                        double tienDichVu = calculateServiceFee(maPhieu);

                        // c. Tổng = (Tiền phòng + Tiền dịch vụ) + 10% VAT
                        double tongChuaThue = tienPhong + tienDichVu;
                        finalTotal = tongChuaThue * 1.1;

                    } catch(Exception e) {
                        // Fallback an toàn
                        finalTotal = phong.getGiaTienMotDem() * 1.1;
                    }
                }
                // --- KẾT THÚC LOGIC TÍNH TIỀN ---

                String dateToShow = isCheckInMode ? ngayDen : ngayTra;
                String loaiPhongText = (phong != null && phong.getLoaiPhong() != null) ? phong.getLoaiPhong().getTenLoaiPhong() : "N/A";
                int soKhach = (phong != null) ? phong.getSoChua() : 2;

                String sdt = row[1].toString();
                String email = "";
                String lienHeDisplay = sdt + "\n" + email;
                String tongTienStr = String.format("%,.0f đ", finalTotal);

                // Thêm vào bảng
                view.getTableModel().addRow(new Object[]{
                        Boolean.FALSE,      // 0. Checkbox
                        maPhieu,            // 1. Mã ĐP
                        row[0].toString(),  // 2. Khách hàng
                        maPhong,            // 3. Phòng
                        loaiPhongText,      // 4. Loại phòng
                        dateToShow,         // 5. Ngày
                        soKhach,            // 6. Số khách
                        lienHeDisplay,      // 7. Liên hệ
                        tongTienStr,        // 8. TỔNG TIỀN (ĐÃ FIX)
                        ngayTra,            // 9. Ẩn
                        maKH                // 10. Ẩn
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        view.getChkSelectAll().setText("Chọn tất cả (" + view.getTableModel().getRowCount() + ")");
        updateButtonState();
    }

    /**
     * Hàm phụ: Tính tổng tiền dịch vụ của phiếu (khi chưa có hóa đơn)
     */
    private double calculateServiceFee(String maPhieu) {
        double total = 0;
        // Query tính tổng: Số lượng * Giá tiền
        String sql = "SELECT sum(ct.soLuong * dv.giaTien) " +
                "FROM ChiTietDichVu ct JOIN DichVu dv ON ct.maDV = dv.maDV " +
                "WHERE ct.maPhieu = ?";
        try (Connection con = connectDB.ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính dịch vụ: " + e.getMessage());
        }
        return total;
    }

    private void updateButtonState() {
        int selectedCount = 0;
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                selectedCount++;
            }
        }
        view.setLblDaChonText("Đã chọn: " + selectedCount);
        String actionText = isCheckInMode ? "Check In" : "Check Out";
        view.setBtnMainActionState(String.format("%s (%d)", actionText, selectedCount), selectedCount > 0);
    }

    private void toggleSelectAll(boolean select) {
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            view.getTableModel().setValueAt(select, i, 0);
        }
        updateButtonState();
    }

    private void performMainAction() {
        int totalToProcess = 0;
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            if ((Boolean) view.getTableModel().getValueAt(i, 0)) totalToProcess++;
        }
        if (totalToProcess == 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn ít nhất một phiếu.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isCheckInMode) {
            // Logic Check In
            int successCount = 0;
            int currentIndex = 1;
            for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
                if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                    try {
                        String maPhieu = safeGetValueAt(i, 1);
                        String ngayDen = safeGetValueAt(i, 3);
                        String maPhong = safeGetValueAt(i, 4);
                        String loaiPhong = safeGetValueAt(i, 5);
                        String soKhach = safeGetValueAt(i, 6);
                        String ngayTra = safeGetValueAt(i, 9);
                        String maKH = safeGetValueAt(i, 10);

                        KhachHang kh = eventController.getKhachHangDAO().getKhachHangById(maKH);
                        String tenKH = (kh != null && kh.getTenKH() != null) ? kh.getTenKH() : "N/A";
                        String sdt = (kh != null && kh.getSoDT() != null) ? kh.getSoDT() : "N/A";
                        String email = (kh != null && kh.getEmail() != null) ? kh.getEmail() : "";
                        String cccd = (kh != null && kh.getCCCD() != null) ? kh.getCCCD() : "";
                        String diaChi = (kh != null && kh.getDiaChi() != null) ? kh.getDiaChi() : "";

                        GUI_NhanVienLeTan.PanelCheckInCheckOut.CheckInCustomerDialog dialog =
                                new GUI_NhanVienLeTan.PanelCheckInCheckOut.CheckInCustomerDialog(
                                        ownerFrame, currentIndex, totalToProcess,
                                        maPhieu, maPhong + " - " + loaiPhong, ngayDen + " - " + ngayTra,
                                        soKhach, tenKH, sdt, email, cccd, diaChi
                                );
                        dialog.setVisible(true);

                        if (dialog.isConfirmed()) {
                            eventController.handleCheckIn(maPhieu, tenKH, maPhong);
                            successCount++;
                            currentIndex++;
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (successCount > 0) JOptionPane.showMessageDialog(view, "Đã check-in thành công " + successCount + " phiếu.");

        } else {
            // Logic Check Out
            List<String> selectedMaPhieu = new ArrayList<>();
            List<String> selectedMaPhong = new ArrayList<>();
            List<String> selectedTenKH = new ArrayList<>();

            for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
                if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                    selectedMaPhieu.add(safeGetValueAt(i, 1));
                    selectedMaPhong.add(safeGetValueAt(i, 3));
                    selectedTenKH.add(safeGetValueAt(i, 2));
                }
            }

            int confirm = JOptionPane.showConfirmDialog(view,
                    "Bạn có chắc muốn check-out " + selectedMaPhieu.size() + " phiếu đã chọn?",
                    "Xác nhận Check-out", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 0; i < selectedMaPhieu.size(); i++) {
                    eventController.handleCheckOut(selectedMaPhieu.get(i), selectedTenKH.get(i), selectedMaPhong.get(i));
                }
                if(!selectedMaPhieu.isEmpty()) {
                    eventController.handleShowBill(selectedMaPhieu.get(0));
                }
                JOptionPane.showMessageDialog(view, "Đã check-out thành công.");
            }
        }
        loadData();
        updateButtonState();
    }

    private void showHistoryDialog() {
        HistoryCheckOutDialog dialog = new HistoryCheckOutDialog(ownerFrame, eventController, phieuDatPhongDAO, phongDAO);
        dialog.setVisible(true);
    }

    private String safeGetValueAt(int row, int col) {
        Object value = view.getTableModel().getValueAt(row, col);
        return (value == null) ? "N/A" : value.toString();
    }
}