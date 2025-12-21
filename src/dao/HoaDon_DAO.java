package dao;

import connectDB.ConnectDB;
import entity.*; // Import all entities

import java.sql.*;
import java.time.LocalDateTime;

public class HoaDon_DAO {

    private KhachHang_DAO khachHangDAO;
    private KhuyenMai_DAO khuyenMaiDAO;
    private ChiTietHoaDon_DAO chiTietHoaDonDAO;

    public HoaDon_DAO() {
        try {
            khachHangDAO = new KhachHang_DAO();
            khuyenMaiDAO = new KhuyenMai_DAO();
            chiTietHoaDonDAO = new ChiTietHoaDon_DAO();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo DAO phụ thuộc trong HoaDon_DAO: " + e.getMessage());
        }
    }

    /**
     * Lấy Hóa đơn bằng Mã Hóa Đơn
     * (SỬA: Đã xóa cột 'maNV' khỏi SQL)
     */
    public HoaDon getHoaDonById(String maHD) throws SQLException {
        HoaDon hd = null;
        Connection con = ConnectDB.getConnection();

        KhachHang_DAO khachHangDAO = new KhachHang_DAO();
        KhuyenMai_DAO khuyenMaiDAO = new KhuyenMai_DAO();
        ChiTietHoaDon_DAO chiTietDAO = new ChiTietHoaDon_DAO();

        // SỬA: Xóa maNV khỏi SQL
        String sql = "SELECT maHoaDon, ngayLap, VAT, hinhThucThanhToan, tongTien, maKH, maKhuyenMai FROM HoaDon WHERE maHoaDon = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("maHoaDon"));
                    hd.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
                    hd.setVat(rs.getDouble("VAT"));
                    hd.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                    hd.setTongTien(rs.getDouble("tongTien"));

                    String maKH = rs.getString("maKH");
                    hd.setKhachHang(khachHangDAO.getKhachHangById(maKH));

                    // (ĐÃ XÓA LẤY NHÂN VIÊN VÌ CSDL KHÔNG CÓ)

                    String maKM = rs.getString("maKhuyenMai");
                    if (maKM != null) {
                        hd.setKhuyenMai(khuyenMaiDAO.getKhuyenMaiById(maKM));
                    }

                    hd.setDsChiTietPhong(chiTietDAO.getChiTietPhongByMaHD(maHD));
                    hd.setDsChiTietDichVu(chiTietDAO.getChiTietDichVuByMaHD(maHD));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Lỗi khi tải hóa đơn " + maHD + ": " + e.getMessage());
        }
        return hd;
    }

    public boolean addHoaDon(HoaDon hd) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            // Cần đảm bảo hàm này hỗ trợ transaction nếu được gọi từ ngoài
            con.setAutoCommit(false);
            try {
                boolean result = addHoaDon(con, hd);
                con.commit();
                return result;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Thêm Hóa đơn (DÙNG CHO TRANSACTION)
     * (SỬA: Đã xóa cột 'maNV' khỏi INSERT)
     */
    public boolean addHoaDon(Connection con, HoaDon hd) throws SQLException {
        // SỬA: Xóa maNV khỏi câu SQL (còn 7 tham số)
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLap, VAT, hinhThucThanhToan, tongTien, maKH, maKhuyenMai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            stmt.setDouble(3, hd.getVat());
            stmt.setString(4, hd.getHinhThucThanhToan());
            stmt.setDouble(5, hd.getTongTien());
            stmt.setString(6, hd.getKhachHang().getMaKH());
            // Dòng 7 (maNV) ĐÃ BỊ XÓA
            stmt.setString(7, (hd.getKhuyenMai() != null) ? hd.getKhuyenMai().getMaKhuyenMai() : null);
            return stmt.executeUpdate() > 0;
        }
        // Không đóng connection, để transaction quản lý
    }

    /**
     * Tìm mã hóa đơn dựa trên mã phiếu đặt phòng.
     * (SỬA: Logic truy vấn theo CSDL)
     */
    public String findMaHoaDonByMaPhieu(String maPhieu) throws SQLException {
        String maHD = null;
        Connection con = ConnectDB.getConnection();

        // Bước 1: Tìm maPhong từ maPhieu
        String maPhong = null;
        String sqlFindPhong = "SELECT maPhong FROM dbo.PhieuDatPhong WHERE maPhieu = ?";
        try (PreparedStatement stmtFind = con.prepareStatement(sqlFindPhong)) {
            stmtFind.setString(1, maPhieu);
            try (ResultSet rsFind = stmtFind.executeQuery()) {
                if (rsFind.next()) {
                    maPhong = rsFind.getString("maPhong");
                }
            }
        }
        if (maPhong == null) {
            throw new SQLException("Không tìm thấy phòng cho phiếu: " + maPhieu);
        }

        // Bước 2: Tìm Hóa đơn chứa maPhong đó
        String sqlFindHD = "SELECT TOP 1 maHoaDon " +
                "FROM dbo.ChiTietHoaDon_Phong " +
                "WHERE maPhong = ? " +
                "ORDER BY maHoaDon DESC"; // Lấy hóa đơn mới nhất của phòng này

        try (PreparedStatement stmt = con.prepareStatement(sqlFindHD)) {
            stmt.setString(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    maHD = rs.getString("maHoaDon");
                }
            }
        }
        return maHD;
    }

    /**
     * Cập nhật thông tin Hóa đơn (Tổng tiền, Mã khuyến mãi)
     */
    public boolean updateHoaDon(HoaDon hd) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "UPDATE HoaDon SET tongTien = ?, maKhuyenMai = ? WHERE maHoaDon = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, hd.getTongTien());
            stmt.setString(2, (hd.getKhuyenMai() != null) ? hd.getKhuyenMai().getMaKhuyenMai() : null);
            stmt.setString(3, hd.getMaHoaDon());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}