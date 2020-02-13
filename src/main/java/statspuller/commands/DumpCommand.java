package statspuller.commands;

import me.kbrewster.mojangapi.MojangAPI;
import net.minecraft.util.ChatComponentText;
import statspuller.Backend;
import statspuller.StatsPuller;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import statspuller.gui.StatsPlayerListGui;
import statspuller.servers.Server;

import javax.annotation.Nonnull;
import java.util.*;

public class DumpCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "dump";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/dump";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        new Thread(() -> {
            Server server = args.length >= 3 ? StatsPuller.getServer(args[2]) : StatsPuller.getServer();
            Backend backend = args.length >= 2 ? server.getBackend(args[1], true) : server.getApplicableBackend(true);
            String playerName = args.length >= 1 ? args[0] : sender.getName();

            sender.addChatMessage(new ChatComponentText("Server: " + server.getName()));
            sender.addChatMessage(new ChatComponentText("Backend: " + backend.getName()));

            sender.addChatMessage(new ChatComponentText("Player: " + playerName));

            try {
                UUID playerUUID = MojangAPI.getUUID(playerName);
                sender.addChatMessage(new ChatComponentText("Player UUID: " + playerUUID.toString()));
                sender.addChatMessage(new ChatComponentText(server.getGenericDump(playerUUID, true)));
                sender.addChatMessage(new ChatComponentText(backend.getDump(playerUUID, true, true)));
            } catch (Exception exception) {
                exception.printStackTrace();
                sender.addChatMessage(new ChatComponentText("Player UUID is invalid; breaking"));

            }
        }).start();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@Nonnull ICommand o) {
        return getCommandName().compareTo(o.getCommandName());
    }
}
