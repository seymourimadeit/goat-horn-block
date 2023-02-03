package tallestred.goathornblock.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class GoatHornBlock extends DirectionalBlock {
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(4, -1, 0, 12, 7, 11), Block.box(6, 0, 11, 10, 4, 17));
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(4, 2, 5, 12, 10, 16), Block.box(6, 0, -1, 10, 4, 5));
    protected static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(5, 0, 4, 16, 8, 12), Block.box(0, 0, 6, 6, 4, 10));
    protected static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4, 5, 4, 12, 16, 12), Block.box(6, -1, 6, 10, 5, 10));
    protected static final VoxelShape SHAPE_DOWN= Shapes.or(Block.box(4, 1, 4, 12, 12, 12), Block.box(6, 9, 10, 10, 15, 14));
    protected static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1, 0, 4, 12, 8, 12), Block.box(11, 1, 6, 17, 5, 10));

    public GoatHornBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
}
