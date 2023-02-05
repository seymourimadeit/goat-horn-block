package tallestred.goathornblock;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;
import tallestred.goathornblock.common.blocks.GoatHornBlock;

@Mod(GoatHornBlockMod.MODID)
public class GoatHornBlockMod {
    public static final String MODID = "goat_horn_block_mod";
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<GoatHornBlock> GOAT_HORN = BLOCKS.register("goat_horn_amplifier", () -> new GoatHornBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<BlockEntityType<GoatHornBlockEntity>> GOAT_HORN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("goat_horn_block_entity", () -> BlockEntityType.Builder.of(GoatHornBlockEntity::new, GOAT_HORN.get()).build(null));
    private static final Logger LOGGER = LogUtils.getLogger();

    public GoatHornBlockMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
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
            player.swing(event.getHand());
            BlockPos pos = event.getHitVec().getBlockPos().relative(event.getHitVec().getDirection());
            BlockState originalBlock =  player.getLevel().getBlockState(pos);
            GoatHornBlock goatHornBlock =  GoatHornBlockMod.GOAT_HORN.get();
            BlockPlaceContext placeContext = new BlockPlaceContext(player.getLevel(), player, event.getHand(), playerItemStack, event.getHitVec());
            player.getLevel().setBlock(pos, goatHornBlock.getStateForPlacement(placeContext), 11);
            BlockEntity blockentity =  player.getLevel().getBlockEntity(pos);
            if (blockentity instanceof GoatHornBlockEntity goatHornBlockEntity) {
                goatHornBlockEntity.setGoatHornItemDrop(playerItemStack.copy());
            }
            player.getLevel().gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, GoatHornBlockMod.GOAT_HORN.get().getStateForPlacement(placeContext)));
            SoundType soundtype =  originalBlock.getSoundType(player.getLevel(), pos, player);
            player.getLevel().playSound(player, pos, originalBlock.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
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
