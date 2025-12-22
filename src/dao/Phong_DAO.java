package dao;

import connectDB.ConnectDB;
// highlight-start
// SỬA: Import đúng các lớp Entity/Enum mà DAO phụ thuộc trả về
import entity.*;
// highlight-end

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Phong_DAO {

    // Giữ nguyên các DAO này vì chúng ta sẽ dùng kiểu trả về của chúng
    private LoaiPhong_DAO loaiPhongDAO;
    private TrangThaiPhong_DAO trangThaiPhongDAO;

    public Phong_DAO() {
        // Khởi tạo các DAO phụ thuộc (để lấy đối tượng LoaiPhong, TrangThaiPhong)
        // Đảm bảo các lớp DAO này tồn tại và trả về đúng kiểu entity/enum
        try {
            loaiPhongDAO = new LoaiPhong_DAO();
            trangThaiPhongDAO = new TrangThaiPhong_DAO();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo LoaiPhong_DAO hoặc TrangThaiPhong_DAO: " + e.getMessage());
            // Có thể throw lỗi ở đây nếu các DAO này là bắt buộc
        }
    }

    /**
     * Lấy phòng theo mã.
     * (ĐÃ SỬA: Tạo đúng kiểu LoaiPhongEntity/TrangThaiPhongEntity khi tạo đối tượng
     * Phong)
     */
    public Phong getPhongById(String maPhong) throws SQLException {
        Phong p = null;
        Connection con = ConnectDB.getConnection(); // Use getInstance()

        String sql = "SELECT p.*, lp.tenLoaiPhong, ttp.tenTrangThai " + // Select names too
                "FROM Phong p " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "JOIN TrangThaiPhong ttp ON p.maTrangThai = ttp.maTrangThai " +
                "WHERE p.maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maPhong);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // highlight-start
                    // Create the CORRECT Entity types directly from ResultSet
                    LoaiPhongEntity lp = new LoaiPhongEntity(
                            rs.getInt("maLoaiPhong"),
                            rs.getString("tenLoaiPhong"));
                    TrangThaiPhongEntity ttp = new TrangThaiPhongEntity(
                            rs.getInt("maTrangThai"),
                            rs.getString("tenTrangThai"));
                    // highlight-end

                    // Create Phong object using the correct Entity types
                    p = new Phong(
                            rs.getString("maPhong"),
                            rs.getDouble("giaTienMotDem"),
                            rs.getString("moTa"),
                            rs.getInt("soChua"),
                            lp, // Now correctly LoaiPhongEntity
                            ttp // Now correctly TrangThaiPhongEntity
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy phòng theo ID: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow exception
        } finally {
            // Don't close connection from Singleton
        }
        return p;
    }

    /**
     * Lấy tất cả phòng (bao gồm chi tiết loại phòng, trạng thái).
     * (ĐÃ SỬA: Sử dụng đúng kiểu LoaiPhong/TrangThaiPhong khi tạo đối tượng Phong)
     */
    public List<Phong> getAllPhongWithDetails() throws SQLException {
        List<Phong> dsPhong = new ArrayList<>();
        Connection con = ConnectDB.getConnection(); // Sửa: Dùng getInstance()

        String sql = "SELECT p.*, lp.tenLoaiPhong, ttp.tenTrangThai " +
                "FROM Phong p " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "JOIN TrangThaiPhong ttp ON p.maTrangThai = ttp.maTrangThai " +
                "ORDER BY p.maPhong";
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Tạo đối tượng LoaiPhong/TrangThaiPhong từ kết quả JOIN
                LoaiPhongEntity lp = new LoaiPhongEntity(rs.getInt("maLoaiPhong"), rs.getString("tenLoaiPhong")); // <--
                                                                                                                  // SỬA
                                                                                                                  // LẠI
                                                                                                                  // TÊN
                                                                                                                  // LỚP
                TrangThaiPhongEntity ttp = new TrangThaiPhongEntity(rs.getInt("maTrangThai"),
                        rs.getString("tenTrangThai")); // <-- KIỂM TRA VÀ SỬA NẾU CẦN

                Phong p = new Phong(
                        rs.getString("maPhong"),
                        rs.getDouble("giaTienMotDem"),
                        rs.getString("moTa"),
                        rs.getInt("soChua"),
                        lp, // Kiểu LoaiPhong
                        ttp // Kiểu TrangThaiPhong
                );
                dsPhong.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả phòng chi tiết: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Không đóng connection
        }
        return dsPhong;
    }

    /**
     * Cập nhật trạng thái cho một phòng.
     * (Giữ nguyên logic)
     */
    public boolean updatePhongTrangThai(String maPhong, int maTrangThaiMoi) throws SQLException {
        Connection con = ConnectDB.getConnection(); // Sửa: Dùng getInstance()

        String sql = "UPDATE Phong SET maTrangThai = ? WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, maTrangThaiMoi);
            pstmt.setString(2, maPhong);
            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái phòng: " + e.getMessage());
            e.printStackTrace();
            // Không nên return false ở đây, nên throw lỗi hoặc xử lý khác
            throw e;
            // return false; // Trả về false nếu không muốn dừng chương trình
        } finally {
            // Không đóng connection
        }
    }

    /**
     * Thêm phòng mới.
     * (ĐÃ SỬA: Sử dụng đúng getter từ LoaiPhong/TrangThaiPhong)
     */
    public boolean addPhong(Phong p) throws SQLException {
        Connection con = ConnectDB.getConnection(); // Sửa: Dùng getInstance()
        String sql = "INSERT INTO Phong (maPhong, giaTienMotDem, moTa, soChua, maLoaiPhong, maTrangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getMaPhong());
            pstmt.setDouble(2, p.getGiaTienMotDem());
            pstmt.setString(3, p.getMoTa());
            pstmt.setInt(4, p.getSoChua());
            // Lấy mã từ đối tượng LoaiPhong/TrangThaiPhong bên trong Phong
            pstmt.setInt(5, p.getLoaiPhong().getMaLoaiPhong());
            pstmt.setInt(6, p.getTrangThaiPhong().getMaTrangThai());
            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phòng mới: " + e.getMessage());
            e.printStackTrace();
            throw e;
            // return false;
        } finally {
            // Không đóng connection
        }
    }

    /**
     * Lấy danh sách phòng ĐÃ LỌC theo Tên Loại Phòng và/hoặc Số Chứa.
     * (ĐÃ SỬA: Sử dụng đúng kiểu LoaiPhong/TrangThaiPhong khi tạo đối tượng Phong)
     */
    public List<Phong> getFilteredPhong(String tenLoaiPhong, int soChua, Integer tang, Integer maTrangThai,
            java.util.Date tuNgay, java.util.Date denNgay) throws SQLException {
        List<Phong> dsPhong = new ArrayList<>();
        Connection con = ConnectDB.getConnection();

        String sql = "SELECT DISTINCT p.maPhong, p.giaTienMotDem, p.moTa, p.soChua, " +
                "lp.maLoaiPhong, lp.tenLoaiPhong, " +
                "ttp.maTrangThai, ttp.tenTrangThai " +
                "FROM Phong p " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "JOIN TrangThaiPhong ttp ON p.maTrangThai = ttp.maTrangThai " +
                "LEFT JOIN (SELECT DISTINCT maPhong FROM PhieuDatPhong) pdp ON p.maPhong = pdp.maPhong " +
                "WHERE 1=1";

        // Thêm điều kiện lọc theo khoảng thời gian
        if (tuNgay != null && denNgay != null) {
            sql += " AND (p.maPhong NOT IN (" +
                    "SELECT DISTINCT pdp2.maPhong " +
                    "FROM PhieuDatPhong pdp2 " +
                    "WHERE (pdp2.ngayTraPhong > ? AND pdp2.ngayNhanPhong < ?)" +
                    ") OR p.maTrangThai = 1)"; // 1 = Sẵn sàng
        }

        // Thêm điều kiện lọc Loại phòng
        if (tenLoaiPhong != null) {
            sql += " AND lp.tenLoaiPhong = ?";
        }

        // Thêm điều kiện lọc Số người (phòng phải chứa được ÍT NHẤT số người yêu cầu)
        if (soChua > 0) { // Nếu có yêu cầu về số người (> 0)
            sql += " AND p.soChua >= ?";
        }

        // Thêm điều kiện lọc Tầng (P1% = tầng 1, P2% = tầng 2, ...)
        if (tang != null) {
            sql += " AND p.maPhong LIKE ?";
        }

        // Thêm điều kiện lọc Trạng thái
        if (maTrangThai != null) {
            sql += " AND ttp.maTrangThai = ?";
        }

        // DEBUG: In ra câu SQL và giá trị tang
        System.out.println("DEBUG SQL: " + sql);
        System.out.println("DEBUG tang: " + tang + " -> P" + tang + "%");

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int paramIndex = 1;

            // Set các tham số ngày trước
            if (tuNgay != null && denNgay != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(denNgay.getTime())); // ngayTra >= tuNgay
                stmt.setDate(paramIndex++, new java.sql.Date(tuNgay.getTime())); // ngayNhan <= denNgay
            }

            // Tiếp tục với các tham số khác
            if (tenLoaiPhong != null) {
                stmt.setString(paramIndex++, tenLoaiPhong);
            }
            if (soChua > 0) {
                stmt.setInt(paramIndex++, soChua);
            }
            if (tang != null) {
                stmt.setString(paramIndex++, "P" + tang.toString() + "%"); // Tạo P1%, P2%, P3%, P4%
            }
            if (maTrangThai != null) {
                stmt.setInt(paramIndex++, maTrangThai);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Tạo đối tượng LoaiPhong/TrangThaiPhong từ kết quả JOIN
                    LoaiPhongEntity lp = new LoaiPhongEntity(rs.getInt("maLoaiPhong"), rs.getString("tenLoaiPhong")); // <--
                                                                                                                      // SỬA
                                                                                                                      // LẠI
                                                                                                                      // TÊN
                                                                                                                      // LỚP
                    TrangThaiPhongEntity ttp = new TrangThaiPhongEntity(rs.getInt("maTrangThai"),
                            rs.getString("tenTrangThai")); // <-- SỬA LẠI TÊN LỚP

                    // Tạo đối tượng Phong
                    Phong p = new Phong();
                    p.setMaPhong(rs.getString("maPhong"));
                    p.setGiaTienMotDem(rs.getDouble("giaTienMotDem"));
                    p.setMoTa(rs.getString("moTa"));
                    p.setSoChua(rs.getInt("soChua"));
                    p.setLoaiPhong(lp); // Kiểu LoaiPhong
                    p.setTrangThaiPhong(ttp); // Kiểu TrangThaiPhong

                    dsPhong.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lọc phòng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Không đóng connection
        }
        return dsPhong;
    }

    /**
     * Cập nhật thông tin chi tiết của một phòng.
     * (Không cập nhật trạng thái ở đây, dùng updatePhongTrangThai riêng)
     */
    public boolean updatePhong(Phong p) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "UPDATE Phong SET giaTienMotDem = ?, moTa = ?, soChua = ?, maLoaiPhong = ? " +
                "WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDouble(1, p.getGiaTienMotDem());
            pstmt.setString(2, p.getMoTa());
            pstmt.setInt(3, p.getSoChua());
            pstmt.setInt(4, p.getLoaiPhong().getMaLoaiPhong()); //
            pstmt.setString(5, p.getMaPhong());

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật phòng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Xóa một phòng khỏi CSDL (chỉ thành công nếu không có khóa ngoại).
     */
    public boolean deletePhong(String maPhong) throws SQLException {
        Connection con = ConnectDB.getConnection();
        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maPhong);
            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phòng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean updatePhongTrangThai(String maPhong, int maTrangThai, Connection con) throws SQLException {
        // (Hàm này dùng 'con' được truyền vào)
        String sql = "UPDATE Phong SET maTrangThai = ? WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, maTrangThai);
            pstmt.setString(2, maPhong);
            return pstmt.executeUpdate() > 0;
        }
    }
}