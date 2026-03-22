package com.mechama.service;

import com.mechama.dto.UserLoginRequest;
import com.mechama.dto.UserRegisterRequest;
import com.mechama.dto.UserResponse;
import com.mechama.model.User;
import com.mechama.model.UserType;
import com.mechama.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de usuários: cadastro, login e mapeamento para DTO.
 *
 * Integração com Tarefa 2: ao cadastrar PROVIDER, cria ProviderProfile vazio automaticamente.
 * Integração futura (Tarefa 7): ao cadastrar qualquer usuário, criar Wallet associada.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * @Lazy evita dependência circular:
     * UserService → ProviderProfileService → UserRepository → UserService
     */
    @Lazy
    private final ProviderProfileService providerProfileService;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .type(request.getType())
                .active(true)
                .build();

        userRepository.save(user);

        // Tarefa 2: cria perfil vazio automaticamente para prestadores
        if (user.getType() == UserType.PROVIDER) {
            providerProfileService.createEmptyProfile(user);
        }

        // TODO (Tarefa 7): criar Wallet vinculada ao usuário

        String token = jwtService.generateToken(user);
        return toResponse(user, token);
    }

    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Conta desativada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);
        return toResponse(user, token);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return toResponse(user, null);
    }

    // ─── Mapeamento interno ─────────────────────────────────────────────────────

    private UserResponse toResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .type(user.getType())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }
}
