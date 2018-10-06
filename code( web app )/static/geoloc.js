console.log('ayyeh ');
var map,infowindow,request,service;
var markers=[];
function initialize(lat, lng) {
    //console.log("i am in");
    var center = new google.maps.LatLng(lat,lng);
    map=new google.maps.Map(document.getElementById('map'),{
        center:center,
        zoom:13
    });
    request={
        location: center,
        radius: 8047,
        types: ['hospital']
    };
    infowindow = new google.maps.InfoWindow();
    var service = new google.maps.places.PlacesService(map);
    service.nearbySearch(request,callback);
    google.maps.event.addListener(map,'rightclick',function(event){
        map.setCenter(event.latLng)
        clearResults(markers)
        var request={
            location:event.latLng,
            radius:8047,
            types:['hospital']
        };
        service.nearbySearch(request,callback);
    })
}
function callback(results,status){
    if(status==google.maps.places.PlacesServiceStatus.OK){
        for(var i=0;i<results.length;i++){
            markers.push(createMarker(results[i]));
        }
    }
}
function createMarker(place){
    var placeLoc=place.geometry.location;
    var marker=new google.maps.Marker({
        map:map,
        position:place.geometry.location
    });
    google.maps.event.addListener(marker,'click',function(){
        infowindow.setContent(place.name);
        infowindow.open(map,this);
    });
    return marker;
}
function clearResults(markers) {
    for(var m in markers){
        markers[m].setMap(null)
    }
    markers=[]
}
function showp(pos){
    var lat=pos.coords.latitude,
        lng=pos.coords.longitude;
        initialize(lat,lng);

}

document.getElementById('get_location').onclick=function(){
    if (navigator.geolocation) {
        //console.log("show up");
        navigator.geolocation.getCurrentPosition(showp);
    } else {
        document.getElementById('map').innerHTML = "Geolocation is not supported by this browser.";
        //console.log("didnt show up");
        return false;
    }
}