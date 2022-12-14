package cc.mewcraft.betonquest.itemsadder;

import cc.mewcraft.betonquest.util.ItemsAdderUtil;
import dev.lone.itemsadder.api.CustomBlock;
import lombok.CustomLog;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.location.LocationData;
import org.bukkit.Location;

@CustomLog
public class SetBlockEvent extends QuestEvent {

    private final String namespacedID;
    private final LocationData locationData;

    public SetBlockEvent(Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        namespacedID = instruction.next() + ":" + instruction.next();
        ItemsAdderUtil.validateCustomBlockSilently(instruction.getPackage(), namespacedID);
        locationData = instruction.getLocation().getLocationData();
    }

    @Override
    protected Void execute(Profile profile) throws QuestRuntimeException {
        CustomBlock cs = CustomBlock.getInstance(namespacedID);
        Location location = locationData.get(profile);
        cs.place(location);
        return null;
    }

}
