package br.edu.gustavo.minhasfinancas.service;

import br.edu.gustavo.minhasfinancas.exception.RegraNegocioException;
import br.edu.gustavo.minhasfinancas.model.entity.Lancamento;
import br.edu.gustavo.minhasfinancas.model.entity.Usuario;
import br.edu.gustavo.minhasfinancas.model.enums.StatusLancamento;
import br.edu.gustavo.minhasfinancas.model.repository.LancamentoRepository;
import br.edu.gustavo.minhasfinancas.model.repository.LancamentoRepositoryTest;
import br.edu.gustavo.minhasfinancas.service.impl.LancamentoServiceImpl;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImpl service;

    @MockBean
    LancamentoRepository repository;

    @Test
    public void deveSalvarUmLancamento() {
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

        Mockito.doNothing().when(service).validar(lancamentoASalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();

        lancamentoSalvo.setId(1l);

        Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        Lancamento lancamento = service.salvar(lancamentoASalvar);

        assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
        assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

        Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);

        catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);

        Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
    }

    @Test
    public void deveAtualizarUmLancamento() {
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();

        lancamentoSalvo.setId(1l);

        Mockito.doNothing().when(service).validar(lancamentoSalvo);

        Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        service.atualizar(lancamentoSalvo);

        Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
    }

    @Test
    public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        catchThrowableOfType(() -> service.atualizar(lancamento), NullPointerException.class);

        Mockito.verify(repository, Mockito.never()).save(lancamento);
    }

    @Test
    public void deveDeletarUmLancamento() {
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        lancamento.setId(1l);

        service.deletar(lancamento);

        Mockito.verify(repository).delete(lancamento);
    }

    @Test
    public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);

        Mockito.verify(repository, Mockito.never()).delete(lancamento);
    }

    @Test
    public void deveFiltrarLancamentos() {
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        lancamento.setId(1l);

        List<Lancamento> lista = Arrays.asList(lancamento);

        Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);

        List<Lancamento> resultado = service.buscar(lancamento);

        assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
    }

    @Test
    public void deveAtualizarOStatusDeUmLancamento() {
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        lancamento.setId(1l);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;

        Mockito.doReturn(lancamento).when(service).atualizar(lancamento);

        service.atualizarStatus(lancamento, novoStatus);

        assertThat(lancamento.getStatus()).isEqualTo(novoStatus);

        Mockito.verify(service).atualizar(lancamento);
    }

    @Test
    public void deveObterUmLancamentoPorId() {
        Long id = 1l;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        Optional<Lancamento> resultado = service.obterPorId(id);

        assertThat(resultado.isPresent()).isTrue();
    }

    @Test
    public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
        Long id = 1l;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Lancamento> resultado = service.obterPorId(id);

        assertThat(resultado.isEmpty()).isTrue();
    }

    @Test
    public void deveLancarErrosAoValidarUmLancamento() {
        Lancamento lancamento = new Lancamento();

        Throwable erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida");

        lancamento.setDescricao("");

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida");

        lancamento.setDescricao("Salario");

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido");

        lancamento.setMes(0);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido");

        lancamento.setMes(13);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido");

        lancamento.setMes(1);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido");

        lancamento.setAno(202);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido");

        lancamento.setAno(2021);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário");

        lancamento.setUsuario(new Usuario());

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário");

        lancamento.getUsuario().setId(1l);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido");

        lancamento.setValor(BigDecimal.ZERO);

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido");

        lancamento.setValor(BigDecimal.valueOf(500));

        erro = catchThrowable(() -> service.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Tipo de Lançamento");
    }
}
