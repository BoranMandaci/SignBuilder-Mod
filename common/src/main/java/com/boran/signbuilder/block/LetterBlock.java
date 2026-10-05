package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.item.PaintBrushItem;
import com.boran.signbuilder.item.SignBlueprintItem;
import com.boran.signbuilder.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class LetterBlock extends Block implements EntityBlock {

    public static final IntegerProperty LIGHT_MODE = IntegerProperty.create("light_mode", 0, 2);
    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public static final VoxelShape BP_WALL_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    public static final VoxelShape BP_WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape BP_WALL_EAST  = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape BP_WALL_WEST  = Block.box(15, 0, 0, 16, 16, 16);

    public static final VoxelShape BP_FLOOR_NORTH = Block.box(11, 0, 0, 12, 16, 16);
    public static final VoxelShape BP_FLOOR_SOUTH = Block.box(4, 0, 0, 5, 16, 16);
    public static final VoxelShape BP_FLOOR_EAST  = Block.box(0, 0, 11, 16, 16, 12);
    public static final VoxelShape BP_FLOOR_WEST  = Block.box(0, 0, 4, 16, 16, 5);

    public LetterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(MATERIAL, SignMaterial.DEFAULT)
                .setValue(LIGHT_MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, MATERIAL, LIGHT_MODE);
    }

    public static String getCharacterFromBlock(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return switch (path) {
            case "letter_a" -> "A";
            case "letter_a_de" -> "Ä";
            case "letter_b" -> "B";
            case "letter_c" -> "C";
            case "letter_c_tr" -> "Ç";
            case "letter_d" -> "D";
            case "letter_e" -> "E";
            case "letter_f" -> "F";
            case "letter_g" -> "G";
            case "letter_g_tr" -> "Ğ";
            case "letter_h" -> "H";
            case "letter_i" -> "I";
            case "letter_i_tr" -> "İ";
            case "letter_j" -> "J";
            case "letter_k" -> "K";
            case "letter_l" -> "L";
            case "letter_m" -> "M";
            case "letter_n" -> "N";
            case "letter_o" -> "O";
            case "letter_o_tr" -> "Ö";
            case "letter_p" -> "P";
            case "letter_q" -> "Q";
            case "letter_r" -> "R";
            case "letter_s" -> "S";
            case "letter_s_tr" -> "Ş";
            case "letter_t" -> "T";
            case "letter_u" -> "U";
            case "letter_u_tr" -> "Ü";
            case "letter_v" -> "V";
            case "letter_w" -> "W";
            case "letter_x" -> "X";
            case "letter_y" -> "Y";
            case "letter_z" -> "Z";
            case "letter_eszett" -> "ß";
            case "number_0" -> "0";
            case "number_1" -> "1";
            case "number_2" -> "2";
            case "number_3" -> "3";
            case "number_4" -> "4";
            case "number_5" -> "5";
            case "number_6" -> "6";
            case "number_7" -> "7";
            case "number_8" -> "8";
            case "number_9" -> "9";
            case "symbol_plus" -> "+";
            case "symbol_minus" -> "-";
            case "symbol_slash" -> "/";
            case "symbol_backslash" -> "\\";
            case "symbol_hashtag" -> "#";
            case "symbol_asterisk" -> "*";
            case "symbol_star" -> "★";
            case "symbol_checkmark" -> "✓";
            case "symbol_infinity" -> "∞";
            case "symbol_heart" -> "♥";
            case "symbol_euro" -> "€";
            case "symbol_dollar" -> "$";
            case "symbol_pound" -> "£";
            case "symbol_yen" -> "¥";
            case "symbol_tl" -> "₺";
            case "symbol_at" -> "@";
            case "symbol_ampersand" -> "&";
            case "symbol_comma" -> ",";
            case "symbol_percent" -> "%";
            case "symbol_less_than" -> "<";
            case "symbol_greater_than" -> ">";
            case "symbol_dot_left" -> "«";
            case "symbol_dot_center" -> "•";
            case "symbol_dot_right" -> "»";
            case "symbol_bracket_left" -> "(";
            case "symbol_bracket_right" -> ")";
            case "symbol_bracket_double" -> "|";
            case "symbol_square_bracket_left" -> "[";
            case "symbol_square_bracket_right" -> "]";
            case "symbol_square_bracket_double" -> "¦";
            case "arrow_up" -> "↑";
            case "arrow_down" -> "↓";
            case "arrow_left" -> "←";
            case "arrow_right" -> "→";
            case "arrow_left_up" -> "↖";
            case "arrow_right_up" -> "↗";
            case "arrow_left_down" -> "↙";
            case "arrow_right_down" -> "↘";
            case "symbol_colon" -> ":";
            case "symbol_semicolon" -> ";";
            case "symbol_exclamation" -> "!";
            case "symbol_question" -> "?";
            case "symbol_equals" -> "=";
            case "symbol_divide" -> "÷";
            case "symbol_apostrophe" -> "'";
            case "symbol_quotes" -> "\"";
            case "symbol_tilde" -> "~";
            case "symbol_bitcoin" -> "₿";
            case "symbol_key" -> "🗝";
            case "symbol_lightning" -> "⚡";
            case "symbol_lock" -> "🔒";
            case "symbol_trophy" -> "🏆";
            default -> {
                if (path.startsWith("letter_")) {
                    String[] parts = path.split("_");
                    yield parts[1].toUpperCase();
                } else if (path.startsWith("number_")) {
                    yield path.substring("number_".length());
                }
                yield "?";
            }
        };
    }

    private int getDigitValue() {
        String path = BuiltInRegistries.BLOCK.getKey(this).getPath();
        for (int i = 0; i <= 9; i++) {
            if (path.endsWith(String.valueOf(i)) || path.contains("_" + i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity rawLetter) {
            LetterBlockEntity master = findMaster(level, pos, rawLetter);
            int digit = getDigitValue();
            if (master.getButtonMode() == 3) {
                if (!master.isPinPowered()) {
                    return 0;
                }
                return digit >= 0 ? digit : 15;
            } else if (master.getButtonMode() != 0) {
                if (!master.isPressed()) {
                    return 0;
                }
                return digit >= 0 ? digit : 15;
            } else {
                return digit >= 0 ? digit : (master.isActive() ? 15 : 0);
            }
        }
        return 0;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
            if (face == AttachFace.WALL) {
                BlockPos pos = hit.getBlockPos();
                BlockEntity rawBe = level.getBlockEntity(pos);
                if (rawBe instanceof LetterBlockEntity rawLetter) {
                    LetterBlockEntity master = findMaster(level, pos, rawLetter);
                    if (master.getButtonMode() != 0) {
                        Player player = (projectile.getOwner() instanceof Player p) ? p : null;
                        if (master.isSyncWord() && master.getButtonMode() != 3) {
                            triggerWord(level, master.getBlockPos(), player);
                        } else {
                            master.triggerPress(player);
                        }
                    }
                }
            }
        }
        super.onProjectileHit(level, state, hit, projectile);
    }

    public static void updateLightLevel(Level level, BlockPos pos, BlockState state, LetterBlockEntity entity) {
        if (level.isClientSide()) return;

        int targetMode = entity.getEmittedLightMode();

        if (entity.getSize() > 1) {
            BlockPos[] positions = (entity.getSize() == 3)
                    ? get3x3BlockPositions(entity.getBlockPos(), level.getBlockState(entity.getBlockPos()), entity.getFacingRotation())
                    : getBigBlockPositions(entity.getBlockPos(), level.getBlockState(entity.getBlockPos()), entity.getFacingRotation());

            for (BlockPos p : positions) {
                BlockState s = level.getBlockState(p);
                if (s.hasProperty(LIGHT_MODE) && s.getValue(LIGHT_MODE) != targetMode) {
                    level.setBlock(p, s.setValue(LIGHT_MODE, targetMode), 3);
                }
            }
        } else {
            if (state.hasProperty(LIGHT_MODE) && state.getValue(LIGHT_MODE) != targetMode) {
                level.setBlock(pos, state.setValue(LIGHT_MODE, targetMode), 3);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return ModBlocks.getStandardPlacementState(context, this.defaultBlockState());
    }

    public static Direction getAttachmentDirection(BlockState state) {
        AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
        if (face == AttachFace.CEILING) return Direction.UP;
        if (face == AttachFace.FLOOR) return Direction.DOWN;
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return facing.getOpposite();
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.hasProperty(FACE) && state.getValue(FACE) != AttachFace.WALL) {
            return 0;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity rawLetter) {
            LetterBlockEntity master = findMaster(level, pos, rawLetter);
            if (master.getButtonMode() == 3) {
                return master.isPinPowered() ? 15 : 0;
            }
            if (master.isPressed()) {
                return 15;
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.hasProperty(FACE) && state.getValue(FACE) != AttachFace.WALL) {
            return 0;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity rawLetter) {
            LetterBlockEntity master = findMaster(level, pos, rawLetter);
            if (getAttachmentDirection(state).getOpposite() == direction) {
                if (master.getButtonMode() == 3) {
                    return master.isPinPowered() ? 15 : 0;
                }
                if (master.isPressed()) {
                    return 15;
                }
            }
        }
        return 0;
    }

    public static VoxelShape calculateHitbox(BlockState state, BlockGetter level, BlockPos pos, VoxelShape letterShape) {
        BlockEntity be = level.getBlockEntity(pos);
        AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
        Direction dir = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

        if (be instanceof LetterBlockEntity rawLetter) {
            LetterBlockEntity master = findMaster(level, pos, rawLetter);
            int rotation = master.getFacingRotation();
            double rotationDelta = SignRotation.deltaDegrees(rotation, dir);
            int normalRotation = face == AttachFace.WALL ? rotation : Math.floorMod(rotation - 2, 8);
            double normalLength = Math.sqrt(
                    SignRotation.facingX(normalRotation) * SignRotation.facingX(normalRotation)
                            + SignRotation.facingZ(normalRotation) * SignRotation.facingZ(normalRotation));
            if (normalLength == 0.0) normalLength = 1.0;
            double normalX = SignRotation.facingX(normalRotation) / normalLength;
            double normalZ = SignRotation.facingZ(normalRotation) / normalLength;
            double plateClearance = (face == AttachFace.WALL ? 1.0 : 2.0) * master.getSize() / 16.0;
            VoxelShape glyphShape = letterShape;
            VoxelShape plateShape = Shapes.empty();

            if (master.getSize() > 1) {
                int size = master.getSize();
                double offsetX = getMultiblockOffsetX(face, dir, size);
                double offsetZ = getMultiblockOffsetZ(face, dir, size);
                BlockPos relativePos = pos.subtract(master.getBlockPos());
                if (master.hasBackplate()) {
                    plateShape = face == AttachFace.WALL
                            ? switch (dir) {
                        case SOUTH -> BP_WALL_SOUTH;
                        case EAST -> BP_WALL_EAST;
                        case WEST -> BP_WALL_WEST;
                        default -> BP_WALL_NORTH;
                    }
                            : switch (dir) {
                        case NORTH -> BP_FLOOR_NORTH;
                        case SOUTH -> BP_FLOOR_SOUTH;
                        case EAST -> BP_FLOOR_EAST;
                        default -> BP_FLOOR_WEST;
                    };
                }

                glyphShape = scaleAndOffsetShape(glyphShape, size, offsetX, offsetZ);
                if (master.hasBackplate()) {
                    plateShape = scaleAndOffsetShape(plateShape, size, offsetX, offsetZ);
                }
                if (master.isPressed() && face == AttachFace.WALL) {
                    glyphShape = compressPressedShape(glyphShape, dir, 0.0625 * size);
                    if (master.hasBackplate()) plateShape = compressPressedShape(plateShape, dir, 0.0625 * size);
                }
                glyphShape = SignRotation.rotateAroundOrigin(glyphShape, rotationDelta);
                if (master.hasBackplate()) plateShape = SignRotation.rotateAroundOrigin(plateShape, rotationDelta);
                if (master.hasBackplate()) {
                    glyphShape = glyphShape.move(normalX * plateClearance, 0.0, normalZ * plateClearance);
                }
                if (face != AttachFace.WALL) {
                    double stepX = SignRotation.horizontalStepX(rotation, false);
                    double stepZ = SignRotation.horizontalStepZ(rotation, false);
                    double targetX = 0.5 + stepX * (size - 1) * 0.5;
                    double targetZ = 0.5 + stepZ * (size - 1) * 0.5;
                    double glyphTargetX = targetX + (master.hasBackplate() ? normalX * plateClearance : 0.0);
                    double glyphTargetZ = targetZ + (master.hasBackplate() ? normalZ * plateClearance : 0.0);
                    glyphShape = isLateralDot(state)
                            ? centerShapeAlongNormal(glyphShape, glyphTargetX, glyphTargetZ, normalX, normalZ)
                            : centerShapeAt(glyphShape, glyphTargetX, glyphTargetZ);
                    if (master.hasBackplate()) plateShape = centerShapeAt(plateShape, targetX, targetZ);
                }
                VoxelShape finalShape = master.hasBackplate() ? Shapes.or(glyphShape, plateShape) : glyphShape;
                finalShape = sliceShape(finalShape, relativePos);
                return finalShape;
            } else if (master.hasBackplate()) {
                plateShape = face == AttachFace.WALL
                        ? switch (dir) {
                    case SOUTH -> BP_WALL_SOUTH;
                    case EAST  -> BP_WALL_EAST;
                    case WEST  -> BP_WALL_WEST;
                    default    -> BP_WALL_NORTH;
                }
                        : switch (dir) {
                    case NORTH -> BP_FLOOR_NORTH;
                    case SOUTH -> BP_FLOOR_SOUTH;
                    case EAST  -> BP_FLOOR_EAST;
                    default    -> BP_FLOOR_WEST;
                };
            }

            if (master.getSize() == 1) {
                if (master.isPressed() && face == AttachFace.WALL) {
                    glyphShape = compressPressedShape(glyphShape, dir, 0.0625);
                    if (master.hasBackplate()) plateShape = compressPressedShape(plateShape, dir, 0.0625);
                }
                glyphShape = SignRotation.rotateAroundBlockCenter(glyphShape, rotationDelta);
                if (master.hasBackplate()) plateShape = SignRotation.rotateAroundBlockCenter(plateShape, rotationDelta);
                if (master.hasBackplate()) glyphShape = glyphShape.move(normalX * plateClearance, 0.0, normalZ * plateClearance);
                if (face != AttachFace.WALL) {
                    double glyphTargetX = 0.5 + (master.hasBackplate() ? normalX * plateClearance : 0.0);
                    double glyphTargetZ = 0.5 + (master.hasBackplate() ? normalZ * plateClearance : 0.0);
                    glyphShape = isLateralDot(state)
                            ? centerShapeAlongNormal(glyphShape, glyphTargetX, glyphTargetZ, normalX, normalZ)
                            : centerShapeAt(glyphShape, glyphTargetX, glyphTargetZ);
                    if (master.hasBackplate()) {
                        plateShape = centerShapeAt(plateShape, 0.5, 0.5);
                        plateShape = SignRotation.widenAlongTangent(plateShape, rotation, Math.sqrt(2.0));
                    }
                }
                return master.hasBackplate() ? Shapes.or(glyphShape, plateShape) : glyphShape;
            }
        }
        return letterShape;
    }

    private static VoxelShape centerShapeAt(VoxelShape shape, double targetX, double targetZ) {
        AABB bounds = shape.bounds();
        double centerX = (bounds.minX + bounds.maxX) * 0.5;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5;
        return shape.move(targetX - centerX, 0.0, targetZ - centerZ);
    }

    private static VoxelShape centerShapeAlongNormal(VoxelShape shape, double targetX, double targetZ, double normalX, double normalZ) {
        AABB bounds = shape.bounds();
        double centerX = (bounds.minX + bounds.maxX) * 0.5;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5;
        double tangentX = -normalZ;
        double tangentZ = normalX;
        double targetNormal = targetX * normalX + targetZ * normalZ;
        double currentTangent = centerX * tangentX + centerZ * tangentZ;
        double targetCenterX = targetNormal * normalX + currentTangent * tangentX;
        double targetCenterZ = targetNormal * normalZ + currentTangent * tangentZ;
        return shape.move(targetCenterX - centerX, 0.0, targetCenterZ - centerZ);
    }

    public static boolean isLateralDot(BlockState state) {
        var blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return blockId != null
                && blockId.getNamespace().equals("signbuilder")
                && (blockId.getPath().equals("symbol_dot_left") || blockId.getPath().equals("symbol_dot_right"));
    }

    private static double getMultiblockOffsetX(AttachFace face, Direction facing, int size) {
        if (face == AttachFace.WALL) {
            if (size == 3) return facing == Direction.NORTH || facing == Direction.WEST ? -2.0 : 0.0;
            return facing == Direction.NORTH || facing == Direction.WEST ? -1.0 : 0.0;
        }
        if (size == 3) {
            return facing == Direction.NORTH ? -1.375 : facing == Direction.SOUTH ? -0.625 : facing == Direction.EAST ? -2.0 : 0.0;
        }
        return facing == Direction.NORTH ? -0.6875 : facing == Direction.SOUTH ? -0.3125 : facing == Direction.EAST ? -1.0 : 0.0;
    }

    private static double getMultiblockOffsetZ(AttachFace face, Direction facing, int size) {
        if (face == AttachFace.WALL) {
            if (size == 3) return facing == Direction.NORTH || facing == Direction.EAST ? -2.0 : 0.0;
            return facing == Direction.NORTH || facing == Direction.EAST ? -1.0 : 0.0;
        }
        if (size == 3) {
            return facing == Direction.EAST ? -1.375 : facing == Direction.WEST ? -0.625 : facing == Direction.SOUTH ? -2.0 : 0.0;
        }
        return facing == Direction.EAST ? -0.6875 : facing == Direction.WEST ? -0.3125 : facing == Direction.SOUTH ? -1.0 : 0.0;
    }

    private static VoxelShape scaleAndOffsetShape(VoxelShape shape, int scale, double offsetX, double offsetZ) {
        VoxelShape result = Shapes.empty();

        for (AABB box : shape.toAabbs()) {
            result = Shapes.or(result, Shapes.create(
                    box.minX * scale + offsetX,
                    box.minY * scale,
                    box.minZ * scale + offsetZ,
                    box.maxX * scale + offsetX,
                    box.maxY * scale,
                    box.maxZ * scale + offsetZ));
        }
        return result;
    }

    private static VoxelShape sliceShape(VoxelShape shape, BlockPos relativePos) {
        VoxelShape result = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            double minX = Math.max(0.0, box.minX - relativePos.getX());
            double minY = Math.max(0.0, box.minY - relativePos.getY());
            double minZ = Math.max(0.0, box.minZ - relativePos.getZ());
            double maxX = Math.min(1.0, box.maxX - relativePos.getX());
            double maxY = Math.min(1.0, box.maxY - relativePos.getY());
            double maxZ = Math.min(1.0, box.maxZ - relativePos.getZ());

            if (minX < maxX && minY < maxY && minZ < maxZ) {
                result = Shapes.or(result, Shapes.create(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }
        return result;
    }

    private static VoxelShape compressPressedShape(VoxelShape shape, Direction facing, double amount) {
        VoxelShape result = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            double minX = box.minX;
            double minZ = box.minZ;
            double maxX = box.maxX;
            double maxZ = box.maxZ;
            switch (facing) {
                case NORTH -> minZ = Math.min(maxZ, minZ + amount);
                case SOUTH -> maxZ = Math.max(minZ, maxZ - amount);
                case WEST -> minX = Math.min(maxX, minX + amount);
                case EAST -> maxX = Math.max(minX, maxX - amount);
                default -> { }
            }
            if (minX < maxX && minZ < maxZ) {
                result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
            }
        }
        return result;
    }

    public static LetterBlockEntity findMaster(BlockGetter level, BlockPos pos, LetterBlockEntity entity) {
        if (entity.getSize() > 1 && !entity.isDummy()) {
            return entity;
        }

        if (entity.getMasterPos() != null) {
            BlockEntity be = level.getBlockEntity(entity.getMasterPos());
            if (be instanceof LetterBlockEntity master && master.getSize() > 1 && !master.isDummy()) {
                return master;
            }
        }

        int rotation = entity.getFacingRotation();
        BlockState currentState = level.getBlockState(pos);
        boolean wall = !currentState.hasProperty(FACE) || currentState.getValue(FACE) == AttachFace.WALL;
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);
        for (int dx = 0; dx < 3; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                BlockPos masterPos = pos.offset(-rightX * dx, -dy, -rightZ * dx);
                BlockEntity be = level.getBlockEntity(masterPos);
                if (!(be instanceof LetterBlockEntity master)
                        || master.getSize() <= 1
                        || master.isDummy()
                        || master.getFacingRotation() != rotation) {
                    continue;
                }

                BlockState masterState = level.getBlockState(masterPos);
                if (!masterState.hasProperty(FACING)) {
                    continue;
                }

                BlockPos[] positions = master.getSize() == 3
                        ? get3x3BlockPositions(masterPos, masterState, rotation)
                        : getBigBlockPositions(masterPos, masterState, rotation);
                for (BlockPos coveredPos : positions) {
                    if (coveredPos.equals(pos)) {
                        entity.setMultiblockDummy(masterPos);
                        return master;
                    }
                }
            }
        }
        return entity;
    }

    public static BlockPos[] getBigBlockPositions(BlockPos masterPos, BlockState masterState) {
        return getBigBlockPositions(masterPos, masterState, SignRotation.fromDirection(masterState.getValue(FACING)));
    }

    public static BlockPos[] getBigBlockPositions(BlockPos masterPos, BlockState masterState, int rotation) {
        boolean wall = !masterState.hasProperty(FACE) || masterState.getValue(FACE) == AttachFace.WALL;
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);

        return new BlockPos[] {
                masterPos,
                masterPos.offset(rightX, 0, rightZ),
                masterPos.relative(Direction.UP, 1),
                masterPos.offset(rightX, 1, rightZ)
        };
    }

    public static BlockPos[] get3x3BlockPositions(BlockPos masterPos, BlockState masterState) {
        return get3x3BlockPositions(masterPos, masterState, SignRotation.fromDirection(masterState.getValue(FACING)));
    }

    public static BlockPos[] get3x3BlockPositions(BlockPos masterPos, BlockState masterState, int rotation) {
        boolean wall = !masterState.hasProperty(FACE) || masterState.getValue(FACE) == AttachFace.WALL;
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);
        BlockPos[] positions = new BlockPos[9];
        int idx = 0;
        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                positions[idx++] = masterPos.offset(rightX * dx, dy, rightZ * dx);
            }
        }
        return positions;
    }

    public static void updateAllEntitiesBackplate(Level level, BlockPos masterPos, BlockState masterState, int size, boolean hasBackplate, SignMaterial fMat, SignMaterial bMat, int fCol, int bCol, boolean fRain, boolean bRain) {
        BlockPos[] targets = (size == 3)
                ? get3x3BlockPositions(masterPos, masterState, getMasterRotation(level, masterPos, masterState))
                : (size == 2 ? getBigBlockPositions(masterPos, masterState, getMasterRotation(level, masterPos, masterState)) : new BlockPos[] { masterPos });

        for (BlockPos p : targets) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof LetterBlockEntity lbe) {
                lbe.setHasBackplate(hasBackplate);
                lbe.setBackplateFrontMaterial(fMat);
                lbe.setBackplateBackMaterial(bMat);
                lbe.setBackplateFrontColor(fCol);
                lbe.setBackplateBackColor(bCol);
                lbe.setBackplateFrontRainbow(fRain);
                lbe.setBackplateBackRainbow(bRain);
                lbe.setChanged();
                lbe.sync();
                BlockState s = level.getBlockState(p);
                level.sendBlockUpdated(p, s, s, 3);
            }
        }
    }

    private static int getMasterRotation(Level level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof LetterBlockEntity letter ? letter.getFacingRotation() : SignRotation.fromDirection(state.getValue(FACING));
    }

    public static boolean tryDetachBackplate(Level level, BlockPos clickedPos, Player player) {
        BlockEntity rawBe = level.getBlockEntity(clickedPos);
        if (!(rawBe instanceof LetterBlockEntity rawLetter)) return false;

        LetterBlockEntity master = findMaster(level, clickedPos, rawLetter);
        if (!master.hasBackplate()) return false;

        BlockPos mPos = master.getBlockPos();
        BlockState mState = level.getBlockState(mPos);
        int size = master.getSize();
        int cost = (size == 3) ? 9 : (size == 2 ? 4 : 1);

        if (!level.isClientSide()) {
            ItemStack droppedItem = BackplateBlock.getDroppedBackplateItemStack(master);
            ItemStack backGlyph = master.getBackGlyph().copy();
            master.setBackGlyph(ItemStack.EMPTY);

            updateAllEntitiesBackplate(level, mPos, mState, size, false, SignMaterial.DEFAULT, SignMaterial.DEFAULT, 0xFFFFFF, 0xFFFFFF, false, false);

            if (!player.isCreative()) {
                ItemStack refund = droppedItem.copy();
                refund.setCount(cost);
                if (!player.getInventory().add(refund)) {
                    player.drop(refund, false);
                }
                if (!backGlyph.isEmpty()) {
                    backGlyph.setCount(cost);
                    if (!player.getInventory().add(backGlyph)) {
                        player.drop(backGlyph, false);
                    }
                }
            }

            level.playSound(null, clickedPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            int rotation = master.getFacingRotation();
            dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                com.boran.signbuilder.client.ClientHooks.setBlocksDirty(mPos);
                if (size == 3) {
                    for (BlockPos p : get3x3BlockPositions(mPos, mState, rotation)) {
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                    }
                } else if (size == 2) {
                    for (BlockPos p : getBigBlockPositions(mPos, mState, rotation)) {
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                    }
                }
            });
        }
        return true;
    }

    private static void triggerWord(Level level, BlockPos startPos, Player player) {
        BlockEntity startBe = level.getBlockEntity(startPos);
        if (!(startBe instanceof LetterBlockEntity startRaw)) return;
        LetterBlockEntity originMaster = findMaster(level, startPos, startRaw);
        if (originMaster.getButtonMode() == 0) return;
        if (originMaster.getButtonMode() == 4) return;

        boolean isPulse = (originMaster.getButtonMode() == 1);
        if (isPulse && originMaster.isPressed()) return;

        boolean targetState = isPulse || !originMaster.isPressed();
        int duration = isPulse ? (originMaster.isWoodMaterial() ? 30 : 20) : 0;

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> triggeredMasters = new HashSet<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 64) {
            BlockPos current = queue.poll();
            BlockState curState = level.getBlockState(current);
            if (curState.hasProperty(FACE) && curState.getValue(FACE) == AttachFace.WALL) {
                BlockEntity be = level.getBlockEntity(current);
                if (be instanceof LetterBlockEntity lbe) {
                    LetterBlockEntity master = findMaster(level, current, lbe);
                    if (triggeredMasters.add(master.getBlockPos())) {
                        master.applyPressedState(targetState, duration);
                    }
                }
            }
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    BlockState ns = level.getBlockState(neighbor);
                    if (ns.getBlock() instanceof LetterBlock && ns.hasProperty(FACE) && ns.getValue(FACE) == AttachFace.WALL) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        originMaster.playPressSound(targetState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PaintBrushItem || held.getItem() instanceof WrenchItem || held.getItem() instanceof SignBlueprintItem) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (tryDetachBackplate(level, pos, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.PASS;
        }

        BlockEntity rawBe = level.getBlockEntity(pos);
        if (!(rawBe instanceof LetterBlockEntity rawLetter)) {
            return super.use(state, level, pos, player, hand, hit);
        }

        LetterBlockEntity master = findMaster(level, pos, rawLetter);
        BlockPos mPos = master.getBlockPos();
        BlockState mState = level.getBlockState(mPos);
        int size = master.getSize();
        int cost = (size == 3) ? 9 : (size == 2 ? 4 : 1);

        if (held.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof LetterBlock
                && master.hasBackplate()
                && state.hasProperty(FACE)
                && state.getValue(FACE) != AttachFace.WALL
                && isOnBackSide(player, pos, state, master.getFacingRotation())) {
            if (!master.getBackGlyph().isEmpty()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                master.setBackGlyph(held);
                if (!player.isCreative()) held.shrink(cost);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (held.is(ModBlocks.BACKPLATE_ITEM.get())) {
            if (master.hasBackplate()) {
                return InteractionResult.PASS;
            }

            if (!player.isCreative() && held.getCount() < cost) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                CompoundTag beTag = held.getTagElement("BlockEntityTag");
                LetterBlockEntity temp = new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), mPos, mState);
                if (beTag != null) {
                    BackplateBlock.applyBackplateTagToEntity(temp, beTag);
                }

                SignMaterial fMat = temp.getBackplateFrontMaterial();
                SignMaterial bMat = temp.getBackplateBackMaterial();
                int fCol = temp.getBackplateFrontColor();
                int bCol = temp.getBackplateBackColor();
                boolean fRain = temp.isBackplateFrontRainbow();
                boolean bRain = temp.isBackplateBackRainbow();

                updateAllEntitiesBackplate(level, mPos, mState, size, true, fMat, bMat, fCol, bCol, fRain, bRain);

                if (!player.isCreative()) {
                    held.shrink(cost);
                }

                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                int rotation = master.getFacingRotation();
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(mPos);
                    if (size == 3) {
                        for (BlockPos p : get3x3BlockPositions(mPos, mState, rotation)) {
                            com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                        }
                    } else if (size == 2) {
                        for (BlockPos p : getBigBlockPositions(mPos, mState, rotation)) {
                            com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                        }
                    }
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
        if (face == AttachFace.WALL && master.getButtonMode() != 0) {
            if (master.getButtonMode() == 4) {
                return InteractionResult.CONSUME;
            }
            if (!level.isClientSide()) {
                if (master.isSyncWord() && master.getButtonMode() != 3) {
                    triggerWord(level, mPos, player);
                } else {
                    master.triggerPress(player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity lbe) {
            CompoundTag beTag = stack.getTagElement("BlockEntityTag");
            if (beTag != null) {
                lbe.load(beTag);
            }
            if (state.getValue(FACE) == AttachFace.WALL) {
                lbe.setFacingRotation(SignRotation.fromDirection(state.getValue(FACING)));
            } else if (placer != null && (beTag == null || !beTag.contains("FacingRotation"))) {
                lbe.setFacingRotation(SignRotation.fromPlacement(state, placer.getYRot()));
            }
            lbe.setSize(1);
            lbe.setDummy(false);
            lbe.setMasterPos(null);
            lbe.setChanged();
            if (!level.isClientSide()) {
                lbe.sync();
                updateLightLevel(level, pos, state, lbe);
            } else {
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                );
            }
            level.sendBlockUpdated(pos, state, level.getBlockState(pos), 3);
        }

        if (!level.isClientSide()) {
            checkAndFormMultiblock(level, pos, state);
        }
    }

    private void checkAndFormMultiblock(Level level, BlockPos placedPos, BlockState placedState) {
        BlockEntity be = level.getBlockEntity(placedPos);
        if (!(be instanceof LetterBlockEntity letter)) return;
        int rotation = letter.getFacingRotation();
        if ((rotation & 1) != 0) return;
        boolean wall = !placedState.hasProperty(FACE) || placedState.getValue(FACE) == AttachFace.WALL;
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);
        Direction facing = placedState.getValue(FACING);

        if (scan3x3(level, placedPos, placedState.getBlock(), rightX, rightZ, facing, rotation)) return;
        scan2x2(level, placedPos, placedState.getBlock(), rightX, rightZ, facing, rotation);
    }

    private static boolean isOnBackSide(Player player, BlockPos pos, BlockState state, int rotation) {
        int normalRotation = state.hasProperty(FACE) && state.getValue(FACE) == AttachFace.WALL
                ? rotation
                : Math.floorMod(rotation - 2, 8);
        double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(normalRotation)
                + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(normalRotation);
        return side < 0.0;
    }

    private boolean scan3x3(Level level, BlockPos placedPos, Block blockType, int rightX, int rightZ, Direction finalFacing, int rotation) {
        for (int oy = -2; oy <= 0; oy++) {
            for (int ox = 0; ox <= 2; ox++) {
                BlockPos b00 = placedPos.offset(-rightX * ox, oy, -rightZ * ox);
                if (tryMerge9(level, b00, blockType, finalFacing, rightX, rightZ, rotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryMerge9(Level level, BlockPos b00, Block expectedBlock, Direction finalFacing, int rightX, int rightZ, int rotation) {
        BlockPos[] positions = new BlockPos[9];
        LetterBlockEntity[] entities = new LetterBlockEntity[9];
        int idx = 0;

        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                positions[idx++] = b00.offset(rightX * dx, dy, rightZ * dx);
            }
        }
        idx = 0;

        BlockState mState00 = level.getBlockState(b00);
        if (!mState00.hasProperty(FACE)) return false;
        AttachFace commonFace = mState00.getValue(FACE);

        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                BlockPos p = positions[idx];
                BlockState s = level.getBlockState(p);
                if (s.getBlock() != expectedBlock) return false;
                if (!s.hasProperty(FACE) || s.getValue(FACE) != commonFace) return false;
                if (!s.hasProperty(FACING) || s.getValue(FACING) != mState00.getValue(FACING)) return false;

                BlockEntity be = level.getBlockEntity(p);
                if (!(be instanceof LetterBlockEntity lbe)) return false;
                if (lbe.getFacingRotation() != rotation) return false;
                if (lbe.getSize() == 3) return false;

                if (lbe.getSize() == 2 && !lbe.isDummy()) {
                    BlockPos[] p2x2 = getBigBlockPositions(p, s, lbe.getFacingRotation());
                    for (BlockPos cp : p2x2) {
                        boolean inside = false;
                        for (BlockPos position : positions) {
                            if (position != null && position.equals(cp)) {
                                inside = true;
                                break;
                            }
                        }
                        if (!inside) return false;
                    }
                }

                entities[idx] = lbe;
                idx++;
            }
        }

        boolean hasAnyBackplate = false;
        SignMaterial fMat = SignMaterial.DEFAULT;
        SignMaterial bMat = SignMaterial.DEFAULT;
        int fCol = 0xFFFFFF;
        int bCol = 0xFFFFFF;
        boolean fRain = false;
        boolean bRain = false;

        for (LetterBlockEntity e : entities) {
            if (e.hasBackplate()) {
                hasAnyBackplate = true;
                fMat = e.getBackplateFrontMaterial();
                bMat = e.getBackplateBackMaterial();
                fCol = e.getBackplateFrontColor();
                bCol = e.getBackplateBackColor();
                fRain = e.isBackplateFrontRainbow();
                bRain = e.isBackplateBackRainbow();
                break;
            }
        }

        for (BlockPos p : positions) {
            level.setBlock(p, level.getBlockState(p).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
        }

        LetterBlockEntity master = (LetterBlockEntity) level.getBlockEntity(b00);
        if (master == null) return false;

        master.setMultiblockMaster(3);

        for (int i = 1; i < 9; i++) {
            LetterBlockEntity dummy = (LetterBlockEntity) level.getBlockEntity(positions[i]);
            if (dummy != null) {
                setupDummy(dummy, b00);
            }
        }

        if (hasAnyBackplate) {
            updateAllEntitiesBackplate(level, b00, level.getBlockState(b00), 3, true, fMat, bMat, fCol, bCol, fRain, bRain);
        }

        updateLightLevel(level, b00, level.getBlockState(b00), master);

        for (BlockPos p : positions) {
            level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 3);
        }
        return true;
    }

    private boolean scan2x2(Level level, BlockPos placedPos, Block blockType, int rightX, int rightZ, Direction finalFacing, int rotation) {
        int[][] cornerOffsets = { {0, 0}, {-1, 0}, {0, -1}, {-1, -1} };

        for (int[] offset : cornerOffsets) {
            BlockPos b00 = placedPos.offset(rightX * offset[0], offset[1], rightZ * offset[0]);
            BlockPos b10 = b00.offset(rightX, 0, rightZ);
            BlockPos b01 = b00.relative(Direction.UP, 1);
            BlockPos b11 = b00.offset(rightX, 1, rightZ);

            if (tryMerge4(level, b00, b10, b01, b11, blockType, finalFacing, rotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryMerge4(Level level, BlockPos b00, BlockPos b10, BlockPos b01, BlockPos b11, Block expectedBlock, Direction finalFacing, int rotation) {
        if (isValidSingle(level, b00, expectedBlock, rotation) &&
                isValidSingle(level, b10, expectedBlock, rotation) &&
                isValidSingle(level, b01, expectedBlock, rotation) &&
                isValidSingle(level, b11, expectedBlock, rotation)) {

            LetterBlockEntity master = (LetterBlockEntity) level.getBlockEntity(b00);
            LetterBlockEntity e10 = (LetterBlockEntity) level.getBlockEntity(b10);
            LetterBlockEntity e01 = (LetterBlockEntity) level.getBlockEntity(b01);
            LetterBlockEntity e11 = (LetterBlockEntity) level.getBlockEntity(b11);

            if (master != null && e10 != null && e01 != null && e11 != null) {
                BlockState mState = level.getBlockState(b00);
                AttachFace commonFace = mState.getValue(FACE);
                if (level.getBlockState(b10).getValue(FACE) != commonFace
                        || level.getBlockState(b01).getValue(FACE) != commonFace
                        || level.getBlockState(b11).getValue(FACE) != commonFace
                        || level.getBlockState(b10).getValue(FACING) != mState.getValue(FACING)
                        || level.getBlockState(b01).getValue(FACING) != mState.getValue(FACING)
                        || level.getBlockState(b11).getValue(FACING) != mState.getValue(FACING)) {
                    return false;
                }

                boolean hasAnyBackplate = master.hasBackplate() || e10.hasBackplate() || e01.hasBackplate() || e11.hasBackplate();
                SignMaterial fMat = master.hasBackplate() ? master.getBackplateFrontMaterial() : (e10.hasBackplate() ? e10.getBackplateFrontMaterial() : (e01.hasBackplate() ? e01.getBackplateFrontMaterial() : e11.getBackplateFrontMaterial()));
                SignMaterial bMat = master.hasBackplate() ? master.getBackplateBackMaterial() : (e10.hasBackplate() ? e10.getBackplateBackMaterial() : (e01.hasBackplate() ? e01.getBackplateBackMaterial() : e11.getBackplateBackMaterial()));
                int fCol = master.hasBackplate() ? master.getBackplateFrontColor() : (e10.hasBackplate() ? e10.getBackplateFrontColor() : 0xFFFFFF);
                int bCol = master.hasBackplate() ? master.getBackplateBackColor() : (e10.hasBackplate() ? e10.getBackplateBackColor() : 0xFFFFFF);
                boolean fRain = master.hasBackplate() ? master.isBackplateFrontRainbow() : e10.isBackplateFrontRainbow();
                boolean bRain = master.hasBackplate() ? master.isBackplateBackRainbow() : e10.isBackplateBackRainbow();

                level.setBlock(b00, level.getBlockState(b00).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b10, level.getBlockState(b10).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b01, level.getBlockState(b01).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b11, level.getBlockState(b11).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);

                LetterBlockEntity actualMaster = (LetterBlockEntity) level.getBlockEntity(b00);
                LetterBlockEntity actual10 = (LetterBlockEntity) level.getBlockEntity(b10);
                LetterBlockEntity actual01 = (LetterBlockEntity) level.getBlockEntity(b01);
                LetterBlockEntity actual11 = (LetterBlockEntity) level.getBlockEntity(b11);

                if (actualMaster != null && actual10 != null && actual01 != null && actual11 != null) {
                    actualMaster.setMultiblockMaster(2);

                    setupDummy(actual10, b00);
                    setupDummy(actual01, b00);
                    setupDummy(actual11, b00);

                    if (hasAnyBackplate) {
                        updateAllEntitiesBackplate(level, b00, mState, 2, true, fMat, bMat, fCol, bCol, fRain, bRain);
                    }

                    updateLightLevel(level, b00, level.getBlockState(b00), actualMaster);

                    level.sendBlockUpdated(b00, level.getBlockState(b00), level.getBlockState(b00), 3);
                    level.sendBlockUpdated(b10, level.getBlockState(b10), level.getBlockState(b10), 3);
                    level.sendBlockUpdated(b01, level.getBlockState(b01), level.getBlockState(b01), 3);
                    level.sendBlockUpdated(b11, level.getBlockState(b11), level.getBlockState(b11), 3);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidSingle(Level level, BlockPos pos, Block expectedBlock, int rotation) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() != expectedBlock) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LetterBlockEntity lbe)) return false;

        return lbe.getSize() == 1 && !lbe.isDummy() && lbe.getFacingRotation() == rotation;
    }

    private void setupDummy(LetterBlockEntity dummy, BlockPos masterPos) {
        dummy.setMultiblockDummy(masterPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity rawLbe) {
                LetterBlockEntity master = findMaster(level, pos, rawLbe);
                BlockPos mPos = master.getBlockPos();
                BlockState mState = level.getBlockState(mPos);
                int size = master.getSize();

                if (size == 1) {
                    BlockPos inferredMasterPos = findUnmerged2x2Master(level, pos, state, master.getFacingRotation());
                    if (inferredMasterPos != null && level.getBlockEntity(inferredMasterPos) instanceof LetterBlockEntity inferredMaster) {
                        master = inferredMaster;
                        mPos = inferredMasterPos;
                        mState = level.getBlockState(mPos);
                        size = 2;
                    }
                }

                if (size == 3) {
                    if (!player.isCreative()) {
                        spawnLetterDrops(level, mPos, mState, master, 9, 3, player.getMainHandItem());
                    }
                    destroyAll9Blocks(level, mPos, mState, pos, master.getFacingRotation());
                } else if (size == 2) {
                    if (!player.isCreative()) {
                        spawnLetterDrops(level, mPos, mState, master, 4, 2, player.getMainHandItem());
                    }
                    destroyAll4Blocks(level, mPos, mState, pos, master.getFacingRotation());
                } else if (!rawLbe.isDummy()) {
                    if (!player.isCreative()) {
                        spawnLetterDrops(level, pos, state, rawLbe, 1, 1, player.getMainHandItem());
                    }
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private static void destroyAll4Blocks(Level level, BlockPos masterPos, BlockState masterState, BlockPos triggeredPos, int rotation) {
        BlockPos[] positions = getBigBlockPositions(masterPos, masterState, rotation);

        for (BlockPos p : positions) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof LetterBlockEntity lbe) {
                lbe.setSize(1);
                lbe.setDummy(false);
                lbe.setMasterPos(null);
                lbe.setChanged();
            }
            if (!p.equals(triggeredPos)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = masterPos.offset(dx, dy, dz);
                    if (!check.equals(triggeredPos)) {
                        BlockEntity be = level.getBlockEntity(check);
                        if (be instanceof LetterBlockEntity lbe && lbe.isDummy() && masterPos.equals(lbe.getMasterPos())) {
                            lbe.setSize(1);
                            lbe.setDummy(false);
                            lbe.setMasterPos(null);
                            lbe.setChanged();
                            level.setBlock(check, Blocks.AIR.defaultBlockState(), 35);
                        }
                    }
                }
            }
        }
    }

    private static void destroyAll9Blocks(Level level, BlockPos masterPos, BlockState masterState, BlockPos triggeredPos, int rotation) {
        BlockPos[] positions = get3x3BlockPositions(masterPos, masterState, rotation);

        for (BlockPos p : positions) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof LetterBlockEntity lbe) {
                lbe.setSize(1);
                lbe.setDummy(false);
                lbe.setMasterPos(null);
                lbe.setChanged();
            }
            if (!p.equals(triggeredPos)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos check = masterPos.offset(dx, dy, dz);
                    if (!check.equals(triggeredPos)) {
                        BlockEntity be = level.getBlockEntity(check);
                        if (be instanceof LetterBlockEntity lbe && lbe.isDummy() && masterPos.equals(lbe.getMasterPos())) {
                            lbe.setSize(1);
                            lbe.setDummy(false);
                            lbe.setMasterPos(null);
                            lbe.setChanged();
                            level.setBlock(check, Blocks.AIR.defaultBlockState(), 35);
                        }
                    }
                }
            }
        }
    }

    private void spawnLetterDrops(Level level, BlockPos dropPos, BlockState dropState, LetterBlockEntity lbe, int multiplier, int groupSize, ItemStack tool) {
        boolean hasSilkTouch = !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
        LetterBlockEntity backplateSource = findBackplateSource(level, dropPos, dropState, lbe, groupSize);

        if (hasSilkTouch) {
            ItemStack backGlyph = lbe.getBackGlyph().copy();
            ItemStack letterStack = lbe.getDroppedItemStack(dropState);
            CompoundTag itemTag = letterStack.getTag();
            if (itemTag != null && itemTag.contains("BlockEntityTag", 10)) {
                CompoundTag beTag = itemTag.getCompound("BlockEntityTag");
                beTag.remove("BackGlyph");
                beTag.remove("HasBackplate");
                beTag.remove("BPFrontMat");
                beTag.remove("BPBackMat");
                beTag.remove("BPFrontColor");
                beTag.remove("BPBackColor");
                beTag.remove("BPFrontRainbow");
                beTag.remove("BPBackRainbow");
                beTag.remove("BackplateFrontMaterial");
                beTag.remove("BackplateBackMaterial");
                beTag.remove("BackplateFrontColor");
                beTag.remove("BackplateBackColor");
                beTag.remove("BackplateFrontRainbow");
                beTag.remove("BackplateBackRainbow");
                if (beTag.isEmpty()) itemTag.remove("BlockEntityTag");
            }
            letterStack.setCount(multiplier);
            popResource(level, dropPos, letterStack);

            if (!backGlyph.isEmpty()) {
                popResource(level, dropPos, backGlyph);
            }

            if (backplateSource != null) {
                ItemStack bp = BackplateBlock.getDroppedBackplateItemStack(backplateSource);
                bp.setCount(multiplier);
                popResource(level, dropPos, bp);
            }
        } else {
            popResource(level, dropPos, new ItemStack(Blocks.WHITE_CONCRETE, 3 * multiplier));
            SignMaterial mat = dropState.hasProperty(MATERIAL) ? dropState.getValue(MATERIAL) : lbe.getSavedMaterial();
            if (mat != SignMaterial.DEFAULT) {
                ItemStack matStack = BackplateBlock.getItemForMaterial(mat);
                if (!matStack.isEmpty()) {
                    matStack.setCount(multiplier);
                    popResource(level, dropPos, matStack);
                }
            }

            if (lbe.isActive()) {
                popResource(level, dropPos, new ItemStack(Items.GLOWSTONE_DUST, multiplier));
            }

            ItemStack backGlyph = lbe.getBackGlyph();
            if (!backGlyph.isEmpty()) {
                popResource(level, dropPos, new ItemStack(Blocks.WHITE_CONCRETE, 3 * multiplier));
                SignMaterial backGlyphMaterial = getBackGlyphMaterial(backGlyph);
                if (backGlyphMaterial != SignMaterial.DEFAULT) {
                    ItemStack backGlyphMaterialStack = BackplateBlock.getItemForMaterial(backGlyphMaterial);
                    if (!backGlyphMaterialStack.isEmpty()) {
                        backGlyphMaterialStack.setCount(multiplier);
                        popResource(level, dropPos, backGlyphMaterialStack);
                    }
                }
            }

            if (backplateSource != null) {
                popResource(level, dropPos, new ItemStack(Blocks.WHITE_CONCRETE, 3 * multiplier));
                SignMaterial fMat = backplateSource.getBackplateFrontMaterial();
                SignMaterial bMat = backplateSource.getBackplateBackMaterial();

                if (fMat != SignMaterial.DEFAULT) {
                    ItemStack fStack = BackplateBlock.getItemForMaterial(fMat);
                    if (!fStack.isEmpty()) {
                        fStack.setCount(multiplier);
                        popResource(level, dropPos, fStack);
                    }
                }
                if (bMat != SignMaterial.DEFAULT) {
                    ItemStack bStack = BackplateBlock.getItemForMaterial(bMat);
                    if (!bStack.isEmpty()) {
                        bStack.setCount(multiplier);
                        popResource(level, dropPos, bStack);
                    }
                }
            }
        }
    }

    private static SignMaterial getBackGlyphMaterial(ItemStack glyph) {
        CompoundTag blockEntityTag = glyph.getTagElement("BlockEntityTag");
        if (blockEntityTag != null && blockEntityTag.contains("SavedMaterial")) {
            String value = blockEntityTag.getString("SavedMaterial");
            for (SignMaterial material : SignMaterial.values()) {
                if (material.name().equalsIgnoreCase(value) || material.getSerializedName().equals(value)) {
                    return material;
                }
            }
        }

        CompoundTag stateTag = glyph.getTagElement("BlockStateTag");
        if (stateTag != null && stateTag.contains("material")) {
            String value = stateTag.getString("material");
            for (SignMaterial material : SignMaterial.values()) {
                if (material.getSerializedName().equals(value) || material.name().equalsIgnoreCase(value)) {
                    return material;
                }
            }
        }
        return SignMaterial.DEFAULT;
    }

    @Nullable
    private LetterBlockEntity findBackplateSource(Level level, BlockPos masterPos, BlockState masterState, LetterBlockEntity master, int groupSize) {
        if (master.hasBackplate()) return master;

        BlockPos[] positions = groupSize == 3
                ? get3x3BlockPositions(masterPos, masterState, master.getFacingRotation())
                : groupSize == 2
                ? getBigBlockPositions(masterPos, masterState, master.getFacingRotation())
                : new BlockPos[] { masterPos };

        for (BlockPos position : positions) {
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity instanceof LetterBlockEntity letter && letter.hasBackplate()) {
                return letter;
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos findUnmerged2x2Master(Level level, BlockPos pos, BlockState state, int rotation) {
        if ((rotation & 1) != 0) return null;

        boolean wall = !state.hasProperty(FACE) || state.getValue(FACE) == AttachFace.WALL;
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);
        int[][] cornerOffsets = {{0, 0}, {-1, 0}, {0, -1}, {-1, -1}};

        for (int[] offset : cornerOffsets) {
            BlockPos candidate = pos.offset(rightX * offset[0], offset[1], rightZ * offset[0]);
            BlockState candidateState = level.getBlockState(candidate);
            if (candidateState.getBlock() != state.getBlock()) continue;

            BlockPos[] positions = getBigBlockPositions(candidate, candidateState, rotation);
            boolean complete = true;
            for (BlockPos position : positions) {
                BlockState partState = level.getBlockState(position);
                BlockEntity partEntity = level.getBlockEntity(position);
                if (partState.getBlock() != state.getBlock()
                        || !partState.hasProperty(FACE)
                        || partState.getValue(FACE) != state.getValue(FACE)
                        || !partState.hasProperty(FACING)
                        || partState.getValue(FACING) != state.getValue(FACING)
                        || !(partEntity instanceof LetterBlockEntity letter)
                        || letter.getSize() != 1
                        || letter.isDummy()
                        || letter.getFacingRotation() != rotation) {
                    complete = false;
                    break;
                }
            }
            if (complete) return candidate;
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity lbe) {
                if (lbe.getSize() == 3) {
                    destroyAll9Blocks(level, pos, state, pos, lbe.getFacingRotation());
                } else if (lbe.getSize() == 2) {
                    destroyAll4Blocks(level, pos, state, pos, lbe.getFacingRotation());
                } else if (lbe.isDummy() && lbe.getMasterPos() != null) {
                    BlockPos mPos = lbe.getMasterPos();
                    BlockEntity mBe = level.getBlockEntity(mPos);
                    if (mBe instanceof LetterBlockEntity masterLbe) {
                        if (masterLbe.getSize() == 3) {
                            destroyAll9Blocks(level, mPos, level.getBlockState(mPos), pos, masterLbe.getFacingRotation());
                        } else if (masterLbe.getSize() == 2) {
                            destroyAll4Blocks(level, mPos, level.getBlockState(mPos), pos, masterLbe.getFacingRotation());
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.LETTER_BLOCK_ENTITY.get()) {
            return (l, p, s, entity) -> LetterBlockEntity.tick(l, p, s, (LetterBlockEntity) entity);
        }
        return null;
    }
}
