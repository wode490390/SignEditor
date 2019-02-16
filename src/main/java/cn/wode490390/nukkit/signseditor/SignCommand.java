package cn.wode490390.nukkit.signseditor;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import java.util.StringJoiner;

public class SignCommand extends Command {

    private final SignsEditor plugin;

    public SignCommand(SignsEditor plugin) {
        super("sign", "Changes sign text", "/sign <line> [text]");
        this.setPermission("sign.command");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("line", CommandParamType.INT, false),
                new CommandParameter("text", CommandParamType.STRING, true)
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.plugin.isEnabled()) {
            return false;
        }
        if (!this.testPermission(sender)) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        if (!sender.isPlayer()) {
            sender.sendMessage(TextFormat.RED + "The command can only be used by a valid player");
            return true;
        }

        Player player = (Player) sender;
        Position position = this.plugin.getSelected(player);
        Block block;
        if (position == null || !((block = position.getLevelBlock()) instanceof BlockSignPost) || player.getLevel() != block.getLevel()) {
            sender.sendMessage(TextFormat.RED + "You haven't chosen a sign");
            this.plugin.reset(player);
            return true;
        }

        int line;
        try {
            line = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            StringJoiner command = new StringJoiner(" ", "/sign ", "");
            for (String arg : args) {
                command.add(arg);
            }
            sender.sendMessage(TextFormat.RED + "Syntax error: Unexpected \"" + args[0] + "\": at \"" + command.toString() + "\"");
            return false;
        }
        if (line < 0 || line > 4) {
            sender.sendMessage(TextFormat.RED + "'" + args[0] + "' is not a valid parameter");
            return false;
        }

        BlockEntity tile = player.getLevel().getBlockEntity(block);
        BlockEntitySign sign;
        if (tile instanceof BlockEntitySign) {
            sign = (BlockEntitySign) tile;
        } else {
            sign = new BlockEntitySign(block.getLevel().getChunk(block.getFloorX() >> 4, block.getFloorZ() >> 4), BlockEntity.getDefaultCompound(block, BlockEntity.SIGN));
        }

        String text;
        if (args.length > 1) {
            StringJoiner joiner = new StringJoiner(" ");
            for (int i = 1; i < args.length; i++) {
                joiner.add(args[i]);
            }
            text = TextFormat.colorize(joiner.toString());
        } else {
            text = "";
        }

        String[] texts = sign.getText();
        for (int i = 0; i < 4; i++) {
            if (texts[i] == null) {
                texts[i] = "";
            }
        }
        switch (line) {
            case 0:
                sign.setText("", "", "", "");
                break;
            case 1:
                sign.setText(text, texts[1], texts[2], texts[3]);
                break;
            case 2:
                sign.setText(texts[0], text, texts[2], texts[3]);
                break;
            case 3:
                sign.setText(texts[0], texts[1], text, texts[3]);
                break;
            case 4:
                sign.setText(texts[0], texts[1], texts[2], text);
                break;
        }
        sender.sendMessage("Successfully modified");
        this.plugin.reset(player);
        return true;
    }
}
