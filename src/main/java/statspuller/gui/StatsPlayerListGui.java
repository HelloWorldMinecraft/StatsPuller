package statspuller.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemArmor;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import statspuller.Backend;
import statspuller.StatsPuller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import statspuller.servers.Server;

import java.util.*;
import java.util.stream.Collectors;

public class StatsPlayerListGui extends GuiPlayerTabOverlay {
    private static final Ordering<NetworkPlayerInfo> SORTER = Ordering.from(new PlayerComparator());
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private IChatComponent footer;
    private IChatComponent header;
    private long lastTimeOpened;
    private boolean isBeingRendered;

    private Set<Set<UUID>> friendGroups = new LinkedHashSet<>();

    public StatsPlayerListGui(Minecraft minecraft, GuiIngame guiIngame) {
        super(minecraft, guiIngame);
        mc = minecraft;
        this.guiIngame = guiIngame;
    }

    @Override
    public void updatePlayerList(boolean updatePlayerList) {
        if (updatePlayerList && !this.isBeingRendered) {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = updatePlayerList;
    }

    @Override
    public void renderPlayerlist(int screenWidth, Scoreboard scoreboard, ScoreObjective objective) {
        List<NetworkPlayerInfo> playerList = SORTER.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
        int maxNameWidth = 0;

        List<Integer> maxStatWidth = new ArrayList<>(playerList.size());
        List<List<String>> statInfo = new ArrayList<>(playerList.size());
        int longestStatLength = 0;

        prerender(playerList);

        for (NetworkPlayerInfo playerInfo : playerList) {
            //Work with name width
            int nameWidth = mc.fontRendererObj.getStringWidth(getPlayerName(playerInfo));
            maxNameWidth = Math.max(maxNameWidth, nameWidth);


            //Work with stat width
            List<String> strings = getPlayerInfo(playerInfo);
            //Work with objective width
            if (objective != null) {
                strings.add(0, scoreboard.getValueFromObjective(playerInfo.getGameProfile().getName(), objective).getScorePoints() + "");
            }

            statInfo.add(strings);

            longestStatLength = Math.max(longestStatLength, strings.size());

            for (int i = 0; i < strings.size(); i++) {
                int length = mc.fontRendererObj.getStringWidth(strings.get(i));
                if (maxStatWidth.size() < i + 1) maxStatWidth.add(length);
                else if (maxStatWidth.get(i) < length) maxStatWidth.set(i, length);
            }
        }

        int paddingPixels = 5;

        //Remove any extra space that's not at the start
        boolean isAtStart = true;
        for (int i = 0; i < maxStatWidth.size(); i++) {
            int amount = maxStatWidth.get(0);
            if (amount > 0) isAtStart = false;

            if (amount == 0 && !isAtStart) maxStatWidth.set(i, mc.fontRendererObj.getCharWidth(' '));
        }

        //Show at most 80 players
        playerList = playerList.subList(0, Math.min(playerList.size(), 80));
        
        int playerListSize = playerList.size();

        //Find how many columns are needed
        int columnAmount = 1;
        int playersPerColumn;
        for (playersPerColumn = playerList.size(); playersPerColumn > 20; playersPerColumn = (playerListSize + columnAmount - 1) / columnAmount) { // TODO Math.ceil(playersPerColumn = playerListSize / columnAmount)
            columnAmount++;
        }


        int extraPaddingNeeded = paddingPixels;
        for (int i : maxStatWidth) extraPaddingNeeded += i + paddingPixels;

        boolean serverIsSecure = mc.isIntegratedServerRunning() || mc.getNetHandler().getNetworkManager().getIsencrypted();
        int columnWidth = Math.min(columnAmount * ((serverIsSecure ? 9 : 0) + maxNameWidth + extraPaddingNeeded + 13), screenWidth - 50) / columnAmount;
        int xPositionStart = screenWidth / 2 - (columnWidth * columnAmount + (columnAmount - 1) * 5) / 2;
        int yPositionStart = 10;
        int miscWidth = columnWidth * columnAmount + (columnAmount - 1) * 5;

        List<String> headerLines = null;
        List<String> footerLines = null;

        //Find the max the header needs to be
        if (header != null) {
            headerLines = mc.fontRendererObj.listFormattedStringToWidth(header.getFormattedText(), screenWidth - 50);

            for (String string : headerLines) {
                miscWidth = Math.max(miscWidth, mc.fontRendererObj.getStringWidth(string));
            }
        }

        if (footer != null) {
            footerLines = mc.fontRendererObj.listFormattedStringToWidth(footer.getFormattedText(), screenWidth - 50);
            
            for (String string : footerLines) {
                miscWidth = Math.max(miscWidth, mc.fontRendererObj.getStringWidth(string));
            }
        }

        //Draw the header lines
        if (headerLines != null) {
            drawRect(screenWidth / 2 - miscWidth / 2 - 1, yPositionStart - 1, screenWidth / 2 + miscWidth / 2 + 1, yPositionStart + headerLines.size() * mc.fontRendererObj.FONT_HEIGHT, -2147483648);

            for (String string : headerLines) {
                mc.fontRendererObj.drawStringWithShadow(string, (float) (screenWidth / 2 - mc.fontRendererObj.getStringWidth(string) / 2), (float) yPositionStart, -1);
                yPositionStart += mc.fontRendererObj.FONT_HEIGHT;
            }

            yPositionStart += 1;
        }

        drawRect(screenWidth / 2 - miscWidth / 2 - 1, yPositionStart - 1, screenWidth / 2 + miscWidth / 2 + 1, yPositionStart + playersPerColumn * 9, -2147483648);

        for(int i = 0; i < playersPerColumn * columnAmount; ++i) {
            int column = i / playersPerColumn;
            int row = i % playersPerColumn;

            int xPosition = xPositionStart + column * columnWidth + column * 5;
            int yPosition = yPositionStart + row * 9;

            //Draw the background
            drawRect(xPosition, yPosition, xPosition + columnWidth, yPosition + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (i >= playerList.size()) continue;

            NetworkPlayerInfo playerInfo = playerList.get(i);
            String playerName = getPlayerName(playerInfo);
            GameProfile gameProfile = playerInfo.getGameProfile();

            //Draw the skins
            if (serverIsSecure) {
                EntityPlayer player = mc.theWorld.getPlayerEntityByUUID(gameProfile.getId());
                boolean flip = player != null && player.isWearing(EnumPlayerModelParts.CAPE) && (gameProfile.getName().equals("Dinnerbone") || gameProfile.getName().equals("Grumm"));
                mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());

                int headSize = 8 + (flip ? 8 : 0);
                int verticalFlip = 8 * (flip ? -1 : 1);
                Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, 8.0F, (float) headSize, 8, verticalFlip, 8, 8, 64.0F, 64.0F);

                //Draw the hat if present
                if (player != null && player.isWearing(EnumPlayerModelParts.HAT)) {
                    Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, 40.0F, (float) headSize, 8, verticalFlip, 8, 8, 64.0F, 64.0F);
                }

                xPosition += 9;
            }

            if (playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR) {
                //Spectators are in italics
                playerName = EnumChatFormatting.ITALIC + playerName;
                mc.fontRendererObj.drawStringWithShadow(playerName, (float) xPosition, (float) yPosition, -1862270977);
            } else {
                mc.fontRendererObj.drawStringWithShadow(playerName, (float) xPosition, (float) yPosition, -1);
            }

            int extraXPosition = xPosition + maxNameWidth + extraPaddingNeeded;

            List<String> playerStats = statInfo.get(i);
            for (int statIndex = 0; statIndex < playerStats.size(); statIndex++) {
                String string = playerStats.get(statIndex);
                int startPosition = extraXPosition - mc.fontRendererObj.getStringWidth(string);

                mc.fontRendererObj.drawStringWithShadow(string, startPosition, yPosition, -1);

                extraXPosition -= paddingPixels + maxStatWidth.get(statIndex);
            }

            this.drawPing(columnWidth, xPosition - (serverIsSecure ? 9 : 0), yPosition, playerInfo);
        }

