package com.oop.gch.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Filters implements Cloneable {
    @Override
    public String toString() {
        return "Filters{" +
                "type='" + type + '\'' +
                ", curfew=" + curfew +
                ", contract=" + contract +
                '}';
    }

    private @Nullable String type;
    private @Nullable Boolean curfew;
    private @Nullable Boolean contract;

    public Filters(@Nullable String type, @Nullable Boolean curfew, @Nullable Boolean contract) {
        this.type = type;
        this.curfew = curfew;
        this.contract = contract;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public Boolean getCurfew() {
        return curfew;
    }

    public void setCurfew(@Nullable Boolean curfew) {
        this.curfew = curfew;
    }

    @Nullable
    public Boolean getContract() {
        return contract;
    }

    public void setContract(@Nullable Boolean contract) {
        this.contract = contract;
    }

    @NonNull
    @Override
    public Filters clone() {
        try {
            return (Filters) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
