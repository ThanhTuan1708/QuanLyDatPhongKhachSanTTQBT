package event;

import dao.LoaiPhong_DAO;
import dao.Phong_DAO;
import dao.TrangThaiPhong_DAO;
import entity.LoaiPhongEntity;
import entity.Phong;
import entity.TrangThaiPhongEntity;
import ui.gui.FormDialog.PhongFormDialog;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp Controller xử lý sự kiện cho PanelPhongContent.
 * (ĐÃ THÊM TÍNH NĂNG MỚI: CẬP NHẬT TRẠNG THÁI)
 */
public class EventPhong {

    private GUI_NhanVienLeTan.PanelPhongContent view;
    private Phong_DAO phongDAO;
    private LoaiPhong_DAO loaiPhongDAO;
    private TrangThaiPhong_DAO trangThaiPhongDAO;

    // highlight-start
    private EventDatPhong datPhongController; // <-- THÊM BIẾN ĐỂ CẬP NHẬT TAB ĐẶT PHÒNG
    // highlight-end

    private List<Phong> danhSachPhongFull; // Danh sách gốc

    // highlight-start
    // *** THÊM HẰNG SỐ NÀY ***
    private static final int TT_PHONG_SAN_SANG = 1; // Giả sử 1 = Sẵn sàng
    // highlight-end

