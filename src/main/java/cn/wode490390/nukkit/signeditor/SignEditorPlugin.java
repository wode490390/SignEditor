package cn.wode490390.nukkit.signeditor;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.signeditor.command.SignCommand;
import cn.wode490390.nukkit.signeditor.util.MetricsLite;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Arrays;

public class SignEditorPlugin extends PluginBase implements Listener {

    private final Long2ObjectMap<Position> select = new Long2ObjectOpenHashMap<>();

    private final Long2IntMap uiWindows = new Long2IntOpenHashMap();

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 3111);
        } catch (Throwable ignore) {

        }

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getCommandMap().register("sign", new SignCommand(this));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getBlock();
        if (block instanceof BlockSignPost) {
            Player player = event.getPlayer();
            if (player.hasPermission("sign.command")) {
                this.select.put(player.getId(), block);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.reset(event.getPlayer());
    }

    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        long id = player.getId();
        if (this.uiWindows.get(id) == event.getFormID()) {
            FormWindow window = event.getWindow();
            if (window instanceof FormWindowCustom) {
                FormWindowCustom customWindow = (FormWindowCustom) window;
                if (customWindow.getTitle().equals("Sign Editor")) {
                    if (!window.wasClosed()) {
                        FormResponse response = event.getResponse();
                        if (response instanceof FormResponseCustom) {
                            FormResponseCustom customResponse = (FormResponseCustom) response;
                            BlockEntitySign sign = this.getSelected(player);
                            if (sign != null) {
                                Object empty = customResponse.getResponse(5);
                                if (empty instanceof Boolean) {
                                    if ((Boolean) empty) {
                                        sign.setText("", "", "", "");
                                    } else {
                                        sign.setText(TextFormat.colorize(String.valueOf(customResponse.getResponse(1))),
                                                TextFormat.colorize(String.valueOf(customResponse.getResponse(2))),
                                                TextFormat.colorize(String.valueOf(customResponse.getResponse(3))),
                                                TextFormat.colorize(String.valueOf(customResponse.getResponse(4))));
                                    }
                                    player.sendMessage("Successfully modified");
                                }
                            }
                        }
                    }
                    this.uiWindows.remove(id);
                }
            }
        }
    }

    public void showUI(Player player, BlockEntitySign sign) {
        String[] texts = sign.getText();
        this.uiWindows.put(player.getId(), player.showFormWindow(new FormWindowCustom("Sign Editor", Arrays.asList(
                new ElementLabel("Position: " + sign.getFloorX() + ", " + sign.getFloorY() + ", " + sign.getFloorZ()), // 0
                new ElementInput("Line 1", "", texts[0]), // 1
                new ElementInput("Line 2", "", texts[1]), // 2
                new ElementInput("Line 3", "", texts[2]), // 3
                new ElementInput("Line 4", "", texts[3]), // 4
                new ElementToggle("Empty Text") // 5
        ))));
    }

    public BlockEntitySign getSelected(Player player) {
        Position position = this.select.get(player.getId());
        if (position != null) {
            Block block = position.getLevelBlock();
            if (block instanceof BlockSignPost) {
                Level level = player.getLevel();
                if (level == block.getLevel()) {
                    BlockEntity blockEntity = level.getBlockEntity(block);
                    BlockEntitySign sign;
                    if (blockEntity instanceof BlockEntitySign) {
                        sign = (BlockEntitySign) blockEntity;
                    } else {
                        sign = (BlockEntitySign) BlockEntity.createBlockEntity(BlockEntity.SIGN, block.getChunk(), BlockEntity.getDefaultCompound(block, BlockEntity.SIGN));
                    }
                    return sign;
                }
            }
        }
        return null;
    }

    public void reset(Player player) {
        this.select.remove(player.getId());
    }
}
