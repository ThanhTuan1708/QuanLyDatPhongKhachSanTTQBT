package event;

import dao.KhuyenMai_DAO;
import entity.KhuyenMai;
import ui.gui.FormDialog.KhuyenMaiFormDialog;
import ui.gui.GUI_NhanVienQuanLy;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class EventKhuyenMai {
    private final GUI_NhanVienQuanLy.PanelKhuyenMaiContent view;
    private final KhuyenMai_DAO dao;

    public EventKhuyenMai(GUI_NhanVienQuanLy.PanelKhuyenMaiContent view, KhuyenMai_DAO dao) {
        this.view = view;
        this.dao = dao;
    }

    // Mở form thêm khuyến mãi
    public void handleAddKhuyenMai() throws SQLException {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(view);
        KhuyenMaiFormDialog dlg = new KhuyenMaiFormDialog(parent, null);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            KhuyenMai km = dlg.getKhuyenMai();
            km.setTrangThai(xacDinhTrangThai(km));

            if (dao.addKhuyenMai(km)) {
                JOptionPane.showMessageDialog(view, "Thêm khuyến mãi thành công!");
                view.reloadData();
                view.refreshStats(); // cập nhật thống kê sau khi thêm
            } else {
                JOptionPane.showMessageDialog(view, "Thêm thất bại!");
            }
        }
    }

    // Mở form sửa khuyến mãi
    public void handleEditKhuyenMai(KhuyenMai editKM) throws SQLException {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(view);
        KhuyenMaiFormDialog dlg = new KhuyenMaiFormDialog(parent, editKM);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            editKM.setTrangThai(xacDinhTrangThai(editKM));

            if (dao.updateKhuyenMai(editKM)) {
                JOptionPane.showMessageDialog(view, "Cập nhật khuyến mãi thành công!");
                view.reloadData();
                view.refreshStats(); // cập nhật thống kê sau khi sửa
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật thất bại!");
            }
        }
    }

    // Xóa khuyến mãi
    public void handleDeleteKhuyenMai(KhuyenMai km) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc muốn xóa khuyến mãi '" + km.getTenKhuyenMai() + "' không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteKhuyenMai(km.getMaKhuyenMai())) {
                JOptionPane.showMessageDialog(view, "Đã xóa khuyến mãi.");
                view.reloadData();
                view.refreshStats(); // cập nhật thống kê sau khi xoá
            } else {
                JOptionPane.showMessageDialog(view, "Xóa thất bại!");
            }
        }
    }

    // Hàm xác định trạng thái khuyến mãi dựa trên ngày
    private String xacDinhTrangThai(KhuyenMai km) {
        LocalDate now = LocalDate.now();
        LocalDate start = km.getNgayBatDau().toLocalDate();
        LocalDate end = km.getNgayKetThuc().toLocalDate();

        if ((now.isAfter(start) || now.isEqual(start)) && (now.isBefore(end) || now.isEqual(end))) {
            return "Đang hoạt động";
        } else {
            return "Hết hạn";
        }
    }
}