package ui;

import javax.swing.*;
import connectDB.ConnectDB;
import entity.NhanVien;
import entity.LoaiNhanVien;
import ui.gui.FormDialog.FormDangNhap;
import ui.gui.GUI_NhanVienLeTan;
import ui.gui.GUI_NhanVienQuanLy;
import java.sql.Connection;
import java.sql.SQLException;

public class Application_new {
    public static NhanVien currentLoggedInNhanVien = null;

    public static void main(String[] args) {
        try {
            // Set look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Kết nối CSDL
            Connection conn = ConnectDB.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully!");
            } else {
                throw new SQLException("Không thể tạo kết nối đến CSDL");
            }

            // Thiết lập shutdown hook để đóng kết nối khi thoát
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Đang đóng kết nối CSDL...");
                try {
                    ConnectDB.disconnect();
                    System.out.println("Đã đóng kết nối CSDL.");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng kết nối CSDL: " + e.getMessage());
                }
            }));

            SwingUtilities.invokeLater(() -> showLoginForm());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi khởi động ứng dụng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void showLoginForm() {
        try {
            FormDangNhap login = new FormDangNhap();
            login.setVisible(true);

            // Thread to wait for login form to close
            new Thread(() -> {
                while (login.isVisible()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                handleLoginResult(login);

            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi hiển thị form đăng nhập: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void handleLoginResult(FormDangNhap login) {
        try {
            currentLoggedInNhanVien = login.getLoggedInNhanVien();

            if (currentLoggedInNhanVien != null) {
                LoaiNhanVien roleEnum = currentLoggedInNhanVien.getChucVu();

                if (roleEnum != null) {
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("Login successful. Role: " + roleEnum.getLabel());

                        try {
                            if (roleEnum == LoaiNhanVien.QUAN_LY) {
                                new GUI_NhanVienQuanLy(currentLoggedInNhanVien).setVisible(true);
                            } else if (roleEnum == LoaiNhanVien.LE_TAN) {
                                new GUI_NhanVienLeTan(currentLoggedInNhanVien).setVisible(true);
                            } else {
                                throw new Exception("Chức vụ không được hỗ trợ: " + roleEnum.getLabel());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null,
                                    "Lỗi mở giao diện: " + e.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Không thể xác định chức vụ cho nhân viên.",
                            "Lỗi Dữ liệu",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            } else {
                System.out.println("Login failed or cancelled.");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi xử lý đăng nhập: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}