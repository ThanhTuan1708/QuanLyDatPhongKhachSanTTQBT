package ui.gui.FormDialog;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import javax.swing.JOptionPane;

/**
 * Class hỗ trợ mở file hướng dẫn sử dụng trong trình duyệt web
 */
public class FormHuongDanSuDung {

    /**
     * Mở file hướng dẫn sử dụng trong trình duyệt mặc định
     * 
     * @param parent Frame cha (dùng để hiển thị thông báo lỗi nếu có)
     */
    public static void showDialog(java.awt.Frame parent) {
        openInBrowser();
    }

    /**
     * Mở file HTML trong trình duyệt web mặc định
     */
    public static void openInBrowser() {
        try {
            // Lấy đường dẫn tới file HTML
            String basePath = System.getProperty("user.dir");
            File htmlFile = new File(basePath, "src/HDSD/Huong_dan_sd.html");

            // Kiểm tra file tồn tại
            if (!htmlFile.exists()) {
                // Thử đường dẫn khác
                htmlFile = new File(basePath, "HDSD/Huong_dan_sd.html");
            }

            if (htmlFile.exists()) {
                // Mở file trong trình duyệt mặc định
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(htmlFile.toURI());
                    } else if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(htmlFile);
                    } else {
                        // Fallback cho Windows
                        Runtime.getRuntime().exec("cmd /c start " + htmlFile.getAbsolutePath());
                    }
                } else {
                    // Fallback cho Windows
                    Runtime.getRuntime().exec("cmd /c start " + htmlFile.getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Không tìm thấy file hướng dẫn!\nĐường dẫn: " + htmlFile.getAbsolutePath(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Không thể mở file hướng dẫn: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
