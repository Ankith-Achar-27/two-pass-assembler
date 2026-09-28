public class Literal {

    private String literal;
    private String value;
    private int length;
    private String address;

    public Literal(String literal, String value, int length) {
        this.literal = literal;
        this.value = value;
        this.length = length;
        this.address = null;
    }

    public String getLiteral() {
        return literal;
    }

    public String getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return literal + " -> " + address;
    }
}