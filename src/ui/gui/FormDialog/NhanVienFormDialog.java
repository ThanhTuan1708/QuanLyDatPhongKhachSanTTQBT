package ui.gui.FormDialog;

import dao.NhanVien_DAO;
import entity.NhanVien;
import entity.GioiTinh;
import entity.LoaiNhanVien;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NhanVienFormDialog extends JDialog {

    private final NhanVien_DAO dao = new NhanVien_DAO();
    private boolean success = false; // true nếu thêm/sửa thành công

    public NhanVienFormDialog(Frame owner, NhanVien editEmp) {
        super(owner, editEmp == null ? "Thêm nhân viên" : "Sửa nhân viên", true);
        setSize(400, 420);
        setLocationRelativeTo(owner);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JTextField code = new JTextField(editEmp == null ? "" : editEmp.getMaNV());
        JTextField name = new JTextField(editEmp == null ? "" : editEmp.getTenNV());
        JTextField dob = new JTextField(editEmp == null ? "" :
                (editEmp.getNgaySinh() != null ? editEmp.getNgaySinh().format(formatter) : ""));
        JTextField phone = new JTextField(editEmp == null ? "" : editEmp.getSoDT());
        JTextField email = new JTextField(editEmp == null ? "" : editEmp.getEmail());
        JTextField cccd = new JTextField(editEmp == null ? "" : editEmp.getCCCD());

        // === Giới tính ===
        JRadioButton radNam = new JRadioButton("Nam");
        JRadioButton radNu = new JRadioButton("Nữ");
        ButtonGroup groupGender = new ButtonGroup();
        groupGender.add(radNam);
        groupGender.add(radNu);

        if (editEmp != null && editEmp.getGioiTinh() != null) {
            radNam.setSelected(editEmp.getGioiTinh() == GioiTinh.NAM);
            radNu.setSelected(editEmp.getGioiTinh() == GioiTinh.NU);
        } else {
            radNam.setSelected(true);
        }

        // === Chức vụ (Loại nhân viên) ===
        JComboBox<String> role = new JComboBox<>(new String[]{"Lễ tân", "Quản lý"});
        if (editEmp != null && editEmp.getChucVu() != null) {
            if (editEmp.getChucVu() == LoaiNhanVien.LE_TAN) {
                role.setSelectedItem("Lễ tân");
            } else if (editEmp.getChucVu() == LoaiNhanVien.QUAN_LY) {
                role.setSelectedItem("Quản lý");
            }
        }

        // === Panel form ===
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        addLabelAndField(form, "Mã NV:", code);
        addLabelAndField(form, "Họ tên:", name);
        addLabelAndField(form, "Ngày sinh (dd/MM/yyyy):", dob);
        addLabelAndField(form, "Giới tính:", createGenderPanel(radNam, radNu));
        addLabelAndField(form, "SĐT:", phone);
        addLabelAndField(form, "Email:", email);
        addLabelAndField(form, "CCCD:", cccd);
        addLabelAndField(form, "Loại NV:", role);

        // === Nút OK / Cancel ===
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        btnOk.addActionListener(e -> {
            if (saveData(editEmp, code, name, dob, phone, email, cccd, radNam, role, formatter))
                dispose();
        });
    }

    private void addLabelAndField(JPanel panel, String label, Component field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl);
        panel.add(field);
    }

    private JPanel createGenderPanel(JRadioButton nam, JRadioButton nu) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(nam);
        p.add(nu);
        return p;
    }

    private boolean saveData(NhanVien editEmp, JTextField code, JTextField name, JTextField dob,
                             JTextField phone, JTextField email, JTextField cccd,
                             JRadioButton radNam, JComboBox<String> role, DateTimeFormatter formatter) {

        String maNV = code.getText().trim();
        String hoTen = name.getText().trim();
        String sdtStr = phone.getText().trim();
        String emailStr = email.getText().trim();
        String cccdStr = cccd.getText().trim();
        String dobText = dob.getText().trim();

        GioiTinh gioiTinh = radNam.isSelected() ? GioiTinh.NAM : GioiTinh.NU;
        String chucVuStr = (String) role.getSelectedItem();
        LoaiNhanVien chucVu = chucVuStr.equals("Quản lý") ? LoaiNhanVien.QUAN_LY : LoaiNhanVien.LE_TAN;

        LocalDate ngaySinh = null;

        // === Kiểm tra dữ liệu ===
        if (maNV.isEmpty() || hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã NV và Họ tên không được để trống!");
            return false;
        }
        if (!sdtStr.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "SĐT không hợp lệ!");
            return false;
        }
        if (!cccdStr.matches("\\d{9,12}")) {
            JOptionPane.showMessageDialog(this, "CCCD không hợp lệ!");
            return false;
        }
        if (!emailStr.isEmpty() && !emailStr.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!");
            return false;
        }
        if (!dobText.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(dobText, formatter);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ!");
                return false;
            }
        }

        // === Lưu vào DB ===
        if (editEmp == null) {
            NhanVien nv = new NhanVien(maNV, hoTen, sdtStr, emailStr, "", cccdStr, ngaySinh, gioiTinh, chucVu, "123456");
            if (dao.addNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                success = true;
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
                return false;
            }
        } else {
            editEmp.setTenNV(hoTen);
            editEmp.setSoDT(sdtStr);
            editEmp.setEmail(emailStr);
            editEmp.setCCCD(cccdStr);
            editEmp.setNgaySinh(ngaySinh);
            editEmp.setGioiTinh(gioiTinh);
            editEmp.setChucVu(chucVu);

            if (dao.updateNhanVien(editEmp)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                success = true;
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                return false;
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }
}