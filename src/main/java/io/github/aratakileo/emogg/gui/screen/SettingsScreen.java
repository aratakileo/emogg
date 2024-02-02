package io.github.aratakileo.emogg.gui.screen;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.gui.component.Button;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends AbstractScreen {
    public SettingsScreen() {
        super(Component.translatable("emogg.settings.title"));
    }

    public SettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("emogg.settings.title"), parent);
    }

    @Override
    protected void init() {
        addBooleanConfigButton("enableDebugMode", "enable_debug_mode", 40);
        addBooleanConfigButton("enableAtlasDebugHUD", "enable_atlas_debug_hud", 60);

        addRenderableWidget(new Button(0, 0, Component.translatable("emogg.settings.save_and_quit")) {{
            setOnClicked(button -> onClose());
            x = horizontalCenter() - width / 2;
            y = SettingsScreen.this.height - height - 20;
        }});
    }

    private void addBooleanConfigButton(String field, String translation, int y) {
        if (!(EmoggConfig.getField(field) instanceof Boolean)) {
            Emogg.LOGGER.warn("Failed to create boolean config button: invalid type");
            return;
        }
        final var title = "emogg.settings.option."+translation+".title";
        addRenderableWidget(new Button(
                0, y,
                Component.translatable(title, getBooleanValueText((Boolean) EmoggConfig.getField(field)))) {{
                    setTooltip(Component.translatable("emogg.settings.option."+translation+".description"));
                    setOnClicked(button -> {
                        EmoggConfig.setField(field, ! (Boolean) EmoggConfig.getField(field));
                        ((Button)button).setMessage(Component.translatable(title, getBooleanValueText((Boolean) EmoggConfig.getField(field))), true);
                    });
                    x = horizontalCenter() - width / 2;
        }});
    }

    private @NotNull String getBooleanValueText(boolean state) {
        return "§l"
                + (state ? "§2" : "§c")
                + Language.getInstance().getOrDefault("emogg.gui.state." + (state ? "enabled" : "disabled"));
    }

    @Override
    public void onClose() {
        EmoggConfig.save();
        super.onClose();
    }
}