    // highlight-start
    // *** SỬA CONSTRUCTOR NÀY ***
    public EventPhong(GUI_NhanVienLeTan.PanelPhongContent view, EventDatPhong datPhongController) {
        this.view = view;
        this.datPhongController = datPhongController; // <-- LƯU LẠI
        // highlight-end
        try {
            this.phongDAO = new Phong_DAO();
            this.loaiPhongDAO = new LoaiPhong_DAO();
            this.trangThaiPhongDAO = new TrangThaiPhong_DAO();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khởi tạo DAO cho Phòng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gắn các listener chính cho các nút trong PanelPhongContent.
     */
    public void initListeners() {
        view.getBtnAdd().addActionListener(e -> handleShowAddForm());
        view.getSearchField().addActionListener(e -> handleSearchAndFilter());
        view.getStatusFilter().addActionListener(e -> handleSearchAndFilter());
        view.getTypeFilter().addActionListener(e -> handleSearchAndFilter());
    }

    /**
     * Tải và hiển thị toàn bộ danh sách phòng từ CSDL.
     */
    public void loadPhongData() {
        try {
            this.danhSachPhongFull = phongDAO.getAllPhongWithDetails();
            if (this.danhSachPhongFull == null) {
                this.danhSachPhongFull = new ArrayList<>();
            }
            view.populatePhongList(this.danhSachPhongFull);

            // Tải dữ liệu cho các ComboBox lọc (chỉ tải nếu rỗng)
            if (view.getTypeFilter().getItemCount() <= 1) {
                loadFilterComboBoxes();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tải dữ liệu cho JComboBox lọc (Loại phòng và Trạng thái)
     */
    private void loadFilterComboBoxes() {
        try {
            List<LoaiPhongEntity> dsLoai = loaiPhongDAO.getAllLoaiPhong();
            List<TrangThaiPhongEntity> dsTrangThai = trangThaiPhongDAO.getAllTrangThaiPhong();

            JComboBox<String> cmbLoai = view.getTypeFilter();
            JComboBox<String> cmbTrangThai = view.getStatusFilter();

            cmbLoai.removeAllItems();
            cmbLoai.addItem("Tất cả loại");
            dsLoai.forEach(lp -> cmbLoai.addItem(lp.getTenLoaiPhong()));

            cmbTrangThai.removeAllItems();
            cmbTrangThai.addItem("Tất cả trạng thái");
            dsTrangThai.forEach(ttp -> cmbTrangThai.addItem(ttp.getTenTrangThai()));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu bộ lọc: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Xử lý tìm kiếm và lọc danh sách phòng.
     */
    public void handleSearchAndFilter() {
        String searchText = view.getSearchField().getText().trim().toLowerCase();
        if (searchText.equals("tìm kiếm phòng...")) {
            searchText = ""; // Bỏ qua placeholder
        }

        String trangThaiFilter = view.getStatusFilter().getSelectedItem().toString();
        String loaiPhongFilter = view.getTypeFilter().getSelectedItem().toString();

        String finalSearchText = searchText;
        List<Phong> dsPhongDaLoc = danhSachPhongFull.stream()
                .filter(phong -> {
                    boolean trangThaiMatch = trangThaiFilter.equals("Tất cả trạng thái") ||
                            phong.getTrangThaiPhong().getTenTrangThai().equals(trangThaiFilter);
                    boolean loaiPhongMatch = loaiPhongFilter.equals("Tất cả loại") ||
                            phong.getLoaiPhong().getTenLoaiPhong().equals(loaiPhongFilter);
                    boolean searchMatch = finalSearchText.isEmpty() ||
                            phong.getMaPhong().toLowerCase().contains(finalSearchText);

                    return trangThaiMatch && loaiPhongMatch && searchMatch;
                })
                .collect(Collectors.toList());

        view.populatePhongList(dsPhongDaLoc);
    }

    /**
     * Hiển thị JDialog (Form) để THÊM phòng mới.
     */
    public void handleShowAddForm() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        PhongFormDialog dialog = new PhongFormDialog(owner, null, this, loaiPhongDAO, trangThaiPhongDAO);
        dialog.setVisible(true);
    }

    /**
     * Hiển thị JDialog (Form) để SỬA thông tin phòng.
     */
    public void handleShowEditForm(Phong p) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        PhongFormDialog dialog = new PhongFormDialog(owner, p, this, loaiPhongDAO, trangThaiPhongDAO);
        dialog.setVisible(true);
    }

    /**
     * Xử lý nghiệp vụ LƯU (Thêm mới hoặc Cập nhật) phòng.
     */
    public void handleSavePhong(Phong p, boolean isNew) {
        try {
            if (isNew) {
                if (phongDAO.getPhongById(p.getMaPhong()) != null) {
                    JOptionPane.showMessageDialog(view, "Mã phòng " + p.getMaPhong() + " đã tồn tại!", "Lỗi trùng mã", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                phongDAO.addPhong(p);
                JOptionPane.showMessageDialog(view, "Thêm phòng mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                phongDAO.updatePhong(p);
                JOptionPane.showMessageDialog(view, "Cập nhật thông tin phòng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadPhongData();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý nghiệp vụ XÓA phòng.
     */
    public void handleDeletePhong(Phong p) {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn xóa phòng:\n" + p.getMaPhong() + " (" + p.getLoaiPhong().getTenLoaiPhong() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = phongDAO.deletePhong(p.getMaPhong());

            if (success) {
                JOptionPane.showMessageDialog(view, "Đã xóa phòng " + p.getMaPhong(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadPhongData();
            } else {
                JOptionPane.showMessageDialog(view, "Không thể xóa phòng " + p.getMaPhong(), "Thất bại", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("REFERENCE constraint")) {
                JOptionPane.showMessageDialog(view, "Không thể xóa phòng này vì đã có lịch sử đặt phòng.", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi CSDL khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        }
    }

    // highlight-start
    /**
     * TÍNH NĂNG MỚI: Xử lý chuyển trạng thái phòng về "Sẵn sàng"
     */
    public void handleMarkRoomAsAvailable(Phong p) {
        String currentStatus = p.getTrangThaiPhong().getTenTrangThai();

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn chuyển phòng " + p.getMaPhong() + " từ '" + currentStatus + "' sang 'Sẵn sàng'?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Lấy mã "Sẵn sàng" từ CSDL (Giả sử là 1)
            TrangThaiPhongEntity ttSanSang = trangThaiPhongDAO.getTrangThaiPhongByTen("Sẵn sàng");
            int maTrangThaiSanSang = (ttSanSang != null) ? ttSanSang.getMaTrangThai() : TT_PHONG_SAN_SANG;

            // Gọi DAO để cập nhật trạng thái phòng
            boolean success = phongDAO.updatePhongTrangThai(p.getMaPhong(), maTrangThaiSanSang);

            if (success) {
                JOptionPane.showMessageDialog(view, "Đã cập nhật phòng " + p.getMaPhong() + " sang 'Sẵn sàng'.", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // 1. Tải lại danh sách phòng (màn hình hiện tại)
                loadPhongData();

                // 2. YÊU CẦU MÀN HÌNH ĐẶT PHÒNG CẬP NHẬT
                if (datPhongController != null) {
                    datPhongController.filterRooms(); // <-- CẬP NHẬT MÀN HÌNH ĐẶT PHÒNG
                }
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật trạng thái thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi CSDL khi cập nhật trạng thái phòng: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
    // highlight-end
}