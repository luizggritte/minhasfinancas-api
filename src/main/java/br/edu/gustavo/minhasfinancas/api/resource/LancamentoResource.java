package br.edu.gustavo.minhasfinancas.api.resource;

import br.edu.gustavo.minhasfinancas.api.dto.AtualizaStatusDTO;
import br.edu.gustavo.minhasfinancas.api.dto.LancamentoDTO;
import br.edu.gustavo.minhasfinancas.exception.RegraNegocioException;
import br.edu.gustavo.minhasfinancas.model.entity.Lancamento;
import br.edu.gustavo.minhasfinancas.model.entity.Usuario;
import br.edu.gustavo.minhasfinancas.model.enums.StatusLancamento;
import br.edu.gustavo.minhasfinancas.model.enums.TipoLancamento;
import br.edu.gustavo.minhasfinancas.service.LancamentoService;
import br.edu.gustavo.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {

    private final LancamentoService service;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "tipo", required = false) TipoLancamento tipo,
            @RequestParam(value = "status", required = false) StatusLancamento status,
            @RequestParam("usuario") Long idUsuario
    ) {
        Lancamento lancamentoFiltro = Lancamento.builder()
                .descricao(descricao)
                .ano(ano)
                .mes(mes)
                .tipo(tipo)
                .status(status)
                .build();

        Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);

        if (usuario.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Não foi possível realizar a consulta. Usuario não encontrado para o Id informado");
        } else {
            lancamentoFiltro.setUsuario(usuario.get());
        }

        List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);

        return ResponseEntity.ok(lancamentos);
    }

    @GetMapping("{id}")
    public ResponseEntity buscarPorId(@PathVariable("id") Long id) {
        return service.obterPorId(id)
                .map(lancamento -> new ResponseEntity(converterLancamentoParaLancamentoDTO(lancamento), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento entidade = converterLancamentoDTOParaLancamento(dto);

            entidade = service.salvar(entidade);

            return new ResponseEntity(entidade, HttpStatus.CREATED);
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        return service.obterPorId(id).map(entidade -> {
            try {
                Lancamento lancamento = converterLancamentoDTOParaLancamento(dto);

                lancamento.setId(entidade.getId());

                service.atualizar(lancamento);

                return ResponseEntity.ok(lancamento);
            } catch (RegraNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(
                () -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST)
        );
    }

    @PutMapping("{id}/atualiza-status")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
        return service.obterPorId(id).map(entidade -> {
            try {
                StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());

                entidade.setStatus(statusSelecionado);

                service.atualizar(entidade);

                return ResponseEntity.ok(entidade);
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body("Não foi possível atualizar o status do lançamento, envie um status válido");
            } catch (RegraNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("{id}")
    private ResponseEntity deletar(@PathVariable("id") Long id) {
        return service.obterPorId(id).map(entidade -> {
            service.deletar(entidade);

            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }).orElseGet(
                () -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST)
        );
    }

    private LancamentoDTO converterLancamentoParaLancamentoDTO(Lancamento lancamento) {
        return LancamentoDTO.builder()
                .id(lancamento.getId())
                .descricao(lancamento.getDescricao())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .valor(lancamento.getValor())
                .status(lancamento.getStatus().name())
                .tipo(lancamento.getTipo().name())
                .usuario(lancamento.getUsuario().getId())
                .build();
    }

    private Lancamento converterLancamentoDTOParaLancamento(LancamentoDTO dto) {
        Lancamento lancamento = new Lancamento();

        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setMes(dto.getMes());
        lancamento.setAno(dto.getAno());
        lancamento.setValor(dto.getValor());

        Usuario usuario = usuarioService
                .obterPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuario não encontrado para o Id informado"));

        lancamento.setUsuario(usuario);

        if (dto.getTipo() != null) {
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        }

        if (dto.getStatus() != null) {
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        }

        return lancamento;
    }
}
