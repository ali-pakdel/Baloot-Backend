package org.Baloot.Managers;

import org.Baloot.Database.Database;
import org.Baloot.Entities.Commodity;
import org.Baloot.Entities.User;
import org.Baloot.Exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserManager {
    private Database db;
    public UserManager(Database _db){
        db = _db;
    }

    public void addUser(User user) throws InvalidUsernameException {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]+");
        Matcher matcher = pattern.matcher(user.getUsername());
        try {
            if (matcher.matches()) {
                User foundUser = db.findByUsername(user.getUsername());
                foundUser.modifyFields(user);
            } else {
                throw new InvalidUsernameException("Invalid Username!");
            }
        }
        catch (UserNotFoundException e){
            db.insertUser(user);
        }
    }

    public List<Commodity> getUserBuylist(String username) throws UserNotFoundException, CommodityNotFoundException {
        User user = db.findByUsername(username);
        Set<Integer> buyListIds = user.getBuyList();
        List<Commodity> commodities = new ArrayList<>();
        for (int commodityId : buyListIds) {
            Commodity commodity = db.findByCommodityId(commodityId);
            commodities.add(commodity);
        }
        return commodities;
    }

    public void addCredit(String username, String credit) throws UserNotFoundException, NegativeAmountException {
        double amount = Double.parseDouble(credit);

        User user = db.findByUsername(username);
        if (amount <= 0) {
            throw new NegativeAmountException();
        }
        user.increaseCredit(amount);
    }
    public void finalizePayment(String username) throws UserNotFoundException, NotEnoughCreditException, CommodityNotFoundException {
        User user = db.findByUsername(username);
        Set<Integer> commoditiesId = user.getBuyList();
        double cost = 0;

        for (Integer id: commoditiesId) {
            Commodity commodity = db.findByCommodityId(id);
            cost += commodity.getPrice();
        }
        user.moveBuyToPurchased(cost);
    }
    public void addCommodityToUserBuyList(String userId, String commodityId) throws CommodityNotFoundException, OutOfStockException, UserNotFoundException, CommodityExistenceException {
        Commodity commodityFound = db.findByCommodityId(Integer.parseInt(commodityId));
        if (commodityFound.getInStock() == 0) {
            throw new OutOfStockException("Commodity out of stock!");
        }
        User userFound = db.findByUsername(userId);
        userFound.addToBuyList(Integer.parseInt(commodityId));
        commodityFound.decreaseInStock();
    }

    public void removeCommodityFromUserBuyList(String userId, String commodityId) throws CommodityNotFoundException, UserNotFoundException, CommodityExistenceException {
        Commodity commodityFound = db.findByCommodityId(Integer.parseInt(commodityId));
        User userFound = db.findByUsername(userId);
        userFound.removeFromBuyList(Integer.parseInt(commodityId));
        commodityFound.increaseInStock();
    }
}