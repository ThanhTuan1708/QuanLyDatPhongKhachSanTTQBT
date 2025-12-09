package entity;

import java.util.Objects;

public class DichVu {
    private String maDV;
    private String tenDV;
    private double giaTien;
    private String moTa;

    // Constructors
    public DichVu() {
    }

    public DichVu(String maDV) {
        this.maDV = maDV;
    }

    public DichVu(String maDV, String tenDV, double giaTien, String moTa) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.giaTien = giaTien;
        this.moTa = moTa;
    }

    // Getters and Setters
    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }
    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }
    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    // equals and hashCode based on maDV
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVu dichVu = (DichVu) o;
        return Objects.equals(maDV, dichVu.maDV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDV);
    }

    @Override
    public String toString() {
        return "DichVu{" + "maDV='" + maDV + '\'' + ", tenDV='" + tenDV + '\'' + '}';
    }
}