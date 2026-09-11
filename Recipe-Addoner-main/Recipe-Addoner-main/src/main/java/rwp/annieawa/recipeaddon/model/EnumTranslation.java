package rwp.annieawa.recipeaddon.model;

import org.bukkit.inventory.EquipmentSlot;

public class EnumTranslation {
    public EquipmentSlot SlotTranslation(String s){
        switch (s){
            case "head":
                return EquipmentSlot.HEAD;
            case "chest":
                return EquipmentSlot.CHEST;
        }
        return null;
    }
}
