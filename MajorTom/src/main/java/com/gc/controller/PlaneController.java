package com.gc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gc.dto.FormattedSeatsDTO;
import com.gc.service.DataService;

/**
 * A separate REST controller for the plane.
 */
@RestController
public class PlaneController {
	
	@Autowired
	DataService dataService;
	
	/** 
	 * Pulls the list of seats by the given flight ID and returns it as a 
	 * rest response with a 200 HTTP status, unless it's null, 
	 * then returns a null and a not found status code
	 * @param flightId The flight id
	 * @return ResponseEntity containing FormattedSeats and an HTTP status (accepted, or not found if seats are empty)
	 */
	@RequestMapping(value="getFormattedSeats/{flightId}")
	public ResponseEntity<FormattedSeatsDTO> getFormattedFlightSeats(@PathVariable(value = "flightId") int flightId ) {
		// Find the flight by id, and find all the seats with that flight object
		FormattedSeatsDTO seats = new FormattedSeatsDTO(dataService.findSeatsByFlight(dataService.findFlightById(flightId)));
		return new ResponseEntity<>(seats, seats.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.ACCEPTED);
	}
}
