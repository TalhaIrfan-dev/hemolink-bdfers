package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.entity.BloodRequest;
import com.hemolink.bdfers.enums.RequestListTab;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The requester's own requests for one status tab, plus a count per tab. */
public class MyRequestsView {

    private final RequestListTab tab;
    private final List<BloodRequest> requests;
    private final Map<RequestListTab, Long> counts;

    public MyRequestsView(RequestListTab tab, List<BloodRequest> requests,
                          Map<RequestListTab, Long> counts) {
        this.tab = tab;
        this.requests = List.copyOf(requests);
        EnumMap<RequestListTab, Long> copy = new EnumMap<>(RequestListTab.class);
        copy.putAll(counts);
        this.counts = copy;
    }

    public RequestListTab getTab() { return tab; }
    public List<BloodRequest> getRequests() { return requests; }
    public List<RequestListTab> getTabs() { return List.of(RequestListTab.values()); }

    public long count(RequestListTab t) {
        return counts.getOrDefault(t, 0L);
    }

    /** True when the user has no requests at all (not just none in this tab). */
    public boolean isNoRequestsAtAll() {
        return count(RequestListTab.ALL) == 0;
    }
}
