public class SecurityDB extends SecurityDBBase {

    private int capacity;
    private final int maxSize;
    private int numEntries;
    private String[] keys;
    private String[] values;
    private int[] numTimes;

    /**
     * Creates an empty hashtable and a variable to count non-empty elements.
     *
     * @param numPlanes             number of planes per day
     * @param numPassengersPerPlane number of passengers per plane
     */
    public SecurityDB(int numPlanes, int numPassengersPerPlane) {
        super(numPlanes, numPassengersPerPlane);

        capacity = nextPrime((numPlanes * numPassengersPerPlane) + 1);
        numEntries = 0;
        maxSize = SecurityDBBase.MAX_CAPACITY;
        keys = new String[this.capacity];
        values = new String[this.capacity];
        numTimes = new int[this.capacity];
    }

    @Override
    public int calculateHashCode(String key) {
        int charInt = key.charAt(0);
        int hashCode = 1 + charInt;
        int prevSum = hashCode;

        for (int i = 1; i < key.length(); i++) {
            charInt = key.charAt(i);
            hashCode += (prevSum + charInt);
            prevSum = prevSum + charInt;
        }
        return hashCode;
    }

    /**
     * Helper function to generate the hash function of the provided
     * passport id.
     *
     * @param passportId id of which hash function is to be generated from
     * @return hash code of id modulo database capacity
     */
    private int hashFunction(String passportId) {
        return calculateHashCode(passportId) % capacity;
    }

    @Override
    public int size() {
        return capacity;
    }

    @Override
    public String get(String passportId) {
        int i = hashFunction(passportId);
        while (keys[i] != null) {
            if (keys[i].equals(passportId))
                return values[i];
            i = (i + 1) % capacity;
        }
        return null;
    }

    @Override
    public boolean remove(String passportId) {
        if (get(passportId) == null)
            return false;

        int i = hashFunction(passportId);

        if (keys[i].equals(passportId)) {
            keys[i] = null;
            values[i] = null;
            numTimes[i] = 0;
            numEntries--;
            return true;
        } else
            return false;
    }

    @Override
    public boolean addPassenger(String name, String passportId) {
        int i = hashFunction(passportId);

        if (get(passportId) != null) {
            if (!name.equals(get(passportId)))
                System.err.print("Suspicious behaviour");
            else {
                numTimes[i]++;

                if (numTimes[i] > 5) {
                    System.err.print("Suspicious behaviour");
                    return false;
                }
                return true;
            }
            return false;
        }

        while (keys[i] != null)
            i = (i + 1) % capacity;

        keys[i] = passportId;
        values[i] = name;
        numTimes[i]++;
        numEntries++;
        if (numEntries > capacity)
            capacity = maxSize;

        return true;
    }

    @Override
    public int count() {
        return numEntries;
    }

    @Override
    public int getIndex(String passportId) {
        return hashFunction(passportId);
    }

    /**
     * Checks if given number is prime.
     *
     * @param n number to be checked
     * @return true if the number is prime, false otherwise
     **/
    private boolean isPrime(int n) {
        if (n == 2 || n == 3)
            return true;
        if (n == 1 || n % 2 == 0)
            return false;
        for (int i = 3; i * i <= n; i += 2)
            if (n % i == 0)
                return false;
        return true;
    }

    /**
     * Generates the next prime number greater than or equal to the given
     * number
     *
     * @param n number after which the prime number is to be generated
     * @return the next prime number greater than or equal to the given number
     **/
    private int nextPrime(int n) {
        if (n % 2 == 0)
            n++;
        while (!isPrime(n))
            n += 2;

        return n;
    }

    public static void main(String[] args) {
        SecurityDB db = new SecurityDB(3, 2);

        // add
        db.addPassenger("Rob Bekker", "Awb23f");
        db.addPassenger("Rob Bekher", "Awb23m");
        db.addPassenger("Kira Adams", "MKSD23");
        db.addPassenger("Kira Adams", "MKSD24");
        assert db.contains("Awb23f");

        // count
        assert db.count() == 4;

        // del
        db.remove("MKSD23");
        assert !db.contains("MKSD23");
        assert db.contains("Awb23f");

        // hashcodes
        assert db.calculateHashCode("Asb23f") == 1717;

        // suspicious
        db = new SecurityDB(3, 2);
        db.addPassenger("Rob Bekker", "Asb23f");
        db.addPassenger("Robert Bekker", "Asb23f");
        // Should print a warning to stderr
    }
}
