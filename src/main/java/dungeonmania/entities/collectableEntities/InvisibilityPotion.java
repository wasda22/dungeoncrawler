package dungeonmania.entities.collectableEntities;

import dungeonmania.util.JSONConfig;
import dungeonmania.util.Position;

public class InvisibilityPotion extends Potion {

    public InvisibilityPotion(String type, Position position) {
        super(type, position);
        this.setPotionDuration(JSONConfig.getConfig("invisibility_potion_duration"));
    }
}
