package dungeonmania.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.DungeonGame;
import dungeonmania.DungeonMap;

public class JSONSaveGame {
    // save files to "./bin/saved_games/" + <fileName> + ".json" 
    // save files to "/dungeons/" + <fileName> + ".json" 

    public static JSONObject saveGame(DungeonMap map, JSONObject goalsJSON, DungeonGame game) {
        JSONObject timeTravel = new JSONObject();
        JSONObject config = new JSONObject();
        JSONObject tick = new JSONObject();
        JSONArray entities = map.mapEntitiesToJSON();
        JSONArray inventory = map.getPlayer().inventoryToJSON();
        JSONArray potions = map.getPlayer().potionQueueToJSON();
        JSONArray battles = new JSONArray();

        config.put("file_name", JSONConfig.getConfigName());
        int currTick = game.getCurrentTick();
        tick.put("current_tick", currTick);

        // time travel 
        List<JSONObject> histories = game.getTickHistory();
        JSONArray historyJSON = new JSONArray();
        for (JSONObject hist : histories) {
            historyJSON.put(hist);
        }
        int timeTravelTick = game.getTimeTravelTick();
        timeTravel.put("time_travel_tick", timeTravelTick);
        timeTravel.put("tick_Histories", historyJSON);

        // battle queue to be confirmed 

        // combining
        JSONObject gameJSON = new JSONObject();
        gameJSON.put("tick", tick);
        gameJSON.put("entities", entities);
        gameJSON.put("goal-condition", goalsJSON);
        gameJSON.put("inventory", inventory);
        gameJSON.put("potion-queue", potions);
        gameJSON.put("time-travel", timeTravel);
        gameJSON.put("battle-queue", battles);
        gameJSON.put("config-file", config);

        return gameJSON; 
    }
}