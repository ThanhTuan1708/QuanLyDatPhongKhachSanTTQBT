package dao;

import connectDB.ConnectDB;
import entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatPhong_DAO {

    public PhieuDatPhong_DAO() {
        // Constructor
    }

    /**
     * Thêm một phiếu đặt phòng mới (DÙNG CHO TRANSACTION)
     * (SỬA: Đã xóa cột 'maNV' để khớp CSDL)
     */
    public boolean addPhieuDatPhong(PhieuDatPhong pdp, String trangThai, Connection con) throws SQLException {
        // SỬA: Thêm cột trangThai được truyền vào tham số
        String sql = "INSERT INTO PhieuDatPhong (maPhieu, ngayDatPhong, ngayNhanPhong, ngayTraPhong, maKH, maPhong, trangThai) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, pdp.getMaPhieu());
            pstmt.setTimestamp(2, Timestamp.valueOf(pdp.getNgayDatPhong()));
            pstmt.setTimestamp(3, Timestamp.valueOf(pdp.getNgayNhanPhong()));
            pstmt.setTimestamp(4, Timestamp.valueOf(pdp.getNgayTraPhong()));
            // Dòng maNV (số 5 cũ) ĐÃ BỊ XÓA
            pstmt.setString(5, pdp.getKhachHang().getMaKH());
            pstmt.setString(6, pdp.getPhong().getMaPhong());
            pstmt.setString(7, trangThai);

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phiếu đặt phòng (transaction): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean addPhieuDatPhong(PhieuDatPhong pdp, String trangThai) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "INSERT INTO PhieuDatPhong (maPhieu, ngayDatPhong, ngayNhanPhong, ngayTraPhong, maKH, maPhong, trangThai) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, pdp.getMaPhieu());
            pstmt.setTimestamp(2, Timestamp.valueOf(pdp.getNgayDatPhong()));
            pstmt.setTimestamp(3, Timestamp.valueOf(pdp.getNgayNhanPhong()));
            pstmt.setTimestamp(4, Timestamp.valueOf(pdp.getNgayTraPhong()));
            pstmt.setString(5, pdp.getKhachHang().getMaKH());
            pstmt.setString(6, pdp.getPhong().getMaPhong());
            pstmt.setString(7, trangThai);

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phiếu đặt phòng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Xóa phiếu đặt phòng theo mã phiếu.
     */
    public boolean deletePhieuDatPhong(String maPhieu) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "DELETE FROM PhieuDatPhong WHERE maPhieu = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maPhieu);
            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phiếu đặt phòng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // SỬA: Hàm Check-in cập nhật cả trạng thái Phòng và Phiếu
    public boolean checkIn(String maPhieu, int maTrangThaiDaThue) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String maPhong = findMaPhongByMaPhieu(con, maPhieu);
        if (maPhong == null)
            return false;

        String sqlUpdatePhieu = "UPDATE PhieuDatPhong SET trangThai = N'Đã nhận phòng' WHERE maPhieu = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sqlUpdatePhieu)) {
            // 1. Cập nhật trạng thái Phiếu -> "Đã nhận phòng"
            pstmt.setString(1, maPhieu);
            int n1 = pstmt.executeUpdate();

            // 2. Cập nhật trạng thái Phòng -> "Đang thuê" (qua Phong_DAO)
            Phong_DAO localPhongDAO = new Phong_DAO();
            boolean n2 = localPhongDAO.updatePhongTrangThai(maPhong, maTrangThaiDaThue);

            return n1 > 0 && n2;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // SỬA: Hàm Check-out cập nhật cả trạng thái Phòng và Phiếu
    public boolean checkOut(String maPhieu, int maTrangThaiDangDon) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String maPhong = findMaPhongByMaPhieu(con, maPhieu);
        if (maPhong == null)
            return false;

        // Cập nhật trạng thái phiếu và NGÀY TRẢ THỰC TẾ (nếu cần tính giờ)
        String sqlUpdatePhieu = "UPDATE PhieuDatPhong SET trangThai = N'Đã trả phòng' WHERE maPhieu = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sqlUpdatePhieu)) {
            // 1. Cập nhật trạng thái Phiếu -> "Đã trả phòng"
            pstmt.setString(1, maPhieu);
            int n1 = pstmt.executeUpdate();

            // 2. Cập nhật trạng thái Phòng -> "Đang dọn"
            Phong_DAO localPhongDAO = new Phong_DAO();
            boolean n2 = localPhongDAO.updatePhongTrangThai(maPhong, maTrangThaiDangDon);

            return n1 > 0 && n2;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Helper: Tìm mã phòng dựa trên mã phiếu.
     */
    private String findMaPhongByMaPhieu(Connection con, String maPhieu) throws SQLException {
        String maPhong = null;
        String sql = "SELECT maPhong FROM PhieuDatPhong WHERE maPhieu = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maPhieu);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    maPhong = rs.getString("maPhong");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm mã phòng từ mã phiếu: " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lỗi ra ngoài
        }
        return maPhong;
    }

    public List<Object[]> getFilteredBookingData(String searchText, String selectedFilterUI) throws SQLException {
        List<Object[]> dataList = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // SỬA 1: Thêm pdp.trangThai vào câu SELECT
        String sql = "SELECT kh.hoTen, kh.sdt, p.maPhong, pdp.ngayNhanPhong, pdp.ngayTraPhong, " +
                "pdp.maPhieu, pdp.trangThai, kh.maKH " + // <-- Lấy pdp.trangThai thay vì ttp.tenTrangThai
                "FROM PhieuDatPhong pdp " +
                "JOIN KhachHang kh ON pdp.maKH = kh.maKH " +
                "JOIN Phong p ON pdp.maPhong = p.maPhong " +
                "WHERE 1=1";

        // SỬA 2: Lọc theo trạng thái của PHIẾU ĐẶT PHÒNG (dùng Params để tránh lỗi
        // Unicode)
        boolean hasStatusFilter = (selectedFilterUI != null && !selectedFilterUI.equals("Tất cả"));
        if (hasStatusFilter) {
            sql += " AND pdp.trangThai = ?";
        }

        // Lọc theo từ khóa tìm kiếm
        boolean hasSearch = (searchText != null && !searchText.isEmpty());
        if (hasSearch) {
            sql += " AND (kh.hoTen LIKE ? OR kh.sdt LIKE ? OR p.maPhong LIKE ? OR pdp.maPhieu LIKE ?)";
        }

        // Sắp xếp: Phiếu mới nhất lên đầu
        sql += " ORDER BY pdp.ngayNhanPhong DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int paramIndex = 1;

            if (hasStatusFilter) {
                stmt.setString(paramIndex++, selectedFilterUI); // Driver thường tự xử lý Unicode với setString
            }

            if (hasSearch) {
                String searchPattern = "%" + searchText + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tenKH = rs.getString("hoTen");
                    String sdt = rs.getString("sdt");
                    String maPhong = rs.getString("maPhong");
                    Timestamp tsNhan = rs.getTimestamp("ngayNhanPhong");
                    Timestamp tsTra = rs.getTimestamp("ngayTraPhong");
                    String maPhieu = rs.getString("maPhieu");
                    String maKH = rs.getString("maKH");

                    // SỬA 3: Lấy trạng thái trực tiếp từ DB
                    String trangThaiPhieu = rs.getString("trangThai");

                    // Xử lý ngày tháng an toàn null
                    String ngayNhanStr = (tsNhan != null) ? tsNhan.toLocalDateTime().format(dtf) : "";
                    String ngayTraStr = (tsTra != null) ? tsTra.toLocalDateTime().format(dtf) : "";

                    // Mapping trạng thái sang ID để UI xử lý màu sắc (nếu cần)
                    int statusUI = 0;
                    if (trangThaiPhieu != null) {
                        if (trangThaiPhieu.equalsIgnoreCase("Đã xác nhận"))
                            statusUI = 1;
                        else if (trangThaiPhieu.equalsIgnoreCase("Đã nhận phòng"))
                            statusUI = 2;
                        else if (trangThaiPhieu.equalsIgnoreCase("Đã trả phòng"))
                            statusUI = 3;
                    }

                    Object[] row = {
                            tenKH, sdt, maPhong, ngayNhanStr, ngayTraStr, maPhieu,
                            trangThaiPhieu, // Hiển thị text trạng thái đúng từ DB
                            statusUI,
                            maKH
                    };
                    dataList.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi filter: " + e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }

    public int demCheckInHomNay() {
        String sql = """
                    SELECT COUNT(*)
                    FROM PhieuDatPhong
                    WHERE CAST(ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE)
                      AND trangThai = N'Đã xác nhận'
                """;

        try (var con = ConnectDB.getConnection();
                var ps = con.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int demCheckOutHomNay() {
        String sql = """
                    SELECT COUNT(*)
                    FROM PhieuDatPhong
                    WHERE CAST(ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE)
                      AND trangThai = N'Đã nhận phòng'
                """;

        try (var con = ConnectDB.getConnection();
                var ps = con.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

}