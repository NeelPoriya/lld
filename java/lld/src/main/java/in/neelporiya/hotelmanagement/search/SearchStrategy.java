package in.neelporiya.hotelmanagement.search;

import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.RoomType;
import in.neelporiya.hotelmanagement.model.StayRange;
import in.neelporiya.hotelmanagement.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;

public interface SearchStrategy {

    List<Room> search(List<Room> rooms, ReservationRepository reservations, StayRange range, Optional<RoomType> type);
}
