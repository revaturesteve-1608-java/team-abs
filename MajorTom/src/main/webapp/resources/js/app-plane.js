/**
 * 
 */
var app = angular.module("airline");

app.controller("planeController", function($scope, $rootScope, $location, planeDataService) {
	var me = this;
	
	this.getMessages = function() {
		
		dataService.get(function(response) {
			me.messages = response.data;
		});
	};

	this.pilot = {
		name: "Major Tom"
	};
	this.copilot = {
		name: "Major Todd"
	};
	
	this.selectionDisplay = "No seat selected";
	this.selectedSeat;
	
	var flightId = 1402;
	planeDataService.getFormattedSeats(flightId, function(response){
		me.firstclass = response.data.first;
		me.buisclass = response.data.buisness;
		me.econclass = response.data.economy;
	});
	
	this.selectSeat = function(seat){
		this.selectionDisplay = "Selected seat: " + seat.seatId;
		this.selectedSeat = seat;
		// Trigger an event in the info controller (info.js)
        $rootScope.$emit('seatClick', this.selectedSeat);
	};
	

	$rootScope.$on('changeFlight', function(event, data) {
		// This event is triggered when the admin changes the flight (and thus, wants the airplane to change)
		planeDataService.getFormattedSeats(data, function(response) {
			me.firstclass = response.data.first;
			me.buisclass = response.data.buisness;
			me.econclass = response.data.economy;
		})
	});
	
	$rootScope.$on('removeSelected', function() {
		// This event is triggered when we want to null the selected seat
		this.selectedSeat = null;
	});
	
	
});


app.service("planeDataService", function($http){
	this.getFormattedSeats = function(flightId, callback) {
		$http.get("rest/getFormattedSeats/"+flightId, flightId).then(callback);
	}
});



