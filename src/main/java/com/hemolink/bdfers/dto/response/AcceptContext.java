package com.hemolink.bdfers.dto.response;

/**
 * Request summary shown on the donor's health-check page. Contains no
 * requester identity or contact details.
 */
public class AcceptContext {

    private final Long matchId;
    private final Long requestId;
    private final String bloodGroupLabel;
    private final String city;
    private final String hospitalName;
    private final String urgencyLabel;
    private final int remainingUnits;

    public AcceptContext(Long matchId, Long requestId, String bloodGroupLabel,
                         String city, String hospitalName, String urgencyLabel,
                         int remainingUnits) {
        this.matchId = matchId;
        this.requestId = requestId;
        this.bloodGroupLabel = bloodGroupLabel;
        this.city = city;
        this.hospitalName = hospitalName;
        this.urgencyLabel = urgencyLabel;
        this.remainingUnits = remainingUnits;
    }

    public Long getMatchId() { return matchId; }
    public Long getRequestId() { return requestId; }
    public String getBloodGroupLabel() { return bloodGroupLabel; }
    public String getCity() { return city; }
    public String getHospitalName() { return hospitalName; }
    public String getUrgencyLabel() { return urgencyLabel; }
    public int getRemainingUnits() { return remainingUnits; }
}
