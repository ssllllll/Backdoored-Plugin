package me.ego.ezbd.lib.fo.menu.model;

public enum MenuQuantity {
    ONE(1),
    TWO(2),
    FIVE(5),
    TEN(10),
    TWENTY(20);

    private final int amount;

    public final MenuQuantity previous() {
        int next = this.ordinal() - 1;
        MenuQuantity[] values = values();
        return next >= 0 ? values[next] : values[values.length - 1];
    }

    public final MenuQuantity next() {
        int next = this.ordinal() + 1;
        MenuQuantity[] values = values();
        return next >= values.length ? values[0] : values[next];
    }

    private MenuQuantity(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }
}