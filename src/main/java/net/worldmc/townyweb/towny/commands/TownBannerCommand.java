package net.worldmc.townyweb.towny.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.worldmc.townyweb.sets.ItemStacks;
import org.bukkit.Tag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class TownBannerCommand implements CommandExecutor {

    private static final String PERMISSION = "towny.command.town.set.banner";
    private static final String METADATA_KEY = "banner";

    private static final Component PERM_DENY_MSG = Component.text("You do not have permission to use this command!", NamedTextColor.RED);
    private static final Component PLAYER_ONLY_MSG = Component.text("This command can only be used by players.", NamedTextColor.RED);
    private static final Component NO_BANNER_MSG = Component.text("You must be holding a banner to use this command.", NamedTextColor.RED);
    private static final Component NOT_MAYOR_MSG = Component.text("Only the mayor can change the town banner.", NamedTextColor.RED);
    private static final Component NO_TOWN_MSG = Component.text("You must be in a town to use this command.", NamedTextColor.RED);

    private final Gson gson;

    public TownBannerCommand() {
        this.gson = new GsonBuilder().create();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PLAYER_ONLY_MSG);
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(PERM_DENY_MSG);
            return true;
        }

        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            player.sendMessage(NO_TOWN_MSG);
            return true;
        }

        Town town = resident.getTownOrNull();
        if (town == null) {
            player.sendMessage(NO_TOWN_MSG);
            return true;
        }

        if (!resident.isMayor()) {
            player.sendMessage(NOT_MAYOR_MSG);
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!Tag.BANNERS.isTagged(heldItem.getType())) {
            player.sendMessage(NO_BANNER_MSG);
            return true;
        }

        try {
            Map<String, Object> bannerData = ItemStacks.getItemStack(heldItem);
            String bannerJson = gson.toJson(bannerData);
            StringDataField bannerMetadata = new StringDataField(METADATA_KEY, bannerJson);
            town.addMetaData(bannerMetadata);
        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while setting the banner.", NamedTextColor.RED));
            e.printStackTrace();
            return true;
        }

        TownyMessaging.sendPrefixedTownMessage(town, resident.getName() + " has changed the town banner.");

        return true;
    }
}