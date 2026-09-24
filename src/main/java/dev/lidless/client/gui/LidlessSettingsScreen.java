package dev.lidless.client.gui;

import dev.lidless.client.LidlessClient;
import dev.lidless.config.LidlessConfig;
import dev.lidless.config.LidlessPolicy;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

public final class LidlessSettingsScreen extends OptionsSubScreen {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 20;

    public LidlessSettingsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, Component.translatable("lidless.options.title"));
    }

    @Override
    protected void addOptions() {
        LidlessConfig config = LidlessClient.config();

        list.addSmall(List.of(
                toggleButton("lidless.options.peek", "lidless.options.peek.tooltip",
                        config::peek, config::setPeek),
                toggleButton("lidless.options.require_sneak", "lidless.options.require_sneak.tooltip",
                        config::requireSneak, config::setRequireSneak)));

        list.addSmall(List.of(
                toggleButton("lidless.options.compact", "lidless.options.compact.tooltip",
                        config::compact, config::setCompact),
                toggleButton("lidless.options.remember", "lidless.options.remember.tooltip",
                        config::remember, config::setRemember)));

        list.addSmall(List.of(
                toggleButton("lidless.options.tooltip_preview", "lidless.options.tooltip_preview.tooltip",
                        config::tooltipPreview, config::setTooltipPreview),
                toggleButton("lidless.options.search_box", "lidless.options.search_box.tooltip",
                        config::searchBox, config::setSearchBox)));

        list.addSmall(List.of(
                toggleButton("lidless.options.sort_buttons", "lidless.options.sort_buttons.tooltip",
                        config::sortButtons, config::setSortButtons),
                toggleButton("lidless.options.middle_click_sort", "lidless.options.middle_click_sort.tooltip",
                        config::middleClickSort, config::setMiddleClickSort)));

        AbstractWidget rowsSlider = new StepSlider(
                "lidless.options.rows", "lidless.options.rows.tooltip",
                LidlessPolicy.MIN_ROWS, LidlessPolicy.MAX_ROWS, 1.0,
                config.rows(),
                value -> Integer.toString((int) value),
                value -> config.setRows((int) value));
        list.addSmall(List.of(sortOrderButton(config), rowsSlider));

        AbstractWidget xSlider = new StepSlider(
                "lidless.options.x_position", "lidless.options.x_position.tooltip",
                LidlessPolicy.MIN_POSITION, LidlessPolicy.MAX_POSITION, 1.0,
                config.xPosition(),
                value -> (int) value + "%",
                value -> config.setXPosition((int) value));
        AbstractWidget ySlider = new StepSlider(
                "lidless.options.y_position", "lidless.options.y_position.tooltip",
                LidlessPolicy.MIN_POSITION, LidlessPolicy.MAX_POSITION, 1.0,
                config.yPosition(),
                value -> (int) value + "%",
                value -> config.setYPosition((int) value));
        list.addSmall(List.of(xSlider, ySlider));

        list.addSmall(List.of(resetButton(config)));
    }

    @Override
    public void removed() {
        super.removed();
        LidlessClient.saveConfig();
    }

    private AbstractWidget toggleButton(String key, String tooltipKey, BooleanSupplier getter, Consumer<Boolean> setter) {
        return Button.builder(onOffLabel(key, getter.getAsBoolean()), button -> {
                    boolean next = !getter.getAsBoolean();
                    setter.accept(next);
                    button.setMessage(onOffLabel(key, next));
                })
                .width(WIDTH)
                .tooltip(Tooltip.create(Component.translatable(tooltipKey)))
                .build();
    }

    private static Component onOffLabel(String key, boolean value) {
        Component state = Component.translatable(value ? "options.on" : "options.off");
        return Component.translatable("options.generic_value", Component.translatable(key), state);
    }

    private AbstractWidget sortOrderButton(LidlessConfig config) {
        return Button.builder(sortOrderLabel(config), button -> {
                    config.setSortOrder(config.sortOrder().next());
                    button.setMessage(sortOrderLabel(config));
                })
                .width(WIDTH)
                .tooltip(Tooltip.create(Component.translatable("lidless.options.sort_order.tooltip")))
                .build();
    }

    private static Component sortOrderLabel(LidlessConfig config) {
        return Component.translatable("options.generic_value",
                Component.translatable("lidless.options.sort_order"),
                Component.translatable(config.sortOrder().translationKey()));
    }

    private AbstractWidget resetButton(LidlessConfig config) {
        return Button.builder(Component.translatable("lidless.options.reset"), button -> {
                    config.resetToDefaults();
                    ScreenOpener.open(minecraft, new LidlessSettingsScreen(lastScreen, options));
                })
                .width(WIDTH)
                .build();
    }

    private static final class StepSlider extends AbstractSliderButton {
        private final String captionKey;
        private final double min;
        private final double max;
        private final double step;
        private final DoubleFunction<String> format;
        private final DoubleConsumer onChange;

        StepSlider(
                String captionKey,
                String tooltipKey,
                double min,
                double max,
                double step,
                double initial,
                DoubleFunction<String> format,
                DoubleConsumer onChange) {
            super(0, 0, WIDTH, HEIGHT, Component.empty(), 0.0);
            this.captionKey = captionKey;
            this.min = min;
            this.max = max;
            this.step = step;
            this.format = format;
            this.onChange = onChange;
            this.value = (snap(initial) - min) / (max - min);
            setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
            updateMessage();
        }

        private double snap(double raw) {
            double clamped = Math.max(min, Math.min(max, raw));
            double snapped = min + Math.round((clamped - min) / step) * step;
            return Math.max(min, Math.min(max, snapped));
        }

        private double current() {
            return snap(min + value * (max - min));
        }

        @Override
        protected void updateMessage() {
            Component shown = Component.literal(format.apply(current()));
            setMessage(Component.translatable("options.generic_value", Component.translatable(captionKey), shown));
        }

        @Override
        protected void applyValue() {
            double snapped = current();
            value = (snapped - min) / (max - min);
            onChange.accept(snapped);
        }
    }
}
