package com.oop.gch.model.user;

/**
 * See {@link com.oop.gch.auth.SellerSignUpFragment#onCreateView}
 */
public class Seller {
    private String name;

    public Seller(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
