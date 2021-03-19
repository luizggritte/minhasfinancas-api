package br.edu.gustavo.minhasfinancas.exception;

public class ErroAutenticacao extends RuntimeException {

    public ErroAutenticacao(String mensagem) {
        super(mensagem);
    }
}
