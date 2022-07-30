package dungeonmania.entities.movingEntity.enemies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import dungeonmania.DungeonGame;
import dungeonmania.DungeonMap;
import dungeonmania.entities.Entity;
import dungeonmania.util.Direction;
import dungeonmania.util.JSONConfig;
import dungeonmania.util.Position;

public class Spider extends Enemy {


    private boolean isClockwiseMove;
    
    public Spider(String type, Position position, boolean isInteractable) {
        super(type, position, isInteractable);
        this.setMovingStrategy(new CirclingSpawn());
        this.setHealth(JSONConfig.getConfig("spider_health"));
        this.setAttack(JSONConfig.getConfig("spider_attack"));
        this.setNonTraversibles(Arrays.asList("boulder"));
        this.isClockwiseMove = true; // initially true for spider
    }
    
    @Override
    public void move(Enemy movingEntity, DungeonMap map) {
        this.stuckOnSwamp(map);
        if (getRemainingStuckTicks() == 0) {
            getMovingStrategy().move(this, map);  
        } 
    }

    public boolean isClockwiseMove() {
        return isClockwiseMove;
    }

    public void setClockwiseMove(boolean clockwise) {
        this.isClockwiseMove = clockwise;
    }

    // returns the movement position of spiders 
    public int getMovementPosition() {
        return 0;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("movement", getMovementPosition());
        return obj;
    }
    
}