package uk.gov.dwp.uc.pairtest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;
    
    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }
    
    @Test
    public void testpurchaseTicketsShouldMakePaymentAndReserveSeats() throws InvalidPurchaseException {
    	Long accountId = 100L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.CHILD, 2),
                new TicketTypeRequest(Type.INFANT, 1)
        };
        ticketService.purchaseTickets(accountId, ticketTypeRequests); //passed ticketTypeRequests as array

        verify(paymentService).makePayment(accountId, 70);
        verify(seatReservationService).reserveSeat(accountId, 5);
    }
    
    @Test
    public void testPurchaseTicketsValidRequestPaymentServiceOnly() throws InvalidPurchaseException {
        TicketTypeRequest request1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(Type.CHILD, 1);
        
        TicketTypeRequest[] requests = {request1, request2};
        Long accountId = 100L;
        
        ticketService.purchaseTickets(accountId, requests);
        verify(paymentService).makePayment(accountId, 50); 
    }
    
    @Test
    public void testPurchaseTicketsValidRequestseatReservationService() throws InvalidPurchaseException {
        TicketTypeRequest request1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest[] requests = {request1, request2};
        Long accountId = 100L;
        
        ticketService.purchaseTickets(accountId, requests);
        
        verify(seatReservationService).reserveSeat(accountId, 3); 
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithNullAccountId() throws InvalidPurchaseException {
        TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);
        ticketService.purchaseTickets(null, request);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithZeroAccountId() throws InvalidPurchaseException {
        TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);
        ticketService.purchaseTickets(0L, request);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithInvalidTicketTypeRequest() throws InvalidPurchaseException {
        TicketTypeRequest request = new TicketTypeRequest(null, 1);
        ticketService.purchaseTickets(100L, request);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithZeroTicketQuantity() throws InvalidPurchaseException {
        TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 0);
        ticketService.purchaseTickets(100L, request);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithExceedingMaxTicketQuantity() throws InvalidPurchaseException {
        TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 21);
        ticketService.purchaseTickets(1L, request);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsWithoutPurchasingAdultTicket() throws InvalidPurchaseException {
        TicketTypeRequest request1 = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest request2 = new TicketTypeRequest(Type.INFANT, 1);
        ticketService.purchaseTickets(100L, request1, request2); //passed individual ticketTypeRequests as array
    }
    
}
