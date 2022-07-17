package dungeonmania.movingEntity;

import dungeonmania.util.JSONConfig;

public class MercViciousState implements MercenaryState {

    @Override
    public void currentState(Mercenary merc) {
        merc.setHealth(JSONConfig.getConfig("mercenary_health"));
        merc.setAttack(JSONConfig.getConfig("mercenary_attack"));
        merc.setBribed(false);
    }
  
}
