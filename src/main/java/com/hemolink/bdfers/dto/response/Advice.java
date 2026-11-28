package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.AdviceLevel;

import java.util.List;

/**
 * Advisory eligibility result (D-01). {@code reasons} explain a
 * "Not advised" result; {@code facts} are neutral information such as
 * "Your last donation was 40 days ago". Advice never blocks a donor.
 */
public class Advice {

    private final AdviceLevel level;
    private final List<String> reasons;
    private final List<String> facts;

    public Advice(AdviceLevel level, List<String> reasons, List<String> facts) {
        this.level = level;
        this.reasons = List.copyOf(reasons);
        this.facts = List.copyOf(facts);
    }

    public AdviceLevel getLevel() { return level; }
    public List<String> getReasons() { return reasons; }
    public List<String> getFacts() { return facts; }

    public boolean isOk() {
        return level == AdviceLevel.OK;
    }

    /** Reasons joined into one line, for storage. */
    public String reasonsAsText() {
        return reasons.isEmpty() ? null : String.join(" | ", reasons);
    }
}
