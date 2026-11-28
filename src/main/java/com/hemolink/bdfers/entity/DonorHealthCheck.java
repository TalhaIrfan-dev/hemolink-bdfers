package com.hemolink.bdfers.entity;

import com.hemolink.bdfers.enums.AdviceLevel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Health check entered by a donor when accepting a request (D-01).
 * Advisory only. Never shown to requesters (NFR-03, D-04).
 */
@Entity
@Table(name = "donor_health_checks")
public class DonorHealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private DonorMatch match;

    @Column(name = "haemoglobin_g_dl", nullable = false, precision = 4, scale = 1)
    private BigDecimal haemoglobinGdl;

    @Column(name = "cbc_date", nullable = false)
    private LocalDate cbcDate;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "units_offered", nullable = false)
    private Integer unitsOffered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdviceLevel advice;

    @Column(name = "advice_reasons", length = 1000)
    private String adviceReasons;

    @Column(name = "accepted_against_advice", nullable = false)
    private boolean acceptedAgainstAdvice;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public DonorMatch getMatch() { return match; }
    public BigDecimal getHaemoglobinGdl() { return haemoglobinGdl; }
    public LocalDate getCbcDate() { return cbcDate; }
    public BigDecimal getWeightKg() { return weightKg; }
    public Integer getUnitsOffered() { return unitsOffered; }
    public AdviceLevel getAdvice() { return advice; }
    public String getAdviceReasons() { return adviceReasons; }
    public boolean isAcceptedAgainstAdvice() { return acceptedAgainstAdvice; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setMatch(DonorMatch match) { this.match = match; }
    public void setHaemoglobinGdl(BigDecimal haemoglobinGdl) { this.haemoglobinGdl = haemoglobinGdl; }
    public void setCbcDate(LocalDate cbcDate) { this.cbcDate = cbcDate; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public void setUnitsOffered(Integer unitsOffered) { this.unitsOffered = unitsOffered; }
    public void setAdvice(AdviceLevel advice) { this.advice = advice; }
    public void setAdviceReasons(String adviceReasons) { this.adviceReasons = adviceReasons; }
    public void setAcceptedAgainstAdvice(boolean acceptedAgainstAdvice) { this.acceptedAgainstAdvice = acceptedAgainstAdvice; }
}
