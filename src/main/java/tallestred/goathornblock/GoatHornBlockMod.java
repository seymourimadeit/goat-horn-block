package tallestred.goathornblock;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import tallestred.goathornblock.common.blocks.GoatHornBlock;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GoatHornBlockMod.MODID)
public class GoatHornBlockMod {
    public static final String MODID = "goat_horn_block_mod";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> GOAT_HORN = BLOCKS.register("goat_horn_amplifier", () -> new GoatHornBlock(BlockBehaviour.Properties.of(Material.STONE)));
    private static final Logger LOGGER = LogUtils.getLogger();

    public GoatHornBlockMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }


    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack playerItemStack = event.getItemStack();
        Player player = event.getEntity();
        if (playerItemStack.getItem() == Items.GOAT_HORN) {
            player.swing(InteractionHand.MAIN_HAND);
            BlockPos pos = event.getHitVec().getBlockPos().relative(event.getHitVec().getDirection());
            player.getLevel().setBlock(pos, GoatHornBlockMod.GOAT_HORN.get().defaultBlockState(), 11);
            player.getLevel().gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, GoatHornBlockMod.GOAT_HORN.get().defaultBlockState()));
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
