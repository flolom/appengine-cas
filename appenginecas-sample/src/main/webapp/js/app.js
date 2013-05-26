angular.module('appengine-cas', [])
	.config(['$routeProvider', function($routeProvider) {
	
		$routeProvider
			.when('/caslogin', {templateUrl: 'partials/caslogin.html', controller: CasLoginCtrl})
			.when('/moodlelogin', {templateUrl: 'partials/moodlelogin.html', controller: MoodleLoginCtrl})
			.otherwise({redirectTo: '/caslogin'});
	}]);