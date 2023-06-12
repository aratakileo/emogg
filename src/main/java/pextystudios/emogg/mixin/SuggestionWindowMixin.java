package pextystudios.emogg.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.Emoji;

import java.util.List;

@Mixin(CommandSuggestor.SuggestionWindow.class)
public abstract class SuggestionWindowMixin {
    @Final @Shadow CommandSuggestor field_21615;

    @Shadow private int selection;

    @Shadow @Final private List<Suggestion> suggestions;

    @Inject(method="complete",at=@At("TAIL"))
    private void overwriteComplete(CallbackInfo ci){
        Emogg.LOGGER.info("Process: SuggestionWindowMixin");

        TextFieldWidget textFieldWidget = ((CommandSuggestorAccessor)field_21615).getTextField();
        Suggestion suggestion = this.suggestions.get(this.selection);
        int just = suggestion.getRange().getStart() + suggestion.getText().length() - 2;
        for(Emoji emoji: Emogg.getInstance().emojis.values()){
            int justTyped = just - emoji.getCode().length();
            if (emoji.match(textFieldWidget.getText(), justTyped)){
//                textFieldWidget.eraseCharacters( - emoji.getCode().length() - (1 + emoji.getCode().length()));
                textFieldWidget.setSelectionEnd(textFieldWidget.getCursor());
//                textFieldWidget.write(emoji.getCode());
                break;
            }
        }
    }
}
