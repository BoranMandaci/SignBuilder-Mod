package com.boran.signbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BlockstateGenerator {

    private static final String[] MATERIALS = {
            "default", "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "iron", "andesite", "gold", "diamond",
            "lapis", "smooth_stone", "polished_diorite", "bricks", "stone_bricks",
            "redstone_block", "netherite_block", "quartz_block", "polished_granite",
            "purpur_block", "stone", "emerald_block", "smooth_sandstone", "smooth_red_sandstone"
    };

    private static final String[] BLOCKS = {
            "letter_a", "letter_b", "letter_c", "letter_d", "letter_e", "letter_f",
            "letter_g", "letter_h", "letter_i", "letter_j", "letter_k", "letter_l",
            "letter_m", "letter_n", "letter_o", "letter_p", "letter_q", "letter_r",
            "letter_s", "letter_t", "letter_u", "letter_v", "letter_w", "letter_x",
            "letter_y", "letter_z",
            "number_0", "number_1", "number_2", "number_3", "number_4", "number_5",
            "number_6", "number_7", "number_8", "number_9",
            "arrow_up", "arrow_down", "arrow_left", "arrow_right",
            "arrow_left_up", "arrow_right_up", "arrow_left_down", "arrow_right_down",
            "symbol_plus", "symbol_minus", "symbol_heart", "symbol_dot_left",
            "symbol_dot_center", "symbol_dot_right", "symbol_slash", "symbol_comma",
            "symbol_bracket_left", "symbol_bracket_right", "symbol_bracket_double",
            "symbol_square_bracket_left", "symbol_square_bracket_right", "symbol_square_bracket_double",
            "symbol_hashtag", "symbol_euro", "symbol_dollar", "symbol_tl", "symbol_yen",
            "symbol_backslash", "symbol_star", "symbol_pound", "symbol_at",
            "symbol_ampersand", "symbol_percent", "symbol_colon", "symbol_semicolon",
            "symbol_exclamation", "symbol_question", "symbol_equals", "symbol_divide",
            "symbol_apostrophe", "symbol_quotes"
    };

    public static void main(String[] args) {
        File outputDir = new File("common/src/main/resources/assets/signbuilder/blockstates");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (String blockName : BLOCKS) {
            String jsonContent = generateBlockstateJson(blockName);
            File file = new File(outputDir, blockName + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonContent);
                System.out.println("Oluşturuldu: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Tüm blockstate JSON dosyaları başarıyla üretildi!");
    }

    private static String generateBlockstateJson(String blockName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"variants\": {\n");

        boolean first = true;

        String[] faces = {"floor", "wall", "ceiling"};
        String[] facings = {"north", "east", "south", "west"};

        for (String face : faces) {
            for (String facing : facings) {
                int yRot = switch (facing) {
                    case "east" -> face.equals("wall") ? 180 : 90;
                    case "south" -> face.equals("wall") ? 270 : 180;
                    case "west" -> face.equals("wall") ? 0 : 270;
                    default -> face.equals("wall") ? 90 : 0; // north
                };

                String modelPath = face.equals("wall")
                        ? "signbuilder:block/" + blockName + "_wall"
                        : "signbuilder:block/" + blockName;

                for (String mat : MATERIALS) {
                    if (!first) {
                        sb.append(",\n");
                    }
                    first = false;

                    sb.append("    \"face=").append(face)
                            .append(",facing=").append(facing)
                            .append(",material=").append(mat).append("\": { \"model\": \"")
                            .append(modelPath).append("\"");

                    if (yRot != 0) {
                        sb.append(", \"y\": ").append(yRot);
                    }
                    sb.append(" }");
                }
            }
        }

        sb.append("\n  }\n}");
        return sb.toString();
    }
}
