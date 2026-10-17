package com.boran.signbuilder.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LcdTextFormatter {

    private static final Pattern HEX_COLOR_PATTERN_1 = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern HEX_COLOR_PATTERN_2 = Pattern.compile("<#([0-9a-fA-F]{6})>");
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile("&([0-9a-fA-Fk-orK-OR])");
    private static final char[] GLITCH_GLYPHS = "0123456789ABCDEF?!#@$%&*<>+-=/\\".toCharArray();

    public static final int WIDGET_NONE = 0;
    public static final int WIDGET_TIME = 1;
    public static final int WIDGET_WEATHER = 2;
    public static final int WIDGET_PLAYER = 3;
    public static final int WIDGET_DAY = 4;
    public static final int WIDGET_ONLINE = 5;
    public static final int WIDGET_COORDS = 6;
    public static final int WIDGET_BIOME = 7;
    public static final int WIDGET_MOON = 8;
    public static final int WIDGET_FPS = 9;
    public static final int WIDGET_HEALTH = 10;
    public static final int WIDGET_COUNT = 11;

    public static final String[] WIDGET_KEYS = {
            "gui.signbuilder.lcd.widget.none",
            "gui.signbuilder.lcd.widget.time",
            "gui.signbuilder.lcd.widget.weather",
            "gui.signbuilder.lcd.widget.player",
            "gui.signbuilder.lcd.widget.day",
            "gui.signbuilder.lcd.widget.online",
            "gui.signbuilder.lcd.widget.coords",
            "gui.signbuilder.lcd.widget.biome",
            "gui.signbuilder.lcd.widget.moon",
            "gui.signbuilder.lcd.widget.fps",
            "gui.signbuilder.lcd.widget.health"
    };

    public static String getWidgetText(int widgetType, @Nullable Level level, @Nullable BlockPos pos) {
        return switch (widgetType) {
            case WIDGET_TIME -> "⌚ " + getTimeString(level);
            case WIDGET_WEATHER -> getWeatherString(level);
            case WIDGET_PLAYER -> getPlayerString(level, pos);
            case WIDGET_DAY -> getDayString(level);
            case WIDGET_ONLINE -> "👥 " + getOnlineCountString(level);
            case WIDGET_COORDS -> "⚲ " + (pos != null ? (pos.getX() + " " + pos.getY() + " " + pos.getZ()) : "0 0 0");
            case WIDGET_BIOME -> "🌲 " + getBiomeString(level, pos);
            case WIDGET_MOON -> getMoonPhaseString(level);
            case WIDGET_FPS -> getFpsString() + " FPS";
            case WIDGET_HEALTH -> getHealthString(level, pos);
            default -> "";
        };
    }

    public static String resolveAll(String text, @Nullable Level level, @Nullable BlockPos pos) {
        if (text == null || text.isEmpty()) return "";
        String s = resolveSymbols(text);
        s = resolveVariables(s, level, pos);
        return resolveColors(s);
    }

    public static String resolveSymbols(String text) {
        if (text == null || text.isEmpty() || !text.contains(":")) return text != null ? text : "";
        return text
                .replace(":heart:", "❤")
                .replace(":heart2:", "♥")
                .replace(":sparkle:", "✦")
                .replace(":star:", "★")
                .replace(":star2:", "☆")
                .replace(":bolt:", "⚡")
                .replace(":lightning:", "⚡")
                .replace(":zap:", "⚡")
                .replace(":warn:", "⚠")
                .replace(":danger:", "⚠")
                .replace(":sword:", "⚔")
                .replace(":swords:", "⚔")
                .replace(":pick:", "⛏")
                .replace(":pickaxe:", "⛏")
                .replace(":skull:", "☠")
                .replace(":check:", "✔")
                .replace(":v:", "✔")
                .replace(":x:", "✖")
                .replace(":cross:", "✖")
                .replace(":sun:", "☀")
                .replace(":cloud:", "☁")
                .replace(":umbrella:", "☂")
                .replace(":rain:", "☂")
                .replace(":snow:", "❄")
                .replace(":fire:", "♨")
                .replace(":steam:", "♨")
                .replace(":time:", "⌛")
                .replace(":hourglass:", "⌛")
                .replace(":clock:", "⌚")
                .replace(":watch:", "⌚")
                .replace(":gear:", "⚙")
                .replace(":mail:", "✉")
                .replace(":music:", "♫")
                .replace(":note:", "♪")
                .replace(":diamond:", "◆")
                .replace(":gem:", "◆")
                .replace(":dot:", "●")
                .replace(":square:", "■")
                .replace(":smile:", "☺")
                .replace(":left:", "←")
                .replace(":right:", "→")
                .replace(":up:", "↑")
                .replace(":down:", "↓")
                .replace(":tri_left:", "◄")
                .replace(":tri_right:", "►")
                .replace(":tri_up:", "▲")
                .replace(":tri_down:", "▼")
                .replace(":dleft:", "«")
                .replace(":dright:", "»");
    }

    public static String resolveVariables(String text, @Nullable Level level, @Nullable BlockPos pos) {
        if (text == null || text.isEmpty() || !text.contains("{")) return text;

        if (text.contains("{time}")) {
            text = text.replace("{time}", getTimeString(level));
        }
        if (text.contains("{weather}")) {
            text = text.replace("{weather}", getWeatherString(level));
        }
        if (text.contains("{player}")) {
            text = text.replace("{player}", getPlayerNameOnly(level, pos));
        }
        if (text.contains("{day}")) {
            text = text.replace("{day}", getDayNumberString(level));
        }
        if (text.contains("{online}")) {
            text = text.replace("{online}", getOnlineCountString(level));
        }
        if (text.contains("{coords}")) {
            text = text.replace("{coords}", pos != null ? (pos.getX() + ", " + pos.getY() + ", " + pos.getZ()) : "0, 0, 0");
        }
        if (text.contains("{x}")) {
            text = text.replace("{x}", pos != null ? String.valueOf(pos.getX()) : "0");
        }
        if (text.contains("{y}")) {
            text = text.replace("{y}", pos != null ? String.valueOf(pos.getY()) : "0");
        }
        if (text.contains("{z}")) {
            text = text.replace("{z}", pos != null ? String.valueOf(pos.getZ()) : "0");
        }
        if (text.contains("{biome}")) {
            text = text.replace("{biome}", getBiomeString(level, pos));
        }
        if (text.contains("{moon}")) {
            text = text.replace("{moon}", getMoonPhaseString(level));
        }
        if (text.contains("{fps}")) {
            text = text.replace("{fps}", getFpsString());
        }
        if (text.contains("{health}")) {
            text = text.replace("{health}", getHealthString(level, pos));
        }
        if (text.contains("{hp}")) {
            text = text.replace("{hp}", getHpNumberString(level, pos));
        }
        if (text.contains("{hearts}")) {
            text = text.replace("{hearts}", getHeartsString(level, pos));
        }

        return text;
    }

    public static String resolveColors(String text) {
        if (text == null || text.isEmpty() || (!text.contains("&") && !text.contains("<#"))) return text;

        String s = text.replace("&&", "\u0000_AMP_\u0000");

        Matcher hexMatcher1 = HEX_COLOR_PATTERN_1.matcher(s);
        StringBuilder sb1 = new StringBuilder();
        while (hexMatcher1.find()) {
            String hex = hexMatcher1.group(1).toLowerCase();
            StringBuilder rep = new StringBuilder("§x");
            for (int i = 0; i < 6; i++) {
                rep.append('§').append(hex.charAt(i));
            }
            hexMatcher1.appendReplacement(sb1, Matcher.quoteReplacement(rep.toString()));
        }
        hexMatcher1.appendTail(sb1);
        s = sb1.toString();

        Matcher hexMatcher2 = HEX_COLOR_PATTERN_2.matcher(s);
        StringBuilder sb2 = new StringBuilder();
        while (hexMatcher2.find()) {
            String hex = hexMatcher2.group(1).toLowerCase();
            StringBuilder rep = new StringBuilder("§x");
            for (int i = 0; i < 6; i++) {
                rep.append('§').append(hex.charAt(i));
            }
            hexMatcher2.appendReplacement(sb2, Matcher.quoteReplacement(rep.toString()));
        }
        hexMatcher2.appendTail(sb2);
        s = sb2.toString();

        Matcher codeMatcher = FORMAT_CODE_PATTERN.matcher(s);
        StringBuilder sb3 = new StringBuilder();
        while (codeMatcher.find()) {
            char code = Character.toLowerCase(codeMatcher.group(1).charAt(0));
            codeMatcher.appendReplacement(sb3, Matcher.quoteReplacement("§" + code));
        }
        codeMatcher.appendTail(sb3);
        s = sb3.toString();

        return s.replace("\u0000_AMP_\u0000", "&");
    }

    public static String getTimeString(@Nullable Level level) {
        if (level == null) return "12:00";
        long dayTime = (level.getDayTime() + 6000) % 24000;
        int hours = (int) (dayTime / 1000);
        int minutes = (int) ((dayTime % 1000) * 60 / 1000);
        return String.format("%02d:%02d", hours, minutes);
    }

    public static String getWeatherString(@Nullable Level level) {
        if (level == null) return Component.translatable("gui.signbuilder.lcd.weather.clear").getString();
        if (level.isThundering()) return Component.translatable("gui.signbuilder.lcd.weather.thunder").getString();
        if (level.isRaining()) return Component.translatable("gui.signbuilder.lcd.weather.rain").getString();
        return Component.translatable("gui.signbuilder.lcd.weather.clear").getString();
    }

    public static String getPlayerString(@Nullable Level level, @Nullable BlockPos pos) {
        return "👤 " + getPlayerNameOnly(level, pos);
    }

    public static String getPlayerNameOnly(@Nullable Level level, @Nullable BlockPos pos) {
        if (level != null && pos != null) {
            Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
            if (p != null) return p.getName().getString();
        }
        return "--";
    }

    public static String getDayString(@Nullable Level level) {
        return Component.translatable("gui.signbuilder.lcd.day_format", getDayNumberString(level)).getString();
    }

    public static String getDayNumberString(@Nullable Level level) {
        if (level == null) return "1";
        return String.valueOf((level.getDayTime() / 24000L) + 1);
    }

    public static String getOnlineCountString(@Nullable Level level) {
        if (level != null && level.players() != null) {
            return String.valueOf(level.players().size());
        }
        return "1";
    }

    public static String getBiomeString(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return "Unknown";
        try {
            var holder = level.getBiome(pos);
            var keyOpt = holder.unwrapKey();
            if (keyOpt.isPresent()) {
                var loc = keyOpt.get().location();
                String langKey = "biome." + loc.getNamespace() + "." + loc.getPath();
                Component comp = Component.translatable(langKey);
                String translated = comp.getString();
                if (!translated.equals(langKey)) {
                    return translated;
                }
                String path = loc.getPath();
                String[] words = path.split("_");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) {
                        if (!sb.isEmpty()) sb.append(" ");
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
                    }
                }
                return sb.toString();
            }
        } catch (Throwable ignored) {}
        return "Unknown";
    }

    public static String getMoonPhaseString(@Nullable Level level) {
        if (level == null) return "○ --";
        int phase = level.getMoonPhase();
        return switch (phase) {
            case 0 -> Component.translatable("gui.signbuilder.lcd.moon.full").getString();
            case 1 -> Component.translatable("gui.signbuilder.lcd.moon.waning_gibbous").getString();
            case 2 -> Component.translatable("gui.signbuilder.lcd.moon.last_quarter").getString();
            case 3 -> Component.translatable("gui.signbuilder.lcd.moon.waning_crescent").getString();
            case 4 -> Component.translatable("gui.signbuilder.lcd.moon.new_moon").getString();
            case 5 -> Component.translatable("gui.signbuilder.lcd.moon.waxing_crescent").getString();
            case 6 -> Component.translatable("gui.signbuilder.lcd.moon.first_quarter").getString();
            case 7 -> Component.translatable("gui.signbuilder.lcd.moon.waxing_gibbous").getString();
            default -> "○ --";
        };
    }

    private static java.lang.reflect.Method GET_FPS_METHOD = null;
    private static Object MC_INSTANCE = null;
    private static boolean REFLECTION_INITIALIZED = false;
    private static int cachedFps = 60;
    private static long lastFpsUpdateMs = 0L;

    public static String getFpsString() {
        long now = System.currentTimeMillis();
        if (now - lastFpsUpdateMs > 500L) {
            lastFpsUpdateMs = now;
            try {
                if (!REFLECTION_INITIALIZED) {
                    REFLECTION_INITIALIZED = true;
                    Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
                    MC_INSTANCE = mcClass.getMethod("getInstance").invoke(null);
                    GET_FPS_METHOD = mcClass.getMethod("getFps");
                }
                if (GET_FPS_METHOD != null && MC_INSTANCE != null) {
                    cachedFps = (int) GET_FPS_METHOD.invoke(MC_INSTANCE);
                }
            } catch (Throwable ignored) {}
        }
        return String.valueOf(cachedFps);
    }

    public static String getHealthString(@Nullable Level level, @Nullable BlockPos pos) {
        if (level != null && pos != null) {
            Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
            if (p != null) {
                int hp = (int) Math.ceil(p.getHealth());
                int maxHp = (int) Math.ceil(p.getMaxHealth());
                return hp + "/" + maxHp + "❤";
            }
        }
        return "--❤";
    }

    public static String getHpNumberString(@Nullable Level level, @Nullable BlockPos pos) {
        if (level != null && pos != null) {
            Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
            if (p != null) {
                return String.valueOf((int) Math.ceil(p.getHealth()));
            }
        }
        return "--";
    }

    public static String getHeartsString(@Nullable Level level, @Nullable BlockPos pos) {
        if (level != null && pos != null) {
            Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
            if (p != null) {
                int hp = (int) Math.ceil(p.getHealth());
                int heartCount = Math.max(1, Math.min(10, (hp + 1) / 2));
                return "❤".repeat(heartCount);
            }
        }
        return "❤❤❤❤❤";
    }

    public static int countVisibleChars(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (int i = 0; i < text.length(); ) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                i += 2;
                continue;
            }
            int cp = text.codePointAt(i);
            count++;
            i += Character.charCount(cp);
        }
        return count;
    }

    public static String substringVisibleChars(String text, int targetVisibleCount) {
        if (text == null || text.isEmpty() || targetVisibleCount <= 0) return "";
        StringBuilder sb = new StringBuilder();
        int visibleCount = 0;
        for (int i = 0; i < text.length(); ) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                sb.append('§').append(text.charAt(i + 1));
                i += 2;
                continue;
            }
            if (visibleCount >= targetVisibleCount) break;
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);
            sb.append(text, i, i + charCount);
            visibleCount++;
            i += charCount;
        }
        return sb.toString();
    }

    public static String buildGlitchText(String text, int speed, long timeMs) {
        if (text == null || text.isEmpty()) return "";
        long cycleMs = switch (speed) {
            case 1 -> 6000L;
            case 2 -> 4500L;
            case 3 -> 3000L;
            case 4 -> 1800L;
            default -> 4500L;
        };
        long cycleTime = timeMs % cycleMs;

        int visibleCount = countVisibleChars(text);
        if (visibleCount == 0) return text;

        boolean isDecodePhase = cycleTime < (cycleMs * 0.28);
        boolean isJitterPhase = cycleTime > (cycleMs * 0.88);

        if (!isDecodePhase && !isJitterPhase) {
            return text;
        }

        int resolvedCount = isDecodePhase ? (int) ((cycleTime / (double) (cycleMs * 0.28)) * visibleCount) : visibleCount;
        int jitterCharIdx = isJitterPhase ? (int) ((timeMs / 100) % (visibleCount + 2)) : -1;

        StringBuilder sb = new StringBuilder(text.length());
        int visIdx = 0;
        for (int i = 0; i < text.length(); ) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                sb.append('§').append(text.charAt(i + 1));
                i += 2;
                continue;
            }
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                sb.append(c);
            } else if (isDecodePhase) {
                if (visIdx < resolvedCount) {
                    sb.append(text, i, i + charCount);
                } else {
                    int seed = (int) (timeMs / 70 + visIdx * 11);
                    char glyph = GLITCH_GLYPHS[Math.abs(seed) % GLITCH_GLYPHS.length];
                    sb.append(glyph);
                }
            } else if (isJitterPhase) {
                if (visIdx == jitterCharIdx) {
                    int seed = (int) (timeMs / 60 + visIdx * 7);
                    char glyph = GLITCH_GLYPHS[Math.abs(seed) % GLITCH_GLYPHS.length];
                    sb.append(glyph);
                } else {
                    sb.append(text, i, i + charCount);
                }
            } else {
                sb.append(text, i, i + charCount);
            }

            visIdx++;
            i += charCount;
        }

        return sb.toString();
    }
}
