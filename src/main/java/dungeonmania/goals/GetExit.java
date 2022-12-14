package dungeonmania.goals;

import dungeonmania.DungeonMap;
import dungeonmania.entities.Entity;
import dungeonmania.util.Position;

import java.util.List;

import org.json.JSONObject;

public class GetExit extends LeafGoal {

    // public GetExit(DungeonMap map) {
    //     map.setRemainingConditions(1);
    // }

    public GetExit() {
    }
    
    public GetExit(DungeonMap map, boolean prevIsAchieved) {
        super(map, prevIsAchieved);
        map.setRemainingConditions(1);
    }

    @Override
    public boolean isAchieved(DungeonMap map) {
        if (map.getRemainingConditions() > 1 || map.getPlayer() == null) { 
            return false; 
        }

        Position playerPosition = map.getPlayer().getPosition();

        List<Entity> hasExit = map.getEntitiesFromType(map.getMapEntities(), "exit");
        if (hasExit.size() == 0) { return false; }
        
        Position exitPosition = hasExit.get(0).getPosition();                           
        if (playerPosition.equals(exitPosition)) {
            map.setRemainingConditions(-1);
            return true;
        }
        return false;
    }

    @Override
    public String getGoalsAsString(DungeonMap map) {
        if (isAchieved(map)) { return ""; }
        return ":exit";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("goal", "exit");
        obj.put("prev_is_achieved", getPrevIsAchieved());
        return obj;
    }

}
