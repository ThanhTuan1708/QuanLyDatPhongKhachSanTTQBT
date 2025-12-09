package dao;

import connectDB.ConnectDB;
import entity.LichLamViec;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichLamViec_DAO {

    public List<LichLamViec> getLichLamTheoMaNV(String maNV) {
        List<LichLamViec> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM LichLamViec WHERE maNV = ? ORDER BY ngayLam";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LichLamViec llv = new LichLamViec(
                        rs.getString("maNV"),
                        rs.getDate("ngayLam"),
                        rs.getString("caLam"),
                        rs.getTime("gioBatDau"),
                        rs.getTime("gioKetThuc"),
                        rs.getString("gioCong"),
                        rs.getString("tangCa"),
                        rs.getString("nhiemVu"),
                        rs.getString("thoiGianNV"),
                        rs.getString("trangThai"),
                        rs.getString("ghiChu"),
                        rs.getString("trangThaiNhiemVu")
                );
                ds.add(llv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    //tổng giờ làm
    public int tinhTongGioLamTrongTuan(String maNV) {
        int tongGio = 0;
        try {
            Connection con = ConnectDB.getConnection();

            String sql = """
            DECLARE @today DATE = CAST(GETDATE() AS DATE);
            DECLARE @weekday INT = DATEPART(WEEKDAY, @today);
            DECLARE @daysFromMonday INT;
            IF @weekday = 1 
                SET @daysFromMonday = -6;
            ELSE 
                SET @daysFromMonday = 2 - @weekday;
            DECLARE @thuHai DATE = DATEADD(DAY, @daysFromMonday, @today);

            SELECT SUM(TRY_CAST(REPLACE(REPLACE(gioCong, N'giờ', ''), ' ', '') AS INT)) AS tongGio
            FROM LichLamViec
            WHERE maNV = ?
              AND ngayLam BETWEEN @thuHai AND DATEADD(DAY, 6, @thuHai)
              AND caLam <> N'Nghỉ';
        """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tongGio = rs.getInt("tongGio");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tongGio;
    }

    //thống kê ca làm
    public int[] getThongKeCaTuan(String maNV) {
        int[] result = new int[2]; // [0] = caHoanThanh, [1] = tongCa (5)

        String sql = """
DECLARE @today DATE = CAST(GETDATE() AS DATE);

-- Tính ngày thứ Hai của tuần hiện tại (độc lập với DATEFIRST)
DECLARE @thuHai DATE = DATEADD(DAY, -((DATEPART(WEEKDAY, @today) + @@DATEFIRST - 2) % 7), @today);

SELECT 
    SUM(CASE WHEN trangThai = N'Hoàn thành' THEN 1 ELSE 0 END) AS caHoanThanh
FROM LichLamViec
WHERE maNV = ?
  AND ngayLam BETWEEN @thuHai AND DATEADD(DAY, 4, @thuHai); -- Thứ 2 -> Thứ 6
""";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result[0] = rs.getInt("caHoanThanh");
                result[1] = 5; // tổng số ca trong tuần
            }

            // Debug in ra để xem phạm vi tính
            System.out.println("Ca hoàn thành: " + result[0] + "/5");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    //đếm số yêu cầu
    public int getSoGhiChu(String maNV) {
        int count = 0;
        String sql = "SELECT COUNT(*) AS soGhiChu FROM LichLamViec WHERE maNV = ? AND ghiChu IS NOT NULL AND ghiChu <> ''";

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


    //đếm giờ tăng ca
    public int getTongGioTangCaInt(String maNV) {
        int tongGio = 0;
        String sql = """
        SELECT SUM(
            DATEDIFF(MINUTE, 
                CAST(LEFT(tangCa,5) AS TIME),
                CAST(RIGHT(tangCa,5) AS TIME)
            ) / 60
        ) AS tongGioTangCa
        FROM LichLamViec
        WHERE maNV = ? AND tangCa IS NOT NULL AND tangCa <> ''
    """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tongGio = rs.getInt("tongGioTangCa");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tongGio;
    }


}