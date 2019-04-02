public class Setting {
    private int amount;
    private boolean coloured;
    private boolean doubleSided;
    private boolean stapled;
    private String size;

    public Setting() {
        amount = 1;
        coloured = false;
        doubleSided = false;
        stapled = false;
        size = "A4";
    }

    public Setting(int pAmount, boolean pColoured, boolean pDoubleSided, boolean pStapled, String pSize) {
        amount = pAmount;
        coloured = pColoured;
        doubleSided = pDoubleSided;
        stapled = pStapled;
        size = pSize;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isColoured() {
        return coloured;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

    public boolean isStapled() {
        return stapled;
    }

    public String getSize() {
        return size;
    }
}
