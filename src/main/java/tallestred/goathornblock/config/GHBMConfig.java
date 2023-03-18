package tallestred.goathornblock.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import tallestred.goathornblock.GoatHornBlockMod;

@Mod.EventBusSubscriber(modid = GoatHornBlockMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GHBMConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.IntValue amountOfSoundsAbleToBePlayedByGoatHorn;
        public final ForgeConfigSpec.IntValue goatHornRedstoneSoundLimit;
        public final ForgeConfigSpec.IntValue goatHornSoundRange;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("goat horn");
            amountOfSoundsAbleToBePlayedByGoatHorn = builder.comment("The maximum amount of sounds able to be played the goat horn is defined here.").defineInRange("Maximum amount", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            goatHornRedstoneSoundLimit = builder.defineInRange("Defines how far a goat horn block from the other end of redstone can be in order to get sounds to transfer", 90, Integer.MIN_VALUE, Integer.MAX_VALUE);
            goatHornSoundRange = builder.defineInRange("Range for the sounds heard by a goat horn block", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
        }
    }
}
