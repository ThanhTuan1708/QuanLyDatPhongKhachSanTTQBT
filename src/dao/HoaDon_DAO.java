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
                    System.out.println("DEBUG HoaDon_DAO - Mã hóa đơn: " + maHD + " | Mã khuyến mãi trong DB: " + maKM);
                    if (maKM != null && !maKM.isEmpty()) {
                        KhuyenMai km = khuyenMaiDAO.getKhuyenMaiById(maKM);
                        hd.setKhuyenMai(km);
                        System.out.println("DEBUG HoaDon_DAO - Đã load KhuyenMai: "
                                + (km != null ? km.getTenKhuyenMai() + " - " + km.getChietKhau() + "%" : "NULL"));
                    } else {
                        System.out.println("DEBUG HoaDon_DAO - Không có mã khuyến mãi cho hóa đơn này");
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

    public String findMaHoaDonByMaPhieu(String maPhieu) throws SQLException {
        String maHD = null;
        Connection con = ConnectDB.getConnection();

        // Query kết hợp: Tìm hóa đơn có chứa phòng đó VÀ phải cùng Khách Hàng với phiếu
        // đặt phòng
        // Điều này đảm bảo không lấy nhầm hóa đơn cũ của khách khác ở cùng phòng đó.
        String sql = "SELECT TOP 1 hd.maHoaDon " +
                "FROM HoaDon hd " +
                "JOIN ChiTietHoaDon_Phong ctp ON hd.maHoaDon = ctp.maHoaDon " +
                "JOIN PhieuDatPhong pdp ON hd.maKH = pdp.maKH " + // Ràng buộc: Phải cùng khách hàng
                "WHERE pdp.maPhieu = ? " +
                "  AND ctp.maPhong = pdp.maPhong " + // Ràng buộc: Phải đúng phòng trong phiếu
                "ORDER BY hd.maHoaDon DESC"; // Lấy cái mới nhất

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPhieu);
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