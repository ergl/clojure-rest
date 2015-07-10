var ViewEventHandler = (function() {
	"use strict";

	var viewEvent = function(id) {
		$.ajax({
			type: "GET",
			url: "api/events/" + id,
			datatype: "json",
			success: function(response) {
				var event = {
					id: id,
					title: response.title,
					author: response.author,
					content: response.content,
					latitude: response.latitude,
					longitude: response.longitude,
					commentCount: response.commentcount
				};

				Utils.reverseGeocode(event.latitude, event.longitude, function(results) {
					event.location = (results[1]) ? results[1].formatted_address : "Try another address maybe?";

					var template = $("#event-header-template").html();
					$("#event-header").html(Mustache.render(template, event));

					template = $("#event-content-template").html();
					$("#event-content-wrapper").html(Mustache.render(template, event));

					if (LoginHandler.isLogedIn()) {
						$("#event-submit").show();
						$("#event-more").show();
					} else {
						$("#event-submit").hide();
						$("#event-more").hide();
					}

					Overlays.toggleEventPane();
				});


			},
			statusCode: {
				404: function() {
					Overlays.showErrorDialog("Uh oh, it seems that this event no longer exists!");
				}
			}
		});
	};


	var viewComments = function(id) {
		$.ajax({
			type: "GET",
			url: "api/events/" + eventId + "/comments",
			datatype: "json",
			success: function(response) {
				var comments = {
					id: response.commentsId,
					author: response.author,
					content: response.content,
					positiveVotes: response.positiveVotes,
					negativeVotes: response.negativeVotes,
					parentId: response.parentId
				};

				var template = $("#comments-template").html();
				$("#comments-content").html(Mustache.render(template, comments));
			}
		});
	};


	return {
		viewEvent: viewEvent,
		viewComments: viewComments
	};
}());

// () -> ()
// Toggles the add user to event component
function toggleAddUser() {
	var elem = document.getElementById('componente-usuario');
	elem.style.display = (elem.style.display === 'none') ? 'block' : 'none';
}

// Event -> ()
// Cleans up the comment textarea
function cleanupCommentOnEnter(e) {
	var ENTER_KEY_CODE = 13;
	e = e || window.event;
	if (e.keyCode === ENTER_KEY_CODE) {
		document.getElementById('body-comment').value = "";
	}
}

// () -> ()
// Cleans up the comment textarea
function cleanupComment() {
	document.getElementById('body-comment').value = "";
}
