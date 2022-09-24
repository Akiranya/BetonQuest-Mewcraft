package cc.mewcraft.betonquest.conditions;

import dev.lone.itemsadder.api.CustomStack;
import lombok.CustomLog;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.inventory.ItemStack;

@CustomLog(topic = "BetonQuestItemsAdder")
public class HasItemInHand extends Condition {

    private final String namespacedID;
    private final int amount;

    public HasItemInHand(Instruction instruction) throws InstructionParseException {
        super(instruction, true);
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

    @Override
    protected Boolean execute(String playerID) {
        ItemStack handItem = PlayerConverter.getPlayer(playerID).getInventory().getItemInMainHand();
        CustomStack cs = CustomStack.byItemStack(handItem);
        if (cs != null && cs.getNamespacedID().equalsIgnoreCase(namespacedID)) {
            return handItem.getAmount() >= amount;
        }
        return false;
    }
}