package tallestred.goathornblock.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tallestred.goathornblock.GoatHornBlockMod;
import tallestred.goathornblock.common.blocks.GoatHornBlock;
import tallestred.goathornblock.config.GHBMConfig;

import java.util.ArrayList;

public class GoatHornBlockEntity extends BlockEntity {
    protected final String GOAT_HORN_ITEM = "goat_horn_id";
    protected final String GOAT_HORN_INSTRUMENT = "goat_horn_instrument";
    protected final String GOAT_SOUND_EVENT = "goat_sound";
    protected ArrayList<ResourceLocation> sounds = new ArrayList<>();
    private Holder<Instrument> instrument;

    public GoatHornBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, GoatHornBlockEntity pBlockEntity) {
        for (Direction direction : Direction.values()) {
            Direction oppositeFacingDirection = pState.getValue(GoatHornBlock.FACING).getOpposite();
            if (direction == oppositeFacingDirection && pState.getValue(GoatHornBlock.POWERED)) {
                for (int i = 1; i < GHBMConfig.COMMON.goatHornRedstoneSoundLimit.get(); ++i) {
                    BlockPos blockpos = pPos.relative(direction, i);
                    BlockState blockstate = pLevel.getBlockState(blockpos);
                    if (blockstate.getBlock() instanceof GoatHornBlock hornBlock && pLevel.getBlockState(blockpos.relative(oppositeFacingDirection.getOpposite(), 1)).getBlock() instanceof RedStoneWireBlock) {
                        if (pLevel.getBlockEntity(blockpos) instanceof GoatHornBlockEntity) {
                            pLevel.setBlock(blockpos, blockstate.setValue(GoatHornBlock.SOUND, Boolean.valueOf(true)), 3);
                            if (blockstate.getValue(GoatHornBlock.SOUND) && pBlockEntity.getSounds() != null && blockstate.getValue(GoatHornBlock.POWERED))
                                hornBlock.setSounds(pBlockEntity, pLevel, blockpos, blockstate);
                            pBlockEntity.getSounds().clear(); // Stops sounds from playing repeatedly
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockEntityType<?> getType() {
        return GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get();
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider lookup) {
        super.loadAdditional(pTag, lookup);
        if (pTag.contains(GOAT_HORN_INSTRUMENT))
            Instrument.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), pTag.get(GOAT_HORN_INSTRUMENT)).ifSuccess(i -> instrument = i);
        if (pTag.contains(GOAT_HORN_ITEM, 10))
            this.instrument = ItemStack.parseOptional(lookup, pTag.getCompound(GOAT_HORN_ITEM)).get(DataComponents.INSTRUMENT); //For forwards compat, hope this works
        if (pTag.contains(GOAT_SOUND_EVENT)) {
            for (int i = 0; i < this.getSounds().size(); ++i) {
                this.getSounds().set(i, ResourceLocation.tryParse(GOAT_SOUND_EVENT));
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        return this.saveWithoutMetadata(lookup); //TODO is this still true??
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider lookup) {
        super.saveAdditional(pTag, lookup);
        if (this.instrument != null)
            pTag.put(GOAT_HORN_INSTRUMENT, Instrument.CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), this.instrument).getOrThrow());
        if (this.getSounds() != null) {
            for (int i = 0; i < this.getSounds().size(); ++i) {
                pTag.putString(GOAT_SOUND_EVENT, this.getSounds().get(i).toString());
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        this.instrument = input.get(DataComponents.INSTRUMENT);

    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.INSTRUMENT, instrument);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(GOAT_HORN_ITEM);
    }

    public Holder<Instrument> getInstrument() {
        return this.instrument;
    }

    public ArrayList<ResourceLocation> getSounds() {
        return sounds;
    }

    public void setSoundEvent(int index, ResourceLocation soundEvent) {
        this.sounds.add(index, soundEvent);
        this.setChanged();
    }
}

