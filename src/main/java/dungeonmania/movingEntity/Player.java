package dungeonmania.movingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.DungeonGame;
import dungeonmania.DungeonMap;
import dungeonmania.Entity;
import dungeonmania.StaticEntities.*;
import dungeonmania.entities.*;
import dungeonmania.entities.buildableEntities.*;
import dungeonmania.entities.collectableEntities.*;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.response.models.ItemResponse;
import dungeonmania.util.Battle;
import dungeonmania.util.Direction;
import dungeonmania.util.JSONConfig;
import dungeonmania.util.Position;
import dungeonmania.util.PotionQueue;
import dungeonmania.util.Round;

public class Player extends MovingEntity {

    private boolean isInvisible;
    private boolean isInvincible;
    private Position prevPosition;
    private int wealth;
    private PlayerState state;
    private List<Item> inventory = new ArrayList<Item>();
    private List<Enemy> battleQueue = new ArrayList<Enemy>();
    private PotionQueue potionQueue = new PotionQueue();
    private Potion currPotion = null;
    private Key currKey = null;
    private boolean playerWin = false;
    private boolean playerDied = false;
    private int slayedEnemy = 0;

    public Player(String type, Position position, boolean isInteractable) {
        super(type, position, isInteractable);
        this.prevPosition = null;
        this.setHealth(JSONConfig.getConfig("player_health"));
        this.setAttack(JSONConfig.getConfig("player_attack"));
        this.wealth = 0; // initially has not collected any treasure
        this.setState(new PlayerDefaultState());
        state.playerStateChange(this);
    }

    
    public boolean isPlayerDied() {
        return playerDied;
    }


