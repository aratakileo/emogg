package pextystudios.emogg.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import pextystudios.emogg.gui.component.Button;
import pextystudios.emogg.EmoggConfig;

public class SettingsScreen extends AbstractScreen {
    public SettingsScreen() {
        super(Component.translatable("emogg.settings.title"));
    }

    public SettingsScreen(Screen parent) {
        super(Component.translatable("emogg.settings.title"), parent);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(0, 40, getUseBuiltinEmojisEnabledText()) {{
            setHint(Component.translatable("emogg.settings.option.is_using_builtin_emojis.description"));
            setOnClicked(button -> {
                EmoggConfig.instance.isUsingBuiltinEmojis = !EmoggConfig.instance.isUsingBuiltinEmojis;
                button.setMessage(getUseBuiltinEmojisEnabledText());
            });
            x = horizontalCenter() - width / 2;
        }});

        addRenderableWidget(new Button(0, 62, getExperimentalExperienceEnabledText()) {{
            setHint(Component.translatable("emogg.settings.option.is_experimental_experience_enabled.description"));
            setOnClicked(button -> {
                EmoggConfig.instance.isExperimentalExperienceEnabled = !EmoggConfig.instance.isExperimentalExperienceEnabled;
                button.setMessage(getExperimentalExperienceEnabledText());
            });
            x = horizontalCenter() - width / 2;
        }});

        addRenderableWidget(new Button(0, 84, getDebugModeEnabledText()) {{
            setOnClicked(button -> {
                EmoggConfig.instance.isDebugModeEnabled = !EmoggConfig.instance.isDebugModeEnabled;
                button.setMessage(getDebugModeEnabledText());
            });
            x = horizontalCenter() - width / 2;
        }});

        addRenderableWidget(new Button(0, 0, Component.translatable("emogg.settings.save_and_quit")) {{
            setOnClicked(button -> onClose());
            x = horizontalCenter() - width / 2;
            y = SettingsScreen.this.height - height - 20;
        }});
    }

    private Component getUseBuiltinEmojisEnabledText() {
        return Component.translatable(
                "emogg.settings.option.is_using_builtin_emojis.title",
                getState(EmoggConfig.instance.isUsingBuiltinEmojis)
        );
    }

    private Component getExperimentalExperienceEnabledText() {
        return Component.translatable(
                "emogg.settings.option.is_experimental_experience_enabled.title",
                getState(EmoggConfig.instance.isExperimentalExperienceEnabled)
        );
    }

    private Component getDebugModeEnabledText() {
        return Component.translatable(
                "emogg.settings.option.is_debug_mode_enabled.title",
                getState(EmoggConfig.instance.isDebugModeEnabled)
        );
    }

    private String getState(boolean state) {
        return "§l" + (state ? "§2" : "§c") + Language.getInstance().getOrDefault("emogg.gui.state." + (state ? "enabled" : "disabled"));
    }

    @Override
    public void onClose() {
        EmoggConfig.save();
        super.onClose();
    }
}
