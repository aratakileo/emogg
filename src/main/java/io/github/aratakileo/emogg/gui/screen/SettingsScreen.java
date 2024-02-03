package io.github.aratakileo.emogg.gui.screen;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.gui.component.Button;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends AbstractScreen {
    private final @Nullable List<Button> debugButtons = new ArrayList<>();
    private @Nullable Button subdebug_btn_1;

    public SettingsScreen() {
        super(Component.translatable("emogg.settings.title"));
    }

    public SettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("emogg.settings.title"), parent);
    }

    @Override
    protected void init() {
        int y = 40 - 20;
        addConfigToggleButton(
                "enableDebugMode",
                "enable_debug_mode",
                y+=20,
                this::onToggleDebugMode
        );

        var debugButton = addConfigToggleButton(
                "enableAtlasDebugHUD",
                "enable_atlas_debug_hud",
                y+=20,
                null
        );
        subdebug_btn_1 = debugButton;

        if (Objects.nonNull(debugButton) && Objects.nonNull(debugButtons))
            debugButtons.add(debugButton);

        debugButton = new Button(
                0, y+=20,
                Component.translatable("emogg.settings.option.reload_all.title")) {{
            setTooltip(Component.translatable("emogg.settings.option.reload_all.description"));
            setOnClicked(button -> EmojiManager.getInstance().getEmojisStream()
                    .forEach(e -> e.reload(false)));
            x = horizontalCenter() - width / 2;
        }};

        addRenderableWidget(debugButton);

        if (Objects.nonNull(debugButtons))
            debugButtons.add(debugButton);

        debugButton = new Button(
                0, y+=20,
                Component.translatable("emogg.settings.option.force_load_all.title")) {{
            setTooltip(Component.translatable("emogg.settings.option.force_load_all.description"));
            setOnClicked(button -> EmojiManager.getInstance().getEmojisStream()
                    .forEach(Emoji::forceLoad));
            x = horizontalCenter() - width / 2;
        }};

        addRenderableWidget(debugButton);

        if (Objects.nonNull(debugButtons))
            debugButtons.add(debugButton);

        addRenderableWidget(new Button(0, 0, Component.translatable("emogg.settings.save_and_quit")) {{
            setOnClicked(button -> onClose());
            x = horizontalCenter() - width / 2;
            y = SettingsScreen.this.height - height - 20;
        }});

        onToggleDebugMode(null);
    }

    private @Nullable Button addConfigToggleButton(
            @NotNull String configField,
            @NotNull String translationKey,
            int y,
            @Nullable Consumer<Button> onToggle
    ) {
        if (!(EmoggConfig.getField(configField) instanceof Boolean)) {
            Emogg.LOGGER.warn("Failed to create boolean config button: invalid type");
            return null;
        }
        final var title = "emogg.settings.option."+translationKey+".title";
        final var button = new Button(
                0, y,
                Component.translatable(title, getBooleanValueText((Boolean) EmoggConfig.getField(configField)))) {{
            setTooltip(Component.translatable("emogg.settings.option."+translationKey+".description"));
            setOnClicked(button -> {
                EmoggConfig.setField(configField, ! (Boolean) EmoggConfig.getField(configField));
                ((Button)button).setMessage(Component.translatable(title, getBooleanValueText((Boolean) EmoggConfig.getField(configField))), true);

                if (Objects.nonNull(onToggle))
                    onToggle.accept((Button) button);
            });
            x = horizontalCenter() - width / 2;
        }};

        addRenderableWidget(button);

        return button;
    }

    private @NotNull String getBooleanValueText(boolean state) {
        return "§l"
                + (state ? "§2" : "§c")
                + Language.getInstance().getOrDefault("emogg.gui.state." + (state ? "enabled" : "disabled"));
    }

    private void onToggleDebugMode(@Nullable Button button /* only for replacing lambda with method reference */) {
        if (Objects.isNull(debugButtons)) return;

        debugButtons.forEach(btn -> btn.visible = EmoggConfig.instance.enableDebugMode);

        if (
                !EmoggConfig.instance.enableDebugMode
                        && EmoggConfig.instance.enableAtlasDebugHUD
                        && Objects.nonNull(subdebug_btn_1)
        ) subdebug_btn_1.onPress(false);
    }

    @Override
    public void onClose() {
        EmoggConfig.save();
        super.onClose();
    }
}
