package tallestred.goathornblock;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Instrument;
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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;
import tallestred.goathornblock.common.blocks.GoatHornBlock;
import tallestred.goathornblock.config.GHBMConfig;

@SuppressWarnings("unused")
@Mod(GoatHornBlockMod.MODID)
public class GoatHornBlockMod {
    public static final String MODID = "goat_horn_block_mod";
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredHolder<Block, GoatHornBlock> GOAT_HORN = BLOCKS.register("goat_horn_amplifier", () -> new GoatHornBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).randomTicks().destroyTime(0.25F), GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY::get));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoatHornBlockEntity>> GOAT_HORN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("goat_horn_block_entity", () -> BlockEntityType.Builder.of(GoatHornBlockEntity::new, GOAT_HORN.get()).build(null));

    public GoatHornBlockMod(ModContainer container, IEventBus modEventBus, Dist dist) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, GHBMConfig.COMMON_SPEC);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onSoundPlayed(PlayLevelSoundEvent.AtPosition event) {
        if (event.getSource() != SoundSource.AMBIENT && event.getSource() != SoundSource.VOICE && event.getSource() != SoundSource.MASTER) {
            Level level = event.getLevel();
            BlockPos soundPosition = BlockPos.containing(event.getPosition());
            int soundRange = GHBMConfig.COMMON.goatHornSoundRange.get();
            ChunkAccess soundChunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(soundPosition.getX()), SectionPos.blockToSectionCoord(soundPosition.getZ()));
            if (level.isClientSide && Minecraft.getInstance().getConnection() == null && (soundChunk == null || !soundChunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)))
                return;
            BlockPos blockPos = BlockPos.findClosestMatch(soundPosition, soundRange, soundRange, (block) -> level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())) != null && level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())).getPersistedStatus().isOrAfter(ChunkStatus.FULL) && level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())).getBlockState(block).getBlock() instanceof GoatHornBlock).orElse(null);
            if (blockPos != null) {
                ChunkAccess goatPosChunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
                if (goatPosChunk != null && goatPosChunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL) && goatPosChunk.getBlockEntity(blockPos) instanceof GoatHornBlockEntity goatHornBlockEntity && level.getBestNeighborSignal(blockPos) >= 1) {
                    for (int i = 0; i < GHBMConfig.COMMON.amountOfSoundsAbleToBePlayedByGoatHorn.get(); i++) {
                        goatHornBlockEntity.setSoundEvent(i, event.getSound().value().getLocation());
                    }
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
            BlockState originalBlock = player.level().getBlockState(pos);
            GoatHornBlock goatHornBlock = GoatHornBlockMod.GOAT_HORN.get();
            BlockPlaceContext placeContext = new BlockPlaceContext(player.level(), player, event.getHand(), playerItemStack, event.getHitVec());
            if (player.level().setBlock(pos, goatHornBlock.getStateForPlacement(placeContext), 11)) {
                player.swing(event.getHand());
                BlockEntity blockentity = player.level().getBlockEntity(pos);
                if (blockentity instanceof GoatHornBlockEntity) {
                    blockentity.applyComponentsFromItemStack(playerItemStack);
                    blockentity.setChanged();
                }
                player.level().gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, GoatHornBlockMod.GOAT_HORN.get().getStateForPlacement(placeContext)));
                SoundType soundtype = originalBlock.getSoundType(player.level(), pos, player);
                player.level().playSound(player, pos, originalBlock.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (!player.getAbilities().instabuild)
                    playerItemStack.shrink(1);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}




