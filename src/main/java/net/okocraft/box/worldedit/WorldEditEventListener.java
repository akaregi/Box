package net.okocraft.box.worldedit;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;

public class WorldEditEventListener {

    private static final WorldEdit worldEdit = WorldEdit.getInstance();
    private static WorldEditEventListener listener;
    private static Map<Player, Long> cooldowns = new HashMap<>();

    private WorldEditEventListener() {
    }

    public static void register() {
        if (listener == null) {
            listener = new WorldEditEventListener();
        }
        worldEdit.getEventBus().register(listener);
    }

    public static void unregister() {
        if (listener != null) {
            worldEdit.getEventBus().unregister(listener);
            listener = null;
        }
    }

    static void putCooldown(Player player, long cooldown) {
        cooldowns.put(player, cooldown);
    }

    static long getCooldown(Player player) {
        return cooldowns.getOrDefault(player, System.currentTimeMillis());
    }

    @Subscribe
    public void onEditSessionEvent(EditSessionEvent event) {
        if (event.getStage() != Stage.BEFORE_CHANGE) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getActor().getUniqueId());
        if (player == null) {
            return;
        }
        Extent extent = event.getExtent();
        ConsumeBlockFromBoxExtent consume;
        extent = new PreventReplacingDisallowExtent(extent, player);
        extent = consume = new ConsumeBlockFromBoxExtent(extent, player);
        event.setExtent(extent);
        if (false) {
        //if (!cooldowns.containsKey(player)) {
            new BukkitRunnable() {

                private long startTime = System.currentTimeMillis();

                @Override
                public void run() {
                    player.sendMessage("run");
                    player.sendMessage("starttime: " + startTime);
                    player.sendMessage("cooldown: " + cooldowns.getOrDefault(player, startTime));
                    player.sendMessage("cooldown + 3000: " + (cooldowns.getOrDefault(player, startTime) + 3000L));
                    player.sendMessage("currentTime: " + System.currentTimeMillis());
                    
                    player.sendMessage("match: " + (cooldowns.getOrDefault(player, startTime) + 3000L <= System.currentTimeMillis()));
                    if (cooldowns.getOrDefault(player, startTime) + 3000L <= System.currentTimeMillis()) {
                        Map<BlockType, Integer> missing = consume.popMissing();
                        if (!missing.isEmpty()) {
                            player.sendMessage("§8[§6Box§8] §7不足したブロック:");
                            missing.forEach(
                                    (type, amount) -> player.sendMessage("  §b" + type.getName() + "§7: §b" + amount));
                        }
                        player.sendMessage("cancel");
                        cancel();
                        cooldowns.remove(player);
                    } else if (startTime + 10000 < System.currentTimeMillis()) {
                        player.sendMessage("cancel timelimit");
                        cancel();

                    }
                }
            }.runTaskTimer(Box.getInstance(), 60L, 60L);
        }
    }
}