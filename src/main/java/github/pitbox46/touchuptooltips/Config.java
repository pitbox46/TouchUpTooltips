package github.pitbox46.touchuptooltips;

import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Predicate;

public class Config
{
    private static Predicate<Object> isInteger = num -> {
        if (num instanceof String s) {
            try {
                Integer.parseUnsignedInt(s, 16);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    };

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SCROLL = BUILDER
            .push("autoscroll")
            .comment("Enable autoscroller")
            .define("autoscroll", true);

    public static final ModConfigSpec.IntValue SCROLL_WAIT_TOP = BUILDER
            .comment("Time (ticks) before autoscrolling starts")
            .defineInRange("scroll_wait_top", 20, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SCROLL_WAIT_BOTTOM = BUILDER
            .comment("Time (ticks) before autoscrolling resets to top once it hits the bottom")
            .defineInRange("scroll_wait_bottom", 60, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue SCROLL_SPEED = BUILDER
            .comment("Multiplier to autoscroll speed")
            .defineInRange("scroll_speed", 0.5, 0.0, 255.0);

    public static final ModConfigSpec.BooleanValue SCALE = BUILDER
            .pop().push("autoscale")
            .comment("Enable autoscaler")
            .define("autoscale", false);

    public static final ModConfigSpec.DoubleValue SCALE_MAX = BUILDER
            .comment("Max autoscaling")
            .defineInRange("scale_max", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.ConfigValue<String> BACKGROUND_COLOR1 = BUILDER
            .pop().push("color")
            .comment("Tooltip background gradient start color (hex ARGB)")
            .define("background_color1", "F0100010", isInteger);

    public static final ModConfigSpec.ConfigValue<String> BACKGROUND_COLOR2 = BUILDER
            .comment("Tooltip background gradient end color (hex ARGB)")
            .define("background_color2", "F0100010", isInteger);

    public static final ModConfigSpec.ConfigValue<String> BORDER_COLOR1 = BUILDER
            .comment("Tooltip border gradient end color (hex ARGB)")
            .define("border_color1", "505000FF", isInteger);

    public static final ModConfigSpec.ConfigValue<String> BORDER_COLOR2 = BUILDER
            .comment("Tooltip border gradient end color (hex ARGB)")
            .define("border_color2", "5028007F", isInteger);

    public static final ModConfigSpec.DoubleValue OPACITY = BUILDER
            .comment("Opacity multiplier for the background. Used as a convenience over manually changing the ARGB values")
            .defineInRange("opacity", 1.0, 0, 1);

    public static final ModConfigSpec CLIENT = BUILDER.build();

    public static Lazy<Integer> BACKGROUND_COLOR1_INT = Lazy.of(() -> {
        int col = decodeHex(Config.BACKGROUND_COLOR1);
        return FastColor.ARGB32.color(
                (byte) (FastColor.ARGB32.alpha(col) * Config.OPACITY.get()),
                FastColor.ARGB32.red(col),
                FastColor.ARGB32.green(col),
                FastColor.ARGB32.blue(col)
        );
    });
    public static Lazy<Integer> BACKGROUND_COLOR2_INT = Lazy.of(() -> {
        int col = decodeHex(Config.BACKGROUND_COLOR2);
        return FastColor.ARGB32.color(
                (byte) (FastColor.ARGB32.alpha(col) * Config.OPACITY.get()),
                FastColor.ARGB32.red(col),
                FastColor.ARGB32.green(col),
                FastColor.ARGB32.blue(col)
        );
    });
    public static Lazy<Integer> BORDER_COLOR1_INT = Lazy.of(() -> decodeHex(BORDER_COLOR1));
    public static Lazy<Integer> BORDER_COLOR2_INT = Lazy.of(() -> decodeHex(BORDER_COLOR2));

    public static int decodeHex(ModConfigSpec.ConfigValue<String> value) {
        return Integer.parseUnsignedInt(value.get(), 16);
    }
}
