package ui.gui.FormDialog;

import entity.DichVu;
import event.EventDichVu;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * JDialog (Form) để Thêm mới hoặc Cập nhật thông tin Dịch Vụ.
 */
public class DichVuFormDialog extends JDialog {

    private EventDichVu controller;
    private DichVu dichVuHienTai;
    private boolean isNew;

    // Các trường nhập liệu
    private JTextField txtMaDV, txtTenDV, txtGiaTien, txtDonViTinh;
    private JTextArea txtMoTa;

    public DichVuFormDialog(Frame owner, DichVu dv, EventDichVu controller) {
        super(owner, true);
        this.controller = controller;
        this.dichVuHienTai = dv;
        this.isNew = (dv == null);

        setTitle(isNew ? "Thêm Dịch vụ mới" : "Cập nhật thông tin Dịch vụ");
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createInputFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        if (!isNew) {
            populateData();
        }
    }

    /**
     * Tạo panel chứa các trường nhập liệu.
     */
    private JPanel createInputFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1: Mã DV, Tên DV
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        form.add(new JLabel("Mã dịch vụ *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3;
        txtMaDV = new JTextField();
        form.add(txtMaDV, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2;
        form.add(new JLabel("Tên dịch vụ *"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.3;
        txtTenDV = new JTextField();
        form.add(txtTenDV, gbc);

        // Hàng 2: Giá tiền, Đơn vị tính
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Giá tiền *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtGiaTien = new JTextField();
        form.add(txtGiaTien, gbc);



        // Hàng 3: Mô tả
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Mô tả"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtMoTa);
        form.add(scroll, gbc);

        return form;
    }

    /**
     * Tạo panel chứa nút Hủy và Lưu.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton(isNew ? "Thêm mới" : "Lưu thay đổi");

        btnSave.setBackground(GUI_NhanVienLeTan.ACCENT_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnCancel.setFocusPainted(false);
        btnSave.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCancel.setBorder(new EmptyBorder(8, 20, 8, 20));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        return buttonPanel;
    }

    /**
     * Điền dữ liệu của 'dichVuHienTai' vào các trường input.
     */
    private void populateData() {
        txtMaDV.setText(dichVuHienTai.getMaDV());
        txtMaDV.setEnabled(false); // Không cho sửa Mã (khóa chính)
        txtTenDV.setText(dichVuHienTai.getTenDV());
        txtGiaTien.setText(String.valueOf(dichVuHienTai.getGiaTien()));
        txtMoTa.setText(dichVuHienTai.getMoTa());
    }

    /**
     * Thu thập dữ liệu, validate, và gọi Controller để lưu.
     */
    private void handleSave() {
        // 1. Validate
        String maDV = txtMaDV.getText().trim();
        String tenDV = txtTenDV.getText().trim();
        String giaTienStr = txtGiaTien.getText().trim();

        if (maDV.isEmpty() || tenDV.isEmpty() || giaTienStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã, Tên và Giá tiền.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double giaTien;
        try {
            giaTien = Double.parseDouble(giaTienStr);
            if (giaTien < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá tiền phải là một số (hoặc 0).", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Thu thập dữ liệu
        // Dùng constructor 5 tham số
        DichVu dv = new DichVu(
                maDV,
                tenDV,
                giaTien,
                txtMoTa.getText().trim()
        );

        // Nếu là cập nhật, đối tượng dv đã có đủ thông tin
        // Nếu là thêm mới, đối tượng dv cũng đã có đủ thông tin

        // 3. Gọi Controller
        controller.handleSaveDichVu(dv, isNew);

        dispose();
    }
}
