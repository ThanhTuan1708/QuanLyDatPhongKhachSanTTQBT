package entity;

import java.util.Objects;

public class Phong {
    private String maPhong;
    private double giaTienMotDem;
    private String moTa;
    private int soChua;
    private LoaiPhongEntity loaiPhong; // Tham chiếu Entity
    private TrangThaiPhongEntity trangThaiPhong;

    // Constructors
    public Phong() {
    }

    public Phong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Phong(String maPhong, double giaTienMotDem, String moTa, int soChua, LoaiPhongEntity loaiPhong, TrangThaiPhongEntity trangThaiPhong) {
        this.maPhong = maPhong;
        this.giaTienMotDem = giaTienMotDem;
        this.moTa = moTa;
        this.soChua = soChua;
        this.loaiPhong = loaiPhong;
        this.trangThaiPhong = trangThaiPhong;
    }


    // Getters and Setters
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public double getGiaTienMotDem() { return giaTienMotDem; }
    public void setGiaTienMotDem(double giaTienMotDem) { this.giaTienMotDem = giaTienMotDem; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public int getSoChua() { return soChua; }
    public void setSoChua(int soChua) { this.soChua = soChua; }
    public LoaiPhongEntity getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhongEntity loaiPhong) { this.loaiPhong = loaiPhong; }
    public TrangThaiPhongEntity getTrangThaiPhong() { return trangThaiPhong; }
    public void setTrangThaiPhong(TrangThaiPhongEntity trangThaiPhong) { this.trangThaiPhong = trangThaiPhong; }

    // equals and hashCode based on maPhong
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phong phong = (Phong) o;
        return Objects.equals(maPhong, phong.maPhong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhong);
    }

    @Override
    public String toString() {
        return "Phong{" + "maPhong='" + maPhong + '\'' + ", loaiPhong=" + loaiPhong + ", trangThaiPhong=" + trangThaiPhong + '}';
    }
}