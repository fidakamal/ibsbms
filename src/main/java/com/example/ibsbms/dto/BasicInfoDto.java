package com.example.ibsbms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BasicInfoDto {

    @Size(max = 16)
    private String folioBo;

    @NotBlank
    @Size(max = 100)
    private String custName;

    @Size(max = 60)
    private String fatherName;

    @Size(max = 60)
    private String motherName;

    @Size(max = 60)
    private String spouseName;

    @Size(max = 80)
    private String representative;

    private Integer custType;

    private Integer citizenType;

    @NotBlank
    @Size(max = 15)
    private String residentType;

    @Size(max = 11)
    private String phone;

    @Email
    @Size(max = 60)
    private String email;

    private LocalDate dob;

    private Integer isEmployee;

    @Size(max = 17)
    private String nidNo;

    @Size(max = 12)
    private String tinNo;

    @Max(999)
    private Integer icbCode;

    // Getters and setters

    public String getFolioBo() {
        return folioBo;
    }

    public void setFolioBo(String folioBo) {
        this.folioBo = folioBo;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public Integer getCustType() {
        return custType;
    }

    public void setCustType(Integer custType) {
        this.custType = custType;
    }

    public Integer getCitizenType() {
        return citizenType;
    }

    public void setCitizenType(Integer citizenType) {
        this.citizenType = citizenType;
    }

    public String getResidentType() {
        return residentType;
    }

    public void setResidentType(String residentType) {
        this.residentType = residentType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Integer getIsEmployee() {
        return isEmployee;
    }

    public void setIsEmployee(Integer isEmployee) {
        this.isEmployee = isEmployee;
    }

    public String getNidNo() {
        return nidNo;
    }

    public void setNidNo(String nidNo) {
        this.nidNo = nidNo;
    }

    public String getTinNo() {
        return tinNo;
    }

    public void setTinNo(String tinNo) {
        this.tinNo = tinNo;
    }

    public Integer getIcbCode() {
        return icbCode;
    }

    public void setIcbCode(Integer icbCode) {
        this.icbCode = icbCode;
    }
}
