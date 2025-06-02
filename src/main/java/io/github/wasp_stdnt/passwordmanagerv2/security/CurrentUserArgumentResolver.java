package io.github.wasp_stdnt.passwordmanagerv2.security;

import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;

import java.util.Optional;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    public CurrentUserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    @Transactional
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("preferred_username");
        if (email == null || email.isBlank()) {
            return null;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get().getId();
        }

        User newUser = new User();
        newUser.setEmail(email);
        String givenName = jwt.getClaimAsString("given_name");
        newUser.setName(givenName != null ? givenName : email);

        newUser.setPasswordHash("<auto-provisioned>");
        User saved = userRepository.save(newUser);
        return saved.getId();
    }
}
