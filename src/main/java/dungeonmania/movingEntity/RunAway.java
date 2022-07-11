package dungeonmania.movingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dungeonmania.DungeonMap;
import dungeonmania.Entity;
import dungeonmania.util.Position;

public class RunAway implements MovingStrategy{

    // Mercenaries and Zombies will run away from the Player when they are invincible.

    // see forum post, basically observe player location (x,y)
    // calculate distance (absolute difference) between movable positions of emeny and current player position
    // filter a list of movable positions that increase distance, and choose one from the list randomly
    // if no movable positions, enemy simply remain still

    @Override
    public void move(MovingEntity movingEntity, DungeonMap map) {
        // change to observer pattern later?
        Player player = map.getPlayer();
        // if player invisible, move randomly
        Position playerPos = player.getPosition();
        Position currPos = movingEntity.getPosition();
        List<Position> adjPos = currPos.getCardinallyAdjacentPositions();
        List<Position> moveablePos = new ArrayList<Position>();
        for (Position pos : adjPos) {
            if (pos.getDistanceBetween(playerPos) > currPos.getDistanceBetween(playerPos)) {
                List<Entity> atAdj = map.getEntityFromPos(pos);
                if (atAdj.size() == 0 || !movingEntity.blockedBy(atAdj)) {
                    moveablePos.add(pos);
                }     
            }
        }
        if (moveablePos.size() == 0) {
            return;
        }
        movingEntity.setPosition(getRandomPosition(moveablePos));
        
    }

    // Randomly select items from a List in Java
    // Reference: https://www.geeksforgeeks.org/randomly-select-items-from-a-list-in-java/

    public Position getRandomPosition(List<Position> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
    
}