package com.hemolink.bdfers.dto.response;

import com.hemolink.bdfers.enums.DonorRequestTab;

import java.util.List;

/** Donor's matched requests for one tab, with a count per tab (Module 3.5). */
public class DonorRequestsView {

    private final DonorRequestTab tab;
    private final List<DonorRequestRow> rows;
    private final long waiting;
    private final long accepted;
    private final long past;

    public DonorRequestsView(DonorRequestTab tab, List<DonorRequestRow> rows,
                             long waiting, long accepted, long past) {
        this.tab = tab;
        this.rows = List.copyOf(rows);
        this.waiting = waiting;
        this.accepted = accepted;
        this.past = past;
    }

    public DonorRequestTab getTab() { return tab; }
    public List<DonorRequestRow> getRows() { return rows; }
    public List<DonorRequestTab> getTabs() { return List.of(DonorRequestTab.values()); }

    public long count(DonorRequestTab t) {
        return switch (t) {
            case WAITING -> waiting;
            case ACCEPTED -> accepted;
            case PAST -> past;
        };
    }

    public boolean isNoMatchesAtAll() {
        return waiting + accepted + past == 0;
    }
}
