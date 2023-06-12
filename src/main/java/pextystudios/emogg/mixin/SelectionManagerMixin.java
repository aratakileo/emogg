package pextystudios.emogg.mixin;

import net.minecraft.client.util.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.Emoji;

import java.util.function.Supplier;

@Mixin(SelectionManager.class)
public abstract class SelectionManagerMixin {

    @Shadow private int selectionStart;

    @Shadow @Final private Supplier<String> stringGetter;

    @Shadow public abstract void delete(int cursorOffset);

    @Shadow public abstract void insert(String string);

    @Inject(method="insert(Ljava/lang/String;Ljava/lang/String;)V",at=@At("TAIL"))
    private void inject(String _unused, String insertion, CallbackInfo ci){
        Emogg.LOGGER.info("Process: SelectionManagerMixin");

        String text = stringGetter.get();
        for(Emoji emoji: Emogg.getInstance().emojis.values()){
            if (emoji.match(text,selectionStart-1)){
//                delete(-emoji.getCode().length());
//                insert(emoji.getCode());
                break;
            }
        }
    }
}
