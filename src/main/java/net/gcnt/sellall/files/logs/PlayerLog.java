package net.gcnt.sellall.files.logs;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.items.Item;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLog {

    private static final HashMap<UUID, PlayerLog> storedLogs = new HashMap<>();

    private final HashMap<Item, Integer> items;
    private final UUID uuid;

    private PlayerLog(UUID uuid) {
        this.uuid = uuid;
        this.items = new HashMap<>();
    }

    public static PlayerLog getPlayerLog(SellAll plugin, UUID uuid) {
        if (storedLogs.containsKey(uuid)) {
            return storedLogs.get(uuid);
        }

        PlayerLog log = new PlayerLog(uuid);
        plugin.getMySQLLog().initializeLogs(log);

        storedLogs.put(uuid, log);
        return log;
    }

    public static void removePlayerLog(UUID uuid) {
        storedLogs.remove(uuid);
    }

    public void clear() {
        this.items.clear();
    }

    public int getSellCount(Item item) {
        return this.items.getOrDefault(item, 0);
    }

    public void setSellCount(Item item, int amount) {
        this.items.put(item, amount);
    }

    public int getSellsLeft(Item item) {
        if (item == null || item.getMaxDailySells() <= 0) return -1;

        int sold = getSellCount(item);
        return Math.max(0, item.getMaxDailySells() - sold);
    }

    public UUID getUuid() {
        return uuid;
    }
}
