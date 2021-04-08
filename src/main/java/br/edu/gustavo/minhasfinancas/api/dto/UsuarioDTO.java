package br.edu.gustavo.minhasfinancas.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private String email;
    private String nome;
    private String senha;
}
