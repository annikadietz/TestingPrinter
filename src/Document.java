public class Document {
    private int amountPages;
    private String text;
    private String name;

    public Document(int pAmountPages, String pText, String pName) {
        amountPages = pAmountPages;
        text = pText;
        name = pName;

    }

    public int getAmountPages() {
        return amountPages;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }
}
