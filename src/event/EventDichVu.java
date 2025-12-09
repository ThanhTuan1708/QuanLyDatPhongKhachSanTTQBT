package event;

// --- Imports from Previous Code ---
import connectDB.ConnectDB;
import ui.gui.FormDialog.DichVuFormDialog;
import ui.gui.GUI_NhanVienLeTan.PanelDatPhongContent;
import ui.gui.FormDialog.BillDialog;
import ui.gui.FormDialog.BookingFormDialog;
import ui.gui.GUI_NhanVienLeTan;
import entity.*; // Covers KhachHang, GioiTinh, NhanVien, Phong, PhieuDatPhong, HoaDon, ChiTietHoaDon_Phong, DichVu, ChiTietHoaDon_DichVu
import dao.*;   // Covers KhachHang_DAO, Phong_DAO, PhieuDatPhong_DAO, HoaDon_DAO, DichVu_DAO, ChiTietHoaDon_DAO

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;

// --- Time related imports (Ensure these are present) ---
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; // Needed for parsing dates
import java.time.temporal.ChronoUnit;         // Needed for calculating days

// --- Util imports (Ensure these are present) ---
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // Needed for fetchRoomDetails
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * Lớp Controller xử lý sự kiện cho PanelDichVuContent.
 */
public class EventDichVu {

    private GUI_NhanVienLeTan.PanelDichVuContent view;
    private DichVu_DAO dichVuDAO;
    private List<DichVu> danhSachDichVuFull; // Danh sách gốc

    public EventDichVu(GUI_NhanVienLeTan.PanelDichVuContent view) {
        this.view = view;
        try {
            // Assumes DichVu_DAO constructor is simple and doesn't throw checked exceptions other than potential runtime ones
            this.dichVuDAO = new DichVu_DAO();
        } catch (Exception e) {
            // Catching general Exception can hide specific issues, but okay for broad init error
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi nghiêm trọng khi khởi tạo DichVu_DAO: " + e.getMessage(), "Lỗi Khởi Tạo", JOptionPane.ERROR_MESSAGE);
            // Consider disabling features if DAO fails
        }
    }

    /**
     * Gắn các listener chính cho các nút trong PanelDichVuContent.
     */
    public void initListeners() {
        // Ensure the view provides these getter methods
        if (view.getBtnAdd() != null) {
            view.getBtnAdd().addActionListener(e -> handleShowAddForm());
        }
        if (view.getSearchButton() != null) {
            view.getSearchButton().addActionListener(e -> handleSearch());
        }
        if (view.getSearchField() != null) {
            // Allow searching on pressing Enter in the text field
            view.getSearchField().addActionListener(e -> handleSearch());
        }
    }

