package event;

import dao.KhachHang_DAO;
import entity.KhachHang;
import ui.gui.FormDialog.KhachHangFormDialog;
import ui.gui.GUI_NhanVienLeTan;


import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class EventKhachHang{
    private final GUI_NhanVienLeTan.PanelKhachHangContent view;
    private final KhachHang_DAO dao;

    public EventKhachHang(GUI_NhanVienLeTan.PanelKhachHangContent view, KhachHang_DAO dao) {
        this.view = view;
        this.dao = dao;
    }

    // Mở form thêm
    public void handleAddKhachHang() throws SQLException {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(view);
        KhachHangFormDialog dlg = new KhachHangFormDialog(parent, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            KhachHang kh = dlg.getKhachHang();
            kh.setSoLanLuuTru(0);
            kh.setTongChiTieu(0);
            kh.setDanhGiaTrungBinh(0);
            if (dao.addKhachHang(kh)) {
                JOptionPane.showMessageDialog(view, "Thêm khách hàng thành công!");
                view.reloadData();
            } else {
                JOptionPane.showMessageDialog(view, "Thêm thất bại!");
            }
        }
    }

    // Mở form sửa
    public void handleEditKhachHang(KhachHang editKH) throws SQLException {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(view);
        KhachHangFormDialog dlg = new KhachHangFormDialog(parent, editKH);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            if (dao.updateKhachHang(editKH)) {
                JOptionPane.showMessageDialog(view, "Cập nhật thành công!");
                view.reloadData();
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật thất bại!");
            }
        }
    }

    // Xóa
    public void handleDeleteKhachHang(KhachHang kh) throws SQLException {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc muốn xóa khách hàng " + kh.getTenKH() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteKhachHang(kh.getMaKH())) {
                JOptionPane.showMessageDialog(view, "Đã xóa khách hàng.");
                view.reloadData();
            } else {
                JOptionPane.showMessageDialog(view, "Xóa thất bại!");
            }
        }
    }
}