import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

class Scratch {

    private static final String REGEX = "[^${}]+";

    private static final String I18N_DIRECTORY =
            //"/Users/monsif.elaissoussi/workspace/app-seller-payment/seller-payment-database/seller-payment-database-install-mt-app/src/main/resources/META-INF/i18n/template-email-fields/";
          "/Users/monsif.elaissoussi/workspace/app-seller-payment/seller-payment-app/src/main/resources/META-INF/i18n/";
          //"/Users/monsif.elaissoussi/workspace/app-seller-payment/seller-payment-database/seller-payment-database-install-mt-app/src/main/resources/META-INF/i18n/template-email/";

     private static final String
             TO_CHECK = I18N_DIRECTORY + "messages_bg.properties",
             REFERENCE = I18N_DIRECTORY + "messages.properties";

    public static void main(String[] args) throws IOException {

        Path toCheck = Paths.get(TO_CHECK), reference = Paths.get(REFERENCE);

        Checker translationChecker = CheckerFactory.createChecker(reference, toCheck);

        translationChecker.check();

    }

    interface Checker {
        boolean check();
    }

    static class CheckerFactory {

        static Checker createChecker(Path source, Path check) {

            String fileExtension =
                    Optional.of(source.toFile())
                            .map(file -> file.getName())
                            .map(name -> name.substring(name.lastIndexOf(".") + 1))
                            .orElse("");

            if ("properties".equals(fileExtension))
                return new PropertiesTranslationChecker(source, check);

            if ("xml".equals(fileExtension))
                return new XmlTranslationChecker(source, check);

            return null;
        }
    }



    static class PropertiesTranslationChecker implements Checker {

        private Properties referenceProperties;
        private Properties checkProperties;

        PropertiesTranslationChecker(Path source, Path check){
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
            return false;
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

    static class XmlTranslationChecker implements Checker {

        private String reference;
        private String toCheck;

        XmlTranslationChecker(Path source, Path check){
            this.reference = read(source);
            this.toCheck=read(check);
        }

        public boolean check(){

            var referenceValuePattern = reference.replaceAll(REGEX, "").trim();
            var toCheckValuePattern = toCheck.replaceAll(REGEX, "").trim();

            if(!toCheckValuePattern.equals(referenceValuePattern)) {
                System.out.println(" something is wrong with this Email content");
            }
            return true;
        }

        private String read(final Path file ) {
            try {
              return  Files.readString(file);
            } catch (IOException e) {
            }
            return null;
        }
    }

}
