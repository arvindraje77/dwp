package uk.gov.dwp.uc.pairtest.domain;

public enum Type {
	INFANT(0),
    CHILD(10),
    ADULT(20);

    private final int price;

    Type(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    } 
}
