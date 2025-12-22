package dao;

import connectDB.ConnectDB;
import entity.DichVu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVu_DAO {

    /**
     * Lấy dịch vụ theo mã.
     * (ĐÃ SỬA: Sử dụng đúng tên cột CSDL và bỏ donViTinh)
     */
    public DichVu getDichVuById(String maDV) throws SQLException {
        DichVu dv = null;
        Connection con = ConnectDB.getConnection();

        // SỬA TÊN CỘT Ở ĐÂY VÀ BỎ donViTinh
        String sql = "SELECT maDichVu, tenDichVu, gia, moTa FROM DichVu WHERE maDichVu = ?"; // <--- SỬA
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDV);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // SỬA TÊN CỘT VÀ BỎ donViTinh khỏi constructor (cần constructor 4 tham số)
                    // Giả sử Entity DichVu có constructor (String, String, double, String)
                    dv = new DichVu(
                            rs.getString("maDichVu"),  // <--- SỬA
                            rs.getString("tenDichVu"),  // <--- SỬA
                            rs.getDouble("gia"),       // <--- SỬA
                            rs.getString("moTa")       // <--- Bỏ donViTinh
                    );
                    // Nếu không có constructor 4 tham số, dùng setter:
                    /*
                    dv = new DichVu();
                    dv.setMaDV(rs.getString("maDichVu"));
                    dv.setTenDV(rs.getString("tenDichVu"));
                    dv.setGiaTien(rs.getDouble("gia"));
                    dv.setMoTa(rs.getString("moTa"));
                    */
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dịch vụ theo ID '" + maDV + "': " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return dv;
    }

    /**
     * Lấy danh sách tất cả dịch vụ.
     * (ĐÃ SỬA: Sử dụng đúng tên cột và bỏ donViTinh)
     */
    public List<DichVu> getAllDichVu() throws SQLException {
        List<DichVu> dsDV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();

        // SỬA TÊN CỘT VÀ BỎ donViTinh
        String sql = "SELECT maDichVu, tenDichVu, gia, moTa FROM DichVu ORDER BY tenDichVu"; // <--- SỬA
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // SỬA TÊN CỘT VÀ BỎ donViTinh khỏi constructor
                DichVu dv = new DichVu(
                        rs.getString("maDichVu"),  // <--- SỬA
                        rs.getString("tenDichVu"),  // <--- SỬA
                        rs.getDouble("gia"),       // <--- SỬA
                        rs.getString("moTa")       // <--- Bỏ donViTinh
                );
                // Hoặc dùng setter nếu cần
                dsDV.add(dv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return dsDV;
    }

    /**
     * Thêm dịch vụ mới.
     * (ĐÃ SỬA: Sử dụng đúng tên cột và bỏ donViTinh)
     */
    public boolean addDichVu(DichVu dv) throws SQLException {
        Connection con = ConnectDB.getConnection();
        // SỬA TÊN CỘT VÀ BỎ donViTinh
        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, gia, moTa) VALUES (?, ?, ?, ?)"; // <--- SỬA
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, dv.getMaDV());
            pstmt.setString(2, dv.getTenDV());
            pstmt.setDouble(3, dv.getGiaTien());
            pstmt.setString(4, dv.getMoTa()); // <--- Bỏ donViTinh
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin chi tiết của một dịch vụ.
     * (ĐÃ SỬA: Sử dụng đúng tên cột và bỏ donViTinh)
     */
    public boolean updateDichVu(DichVu dv) throws SQLException {
        Connection con = ConnectDB.getConnection();
        // SỬA TÊN CỘT VÀ BỎ donViTinh
        String sql = "UPDATE DichVu SET tenDichVu = ?, gia = ?, moTa = ? " + // <--- SỬA
                "WHERE maDichVu = ?"; // <--- SỬA
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, dv.getTenDV());
            pstmt.setDouble(2, dv.getGiaTien());
            pstmt.setString(3, dv.getMoTa()); // <--- Bỏ donViTinh
            pstmt.setString(4, dv.getMaDV()); // <--- SỬA tên cột điều kiện

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Xóa một dịch vụ khỏi CSDL.
     * (ĐÃ SỬA: Sử dụng đúng tên cột)
     */
    public boolean deleteDichVu(String maDV) throws SQLException {
        Connection con = ConnectDB.getConnection();
        // SỬA TÊN CỘT Ở ĐÂY
        String sql = "DELETE FROM DichVu WHERE maDichVu = ?"; // <--- SỬA
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDV);
            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean updateGiaDichVuByTen(String tenDV, double giaMoi) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "UPDATE DichVu SET gia = ? WHERE tenDichVu = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDouble(1, giaMoi);
            pstmt.setString(2, tenDV);
            return pstmt.executeUpdate() > 0;
        }
    }


}