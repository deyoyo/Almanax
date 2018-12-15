package commands.config;

import commands.model.AbstractCommand;
import data.ChannelLanguage;
import data.Guild;
import enums.Language;
import exceptions.BasicDiscordException;
import exceptions.DiscordException;
import exceptions.NotFoundDiscordException;
import exceptions.TooMuchDiscordException;
import sx.blah.discord.handle.obj.IMessage;
import util.Message;
import util.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by steve on 14/07/2016.
 */
public class LanguageCommand extends AbstractCommand {

    private DiscordException tooMuchLangs;
    private DiscordException notFoundLang;

    public LanguageCommand(){
        super("lang", "(\\s+-channel)?(\\s+[A-Za-z]+)?");
        setUsableInMP(false);
        tooMuchLangs = new TooMuchDiscordException("lang");
        notFoundLang = new NotFoundDiscordException("lang");
    }

    @Override
    public void request(IMessage message, Matcher m, Language lg) {
        if (m.group(2) != null) { // Ajouts
            if (isUserHasEnoughRights(message)) {
                List<Language> langs = new ArrayList<>();
                for(Language lang : Language.values())
                    if (m.group(2).trim().toUpperCase().equals(lang.getAbrev()))
                        langs.add(lang);

                if (langs.size() == 1) {
                    if (m.group(1) == null) {
                        Guild.getGuild(message.getGuild()).setLanguage(langs.get(0));
                        lg = langs.get(0);
                        Message.sendText(message.getChannel(), message.getGuild().getName()
                                + " " + Translator.getLabel(lg, "lang.request.1") + " " + langs.get(0));
                    } else {
                        ChannelLanguage chan = ChannelLanguage.getChannelLanguages().get(message.getChannel().getLongID());
                        if (chan != null){
                            if (chan.getLang().equals(langs.get(0))){
                                chan.removeToDatabase();
                                lg = Translator.getLanguageFrom(message.getChannel());
                                Message.sendText(message.getChannel(), message.getChannel().getName()
                                        + " " + Translator.getLabel(lg, "lang.request.2") + " "
                                        + Guild.getGuild(message.getGuild()).getLanguage());
                            }
                            else {
                                chan.setLanguage(langs.get(0));
                                lg = langs.get(0);
                                Message.sendText(message.getChannel(), message.getChannel().getName()
                                        + " " + Translator.getLabel(lg, "lang.request.1") + " " + chan.getLang());
                            }
                        }
                        else {
                            chan = new ChannelLanguage(langs.get(0), message.getChannel().getLongID());
                            chan.addToDatabase();
                            lg = langs.get(0);
                            Message.sendText(message.getChannel(), message.getChannel().getName()
                                    + " " + Translator.getLabel(lg, "lang.request.1") + " " + chan.getLang());
                        }
                    }
                }
                else if (langs.isEmpty())
                    notFoundLang.throwException(message, this, lg);
                else
                    tooMuchLangs.throwException(message, this, lg);

            } else
                BasicDiscordException.NO_ENOUGH_RIGHTS.throwException(message, this, lg);
        }
        else { // Consultation
            String text = "**" + message.getGuild().getName() + "** " + Translator.getLabel(lg, "lang.request.3")
                    + " " + Guild.getGuild(message.getGuild()).getLanguage() + ".";

            ChannelLanguage chanLang = ChannelLanguage.getChannelLanguages().get(message.getChannel().getLongID());
            if (chanLang != null)
                text += "\nLe salon *" + message.getChannel().getName() + "* " + Translator.getLabel(lg, "lang.request.3")
                + " " + chanLang.getLang() + ".";
            Message.sendText(message.getChannel(), text);
        }
    }

    @Override
    public String help(Language lg, String prefixe) {
        StringBuilder st = new StringBuilder(" (");
        for(Language lang : Language.values())
            st.append(lang.getAbrev()).append(", ");
        st.setLength(st.length() - 2);
        st.append(").");

        return "**" + prefixe + name + "** " + Translator.getLabel(lg, "lang.help") + st.toString();
    }

    @Override
    public String helpDetailed(Language lg, String prefixe) {
        return help(lg, prefixe)
                + "\n`" + prefixe + name + " `*`language`* : " + Translator.getLabel(lg, "lang.help.detailed.1")
                + "\n`" + prefixe + name + " -channel `*`language`* : " + Translator.getLabel(lg, "lang.help.detailed.2") + "\n";
    }
}
