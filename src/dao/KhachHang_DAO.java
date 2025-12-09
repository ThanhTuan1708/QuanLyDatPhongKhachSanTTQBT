package dao;

import connectDB.ConnectDB;
import entity.GioiTinh;
import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class KhachHang_DAO {

    /**
     * Lấy tất cả khách hàng (ĐÃ SỬA LỖI GioiTinh và NgaySinh)
     */
    public List<KhachHang> getAllKhachHang() throws SQLException {
        List<KhachHang> dsKhachHang = new ArrayList<>();

        String sql = """
        SELECT maKH, hoTen, gioiTinh, ngaySinh, cccd, sdt, email, diaChi,
               hangThanhVien, soLanLuuTru, ngayLuuTruGanNhat,
               tongChiTieu, danhGiaTrungBinh
        FROM KhachHang
    """;

        try (Connection con = ConnectDB.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("maKH"));
                kh.setTenKH(rs.getString("hoTen"));
                kh.setCCCD(rs.getString("cccd"));
                kh.setSoDT(rs.getString("sdt"));
                kh.setEmail(rs.getString("email"));
                kh.setDiaChi(rs.getString("diaChi"));

                // --- map dữ liệu mới ---
                kh.setHangThanhVien(rs.getString("hangThanhVien"));
                kh.setSoLanLuuTru(rs.getInt("soLanLuuTru"));
                kh.setTongChiTieu(rs.getDouble("tongChiTieu"));
                kh.setDanhGiaTrungBinh(rs.getDouble("danhGiaTrungBinh"));

                Date lastStay = rs.getDate("ngayLuuTruGanNhat");
                if (lastStay != null)
                    kh.setNgayLuuTruGanNhat(lastStay);

                // --- giới tính ---
                String gt = rs.getString("gioiTinh");
                if ("Nam".equalsIgnoreCase(gt)) kh.setGioiTinh(GioiTinh.NAM);
                else if ("Nữ".equalsIgnoreCase(gt)) kh.setGioiTinh(GioiTinh.NU);
                else kh.setGioiTinh(GioiTinh.KHAC);

                // --- ngày sinh ---
                Date ngSinh = rs.getDate("ngaySinh");
                if (ngSinh != null)
                    kh.setNgaySinh(ngSinh.toLocalDate());

                dsKhachHang.add(kh);
            }
        }
        return dsKhachHang;
    }

    /**
     * Lấy khách hàng bằng ID (ĐÃ SỬA LỖI GioiTinh và NgaySinh)
     */
    public KhachHang getKhachHangById(String maKH) throws SQLException {
        KhachHang kh = null;

        String sql = """
        SELECT 
            maKH, hoTen, gioiTinh, ngaySinh, cccd, sdt, email, diaChi,
            hangThanhVien, soLanLuuTru, ngayLuuTruGanNhat, 
            tongChiTieu, danhGiaTrungBinh
        FROM KhachHang
        WHERE maKH = ?
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, maKH);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    kh = new KhachHang();

                    // Thông tin cơ bản
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("hoTen"));
                    kh.setCCCD(rs.getString("cccd"));
                    kh.setSoDT(rs.getString("sdt"));
                    kh.setEmail(rs.getString("email"));
                    kh.setDiaChi(rs.getString("diaChi"));

                    // Giới tính
                    String gioiTinhString = rs.getString("gioiTinh");
                    GioiTinh gioiTinhEnum = GioiTinh.KHAC;
                    if (gioiTinhString != null) {
                        if (gioiTinhString.equalsIgnoreCase("Nam")) {
                            gioiTinhEnum = GioiTinh.NAM;
                        } else if (gioiTinhString.equalsIgnoreCase("Nữ")) {
                            gioiTinhEnum = GioiTinh.NU;
                        }
                    }
                    kh.setGioiTinh(gioiTinhEnum);

                    // Ngày sinh
                    java.sql.Date sqlNgaySinh = rs.getDate("ngaySinh");
                    if (sqlNgaySinh != null) {
                        kh.setNgaySinh(sqlNgaySinh.toLocalDate());
                    }

                    // === Thông tin mở rộng ===
                    kh.setHangThanhVien(rs.getString("hangThanhVien"));
                    kh.setSoLanLuuTru(rs.getInt("soLanLuuTru"));

                    java.sql.Date sqlLuuTru = rs.getDate("ngayLuuTruGanNhat");
                    if (sqlLuuTru != null) {
                        kh.setNgayLuuTruGanNhat(new java.util.Date(sqlLuuTru.getTime()));
                    }

                    kh.setTongChiTieu(rs.getDouble("tongChiTieu"));
                    kh.setDanhGiaTrungBinh(rs.getDouble("danhGiaTrungBinh"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin khách hàng ID=" + maKH + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return kh;
    }

    // (Hàm add cũ - dùng kết nối riêng)
    public boolean addKhachHang(KhachHang kh) throws SQLException {
        Connection con = ConnectDB.getConnection();
        // Gọi hàm transaction mới, tự động commit
        return addKhachHang(con, kh);
    }

    // highlight-start
    /**
     * THÊM HÀM MỚI ĐỂ DÙNG TRONG TRANSACTION
     * (Sửa: Cho phép các trường NULL)
     */
    public boolean addKhachHang(Connection con, KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (maKH, hoTen, gioiTinh, ngaySinh, cccd, sdt, email, diaChi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getTenKH());
            stmt.setString(3, kh.getGioiTinh().toString()); // Chuyển Enum sang String (Nam/Nữ)

            if (kh.getNgaySinh() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(kh.getNgaySinh()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE); // Cho phép ngaySinh NULL
            }

            if (kh.getCCCD() != null) {
                stmt.setString(5, kh.getCCCD());
            } else {
                stmt.setNull(5, java.sql.Types.NVARCHAR); // Cho phép cccd NULL
            }

            stmt.setString(6, kh.getSoDT());
            stmt.setString(7, kh.getEmail());

            if (kh.getDiaChi() != null) {
                stmt.setString(8, kh.getDiaChi());
            } else {
                stmt.setNull(8, java.sql.Types.NVARCHAR); // Cho phép diaChi NULL
            }

            return stmt.executeUpdate() > 0;
        }
        // Không catch ở đây, để transaction ở EventDatPhong catch
    }
    // highlight-end

    public boolean updateKhachHang(KhachHang kh) throws SQLException {
        String sql = "UPDATE KhachHang SET hoTen = ?, gioiTinh = ?, ngaySinh = ?, cccd = ?, sdt = ?, email = ?, diaChi = ? WHERE maKH = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, kh.getTenKH());
            stmt.setString(2, kh.getGioiTinh().toString());

            if (kh.getNgaySinh() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(kh.getNgaySinh()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setString(4, kh.getCCCD());
            stmt.setString(5, kh.getSoDT());
            stmt.setString(6, kh.getEmail());
            stmt.setString(7, kh.getDiaChi());
            stmt.setString(8, kh.getMaKH());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteKhachHang(String maKH) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE maKH = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH);
            return stmt.executeUpdate() > 0;
        }
    }

    public KhachHang findKhachHangBySdt(String sdt) throws SQLException {
        KhachHang kh = null;
        String sql = "SELECT maKH, hoTen, gioiTinh, ngaySinh, cccd, sdt, email, diaChi FROM KhachHang WHERE sdt = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, sdt);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("hoTen"));
                    String gioiTinhString = rs.getString("gioiTinh");
                    GioiTinh gioiTinhEnum = GioiTinh.KHAC;
                    if (gioiTinhString != null) {
                        if (gioiTinhString.equalsIgnoreCase("Nam")) {
                            gioiTinhEnum = GioiTinh.NAM;
                        } else if (gioiTinhString.equalsIgnoreCase("Nữ")) {
                            gioiTinhEnum = GioiTinh.NU;
                        }
                    }
                    kh.setGioiTinh(gioiTinhEnum);
                    java.sql.Date sqlNgaySinh = rs.getDate("ngaySinh");
                    if (sqlNgaySinh != null) {
                        kh.setNgaySinh(sqlNgaySinh.toLocalDate());
                    }
                    kh.setCCCD(rs.getString("cccd"));
                    kh.setSoDT(rs.getString("sdt"));
                    kh.setEmail(rs.getString("email"));
                    kh.setDiaChi(rs.getString("diaChi"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return kh;
    }

    public int countAllKhachHang() {
        String sql = "SELECT COUNT(*) FROM KhachHang";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double sumTongChiTieu() {
        String sql = "SELECT SUM(tongChiTieu) FROM KhachHang";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double avgDanhGia() {
        String sql = "SELECT AVG(danhGiaTrungBinh) FROM KhachHang";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countKhachHangByTier(String tier) {
        String sql = "SELECT COUNT(*) FROM KhachHang WHERE hangThanhVien = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tier);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}