        if (footerLines != null) {
            yPositionStart += playersPerColumn * 9 + 1;
            drawRect(screenWidth / 2 - miscWidth / 2 - 1, yPositionStart - 1, screenWidth / 2 + miscWidth / 2 + 1, yPositionStart + footerLines.size() * mc.fontRendererObj.FONT_HEIGHT, -2147483648);

            for (String string : footerLines) {
                mc.fontRendererObj.drawStringWithShadow(string, (float) (screenWidth / 2 - mc.fontRendererObj.getStringWidth(string) / 2), (float)yPositionStart, -1);
                yPositionStart += mc.fontRendererObj.FONT_HEIGHT;
            }
        }

    }

    @Override
    public void setFooter(IChatComponent footer) {
        this.footer = footer;
    }

    @Override
    public void setHeader(IChatComponent header) {
        this.header = header;
    }

    @Override
    public void func_181030_a() {
        header = null;
        footer = null;
    }

    @SideOnly(Side.CLIENT)
    static class PlayerComparator implements Comparator<NetworkPlayerInfo> {
        private PlayerComparator() {
        }

        public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_) {
            ScorePlayerTeam lvt_3_1_ = p_compare_1_.getPlayerTeam();
            ScorePlayerTeam lvt_4_1_ = p_compare_2_.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(lvt_3_1_ != null ? lvt_3_1_.getRegisteredName() : "", lvt_4_1_ != null ? lvt_4_1_.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
        }
    }

    public void prerender(List<NetworkPlayerInfo> players) {
        Set<UUID> uuids = new LinkedHashSet<>();

        for (NetworkPlayerInfo info : players) {
            UUID playerUUID = info.getGameProfile().getId();
            if (playerUUID != null) uuids.add(playerUUID);
        }

        Server server = StatsPuller.getServer();

        friendGroups.clear();
        for (UUID playerUUID : uuids) {
            Set<UUID> friends = server.getFriends(playerUUID, false);
            Set<UUID> onlineFriends = new TreeSet<>();

            for (UUID nextPlayerUUID : uuids) {
                if (friends.contains(nextPlayerUUID)) onlineFriends.add(nextPlayerUUID);
            }

            if (onlineFriends.size() > 1) friendGroups.add(onlineFriends);
        }

    }

    public List<String> getPlayerInfo(NetworkPlayerInfo player) {
        List<String> list = new LinkedList<>();

        Server server = StatsPuller.getServer();
        if (server == null) return list;

        UUID uuid = player.getGameProfile().getId();

        //Online UUIDs
        Collection<NetworkPlayerInfo> onlinePlayers = mc.thePlayer.sendQueue.getPlayerInfoMap();
        Set<UUID> uuids = new LinkedHashSet<>();

        for (NetworkPlayerInfo info : onlinePlayers) {
            UUID playerUUID = info.getGameProfile().getId();
            if (playerUUID != null) uuids.add(playerUUID);
        }

        //Nickname
        if (uuid == null || !server.isValid(uuid, false)) {
            list.add("+");
            return list;
        }

        //Guild
        String guildId = server.getGuildId(uuid, false);

        if (uuids.stream().anyMatch(otherPlayer -> {
            if (otherPlayer == uuid) return false;

            String otherGuildId = server.getGuildId(otherPlayer, false);
            return otherGuildId != null && otherGuildId.equals(guildId);
        })) {
            list.add(server.getGuildName(guildId, false));
        } else list.add("");

        //Minigame-specific
        Backend backend = server.getApplicableBackend(false);
        if (backend == null) return list;

        //KDR
        double deaths = backend.getDeaths(uuid, false);
        if (deaths == 0) deaths = 1;

        list.add(formatKdr(Math.round((backend.getKills(uuid, false) / deaths) * 100) / 100d));
        list.add(backend.getExtra(uuid, false));

        //Friends
        //friendGroups is a list of friend groups. Each friend group is a list of friend

        for (Set<UUID> friendGroup : friendGroups) {
            if (friendGroup.contains(uuid)) list.add("*");
            else list.add("");
        }

        return list;
    }

    public String formatKdr(double kdr) {
        String str = "§";

        if (kdr <= 0.5) str += "7";
        else if (kdr <= 1) str += "2";
        else if (kdr <= 2) str += "1";
        else if (kdr <= 4) str += "6";
        else if (kdr <= 8) str += "c";
        else str += "4";

        return str + kdr;
    }
}
