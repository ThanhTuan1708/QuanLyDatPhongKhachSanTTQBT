package dao;

import connectDB.ConnectDB;
import entity.KhuyenMai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai_DAO {

    /**
     * Lấy tất cả Khuyến mãi
     */
    public List<KhuyenMai> getAllKhuyenMai() throws SQLException {
        List<KhuyenMai> dsKM = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM KhuyenMai";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // SỬA LỖI: Dùng constructor rỗng và setter
                KhuyenMai km = new KhuyenMai();
                km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setChietKhau(rs.getDouble("chietKhau"));
                km.setNgayBatDau(rs.getDate("ngayBatDau"));
                km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
                km.setTrangThai(rs.getString("trangThai"));
                km.setLuotSuDung(rs.getInt("luotSuDung"));
                dsKM.add(km);
            }
        }
        return dsKM;
    }

    /**
     * Lấy Khuyến mãi bằng ID
     */
    public KhuyenMai getKhuyenMaiById(String maKM) throws SQLException {
        KhuyenMai km = null;
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maKM);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // SỬA LỖI: Dùng constructor rỗng và setter
                    km = new KhuyenMai();
                    km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                    km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                    km.setChietKhau(rs.getDouble("chietKhau"));
                    km.setNgayBatDau(rs.getDate("ngayBatDau"));
                    km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
                    km.setTrangThai(rs.getString("trangThai"));
                    km.setLuotSuDung(rs.getInt("luotSuDung"));
                }
            }
        }
        return km;
    }

    /**
     * Thêm Khuyến mãi mới
     */
    public boolean addKhuyenMai(KhuyenMai km) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, chietKhau, ngayBatDau, ngayKetThuc, trangThai, luotSuDung) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, km.getMaKhuyenMai());
            stmt.setString(2, km.getTenKhuyenMai());
            stmt.setDouble(3, km.getChietKhau());
            stmt.setDate(4, km.getNgayBatDau());
            stmt.setDate(5, km.getNgayKetThuc());
            stmt.setString(6, km.getTrangThai());
            stmt.setInt(7, km.getLuotSuDung());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật Khuyến mãi (Phiên bản KHÔNG DÙNG transaction)
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        try (Connection con = ConnectDB.getConnection()) {
            return updateKhuyenMai(con, km);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật Khuyến mãi (Phiên bản DÙNG CHO transaction)
     */
    public boolean updateKhuyenMai(Connection con, KhuyenMai km) throws SQLException {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, chietKhau = ?, ngayBatDau = ?, ngayKetThuc = ?, luotSuDung = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, km.getTenKhuyenMai());
            stmt.setDouble(2, km.getChietKhau());
            stmt.setDate(3, km.getNgayBatDau());
            stmt.setDate(4, km.getNgayKetThuc());
            stmt.setInt(5, km.getLuotSuDung());
            stmt.setString(6, km.getTrangThai());
            stmt.setString(7, km.getMaKhuyenMai());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Xóa Khuyến mãi
     */
    public boolean deleteKhuyenMai(String maKM) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKM);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // highlight-start
    // *** THÊM CÁC HÀM THỐNG KÊ BỊ THIẾU ***

    private int countKhuyenMaiByTrangThai(String trangThai) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT COUNT(*) FROM KhuyenMai WHERE trangThai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAllKhuyenMai() throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT COUNT(*) FROM KhuyenMai";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countKhuyenMaiDangHoatDong() throws SQLException {
        return countKhuyenMaiByTrangThai("Đang hoạt động");
    }

    public int countKhuyenMaiHetHan() throws SQLException {
        return countKhuyenMaiByTrangThai("Hết hạn");
    }

    // *** KẾT THÚC THÊM HÀM ***
    // highlight-end
}