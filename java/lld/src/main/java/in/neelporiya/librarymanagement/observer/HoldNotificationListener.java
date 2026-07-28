package in.neelporiya.librarymanagement.observer;

import in.neelporiya.librarymanagement.model.BookItem;
import in.neelporiya.librarymanagement.model.Hold;

/** // DESIGN PATTERN: Observer — notification delivery is decoupled from circulation logic. */
public interface HoldNotificationListener {
    void onHoldAvailable(Hold hold, BookItem item);
}
