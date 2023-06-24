package pextystudios.emogg.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import pextystudios.emogg.gui.component.Button;
import pextystudios.emogg.handler.ConfigHandler;

public class SettingsScreen extends AbstractScreen {
    public SettingsScreen(Screen parent) {
        super(new TextComponent("Emogg settings"), parent);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(0, 40, getUseBuiltinEmojisEnabledText()) {{
                setHint("If this option disabled, built-in emojis will not be offered in the suggestions when you enter the name of the emoji, and they will also remove from the emoji picker");
                setOnClicked(button -> {
                    ConfigHandler.data.useBuiltinEmojiEnabled = !ConfigHandler.data.useBuiltinEmojiEnabled;
                    button.setMessage(getUseBuiltinEmojisEnabledText());
                });
                x = centerX() - width / 2;
        }});

        addRenderableWidget(new Button(0, 62, getExperimentalExperienceEnabledText()) {{
            setHint("If this option enabled, then the experimental version of emoji picker will be shown in the chat screen. It is not recommended to include it if there are more than 81 emojis available in your collection!");
            setOnClicked(button -> {
                ConfigHandler.data.isExperimentalExperienceEnabled = !ConfigHandler.data.isExperimentalExperienceEnabled;
                button.setMessage(getExperimentalExperienceEnabledText());
            });
            x = centerX() - width / 2;
        }});

        addRenderableWidget(new Button(0, 0, "Save & Quit") {{
            setOnClicked(button -> onClose());
            x = centerX() - width / 2;
            y = SettingsScreen.this.height - height - 2;
        }});
    }

    private Component getUseBuiltinEmojisEnabledText() {
        return new TextComponent(
                "Use built-in emoji: §l" + (ConfigHandler.data.useBuiltinEmojiEnabled ? "§2Enabled" : "§cDisabled")
        );
    }

    private Component getExperimentalExperienceEnabledText() {
        return new TextComponent(
                "Experimental experience: §l" + (ConfigHandler.data.isExperimentalExperienceEnabled ? "§2Enabled" : "§cDisabled")
        );
    }

    @Override
    public void onClose() {
        ConfigHandler.save();
        super.onClose();
    }
}
