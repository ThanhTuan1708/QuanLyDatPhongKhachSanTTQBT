package event;

import dao.DichVu_DAO;
import entity.DichVu;
import ui.gui.FormDialog.DichVuFormDialog;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventDichVu {

    private GUI_NhanVienLeTan.PanelDichVuContent view;
    private DichVu_DAO dichVuDAO;
    private List<DichVu> danhSachDichVuFull;

    public EventDichVu(GUI_NhanVienLeTan.PanelDichVuContent view) {
        this.view = view;
        try {
            this.dichVuDAO = new DichVu_DAO();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khởi tạo DAO: " + e.getMessage());
        }
    }

    public void initListeners() {
        if (view.getBtnAdd() != null) {
            view.getBtnAdd().addActionListener(e -> handleShowAddForm());
        }
        if (view.getSearchButton() != null) {
            view.getSearchButton().addActionListener(e -> handleSearch());
        }
        if (view.getSearchField() != null) {
            view.getSearchField().addActionListener(e -> handleSearch());
        }
    }

    public void loadDichVuData() {
        if (dichVuDAO == null) return;
        try {
            // Lấy dữ liệu mới nhất từ SQL
            this.danhSachDichVuFull = dichVuDAO.getAllDichVu();
            if (this.danhSachDichVuFull == null) this.danhSachDichVuFull = new ArrayList<>();

            // 1. Cập nhật bảng danh sách chi tiết (bên trên)
            view.populateDichVuList(this.danhSachDichVuFull);

            // 2. CẬP NHẬT GRID DANH MỤC ĐẦY ĐỦ (Bên dưới) - QUAN TRỌNG
            view.populateCategoryPanel(this.danhSachDichVuFull);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    public void handleSearch() {
        if (view.getSearchField() == null || danhSachDichVuFull == null) return;
        String searchText = view.getSearchField().getText().trim().toLowerCase();
        if (searchText.isEmpty() || searchText.equalsIgnoreCase("tìm kiếm dịch vụ...")) {
            view.populateDichVuList(this.danhSachDichVuFull);
            return;
        }
        List<DichVu> filtered = danhSachDichVuFull.stream()
                .filter(dv -> dv.getTenDV().toLowerCase().contains(searchText) || dv.getMaDV().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        view.populateDichVuList(filtered);
    }

    public void handleShowAddForm() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        new DichVuFormDialog(owner, null, this).setVisible(true);
    }

    public void handleShowEditForm(DichVu dv) {
        if (dv == null) return;
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        new DichVuFormDialog(owner, dv, this).setVisible(true);
    }

    public void handleSaveDichVu(DichVu dv, boolean isNew) {
        try {
            if (isNew) {
                if (dichVuDAO.getDichVuById(dv.getMaDV()) != null) {
                    JOptionPane.showMessageDialog(view, "Mã dịch vụ đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dichVuDAO.addDichVu(dv);
                JOptionPane.showMessageDialog(view, "Thêm thành công!");
            } else {
                dichVuDAO.updateDichVu(dv);
                JOptionPane.showMessageDialog(view, "Cập nhật thành công!");
            }
            loadDichVuData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi CSDL: " + e.getMessage());
        }
    }

    public void handleDeleteDichVu(DichVu dv) {
        int confirm = JOptionPane.showConfirmDialog(view, "Xóa dịch vụ " + dv.getTenDV() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (dichVuDAO.deleteDichVu(dv.getMaDV())) {
                JOptionPane.showMessageDialog(view, "Đã xóa!");
                loadDichVuData();
            } else {
                JOptionPane.showMessageDialog(view, "Không tìm thấy dịch vụ để xóa.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi: Dịch vụ đang được sử dụng, không thể xóa.");
        }
    }

    // --- MỚI THÊM: Xử lý sửa giá nhanh từ danh mục ---
    public void handleQuickEditPrice(String categoryName, JLabel priceLabel) {
        // Lấy giá cũ (xóa chữ ' đ' và dấu phẩy)
        String oldPriceStr = priceLabel.getText().replaceAll("[^0-9]", "");

        String input = JOptionPane.showInputDialog(view,
                "Cập nhật giá cho: " + categoryName + "\nNhập giá mới (VNĐ):",
                oldPriceStr);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double newPrice = Double.parseDouble(input.trim());

                // Gọi DAO cập nhật
                boolean success = dichVuDAO.updateGiaDichVuByTen(categoryName, newPrice);

                if (success) {
                    // Cập nhật UI ngay lập tức
                    priceLabel.setText(String.format("%,.0f đ", newPrice));
                    JOptionPane.showMessageDialog(view, "Đã cập nhật giá mới thành công!");

                    // Reload lại danh sách chính bên trên để đồng bộ
                    loadDichVuData();
                } else {
                    JOptionPane.showMessageDialog(view,
                            "Không tìm thấy dịch vụ có tên '" + categoryName + "' trong CSDL.\nHãy kiểm tra lại tên dịch vụ.",
                            "Lỗi cập nhật", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(view, "Vui lòng nhập số tiền hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}