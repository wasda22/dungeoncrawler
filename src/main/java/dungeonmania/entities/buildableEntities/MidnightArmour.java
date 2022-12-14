package dungeonmania.entities.buildableEntities;

import dungeonmania.DungeonMap;
import dungeonmania.entities.collectableEntities.*;
import dungeonmania.entities.movingEntity.player.Player;
import dungeonmania.entities.Item;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.util.JSONConfig;

import java.util.ArrayList;
import java.util.List;

public class MidnightArmour extends Weapon implements ItemBuildable {

    public MidnightArmour(String type) {
        super(type);
        setDamageValue((int) JSONConfig.getConfig("midnight_armour_attack"));
        setDefence((int) JSONConfig.getConfig("midnight_armour_defence"));
    }

    public boolean build(List<Item> inventory, Player player, DungeonMap map) throws InvalidActionException {

        if (map.hasZombies()) {
            throw new InvalidActionException("There are zombies in map");
        }

        // Record all the removing items
        List<Item> removingPosition = new ArrayList<>();

        if (!inventory.isEmpty()) {
            int swordNum = 0;
            int sunStone = 0;

            for (Item item : inventory) {
                if (item instanceof Sword && swordNum < 1) {
                    swordNum++;
                    removingPosition.add(item);
                }

                if (item instanceof SunStone && sunStone < 1) {
                    sunStone++;
                    removingPosition.add(item);
                }
            }

            if ((sunStone == 1) && (swordNum == 1)) {
                removingPosition.forEach(i -> inventory.remove(i));
                player.addToInventory(new MidnightArmour(BUILDABLE_TYPE_MIDNIGHT_ARMOUR));
                return true;
            } else {
                throw new InvalidActionException("Player cannot build midnight armour");
            }
        } else {
            throw new InvalidActionException("Player cannot build midnight armour");
        }
    }

    @Override
    public boolean isUsable() {
        return true;
    }

}
