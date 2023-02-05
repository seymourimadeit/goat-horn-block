package tallestred.goathornblock.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;

import javax.annotation.Nullable;

public class GoatHornBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(4, -1, 0, 12, 7, 11), Block.box(6, 0, 11, 10, 4, 17));
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(4, 2, 5, 12, 10, 16), Block.box(6, 0, -1, 10, 4, 5));
    protected static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(5, 0, 4, 16, 8, 12), Block.box(0, 0, 6, 6, 4, 10));
    protected static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4, 5, 4, 12, 16, 12), Block.box(6, -1, 6, 10, 5, 10));
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(Block.box(4, 1, 4, 12, 12, 12), Block.box(6, 9, 10, 10, 15, 14));
    protected static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1, 0, 4, 12, 8, 12), Block.box(11, 1, 6, 17, 5, 10));

    public GoatHornBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof GoatHornBlockEntity horn) {
            if (horn.getGoatHornItemDrop() == null)
                return ItemStack.EMPTY;
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
                if (hornBlockEntity.getGoatHornItemDrop() == null)
                    return;
                ItemStack stack = hornBlockEntity.getGoatHornItemDrop().copy();
                Direction direction = pState.getValue(FACING);
                float f = 0.25F * (float) direction.getStepX();
                float f1 = 0.25F * (float) direction.getStepZ();
                ItemEntity itementity = new ItemEntity(pLevel, (double) pPos.getX() + 0.5D + (double) f, (double) (pPos.getY() + 1), (double) pPos.getZ() + 0.5D + (double) f1, stack);
                itementity.setDefaultPickUpDelay();
                pLevel.addFreshEntity(itementity);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
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
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
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
        pBuilder.add(FACING);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GoatHornBlockEntity(pPos, pState);
    }
}
