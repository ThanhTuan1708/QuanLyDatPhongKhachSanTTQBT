package event; // Hoặc package tương ứng của bạn

import dao.PhieuDatPhong_DAO;
import dao.Phong_DAO;
import entity.KhachHang;
import entity.NhanVien;
import entity.Phong;
import ui.gui.FormDialog.HistoryCheckOutDialog;

// Import các lớp inner class từ GUI_NhanVienLeTan
import ui.gui.GUI_NhanVienLeTan;
import ui.gui.GUI_NhanVienLeTan.PanelCheckInCheckOut;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller (EVENT) cho màn hình Check In / Check Out.
 * (Đã khôi phục logic 2 chế độ)
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

    // --- State ---
    private List<Object[]> currentTableData;
    private boolean isCheckInMode = true; // <-- Khôi phục lại

    // --- Constants ---
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Constructor
     */
    public EventCheckInCheckOut(GUI_NhanVienLeTan.PanelCheckInCheckOut view, NhanVien nv,
            EventDatPhong datPhongController) {
        this.view = view;
        this.nhanVienHienTai = nv;
        this.eventController = datPhongController;
        this.ownerFrame = view.getOwnerFrame();

        try {
            this.phieuDatPhongDAO = new PhieuDatPhong_DAO();
            this.phongDAO = new Phong_DAO();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khởi tạo DAO trong CheckIn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Khởi tạo controller: Gắn listener
     */
    public void initController() {
        initListeners();
    }

    public void initListeners() {
        // Toggle chế độ (CheckIn <-> CheckOut)
        view.getBtnToggleCheckIn().addActionListener(e -> handleToggleMode(true));
        view.getBtnToggleCheckOut().addActionListener(e -> handleToggleMode(false));
        view.getBtnHistory().addActionListener(e -> showHistoryDialog());

        // --- SỰ KIỆN TÌM KIẾM TỔNG HỢP ---
        // 1. Nhấn Enter ở ô tìm kiếm -> Gọi hàm loadData()
        view.getTxtSearch().addActionListener(e -> loadData());

        // 2. Nhấn Nút Tìm -> Gọi hàm loadData()
        view.getBtnSearch().addActionListener(e -> loadData());

        // Lưu ý: Không gắn PropertyChangeListener cho dateChooser để tránh tự động
        // reload

        // Các sự kiện bảng
        view.getChkSelectAll().addActionListener(e -> toggleSelectAll(view.getChkSelectAll().isSelected()));
        view.getBtnMainAction().addActionListener(e -> performMainAction());
        view.getTableModel().addTableModelListener(e -> {
            if (e.getColumn() == 0)
                updateButtonState();
        });

        // highlight-start
        // Thêm sự kiện chọn hàng để cập nhật thông tin chi tiết lên Panel Header
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBookingInfoPanel();
            }
        });
        // highlight-end
    }

    private void updateBookingInfoPanel() {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow >= 0) {
            String maPhieu = safeGetValueAt(selectedRow, 1); // Cột 1: Mã ĐP
            String maPhong = safeGetValueAt(selectedRow, 3); // Cột 3: Phòng
            String tenKhach = safeGetValueAt(selectedRow, 2); // Cột 2: Tên Khách

            view.setBookingInfo(maPhieu, maPhong, tenKhach);
        } else {
            view.setBookingInfo("-", "-", "-");
        }
    }

    // (Trong file EventCheckInCheckOut.java)
    private void handleToggleMode(boolean isCheckIn) {
        isCheckInMode = isCheckIn;

        // Cập nhật giao diện nút dựa trên trạng thái mới
        view.styleToggleButton(view.getBtnToggleCheckIn(), isCheckInMode);
        view.styleToggleButton(view.getBtnToggleCheckOut(), !isCheckInMode);

        // ... các logic khác (đổi header bảng, load lại data...)
        view.updateMainActionButtonColor(isCheckInMode); // Cập nhật màu nút hành động chính bên phải
        if (isCheckInMode) {
            view.setNgayColumnHeader("NGÀY ĐẾN");
        } else {
            view.setNgayColumnHeader("NGÀY ĐI");
        }
        loadData();
    }


    /**
     * Tải dữ liệu lên bảng dựa trên chế độ và bộ lọc
     * - Check-in: Chỉ hiện "Đã xác nhận"
     * - Check-out: Chỉ hiện "Đã nhận phòng"
     */
    public void loadData() {
        if (phieuDatPhongDAO == null || phongDAO == null)
            return;

        // 1. Xác định trạng thái cần lọc dựa trên Tab đang chọn
        String filterStatus;
        if (isCheckInMode) {
            filterStatus = "Đã xác nhận"; // Chế độ Check-in chỉ hiện phiếu đã xác nhận
        } else {
            filterStatus = "Đã nhận phòng"; // Chế độ Check-out chỉ hiện phòng đang ở
        }

        // 2. Lấy thông tin tìm kiếm từ giao diện
        view.getTableModel().setRowCount(0);
        String searchText = view.getTxtSearch().getText().trim();
        if (searchText.contains("Tìm theo") || searchText.isEmpty()) {
            searchText = "";
        }

        // 3. Lấy ngày lọc (nếu có)
        LocalDate selectedDate = null;
        String dateStringFilter = "";
        if (view.getDateChooser().getDate() != null) {
            selectedDate = view.getDateChooser().getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            dateStringFilter = selectedDate.format(DATE_FORMAT);
        }

        try {
            // 4. Gọi DAO lấy dữ liệu (DAO cần hỗ trợ lọc theo trạng thái)
            List<Object[]> dataList = phieuDatPhongDAO.getFilteredBookingData(searchText, filterStatus);
            currentTableData = dataList;

            for (Object[] row : dataList) {
                // row[3]: Ngày đến, row[4]: Ngày đi
                String ngayDen = row[3].toString();
                String ngayTra = row[4].toString();

                // 5. Lọc theo ngày (Java Filter)
                if (!dateStringFilter.isEmpty()) {
                    boolean dateMatch = false;
                    if (isCheckInMode) {
                        // Nếu Check-in: So sánh ngày chọn với Ngày Đến
                        dateMatch = ngayDen.equals(dateStringFilter);
                    } else {
                        // Nếu Check-out: So sánh ngày chọn với Ngày Đi
                        dateMatch = ngayTra.equals(dateStringFilter);
                    }

                    if (!dateMatch)
                        continue; // Bỏ qua nếu không khớp ngày
                }

                // 6. Xử lý hiển thị
                String maPhong = row[2].toString();
                String maPhieu = row[5].toString();
                String maKH = row[8].toString();

                Phong phong = null;
                try {phong = phongDAO.getPhongById(maPhong);} catch (Exception e) {}

                // --- SỬA LOGIC TÍNH TIỀN: ƯU TIÊN HÓA ĐƠN ---
                double tongTienHienThi = 0;

                // Kiểm tra xem có dữ liệu tiền từ DAO không (Index 10)
                if (row.length > 10 && row[10] != null) {
                    try {
                        tongTienHienThi = Double.parseDouble(row[10].toString());
                    } catch (Exception e) { tongTienHienThi = 0; }
                }

                // Nếu chưa có hóa đơn (tiền = 0), mới tính tạm tính tiền phòng
                if (tongTienHienThi <= 0 && phong != null) {
                    try {
                        LocalDate checkin = LocalDate.parse(ngayDen, DATE_FORMAT);
                        LocalDate checkout = LocalDate.parse(ngayTra, DATE_FORMAT);
                        long soDem = ChronoUnit.DAYS.between(checkin, checkout);
                        if (soDem <= 0) soDem = 1;
                        tongTienHienThi = phong.getGiaTienMotDem() * soDem;
                    } catch (Exception e) {
                        tongTienHienThi = phong.getGiaTienMotDem();
                    }
                }

                String dateToShow = isCheckInMode ? ngayDen : ngayTra;
                String loaiPhongText = (phong != null && phong.getLoaiPhong() != null)
                        ? phong.getLoaiPhong().getTenLoaiPhong()
                        : "N/A";
                int soKhach = (phong != null) ? phong.getSoChua() : 2;

                // 1. Lấy Email (Index 9) - Đã an toàn vì DAO trả về đủ độ dài
                String sdt = row[1].toString();
                String email = (row[9] != null) ? row[9].toString() : "";
                String lienHeDisplay = sdt + "\n" + email;

                // Format tiền
                String tongTienStr = String.format("%,.0f đ", tongTienHienThi);

                // Thêm dòng vào bảng
                view.getTableModel().addRow(new Object[]{
                        Boolean.FALSE,
                        maPhieu,
                        row[0].toString(),
                        maPhong,
                        loaiPhongText,
                        dateToShow,
                        soKhach,
                        lienHeDisplay, // Đã có email
                        tongTienStr,   // Đã có tiền
                        ngayTra,
                        maKH
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Cập nhật text nút "Chọn tất cả"
        view.getChkSelectAll().setText("Chọn tất cả (" + view.getTableModel().getRowCount() + ")");
        updateButtonState();
    }

    /**
     * Cập nhật trạng thái nút bấm và label
     */
    private void updateButtonState() {
        int selectedCount = 0;
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                selectedCount++;
            }
        }

        view.setLblDaChonText("Đã chọn: " + selectedCount);

        // *** KHÔI PHỤC LOGIC: Dùng isCheckInMode ***
        String actionText = isCheckInMode ? "Check In" : "Check Out";

        if (selectedCount > 0) {
            view.setBtnMainActionState(String.format("%s (%d)", actionText, selectedCount), true);
        } else {
            view.setBtnMainActionState(String.format("%s (0)", actionText), false);
        }
    }

    /**
     * Chọn/Bỏ chọn tất cả
     */
    private void toggleSelectAll(boolean select) {
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            view.getTableModel().setValueAt(select, i, 0);
        }
        updateButtonState();
    }

    /**
     * Thực hiện hành động CheckIn hoặc CheckOut
     * (ĐÃ SỬA LỖI LOGIC LẤY DỮ LIỆU THẬT TỪ KHÁCH HÀNG)
     */
    private void performMainAction() {
        // 1. Đếm tổng số người được chọn
        int totalToProcess = 0;
        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                totalToProcess++;
            }
        }
        if (totalToProcess == 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn ít nhất một phiếu.", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isCheckInMode) {
            // === LOGIC MỚI: LẶP VÀ LẤY DỮ LIỆU THẬT ===
            int successCount = 0;
            int currentIndex = 1;

            for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
                if ((Boolean) view.getTableModel().getValueAt(i, 0)) { // Nếu hàng này được chọn

                    try {
                        // --- LẤY DỮ LIỆU TỪ BẢNG ---
                        String maPhieu = safeGetValueAt(i, 1);
                        String ngayDen = safeGetValueAt(i, 3);
                        String maPhong = safeGetValueAt(i, 4);
                        String loaiPhong = safeGetValueAt(i, 5);
                        String soKhach = safeGetValueAt(i, 6);
                        String ngayTra = safeGetValueAt(i, 9); // Cột ẩn

                        // highlight-start
                        // --- LẤY DỮ LIỆU THẬT TỪ DATABASE ---
                        String maKH = safeGetValueAt(i, 10); // Lấy maKH từ cột ẩn 10
                        KhachHang kh = eventController.getKhachHangDAO().getKhachHangById(maKH);

                        String tenKH = (kh != null && kh.getTenKH() != null) ? kh.getTenKH() : "N/A";
                        String sdt = (kh != null && kh.getSoDT() != null) ? kh.getSoDT() : "N/A";
                        String email = (kh != null && kh.getEmail() != null) ? kh.getEmail() : "";
                        String cccd = (kh != null && kh.getCCCD() != null) ? kh.getCCCD() : "";
                        String diaChi = (kh != null && kh.getDiaChi() != null) ? kh.getDiaChi() : "";
                        // highlight-end

                        String thoiGian = ngayDen + " - " + ngayTra;
                        String phongInfo = maPhong + " - " + loaiPhong;

                        // 3. TẠO DIALOG VỚI 11 THAM SỐ
                        GUI_NhanVienLeTan.PanelCheckInCheckOut.CheckInCustomerDialog dialog = new GUI_NhanVienLeTan.PanelCheckInCheckOut.CheckInCustomerDialog(
                                ownerFrame, currentIndex, totalToProcess,
                                maPhieu, phongInfo, thoiGian,
                                soKhach, tenKH, sdt,
                                email, cccd, diaChi // <-- Truyền dữ liệu thật
                        );

                        dialog.setVisible(true);

                        // 4. Xử lý kết quả
                        if (dialog.isConfirmed()) {
                            // --- LẤY DỮ LIỆU ĐÃ CHỈNH SỬA TỪ FORM ---
                            String newName = dialog.getCustomerName().trim();
                            String newCCCD = dialog.getCCCD().trim();
                            String newPhone = dialog.getPhone().trim();
                            String newEmail = dialog.getEmail().trim();
                            String newAddress = dialog.getAddress().trim();

                            // --- CẬP NHẬT LẠI OBJECT KHACHHANG VÀ LƯU DB ---
                            if (kh != null) {
                                boolean isChanged = false;
                                if (!newName.equals(tenKH)) {
                                    kh.setTenKH(newName);
                                    isChanged = true;
                                }
                                if (!newCCCD.equals(cccd)) {
                                    kh.setCCCD(newCCCD);
                                    isChanged = true;
                                }
                                if (!newPhone.equals(sdt)) {
                                    kh.setSoDT(newPhone);
                                    isChanged = true;
                                }
                                if (!newEmail.equals(email)) {
                                    kh.setEmail(newEmail);
                                    isChanged = true;
                                }
                                if (!newAddress.equals(diaChi)) {
                                    kh.setDiaChi(newAddress);
                                    isChanged = true;
                                }

                                if (isChanged) {
                                    System.out.println("Updating customer info for: " + kh.getMaKH());
                                    // Gọi DAO update
                                    eventController.getKhachHangDAO().updateKhachHang(kh);
                                }
                            }

                            // --- TIẾN HÀNH CHECK-IN VỚI TÊN MỚI (NẾU CÓ) ---
                            eventController.handleCheckIn(maPhieu, newName, maPhong);
                            successCount++;
                            currentIndex++;
                        } else {
                            JOptionPane.showMessageDialog(view, "Đã hủy thao tác check-in.", "Đã hủy",
                                    JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(view, "Lỗi dữ liệu ở hàng " + (i + 1) + ": " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
            }
            if (successCount > 0) {
                JOptionPane.showMessageDialog(view, "Đã check-in thành công " + successCount + " phiếu.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            // Chế độ Check-out (Giữ nguyên)
            List<String> selectedMaPhieu = new ArrayList<>();
            List<String> selectedMaPhong = new ArrayList<>();
            List<String> selectedTenKH = new ArrayList<>();
            for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
                if ((Boolean) view.getTableModel().getValueAt(i, 0)) {
                    selectedMaPhieu.add(safeGetValueAt(i, 1));
                    selectedMaPhong.add(safeGetValueAt(i, 4));
                    String tenKH = safeGetValueAt(i, 2);
                    selectedTenKH.add(tenKH);
                }
            }
            int confirm = JOptionPane.showConfirmDialog(view,
                    String.format("Bạn có chắc muốn check-out %d phiếu đã chọn?\nHóa đơn sẽ được hiển thị.",
                            selectedMaPhieu.size()),
                    "Xác nhận Check-out",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 0; i < selectedMaPhieu.size(); i++) {
                    eventController.handleCheckOut(selectedMaPhieu.get(i), selectedTenKH.get(i),
                            selectedMaPhong.get(i));
                }
                if (!selectedMaPhieu.isEmpty()) {
                    eventController.handleShowBill(selectedMaPhieu.get(0));
                }
            }
        }
        loadData();
        updateButtonState();
    }

    /**
     * Mở Dialog lịch sử check-out
     */
    private void showHistoryDialog() {
        if (eventController == null || phieuDatPhongDAO == null || phongDAO == null) {
            JOptionPane.showMessageDialog(view, "Lỗi: Controller hoặc DAO chưa được khởi tạo.", "Lỗi nghiêm trọng",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        HistoryCheckOutDialog dialog = new HistoryCheckOutDialog(ownerFrame, eventController, phieuDatPhongDAO,
                phongDAO);
        dialog.setVisible(true);
    }

    /**
     * Hàm helper để lấy giá trị từ bảng một cách an toàn, tránh lỗi
     * NullPointerException.
     */
    private String safeGetValueAt(int row, int col) {
        Object value = view.getTableModel().getValueAt(row, col);
        if (value == null) {
            // Ghi log lỗi để bạn biết cột nào bị null
            System.err.println("CẢNH BÁO: Giá trị tại [hàng=" + row + ", cột=" + col + "] bị null.");
            return "N/A"; // Trả về "N/A" thay vì gây lỗi
        }
        return value.toString();
    }

    public void forceCheckOutMode() {
        isCheckInMode = false;
        updateButtonState();
    }
}