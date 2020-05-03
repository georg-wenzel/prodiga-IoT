package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingService;

import java.util.*;

public class MostWorkingHours implements Badge {
    @Override
    public User calculateUser(Collection<BookingCategory> bookingCategories, BookingService bookingService) {
        Optional<BookingCategory> category = bookingCategories.stream().filter(x -> x.getName().equals("Pause / Vacation")).findFirst();

        if(!category.isPresent()) {
            return null;
        }


        Collection<Booking> bookings = bookingService.getBookingInRangeByCategoryForLastWeek(category.get());
        HashMap<User, Long> hashMap = new HashMap<>();
        for(Booking b : bookings){
            if(hashMap.containsKey(b.getDice().getUser())){
                hashMap.put(b.getDice().getUser(), hashMap.get(b.getDice().getUser()) + b.getActivityEndDate().getTime()-b.getActivityStartDate().getTime());
            }
            else{
                hashMap.put(b.getDice().getUser(), b.getActivityEndDate().getTime()-b.getActivityStartDate().getTime());
            }
        }

        User userToReturn = Collections.min(hashMap.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();

        return userToReturn;
    }


    @Override
    public String getName() {
        return "Most working hours";
    }

    @Override
    public String getExplanation() {
        return "Most hours present";
    }
}
