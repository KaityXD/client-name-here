package hack.raze.client.utils;

import hack.raze.client.api.ChatCompat;
import net.minecraft.network.chat.Component;

public class ChatUtils implements Imports {
    public static void chat(String text) {
        ChatCompat.addMessage(Component.literal(text));
    }

    public static void chat(CharSequence text) {
        ChatCompat.addMessage(Component.literal(text.toString()));
    }
}
