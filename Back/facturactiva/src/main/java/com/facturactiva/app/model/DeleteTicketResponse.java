package com.facturactiva.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteTicketResponse {
    private Integer success;
    private String message;
    private Integer idTicket;
}