package dao;

import connectDB.ConnectDB;
import entity.TrangThaiPhongEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrangThaiPhong_DAO {

    /**
     * Lấy danh sách tất cả các trạng thái phòng.
     */
    public List<TrangThaiPhongEntity> getAllTrangThaiPhong() throws SQLException {
        List<TrangThaiPhongEntity> dsTrangThai = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return dsTrangThai;

        String sql = "SELECT * FROM TrangThaiPhong";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TrangThaiPhongEntity ttp = new TrangThaiPhongEntity(
                        rs.getInt("maTrangThai"),
                        rs.getString("tenTrangThai")
                );
                dsTrangThai.add(ttp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsTrangThai;
    }

    /**
     * Lấy thông tin trạng thái phòng bằng mã.
     */
    public TrangThaiPhongEntity getTrangThaiPhongById(int maTrangThai) throws SQLException {
        TrangThaiPhongEntity ttp = null;
        Connection con = ConnectDB. getConnection();
        if (con == null) return null;

        String sql = "SELECT * FROM TrangThaiPhong WHERE maTrangThai = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, maTrangThai);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ttp = new TrangThaiPhongEntity(
                            rs.getInt("maTrangThai"),
                            rs.getString("tenTrangThai")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ttp;
    }

    /**
     * THÊM HÀM MỚI NÀY VÀO
     * Lấy thông tin trạng thái phòng bằng TÊN.
     */
    public TrangThaiPhongEntity getTrangThaiPhongByTen(String tenTrangThai) throws SQLException {
        TrangThaiPhongEntity ttp = null;
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;

        String sql = "SELECT * FROM TrangThaiPhong WHERE tenTrangThai = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tenTrangThai);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ttp = new TrangThaiPhongEntity(
                            rs.getInt("maTrangThai"),
                            rs.getString("tenTrangThai")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Ném lỗi ra để EventDatPhong xử lý
        }
        return ttp;
    }

    // Thêm hàm này vào Phong_DAO.java
    public boolean updatePhongTrangThai(String maPhong, int maTrangThai, Connection con) throws SQLException {
        // (Hàm này dùng 'con' được truyền vào)
        String sql = "UPDATE Phong SET maTrangThai = ? WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, maTrangThai);
            pstmt.setString(2, maPhong);
            return pstmt.executeUpdate() > 0;
        }
        // Không đóng 'con' ở đây
    }

}