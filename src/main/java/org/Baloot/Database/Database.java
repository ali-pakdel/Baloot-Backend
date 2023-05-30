package org.Baloot.Database;

import org.Baloot.Baloot;
import org.Baloot.Entities.*;
import org.Baloot.Exception.CommodityNotFoundException;
import org.Baloot.Exception.DiscountCodeNotFoundException;
import org.Baloot.Exception.ProviderNotFoundException;
import org.Baloot.Exception.UserNotFoundException;
import org.Baloot.Repository.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    private static List<User> users = new ArrayList<>();
    private static List<Provider> providers = new ArrayList<>();
    private static List<Commodity> commodities = new ArrayList<>();
    private static List<Comment> comments = new ArrayList<>();
//    static HashMap<String, Double> discountCodes = new HashMap<>();

    public static List<User> getUsers() { return users; }
    public static List<Provider> getProviders() { return providers; }
    public static List<Commodity> getCommodities() { return commodities; }
    public static List<Comment> getComments() { return comments; }

    static DiscountRepository<DiscountCode> discountRepository = new DiscountRepository<>();
    static ProviderRepository<Provider> providerRepository = new ProviderRepository<>();
    static UserRepository<User> userRepository = new UserRepository<>();
    static CommodityRepository<Commodity> commodityRepository = new CommodityRepository<>();
    static CommentRepository<Comment> commentRepository = new CommentRepository<>();

    public static void insertInitialData(User[] _users, Provider[] _providers, Commodity[] _commodities,
                                         Comment[] _comments, DiscountCode[] _discountCodes) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        Statement createTableStatement = con.createStatement();
        discountRepository.createTable(createTableStatement);
        discountRepository.createWeakTable(createTableStatement);

        providerRepository.createTable(createTableStatement);

        commodityRepository.createTable(createTableStatement);
        commodityRepository.createWeakTable(createTableStatement);
        commodityRepository.createRatingTable(createTableStatement);

        userRepository.createTable(createTableStatement);
        userRepository.createWeakTable(createTableStatement, "BuyList");
        userRepository.createPurchasedListTable(createTableStatement);
//        commentRepository.createTable(createTableStatement);

        System.out.println(_users.length);
        createTableStatement.executeBatch();
        createTableStatement.close();
        con.close();

        for (DiscountCode discountCode: _discountCodes) {
            discountRepository.insert(discountRepository.insertStatement(discountCode.getAttributes()));
        }

        for (Provider provider: _providers){
            insertProvider(provider);
        }

        for (Commodity commodity: _commodities){
            insertCommodity(commodity);
        }

        for (User user: _users){
            insertUser(user);
        }
