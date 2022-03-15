import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class Scratch {

     private static final String
             I18N_DIRECTORY = "/Users/monsif.elaissoussi/workspace/app-seller-payment/seller-payment-app/src/main/resources/META-INF/i18n/";

     private static final String
             TO_CHECK = I18N_DIRECTORY + "messages_ar.properties",
             REFERENCE = I18N_DIRECTORY + "messages.properties";

    public static void main(String[] args) throws IOException {

        Path toCheck = Paths.get(TO_CHECK), reference = Paths.get(REFERENCE);

        TranslationChecker translationChecker = new TranslationChecker(reference, toCheck);

        translationChecker.check();

    }

    static class TranslationChecker {

        private static final String REGEX = "[^{}]+";

        private Properties referenceProperties;
        private Properties checkProperties;

        TranslationChecker(Path source, Path check){
            this.referenceProperties = read(source);
            this.checkProperties=read(check);
        }

        public boolean check(){

            referenceProperties.forEach(
                    (k, v) -> {

                            var toCheckValue = checkProperties.getProperty(String.valueOf(k));

                            if(toCheckValue == null) return;

                            // replace all property value except {} brackets
                            var referenceValuePattern = String.valueOf(v).replaceAll(REGEX, "").trim();
                            var toCheckValuePattern = toCheckValue.replaceAll(REGEX, "").trim();

                            if(!toCheckValuePattern.equals(referenceValuePattern)) {
                                System.out.println(" something is wrong with this key = " + k);
                            }
                    }
            );
            return true;
        }

        private Properties read(final Path file ) {

            var properties = new Properties();

            try( var in = new InputStreamReader(
                    new FileInputStream( file.toFile() ), StandardCharsets.UTF_8 ) ) {
                properties.load( in );
            }
            catch (IOException e) {
            }
            return properties;
        }
    }
}
