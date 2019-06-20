package statspuller.commands;

import statspuller.StatsPuller;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import statspuller.servers.Server;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClearCacheCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "clearcache";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/clearcache";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) StatsPuller.servers.forEach(Server::clearCache);
        else Arrays.stream(args).map(StatsPuller::getServer).filter(Objects::nonNull).forEach(Server::clearCache);
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
