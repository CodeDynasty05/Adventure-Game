package com.OOP.model.items;

import com.OOP.interfaces.Executable;
import com.OOP.model.entities.LivingBeing;

public class Medicine extends Item implements Executable {
    private int healingAmount;

    public Medicine(String id, String name, String description, int healingAmount) {
        super(id, name, description);
        this.healingAmount = healingAmount;
    }

    public int getHealingAmount() {
        return healingAmount;
    }

    @Override
    public void execute(LivingBeing user, LivingBeing target) { // Target is usually the user
        if (user == target) {
            int currentHp = user.getHealthPoints();
            user.setHealthPoints(currentHp + healingAmount);
            System.out.println(user.getName() + " used " + getName() + " and healed for " + healingAmount + " HP. " +
                    "(HP: " + user.getHealthPoints() + "/" + user.getMaxHealthPoints() + ")");
        } else {
            // Could allow healing others if needed
            System.out.println(getName() + " can only be used on oneself.");
        }
    }
}
