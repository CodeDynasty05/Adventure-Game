package com.OOP.interfaces;


import com.OOP.model.entities.LivingBeing;

public interface Activatable {
    boolean activate(LivingBeing activator, String itemIdToUse); // itemIdToUse could be key ID or "crowbar"
    String getDescription(); // To describe what it is (e.g., "a sturdy oak door", "a rusty chest")
    boolean isLocked();
}
