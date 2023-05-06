package org.Baloot.Application;

import org.Baloot.Baloot;
import org.Baloot.Database.Database;
import org.Baloot.Entities.Comment;
import org.Baloot.Entities.Commodity;
import org.Baloot.Exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class HomeController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<?> getCommodities(@RequestParam(value = "search") String search, @RequestParam(value = "option") String option,
                                            @RequestParam(value = "available") Boolean available, @RequestParam(value = "sort") String sortBy,
                                            @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "username") String username) {

//        System.out.println(search);
//        System.out.println(option);
//        System.out.println(available);
//        System.out.println(page);
//        System.out.println(sortBy);
//        System.out.println("\n");

        List<Commodity> commodities = Baloot.getBaloot().getCommodities();
        if(option.equals("category")) {
            commodities = Baloot.getBaloot().commodityManager.getCommoditiesByCategory(search);
            System.out.println(commodities.size());
        }
        else if (option.equals("name")) {
            commodities = Baloot.getBaloot().commodityManager.getCommoditiesByName(search);
        } else if (option.equals("provider")) {
            try {
                commodities = Baloot.getBaloot().commodityManager.getCommoditiesByProvider(search);
            }catch (ProviderNotFoundException e){

            }
        }
        if(available){
            commodities = Baloot.getBaloot().commodityManager.getAvailableCommodities(commodities);
        }
        if (Objects.equals(sortBy, "name")) {
            Baloot.getBaloot().commodityManager.getSortedCommoditiesByName(commodities);
        } else if (Objects.equals(sortBy, "price")) {
            Baloot.getBaloot().commodityManager.getSortedCommoditiesByPrice(commodities);
        }
        page = page -1;
        int size = 12;
        int totalItems = commodities.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        if (page < 0 || page >= totalPages) {
            return ResponseEntity.badRequest().build();
        }
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);

        commodities = commodities.subList(startIndex, endIndex);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("commodities", commodities);
        responseMap.put("totalPages", totalPages);

        try {
            int cartCounter = Baloot.getBaloot().userManager.getUserBuyList(username).keySet().size();
            responseMap.put("cartCount", cartCounter);
            HashMap<Integer, Integer> userBuylist = Baloot.getBaloot().getUserByUsername(username).getBuyList();
            responseMap.put("buylist", userBuylist);
            System.out.println(userBuylist.size());
            return ResponseEntity.ok(responseMap);
        }
        catch (UserNotFoundException | CommodityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @RequestMapping(value = "/commodity/{id}/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> getCommodity(@PathVariable (value = "id") String id,
                                          @PathVariable (value = "username") String username) {
        try {
            Commodity commodity = Database.findByCommodityId(Integer.parseInt(id));
            List<Comment> comments = Baloot.getBaloot().commodityManager.getCommentsOfCommodity(Integer.parseInt(id));
            String providerName = Baloot.getBaloot().getProviderById(commodity.getProviderId()).getName();
            List<Commodity> suggestedCommodities = Baloot.getBaloot().commodityManager.getSuggestedCommodities(commodity, commodity.getCategories());
            HashMap<String, Object> response = new HashMap<>();
            response.put("info", commodity);
            response.put("comments", comments);
            response.put("providerName", providerName);
            response.put("suggested", suggestedCommodities);
            HashMap<Integer, Integer> userBuyList = Baloot.getBaloot().getUserByUsername(username).getBuyList();
            response.put("cartCount", userBuyList.keySet().size());
            response.put("buyList", userBuyList);
            return ResponseEntity.ok(response);
        } catch (CommodityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(value = "/rate/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> rateCommodity(@PathVariable(value = "id") String id,
                                         @RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String rate = body.get("rate");
            Baloot.getBaloot().commodityManager.rateCommodity(username, id, rate);
            Commodity commodity = Baloot.getBaloot().getCommodityById(Integer.parseInt(id));
            double rating = commodity.getRating();
            int count = commodity.getCountOfRatings();
            HashMap<String, String> response = new HashMap<>();
            response.put("rating", Double.toString(rating));
            response.put("count", Integer.toString(count));
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException| CommodityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidRatingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(value = "/comment/post/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postComment(@PathVariable(value = "id") String id,
                                         @RequestBody Map<String, String> body) {

        String username = body.get("username");
        String text = body.get("text");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currDate = LocalDate.now().format(formatter);
        Comment comment = new Comment(username, Integer.parseInt(id), text, currDate);
        Database.insertComment(comment);
        return ResponseEntity.ok(comment);
    }

    @RequestMapping(value = "/comment/vote/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> vote(@PathVariable (value = "id") String id,
                                           @RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String commentId = body.get("commentId");
            String vote = body.get("vote");
            int status = Baloot.getBaloot().commentManager.voteComment(username, id, commentId, vote);
            return ResponseEntity.ok(status);
        } catch (UserNotFoundException | CommodityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidVoteException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}