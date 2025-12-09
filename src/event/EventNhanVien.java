package event;

import dao.NhanVien_DAO;
import entity.NhanVien;
import ui.gui.FormDialog.NhanVienFormDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EventNhanVien {

    private final NhanVien_DAO dao = new NhanVien_DAO();

    public void handleAdd(Frame parent, Runnable afterUpdate) {
        NhanVienFormDialog dialog = new NhanVienFormDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) afterUpdate.run();
    }

    public void handleEdit(Frame parent, NhanVien emp, Runnable afterUpdate) {
        NhanVienFormDialog dialog = new NhanVienFormDialog(parent, emp);
        dialog.setVisible(true);
        if (dialog.isSuccess()) afterUpdate.run();
    }

    public void handleDelete(Component parent, NhanVien emp, Runnable afterUpdate) {
        int confirm = JOptionPane.showConfirmDialog(parent,
                "Bạn có chắc muốn xóa nhân viên " + emp.getTenNV() + " không?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteNhanVien(emp.getMaNV())) {
                JOptionPane.showMessageDialog(parent, "Đã xóa nhân viên " + emp.getTenNV());
                afterUpdate.run();
            } else {
                JOptionPane.showMessageDialog(parent, "Xóa thất bại!");
            }
        }
    }

    public List<NhanVien> reloadAll() {
        return dao.getAllNhanVien();
    }

    public int countAll() {
        return dao.countAllNhanVien();
    }

    public int countByLoai(int loai) {
        return dao.countNhanVienByLoai(loai);
    }
}
