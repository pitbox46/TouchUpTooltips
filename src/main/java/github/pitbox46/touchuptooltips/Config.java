package github.pitbox46.touchuptooltips;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SCROLL = BUILDER
            .push("autoscroll")
            .comment("Enable autoscroller")
            .define("autoscroll", true);

    public static final ModConfigSpec.IntValue SCROLL_WAIT = BUILDER
            .comment("Time (ticks) before autoscrolling starts AND the time that the scroller hangs at the bottom before resetting to the top")
            .defineInRange("scroll_wait", 20, 0, Integer.MAX_VALUE);

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

    public static final ModConfigSpec CLIENT = BUILDER.build();
}
