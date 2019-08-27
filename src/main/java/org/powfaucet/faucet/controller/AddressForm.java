package org.powfaucet.faucet.controller;

import javax.validation.constraints.*;

public class AddressForm {

    public static final String ADDRESS_REGEX = "^[\\u0020-\\u007F]*$";

    @NotNull(message = "Please fill in target address")
    @Size(min = 8, max = 90, message = "Target address must be between 8 and 90 characters long")
    @Pattern(regexp = ADDRESS_REGEX, message="Target address contains invalid characters")
    private String address;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString() {
        return "AddressForm(Address: " + this.address + ")";
    }
}


