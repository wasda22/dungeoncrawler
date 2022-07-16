package dungeonmania;

import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.response.models.*;
import dungeonmania.util.*;
import dungeonmania.util.JSONMap;
import dungeonmania.entities.buildableEntities.*;
import dungeonmania.entities.collectableEntities.*;
import dungeonmania.movingEntity.*;
import dungeonmania.entities.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;


public class DungeonManiaController {

    private DungeonMap map;
    private DungeonGame game;

    public String getSkin() {
        return "default";
    }

    public String getLocalisation() {
        return "en_US";
    }

    /**
     * /dungeons
     */
    public static List<String> dungeons() {
        return FileLoader.listFileNamesInResourceDirectory("dungeons");
    }

    /**
     * /configs
     */
    public static List<String> configs() {
        return FileLoader.listFileNamesInResourceDirectory("configs");
    }

    /**
     * /game/new
     */
    public DungeonResponse newGame(String dungeonName, String configName) throws IllegalArgumentException {

        JSONConfig.setConfig(configName);
        // get initial entities from json dungeon map, create a dungeon map instance of the game and store all initial entities
        InputStream is = FileLoader.class.getResourceAsStream("/dungeons/" + dungeonName + ".json");
        if (is == null) { throw new IllegalArgumentException(); }
        JSONMap jMap = new JSONMap(is);
        
        List<Entity> entities = jMap.getInitialMapEntities();
        map = new DungeonMap(entities, dungeonName);
        // System.out.println("Configissue" + map.getPlayer().getHealth());
        
        List<EntityResponse> entityResponses = map.getEntityResponses();
        game = new DungeonGame(jMap.getGoals(), null, null, null);
        
        return new DungeonResponse(game.getDungeonId(), dungeonName, entityResponses, null, null, null, jMap.getGoals());
    }

    /**
     * /game/dungeonResponseModel
     */
    public DungeonResponse getDungeonResponseModel() {

        Player player = map.getPlayer();
        List<BattleResponse> battles = map.getBattleResponses(game.getBattles());
        return new DungeonResponse(game.getDungeonId(), map.getDungeonName(), map.getEntityResponses(), player.getInventoryResponses(), battles , player.getBuildables(), game.getGoals());
    }

    /**
     * /game/tick/item
     */
    public DungeonResponse tick(String itemUsedId) throws IllegalArgumentException, InvalidActionException {
        if (null == itemUsedId || "".equals(itemUsedId)) {
            throw new InvalidActionException("Not found the item with the given id(" + itemUsedId + ")");
        }
        Player player = map.getPlayer();
        List<Item> inventory = player.getInventory();
        Item targetItem = null;
        for (Item item : inventory) {
            if (itemUsedId.equals(item.getId())) {
                targetItem = item;
                break;
            }
        }

        if (null == targetItem) {
            throw new InvalidActionException("Not found the item with the given id(" + itemUsedId + ")");
        }

        if (!(targetItem instanceof Bomb)
                && !(targetItem instanceof InvincibilityPotion)
                && !(targetItem instanceof InvisibilityPotion)) {
            throw new IllegalArgumentException();
        }

        // firstly, remove the item from the player's inventory
        inventory.remove(targetItem);

        if (targetItem instanceof Bomb) {
            Bomb bomb = (Bomb)targetItem;
            Position newPosition = player.getPosition();
            bomb.setPosition(newPosition);
            bomb.explode(map);
        }

        if (targetItem instanceof InvincibilityPotion) {
            InvincibilityPotion invincibilityPotion = (InvincibilityPotion)targetItem;
            if (player.getCurrPotion().getId().equals(itemUsedId)) {
                player.setInvincible(true);
                invincibilityPotion.updateTicks();
            }
        }

        if (targetItem instanceof InvisibilityPotion) {
            InvisibilityPotion invisibilityPotion = (InvisibilityPotion)targetItem;
            if (player.getCurrPotion().getId().equals(itemUsedId)) {
                player.setInvisible(true);
                invisibilityPotion.updateTicks();
            }
        }

        List<EntityResponse> entityResponses = map.getEntityResponses();
        String goals = game.getGoals();
        DungeonGame dDame = new DungeonGame(goals, inventory, null, null);
        List<ItemResponse> itemResponses = Helper.convertFromItem(inventory);

        for (Entity entity : map.getMapEntities()) {
            if (entity instanceof Enemy) {
                Enemy enemy = (Enemy) entity;
                enemy.getMovingStrategy().move(enemy, map);
            }
        }

        return new DungeonResponse(dDame.getDungeonId(), map.getDungeonName(), entityResponses, itemResponses, null, null, goals);
    }


    /**
     * /game/tick/movement
     */
    public DungeonResponse tick(Direction movementDirection) {
        Player player = map.getPlayer();
        player.move(game, map, movementDirection);
        System.out.println("playerhere" + player.getPosition());
        List<Enemy> enemies = new ArrayList<>();
        for (Entity entity : map.getMapEntities()) {
             if (entity instanceof Enemy) {
                Enemy enemy = (Enemy) entity;
                enemies.add(enemy);
                enemy.getMovingStrategy().move(enemy, map);
            }
        }
        for (Enemy enemy : enemies) {
            System.out.println("Merccccc" + enemy.getMovingStrategy() + enemy.getPosition() + enemy.getType() + "player" + player.getPosition());
            player.interactWithEnemies(enemy, map);
            player.battleWithEnemies(map, game);
        }

        return getDungeonResponseModel();
    }


    /**
     * /game/build
     */
    public DungeonResponse build(String buildable) throws IllegalArgumentException, InvalidActionException {
    
        Player player = map.getPlayer();

        switch(buildable) {
            case "bow":
                Bow bow = new Bow(buildable);
                bow.build(player.getInventory(), player);
                break;

            case "shield":
                Shield shield = new Shield(buildable);
                shield.build(player.getInventory(), player);
                break;
        }

        return getDungeonResponseModel();

    }

    /**
     * /game/interact
     */
    public DungeonResponse interact(String entityId) throws IllegalArgumentException, InvalidActionException {
        // Player player = map.getPlayer();
        // player.interactWithEnemies((Enemy) map.getEntityFromID(entityId), map);
        
        // return getDungeonResponseModel();
        return null;
    }

    //HELPERS DOWN HERE

    
    // public DungeonMap currentDungeonMap(List<Entity> entities, String dungeonName) {
    //     return new DungeonMap(entities, dungeonName);
    // }

    // public DungeonMap getCurrentMap() {
    //     return map;
    // }


}
