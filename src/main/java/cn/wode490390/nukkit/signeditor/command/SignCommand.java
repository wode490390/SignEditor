package cn.wode490390.nukkit.signeditor.command;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.signeditor.SignEditorPlugin;

import java.util.StringJoiner;

public class SignCommand extends Command {

    private final SignEditorPlugin plugin;

    public SignCommand(SignEditorPlugin plugin) {
        super("sign", "Changes the text of a sign", "/sign [line] [text]");
        this.setPermission("sign.command");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("line", CommandParamType.INT),
                CommandParameter.newType("text", true, CommandParamType.STRING),
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(new TranslationContainer("%commands.generic.ingame"));
            return true;
        }
        Player player = (Player) sender;

        BlockEntitySign sign = this.plugin.getSelected(player);
        if (sign == null) {
            sender.sendMessage(TextFormat.RED + "You haven't chosen a sign");
            this.plugin.reset(player);
            return true;
        }

        if (args.length == 0) {
            this.plugin.showUI(player, sign);
            return true;
        }

        int line;
        try {
            line = Integer.parseInt(args[0]);
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
