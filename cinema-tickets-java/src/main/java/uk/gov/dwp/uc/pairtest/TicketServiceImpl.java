package uk.gov.dwp.uc.pairtest;

import java.util.Objects;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
	 /**
     * Should only have private methods other than the one below.
     */
     
    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;

    
    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
		this.paymentService = paymentService;
		this.seatReservationService=seatReservationService;
	}

	@Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        try {
			validatePurchaseRequest(accountId, ticketTypeRequests);
	        int totalPayment = getCalculatedPayment(ticketTypeRequests);
	        paymentService.makePayment(accountId, totalPayment);
	        
	        int totalAllocatedSeats = calulateAllocatedSeats(ticketTypeRequests);
	        seatReservationService.reserveSeat(accountId, totalAllocatedSeats);
	        
        }catch(NullPointerException e) {
        	throw new InvalidPurchaseException(e.getMessage());
        }
    }
	
    /**
     *  Validate validate PurchaseRequest
     *  @param accountId
     *  @param ticketTypeRequests
     **/
    private void validatePurchaseRequest(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
    	boolean adultTicketPurchased = false;
    	
        if (accountId <= 0) {
            throw new InvalidPurchaseException("Invalid Account ID. It must be greater than 0");
        }

        Objects.requireNonNull(ticketTypeRequests, "Ticket Type Requests cannot be null");
        if (ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Invalid TicketType request");
        }

        for (TicketTypeRequest request : ticketTypeRequests) {
            int noOfTickets = request.getNoOfTickets();
            if (noOfTickets < 1) {
                throw new InvalidPurchaseException("Invalid quantity: " + noOfTickets);
            }
            if (noOfTickets > 20) {
                throw new InvalidPurchaseException("Maximum quantity exceeded: " + noOfTickets);
            }

            Type ticketType = request.getTicketType();
            
            validateTicketType(ticketType);
            
            if (ticketType == Type.ADULT) {
                adultTicketPurchased = true;
            } else {
                // Check if Adult ticket is purchased before Child or Infant ticket
                if (!adultTicketPurchased) {
                    throw new InvalidPurchaseException("Child or Infant tickets cannot be purchased without purchasing an Adult ticket.");
                }
            }
        }
        
        if (!adultTicketPurchased) {
            throw new InvalidPurchaseException("At least one Adult ticket is required.");
        }
    }
    
    /**
     *  Validate ticket type
     *  @param ticketType
     **/
    private void validateTicketType(Type ticketType) {
        Objects.requireNonNull(ticketType, "Ticket type cannot be null");
        
        if (ticketType != Type.INFANT && ticketType != Type.CHILD && ticketType != Type.ADULT) {
            throw new IllegalArgumentException("Invalid ticket type: " + ticketType);
        }
    }

    /**
     *  Calculate total payment for requested tickets
     *  @param ticketTypeRequests
     *  @return totalPayment
     **/
    private int getCalculatedPayment(TicketTypeRequest... ticketTypeRequests) {
        int totalPayment = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            int ticketQuantity = request.getNoOfTickets();
            Type ticketType = request.getTicketType();

            int payment = calculateTotalTicketPrice(ticketType, ticketQuantity);
            totalPayment += payment;
        }
        return totalPayment;
    }

    /**
     *  Calculate total price for requested tickets
     *  @param ticketType
     *  @param ticketQuantity
     *  @return totalPrice
     **/
    private int calculateTotalTicketPrice(Type ticketType, int ticketQuantity) {
        int price = ticketType.getPrice();
        return price * ticketQuantity;
    }
    
    /**
     *  Calculate seats
     *  @param ticketTypeRequests
     **/
	private int calulateAllocatedSeats(TicketTypeRequest... ticketTypeRequests) {
		int totalAllocatedSeats =0;
		for (TicketTypeRequest request : ticketTypeRequests) {
            Type ticketType = request.getTicketType();
            if (ticketType != Type.INFANT) {
            	totalAllocatedSeats += request.getNoOfTickets();
            }
        }
		return totalAllocatedSeats;
	}
}
