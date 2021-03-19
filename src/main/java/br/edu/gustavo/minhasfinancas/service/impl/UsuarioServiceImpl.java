package br.edu.gustavo.minhasfinancas.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.gustavo.minhasfinancas.exception.ErroAutenticacao;
import br.edu.gustavo.minhasfinancas.exception.RegraNegocioException;
import br.edu.gustavo.minhasfinancas.model.entity.Usuario;
import br.edu.gustavo.minhasfinancas.model.repository.UsuarioRepository;
import br.edu.gustavo.minhasfinancas.service.UsuarioService;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	private UsuarioRepository repository;

	public UsuarioServiceImpl(UsuarioRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> usuario = repository.findByEmail(email);

		if (usuario.isEmpty()) {
			throw new ErroAutenticacao("Usuário não encontrado para o email informado");
		}

		if (!usuario.get().getSenha().equals(senha)) {
			throw new ErroAutenticacao("Senha inválida");
		}

		return usuario.get();
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());

		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		boolean existe = repository.existsByEmail(email);
		
		if (existe) {
			throw new RegraNegocioException("Já existe um usuário cadastrado com este email");
		}
	}
}
