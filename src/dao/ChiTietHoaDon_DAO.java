package dao;

import connectDB.ConnectDB;
import entity.*;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO gộp: Quản lý cả ChiTietHoaDon_Phong và ChiTietHoaDon_DichVu.
 * (ĐÃ SỬA LỖI LOGIC VÀ TRANSACTION)
 */
public class ChiTietHoaDon_DAO {

    /**
     * Thêm chi tiết hóa đơn cho phòng đã đặt.
     * (SỬA: Sửa theo CSDL: maHD, maPhong, thanhTien)
     */
    public boolean addChiTietPhong(Connection con, ChiTietHoaDon_Phong chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon_Phong (maHoaDon, maPhong, thanhTien) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, chiTiet.getHoaDon().getMaHoaDon());
            stmt.setString(2, chiTiet.getPhieuDatPhong().getPhong().getMaPhong());
            stmt.setDouble(3, chiTiet.getThanhTien());

            int n = stmt.executeUpdate();
            return n > 0;
        }
    }

    /**
     * Thêm chi tiết hóa đơn cho dịch vụ đã sử dụng.
     */
    public boolean addChiTietDichVu(Connection con, ChiTietHoaDon_DichVu chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon_DichVu (maHoaDon, maDichVu, soLuong, thanhTien) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, chiTiet.getHoaDon().getMaHoaDon());
            stmt.setString(2, chiTiet.getDichVu().getMaDV());
            stmt.setInt(3, chiTiet.getSoLuong());
            stmt.setDouble(4, chiTiet.getThanhTien());

            int n = stmt.executeUpdate();
            return n > 0;
        }
    }

    /**
     * Lấy danh sách tất cả các chi tiết dịch vụ thuộc về một hóa đơn cụ thể.
     */
    public List<ChiTietHoaDon_DichVu> getChiTietDichVuByMaHD(String maHD) throws SQLException {
        List<ChiTietHoaDon_DichVu> dsChiTiet = new ArrayList<>();
        Connection con = ConnectDB.getConnection();

        String sql = "SELECT ctdv.soLuong, ctdv.thanhTien, " +
                "dv.maDichVu, dv.tenDichVu, dv.gia AS giaGoc " +
                "FROM ChiTietHoaDon_DichVu ctdv " +
                "JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu " +
                "WHERE ctdv.maHoaDon = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(maHD);
                    DichVu dv = new DichVu();
                    dv.setMaDV(rs.getString("maDichVu"));
                    dv.setTenDV(rs.getString("tenDichVu"));

                    int soLuong = rs.getInt("soLuong");
                    double thanhTien = rs.getDouble("thanhTien");
                    double donGiaLucDat = (soLuong > 0) ? thanhTien / soLuong : 0;
                    dv.setGiaTien(donGiaLucDat);

                    ChiTietHoaDon_DichVu chiTiet = new ChiTietHoaDon_DichVu(hd, dv, soLuong, donGiaLucDat, thanhTien);
                    dsChiTiet.add(chiTiet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết dịch vụ theo mã hóa đơn: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return dsChiTiet;
    }

    /**
     * Lấy danh sách tất cả các chi tiết phòng thuộc về một hóa đơn cụ thể.
     * (ĐÃ SỬA LỖI: Xóa logic liên quan đến NhanVien khỏi PhieuDatPhong)
     */
    public List<ChiTietHoaDon_Phong> getChiTietPhongByMaHD(String maHD) throws SQLException {
        List<ChiTietHoaDon_Phong> dsChiTiet = new ArrayList<>();
        Connection con = ConnectDB.getConnection();

        // Sửa: Câu query này chỉ lấy thông tin phòng, không lấy PĐP
        String sql = "SELECT " +
                "ctp.maHoaDon, ctp.maPhong, ctp.thanhTien AS tongThanhTien, " +
                "p.giaTienMotDem, p.soChua, " +
                "lp.tenLoaiPhong, " +
                "ttp.tenTrangThai " +
                "FROM ChiTietHoaDon_Phong ctp " +
                "JOIN Phong p ON ctp.maPhong = p.maPhong " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "JOIN TrangThaiPhong ttp ON p.maTrangThai = ttp.maTrangThai " +
                "WHERE ctp.maHoaDon = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(maHD);

                    LoaiPhongEntity lp = new LoaiPhongEntity();
                    lp.setTenLoaiPhong(rs.getString("tenLoaiPhong"));

                    TrangThaiPhongEntity ttp = new TrangThaiPhongEntity();
                    ttp.setTenTrangThai(rs.getString("tenTrangThai"));

                    Phong phong = new Phong();
                    phong.setMaPhong(rs.getString("maPhong"));
                    phong.setGiaTienMotDem(rs.getDouble("giaTienMotDem"));
                    phong.setSoChua(rs.getInt("soChua"));
                    phong.setLoaiPhong(lp);
                    phong.setTrangThaiPhong(ttp);

                    // highlight-start
                    // *** SỬA LỖI 'getNhanVien' ***
                    // CSDL của bạn không liên kết CTHD_Phong với PhieuDatPhong.
                    // Chúng ta tạo 1 PĐP "giả" chỉ chứa phòng
                    PhieuDatPhong pdp = new PhieuDatPhong();
                    pdp.setPhong(phong);
                    // (KHÔNG setNhanVien hoặc KhachHang ở đây nữa)
                    // highlight-end

                    double thanhTien = rs.getDouble("tongThanhTien");
                    double donGiaLucDat = phong.getGiaTienMotDem();
                    if (thanhTien == 0) {
                        thanhTien = donGiaLucDat * 1; // Giả sử 1 đêm
                    }

                    ChiTietHoaDon_Phong chiTiet = new ChiTietHoaDon_Phong(hd, pdp, donGiaLucDat, thanhTien);

                    dsChiTiet.add(chiTiet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết phòng theo mã hóa đơn: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return dsChiTiet;
    }
}