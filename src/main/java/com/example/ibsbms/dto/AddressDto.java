package com.example.ibsbms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddressDto {

    @NotBlank
    @Size(max = 80)
    private String add1;

    @Size(max = 80)
    private String add2;

    @Size(max = 80)
    private String add3;

    @Size(max = 80)
    private String add4;

    @NotBlank
    @Size(max = 60)
    private String countryName;


    public String getAdd1() {
        return add1;
    }

    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    public String getAdd2() {
        return add2;
    }

    public void setAdd2(String add2) {
        this.add2 = add2;
    }

    public String getAdd3() {
        return add3;
    }

    public void setAdd3(String add3) {
        this.add3 = add3;
    }

    public String getAdd4() {
        return add4;
    }

    public void setAdd4(String add4) {
        this.add4 = add4;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}