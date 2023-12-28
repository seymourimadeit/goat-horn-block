package tallestred.goathornblock.config;


import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import tallestred.goathornblock.GoatHornBlockMod;

public class GHBMConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public final ModConfigSpec.IntValue amountOfSoundsAbleToBePlayedByGoatHorn;
        public final ModConfigSpec.IntValue goatHornRedstoneSoundLimit;
        public final ModConfigSpec.IntValue goatHornSoundRange;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("goat horn");
            amountOfSoundsAbleToBePlayedByGoatHorn = builder.comment("The maximum amount of sounds able to be played the goat horn is defined here.").defineInRange("Maximum amount", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            goatHornRedstoneSoundLimit = builder.defineInRange("Defines how far a goat horn block from the other end of redstone can be in order to get sounds to transfer", 90, Integer.MIN_VALUE, Integer.MAX_VALUE);
            goatHornSoundRange = builder.defineInRange("Range for the sounds heard by a goat horn block", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
        }
    }
}
