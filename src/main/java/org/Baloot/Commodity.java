package org.Baloot;

public class Commodity {
    int id;
    String name;

    String providerId;
    double price;
    String categories;
    double rating;
    int inStock;

    public Commodity(int _id, String _name, String _providerId, double _price,
                     String _categories, double _rating, int _inStock) {
        id = _id;
        name = _name;
        providerId = _providerId;
        price = _price;
        categories = _categories;
        rating = _rating;
        inStock = _inStock;
    }
}
