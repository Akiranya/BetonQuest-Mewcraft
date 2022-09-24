package cc.mewcraft.betonquest.objectives;

import dev.lone.itemsadder.api.CustomStack;
import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@CustomLog(topic = "BetonQuestItemsAdder")
public class PickupItem extends Objective implements Listener {

    private final String namespacedID;
    private final int amount;
    private final boolean notify;

    public PickupItem(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = PickupData.class;
        notify = instruction.hasArgument("notify");
        amount = instruction.getInt();
        if (amount < 1) {
            throw new InstructionParseException("Amount cannot be less than 1");
        }
        namespacedID = instruction.next() + ":" + instruction.next();
        CustomStack cs = CustomStack.getInstance(namespacedID);
        if (cs == null) {
            throw new InstructionParseException("Unknown item ID: " + namespacedID);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!isInvalidItem(e.getItem().getItemStack())) return;
        String playerID = PlayerConverter.getID(player);
        if (!containsPlayer(playerID) || !checkConditions(playerID)) {
            return;
        }
        PickupData playerData = getPickupData(playerID);
        ItemStack pickupItem = e.getItem().getItemStack();
        playerData.pickup(pickupItem.getAmount());
        if (playerData.isFinished()) {
            completeObjective(playerID);
            return;
        }
        if (notify)
            try {
                Config.sendNotify(instruction.getPackage().getPackagePath(), playerID, "items_to_pickup", new String[]{Integer.toString(playerData.getAmount())}, "items_to_pickup,info");
            } catch (QuestRuntimeException ex1) {
                try {
                    LOG.warn("The notify system was unable to play a sound for the 'items_to_pickup' category in '" + instruction.getObjective().getFullID() + "'. Error was: '" + ex1.getMessage() + "'");
                } catch (InstructionParseException ex2) {
                    LOG.reportException(ex2);
                }
            }
    }

    private boolean isInvalidItem(ItemStack is) {
        CustomStack cs = CustomStack.byItemStack(is);
        return cs != null && cs.getNamespacedID().equalsIgnoreCase(namespacedID);
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return Integer.toString(amount);
    }

    @Override
    public String getProperty(String name, String playerID) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "left" -> Integer.toString(getPickupData(playerID).getAmount());
            case "amount" -> Integer.toString(amount);
            default -> "";
        };
    }

    private PickupData getPickupData(String playerID) {
        return (PickupData) dataMap.get(playerID);
    }

    public static class PickupData extends Objective.ObjectiveData {
        private int amount;

        public PickupData(String instruction, String playerID, String objID) {
            super(instruction, playerID, objID);
            amount = Integer.parseInt(instruction);
        }

        private void pickup(int pickupAmount) {
            this.amount -= pickupAmount;
            update();
        }

        private boolean isFinished() {
            return (amount < 1);
        }

        private int getAmount() {
            return amount;
        }

        public String toString() {
            return Integer.toString(amount);
        }
    }
}
