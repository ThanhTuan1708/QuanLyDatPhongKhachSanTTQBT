package dao;

import connectDB.ConnectDB;
import entity.LoaiPhongEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiPhong_DAO {

    /**
     * Lấy danh sách tất cả các loại phòng.
     */
    public List<LoaiPhongEntity> getAllLoaiPhong() throws SQLException {
        List<LoaiPhongEntity> dsLoaiPhong = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return dsLoaiPhong;

        String sql = "SELECT * FROM LoaiPhong";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoaiPhongEntity lp = new LoaiPhongEntity(
                        rs.getInt("maLoaiPhong"),
                        rs.getString("tenLoaiPhong")
                );
                dsLoaiPhong.add(lp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoaiPhong;
    }

    /**
     * Lấy thông tin loại phòng bằng mã.
     */
    public LoaiPhongEntity getLoaiPhongById(int maLoaiPhong) throws SQLException {
        LoaiPhongEntity lp = null;
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;

        String sql = "SELECT * FROM LoaiPhong WHERE maLoaiPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, maLoaiPhong);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    lp = new LoaiPhongEntity(
                            rs.getInt("maLoaiPhong"),
                            rs.getString("tenLoaiPhong")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lp;
    }
}