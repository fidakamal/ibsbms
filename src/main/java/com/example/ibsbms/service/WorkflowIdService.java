package com.example.ibsbms.service;

import org.springframework.stereotype.Service;

@Service
public class WorkflowIdService {

    /*
     * Temporary implementation.
     *
     * These IDs must eventually come from the actual
     * Oracle/database mechanism used by IBSBMS.
     *
     * Do NOT use these values for production Oracle data.
     */

    public String generateChangeId() {
        return "TEMP-" + System.currentTimeMillis();
    }

    public String generateFolioBo() {
        throw new IllegalStateException(
                "FOLIO_BO generation is not configured yet. "
                        + "Connect to the office Oracle database and configure the "
                        + "existing Folio generation mechanism."
        );
    }

    public Long generateRequestId() {
        throw new IllegalStateException(
                "REQUEST_ID generation is not configured yet."
        );
    }

    public Long generateActionId() {
        throw new IllegalStateException(
                "ACTION_ID generation is not configured yet."
        );
    }
}