//        for (Comment comment: _comments){
//            commentRepository.insert(comment);
//        }
//        for (Commodity commodity : commodities){
//            try {
//                addToProviderCommodityList(commodity);
//            }catch (ProviderNotFoundException e){
//
//            }
//        }
    }
    public static void insertToBuyList(String username, String commodityId) throws SQLException {
        HashMap<String, String> values = new HashMap<>();
        values.put("cid", commodityId);
        values.put("username", username);
        userRepository.insert(userRepository.insertBuyListStatement(values));
    }
    public static void removeFromBuyList(String username, String commodityId) throws SQLException {
        HashMap<String, String> values = new HashMap<>();
        values.put("cid", commodityId);
        values.put("username", username);
        userRepository.insert(userRepository.removeFromBuyListStatement(values));
    }
    public static void insertToPurchasedList(String username, int commodityId, int quantity, Timestamp timestamp) throws SQLException {
        HashMap<String, String> values = new HashMap<>();
        values.put("cid", String.valueOf(commodityId));
        values.put("username", username);
        values.put("quantity", String.valueOf(quantity));
        values.put("buyTime", timestamp.toString());
        userRepository.insert(userRepository.insertPurchasedListStatement(values));
    }
    public static void insertUser(User user) throws SQLException {
        users.add(user);
        userRepository.insert(userRepository.insertStatement(user.getAttributes()));
    }

    public static void insertProvider(Provider provider) throws SQLException {
        providers.add(provider);
        providerRepository.insert(providerRepository.insertStatement(provider.getAttributes()));
    }

    public static void insertCategories(Commodity commodity) throws SQLException {
        for(String category: commodity.getCategories()){
            HashMap<String, String> values = new HashMap<>();
            values.put("cid", String.valueOf(commodity.getId()));
            values.put("name", category);
            commodityRepository.insert(commodityRepository.insertCategory(values));
        }
    }
    public static void insertCommodity(Commodity commodity) throws SQLException {
        commodities.add(commodity);
        commodityRepository.insert(commodityRepository.insertStatement(commodity.getAttributes()));
        insertCategories(commodity);
//        addToProviderCommodityList(commodity);
    }

    public static void insertComment(Comment comment) { comments.add(comment); }

    public static void addToProviderCommodityList(Commodity commodity) throws ProviderNotFoundException {
        for(Provider provider : providers){
            if(provider.getId() == commodity.getProviderId()){
                provider.addToCommodities(commodity);
                return;
            }
        }
        throw new ProviderNotFoundException("Couldn't find provider with the given Id!");
    }
    public static User findByUsername(String username) throws UserNotFoundException, SQLException {
        List<HashMap<String, String>> userRow = userRepository.select(
                new ArrayList<Object>() {{ add(username); }},
                userRepository.getColNames(),
                userRepository.selectOneStatement()
        );
        if (userRow.isEmpty()) {
            throw new UserNotFoundException("User not found!");
        }

        return new User(userRow.get(0).get("username"),
                userRow.get(0).get("password"),
                userRow.get(0).get("email"),
                userRow.get(0).get("birth_date"), userRow.get(0).get("address"),
                Double.parseDouble(userRow.get(0).get("credit")));
    }
    public static HashMap<Commodity, Integer> castHashMap(List<HashMap<String, String>> originalMap) throws SQLException {
        HashMap<Commodity, Integer> castedMap = new HashMap<>();
        for(HashMap<String, String> hashMap: originalMap) {
            Commodity commodity = new Commodity(Integer.parseInt(hashMap.get("cid")),
                    hashMap.get("name"),
                    Integer.parseInt(hashMap.get("pid")),
                    Double.parseDouble(hashMap.get("price")),
                    commodityRepository.getCategories(Integer.parseInt(hashMap.get("cid"))),
                    Double.parseDouble(hashMap.get("rating")),
                    Integer.parseInt(hashMap.get("in_stock")),
                    hashMap.get("image"));
            castedMap.put(commodity,Integer.parseInt(hashMap.get("quantity")));
        }

        return castedMap;
    }

