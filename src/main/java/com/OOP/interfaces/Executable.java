package com.OOP.interfaces;

import com.OOP.model.entities.LivingBeing;

public interface Executable {
    void execute(LivingBeing user, LivingBeing target); // Target could be user themselves (medicine) or enemy (weapon)
}
