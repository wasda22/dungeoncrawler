package dungeonmania.movingEntity;

import dungeonmania.util.Position;

public abstract class Enemy extends MovingEntity {

    private MovingStrategy movingStrategy;

    public Enemy(String type, Position position, boolean isInteractable) {
        super(type, position, isInteractable);
    }

    public MovingStrategy getMovingStrategy() {
        System.out.println(movingStrategy);
        return movingStrategy;
    }

    public void setMovingStrategy(MovingStrategy movingStrategy) {
        this.movingStrategy = movingStrategy;
    }

    public boolean becomeAlly() {
        return false;
    }
    
}
