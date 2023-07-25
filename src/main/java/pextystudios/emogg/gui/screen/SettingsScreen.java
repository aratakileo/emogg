package pextystudios.emogg.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import pextystudios.emogg.gui.component.Button;
import pextystudios.emogg.handler.ConfigHandler;

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
                setHint(Component.translatable("emogg.settings.option.use_builtin_emoji.description"));
                setOnClicked(button -> {
                    ConfigHandler.data.useBuiltinEmojiEnabled = !ConfigHandler.data.useBuiltinEmojiEnabled;
                    button.setMessage(getUseBuiltinEmojisEnabledText());
                });
                x = horizontalCenter() - width / 2;
        }});

        addRenderableWidget(new Button(0, 62, getExperimentalExperienceEnabledText()) {{
            setHint(Component.translatable("emogg.settings.option.experimental_experience.description"));
            setOnClicked(button -> {
                ConfigHandler.data.isExperimentalExperienceEnabled = !ConfigHandler.data.isExperimentalExperienceEnabled;
                button.setMessage(getExperimentalExperienceEnabledText());
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
                "emogg.settings.option.use_builtin_emoji.title",
                getState(ConfigHandler.data.useBuiltinEmojiEnabled)
        );
    }

    private Component getExperimentalExperienceEnabledText() {
        return Component.translatable(
                "emogg.settings.option.experimental_experience.title",
                getState(ConfigHandler.data.isExperimentalExperienceEnabled)
        );
    }

    private String getState(boolean state) {
        return "§l" + (state ? "§2" : "§c") + Language.getInstance().getOrDefault("emogg.gui.state." + (state ? "enabled" : "disabled"));
    }

    @Override
    public void onClose() {
        ConfigHandler.save();
        super.onClose();
    }
}
