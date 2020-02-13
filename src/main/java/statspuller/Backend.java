package statspuller;

import me.kbrewster.hypixelapi.player.HypixelPlayer;
import me.kbrewster.hypixelapi.player.stats.Stats;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

public interface Backend {
    long getKills(@Nonnull UUID uuid, boolean instant);
    long getDeaths(@Nonnull UUID uuid, boolean instant);
    long getWins(@Nonnull UUID uuid, boolean instant);

    String getExtra(@Nonnull UUID uuid, boolean detailed, boolean instant);
    String getDump(@Nonnull UUID uuid, boolean detailed, boolean instant);

    String getName();
}
