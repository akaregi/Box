package net.okocraft.box.database;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    private final Database database;
    private final ItemTable itemTable;
    private final PlayerTable playerTable;
    private final MasterTable masterTable;

    private final Map<Player, Map<ItemStack, Boolean>> autostore = new HashMap<>();
    private final Map<Player, Map<ItemStack, Integer>> stock = new HashMap<>();

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public PlayerData(Path dbPath) {
        try {
            this.database = new Database(dbPath);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }

        this.itemTable = new ItemTable(database);
        this.playerTable = new PlayerTable(database);
        this.masterTable = new MasterTable(database, itemTable, playerTable);
    }

    ItemTable getItemTable() {
        return itemTable;
    }

    PlayerTable getPlayerTable() {
        return playerTable;
    }

    public List<String> getPlayers() {
        return getPlayerTable().getAllName();
    }

    /**
     * @see Database#dispose()
     */
    public void dispose() {
        Bukkit.getOnlinePlayers().forEach(this::removeCache);
        threadPool.submit(() -> database.dispose());
        try {
            threadPool.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadCache(Player player) {
        stock.put(player, getStockAll(player));
        autostore.put(player, getAutoStoreAll(player));
        playerTable.loadCache(player);
    }

    public void removeCache(Player player) {
        masterTable.setAutoStoreAll(player, autostore.remove(player));
        masterTable.setStockAll(player, stock.remove(player));
        playerTable.removeCache(player);
    }

    public void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        if (player.isOnline()) {
            getAutoStoreAll(player).replaceAll((item, value) -> enabled);
        }

        threadPool.submit(() -> masterTable.setAutoStoreAll(player, enabled));
    }

    public void setAutoStoreAll(OfflinePlayer player, Map<ItemStack, Boolean> enabled) {
        Map<ItemStack, Boolean> replaced = new HashMap<>();
        enabled.forEach((item, value) -> {
            if (item != null) {
                ItemStack clone = item.clone();
                clone.setAmount(1);
                replaced.put(clone, value);
            }
        });
        if (player.isOnline()) {
            getAutoStoreAll(player).replaceAll((item, value) -> replaced.get(item));
        }

        threadPool.submit(() -> masterTable.setAutoStoreAll(player, replaced));
    }

    public void setAutoStore(OfflinePlayer player, ItemStack item, boolean enabled) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getAutoStoreAll(player).put(clone, enabled);
        }

        threadPool.submit(() -> masterTable.setAutoStore(player, clone, enabled));
    }

    public boolean getAutoStore(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getAutoStoreAll(player).getOrDefault(clone, false)
                : masterTable.getAutoStore(player, clone); 
    }

    public Map<ItemStack, Boolean> getAutoStoreAll(OfflinePlayer player) {
        if (player.isOnline()) {
            if (autostore.containsKey(player)) {
                return autostore.get(player);
            } else {
                Map<ItemStack, Boolean> result = masterTable.getAutoStoreAll(player);
                autostore.put(player.getPlayer(), result);
                return result;
            }
        }

        return masterTable.getAutoStoreAll(player);
    }

    public void setStock(OfflinePlayer player, ItemStack item, int stock) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, stock);
        }

        threadPool.submit(() -> masterTable.setStock(player, clone, stock));
    }

    public void setStockAll(OfflinePlayer player, Map<ItemStack, Integer> stock) {
        Map<ItemStack, Integer> clone = new HashMap<>();
        stock.forEach((item, value) -> {
            if (item != null) {
                ItemStack cloneItem = item.clone();
                cloneItem.setAmount(1);
                clone.put(cloneItem, value);
            }
        });
        if (player.isOnline()) {
            getStockAll(player).replaceAll((item, value) -> clone.getOrDefault(item, 0));
        }

        threadPool.submit(() -> masterTable.setStockAll(player, clone));
    }

    public void addStock(OfflinePlayer player, ItemStack item, int amount) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, getStock(player, clone) + amount);
        }

        threadPool.submit(() -> masterTable.addStock(player, clone, amount));
    }

    public int getStock(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return 0;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getStockAll(player).getOrDefault(clone, 0)
                : masterTable.getStock(player, clone); 
    }

    public Map<ItemStack, Integer> getStockAll(OfflinePlayer player) {
        if (player.isOnline()) {
            if (stock.containsKey(player)) {
                return stock.get(player);
            } else {
                Map<ItemStack, Integer> result = masterTable.getStockAll(player);
                stock.put(player.getPlayer(), result);
                return result;
            }
        }

        return masterTable.getStockAll(player);
    }

    public boolean storeAll(Player player) {
        boolean isModified = false;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (itemTable.getId(item) == -1) {
                continue;
            }
            int stock = getStock(player, item);
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            setStock(player, item, stock + amount);
            isModified = true;
        }

        return isModified;
    }
}