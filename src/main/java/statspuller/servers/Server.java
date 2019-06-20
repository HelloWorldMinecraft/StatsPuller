package statspuller.servers;

import statspuller.Backend;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public abstract class Server {
    private final String NAME, IP;

    protected Server(@Nonnull final String NAME, @Nonnull String IP) {
        Objects.requireNonNull(NAME);
        Objects.requireNonNull(IP);
        this.NAME = NAME;
        this.IP = IP;
    }

    @Nonnull
    public final String getName() {
        return NAME;
    }
    @Nonnull
    public final String getIP() {
        return IP;
    }

    public abstract Backend getApplicableBackend(boolean instant);
    public abstract Backend getBackend(String string, boolean instant);

    public abstract void loadCache(@Nonnull File cacheFile) throws IOException;
    public abstract void saveCache(@Nonnull File cacheFile) throws IOException;
    public abstract void clearCache();

    public abstract String getGuildId(@Nonnull UUID uuid, boolean instant);
    public abstract String getGuildName(@Nonnull String id, boolean instant);

    public abstract Set<UUID> getFriends(@Nonnull UUID uuid, boolean instant);
    public abstract void getPossibleIncognitos(@Nonnull Set<UUID> uuids, Consumer<String> consumer);

    public abstract String getPlayerName(@Nonnull UUID uuid, boolean instant);

    public abstract String getGenericDump(UUID uuid, boolean instant);
    public abstract boolean isValid(UUID uuid, boolean instant);

    public abstract void setKey(@Nonnull String string);
    public abstract boolean isReady();

}
