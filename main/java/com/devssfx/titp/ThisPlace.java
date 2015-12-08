package com.devssfx.titp;

public class ThisPlace {

    public Integer PlaceDeviceId;
    public String PlaceId; //id on server
    public String PlaceCode; //used as reference
    //public String PlaceUserId; //user email or phone (or whatever)
    public String PlaceName;
    public Integer PlaceDate;
    public Integer CreateDate;
    public String PlaceAddress;
    public String PlaceLat;
    public String PlaceLong;
    public Integer ModCount;
    public Integer IsAdmin;

    public boolean IsLocationSet(){
        boolean rtn = false;
        if(
            (PlaceAddress != null && PlaceAddress.length() > 0)
            || (PlaceLat != null && PlaceLat.length() > 0)){
            rtn = true;
        }

        return rtn;
    }

}
