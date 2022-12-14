package dungeonmania.entities.StaticEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import dungeonmania.DungeonGame;
import dungeonmania.DungeonMap;
import dungeonmania.entities.Entity;
import dungeonmania.entities.StaticEntity;
import dungeonmania.entities.movingEntity.enemies.ZombieToast;
import dungeonmania.util.JSONConfig;
import dungeonmania.util.Position;

public class ZombieToastSpawner extends StaticEntity {

    public ZombieToastSpawner(String type, Position position, boolean isInteractable) {
        super(type, position, isInteractable);
        setTraversable(true);
    }
    
    /**
     * Method to spawn zombie
     * @param currentTick curent tick 
     * @param spawnTick zombe_spawn_rate, read in from the config file
     */
    public ZombieToast spawnZombie(DungeonGame game) {
        DungeonMap map = game.getMap();
        int currentTick = game.getCurrentTick();
        int spawnrate = (int) JSONConfig.getConfig("zombie_spawn_rate");
        if (spawnrate == 0 ) { return null;}
        if (!(currentTick % spawnrate == 0)) { return null;}

        List<Position> cardinallyAdj = this.getPosition().getCardinallyAdjacentPositions();
        List<Position> possibleSpawns = new ArrayList<Position>();
        if (cardinallyAdj != null && cardinallyAdj.size() != 0) {
            for (Position pos : cardinallyAdj) {
                List<Entity> atAdj = map.getEntityFromPos(pos);
                if (atAdj == null || !containsWall(atAdj)) {
                    possibleSpawns.add(pos);
                }
            }
        }

        if (possibleSpawns != null && possibleSpawns.size() > 0) {
            
            return new ZombieToast("zombie_toast", getRandomPosition(possibleSpawns), true);
        }

        return null;

    }

    public Position getRandomPosition(List<Position> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public boolean containsWall(List<Entity> entities) {
        boolean found = entities.stream().anyMatch(entity -> entity.getType().equals("wall"));
        return found;    
    }

    @Override
    public void tick(DungeonGame game) {
        DungeonMap map = game.getMap();
        ZombieToast zombie = spawnZombie(game);
        if (zombie != null) {
            map.addEnemyToSpawn(zombie);
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("interactable", getInteractable());
        return obj;
    }
}
