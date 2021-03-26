import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class RSACracker {

    private static RSACracker instance = new RSACracker();

    // Port
    public Port port;

    private RSACracker() {
        port = new Port();
    }

    public static RSACracker getInstance() {
        return instance;
    }

    private BigInteger e;
    private BigInteger n;
    private BigInteger cipher;

    private void readKeyFromFile(File keyfile) throws IOException {
        StringBuilder jsonData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(keyfile));

        String line = null;
        while (null != (line = reader.readLine())) {
            jsonData.append(line);
        }

        JSONObject jsonObject = new JSONObject(jsonData.toString());
        JSONObject keyObject = jsonObject.getJSONObject("public");

        this.n = keyObject.getBigInteger("modulus");
        System.out.println(n);
        this.e = keyObject.getBigInteger("exponent");
        System.out.println(e);
    }

    private String innerDecrypt(String encryptedMessage, File keyfile) {
        try {
            readKeyFromFile(keyfile);
            byte[] bytes = Base64.getDecoder().decode(encryptedMessage);
            this.cipher = new BigInteger(bytes);

            BigInteger text = execute();
            System.out.println(text);
            return new String(text.toByteArray());
        } catch (RSACrackerException e) {
            System.out.println(e.getMessage());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public BigInteger execute() throws RSACrackerException {
        BigInteger p, q, d;
        List<BigInteger> factorList = factorize(n);

        if (factorList.size() != 2) {
            throw new RSACrackerException("cannot determine factors p and q");
        }

        p = factorList.get(0);
        q = factorList.get(1);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        d = e.modInverse(phi);


        return cipher.modPow(d, n);
    }

    public List<BigInteger> factorize(BigInteger n) {
        BigInteger two = BigInteger.valueOf(2);
        List<BigInteger> factorList = new LinkedList<>();

        if (n.compareTo(two) < 0) {
            throw new IllegalArgumentException("must be greater than one");
        }

        while (n.mod(two).equals(BigInteger.ZERO)) {
            factorList.add(two);
            n = n.divide(two);
        }

        if (n.compareTo(BigInteger.ONE) > 0) {
            BigInteger factor = BigInteger.valueOf(3);
            while (factor.multiply(factor).compareTo(n) <= 0) {
                if (n.mod(factor).equals(BigInteger.ZERO)) {
                    factorList.add(factor);
                    n = n.divide(factor);
                } else {
                    factor = factor.add(two);
                }
            }

            factorList.add(n);
        }

        return factorList;
    }

    public class Port implements IRSACracker {

        @Override
        public String decrypt(String encryptedMessage, File keyfile) {
            return innerDecrypt(encryptedMessage, keyfile);
        }
    }
}