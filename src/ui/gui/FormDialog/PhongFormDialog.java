package ui.gui.FormDialog;

import dao.LoaiPhong_DAO;
import dao.TrangThaiPhong_DAO;
import entity.LoaiPhongEntity;
import entity.Phong;
import entity.TrangThaiPhongEntity;
import event.EventPhong;
import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * JDialog (Form) để Thêm mới hoặc Cập nhật thông tin Phòng.
 */
public class PhongFormDialog extends JDialog {

    private EventPhong controller;
    private Phong phongHienTai;
    private boolean isNew;

    // Các DAO để lấy dữ liệu cho JComboBox
    private LoaiPhong_DAO loaiPhongDAO;
    private TrangThaiPhong_DAO trangThaiPhongDAO;

    // Các trường nhập liệu
    private JTextField txtMaPhong, txtGiaTien;
    private JTextArea txtMoTa;
    private JSpinner spinnerSoChua;
    private JComboBox<LoaiPhongEntity> cmbLoaiPhong;
    private JComboBox<TrangThaiPhongEntity> cmbTrangThai;

    public PhongFormDialog(Frame owner, Phong p, EventPhong controller, LoaiPhong_DAO lpDAO, TrangThaiPhong_DAO ttpDAO) {
        super(owner, true);
        this.controller = controller;
        this.phongHienTai = p;
        this.isNew = (p == null);
        this.loaiPhongDAO = lpDAO;
        this.trangThaiPhongDAO = ttpDAO;

        setTitle(isNew ? "Thêm Phòng mới" : "Cập nhật thông tin Phòng");
        setSize(500, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(GUI_NhanVienLeTan.COLOR_WHITE);
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createInputFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Tải dữ liệu cho ComboBox
        loadComboBoxData();

        if (!isNew) {
            populateData();
        }
    }

    /**
     * Tải dữ liệu cho JComboBox
     */
    private void loadComboBoxData() {
        try {
            // Lấy dữ liệu từ DAO
            List<LoaiPhongEntity> dsLoai = loaiPhongDAO.getAllLoaiPhong();
            List<TrangThaiPhongEntity> dsTrangThai = trangThaiPhongDAO.getAllTrangThaiPhong();

            // Đổ vào JComboBox
            // toString() của Entity đã được override để hiển thị tên
            cmbLoaiPhong.setModel(new DefaultComboBoxModel<>(new Vector<>(dsLoai)));
            cmbTrangThai.setModel(new DefaultComboBoxModel<>(new Vector<>(dsTrangThai)));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu Loại/Trạng thái phòng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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

        // Hàng 1: Mã phòng, Giá tiền
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        form.add(new JLabel("Mã phòng *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3;
        txtMaPhong = new JTextField();
        form.add(txtMaPhong, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2;
        form.add(new JLabel("Giá tiền / đêm *"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.3;
        txtGiaTien = new JTextField();
        form.add(txtGiaTien, gbc);

        // Hàng 2: Loại phòng, Trạng thái
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Loại phòng *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        cmbLoaiPhong = new JComboBox<>();
        form.add(cmbLoaiPhong, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        form.add(new JLabel("Trạng thái *"), gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        cmbTrangThai = new JComboBox<>();
        form.add(cmbTrangThai, gbc);

        // Hàng 3: Số chứa
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Số người chứa *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        spinnerSoChua = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1)); // Mặc định 2, min 1, max 10
        form.add(spinnerSoChua, gbc);

        // Hàng 4: Mô tả
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Mô tả"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
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
     * Điền dữ liệu của 'phongHienTai' vào các trường input.
     */
    private void populateData() {
        txtMaPhong.setText(phongHienTai.getMaPhong());
        txtMaPhong.setEnabled(false); // Không cho sửa Mã phòng (khóa chính)

        txtGiaTien.setText(String.valueOf(phongHienTai.getGiaTienMotDem()));
        txtMoTa.setText(phongHienTai.getMoTa());
        spinnerSoChua.setValue(phongHienTai.getSoChua());

        // Chọn đúng item trong ComboBox
        cmbLoaiPhong.setSelectedItem(phongHienTai.getLoaiPhong());
        cmbTrangThai.setSelectedItem(phongHienTai.getTrangThaiPhong());

        // Không cho sửa Trạng thái trong form này
        // (Nên dùng nghiệp vụ (Check-in/out) hoặc form riêng)
        cmbTrangThai.setEnabled(false);
    }

    /**
     * Thu thập dữ liệu, validate, và gọi Controller để lưu.
     */
    private void handleSave() {
        // 1. Validate
        String maPhong = txtMaPhong.getText().trim();
        String giaTienStr = txtGiaTien.getText().trim();
        if (maPhong.isEmpty() || giaTienStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã phòng và Giá tiền.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double giaTien;
        try {
            giaTien = Double.parseDouble(giaTienStr);
            if (giaTien < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá tiền phải là một số dương.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Thu thập dữ liệu
        Phong p = isNew ? new Phong() : phongHienTai;

        p.setMaPhong(maPhong);
        p.setGiaTienMotDem(giaTien);
        p.setMoTa(txtMoTa.getText().trim());
        p.setSoChua((Integer) spinnerSoChua.getValue());
        p.setLoaiPhong((LoaiPhongEntity) cmbLoaiPhong.getSelectedItem());

        if (isNew) {
            // Nếu là phòng mới, mặc định là "Sẵn sàng" (ID=1)
            TrangThaiPhongEntity sanSang = (TrangThaiPhongEntity) cmbTrangThai.getModel().getElementAt(0); // Giả sử "Sẵn sàng" ở index 0
            for(int i=0; i<cmbTrangThai.getItemCount(); i++){
                if(cmbTrangThai.getItemAt(i).getMaTrangThai() == 1){ // Tìm đúng ID=1
                    sanSang = cmbTrangThai.getItemAt(i);
                    break;
                }
            }
            p.setTrangThaiPhong(sanSang);
        }
        // (Nếu là cập nhật, trạng thái giữ nguyên)

        // 3. Gọi Controller
        controller.handleSavePhong(p, isNew);

        dispose();
    }
}
