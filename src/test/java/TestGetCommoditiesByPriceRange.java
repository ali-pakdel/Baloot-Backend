//import org.Baloot.Baloot;
//import org.Baloot.Entities.Commodity;
//import org.Baloot.Database.Database;
//import org.Baloot.Entities.Provider;
//import org.Baloot.Exception.*;
//import org.junit.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//public class TestGetCommoditiesByPriceRange {
//    private Baloot baloot;
//    private Commodity commodity1, commodity2, commodity3;
//    @Before
//    public void setUp() throws ProviderNotFoundException {
//        baloot = Baloot.getBaloot();
//        Provider provider = new Provider(1, "A", "2023-09-15");
//        Database.insertProvider(provider);
//
//        List<String> categories1 = new ArrayList<>();
//        categories1.add("Technology");
//        commodity1 = new Commodity(1, "Headphone", 1, 35000, categories1, 0.0,
//                50);
//        Database.insertCommodity(commodity1);
//
//        List<String> categories2 = new ArrayList<>();
//        categories2.add("Technology");
//        categories2.add("Phone");
//        commodity2 = new Commodity(2, "Apple", 1, 3000, categories2, 0.0,
//                0);
//        Database.insertCommodity(commodity2);
//
//        List<String> categories3 = new ArrayList<>();
//        categories3.add("Fruit");
//        commodity3 = new Commodity(3, "Apple", 1, 300, categories3, 0.0,
//                220);
//        Database.insertCommodity(commodity3);
//
//    }
//
//    @After
//    public void tearDown() {
//        baloot = null;
//        commodity1 = null;
//        commodity2 = null;
//        commodity3 = null;
//    }
//    @Test
//    public void testGetCommoditiesIfPriceRangeIsBigEnoughForAllCommodities() {
//        String startPrice = "1";
//        String endPrice = "10000000";
//        List<Commodity> actualCommodities = baloot.commodityManager.getCommoditiesByPriceRange(startPrice, endPrice);
//        List<Commodity> expectedCommodities = Database.getCommodities();
//        assertEquals(expectedCommodities, actualCommodities);
//    }
//
//    @Test
//    public void testGetCommoditiesIfPriceRangeIsAsBigAsOneCommodity() {
//        String startPrice = "1";
//        String endPrice = "305";
//        List<Commodity> actualCommodities = baloot.commodityManager.getCommoditiesByPriceRange(startPrice, endPrice);
//        List<Commodity> expectedCommodities = new ArrayList<>();
//        expectedCommodities.add(commodity3);
//        assertEquals(expectedCommodities, actualCommodities);
//    }
//    @Test
//    public void testGetCommoditiesIfStartRangeIsBiggerThanEndRange() {
//        String startPrice = "1000";
//        String endPrice = "305";
//        List<Commodity> actualCommodities = baloot.commodityManager.getCommoditiesByPriceRange(startPrice, endPrice);
//        List<Commodity> expectedCommodities = new ArrayList<>();
//        assertEquals(expectedCommodities, actualCommodities);
//    }
//    @Test
//    public void testGetCommoditiesIfPriceRangeDoesntContainAnyCommodity() {
//        String startPrice = "1";
//        String endPrice = "2";
//        List<Commodity> actualCommodities = baloot.commodityManager.getCommoditiesByPriceRange(startPrice, endPrice);
//        List<Commodity> expectedCommodities = new ArrayList<>();
//        assertEquals(expectedCommodities, actualCommodities);
//    }
//    @Test
//    public void testErrorInGetCommoditiesWhenPriceRangeIsNotNumerical() {
//        String startPrice = "a";
//        String endPrice = "305";
//
//        assertThrows(NumberFormatException.class, () -> baloot.commodityManager.getCommoditiesByPriceRange(startPrice, endPrice));
//    }
//}
