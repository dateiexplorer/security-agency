public class ShiftCracker {

    private static ShiftCracker instance = new ShiftCracker();

    // Port
    public Port port;

    private ShiftCracker() {
        port = new Port();
    }

    public static ShiftCracker getInstance() {
        return instance;
    }

    public static String innerDecrypt(String encryptedMessage) {
        int[] codes = new int[encryptedMessage.length()];
        for (int i = 0; i < encryptedMessage.length(); i++) {
            codes[i] = encryptedMessage.codePointAt(i);
        }

        for (int i = 1; i <= Integer.MAX_VALUE; i++) {
            String result = smartShift(i, codes);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static String smartShift(int shift, int[] codes) {
        // Define a new array for decrypted codes.
        int[] decryptedCodes = new int[codes.length];

        // Shift characters.
        for (int i = 0; i < codes.length; i++) {
            decryptedCodes[i] = codes[i] - shift;

            // A message doesn't include control characters.
            // Filter this solution.
            if (Character.isISOControl(decryptedCodes[i])) {
                return null;
            }
        }

        // Do upper case transformation for further analysis.
        char[] uppercase = new char[decryptedCodes.length];
        for (int i = 0; i < decryptedCodes.length; i++) {
            uppercase[i] = (char) Character.toUpperCase(decryptedCodes[i]);
        }

        // Analyse frequency of special characters
        double frequency = 0;
        double aFrequency = 0;
        double eFrequency = 0;
        double iFrequency = 0;
        double oFrequency = 0;
        double uFrequency = 0;

        // Analyse frequency of alphabetic characters.
        // If there are more non alphabetic characters than alphabetic characters, the solution cannot be right.
        double alphabeticFrequency = 0;
        double wordFrequency = 0;

        for (int i = 0; i < uppercase.length; i++) {
            frequency++;

            switch (uppercase[i]) {
                case 'A':
                    aFrequency++;
                    break;
                case 'E':
                    eFrequency++;
                    break;
                case 'I':
                    iFrequency++;
                    break;
                case 'O':
                    oFrequency++;
                    break;
                case 'U':
                    uFrequency++;
                    break;
                default:
                    break;
            }

            if (!Character.isAlphabetic(uppercase[i]) && !Character.isWhitespace(uppercase[i])) {
                // Not at end
                if ((i + 1) < uppercase.length) {
                    if (!Character.isWhitespace(uppercase[i + 1])) {
                        return null;
                    }
                }
            }

            // Count words
            if (Character.isSpaceChar(uppercase[i])) {
                wordFrequency++;
            }
        }

        for (int c : decryptedCodes) {
            // Check if character is alphabetic
            if (Character.isAlphabetic(c) || Character.isWhitespace(c)) {
                alphabeticFrequency++;
            }
        }


        StringBuilder stringBuilder = new StringBuilder();
        for (int c : decryptedCodes) {
            stringBuilder.append((char) c);
        }

        if ((eFrequency / frequency >= 0.05 || aFrequency / frequency >= 0.05 ||
                iFrequency / frequency >= 0.05 || oFrequency / frequency >= 0.05 ||
                uFrequency / frequency >= 0.05) &&
                alphabeticFrequency / frequency >= 0.80) {
            return stringBuilder.toString();
        }

        return null;
    }

    public class Port implements IShiftCracker {

        @Override
        public String decrypt(String encryptedMessage) {
            return innerDecrypt(encryptedMessage);
        }
    }
}
