package br.edu.gustavo.minhasfinancas.api.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoDTO {

    private Long id;
    private String descricao;
    private Integer mes;
    private Integer ano;
    private BigDecimal valor;
    private Long usuario;
    private String tipo;
    private String status;
}
