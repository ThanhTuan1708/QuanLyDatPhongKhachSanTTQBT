package ui.gui.FormDialog;

import entity.GioiTinh;
import entity.KhachHang;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;

public class KhachHangFormDialog extends JDialog {
    private JTextField txtMaKH, txtTenKH, txtNgaySinh, txtSoDT, txtEmail, txtCCCD, txtDiaChi;
    private JComboBox<String> cmbHang;
    private JRadioButton radNam, radNu;
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private KhachHang kh;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhachHangFormDialog(Frame parent, KhachHang kh) {
        super(parent, true);
        this.kh = kh;
        initComponents();
        loadData();
        setupEvents();
    }

    private void initComponents() {
        setTitle(kh == null ? "Thêm khách hàng" : "Chỉnh sửa khách hàng");
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        txtMaKH = new JTextField();
        txtTenKH = new JTextField();
        txtNgaySinh = new JTextField();
        txtSoDT = new JTextField();
        txtEmail = new JTextField();
        txtCCCD = new JTextField();
        txtDiaChi = new JTextField();

        radNam = new JRadioButton("Nam");
        radNu = new JRadioButton("Nữ");
        ButtonGroup group = new ButtonGroup();
        group.add(radNam);
        group.add(radNu);

        form.add(new JLabel("Mã khách hàng:")); form.add(txtMaKH);
        form.add(new JLabel("Tên khách hàng:")); form.add(txtTenKH);
        form.add(new JLabel("Ngày sinh (dd/MM/yyyy):")); form.add(txtNgaySinh);
        form.add(new JLabel("Giới tính:"));
        JPanel pnlGender = new JPanel();
        pnlGender.add(radNam); pnlGender.add(radNu);
        form.add(pnlGender);
        form.add(new JLabel("Số điện thoại:")); form.add(txtSoDT);
        form.add(new JLabel("Email:")); form.add(txtEmail);
        form.add(new JLabel("CCCD:")); form.add(txtCCCD);
        form.add(new JLabel("Địa chỉ:")); form.add(txtDiaChi);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        buttons.add(btnSave);
        buttons.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (kh != null) {
            txtMaKH.setText(kh.getMaKH());
            txtTenKH.setText(kh.getTenKH());
            txtNgaySinh.setText(kh.getNgaySinh() != null ? kh.getNgaySinh().format(dateFormatter) : "");
            if (kh.getGioiTinh() == GioiTinh.NAM)
                radNam.setSelected(true);
            else if (kh.getGioiTinh() == GioiTinh.NU)
                radNu.setSelected(true);
            else
                radNam.setSelected(false); // nếu KHÁC thì không chọn gì
            txtSoDT.setText(kh.getSoDT());
            txtEmail.setText(kh.getEmail());
            txtCCCD.setText(kh.getCCCD());
            txtDiaChi.setText(kh.getDiaChi());
            txtMaKH.setEnabled(false); // khóa mã khi sửa
        } else {
            radNam.setSelected(true);
        }
    }

    private void setupEvents() {
        btnSave.addActionListener(e -> saveData());
        btnCancel.addActionListener(e -> dispose());
    }

    private void saveData() {
        try {
            if (kh == null) kh = new KhachHang();

            kh.setMaKH(txtMaKH.getText().trim());
            kh.setTenKH(txtTenKH.getText().trim());
            if (radNam.isSelected())
                kh.setGioiTinh(GioiTinh.NAM);
            else if (radNu.isSelected())
                kh.setGioiTinh(GioiTinh.NU);
            else
                kh.setGioiTinh(GioiTinh.KHAC);
            kh.setSoDT(txtSoDT.getText().trim());
            kh.setEmail(txtEmail.getText().trim());
            kh.setCCCD(txtCCCD.getText().trim());
            kh.setDiaChi(txtDiaChi.getText().trim());

            if (!txtNgaySinh.getText().trim().isEmpty()) {
                kh.setNgaySinh(LocalDate.parse(txtNgaySinh.getText().trim(), dateFormatter));
            } else {
                kh.setNgaySinh(null);
            }

            saved = true;
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ngày sinh phải có định dạng dd/MM/yyyy!",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public KhachHang getKhachHang() {
        return kh;
    }
}
