import java.math.BigInteger;
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

    private final BigInteger e;
    private final BigInteger n;
    private final BigInteger cipher;

    public RSACracker(BigInteger e, BigInteger n, BigInteger cipher) {
        this.e = e;
        this.n = n;
        this.cipher = cipher;
    }

    private String crackRSA(String cipher, File keyFile) {
        getKeyFromFile(keyFile);
        byte[] bytes = Base64.getDecoder().decode(cipher);
        this.cipher = new BigInteger(bytes);

        try {
            BigInteger text = execute();
            return new String(text.toByteArray());
        } catch (RSACrackerException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private void getKeyFromFile(File keyFile) {
        try {
            BigInteger n = null, e = null;
            for(String line : Files.readAllLines(keyFile.toPath(), StandardCharsets.UTF_8)) {
                if(line.contains("n")) n = new BigInteger(line.replaceAll("[^0-9]", ""));
                if(line.contains("e")) e = new BigInteger(line.replaceAll("[^0-9]", ""));
            }
            this.n = n;
            this.e = e;
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
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
        public String crack(String cipher, File keyFile) {
            return crackRSA(cipher, keyFile);
        }
    }
}