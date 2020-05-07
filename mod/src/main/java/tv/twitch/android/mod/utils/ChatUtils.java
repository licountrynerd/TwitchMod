package tv.twitch.android.mod.utils;


import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tv.twitch.android.mod.bridges.IChatMessageFactory;
import tv.twitch.android.mod.emotes.EmoteManager;
import tv.twitch.android.mod.models.Emote;
import tv.twitch.android.models.chat.MessageToken;
import tv.twitch.chat.ChatEmoticon;


public class ChatUtils {
    public enum EmoteSet {
        GLOBAL("-110"),
        FFZ("-109"),
        BTTV("-108");

        public final String mId;

        EmoteSet(String id) {
            this.mId = id;
        }

        public String getId() {
            return mId;
        }
    }

    public static ChatEmoticon[] emotesToChatEmoticonArr(Collection<Emote> emoteList) {
        if (emoteList == null || emoteList.size() == 0)
            return new ChatEmoticon[0];

        ChatEmoticon[] chatEmoticons = new ChatEmoticon[emoteList.size()];

        int i = 0;
        for (Emote emote : emoteList) {
            chatEmoticons[i++] = emote.getChatEmoticon();
        }

        return chatEmoticons;
    }

    public static Spanned addTimestamp(Spanned spanned, Date date) {
        SpannableString dateString = SpannableString.valueOf(new SimpleDateFormat("HH:mm ", Locale.UK).format(date));
        dateString.setSpan(new RelativeSizeSpan(0.75f), 0, dateString.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder message = new SpannableStringBuilder(dateString);
        message.append(new SpannableStringBuilder(spanned));
        return SpannableString.valueOf(message);
    }

    public static String getRawMessage(List<MessageToken> tokens) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MessageToken messageToken : tokens) {
            if (messageToken instanceof MessageToken.TextToken)
                stringBuilder.append(((MessageToken.TextToken) messageToken).getText());
            else if (messageToken instanceof MessageToken.EmoticonToken)
                stringBuilder.append(((MessageToken.EmoticonToken) messageToken).getText());
            else if (messageToken instanceof MessageToken.MentionToken)
                stringBuilder.append(((MessageToken.MentionToken) messageToken).getText());
            else if (messageToken instanceof MessageToken.UrlToken)
                stringBuilder.append(((MessageToken.UrlToken) messageToken).getUrl());
        }

        return stringBuilder.toString();
    }

    public static SpannedString injectCopySpan(SpannedString messageSpan, final List<MessageToken> tokens) {
        if (TextUtils.isEmpty(messageSpan)) {
            Logger.warning("empty message");
            return messageSpan;
        }

        if (tokens == null || tokens.size() == 0) {
            Logger.warning("message has no tokens");
            return messageSpan;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(messageSpan);
        spannableStringBuilder.setSpan(new CopyChatMessage(tokens), 0, messageSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return SpannedString.valueOf(spannableStringBuilder);
    }

    public static SpannedString injectEmotesSpan(IChatMessageFactory factory, EmoteManager emoteManager, SpannedString messageSpan, int channelID, Emote.Size size) {
        if (TextUtils.isEmpty(messageSpan)) {
            Logger.warning("Empty messageSpan");
            return messageSpan;
        }

        if (emoteManager == null) {
            Logger.error("emoteManager is null");
            return messageSpan;
        }

        if (factory == null) {
            Logger.error("factory is null");
            return messageSpan;
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(messageSpan);

        boolean newWord = false;
        int startPos = 0;
        int messageLength = messageSpan.length();

        for (int currentPos = 0; currentPos <= messageLength; currentPos++) {
            if (currentPos != messageLength && messageSpan.charAt(currentPos) != ' ') {
                if (!newWord) {
                    newWord = true;
                    startPos = currentPos;
                }
            } else {
                if (newWord) {
                    newWord = false;

                    String code = String.valueOf(messageSpan.subSequence(startPos, currentPos));
                    Emote emote = emoteManager.getEmote(code, channelID);
                    if (emote != null) {
                        SpannableString emoteSpan = (SpannableString) factory.getSpannedEmote(emote.getUrl(size), emote.getCode(), emote.isGif());
                        if (emoteSpan != null) {
                            ssb.replace(startPos, currentPos, emoteSpan);
                        }
                    }
                }
            }
        }

        return SpannedString.valueOf(ssb);
    }
}
