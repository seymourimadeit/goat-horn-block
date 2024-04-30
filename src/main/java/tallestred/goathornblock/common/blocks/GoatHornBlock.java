package tallestred.goathornblock.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tallestred.goathornblock.GoatHornBlockMod;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GoatHornBlock extends BaseEntityBlock {
    public static final MapCodec<GoatHornBlock> CODEC = simpleCodec(p_304364_ -> new GoatHornBlock(p_304364_, () -> GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get()));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty SOUND = BooleanProperty.create("sound");
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(4, -1, 0, 12, 7, 11), Block.box(6, 0, 11, 10, 4, 17));
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(4, 2, 5, 12, 10, 16), Block.box(6, 0, -1, 10, 4, 5));
    protected static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(5, 0, 4, 16, 8, 12), Block.box(0, 0, 6, 6, 4, 10));
    protected static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4, 5, 4, 12, 16, 12), Block.box(6, -1, 6, 10, 5, 10));
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(Block.box(4, 1, 4, 12, 12, 12), Block.box(6, 9, 10, 10, 15, 14));
    protected static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1, 0, 4, 12, 8, 12), Block.box(11, 1, 6, 17, 5, 10));

    public GoatHornBlock(Properties pProperties, Supplier<BlockEntityType<? extends GoatHornBlockEntity>> supplier) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(SOUND, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof GoatHornBlockEntity horn)
            return InstrumentItem.create(Items.GOAT_HORN, horn.components().get(DataComponents.INSTRUMENT));
        else return super.getCloneItemStack(state, target, level, pos, player);
    }

    public void setSounds(GoatHornBlockEntity horn, Level level, BlockPos pos, BlockState state) {
        if (state.getValue(SOUND)) {
            for (int i = 0; i < horn.getSounds().size(); ++i) {
                SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(horn.getSounds().get(i));
                if (sound != null) {
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                level.setBlock(pos, state.setValue(GoatHornBlock.SOUND, Boolean.valueOf(false)), 3);
                level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
            }
        }
    }


    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof GoatHornBlockEntity horn) {
            boolean neighborSignal = pLevel.hasNeighborSignal(pPos);
            if (neighborSignal != pState.getValue(POWERED)) {
                if (neighborSignal) {
                    Holder<Instrument> instrumentHolder = horn.getInstrument();
                    if (instrumentHolder != null) {
                        SoundEvent soundevent = instrumentHolder.value().soundEvent().value();
                        float volume = instrumentHolder.value().range() / 16.0F;
                        pLevel.playSound(null, pPos, soundevent, SoundSource.BLOCKS, volume, 1.0F);
                        pLevel.gameEvent(GameEvent.INSTRUMENT_PLAY, pPos, GameEvent.Context.of(pState));
                    }
                }
                pLevel.setBlock(pPos, pState.setValue(POWERED, neighborSignal), 3);
            }
        }
        super.onNeighborChange(pState, pLevel, pPos, pFromPos);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) this.powerOn(pLevel, pPos, pState);
    }

    public void powerOn(Level level, BlockPos pos, BlockState blockState) {
        for (Direction blockDirection : Direction.values()) {
            BlockPos nearestPos = pos.relative(blockDirection, 2);
            if (level.hasNeighborSignal(nearestPos))
                level.setBlock(pos, blockState.setValue(POWERED, Boolean.valueOf(true)), 3);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get(), GoatHornBlockEntity::serverTick);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case DOWN:
                return SHAPE_DOWN;
            case UP:
                return SHAPE_UP;
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite()).setValue(POWERED, false);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED, SOUND);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GoatHornBlockEntity(pPos, pState);
    }
}
