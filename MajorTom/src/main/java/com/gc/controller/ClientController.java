package com.gc.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gc.dto.AuthenticationDTO;
import com.gc.dto.ReassignSeatDTO;
import com.gc.dto.SetSeatDTO;
import com.gc.model.Employee;
import com.gc.model.Flight;
import com.gc.model.Seat;
import com.gc.model.Ticket;
import com.gc.service.DataService;

@RestController
public class ClientController {

	@Autowired
	DataService dataService;
	
	Employee emp;
	
	@RequestMapping("/findFlight/{flightId}")
	public ResponseEntity<Flight> findFlightById(@PathVariable(value = "flightId") Integer flightId) {
		System.out.println("flightId: " + flightId);
		Flight newFlightInfo = dataService.findFlightById(flightId);
		System.out.println(newFlightInfo);
		if (newFlightInfo != null) {
			return new ResponseEntity<Flight>(newFlightInfo, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<Flight>(newFlightInfo, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping("/findTicket/{ticketId}")
	public ResponseEntity<Ticket> findTicket(@PathVariable(value = "ticketId") Integer ticketId){
		Ticket tick=dataService.findTicketById(ticketId);
		return new ResponseEntity<Ticket>(tick,tick==null?HttpStatus.NOT_FOUND:HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/findTicketBySeat/{seatId}")
	public ResponseEntity<Ticket> findTicketBySeat(@PathVariable(value = "seatId") Integer seatId) {
		Ticket ticket = dataService.findSeatById(seatId).getTicket();
		if (ticket != null) {
			System.out.println("Ticket: " + ticket);
			return new ResponseEntity<Ticket>(ticket, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<Ticket>(ticket, HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/findSeatsByFlight/{flightId}")
	public ResponseEntity<List<Seat>> findSeatsByFlight(@PathVariable(value = "flightId") Integer flightId) {
		List<Seat> seats = dataService.findSeatsByFlight(dataService.findFlightById(flightId));
		if (seats != null) {
			for (Seat s : seats) {
				System.out.println("Seat: " + s);
			}
			return new ResponseEntity<List<Seat>>(seats, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<List<Seat>>(seats, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/authenticate")
	public ResponseEntity<Employee> authenticate(Model model, @RequestBody AuthenticationDTO data) {
		System.out.println(data);
		emp = dataService.findEmployeeByUsernameAndPassword(data.getUsername(), data.getPassword());
		System.out.println(emp);
		if(emp != null) {
			int token = (int) (Math.random()*100000);
			model.addAttribute("loginToken", token);
			emp.setToken(token);
			return new ResponseEntity<Employee>(emp, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<Employee>(emp, HttpStatus.FORBIDDEN);
		}
	}
	
	@RequestMapping(value="/setSeat")
	public ResponseEntity<Seat> setSeat(@RequestBody SetSeatDTO data) {
		System.out.println(data);
		Ticket ticket = dataService.findTicketById(data.getTicketId());
		Seat seat = dataService.findSeatById(data.getSeatId());
		if (ticket != null && seat != null) {
			seat.setTicket(ticket);
			dataService.saveSeat(seat);
			System.out.println(seat);
			return new ResponseEntity<Seat>(seat, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<Seat>(seat, HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/reassignSeat")
	public ResponseEntity<Seat> reassignSeat(Model model, @RequestBody ReassignSeatDTO data) {
		System.out.println("Token there?: "+emp.getToken());
		System.out.println("Reassign Seat with data: "+data);
		Ticket ticket = dataService.findTicketById(data.getTicketId());
		Seat seat = dataService.findSeatById(data.getSeatId());
		Seat seat2 = dataService.findSeatById(data.getSeat2Id());
		if (ticket == null && data.getLoginToken() == emp.getToken()){
			System.out.println("Nice, you're an employee!");
			if(seat.getTicket() != null){
				if(seat2.getTicket() != null){	
					Ticket tempTicket = seat.getTicket();
					seat.setTicket(seat2.getTicket());
					seat2.setTicket(tempTicket);
					dataService.saveSeat(seat);
					dataService.saveSeat(seat2);
					ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
					return resp;
				} else {
					System.out.println("Seat2 is null");
					seat2.setTicket(seat.getTicket());
					seat.setTicket(null);
					dataService.saveSeat(seat);
					dataService.saveSeat(seat2);
					ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
					return resp;
				}
			} else {
				if(seat2.getTicket() != null){
					System.out.println("Seat1 is null");
					seat.setTicket(seat2.getTicket());
					seat2.setTicket(null);
					dataService.saveSeat(seat);
					dataService.saveSeat(seat2);
					ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
					return resp;
				}
			}
			ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
			return resp;
		} else if (ticket != null && seat != null) {
			if(seat.getTicket().getTicketId() == ticket.getTicketId()){
				System.out.println("You currently hold the seat!");
				ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
				return resp;
			} else if(seat.getTicket() == null){
				seat.setTicket(ticket);
				ResponseEntity<Seat>resp = reassignSeatAndEmail(model, seat, seat2, ticket);
				return resp;
			} else {
				System.out.println("This is ludacris, you can't do this!");
				return new ResponseEntity<Seat>(seat, HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<Seat>(seat, HttpStatus.BAD_REQUEST);
		}
	}
	
	public ResponseEntity<Seat> reassignSeatAndEmail(Model model, Seat seat, Seat seat2, Ticket ticket) {
		System.out.println(seat);
		System.out.println(seat2);
		return new ResponseEntity<Seat>(seat, HttpStatus.ACCEPTED);
	}
}
