package dungeonmania.entities.movingEntity;

import dungeonmania.entities.Entity;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public abstract class MovingEntity extends Entity  {

    private Position initialPosition;
    private Direction direction;
    private double health;
    private double attack;
    private double defence;
    private List<String> nontraversibles = new ArrayList<String>();

    public MovingEntity(String type, Position position, boolean isInteractable) {
        super(type, position, isInteractable);
        this.initialPosition = position;
        this.nontraversibles = null;
    }
    

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    

    public void setInitialPosition(Position initialPosition) {
        this.initialPosition = initialPosition;
    }


    public Position getInitialPosition() {
        return initialPosition;
    }
    
    public boolean blockedBy(List<Entity> atAdj) {
        for (Entity entity : atAdj) {
            if (getNonTraversibles().contains(entity.getType())) {
                return true;
            }
        }
        return false;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;

    }

    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }
    

    public double getDefence() {
        return defence;
    }


    public void setDefence(double defence) {
        this.defence = defence;
    }


    public List<String> getNonTraversibles() {
        return nontraversibles;
    }

    public void setNonTraversibles(List<String> nontraversibles) {
        this.nontraversibles = nontraversibles;
    }
    
    @Override 
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("health", this.getHealth());
        obj.put("interactable", this.getInteractable());
        return obj;
    }

}
