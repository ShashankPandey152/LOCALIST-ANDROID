package com.vastukosh.android.localist.DataStructures;

public class AllItems {

    public String itemName, storeName;

    public AllItems(String name, String store) {
        itemName = name;
        storeName = store;
    }

    public String getItemName() {
        return itemName;
    }

    public String getStoreName() {
        return storeName;
    }
}
