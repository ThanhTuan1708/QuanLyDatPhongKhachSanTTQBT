package ui;

import javax.swing.*;

import connectDB.ConnectDB;
import entity.NhanVien;
import ui.gui.FormDialog.FormDangNhap;
import ui.gui.GUI_NhanVienLeTan;
import ui.gui.GUI_NhanVienQuanLy;
import entity.LoaiNhanVien;
import java.sql.Connection;
import java.sql.SQLException;

public class Application {
    public static NhanVien currentLoggedInNhanVien = null;

    public static void main(String[] args) {
        // 1. Thiết lập look and feel của hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Không thể đặt giao diện hệ thống: " + e.getMessage());
            // Vẫn tiếp tục chạy với giao diện mặc định
        }

        // 2. Kết nối CSDL trước khi hiện form đăng nhập
        try {
            System.out.println("Đang kết nối CSDL...");
            Connection conn = ConnectDB.getConnection(); 
            if (conn != null && !conn.isClosed()) {
                System.out.println("Kết nối CSDL thành công!");
            } else {
                throw new SQLException("Không thể tạo kết nối đến CSDL");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Không thể kết nối đến cơ sở dữ liệu!\nLỗi: " + e.getMessage(),
                "Lỗi Khởi động",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // 3. Chạy ứng dụng trong EDT
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Khởi động ứng dụng...");
                FormDangNhap login = new FormDangNhap();
                login.setLocationRelativeTo(null); // Căn giữa form
                login.setVisible(true);

                // Thread theo dõi form đăng nhập
                new Thread(() -> {
                    try {
                        // Đợi cho đến khi form đóng
                        while (login.isVisible()) {
                            Thread.sleep(100);
                        }

                        // Lấy thông tin nhân viên đăng nhập
                        currentLoggedInNhanVien = login.getLoggedInNhanVien();

                        if (currentLoggedInNhanVien != null) {
                            LoaiNhanVien roleEnum = currentLoggedInNhanVien.getChucVu();

                            if (roleEnum != null) {
                                // Chuyển về EDT để cập nhật UI
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        System.out.println("Đăng nhập thành công. Vai trò: " + roleEnum.getLabel());

                                        switch (roleEnum) {
                                            case QUAN_LY:
                                                new GUI_NhanVienQuanLy(currentLoggedInNhanVien).setVisible(true);
                                                break;
                                            case LE_TAN:
                                                new GUI_NhanVienLeTan(currentLoggedInNhanVien).setVisible(true);
                                                break;
                                            default:
                                                throw new Exception("Chức vụ không được hỗ trợ: " + roleEnum.getLabel());
                                        }
                                    } catch (Exception e) {
                                        String message = "Lỗi khi mở giao diện: " + e.getMessage();
                                        System.err.println(message);
                                        JOptionPane.showMessageDialog(null, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                                        System.exit(1);
                                    }
                                });
                            } else {
                                throw new Exception("Không thể xác định chức vụ cho nhân viên (Dữ liệu lỗi)");
                            }
                        } else {
                            System.out.println("Đăng nhập thất bại hoặc đã hủy.");
                            System.exit(0);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Thread bị gián đoạn: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Lỗi: " + e.getMessage());
                        JOptionPane.showMessageDialog(null,
                            "Lỗi: " + e.getMessage(),
                            "Lỗi Khởi động",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }).start();

            } catch (Exception e) {
                System.err.println("Lỗi không mong đợi: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                    "Lỗi không mong đợi: " + e.getMessage(),
                    "Lỗi Khởi động",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // 4. Thiết lập hook để đóng kết nối CSDL khi thoát
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang đóng kết nối CSDL...");
            try {
                ConnectDB.disconnect();
                System.out.println("Đã đóng kết nối CSDL.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối CSDL: " + e.getMessage());
            }
        }));
    }
}