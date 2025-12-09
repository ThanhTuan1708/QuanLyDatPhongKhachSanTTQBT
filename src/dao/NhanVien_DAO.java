package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.GioiTinh;
import entity.LoaiNhanVien;
import entity.NhanVien;

public class NhanVien_DAO {

    private GioiTinh parseGioiTinh(String gioiTinhStr) {
        if (gioiTinhStr == null) return GioiTinh.KHAC;
        switch (gioiTinhStr.trim().toLowerCase()) {
            case "nam": return GioiTinh.NAM;
            case "nữ":
            case "nu": return GioiTinh.NU;
            default: return GioiTinh.KHAC;
        }
    }

    // Lấy toàn bộ danh sách nhân viên từ database
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> dsNhanVien = new ArrayList<>();

        String sql = "SELECT maNV, hoTen, sdt, email, cccd, ngaySinh, gioiTinh, maLoaiNV, matKhau "
                + "FROM NhanVien";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setTenNV(rs.getString("hoTen"));
                nv.setSoDT(rs.getString("sdt"));
                nv.setEmail(rs.getString("email"));
                nv.setCCCD(rs.getString("cccd"));

                // Sửa: Chuyển java.sql.Date sang java.time.LocalDate
                java.sql.Date sqlNgaySinh = rs.getDate("ngaySinh");
                if (sqlNgaySinh != null) {
                    nv.setNgaySinh(sqlNgaySinh.toLocalDate());
                }

                nv.setGioiTinh(parseGioiTinh(rs.getString("gioiTinh")));

                int maLoai = rs.getInt("maLoaiNV");
                nv.setChucVu(LoaiNhanVien.fromId(maLoai)); // Sửa: Dùng Enum

                nv.setMatKhau(rs.getString("matKhau"));

                dsNhanVien.add(nv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dsNhanVien;
    }

    // Đếm tổng số nhân viên
    public int countAllNhanVien() {
        String sql = "SELECT COUNT(*) FROM NhanVien";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm theo loại nhân viên (ví dụ: "LT" hoặc "QL")
    public int countNhanVienByLoai(int maLoaiNV) {
        int count = 0;
        String sql = "SELECT COUNT(*) AS total FROM NhanVien WHERE maLoaiNV = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiNV);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    //  Thêm một nhân viên mới
    public boolean addNhanVien(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (maNV, hoTen, sdt, email, cccd, ngaySinh, gioiTinh, maLoaiNV, matKhau) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getTenNV());
            stmt.setString(3, nv.getSoDT());
            stmt.setString(4, nv.getEmail());
            stmt.setString(5, nv.getCCCD());

            if (nv.getNgaySinh() != null) {
                stmt.setDate(6, java.sql.Date.valueOf(nv.getNgaySinh()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }

            // Sửa: Chuyển Enum sang String
            stmt.setString(7, nv.getGioiTinh() != null ? nv.getGioiTinh().toString() : "Khác");

            stmt.setInt(8, nv.getChucVu() != null ? nv.getChucVu().getId() : 0);
            stmt.setString(9, nv.getMatKhau());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🔹 Xóa nhân viên theo mã
    public boolean deleteNhanVien(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNV = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("Đang thao tác trên DB: " + conn.getCatalog());
            stmt.setString(1, maNV);
            int rows = stmt.executeUpdate();
            System.out.println("Số dòng bị xoá: " + rows);

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🔹 Cập nhật thông tin nhân viên
    public boolean updateNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET hoTen = ?, sdt = ?, email = ?, cccd = ?, "
                + "ngaySinh = ?, gioiTinh = ?, maLoaiNV = ?, matKhau = ? "
                + "WHERE maNV = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nv.getTenNV());
            stmt.setString(2, nv.getSoDT());
            stmt.setString(3, nv.getEmail());
            stmt.setString(4, nv.getCCCD());
            stmt.setDate(5, Date.valueOf(nv.getNgaySinh()));
            stmt.setString(6, nv.getGioiTinh() != null ? nv.getGioiTinh().toString() : "Khác");
            stmt.setInt(7, nv.getChucVu() != null ? nv.getChucVu().getId() : 0);
            stmt.setString(8, nv.getMatKhau());
            stmt.setString(9, nv.getMaNV());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public NhanVien getNhanVienKhiDangNhap(String maNV, String matKhau) throws SQLException {
        NhanVien nv = null;
        Connection con = ConnectDB.getConnection();

        String sql = """
        SELECT maNV, hoTen, sdt, email, cccd, ngaySinh, gioiTinh, maLoaiNV, matKhau
        FROM NhanVien
        WHERE maNV = ? AND matKhau = ?
    """;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            pstmt.setString(2, matKhau);

            System.out.println("Thực hiện truy vấn với maNV=" + maNV + ", matKhau=" + matKhau);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Tìm thấy nhân viên: " + rs.getString("hoTen"));

                    GioiTinh gioiTinhEnum = parseGioiTinh(rs.getString("gioiTinh"));
                    LoaiNhanVien loaiNVEnum = LoaiNhanVien.fromId(rs.getInt("maLoaiNV"));

                    System.out.println("Loại NV: " + loaiNVEnum + ", Giới tính: " + gioiTinhEnum);

                    nv = new NhanVien(
                            rs.getString("maNV"),
                            rs.getString("hoTen"),
                            rs.getString("sdt"),
                            rs.getString("email"),
                            null, //  không có địa chỉ trong DB
                            rs.getString("cccd"),
                            rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null,
                            gioiTinhEnum,
                            loaiNVEnum,
                            null // không trả mật khẩu ra
                    );
                    System.out.println("Đã tạo đối tượng NhanVien thành công");
                } else {
                    System.out.println("Không tìm thấy nhân viên với mã " + maNV);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return nv;
    }

    // highlight-start
    /**
     * Lấy Nhân viên bằng Mã NV
     * (ĐÃ SỬA LỖI: Bỏ LoaiNhanVien_DAO, Sửa lỗi GioiTinh, Sửa lỗi NgaySinh)
     */
    public NhanVien getNhanVienById(String maNV) throws SQLException {
        NhanVien nv = null;
        Connection con = ConnectDB.getConnection();

        // (Không cần LoaiNhanVien_DAO nữa)
        // LoaiNhanVien_DAO loaiNV_DAO = new LoaiNhanVien_DAO();

        String sql = "SELECT * FROM NhanVien WHERE maNV = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setTenNV(rs.getString("hoTen"));

                    // Sửa lỗi NgaySinh
                    java.sql.Date sqlNgaySinh = rs.getDate("ngaySinh");
                    if (sqlNgaySinh != null) {
                        nv.setNgaySinh(sqlNgaySinh.toLocalDate());
                    }

                    // Sửa lỗi GioiTinh
                    nv.setGioiTinh(parseGioiTinh(rs.getString("gioiTinh")));

                    nv.setCCCD(rs.getString("cccd"));
                    nv.setSoDT(rs.getString("sdt"));
                    nv.setEmail(rs.getString("email"));
                    nv.setMatKhau(rs.getString("matKhau"));

                    int maLoaiNV = rs.getInt("maLoaiNV");
                    // highlight-start
                    // *** SỬA: Dùng setChucVu và Enum ***
                    nv.setChucVu(LoaiNhanVien.fromId(maLoaiNV));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên ID=" + maNV + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return nv;
    }
    // highlight-end
}