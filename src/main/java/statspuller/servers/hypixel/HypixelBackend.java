package statspuller.servers.hypixel;

import me.kbrewster.hypixelapi.player.HypixelPlayer;
import me.kbrewster.hypixelapi.player.stats.Stats;
import statspuller.Backend;
import statspuller.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class HypixelBackend<T> implements Backend {
    private final Class<? extends T> TYPE;

    public HypixelBackend(@Nonnull Class<? extends T> TYPE) {
        this.TYPE = TYPE;
    }

    @Override
    public String getName() {
        return TYPE.getSimpleName();
    }

    @Override
    public final long getKills(@Nonnull UUID uuid, boolean instant) {
        T game = getGame(uuid, instant);
        if (game == null) return 0;
        return getKills(game);
    }

    @Override
    public final long getDeaths(@Nonnull UUID uuid, boolean instant) {
        T game = getGame(uuid, instant);
        if (game == null) return 0;
        return getDeaths(game);
    }

    @Override
    public final long getWins(@Nonnull UUID uuid, boolean instant) {
        T game = getGame(uuid, instant);
        if (game == null) return 0;
        return getWins(game);
    }

    @Override
    public final String getDump(@Nonnull UUID uuid, boolean instant) {
        T game = getGame(uuid, instant);
        if (game == null) return null;
        return getDump(game);
    }

    @Override
    public final String getExtra(@Nonnull UUID uuid, boolean instant) {
        T game = getGame(uuid, instant);
        if (game == null) return null;
        return getExtra(game);
    }

    public long getKills(@Nonnull T game) {
        return (long) Utils.callMethod(game, "getKills");
    }
    public long getDeaths(@Nonnull T game) {
        return (long) Utils.callMethod(game, "getDeaths");
    }
    public long getWins(@Nonnull T game) {
        return (long) Utils.callMethod(game, "getWins");
    }

    public String getDump(@Nonnull T game) {
        return "Kills: " + getKills(game) + "\n" +
                "Deaths: " + getDeaths(game) + "\n" +
                "Wins: " + getWins(game) + "\n";
    }

    public String getExtra(@Nonnull T game) {
        return "";
    }

    @SuppressWarnings("unchecked")
    private T getGame(@Nonnull UUID uuid, boolean instant) {
        HypixelPlayer player = HypixelServer.INSTANCE.getPlayer(uuid, instant);
        if (player == null) return null;

        Stats stats = player.getStats();
        if (stats == null) return null;
        try {
            Optional<Method> method = Arrays.stream(Stats.class.getMethods()).filter(test -> test.getReturnType() == TYPE).findFirst();
            if (!method.isPresent()) throw new UnsupportedOperationException(TYPE.getSimpleName() + " is not returned!");

            return (T) method.get().invoke(stats);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
