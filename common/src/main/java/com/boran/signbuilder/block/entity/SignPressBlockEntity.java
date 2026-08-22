package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.menu.SignPressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignPressBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyInputHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyOutputHandler = LazyOptional.empty();

    public SignPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIGN_PRESS_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.signbuilder.sign_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SignPressMenu(id, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) {
                return lazyOutputHandler.cast();
            }
            else {
                return lazyInputHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        lazyInputHandler = LazyOptional.of(() -> new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(0); }
            @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return itemHandler.insertItem(0, stack, simulate);
            }
            @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return itemHandler.extractItem(0, amount, simulate);
            }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(0); }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return itemHandler.isItemValid(0, stack); }
        });

        lazyOutputHandler = LazyOptional.of(() -> new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(1); }
            @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return stack;
            }
            @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return itemHandler.extractItem(1, amount, simulate);
            }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(1); }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
        });
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyInputHandler.invalidate();
        lazyOutputHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }
}