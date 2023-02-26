package tallestred.goathornblock.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import tallestred.goathornblock.GoatHornBlockMod;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;

import javax.annotation.Nullable;

public class GoatHornBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty SOUND = BooleanProperty.create("sound");
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(4, -1, 0, 12, 7, 11), Block.box(6, 0, 11, 10, 4, 17));
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(4, 2, 5, 12, 10, 16), Block.box(6, 0, -1, 10, 4, 5));
    protected static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(5, 0, 4, 16, 8, 12), Block.box(0, 0, 6, 6, 4, 10));
    protected static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4, 5, 4, 12, 16, 12), Block.box(6, -1, 6, 10, 5, 10));
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(Block.box(4, 1, 4, 12, 12, 12), Block.box(6, 9, 10, 10, 15, 14));
    protected static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1, 0, 4, 12, 8, 12), Block.box(11, 1, 6, 17, 5, 10));

    public GoatHornBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(SOUND, false));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof GoatHornBlockEntity horn) {
            if (horn.getGoatHornItemDrop() == null) return ItemStack.EMPTY;
            Holder<Instrument> instrumentHolder = ((InstrumentItem) horn.getGoatHornItemDrop().getItem()).getInstrument(horn.getGoatHornItemDrop()).get();
            ItemStack instrument = InstrumentItem.create(Items.GOAT_HORN, instrumentHolder);
            return instrument;
        } else {
            return super.getCloneItemStack(state, target, level, pos, player);
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof GoatHornBlockEntity hornBlockEntity) {
                if (hornBlockEntity.getGoatHornItemDrop() == null) return;
                ItemStack stack = hornBlockEntity.getGoatHornItemDrop().copy();
                Direction direction = pState.getValue(FACING);
                float f = 0.25F * (float) direction.getStepX();
                float f1 = 0.25F * (float) direction.getStepZ();
                ItemEntity itementity = new ItemEntity(pLevel, (double) pPos.getX() + 0.5D + (double) f, (double) (pPos.getY() + 1), (double) pPos.getZ() + 0.5D + (double) f1, stack);
                itementity.setDefaultPickUpDelay();
                pLevel.addFreshEntity(itementity);
                hornBlockEntity.setRemoved();
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public void setSounds(GoatHornBlockEntity horn, Level level, BlockPos pos, BlockState state) {
        if (state.getValue(SOUND)) {
            for (int i = 0; i < horn.getSounds().size(); ++i) {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(horn.getSounds().get(i));
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
                    if (horn.getGoatHornItemDrop() == null) return;
                    Holder<Instrument> instrumentHolder = ((InstrumentItem) horn.getGoatHornItemDrop().getItem()).getInstrument(horn.getGoatHornItemDrop()).get();
                    SoundEvent soundevent = instrumentHolder.get().soundEvent().value();
                    float volume = instrumentHolder.get().range() / 16.0F;
                    pLevel.playSound(null, pPos, soundevent, SoundSource.BLOCKS, volume, 1.0F);
                    pLevel.gameEvent(GameEvent.INSTRUMENT_PLAY, pPos, GameEvent.Context.of(pState));
                }
                pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(neighborSignal)), 3);
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
