package dao;

import connectDB.ConnectDB;
import entity.NhiemVu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

    public class NhiemVuCaLam_DAO {

        public List<NhiemVu> getNhiemVuTheoMaNV(String maNV) {
            List<NhiemVu> list = new ArrayList<>();

            String sql = """
        SELECT 
            nv.maNhiemVu,
            llv.maLichLam,
            nv.nhiemVu,
            nv.thoiGian,
            nv.trangThai,
            nv.ghiChu
        FROM dbo.NhiemVuCaLam nv
        JOIN dbo.LichLamViec llv 
            ON nv.maLichLam = llv.maLichLam
        WHERE llv.maNV = ?
          AND llv.ngayLam = CAST(GETDATE() AS DATE)
        ORDER BY nv.thoiGian
    """;

            try (Connection con = ConnectDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    // parse thoiGian an toàn
                    String thoiGianStr = rs.getString("thoiGian");
                    java.sql.Time thoiGian = null;
                    if (thoiGianStr != null && !thoiGianStr.isEmpty()) {
                        try {
                            thoiGian = java.sql.Time.valueOf(thoiGianStr); // "HH:mm:ss"
                        } catch (IllegalArgumentException e1) {
                            try {
                                thoiGian = java.sql.Time.valueOf(thoiGianStr + ":00"); // "HH:mm"
                            } catch (IllegalArgumentException e2) {
                                thoiGian = null; // nếu dữ liệu sai định dạng
                            }
                        }
                    }

                    list.add(new NhiemVu(
                            rs.getInt("maNhiemVu"),
                            rs.getInt("maLichLam"),
                            rs.getString("nhiemVu"),
                            thoiGian,
                            rs.getString("trangThai"),
                            rs.getString("ghiChu")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        public List<NhiemVu> getNhiemVuHomNay(String maNV) {
            List<NhiemVu> list = new ArrayList<>();

            String sql = """
SELECT nv.maNhiemVu, nv.maLichLam, nv.nhiemVU, nv.thoiGian, nv.trangThai, nv.ghiChu
FROM NhiemVuCaLam nv
JOIN LichLamViec llv ON nv.maLichLam = llv.maLichLam
WHERE llv.maNV = ?
  AND CAST(llv.ngayLam AS DATE) = CAST(GETDATE() AS DATE)
ORDER BY nv.thoiGian
""";

            try (Connection con = ConnectDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String thoiGianStr = rs.getString("thoiGian");
                    java.sql.Time thoiGian = null;
                    if (thoiGianStr != null && !thoiGianStr.isEmpty()) {
                        try {
                            thoiGian = java.sql.Time.valueOf(thoiGianStr);
                        } catch (IllegalArgumentException e1) {
                            try {
                                thoiGian = java.sql.Time.valueOf(thoiGianStr + ":00");
                            } catch (IllegalArgumentException e2) {
                                thoiGian = null;
                            }
                        }
                    }

                    NhiemVu nv = new NhiemVu(
                            rs.getInt("maNhiemVu"),
                            rs.getInt("maLichLam"),
                            rs.getString("nhiemVu"),
                            thoiGian,
                            rs.getString("trangThai"),
                            rs.getString("ghiChu")
                    );
                    list.add(nv);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        public int getSoGhiChu(String maNV) {
            int count = 0;

            String sql = """
        SELECT COUNT(*) AS soGhiChu
        FROM NhiemVuCaLam nv
        JOIN LichLamViec llv ON nv.maLichLam = llv.maLichLam
        WHERE llv.maNV = ?
          AND nv.ghiChu IS NOT NULL
          AND nv.ghiChu <> ''
          AND llv.ngayLam = CAST(GETDATE() AS DATE)
    """;

            try (Connection con = ConnectDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("soGhiChu");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return count;
        }

    }

