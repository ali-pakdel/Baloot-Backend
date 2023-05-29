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
        providerRepository.createTable(createTableStatement);
        commodityRepository.createTable(createTableStatement);
        commodityRepository.createWeakTable(createTableStatement);
        userRepository.createTable(createTableStatement);
//        userRepository.createWeakTable(createTableStatement, "BuyList");
//        userRepository.createWeakTable(createTableStatement, "PurchasedList");
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
                username,
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
    public static Commodity findByCommodityId(int commodityId) throws CommodityNotFoundException, SQLException {
        List<HashMap<String, String>> commodityRow = commodityRepository.select(
                Integer.toString(commodityId),
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
                String.valueOf(providerId),
                providerRepository.getColNames(),
                providerRepository.selectOneStatement()
        );
        // TODO: Maybe Error in throwing
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
                code,
                discountRepository.getColNames(),
                discountRepository.selectOneStatement()
        );
        if (discount.isEmpty()) {
            throw new DiscountCodeNotFoundException("Discount code " + code + " is not exist");
        }
        return Double.parseDouble(discount.get(0).get("value"));
    }

    public static void increaseUserCredit(String username, double amount) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement prepStat = con.prepareStatement(userRepository.increaseCreditStatement());
        try {
            prepStat.setDouble(1, amount);
            prepStat.setString(2, username);
            prepStat.execute();
            prepStat.close();
            con.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            prepStat.close();
            con.close();
        }
    }
}