    public void setPlayerDied(boolean playerDied) {
        this.playerDied = playerDied;
    }


    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean isInvisible) {
        this.isInvisible = isInvisible;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean isInvincible) {
        this.isInvincible = isInvincible;
    }

    public Position getPrevPosition() {
        return prevPosition;
    }

    public void setPrevPosition() {
        Direction newD = null;
        // can we use equals for comparing direction
        if (getDirection().equals(Direction.UP)) {
            newD = Direction.DOWN;
        } else if (getDirection().equals(Direction.DOWN)) {
            newD = Direction.UP;
        } else if (getDirection().equals(Direction.LEFT)) {
            newD = Direction.RIGHT;
        } else if (getDirection().equals(Direction.RIGHT)) {
            newD = Direction.LEFT;
        }
        this.prevPosition = getPosition().translateBy(newD);

    }

    

    public Potion getCurrPotion() {
        return currPotion;
    }


    public void setCurrPotion(Potion currPotion) {
        this.currPotion = currPotion;
    }


    public Key getCurrKey() {
        for (Item item : inventory) {
            if (item instanceof Key) {
                return (Key) item;
            }
        }
        return null;
    }


    public void setCurrKey(Key currKey) {
        this.currKey = currKey;
    }


    public boolean isPlayerWin() {
        return playerWin;
    }


    public void setPlayerWin(boolean playerWin) {
        this.playerWin = playerWin;
    }


    public int getWealth() {
        int totalTreasure = (int) inventory.stream().filter(i -> i instanceof Treasure).count();
        
        return totalTreasure;
    }

    public int getSlayedEnemy(){
        return slayedEnemy;
    }


    public void setSlayedEnemy(int slayedEnemy) {
        this.slayedEnemy = slayedEnemy;
    }

    public boolean hasEnoughToBribe() {
        boolean enoughWealth = false;
        if (this.wealth >= JSONConfig.getConfig("bribe_amount")) {
            enoughWealth = true;
        }
        return enoughWealth;
    }

    

    public void setBattleQueue(List<Enemy> battleQueue) {
        this.battleQueue = battleQueue;
    }


    public PlayerState getState() {
        return state;
    }


    public void setState(PlayerState state) {
        this.state = state;
    }


    public List<Item> getInventory() {
        return inventory;
    }


    public void setInventory(List<Item> inventory) {
        this.inventory = inventory;
    }


    public List<Enemy> getBattleQueue() {
        return battleQueue;
    }

    public void move(DungeonGame game, DungeonMap map, Direction direction) {
        System.out.println("entered move");


        boolean blocked = false;

        this.setDirection(direction);
        //System.out.println("Pos: " + getPosition() + "direction: " + direction);
        Position newPos = getPosition().translateBy(direction);
        //System.out.println("newPos: " + newPos);
        List<Entity> encounters = map.getEntityFromPos(newPos);

        // interact with non-moving entities 
        for (Entity encounter : encounters) {

            if (!isInvisible() && !(encounter instanceof Enemy)) {
                blocked = interactWithEntities(encounter, map, direction);
            }
            if (getNonTraversibles().contains(encounter.getType())) {
                blocked = true;
            }
        }

        if (!blocked) {
            this.setPosition(newPos);
        }
    }


    public boolean interactWithEntities(Entity entity, DungeonMap map, Direction direction) {
        boolean interfereByEntity = false;
        // create interact method in each entity
        if (entity instanceof Boulder) {
            interfereByEntity = pushBoulder(map, direction);
        } else if (entity instanceof Exit) {
            // remove exit from goals 
            // remove player from map entities 
        } else if (entity instanceof Item) {
            collectToInventory((Item) entity, map);
        } else if (entity instanceof Door) {
            // check if door is already opened 
            // check if corresponding key is in inventory 
            currKey = getCurrKey();
            if (currKey != null) {
                Door door = (Door) entity;
                door.unlockDoor(currKey);
                inventory.remove(currKey);
            }
        } else if (entity instanceof Portal) {
            interfereByEntity = teleportThroughPortal(entity, map);
        }
        return interfereByEntity;
    }

    public void interactWithEnemies(Enemy enemy, DungeonMap map) {
        if (enemy.getPosition().equals(this.getPosition()) && !enemy.becomeAlly()) {
            System.out.println("entered interact with enemy");
            // could not only bribe when encounter, could also bribe within certain radius
            if (enemy instanceof Mercenary && hasEnoughToBribe()) {
                // bribeMerc();
            } else {
                System.out.println("battle queue");
                battleQueue.add(enemy);
                System.out.println("interact with enemy: " + battleQueue);
            }
        }
    }

    public void battleWithEnemies(DungeonMap map, DungeonGame game) {
        if (battleQueue.size() <= 0) {
            return;
        }
        System.out.println("battle queue has item");
        List<Battle> battles = new ArrayList<Battle>();
        double iniPlayerHealth = this.getHealth();
        Battle currBattle = null;
        System.out.println("player initial health: " + iniPlayerHealth);

        for (Enemy enemy : battleQueue) {

            List<Round> rounds = new ArrayList<Round>();
            double iniEnemyHealth = enemy.getHealth();
            System.out.println("Enemy initial health: " + iniEnemyHealth);
            currBattle = new Battle(enemy.getType(), rounds, iniPlayerHealth, iniEnemyHealth);

            while (this.getHealth() > 0 && enemy.getHealth() > 0) {
                List<Item> weaponryUsed = checkBattleBonuses(map);
                boolean hasShield = false;
                for (Item weapon : weaponryUsed) {
                    if (weapon instanceof Shield) {
                        hasShield = true;
                    }
                }
                double deltaPlayerHealth = - enemy.getAttack()/10;
                double deltaEnemyHealth = - getAttack()/5;
                if (hasShield) {
                    deltaEnemyHealth *= 2;
                }
                double newHealth = getHealth() + deltaPlayerHealth;
                double enemyHealth = enemy.getHealth() + deltaEnemyHealth;
                
                setHealth(newHealth);
                enemy.setHealth(enemyHealth);
                System.out.println("Round player" + getHealth() + "enemyHealth" + enemy.getHealth());
                if (isInvincible()) {
                    weaponryUsed.add(getCurrPotion());
                }
                Round currRound = new Round(deltaPlayerHealth, deltaEnemyHealth, weaponryUsed);
                rounds.add(currRound);
                currBattle.setRounds(rounds);
                
                for (Item weapon : weaponryUsed) {
                    Weapon w = (Weapon) weapon;
                    w.useWeapon();
                }

                if (newHealth <= 0) {
                    // player dies, should remove?????????
                    // map.removeEntityFromMap(this);
                    game.addToBattles(currBattle);
                    setPlayerDied(true);
                    System.out.println("player dies: " + currBattle);
                    return;
                    // return battles;
                } else if (enemyHealth <= 0) {
                    // enemy dies
                    map.removeEntityFromMap(enemy);
                    // increment slayed enemy number
                    setSlayedEnemy(slayedEnemy+1);
                }
                
                if (isInvincible()) {
                    setPlayerWin(true);
                    battles.add(currBattle);
                    game.addToBattles(currBattle);
                    return;
                }
            }
        }
        System.out.println("battle in method: " + currBattle);
        game.addToBattles(currBattle);
        setPlayerWin(true);
    }

    public List<Item> checkBattleBonuses(DungeonMap map) {

        List<Item> weaponryUsed = new ArrayList<Item>();
        double attackBonus = 0;
        double defenceBonus = 0;
        int numAlly = map.getNumOfAlly();
        
        for (Item item: inventory) {
            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;
                if (weapon.isUsable()) {
                    attackBonus += weapon.getDamageValue();
                    defenceBonus += weapon.getDefence();
                    //System.out.println("Aha" + attackBonus + "oho" + defenceBonus);
                    weaponryUsed.add((Item)weapon);
                }
            }
        }

        if (numAlly != 0) {

            attackBonus += numAlly * JSONConfig.getConfig("ally_attack");
            defenceBonus += numAlly * JSONConfig.getConfig("ally_defence");
        }

        this.setAttack(getAttack() + attackBonus);
        this.setDefence(defenceBonus);

        return weaponryUsed;

    }

    public boolean pushBoulder(DungeonMap map, Direction direction) {
        
        boolean blockedBy = false;
        List<Entity> entitiesAtPosition = map.getEntityFromPos(getPosition().translateBy(direction));
        
        for (Entity entity : entitiesAtPosition) {
            if (entity instanceof Boulder) {
                //Check if there is no entity in the direction that the builder is being pushed
                if (map.checkIfEntityAdjacentIsPushable(entity, direction)) {
                    entity.setPosition(entity.getPosition().translateBy(direction));
                } else {
                    blockedBy = true;
                }
                
                break;
            }
        }
        return blockedBy;
    }

    public void collectToInventory(Item item, DungeonMap map) {
        inventory.add(item);
        List<Entity> newMapEntities = map.getMapEntities();
        newMapEntities.remove(item);
        map.setMapEntities(newMapEntities);
    }

    // may need to debug later, update potion queue etc, turn currPotion to null whenever ticks over
    public void consumePotion(String potionType) {
        for (Item item : inventory) {
            if (item.getType().equals(potionType)) {
                potionQueue.addPotionToQueue((Potion) item);
                if (getCurrPotion() == null && !isInvincible() && !isInvisible()) {
                    inventory.remove(item);
                    potionQueue.removePotionFromQueue((Potion) item);
                    setCurrPotion((Potion)item);
                    if (potionType.equals("invincibility_potion")) {
                        setState(new InvincibleState());
                    } else if (potionType.equals("invisibility_potion")){
                        setState(new InvisibleState());
                    }
                    state.playerStateChange(this);
                } 
            }
        }

    }

    public void destorySpawner(){

    }

    public void bribeMerc(Mercenary merc) {
        if (!merc.isBribed() && merc.isInRad() && this.hasEnoughToBribe()) {
            merc.setState(new MercBribedState());
        }
        
        consumeInventory("treasure", JSONConfig.getConfig("bribe_amount"));
    }
    
    public void consumeInventory(String type, int amount) {
        int count = 0;
        while (count < amount) {
            Item delete = null;
            for (Item item : inventory) {
                if (item.getType().equals(type)) {
                    delete = item; 
                }
            
            }
            inventory.remove(delete);
        }
    }

    public boolean isAlive() {

        return true;
    }

    public boolean hasKey() {
        return inventory.stream().anyMatch(i -> i.getType() == "key");
    }

    public List<ItemResponse> getInventoryResponses() {
        return inventory.stream().map(Item::getItemResponse).collect(Collectors.toList());
    }

    public List<String> getBuildables() {

        List<String> ret = new ArrayList<String>();
        
        if (canBuildBow()) {
            ret.add("bow");
        }

        if (canBuildShield()) {
            ret.add("shield");
        }
        
        return ret;
    }

    public boolean canBuildShield() {
        
        if (!inventory.isEmpty()) {
            int woodNumber = 0;
            int treasureOrKeyNumber = 0;
            
            for (Item item : inventory) {

                if (item instanceof Wood) {
                    woodNumber++;
                }

                if (item instanceof Treasure || item instanceof Key) {
                    treasureOrKeyNumber++;
                }
            }

            if ((woodNumber >= 2) && (treasureOrKeyNumber >= 1)) {
                return true;
            } else {
                return false;
            }
                
        }
        return false;
    }

    public boolean canBuildBow() {

        if (!inventory.isEmpty()) {
            int woodNumber = 0;
            int arrowsNumber = 0;
            
            for (Item item : inventory) {

                if (item instanceof Wood) {
                    woodNumber++;
                }

                if (item instanceof Arrows) {
                    arrowsNumber++;
                }
            }

            if ((woodNumber >= 1) && (arrowsNumber >= 3)) {
                return true;
            } else {
                return false;
            }
                
        }
        return false;
    }

    public boolean teleportThroughPortal(Entity entity, DungeonMap map) {
        boolean teleportByPortal = false;
        Portal portal = (Portal) entity;
        portal.linkPortals(map.getMapEntities());
        Position teleport = portal.getPairPosition();
        if (teleport == null) { return teleportByPortal; }

        List<Position> telePositions = teleport.getCardinallyAdjacentPositions();
        Position followDir = teleport.translateBy(getDirection());
        for (Position pos : telePositions) {
            List<Entity> entitiesAtPos = map.getEntityFromPos(pos);
            if (entitiesAtPos != null && (map.containsType(entitiesAtPos,"wall") || 
                map.containsType(entitiesAtPos,"door"))) {
                    telePositions.remove(pos);
            }
        }
        if (telePositions != null) {
            if (telePositions.contains(followDir)) {
                this.setPosition(followDir);
            } else {
                this.setPosition(telePositions.get(0));
            }
            teleportByPortal = true;
            Position teleportedP = this.getPosition();
            
            Entity newPortal = map.getTypeEntityAtPos("portal", teleportedP);
            System.out.println("here" + newPortal);
            //System.out.println("portal" + newPortal.getColour() + newPortal.getPosition());
            if (newPortal == null) { return teleportByPortal; }

            return teleportThroughPortal(newPortal, map);
        }
        return teleportByPortal;
    }

}
