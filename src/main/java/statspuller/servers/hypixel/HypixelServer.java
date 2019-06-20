package statspuller.servers.hypixel;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.kbrewster.hypixelapi.HypixelAPI;
import me.kbrewster.hypixelapi.friends.Friend;
import me.kbrewster.hypixelapi.guild.Guild;
import me.kbrewster.hypixelapi.guild.Member;
import me.kbrewster.hypixelapi.player.HypixelPlayer;
import me.kbrewster.hypixelapi.player.misc.Links;
import me.kbrewster.hypixelapi.player.misc.Settings;
import me.kbrewster.hypixelapi.player.misc.SocialMedia;
import me.kbrewster.hypixelapi.player.stats.battlegrounds.BattleGrounds;
import me.kbrewster.hypixelapi.player.stats.bedwars.Bedwars;
import me.kbrewster.hypixelapi.player.stats.hg.HungerGames;
import me.kbrewster.hypixelapi.player.stats.mcgo.MCGO;
import me.kbrewster.hypixelapi.player.stats.megawalls.Walls3;
import me.kbrewster.hypixelapi.player.stats.mm.MurderMystery;
import me.kbrewster.hypixelapi.player.stats.paintball.Paintball;
import me.kbrewster.hypixelapi.player.stats.quake.Quake;
import me.kbrewster.hypixelapi.player.stats.skyclash.SkyClash;
import me.kbrewster.hypixelapi.player.stats.skywars.Skywars;
import me.kbrewster.hypixelapi.player.stats.smash.SuperSmash;
import me.kbrewster.hypixelapi.player.stats.speeduhc.SpeedUHC;
import me.kbrewster.hypixelapi.player.stats.uhc.UHC;
import me.kbrewster.hypixelapi.player.stats.vampz.VampireZ;
import me.kbrewster.hypixelapi.player.stats.walls.Walls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.impl.DurationImpl;
import statspuller.Backend;
import statspuller.StatsPuller;
import statspuller.Utils;
import statspuller.servers.hypixel.backend.TNTWizardsBackend;
import statspuller.servers.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HypixelServer extends Server {
    public static final HypixelServer INSTANCE = new HypixelServer();

    private Gson gson = new Gson();
    private PrettyTime prettyTime = new PrettyTime();

    private Map<UUID, HypixelPlayer> players = new HashMap<>();
    private Map<UUID, String> guildIds = new HashMap<>();
    private Map<String, Guild> guilds = new HashMap<>();
    private Map<UUID, List<Friend>> friends = new HashMap<>();

    private HypixelAPI hypixel;

    private List<Backend> backends = new LinkedList<>();

    private Set<String> requesting = new HashSet<>();


    private HypixelServer() {
        super("Hypixel Network", "hypixel.net");

        backends.add(new HypixelBackend<>(BattleGrounds.class));
        backends.add(new HypixelBackend<>(HungerGames.class));
        backends.add(new HypixelBackend<>(MCGO.class));
        backends.add(new HypixelBackend<>(Quake.class));
        backends.add(new TNTWizardsBackend()); //TODO Rest of tnt
        backends.add(new HypixelBackend<>(UHC.class));
        //TODO Gingerbread
        //TODO Arcade
        backends.add(new HypixelBackend<>(Skywars.class));
        backends.add(new HypixelBackend<>(SuperSmash.class));
        backends.add(new HypixelBackend<>(Walls.class));
        backends.add(new HypixelBackend<>(Walls3.class));
        backends.add(new HypixelBackend<>(MurderMystery.class));
        backends.add(new HypixelBackend<>(Bedwars.class));
        backends.add(new HypixelBackend<>(Paintball.class));
        backends.add(new HypixelBackend<>(VampireZ.class));
        backends.add(new HypixelBackend<>(SpeedUHC.class));
        backends.add(new HypixelBackend<>(SkyClash.class));
    }

    @Override
    public void loadCache(@Nonnull File cacheFile) throws IOException {
        List<String> list = gson.fromJson(FileUtils.readFileToString(cacheFile), new TypeToken<List<String>>() {
        }.getType());
        players = gson.fromJson(list.get(0), new TypeToken<Map<UUID, HypixelPlayer>>() {
        }.getType());
        guildIds = gson.fromJson(list.get(1), new TypeToken<Map<UUID, String>>() {
        }.getType());
        guilds = gson.fromJson(list.get(2), new TypeToken<Map<String, Guild>>() {
        }.getType());
        friends = gson.fromJson(list.get(2), new TypeToken<Map<UUID, List<Friend>>>() {
        }.getType());
    }

    @Override
    public void saveCache(@Nonnull File cacheFile) throws IOException {
        List<String> list = new LinkedList<>();
        list.add(gson.toJson(players));
        list.add(gson.toJson(guildIds));
        list.add(gson.toJson(guilds));
        list.add(gson.toJson(friends));
        FileUtils.write(cacheFile, gson.toJson(list));
    }

    @Override
    public void clearCache() {
        players = new HashMap<>();
        guildIds = new HashMap<>();
        guilds = new HashMap<>();
        friends = new HashMap<>();
    }

    @Override
    public void setKey(@Nonnull String string) {
        hypixel = new HypixelAPI(string);
    }

    @Override
    public boolean isReady() {
        return hypixel != null;
    }

    @Override
    public Backend getApplicableBackend(boolean instant) {
        try {
            return getBackend(Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName(), instant);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public Backend getBackend(String string, boolean instant) {
        String[] words = StringUtils.stripControlCodes(string)
                .toLowerCase().replaceAll("the|games", "").trim().split(" ");

        for (Backend backend : backends) {
            for (String word : words) {
                if (backend.getName().toLowerCase().contains(word)) return backend;
            }
        }

        return null;
    }

    @Override
    public String getGuildId(@Nonnull UUID uuid, boolean instant) {
        return getGuildID(uuid, instant);
    }

    @Override
    public String getGuildName(@Nonnull String id, boolean instant) {
        Guild guild = getGuild(id, instant);
        if (guild == null) return "UNLOADED";

        return guild.getTag();
    }

    @Override
    public Set<UUID> getFriends(@Nonnull UUID uuid, boolean instant) {
        Set<UUID> friendList = new HashSet<>();

        for (Friend friend : getFriendsInternal(uuid, instant)) {
            UUID senderUUID = Utils.fromString(friend.getUuidSender());
            UUID recieverUUID = Utils.fromString(friend.getUuidReceiver());

            if (uuid.equals(recieverUUID)) {
                friendList.add(senderUUID);
            } else {
                friendList.add(recieverUUID);
            }
        }

        return friendList;
    }

    @Override
    public void getPossibleIncognitos(@Nonnull Set<UUID> uuids, Consumer<String> consumer) {
        //Keep a list of threads
        List<Thread> threads = new ArrayList<>(uuids.size());
        Set<UUID> reportedUUIDs = new HashSet<>();

        for (UUID uuid : uuids) {

            //Do this part multithreaded
            Thread thread = new Thread(() -> {
                String displayName = getPlayerName(uuid, true);

                //Pre-message
                consumer.accept("Scanning player " + displayName);

                //Add the player's uuid to possibleNicks because we don't want false flags
                uuids.add(uuid);

                //Iterate over the friends
                for (UUID friendId : getFriends(uuid, true)) {
                    //Check if they're nicked
                    //Do it multithreaded
                    Thread checkThread = new Thread(() -> {
                        //Check if they're nicked
                        HypixelPlayer player = getPlayer(friendId, true);
                        if (player == null) return;

                        if ((player.getMonthlyPackageRank() == null || !player.getMonthlyPackageRank().equals("SUPERSTAR"))
                                && player.getRank() == null) return;

                        //Log the friend and who it came from
                        if (reportedUUIDs.contains(friendId) || uuids.contains(friendId)) return;
                        reportedUUIDs.add(friendId);

                        consumer.accept(displayName + " - " + getPlayerName(friendId, true));

                        endThread(threads, consumer);
                    });

                    threads.add(checkThread);
                    checkThread.start();
                }


                endThread(threads, consumer);
            });

            threads.add(thread);
            thread.start();
        }
    }

    private void endThread(List<Thread> threads, Consumer<String> consumer) {
        consumer.accept("Finished thread. Remaining: " + threads.stream().filter(Thread::isAlive).count());
    }

    @Override
    public String getPlayerName(@Nonnull UUID uuid, boolean instant) {
        HypixelPlayer player = getPlayer(uuid, instant);


        String prefix = prettyTime.format(new Date(player.getLastLogin())) + " - ";
        if (player.getRank() != null && !player.getRank().equals("NONE")) prefix = "[" + player.getRank() + "]";

        String suffix = "";
        if (player.getLastNick() != null && !player.getLastNick().isEmpty()) suffix = " - " + player.getLastNick();

        return prefix + player.getDisplayname() + suffix;
    }

    @Override
    public String getGenericDump(UUID uuid, boolean instant) {
        HypixelPlayer player = getPlayer(uuid, instant);

        String value = "Hypixel Level: " + player.getAbsoluteLevel() + "\n" +
                "Rank: " + player.getCurrentRank() + "\n" +
                "Language: " + player.getLanguage() + "\n" +
                "User Language: " + player.getUserLanguage() + "\n" +
                "Last Logout: " + prettyTime.format(new Date(player.getLastLogout())) + "\n" +
                "Last Login: " + prettyTime.format(new Date(player.getLastLogin())) + "\n" +
                "Last Nick: " + player.getLastNick() + "\n" +
                "Version: " + player.getMcVersionRp() + "\n\n";

        Settings settings = player.getSettings();
        value += "Allows Frield Requests: " + settings.isAllowFriendRequests() + "\n";

        SocialMedia socialMedia = player.getSocialMedia();
        if (socialMedia != null) {
            Links media = socialMedia.getLinks();
            value += "Hypixel: " + media.getHypixel() + "\n" +
                    "YouTube: " + media.getYoutube() + "\n" +
                    "Twitter: " + media.getTwitter() + "\n\n";
        }

        String guildID = getGuildID(uuid, instant);
        if (guildID != null) {
            Guild guild = getGuild(guildID, instant);
            value += "Guild ID: " + guildID + "\n" +
                    "Guild Name: " + guild.getName() + "\n" +
                    "Guild Tag: " + guild.getTag() + "\n";
        }

        return value;
    }

    @Override
    public boolean isValid(UUID uuid, boolean instant) {
        return getPlayer(uuid, instant) != null;
    }

    HypixelPlayer getPlayer(@Nonnull UUID id, boolean instant) {
        if (!players.containsKey(id)) {
            run(() -> {
                HypixelPlayer player = null;
                try {
                    player = hypixel.getPlayer(id);
                } catch (Exception ignore) {
                }

                players.put(id, player);


            }, instant, "PLAYER_" + id);
        }
        return players.get(id);
    }

    private String getGuildID(@Nonnull UUID id, boolean instant) {
        if (!guildIds.containsKey(id)) {
            run(() -> {
                String guildId = null;
                try {
                    guildId = hypixel.getGuildID(id);
                } catch (Exception ignore) {
                }

                guildIds.put(id, guildId);
            }, instant, "GUILDID_" + id);
        }
        return guildIds.get(id);
    }

    private Guild getGuild(@Nonnull String id, boolean instant) {
        if (!guilds.containsKey(id)) {
            run(() -> {
                Guild guild = null;
                try {
                    guild = hypixel.getGuild(id);
                } catch (Exception ignore) {
                }

                guilds.put(id, guild);
            }, instant, "GUILD_" + id);
        }
        return guilds.get(id);
    }

    private List<Friend> getFriendsInternal(@Nonnull UUID id, boolean instant) {
        if (!friends.containsKey(id)) {
            run(() -> {
                List<Friend> friendsList = new LinkedList<>();
                try {
                    friendsList = hypixel.getFriends(id);
                } catch (Exception ignore) {
                }

                friends.put(id, friendsList);
            }, instant, "FRIENDS_" + id);
        }
        return friends.getOrDefault(id, Collections.emptyList());
    }

    private void run(Runnable runnable, boolean sync, String key) {
        if (sync) runnable.run();
        else {
            if (requesting.contains(key)) return;
            requesting.add(key);

            new Thread(() -> {
                runnable.run();
                requesting.remove(key);
            }).start();
        }
    }
}
