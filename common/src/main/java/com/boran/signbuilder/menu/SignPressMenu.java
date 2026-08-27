package com.boran.signbuilder.menu;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.SignPressBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SignPressMenu extends AbstractContainerMenu {
    public final SignPressBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    public final Container resultContainer = new SimpleContainer(1);
    private String selectedBlock = "";
    private boolean craftMax = false;
    private boolean isQuickCrafting = false;

    public SignPressMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public SignPressMenu(int id, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.SIGN_PRESS_MENU.get(), id);
        blockEntity = (SignPressBlockEntity) entity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        this.addSlot(new Slot(blockEntity, 0, 26, 35) {
            @Override
            public void setChanged() {
                super.setChanged();
                setupResultSlot();
            }
        });

        this.addSlot(new Slot(resultContainer, 0, 143, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (!isQuickCrafting) {
                    ItemStack input = blockEntity.getItem(0);
                    input.shrink(stack.getCount() * 4);
                    setupResultSlot();
                }

                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);

                super.onTake(player, stack);
            }
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public void setSelectedBlock(String blockName, boolean craftMax) {
        this.selectedBlock = blockName;
        this.craftMax = craftMax;
        this.setupResultSlot();
    }

    public void setupResultSlot() {
        ItemStack input = blockEntity.getItem(0);

        if (!this.selectedBlock.isEmpty() && input.getItem() == Items.WHITE_CONCRETE && input.getCount() >= 4) {
            Item resultItem = BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", this.selectedBlock));
            if (resultItem != null && resultItem != Items.AIR) {

                int amountToCraft = 1;
                if (this.craftMax) {
                    amountToCraft = input.getCount() / 4;
                    amountToCraft = Math.min(amountToCraft, resultItem.getMaxStackSize());
                }

                this.resultContainer.setItem(0, new ItemStack(resultItem, amountToCraft));
                this.broadcastChanges();
                return;
            }
        }
        this.resultContainer.setItem(0, ItemStack.EMPTY);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.SIGN_PRESS.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 1) {
                this.isQuickCrafting = true;

                while (slot.hasItem()) {
                    ItemStack currentStack = slot.getItem().copy();
                    int originalCount = currentStack.getCount();

                    if (!this.moveItemStackTo(currentStack, 2, 38, true)) {
                        break;
                    }

                    int taken = originalCount - currentStack.getCount();
                    if (taken > 0) {
                        ItemStack input = blockEntity.getItem(0);
                        input.shrink(taken * 4);

                        slot.onTake(player, currentStack);
                        setupResultSlot();
                    } else {
                        break;
                    }
                }

                this.isQuickCrafting = false;
                return ItemStack.EMPTY;

            } else if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.getItem() == Items.WHITE_CONCRETE) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 2 && index < 29) {
                    if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29 && index < 38 && !this.moveItemStackTo(slotStack, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return itemstack;
    }
}