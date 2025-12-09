package ui.gui.FormDialog;

import entity.KhuyenMai;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class KhuyenMaiFormDialog extends JDialog {
    private JTextField txtMaKM, txtTenKM, txtChietKhau, txtNgayBatDau, txtNgayKetThuc;
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private KhuyenMai km;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhuyenMaiFormDialog(Frame parent, KhuyenMai km) {
        super(parent, true);
        this.km = km;
        initComponents();
        loadData();
        setupEvents();
    }

    private void initComponents() {
        setTitle(km == null ? "Thêm khuyến mãi" : "Chỉnh sửa khuyến mãi");
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        txtMaKM = new JTextField();
        txtTenKM = new JTextField();
        txtChietKhau = new JTextField();
        txtNgayBatDau = new JTextField();
        txtNgayKetThuc = new JTextField();

        form.add(new JLabel("Mã khuyến mãi:")); form.add(txtMaKM);
        form.add(new JLabel("Tên khuyến mãi:")); form.add(txtTenKM);
        form.add(new JLabel("Chiết khấu (%):")); form.add(txtChietKhau);
        form.add(new JLabel("Ngày bắt đầu (dd/MM/yyyy):")); form.add(txtNgayBatDau);
        form.add(new JLabel("Ngày kết thúc (dd/MM/yyyy):")); form.add(txtNgayKetThuc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        buttons.add(btnSave);
        buttons.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (km != null) {
            txtMaKM.setText(km.getMaKhuyenMai());
            txtTenKM.setText(km.getTenKhuyenMai());
            txtChietKhau.setText(String.valueOf(km.getChietKhau()));
            txtNgayBatDau.setText(km.getNgayBatDau() != null ? km.getNgayBatDau().toLocalDate().format(dateFormatter) : "");
            txtNgayKetThuc.setText(km.getNgayKetThuc() != null ? km.getNgayKetThuc().toLocalDate().format(dateFormatter) : "");
            txtMaKM.setEnabled(false); // Khóa mã khi sửa
        }
    }

    private void setupEvents() {
        btnSave.addActionListener(e -> saveData());
        btnCancel.addActionListener(e -> dispose());
    }

    private void saveData() {
        try {
            // --- RÀNG BUỘC DỮ LIỆU ---
            if (txtMaKM.getText().trim().isEmpty() ||
                    txtTenKM.getText().trim().isEmpty() ||
                    txtChietKhau.getText().trim().isEmpty() ||
                    txtNgayBatDau.getText().trim().isEmpty() ||
                    txtNgayKetThuc.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ thông tin!",
                        "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double chietKhau;
            try {
                chietKhau = Double.parseDouble(txtChietKhau.getText().trim());
                if (chietKhau < 0 || chietKhau > 100) {
                    JOptionPane.showMessageDialog(this,
                            "Chiết khấu phải nằm trong khoảng 0 - 100%",
                            "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Chiết khấu phải là số hợp lệ!",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate ngayBD, ngayKT;
            try {
                ngayBD = LocalDate.parse(txtNgayBatDau.getText().trim(), dateFormatter);
                ngayKT = LocalDate.parse(txtNgayKetThuc.getText().trim(), dateFormatter);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ngày phải có định dạng dd/MM/yyyy!",
                        "Lỗi định dạng ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ngayKT.isBefore(ngayBD)) {
                JOptionPane.showMessageDialog(this,
                        "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu!",
                        "Lỗi thời gian", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- GÁN DỮ LIỆU ---
            if (km == null) km = new KhuyenMai();
            km.setMaKhuyenMai(txtMaKM.getText().trim());
            km.setTenKhuyenMai(txtTenKM.getText().trim());
            km.setChietKhau(chietKhau);
            km.setNgayBatDau(Date.valueOf(ngayBD));
            km.setNgayKetThuc(Date.valueOf(ngayKT));

            saved = true;
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Đã xảy ra lỗi khi lưu dữ liệu!",
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public KhuyenMai getKhuyenMai() {
        return km;
    }
}