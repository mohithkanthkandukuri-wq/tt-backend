package com.example.lostfound;

import com.example.lostfound.dto.request.ClaimReviewRequest;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.entity.*;
import com.example.lostfound.exception.BadRequestException;
import com.example.lostfound.exception.ForbiddenException;
import com.example.lostfound.repository.ClaimRepository;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.service.NotificationService;
import com.example.lostfound.service.UserService;
import com.example.lostfound.service.impl.ClaimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private User reporter;
    private User claimant;
    private Item item;
    private Claim claim;

    @BeforeEach
    void setUp() {
        reporter = new User("Reporter User", "reporter@example.com", "hash", "111", Role.USER);
        reporter.setId(1L);

        claimant = new User("Claimant User", "claimant@example.com", "hash", "222", Role.USER);
        claimant.setId(2L);

        item = new Item("Lost Keys", "Set of brass keys", ItemCategory.KEYS, ReportType.FOUND, "Library", LocalDate.now(), reporter);
        item.setId(10L);
        item.setStatus(ItemStatus.CLAIM_PENDING);

        claim = new Claim(item, claimant, "Brass keys with blue lanyard");
        claim.setId(100L);
    }

    @Test
    void approveClaim_ShouldThrowException_WhenUserNotAuthorized() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(claim));

        // User ID 99 is neither reporter (1) nor admin
        assertThrows(ForbiddenException.class, () -> 
            claimService.approveClaim(100L, new ClaimReviewRequest("Approved"), 99L, false)
        );
    }

    @Test
    void approveClaim_ShouldApproveAndChangeItemStatusToClaimed_WhenReporterApproves() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(claim));
        when(userService.getUserById(1L)).thenReturn(reporter);
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findByItemId(10L)).thenReturn(Collections.singletonList(claim));

        ClaimResponse response = claimService.approveClaim(100L, new ClaimReviewRequest("Verified ownership"), 1L, false);

        assertNotNull(response);
        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals(ItemStatus.CLAIMED, item.getStatus());
        verify(itemRepository, times(1)).save(item);
        verify(notificationService, atLeastOnce()).createNotification(eq(claimant), anyString(), anyString());
    }
}
