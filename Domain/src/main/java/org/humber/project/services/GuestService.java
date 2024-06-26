package org.humber.project.services;

import org.humber.project.domain.Guest;
import java.util.List;

public interface GuestService {
    Guest manageGuest(Guest guest);
    List<Guest> getGuestListByEventId(Long eventId);
    Guest updateGuestStatus(Long guestId, String status);
    List<Guest> getInvitationsByUserId(Long userId);
    List<Long> getAcceptedEventIdsByUserId(Long userId);

}
