package br.ueg.tc.ueg_provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TcDTO (@JsonProperty("tcu_id") String tcuId,
                     @JsonProperty("data_acompanhamento") String followupDate,
                     @JsonProperty("horas") String hours,
                     @JsonProperty("assuntos_discutidos") String details,
                     @JsonProperty("tcp_id") String tcpId,
                     @JsonProperty("status") String status){
}
