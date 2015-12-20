package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

public class Locations {
    private String latitude;
    private String longitude;
    private String time;

    public Locations(String latitude, String longitude, String time){
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String get_latitude(){
        return this.latitude;
    }

    public String get_longitude(){
        return this.longitude;
    }

    public String get_time(){
        return this.time;
    }
}
