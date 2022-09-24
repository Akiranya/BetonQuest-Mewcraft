package cc.mewcraft.betonquest.objectives;

import dev.lone.itemsadder.api.CustomStack;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class SmeltingItem extends Objective implements Listener {

    private final String namespacedID;
    private final int amount;

    public SmeltingItem(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = SmeltData.class;
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

    @EventHandler
    public void onItemGet(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getInventory().getType() == InventoryType.FURNACE || e.getInventory().getType() == InventoryType.BLAST_FURNACE) {
            if (e.getRawSlot() == 2) {

                String playerID = PlayerConverter.getID(player);
                if (containsPlayer(playerID)) {
                    CustomStack cs = CustomStack.byItemStack(e.getCurrentItem());
                    if (cs != null && cs.getNamespacedID().equalsIgnoreCase(namespacedID)) {
                        if (checkConditions(playerID)) {
                            SmeltData playerData = (SmeltData) dataMap.get(playerID);
                            playerData.subtract(e.getCurrentItem().getAmount());
                            if (playerData.isZero()) {
                                completeObjective(playerID);
                            }
                        }
                    }
                }

            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShiftSmelting(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.FURNACE
            && event.getRawSlot() == 2
            && event.getClick() == ClickType.SHIFT_LEFT
            && event.getWhoClicked() instanceof Player player) {
            String playerID = PlayerConverter.getID(player);
            if (containsPlayer(playerID)) {
                event.setCancelled(true);
            }
        }
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
        if ("left".equalsIgnoreCase(name))
            return Integer.toString(amount - ((SmeltData) dataMap.get(playerID)).getAmount());
        if ("amount".equalsIgnoreCase(name))
            return Integer.toString(((SmeltData) dataMap.get(playerID)).getAmount());
        return "";
    }

    public static class SmeltData extends Objective.ObjectiveData {
        private int amount;

        public SmeltData(String instruction, String playerID, String objID) {
            super(instruction, playerID, objID);
            amount = Integer.parseInt(instruction);
        }

        private int getAmount() {
            return amount;
        }

        private void subtract(int amount) {
            this.amount -= amount;
            update();
        }

        private boolean isZero() {
            return (amount < 1);
        }

        public String toString() {
            return Integer.toString(amount);
        }
    }
}