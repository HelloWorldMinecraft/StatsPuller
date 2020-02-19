package statspuller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import statspuller.commands.ClearCacheCommand;
import statspuller.commands.DumpCommand;
import statspuller.commands.UnmaskCommand;
import statspuller.listeners.StatsRenderUtilListener;
import me.kbrewster.hypixelapi.HypixelAPI;
import me.kbrewster.hypixelapi.guild.Guild;
import me.kbrewster.hypixelapi.player.HypixelPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import statspuller.listeners.StatsChatListener;
import statspuller.listeners.StatsTickListener;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.io.FileUtils;
import statspuller.servers.Server;
import statspuller.servers.hypixel.HypixelBackend;
import statspuller.servers.hypixel.HypixelServer;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod(modid = StatsPuller.MODID, version = StatsPuller.VERSION, guiFactory = "statspuller.StatsGuiFactory")
public class StatsPuller {
    public static List<Server> servers = new LinkedList<>();

    public static Configuration config;
    public static final String MODID = "statspuller";
    public static final String VERSION = "1.0";
    public static Property apiProperty;


    public static void syncConfig() {
        try {
            config.load();

            apiProperty = config.get(Configuration.CATEGORY_CLIENT,
                    "apiKey",
                    "",
                    "Your Hypixel API key");

            HypixelServer.INSTANCE.setKey(apiProperty.getString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) config.save();
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());

        servers.add(HypixelServer.INSTANCE);

        syncConfig();

    }

    @SuppressWarnings("unused")
    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ClearCacheCommand());
        ClientCommandHandler.instance.registerCommand(new DumpCommand());
        ClientCommandHandler.instance.registerCommand(new UnmaskCommand());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new StatsChatListener());
        MinecraftForge.EVENT_BUS.register(new StatsTickListener());
        MinecraftForge.EVENT_BUS.register(new StatsRenderUtilListener());
    }

    public static Server getServer(String ip) {
        for (Server server : servers) {
            if (server.isReady() && ip.contains(server.getIP())) return server;
        }
        return null;
    }

    public static Server getServer() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) return null;
        return getServer(Minecraft.getMinecraft().getCurrentServerData().serverIP);
    }

}
