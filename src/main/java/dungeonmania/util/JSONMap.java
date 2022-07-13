package dungeonmania.util;

import dungeonmania.Entity;
import dungeonmania.StaticEntities.*;
import dungeonmania.movingEntity.*;
import dungeonmania.entities.buildableEntities.*;
import dungeonmania.entities.collectableEntities.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import netscape.javascript.JSObject;
import java.io.InputStream;

public class JSONMap {

    private ArrayList<Entity> initialMapEntities = new ArrayList<Entity>();
    private String goals;

    public JSONMap(InputStream is) {

        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);

        // complex goals are not handled yet
        goals = object.getJSONObject("goal-condition").toString();

        JSONArray entitiesJSON = object.getJSONArray("entities");
        for (int i = 0; i < entitiesJSON.length(); i++) {
            JSONObject obj = entitiesJSON.getJSONObject(i);
            String type = obj.getString("type");
            int x = obj.getInt("x");
            int y = obj.getInt("y");
            Position pos = new Position(x,y);
            initialiseMapEntities(type, pos, obj);
        }
        
    }

    private void initialiseMapEntities(String type, Position position, JSONObject obj) {
        Entity entity = null;
        switch (type) {
            case "player":
                entity = new Player(type, position, true); break;
            case "wall":
                entity = new Wall(type, position); break;
            case "exit":
                entity = new Exit(type, position); break;
            case "boulder":
                entity = new Boulder(type, position); break;
            case "switch":
                entity = new FloorSwitch(type, position); break;
            case "door":
                entity = new Door(type, position, obj.getInt("key")); break;
            case "portal":
                entity = new Portal(type, position, obj.getString("colour")); break;
            case "zombie_toast_spawner":
                entity = new ZombieToastSpawner(type, position); break;
            case "spider":
                entity = new Spider(type, position, true); break;
            case "zombie_toast":
                entity = new ZombieToast(type, position, true); break;
            case "mercenary":
                entity = new Mercenary(type, position, true); break;
            case "treasure":
                entity = new Treasure(type, position); break;
            case "key":
                entity = new Key(type, position, obj.getInt("key")); break;
            case "invincibility_potion":
                entity = new InvincibilityPotion(type, position); break;
            case "invisibility_potion":
                entity = new InvisibilityPotion(type, position); break;
            case "wood":
                entity = new Wood(type, position); break;
            case "arrow":
                entity = new Arrows(type, position); break;
            case "bomb":
                entity = new Bomb(type, position); break;
            case "sword":
                entity = new Sword(type, position); break;
            case "bow":
                entity = new Bow(type, position); break;
            case "shield":
                entity = new Shield(type, position); break;
        }
        initialMapEntities.add(entity);
    }

    public List<Entity> getInitialMapEntities() {
        return initialMapEntities;
    }

    public String getGoals() {
        return goals;
    }
 
}