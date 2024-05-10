package com.oop.gch.model.user;

import androidx.annotation.Nullable;

/**
 * See {@link com.oop.gch.auth.BuyerSignUpFragment#onCreateView}
 */
public class Buyer {

    private String name;
    private int age;
    private String phone;
    private String Guardian_name;
    private String Guardian_phone;
    private int Year_Level;
    private String program;

    public Buyer (String name, int age, String phone, String Guardian_name, String Guardian_phone, int Year_Level, String program) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.Guardian_name = Guardian_name;
        this.Guardian_phone = Guardian_phone;
        this.Year_Level = Year_Level;
        this.program = program;
    }

    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public String getPhone() {
        return phone;
    }
    public String getGuardianName() {
        return Guardian_name;
    }
    public String getGuardianPhone() {
        return Guardian_phone;
    }
    public int getYearLevel() {
        return Year_Level;
    }
    public String getProgram() {
        return program;
    }
}
