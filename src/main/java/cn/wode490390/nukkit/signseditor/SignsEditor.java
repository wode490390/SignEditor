package cn.wode490390.nukkit.signseditor;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import java.util.concurrent.ConcurrentHashMap;

public class SignsEditor extends PluginBase implements Listener {

    private static SignsEditor instance;

    private final ConcurrentHashMap<Player, Position> select = new ConcurrentHashMap<>();

    public static SignsEditor getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getCommandMap().register("sign", new SignCommand(getInstance()));
        new MetricsLite(getInstance());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("sign.command")) {
            Block block = event.getBlock();
            if (block instanceof BlockSignPost) {
                this.select.put(player, block);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.reset(event.getPlayer());
    }

    protected Position getSelected(Player player) {
        return this.select.get(player);
    }

    protected void reset(Player player) {
        this.select.remove(player);
    }
}