//    public static HashMap<Commodity, Integer> joinWithCommodity(){
//
//    }
    public static HashMap<Commodity, Integer> getUserList(String username, String type) throws SQLException{
        List<String> colNames = new ArrayList<>();
        colNames.add("cid");
        colNames.add("name");
        colNames.add("pid");
        colNames.add("price");
        colNames.add("rating");
        colNames.add("in_stock");
        colNames.add("image");
        colNames.add("quantity");
        String selectStatement = userRepository.selectListStatement(type);
        List<HashMap<String, String>> listRow = userRepository.select(new ArrayList<Object>() {{ add(username); }},
                colNames,
                selectStatement);

        return castHashMap(listRow);
    }
    public static Commodity findByCommodityId(int commodityId) throws CommodityNotFoundException, SQLException {
        List<HashMap<String, String>> commodityRow = commodityRepository.select(
                new ArrayList<Object>() {{ add(commodityId); }},
                commodityRepository.getColNames(),
                commodityRepository.selectOneStatement()
        );
        if (commodityRow.isEmpty()) {
            throw new CommodityNotFoundException("Commodity not found!");
        }

        return new Commodity(Integer.parseInt(commodityRow.get(0).get("cid")),
                commodityRow.get(0).get("name"),
                Integer.parseInt(commodityRow.get(0).get("pid")),
                Double.parseDouble(commodityRow.get(0).get("price")),
                commodityRepository.getCategories(commodityId),
                Double.parseDouble(commodityRow.get(0).get("rating")),
                Integer.parseInt(commodityRow.get(0).get("in_stock")),
                commodityRow.get(0).get("image"));
    }
    public static Provider findByProviderId(int providerId) throws ProviderNotFoundException, SQLException {
        List<HashMap<String, String>> providerRow = providerRepository.select(
                new ArrayList<Object>() {{ add(providerId); }},
                providerRepository.getColNames(),
                providerRepository.selectOneStatement()
        );
        // TODO: Maybe Error in throwing(providerRow[0].isempty()
        if (providerRow.isEmpty()) {
            throw new ProviderNotFoundException("Provider not found!");
        }

        return new Provider(Integer.parseInt(providerRow.get(0).get("pid")),
                providerRow.get(0).get("name"),
                providerRow.get(0).get("date"),
                providerRow.get(0).get("image"));
    }

    public static double getDiscountFromCode(String code) throws DiscountCodeNotFoundException, SQLException {
        List<HashMap<String, String>> discount = discountRepository.select(
                new ArrayList<Object>() {{ add(code); }},
                discountRepository.getColNames(),
                discountRepository.selectOneStatement()
        );
        if (discount.isEmpty()) {
            throw new DiscountCodeNotFoundException("Discount code " + code + " is not exist");
        }
        return Double.parseDouble(discount.get(0).get("value")) / 100;
    }
    public static void updateUserCredit(String username, double amount) throws SQLException {
        String statement = userRepository.updateCreditStatement();
        List<Object> values = new ArrayList<Object>() {{add(amount); add(username);}};
        userRepository.update(statement, values);
    }

    public static boolean findDiscount(String username, String code) throws SQLException {
        List<HashMap<String, String>> rows = discountRepository.select(new ArrayList<Object>() {{ add(username); add(code);}},
                new ArrayList<String>() {{ add("uid"); add("code");}},
                discountRepository.findDiscountCodeStatement(username, code));
        return !rows.isEmpty();
    }

    public static void insertToUsedCode(String username, String code) throws SQLException {

        discountRepository.insert(discountRepository.insertUsedCodeStatement(
                new HashMap<String, String>() {{ put("uid", username); put("code", code);}}
        ));
    }

    public static void getPurchasedList(String username) {

    }

    public static void insertRating(String commodityId, String username, String score) throws SQLException {
        HashMap<String, String> values = new HashMap<>() {{
            put("cid", commodityId);
            put("username", username);
            put("rate", score);}};
        commodityRepository.insert(commodityRepository.insertRatingStatement(values));
    }

    public static List<String> getRatings(String commodityId) throws SQLException {
        List<Object> values = new ArrayList<Object>() {{add(commodityId);}};
        List<String> colNames = new ArrayList<String>() {{add("rate");}};
        List<HashMap<String, String>> ratings = commodityRepository.select(values, colNames, commodityRepository.selectRatingStatement());
        return ratings.stream()
                .flatMap(map -> map.values().stream())
                .collect(Collectors.toList());
    }

    public static void updateRating(String commodityId, double rating) throws SQLException {
        String statement = commodityRepository.updateRatingStatement();
        List<Object> values = new ArrayList<Object>() {{add(rating); add(commodityId);}};
        commodityRepository.update(statement, values);
    }

    public static void modifyInStock(String commodityId, int count) throws SQLException {
        String statement = commodityRepository.modifyInStockStatement();
        List<Object> values = new ArrayList<Object>() {{add(count); add(Integer.parseInt(commodityId));}};
        commodityRepository.update(statement, values);
    }
}
