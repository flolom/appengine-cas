var progressBar='<div id="floatingCirclesG">' +
'<div class="f_circleG" id="frotateG_01"></div>'+
'<div class="f_circleG" id="frotateG_02"></div>'+
'<div class="f_circleG" id="frotateG_03"></div>'+
'<div class="f_circleG" id="frotateG_04"></div>'+
'<div class="f_circleG" id="frotateG_05"></div>'+
'<div class="f_circleG" id="frotateG_06"></div>'+
'<div class="f_circleG" id="frotateG_07"></div>'+
'<div class="f_circleG" id="frotateG_08"></div>'+
'</div>';

function CasLoginCtrl($scope, $http) {
	
	$scope.submit = function () {
		$scope.progressbar = progressBar;
		
		var params = {
				'casurl': $scope.casurl, 
				'serviceurl':$scope.serviceurl,
				'email':$scope.email,
				'password':$scope.password
		};
		
		$http.get('/casloginws', {'params':params}).success(function (data) {
			$scope.progressbar = "";
			$scope.response = data;
		}).error(function() {
			$scope.progressbar = "";
			$scope.response = "An error occured. Please try again";
		});
		
	};
	
}

function MoodleLoginCtrl($scope, $http) {
	$scope.submit = function () {
		$scope.progressbar = progressBar;
		
		var params = {
				'casurl': $scope.casurl, 
				'serviceurl':$scope.serviceurl,
				'email':$scope.email,
				'password':$scope.password
		};
		
		$http.get('/moodlecasloginws', {'params':params}).success(function (data) {
			$scope.progressbar = "";
			$scope.response = data;
		}).error(function() {
			$scope.progressbar = "";
			$scope.response = "An error occured. Please try again";
		});
		
	};
}