package com.watt.canpay.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MenuItem implements Serializable {

    private String itemName;
    private String itemPrice;
    private int itemQuantity = 0;
    private Boolean isAdded;

    public MenuItem(){}

    public MenuItem(String itemName, String itemPrice) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public String getItemName() {
        return itemName;
    }

/*    public void setItemName(String itemName) {
        this.itemName = itemName;
    }*/

    public String getItemPrice() {
        return itemPrice;
    }

/*    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }*/

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public Boolean getAdded() {
        return isAdded;
    }

    public void setAdded(Boolean added) {
        isAdded = added;
    }
}
