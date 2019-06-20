package statspuller.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import statspuller.StatsPuller;
import statspuller.servers.Server;

import javax.annotation.Nonnull;
import java.util.*;

public class UnmaskCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "unmask";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/unmask";
    }

    @Override
    public List<String> getCommandAliases() {
        return new LinkedList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Server server = StatsPuller.getServer();
        if (server == null) return;

        Set<UUID> uuids = new HashSet<>();

        for (NetworkPlayerInfo playerInfo : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            UUID uuid = playerInfo.getGameProfile().getId();
            if (uuid != null) uuids.add(uuid);
        }

        server.getPossibleIncognitos(uuids, str -> Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(str)));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return new LinkedList<>();
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