    /**
     * Tải và hiển thị toàn bộ danh sách dịch vụ từ CSDL.
     */
    public void loadDichVuData() {
        // Ensure dichVuDAO was initialized successfully
        if (dichVuDAO == null) {
            JOptionPane.showMessageDialog(view, "Lỗi: DichVu_DAO chưa được khởi tạo.", "Lỗi Controller", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // Lấy tất cả dịch vụ từ DAO
            // *** CRITICAL: Ensure DichVu_DAO.getAllDichVu() uses CORRECT column names (maDichVu, tenDichVu, gia) ***
            this.danhSachDichVuFull = dichVuDAO.getAllDichVu(); //
            if (this.danhSachDichVuFull == null) {
                this.danhSachDichVuFull = new ArrayList<>(); // Initialize if DAO returns null
                System.err.println("Warning: dichVuDAO.getAllDichVu() returned null.");
            }
            // Cập nhật giao diện (View) với danh sách đầy đủ
            // Ensure PanelDichVuContent has this method
            view.populateDichVuList(this.danhSachDichVuFull);

        } catch (SQLException e) {
            e.printStackTrace();
            // *** CRITICAL: Check the SQLException message here. If it's "Invalid column name 'maDV'" or 'donViTinh',
            // *** you MUST fix DichVu_DAO.java as instructed previously. ***
            JOptionPane.showMessageDialog(view, "Lỗi CSDL khi tải danh sách dịch vụ: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Catch other potential runtime errors during UI update
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi không xác định khi tải dịch vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý tìm kiếm/lọc danh sách dịch vụ.
     */
    public void handleSearch() {
        // Ensure view components are accessible
        if (view.getSearchField() == null || danhSachDichVuFull == null) {
            System.err.println("Error in handleSearch: View components or full list not ready.");
            return;
        }

        String searchText = view.getSearchField().getText().trim().toLowerCase();
        // Use equalsIgnoreCase for placeholder check for robustness
        if (searchText.isEmpty() || searchText.equalsIgnoreCase("tìm kiếm dịch vụ...")) {
            // If search is cleared, show the full list
            view.populateDichVuList(this.danhSachDichVuFull);
            return;
        }

        // Use Stream API to filter (ensure DichVu has getTenDV and getMaDV)
        String finalSearchText = searchText; // Need final variable for lambda
        List<DichVu> dsDichVuDaLoc = danhSachDichVuFull.stream()
                .filter(dv -> dv != null && dv.getTenDV() != null && dv.getMaDV() != null) // Add null checks
                .filter(dv -> dv.getTenDV().toLowerCase().contains(finalSearchText) ||
                        dv.getMaDV().toLowerCase().contains(finalSearchText))
                .collect(Collectors.toList());

        // Cập nhật giao diện (View) với danh sách đã lọc
        view.populateDichVuList(dsDichVuDaLoc);
    }

    /**
     * Hiển thị JDialog (Form) để THÊM dịch vụ mới.
     */
    public void handleShowAddForm() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        if (owner == null) {
            System.err.println("Error: Cannot find parent frame for Add Dialog.");
            return;
        }
        // Assumes DichVuFormDialog exists and has this constructor
        DichVuFormDialog dialog = new DichVuFormDialog(owner, null, this);
        dialog.setVisible(true); // Show the dialog modally
    }

    /**
     * Hiển thị JDialog (Form) để SỬA thông tin dịch vụ.
     */
    public void handleShowEditForm(DichVu dv) {
        if (dv == null) return; // Don't open if dv is null
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        if (owner == null) {
            System.err.println("Error: Cannot find parent frame for Edit Dialog.");
            return;
        }
        // Assumes DichVuFormDialog exists and has this constructor
        DichVuFormDialog dialog = new DichVuFormDialog(owner, dv, this);
        dialog.setVisible(true); // Show the dialog modally
    }

    /**
     * Xử lý nghiệp vụ LƯU (Thêm mới hoặc Cập nhật) dịch vụ.
     */
    public void handleSaveDichVu(DichVu dv, boolean isNew) {
        if (dichVuDAO == null || dv == null) {
            JOptionPane.showMessageDialog(view, "Lỗi: Không thể lưu dịch vụ (DAO hoặc dữ liệu null).", "Lỗi Controller", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (isNew) {
                // Kiểm tra mã DV không được trùng
                // *** CRITICAL: Ensure DichVu_DAO.getDichVuById() uses CORRECT column name (maDichVu) ***
                if (dichVuDAO.getDichVuById(dv.getMaDV()) != null) { //
                    JOptionPane.showMessageDialog(view, "Mã dịch vụ '" + dv.getMaDV() + "' đã tồn tại!", "Lỗi trùng mã", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent saving
                }
                // Gọi DAO để thêm
                // *** CRITICAL: Ensure DichVu_DAO.addDichVu() uses CORRECT column names & no donViTinh ***
                dichVuDAO.addDichVu(dv); //
                JOptionPane.showMessageDialog(view, "Thêm dịch vụ '" + dv.getTenDV() + "' thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Gọi DAO để cập nhật
                // *** CRITICAL: Ensure DichVu_DAO.updateDichVu() uses CORRECT column names & no donViTinh ***
                dichVuDAO.updateDichVu(dv); //
                JOptionPane.showMessageDialog(view, "Cập nhật dịch vụ '" + dv.getTenDV() + "' thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            // Tải lại danh sách trên giao diện để hiển thị thay đổi
            loadDichVuData();

        } catch (SQLException e) {
            e.printStackTrace();
            // *** CRITICAL: Check the SQLException message here. If it's about column names, fix DAO. ***
            JOptionPane.showMessageDialog(view, "Lỗi CSDL khi lưu dịch vụ: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi không xác định khi lưu dịch vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý nghiệp vụ XÓA dịch vụ.
     */
    public void handleDeleteDichVu(DichVu dv) {
        if (dichVuDAO == null || dv == null) {
            JOptionPane.showMessageDialog(view, "Lỗi: Không thể xóa dịch vụ (DAO hoặc dữ liệu null).", "Lỗi Controller", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa dịch vụ:\n" + dv.getTenDV() + " (Mã: " + dv.getMaDV() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        try {
            // Gọi DAO để xóa
            // *** CRITICAL: Ensure DichVu_DAO.deleteDichVu() uses CORRECT column name (maDichVu) ***
            boolean success = dichVuDAO.deleteDichVu(dv.getMaDV()); //

            if (success) {
                JOptionPane.showMessageDialog(view, "Đã xóa dịch vụ " + dv.getTenDV(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDichVuData(); // Reload the list
            } else {
                // This might happen if the ID doesn't exist, though unlikely if selected from list
                JOptionPane.showMessageDialog(view, "Không tìm thấy dịch vụ '" + dv.getTenDV() + "' để xóa.", "Thất bại", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            // Handle foreign key constraint errors (e.g., service used in an invoice)
            if (e.getMessage().contains("REFERENCE constraint") || e.getMessage().contains("foreign key constraint")) {
                JOptionPane.showMessageDialog(view, "Không thể xóa dịch vụ này vì đang được sử dụng trong hóa đơn hoặc đặt phòng.", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            } else {
                // Other SQL errors
                JOptionPane.showMessageDialog(view, "Lỗi CSDL khi xóa dịch vụ: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi không xác định khi xóa dịch vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

}

