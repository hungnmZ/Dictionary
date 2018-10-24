//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package GoogleAPI;

import GoogleAPI.parsing.Parse;
import GoogleAPI.parsing.ParseTextDetect;
import GoogleAPI.parsing.ParseTextTranslate;
import GoogleAPI.text.Text;
import GoogleAPI.text.TextTranslate;

public class Translator {
    private static Translator translator;

    private Translator() {
    }

    public static synchronized Translator getInstance() {
        if (translator == null) {
            translator = new Translator();
        }

        return translator;
    }

    public void translate(TextTranslate textTranslate) {
        Parse parse = new ParseTextTranslate(textTranslate);
        parse.parse();
    }

    public String translate(String text, String languageInput, String languageOutput) {
        Text input = new Text(text, languageInput);
        TextTranslate textTranslate = new TextTranslate(input, languageOutput);
        Parse parse = new ParseTextTranslate(textTranslate);
        parse.parse();
        return textTranslate.getOutput().getText();
    }

    public String detect(String text) {
        Text input = new Text(text);
        Parse parse = new ParseTextDetect(input);
        parse.parse();
        return input.getLanguage();
    }
}
