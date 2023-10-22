package tallestred.goathornblock;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;
import tallestred.goathornblock.common.blocks.GoatHornBlock;
import tallestred.goathornblock.config.GHBMConfig;

@Mod(GoatHornBlockMod.MODID)
public class GoatHornBlockMod {
    public static final String MODID = "goat_horn_block_mod";
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<GoatHornBlock> GOAT_HORN = BLOCKS.register("goat_horn_amplifier", () -> new GoatHornBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().requiresCorrectToolForDrops().destroyTime(0.25F)));
    public static final RegistryObject<BlockEntityType<GoatHornBlockEntity>> GOAT_HORN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("goat_horn_block_entity", () -> BlockEntityType.Builder.of(GoatHornBlockEntity::new, GOAT_HORN.get()).build(null));
    // Why does the autoformatter keep putting the above line in weird ass places?

    public GoatHornBlockMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GHBMConfig.COMMON_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onSoundPlayed(PlayLevelSoundEvent.AtPosition event) {
        if (event.getSource() != SoundSource.AMBIENT && event.getSource() != SoundSource.VOICE && event.getSource() != SoundSource.MASTER) {
            Level level = event.getLevel();
            BlockPos soundPosition = new BlockPos((int) event.getPosition().x(), (int) event.getPosition().y(), (int) event.getPosition().z());
            int soundRange = GHBMConfig.COMMON.goatHornSoundRange.get();
            BlockPos blockPos = BlockPos.findClosestMatch(soundPosition, soundRange, soundRange, (block) -> level.isLoaded(block) && level.getBlockEntity(block) instanceof GoatHornBlockEntity).orElse(null);
            if (blockPos != null && level.getBlockEntity(blockPos) instanceof GoatHornBlockEntity goatHornBlockEntity && level.getBestNeighborSignal(blockPos) >= 1) {
                if (level.isClientSide && Minecraft.getInstance().getConnection() == null)
                    return;
                for (int i = 0; i < GHBMConfig.COMMON.amountOfSoundsAbleToBePlayedByGoatHorn.get(); i++) {
                    goatHornBlockEntity.setSoundEvent(i, event.getSound().getLocation());
                }
            }
        }
    }

    @SubscribeEvent
    public void toolTip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == Items.GOAT_HORN) {
            event.getToolTip().add(Component.translatable("item.goathornblockmod.goathorn.desc").withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack playerItemStack = event.getItemStack();
        Player player = event.getEntity();
        if (playerItemStack.getItem() == Items.GOAT_HORN && player.isCrouching()) {
            BlockPos pos = event.getHitVec().getBlockPos().relative(event.getHitVec().getDirection());
            BlockState originalBlock = player.getLevel().getBlockState(pos);
            GoatHornBlock goatHornBlock = GoatHornBlockMod.GOAT_HORN.get();
            BlockPlaceContext placeContext = new BlockPlaceContext(player.getLevel(), player, event.getHand(), playerItemStack, event.getHitVec());
            if (player.getLevel().setBlock(pos, goatHornBlock.getStateForPlacement(placeContext), 11)) {
                player.swing(event.getHand());
                BlockEntity blockentity = player.getLevel().getBlockEntity(pos);
                if (blockentity instanceof GoatHornBlockEntity goatHornBlockEntity) {
                    goatHornBlockEntity.setGoatHornItemDrop(playerItemStack.copy());
                }
                player.getLevel().gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, GoatHornBlockMod.GOAT_HORN.get().getStateForPlacement(placeContext)));
                SoundType soundtype = originalBlock.getSoundType(player.getLevel(), pos, player);
                player.getLevel().playSound(player, pos, originalBlock.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (!player.getAbilities().instabuild)
                    playerItemStack.shrink(1);
            }
        }
    }




}


